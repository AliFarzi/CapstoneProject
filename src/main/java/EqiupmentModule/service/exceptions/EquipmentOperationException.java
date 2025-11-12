package EqiupmentModule.service.exceptions;

import LoggingModule.LogLevel;
import LoggingModule.LoggingManager;

public class EquipmentOperationException extends Exception {
    public EquipmentOperationException(String message) {
        super(message);
        LoggingManager.getInstance().log(message, LogLevel.ERROR, "EquipmentOperationException");
    }

    public EquipmentOperationException(String message, Throwable cause) {
        super(message, cause);
    }
}
