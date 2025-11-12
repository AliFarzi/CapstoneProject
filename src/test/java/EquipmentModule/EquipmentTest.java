package EquipmentModule;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import EqiupmentModule.model.*;

class EquipmentTest {

    static class Dummy extends Equipment {
        Dummy() {
            super("D1", null, 2.0, 50.0);
        }

        @Override
        public void charge(ChargingStation c) {
        }
    }

    @Test
    void id_is_set() {
        Dummy d = new Dummy();
        assertEquals("D1", d.getId());
    }

    @Test
    void speed_positive() {
        Dummy d = new Dummy();
        assertTrue(d.getSpeed() >= 0);
    }

    @Test
    void battery_positive() {
        Dummy d = new Dummy();
        assertTrue(d.getBatteryLevel() >= 0);
    }

    @Test
    void state_defaults_idle() {
        Dummy d = new Dummy();
        assertEquals(Equipment.EquipmentState.IDLE, d.getState());
    }

    @Test
    void set_state_works() {
        Dummy d = new Dummy();
        d.setState(Equipment.EquipmentState.BUSY);
        assertEquals(Equipment.EquipmentState.BUSY, d.getState());
    }
}
