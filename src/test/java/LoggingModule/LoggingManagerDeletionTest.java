package LoggingModule;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class LoggingManagerDeletionTest {

    private LoggingManager loggingManager;

    @BeforeEach
    void setUp() {
        loggingManager = LoggingManager.getInstance();
    }

    @Test
    void testDeleteNonExistentLogThrowsLogNotFoundException() {
        assertThrows(LogNotFoundException.class, () -> {
            loggingManager.deleteLog("nonexistent", "1900-01-01");
        });
    }

    @Test
    void testDeleteWithInvalidPath() {
        assertThrows(LogNotFoundException.class, () -> {
            loggingManager.deleteLog("../../../etc", "invalid-date");
        });
    }
}