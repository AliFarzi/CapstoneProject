package StorageModule.exceptions;

import StorageModule.constants.ExceptionMessages;
import StorageModule.model.Position;

public class CellOccupiedException extends Exception {
    public CellOccupiedException(Position position) {
        super(ExceptionMessages.CELL_OCCUPIED + position);
    }
}
