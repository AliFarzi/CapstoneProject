package TaskModule;

import StorageModule.model.*;
import StorageModule.service.StorageManager;
import LoggingModule.LoggingManager;
import LoggingModule.LogLevel;

public class RetrieveTask implements Runnable {

    private final String id;
    private final StorageManager storageManager;
    private final Position retrievePosition;
    private final LoggingManager loggingManager = LoggingManager.getInstance();
    private Exception exception;

    public RetrieveTask(String id, StorageManager storageManager, Position retrievePosition) {
        this.id = id;
        this.storageManager = storageManager;
        this.retrievePosition = retrievePosition;
    }

    @Override
    public void run() {

        try {
            Item retrievedItem = storageManager.retrieveItem(retrievePosition);
            loggingManager.log("Retrieve Task completed for Item: " + retrievedItem.getId() + " from Position: "
                    + retrievePosition.toString(), LogLevel.INFO, id);

        } catch (Exception e) {
            loggingManager.log(
                    "Retrieve Task failed at Position: " + retrievePosition.toString() + " - " + e.getMessage(),
                    LogLevel.ERROR, id);
            this.exception = e;
        }

    }

    public Exception getException() {
        return exception;
    }
}
