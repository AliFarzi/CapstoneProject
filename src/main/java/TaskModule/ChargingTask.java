package TaskModule;

import EqiupmentModule.model.ChargingStation;
import EqiupmentModule.model.Equipment;
import EqiupmentModule.service.EquipmentManager;
import LogingModule.LoggingManager;
import LogingModule.LogLevel;

public class ChargingTask implements Runnable {
    private final String id;
    private final EquipmentManager equipmentManager;
    private final ChargingStation chargingStation;
    private final Equipment equipment;
    private final LoggingManager logger = LoggingManager.getInstance();

    public ChargingTask(EquipmentManager equipmentManager, ChargingStation chargingStation, Equipment equipment, String id) {

        this.equipmentManager = equipmentManager;
        this.chargingStation = chargingStation;
        this.equipment = equipment;
        this.id = id;
    }

    @Override
    public void run() {

        System.out.println("Starting Charging Task for Equipment ID: " + equipment.getId());
        // Step 1: Try to start charging
        try {
            logger.log("Starting Charging Process for Equipment: " + equipment.getId(), LogLevel.INFO, id);
            equipmentManager.sendToCharge(equipment.getId(), chargingStation);

        } catch (Exception e) {
            logger.log("Charging initialization failed for Equipment: " + equipment.getId() + " - " + e.getMessage(), LogLevel.ERROR, id);
             return; // Exit if sending to charge failed
        }

        // Step 2: Simulate charging progress and stop correctly
        try {

            double steps = Math.ceil((100 - equipment.getBatteryLevel()) / 10.0);
            double expectedtime = 1000 * steps; // Total expected time in ms

            equipment.setChargingTime(expectedtime);
            System.out.println("Equipment ID: " + equipment.getId() + " Expected Charging Time (ms): " + expectedtime);

             while(equipment.getBatteryLevel() < 100) {
                logger.log("Equipment: " + equipment.getId() + " Charging... Current Level: " + equipment.getBatteryLevel() + "%", LogLevel.INFO, id);
                Thread.sleep(1000); // Simulate time passing
                equipment.setBatteryLevel(equipment.getBatteryLevel() + 10);
                equipment.setChargingTime(equipment.getChargingTime() - 1000);
                System.out.println("Equipment ID: " + equipment.getId() + " Expected Charging Time (ms): " + equipment.getChargingTime());
            }

            logger.log("Equipment: " + equipment.getId() + " Fully Charged!", LogLevel.INFO, id);

        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            logger.log("Charging interrupted for Equipment: " + equipment.getId(),
                       LogLevel.WARN, id);
        
        } catch (Exception e) {
            logger.log("Unexpected error during charging for Equipment: " + equipment.getId()
                    + " - " + e.getMessage(), LogLevel.ERROR, id);
        } finally{

            try {
                System.out.println("Releasing Equipment ID: " + equipment.getId() + " from Charging Station.");

                equipmentManager.releaseFromCharge(equipment.getId(), chargingStation);
                logger.log("Equipment: " + equipment.getId() + " Released from Charging Station.", LogLevel.INFO, id);
                equipment.setChargingTime(0);

            } catch (Exception e) {

                logger.log("Error occurred while releasing Equipment ID: " + equipment.getId() + " from charge - " + e.getMessage(), LogLevel.ERROR, id);

            }
        }


    }
}
