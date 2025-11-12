package EqiupmentModule.service.exceptions;

import LogingModule.LogLevel;
import LogingModule.LoggingManager;

public class EquipmentOperationException extends Exception {
    public EquipmentOperationException(String message) {
        super(message);
        LoggingManager.getInstance().log(message, LogLevel.ERROR, "EquipmentOperationException");
    }

    public EquipmentOperationException(String message, Throwable cause) {
        super(message, cause);
    }
}
