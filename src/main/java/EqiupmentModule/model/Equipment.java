package EqiupmentModule.model;

import StorageModule.model.Position;

public abstract class Equipment {

    public enum EquipmentState {
        IDLE, MOVING, BUSY, CHARGING, STOPPED, ERROR
    }

    private final String id;
    private Position position;
    private EquipmentState state;
    private double speed;
    private double batteryLevel;
    private double chargingTime = 0;

    protected Equipment(String id, Position position, double speed, double batteryLevel) {
        this.id = id;
        this.position = position;
        this.speed = speed;
        this.batteryLevel = batteryLevel;
        this.state = EquipmentState.IDLE;
    }

    // --- getters ---
    public String getId() {
        return id;
    }

    public Position getPosition() {
        return position;
    }

    public EquipmentState getState() {
        return state;
    }

    public double getSpeed() {
        return speed;
    }

    public double getBatteryLevel() {
        return batteryLevel;
    }

    public double getChargingTime() {
        return chargingTime;
    }

    public void setChargingTime(double chargingTime) {
        this.chargingTime = chargingTime;
    }

    public void setBatteryLevel(double batteryLevel) {

        // Ensure battery level is between 0 and 100
        this.batteryLevel = Math.max(0, Math.min(batteryLevel, 100));
    }

    public void setPosition(Position position) {
        this.position = position;
    }

    public void setState(EquipmentState state) {
        this.state = state;
    }

    // public abstract void charge(ChargingStation c);
}
