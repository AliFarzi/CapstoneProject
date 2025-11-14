package TaskModule;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import EqiupmentModule.service.EquipmentManager;
import EqiupmentModule.service.exceptions.EquipmentChargeFullException;
import EqiupmentModule.service.exceptions.EquipmentUnavailableException;
import EqiupmentModule.model.*;
import EqiupmentModule.model.Equipment.EquipmentState;
import StorageModule.model.Cell;
import StorageModule.model.Item;
import StorageModule.model.Position;
import StorageModule.model.Storage;
import StorageModule.constants.ExceptionMessages;
import StorageModule.exceptions.CellNotFoundException;
import StorageModule.service.StorageManager;


public class StoreManualTaskTest {


    @Test
void testStoreManualTaskWorkingScenario() {
    // Arrange
    EquipmentManager manager = new EquipmentManager();
    Storage storage = new Storage("WH-MAIN", "Main Warehouse", 10, 10, 5);

    StorageManager storageManager = new StorageManager(storage);

    Item item = new Item("ITEM1", "Test Item", 2.0, new Position(0, 0, 0) );

    Position targetPos = new Position(1, 2, 3);
    StoreManualTask task = new StoreManualTask("SMT1", manager, storageManager, item, targetPos);

    // Act
    task.run(); // synchronous run

    // Assert
    assertNull(task.getException(), "No exception should occur");
    assertEquals(Item.Status.STORED, item.getStatus(), "Item should be marked as STORED");
    assertEquals(targetPos, item.getPosition(), "Item should be at target position");
}

    @Test
void testStoreManualTaskCellNotFoundScenario() {
    // Arrange
    EquipmentManager manager = new EquipmentManager();
    Storage storage = new Storage("WH-MAIN", "Main Warehouse", 10, 10, 5);

    StorageManager storageManager = new StorageManager(storage);

    Item item = new Item("ITEM1", "Test Item", 2.0, new Position(0, 0, 0) );
    item.updateStatus(Item.Status.STORED); // Simulate already stored item

    Position targetPos = new Position(1, 2, 7); // Z=7 is out of bounds
    StoreManualTask task = new StoreManualTask("SMT1", manager, storageManager, item, targetPos);

    // Act
    task.run(); // synchronous run

    // Assert
    Exception ex = task.getException();
    assertNotNull(task.getException());
    assertTrue(ex instanceof CellNotFoundException);
    
    assertEquals(ExceptionMessages.CELL_NOT_FOUND + targetPos, ex.getMessage());
}
}
