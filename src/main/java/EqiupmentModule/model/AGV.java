package EqiupmentModule.model;

import StorageModule.model.Position;
import LogingModule.LoggingManager;
import LogingModule.LogLevel;

public class AGV extends Equipment {
    private final double maxLoadWeight;
    private final double range; // abstract range units
    private LoggingManager logger = LoggingManager.getInstance();

    public AGV(String id, Position position, double speed, double batteryLevel,
            double maxLoadWeight, double range) {
        super(id, position, speed, batteryLevel);
        this.maxLoadWeight = maxLoadWeight;
        this.range = range;
        logger.log(id + " created at position " + position, LogLevel.INFO, id);
    }

    public double getMaxLoadWeight() {
        return maxLoadWeight;
    }

    public double getRange() {
        return range;
    }

    // needs to be changed later
    // @Override
    // public void charge(ChargingStation c) {
    //     if (c.startChargingSteps(this)) {
    //         this.setState(EquipmentState.CHARGING);
    //     }
    // }
}
