package StorageModule.test.exceptions;


import StorageModule.exceptions.StorageFullException;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class StorageFullExceptionTest {
    @Test
    void testDefaultConstructor() {
        StorageFullException exception = new StorageFullException();
        assertNotNull(exception.getMessage());
    }

    @Test
    void testWithCause() {
        Exception cause = new Exception("Test cause");
        StorageFullException exception = new StorageFullException(cause);
        assertEquals(cause, exception.getCause());
        assertTrue(exception.getMessage().contains("full"));
    }
}