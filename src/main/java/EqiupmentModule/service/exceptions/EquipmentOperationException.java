package EqiupmentModule.service.exceptions;

public class EquipmentOperationException extends Exception {
    public EquipmentOperationException(String message) {
        super(message);
    }

    public EquipmentOperationException(String message, Throwable cause) {
        super(message, cause);
    }
}
