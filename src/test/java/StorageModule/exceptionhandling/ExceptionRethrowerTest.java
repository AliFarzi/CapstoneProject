package StorageModule.exceptionhandling;

import org.junit.jupiter.api.Test;
import StorageModule.exceptions.StorageFullException;

import StorageModule.model.Position;
import static org.junit.jupiter.api.Assertions.*;

public class ExceptionRethrowerTest {
    
    @Test
    void testPerformRiskyOperationWithValidPosition() {
        assertThrows(StorageFullException.class,
            () -> ExceptionRethrower.performRiskyOperation(new Position(1, 1, 1)));
    }
    
    @Test
    void testPerformRiskyOperationWithInvalidPosition() {
        StorageFullException exception = assertThrows(StorageFullException.class,
            () -> ExceptionRethrower.performRiskyOperation(new Position(-1, 1, 1)));
        assertNotNull(exception.getCause());
        assertEquals("Invalid position: X cannot be negative", exception.getCause().getMessage());
    }
    
    @Test
    void testHandleStorageOperation() {
        assertDoesNotThrow(ExceptionRethrower::handleStorageOperation);
    }
    
    @Test
    void testExceptionMessageContainsOriginalMessage() {
        try {
            ExceptionRethrower.performRiskyOperation(new Position(-1, 1, 1));
            fail("Expected StorageFullException");
        } catch (StorageFullException e) {
            assertTrue(e.getMessage().contains("Storage is full"), 
                "Exception message should indicate storage is full");
        }
    }
    
    @Test
    void testExceptionChaining() {
        try {
            ExceptionRethrower.performRiskyOperation(new Position(-1, 1, 1));
            fail("Expected StorageFullException");
        } catch (StorageFullException e) {
            assertNotNull(e.getCause());
            assertEquals(IllegalArgumentException.class, e.getCause().getClass());
        }
    }
}