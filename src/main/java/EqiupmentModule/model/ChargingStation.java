package EqiupmentModule.model;

import java.util.ArrayList;

import StorageModule.model.Position;

public class ChargingStation {
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

    public void assignEquipment(Equipment e) {
        if (e != null && !assignedEquipments.contains(e)) {
            assignedEquipments.add(e);
        }
    }

    public void unassignEquipment(Equipment e) {
        assignedEquipments.remove(e);
    }

    public double calculateQueueTime() {
        double totalTime = 0;
        if(assignedEquipments.isEmpty()) {
            System.out.println("Charging Station ID: " + id + " No AGV, Current Queue Time (ms): " + queueTime);
            return 0;
        }
        for (Equipment e : assignedEquipments) {
            totalTime += e.getChargingTime();
        }
        this.queueTime = totalTime;
        System.out.println("Charging Station ID: " + id + " Current Queue Time (ms): " + queueTime);
        return queueTime;
    }
    public boolean startCharging(Equipment e) {
        if (occupied)
            return false;
        occupied = true;
        equipmentId = e.getId();
        return true;
    }

    public void stopCharging() {
        occupied = false;
        equipmentId = null;
    }
}
