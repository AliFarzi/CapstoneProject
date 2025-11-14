package TaskModule;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import EqiupmentModule.service.EquipmentManager;
import EqiupmentModule.service.exceptions.EquipmentChargeFullException;
import EqiupmentModule.service.exceptions.EquipmentUnavailableException;
import EqiupmentModule.model.*;
import EqiupmentModule.model.Equipment.EquipmentState;
import StorageModule.model.Position;

public class ChargingTaskTest {

    @Test
    void testChargingTaskCreation() {
        EquipmentManager manager = new EquipmentManager();
        AGV agv = new AGV("AGV1", null, 1.0, 90.0, 50.0, 200.0);
        manager.addEquipment(agv);

        ChargingStation station = new ChargingStation("CS1", new Position(0, 0, 0), 2);

        ChargingTask task = new ChargingTask(manager, station, agv, "CT1");

        task.run();
        assertNull(task.getException());
        assertEquals(100.0, agv.getBatteryLevel());

    }

    @Test
    void testChargingTask_WhenBatteryFull_ShouldCaptureException() {
        EquipmentManager manager = new EquipmentManager();

        AGV agv = new AGV("AGV1", null, 1.0, 100.0, 50.0, 200.0);
        // Battery is 100% → should throw EquipmentChargeFullException

        ChargingStation station = new ChargingStation("CS1", new Position(0, 0, 0), 2);

        ChargingTask task = new ChargingTask(manager, station, agv, "CT1");

        // Run synchronously
        task.run();

        // Assert
        Exception ex = task.getException();
        assertNotNull(ex);
        assertTrue(ex instanceof EquipmentChargeFullException);
        assertEquals("Equipment ID: AGV1 Battery is already full.", ex.getMessage());
    }

   @Test
    void testChargingTask_WhenEquipmentBusy_ShouldCaptureException() {
        EquipmentManager manager = new EquipmentManager();

        AGV agv = new AGV("AGV1", null, 1.0, 10.0, 50.0, 200.0);
        
        agv.setState(EquipmentState.BUSY); // Set equipment as busy
        ChargingStation station = new ChargingStation("CS1", new Position(0, 0, 0), 2);

        ChargingTask task = new ChargingTask(manager, station, agv, "CT1");

        // Run synchronously
        task.run();

        // Assert
        Exception ex = task.getException();
        assertNotNull(ex);
        assertTrue(ex instanceof EquipmentUnavailableException);
        assertEquals("Busy equipment cannot start charging", ex.getMessage());
    }

}
