package EqiupmentModule.model;

import StorageModule.model.Position;

public class Shuttle extends Equipment {
    private final double maxLoadWeight;

    public Shuttle(String id, Position position, double speed, double batteryLevel, double maxLoadWeight) {
        super(id, position, speed, batteryLevel);
        this.maxLoadWeight = maxLoadWeight;
    }

    public double getMaxLoadWeight() {
        return maxLoadWeight;
    }

    // needs to be changed later
    // @Override
    // public void charge(ChargingStation c) {
    //     if (c.startCharging(this)) {
    //         this.setState(EquipmentState.CHARGING);
    //     }
    // }
}
