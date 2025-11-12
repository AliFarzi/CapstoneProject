package EquipmentModule;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import EqiupmentModule.model.*;


class ChargingStationTest {

    @Test
    void id_is_set() {
        ChargingStation cs = new ChargingStation("CS1", null, 10.0);
        assertEquals("CS1", cs.getId());
    }

    @Test
    void power_correct() {
        ChargingStation cs = new ChargingStation("CS1", null, 10.0);
        assertEquals(10.0, cs.getPowerKW());
    }

    @Test
    void starts_free() {
        ChargingStation cs = new ChargingStation("CS1", null, 10.0);
        assertFalse(cs.isOccupied());
    }

    @Test
    void can_start_charging() {
        ChargingStation cs = new ChargingStation("CS1", null, 10.0);
        AGV a = new AGV("A1", null, 2.0, 50.0, 100.0, 200.0);
        assertTrue(cs.startCharging(a));
    }

}
