package EquipmentModule;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import EqiupmentModule.model.*;

class ShuttleTest {

    @Test
    void id_is_set() {
        Shuttle s = new Shuttle("S1", null, 3.0, 80.0, 50.0);
        assertEquals("S1", s.getId());
    }

    @Test
    void state_changes() {
        Shuttle s = new Shuttle("S1", null, 3.0, 80.0, 50.0);
        s.setState(Equipment.EquipmentState.MOVING);
        assertEquals(Equipment.EquipmentState.MOVING, s.getState());
    }

    @Test
    void battery_level_valid() {
        Shuttle s = new Shuttle("S1", null, 3.0, 80.0, 50.0);
        assertTrue(s.getBatteryLevel() >= 0);
    }

    @Test
    void speed_positive() {
        Shuttle s = new Shuttle("S1", null, 3.0, 80.0, 50.0);
        assertTrue(s.getSpeed() >= 0);
    }

    @Test
    void max_load_correct() {
        Shuttle s = new Shuttle("S1", null, 3.0, 80.0, 50.0);
        assertEquals(50.0, s.getMaxLoadWeight());
    }
}
