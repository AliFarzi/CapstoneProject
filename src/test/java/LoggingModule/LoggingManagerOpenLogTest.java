package LoggingModule;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class LoggingManagerOpenLogTest {

    private LoggingManager loggingManager;

    @BeforeEach
    void setUp() {
        loggingManager = LoggingManager.getInstance();
    }

    @Test
    void testOpenNonExistentLogThrowsException() {
        assertThrows(LoggingException.class, () -> {
            loggingManager.openLog("nonexistent", "1900-01-01");
        });
    }
}