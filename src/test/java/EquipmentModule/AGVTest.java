package EquipmentModule;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import EqiupmentModule.model.*;

class AGVTest {

    @Test
    void id_is_set() {
        AGV a = new AGV("A1", null, 2.0, 50.0, 100.0, 200.0);
        assertEquals("A1", a.getId());
    }

    @Test
    void state_changes() {
        AGV a = new AGV("A1", null, 2.0, 50.0, 100.0, 200.0);
        a.setState(Equipment.EquipmentState.BUSY);
        assertEquals(Equipment.EquipmentState.BUSY, a.getState());
    }

    @Test
    void battery_non_negative() {
        AGV a = new AGV("A1", null, 2.0, 50.0, 100.0, 200.0);
        assertTrue(a.getBatteryLevel() >= 0);
    }

    @Test
    void speed_non_negative() {
        AGV a = new AGV("A1", null, 2.0, 50.0, 100.0, 200.0);
        assertTrue(a.getSpeed() >= 0);
    }

}
