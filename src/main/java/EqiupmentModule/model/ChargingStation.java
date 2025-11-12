package EqiupmentModule.model;

import java.util.ArrayList;

import LogingModule.LogLevel;
import LogingModule.LoggingManager;
import StorageModule.model.Position;
import EqiupmentModule.service.exceptions.EquipmentChargeFullException;

public class ChargingStation {
    private final LoggingManager logger = LoggingManager.getInstance();
    private final String id;
    private final Position position;
    private final double powerKW;
    private boolean occupied;
    private String equipmentId;
    private ArrayList<Equipment> assignedEquipments = new ArrayList<>();
    private double queueTime = 0;

    public ChargingStation(String id, Position position, double powerKW) {
        this.id = id;
        this.position = position;
        this.powerKW = powerKW;
        this.occupied = false;
        logger.log("ChargingStation " + id + " initialized at position " + position.toString(), LogLevel.INFO, id);
    }

    public String getId() {
        return id;
    }

    public Position getPosition() {
        return position;
    }

    public double getPowerKW() {
        return powerKW;
    }

    public boolean isOccupied() {
        return occupied;
    }

    public String getEquipmentId() {
        return equipmentId;
    }

    public ArrayList<Equipment> getAssignedEquipments() {
        return assignedEquipments;
    }

    public void  assignEquipment(Equipment e) {
        synchronized (assignedEquipments) {
            if (e != null && !assignedEquipments.contains(e)) {
                assignedEquipments.add(e);
            }
        }
    }

    public void unassignEquipment(Equipment e) {
        synchronized (assignedEquipments) {
            assignedEquipments.remove(e);
        }
    }

    public double calculateQueueTime() {
        double totalTime = 0;
        synchronized (assignedEquipments) {
            
             if(assignedEquipments.isEmpty()) {
                System.out.println("Charging Station ID: " + id + " No AGV, Current Queue Time (ms): " + queueTime);
                return 0;
            }
            for (Equipment e : assignedEquipments) {
                totalTime += e.getChargingTime();
            }
            this.queueTime = totalTime;
            return queueTime;
        }
       
    }
    public synchronized void startChargingSteps(Equipment equipment) throws EquipmentChargeFullException {

        synchronized (equipment) {
            if (equipment.getBatteryLevel() >= 100.0) {
                logger.log("Equipment: " + equipment.getId() + " Already Fully Charged!", LogLevel.ERROR, id);
                throw new EquipmentChargeFullException("Equipment ID: " + equipment.getId() + " Battery is already full.");
            }
            double steps = Math.ceil((100 - equipment.getBatteryLevel()) / 10.0);
            double expectedtime = 1000 * steps; // Total expected time in ms
            equipment.setChargingTime(expectedtime);
            System.out.println("Equipment ID: " + equipment.getId() + " Expected Charging Time (ms): " + expectedtime);
            logger.log("Equipment: " + equipment.getId() + " Charging... Current Level: " + equipment.getBatteryLevel() + "%", LogLevel.INFO, id);
            equipment.setBatteryLevel(equipment.getBatteryLevel() + 10);
            equipment.setChargingTime(equipment.getChargingTime() - 1000);
            System.out.println("Equipment ID: " + equipment.getId() + " Expected Charging Time (ms): " + equipment.getChargingTime());
        }
        
        
    }

    public void stopCharging() {
        occupied = false;
        equipmentId = null;
    }
}
