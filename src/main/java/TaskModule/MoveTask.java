package TaskModule;

import StorageModule.model.*;
import StorageModule.service.StorageManager;
import LoggingModule.LoggingManager;
import LoggingModule.LogLevel;

public class MoveTask implements Runnable {

    private final String id;
    private final StorageManager storageManager;
    private final Position fromPosition;
    private final Position toPosition;
    private final LoggingManager loggingManager = LoggingManager.getInstance();
    private Exception exception;

    public MoveTask(String id, StorageManager storageManager, Position fromPosition, Position toPosition) {
        this.id = id;
        this.storageManager = storageManager;
        this.fromPosition = fromPosition;
        this.toPosition = toPosition;
    }

    @Override
    public void run() {

        try {
            
            storageManager.moveItem(fromPosition, toPosition);
            loggingManager.log("Move Task completed from Position: " + fromPosition.toString() + " to Position: "
                    + toPosition.toString(), LogLevel.INFO, id);
            
        } catch (Exception e) {
            loggingManager.log(
                    "Move Task failed from Position: " + fromPosition.toString() + " to Position: " + toPosition.toString() + " - " + e.getMessage(),
                    LogLevel.ERROR, id);
            this.exception = e;
        }

    }

    public Exception getException() {
        return exception;
    }
}
