package StorageModule.model;

import StorageModule.exceptions.CellEmptyException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class CellTest {
    private Cell cell;
    private Item testItem;
    private Position testPosition;

    @BeforeEach
    void setUp() {
        testPosition = new Position(1, 1, 1);
        cell = new Cell("A1", testPosition);
        testItem = new Item("1", "Test Item", 1.0, testPosition);
    }

    @Test
    void testStoreItem() {
        // Test storing item in an empty cell
        assertDoesNotThrow(() -> cell.store(testItem));
        assertFalse(cell.isEmpty());
        assertEquals(testItem, cell.getContent());
    }

    @Test
    void testStoreItemInOccupiedCell() {
        // Test storing when cell is already occupied
        cell.store(testItem);
        assertThrows(IllegalStateException.class, () -> cell.store(testItem));
    }

    @Test
    void testRetrieveItem() throws CellEmptyException {
        // Test retrieving an item
        cell.store(testItem);
        Item retrieved = cell.retrieve();
        assertEquals(testItem, retrieved);
        assertTrue(cell.isEmpty());
    }

    @Test
    void testRetrieveFromEmptyCell() {
        // Test retrieving from an empty cell
        assertThrows(CellEmptyException.class, () -> cell.retrieve());
    }

    @Test
    void testLockAndUnlock() {
        // Test lock/unlock functionality
        assertFalse(cell.isLocked());
        
        cell.lock();
        assertTrue(cell.isLocked());
        
        cell.unlock();
        assertFalse(cell.isLocked());
    }
}
