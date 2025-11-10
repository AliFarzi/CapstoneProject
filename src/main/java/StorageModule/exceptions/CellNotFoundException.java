package StorageModule.exceptions;

import StorageModule.model.Position;
import StorageModule.constants.ExceptionMessages;

public class CellNotFoundException extends Exception {

    public CellNotFoundException(Position position) {
        super(ExceptionMessages.CELL_NOT_FOUND + position);
    }

    public CellNotFoundException() {
        super(ExceptionMessages.CELL_NOT_FOUND);
    }
}
