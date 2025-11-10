package StorageModule.test.exceptionhandling;

import org.junit.jupiter.api.Test;
import StorageModule.exceptions.*;
import StorageModule.model.Position;
import StorageModule.exceptionhandling.MultipleExceptionHandler;
import static org.junit.jupiter.api.Assertions.*;

public class MultipleExceptionHandlerTest {
    
    @Test
    void testHandleCellEmpty() {
        Position pos = new Position(0, 1, 1);
        assertThrows(CellEmptyException.class, 
            () -> MultipleExceptionHandler.handleCellOperations(pos));
    }
    
    @Test
    void testHandleCellLocked() {
        Position pos = new Position(1, 0, 1);
        assertThrows(CellLockedException.class,
            () -> MultipleExceptionHandler.handleCellOperations(pos));
    }
    
    @Test
    void testHandleCellOccupied() {
        Position pos = new Position(1, 1, 0);
        assertThrows(CellOccupiedException.class,
            () -> MultipleExceptionHandler.handleCellOperations(pos));
    }
    
    @Test
    void testProcessCellOperationsWithException() {
        Position pos = new Position(0, 1, 1);
        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> MultipleExceptionHandler.processCellOperations(pos));
        assertTrue(exception.getCause() instanceof CellEmptyException);
    }
    
    @Test
    void testProcessCellOperationsSuccess() {
        Position pos = new Position(1, 1, 1);
        assertDoesNotThrow(() -> MultipleExceptionHandler.processCellOperations(pos));
    }
}