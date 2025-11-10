package EqiupmentModule.service.exceptions;

public class InvalidEquipmentStateException extends EquipmentOperationException {
    public InvalidEquipmentStateException(String message) {
        super(message);
    }
}
