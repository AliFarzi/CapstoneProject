package EquipmentModule;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import EqiupmentModule.model.*;

class CraneTest {

    @Test
    void id_is_set() {
        Crane c = new Crane("C1", null, 2.0, 90.0, 200.0);
        assertEquals("C1", c.getId());
    }

    @Test
    void state_can_change() {
        Crane c = new Crane("C1", null, 2.0, 90.0, 200.0);
        c.setState(Equipment.EquipmentState.BUSY);
        assertEquals(Equipment.EquipmentState.BUSY, c.getState());
    }

    @Test
    void battery_non_negative() {
        Crane c = new Crane("C1", null, 2.0, 90.0, 200.0);
        assertTrue(c.getBatteryLevel() >= 0);
    }

    @Test
    void speed_non_negative() {
        Crane c = new Crane("C1", null, 2.0, 90.0, 200.0);
        assertTrue(c.getSpeed() >= 0);
    }

    @Test
    void max_load_is_correct() {
        Crane c = new Crane("C1", null, 2.0, 90.0, 200.0);
        assertEquals(200.0, c.getMaxLoadWeight());
    }
}
