package StorageModule.exceptions;

import StorageModule.constants.ExceptionMessages;
import StorageModule.model.Position;

public class CellEmptyException extends Exception {
    public CellEmptyException(Position position) {
        super(ExceptionMessages.CELL_EMPTY + position);
    }

    public CellEmptyException() {
        super(ExceptionMessages.CELL_EMPTY_ID);
    }
}
