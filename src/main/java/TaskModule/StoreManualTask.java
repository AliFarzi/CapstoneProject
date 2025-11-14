package TaskModule;

import EqiupmentModule.model.*;
import EqiupmentModule.service.EquipmentManager;
import LoggingModule.LoggingManager;
import LoggingModule.LogLevel;
import StorageModule.model.*;
import StorageModule.service.StorageManager;
import javafx.geometry.Pos;

public class StoreManualTask implements Runnable {

    private final String id;
    private final EquipmentManager equipmentManager;
    private final StorageManager storageManager;
    private final Item item;
    private final Position targetPosition;
    private final LoggingManager logger = LoggingManager.getInstance();

    public StoreManualTask(String id,EquipmentManager equipmentManager, StorageManager storageManager, Item item, Position targetPosition) {
        this.id = id;
        this.equipmentManager = equipmentManager;
        this.storageManager = storageManager;
        this.item = item;
        this.targetPosition = targetPosition;
        logger.log("StoreManualTask initialized for Item: " + item.getId(), LogLevel.INFO, id);
    }

    @Override
    public void run() {

        logger.log("Starting Store Manual Task for Item: " + item.getId() + " to Position: " + targetPosition.toString(), LogLevel.INFO, id);
        
        try {
            synchronized (item){
                item.updateStatus(Item.Status.MOVING);
                storageManager.addItem(item, targetPosition);
                Thread.sleep(2000);
                logger.log("Equipment: " + " successfully moved to Position: " + item.getPosition().toString(), LogLevel.INFO, id);
                item.updateStatus(Item.Status.STORED);
            }
            
        } catch (Exception e) {
            logger.log("Store Auto Task failed for Item: " + item.getId() + " - " + e.getMessage(), LogLevel.ERROR, id);
        }



    }

}