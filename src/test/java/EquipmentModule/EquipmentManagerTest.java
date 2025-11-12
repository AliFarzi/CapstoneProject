package EquipmentModule;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import EqiupmentModule.model.*;
import EqiupmentModule.service.*;
import EqiupmentModule.service.exceptions.*;

import java.nio.file.Paths;

class EquipmentManagerTest {

    @Test
    void add_equipment_increases_list_size() {
        EquipmentManager manager = new EquipmentManager();
        manager.addEquipment(new Crane("C1", null, 2.0, 80.0, 100.0));
        assertEquals(1, manager.getAll().size());
    }

    @Test
    void available_equipment_includes_idle() {
        EquipmentManager manager = new EquipmentManager();
        manager.addEquipment(new Crane("C1", null, 2.0, 80.0, 100.0));
        assertEquals(1, manager.getAvailableEquipment().size());
    }

    @Test
    void assign_to_task_changes_state_to_busy() throws Exception {
        EquipmentManager manager = new EquipmentManager();
        Crane c = new Crane("C1", null, 2.0, 80.0, 100.0);
        manager.addEquipment(c);
        manager.assignToTask("C1");
        assertEquals(Equipment.EquipmentState.BUSY, c.getState());
    }

    @Test
    void release_changes_state_to_idle() throws Exception {
        EquipmentManager manager = new EquipmentManager();
        Crane c = new Crane("C1", null, 2.0, 80.0, 100.0);
        manager.addEquipment(c);
        manager.assignToTask("C1");
        manager.release("C1");
        assertEquals(Equipment.EquipmentState.IDLE, c.getState());
    }

    @Test
    void export_csv_does_not_throw() {
        EquipmentManager manager = new EquipmentManager();
        manager.addEquipment(new Crane("C1", null, 2.0, 80.0, 100.0));
        assertDoesNotThrow(() -> manager.exportEquipmentCsv(Paths.get("equipment.csv")));
    }
}
