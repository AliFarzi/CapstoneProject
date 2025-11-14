package LoggingModule;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

public class LoggingManagerExceptionTest {
    
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
    void testOpenNonExistentLogThrowsException() {
        assertThrows(LoggingException.class, () -> {
            loggingManager.openLog("nonexistent", "1900-01-01");
        });
    }
    


    @Test
    void testDeleteWithInvalidPath() {
        assertThrows(LogNotFoundException.class, () -> {
            loggingManager.deleteLog("../../../etc", "invalid-date");
        });
    }
    
    
    
  
}