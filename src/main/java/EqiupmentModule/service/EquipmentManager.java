package EqiupmentModule.service;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import EqiupmentModule.model.*;
import LogingModule.LoggingManager;
import LogingModule.LogLevel;
import EqiupmentModule.service.exceptions.*;

public class EquipmentManager {

    private final List<Equipment> equipmentList = new ArrayList<>();
    private final LoggingManager logger = LoggingManager.getInstance();

    public EquipmentManager() {
        logger.log("EquipmentManager initialized", LogLevel.INFO, "EquipmentManager");
    }

    public void addEquipment(Equipment e) {
        synchronized (equipmentList) {
            if (e == null)
                return;
            equipmentList.add(e);
            logger.log("Added " + e.getId(), LogLevel.INFO, "EquipmentManager");
        }
    }

    public List<Equipment> getAll() {
        return new ArrayList<>(equipmentList);
    }

    public List<Equipment> getAvailableEquipment() {
        List<Equipment> available = new ArrayList<>();
        for (Equipment e : equipmentList) {
            if (e != null && e.getState() == Equipment.EquipmentState.IDLE) {
                available.add(e);
            }
        }
        return available;
    }

    private Equipment requireById(String id) throws EquipmentNotFoundException {
        if (id == null || id.isBlank())
            throw new EquipmentNotFoundException("Equipment id is empty");
        for (Equipment e : equipmentList) {
            if (e != null && id.equals(e.getId()))
                return e;
        }
        throw new EquipmentNotFoundException("Equipment '" + id + "' not found");
    }

    public void assignToTask(String id) throws EquipmentOperationException {
        try {
            Equipment e = requireById(id);

            Equipment.EquipmentState st = e.getState();
            if (st == Equipment.EquipmentState.BUSY
                    || st == Equipment.EquipmentState.CHARGING
                    || st == Equipment.EquipmentState.ERROR) {
                throw new EquipmentUnavailableException("Equipment '" + id + "' not available: " + st);
            }
            if (e.getBatteryLevel() < 10.0) {
                throw new InvalidEquipmentStateException("Battery too low for assignment");
            }

            e.setState(Equipment.EquipmentState.BUSY);
            logger.log("Assigned " + id, LogLevel.INFO, "EquipmentManager");

        } catch (EquipmentNotFoundException
                | EquipmentUnavailableException
                | InvalidEquipmentStateException ex) {
            logger.log(ex.getMessage(), LogLevel.ERROR, "EquipmentManager");
            throw ex;
        }
    }

    public void release(String id) throws EquipmentOperationException {
        Equipment e = requireById(id);
        if (e.getState() == Equipment.EquipmentState.IDLE ) {
            throw new InvalidEquipmentStateException("Only BUSY equipment can be released");
        }
        e.setState(Equipment.EquipmentState.IDLE);
        logger.log("Released " + id, LogLevel.INFO, "EquipmentManager");
    }

    public void sendToCharge(Equipment e, ChargingStation station) throws EquipmentOperationException {

        try {
            if (station == null){
                throw new IllegalArgumentException("ChargingStation is null");
            }
            synchronized (e) {
                if (e.getState() == Equipment.EquipmentState.BUSY ) {
                throw new EquipmentUnavailableException("Busy equipment cannot start charging");
                }

                if (e.getState() == Equipment.EquipmentState.CHARGING) {
                    throw new InvalidEquipmentStateException("Equipment is already charging");
                }
                e.setState(Equipment.EquipmentState.CHARGING);
                station.assignEquipment(e);
                logger.log("Charging started for " + e.getId(), LogLevel.INFO, "EquipmentManager");
        }
            

        } catch (IllegalArgumentException iae) {
            throw new InvalidEquipmentStateException(iae.getMessage());
        } catch (EquipmentOperationException known) {
            throw known;
        } catch (RuntimeException unexpected) {

            throw new EquipmentOperationException("Unexpected error while sending '" + e.getId() + "' to charge", unexpected);
        }
    }

    public void releaseFromCharge(Equipment e, ChargingStation station) throws EquipmentOperationException {
        synchronized (e) {
            if (e.getState() != Equipment.EquipmentState.CHARGING ) {
            throw new InvalidEquipmentStateException("Only CHARGING equipment can be released from charge");
            }
            e.setState(Equipment.EquipmentState.IDLE);
            station.unassignEquipment(e);
            logger.log("Released from charge " + e.getId(), LogLevel.INFO, "EquipmentManager");
    }
}

    public void exportEquipmentCsv(Path out) throws EquipmentOperationException {
        try (BufferedWriter bw = Files.newBufferedWriter(out)) {
            bw.write("id,type,state,battery\n");
            for (Equipment e : equipmentList) {
                if (e == null)
                    continue;
                bw.write(String.format("%s,%s,%s,%.1f%n",
                        e.getId(),
                        e.getClass().getSimpleName(),
                        e.getState(),
                        e.getBatteryLevel()));
            }
            logger.log("Exported CSV to " + out, LogLevel.INFO, "EquipmentManager");
        } catch (IOException io) {
            throw new ResourceAccessException("Failed to write CSV: " + out, io);
        }
    }

    public void printEquipmentInfo() {
        System.out.println("\n=== EQUIPMENT INFO ===");
        for (Equipment e : equipmentList) {
            if (e == null)
                continue;
            System.out.println(" " + e.getId()
                    + " | " + e.getClass().getSimpleName()
                    + " | State: " + e.getState()
                    + " | Battery: " + e.getBatteryLevel());
        }
    }
}
