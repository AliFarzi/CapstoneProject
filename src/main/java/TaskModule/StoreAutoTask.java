package TaskModule;

import EqiupmentModule.model.*;
import EqiupmentModule.service.EquipmentManager;
import LoggingModule.LoggingManager;
import LoggingModule.LogLevel;
import StorageModule.model.*;
import StorageModule.service.StorageManager;



public class StoreAutoTask implements Runnable {

    private final String id;
    private final EquipmentManager equipmentManager;
    private final StorageManager storageManager;
    private final Item item;
    private final LoggingManager logger = LoggingManager.getInstance();

    public StoreAutoTask(String id,EquipmentManager equipmentManager, StorageManager storageManager, Item item) {
        this.id = id;
        this.equipmentManager = equipmentManager;
        this.storageManager = storageManager;
        this.item = item;
        logger.log("StoreAutoTask initialized for Item: " + item.getId(), LogLevel.INFO, id);
    }

    @Override
    public void run() {
        // Task logic is executed in the constructor for simplicity
        
        logger.log("Starting Store Auto Task for Item: " + item.getId() + " to Position:First avaliable Cell", LogLevel.INFO, id);
        
        try {
            synchronized (item){
                item.updateStatus(Item.Status.MOVING);
                storageManager.addItem(item);
                Thread.sleep(2000);
                logger.log("Equipment: " + " successfully moved to Position: " + item.getPosition().toString(), LogLevel.INFO, id);
                item.updateStatus(Item.Status.STORED);
            }
            
        } catch (Exception e) {
            logger.log("Store Auto Task failed for Item: " + item.getId() + " - " + e.getMessage(), LogLevel.ERROR, id);
        }
    }
}
