package TaskModule;

import EqiupmentModule.model.ChargingStation;
import EqiupmentModule.model.Equipment;
import EqiupmentModule.service.EquipmentManager;
import EqiupmentModule.service.exceptions.EquipmentChargeFullException;
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

        // Step 1: Try to start charging
        try {
              if (equipment.getBatteryLevel() >= 100.0) {
                System.out.println("Equipment ID: " + equipment.getId() + " Battery is already full.");
                throw new EquipmentChargeFullException("Equipment ID: " + equipment.getId() + " Battery is already full.");
              }

            logger.log("Starting Charging Process for Equipment: " + equipment.getId(), LogLevel.INFO, id);
            equipmentManager.sendToCharge(equipment, chargingStation);

        } catch (EquipmentChargeFullException e) {
            logger.log("Charging failed for Equipment: " + equipment.getId() + " - " + e.getMessage(), LogLevel.ERROR, id);
                return; // Exit if already fully charged

        } catch (Exception e) {
            logger.log("Charging initialization failed for Equipment: " + equipment.getId() + " - " + e.getMessage(), LogLevel.ERROR, id);
             return; // Exit if sending to charge failed
        }

        // Step 2: Simulate charging progress and stop correctly
        try {

            synchronized (chargingStation) {
                while(equipment.getBatteryLevel() < 100) {
                    chargingStation.startChargingSteps(equipment);
                    Thread.sleep(1000); // Simulate time passing
                }
            }
            logger.log("Charging completed for Equipment: " + equipment.getId(), LogLevel.INFO, id);
            

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

                equipmentManager.releaseFromCharge(equipment, chargingStation);
                logger.log("Equipment: " + equipment.getId() + " Released from Charging Station.", LogLevel.INFO, id);
                equipment.setChargingTime(0);

            } catch (Exception e) {

                logger.log("Error occurred while releasing Equipment ID: " + equipment.getId() + " from charge - " + e.getMessage(), LogLevel.ERROR, id);

            }
        }


    }
}
