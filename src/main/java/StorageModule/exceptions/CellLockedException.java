package StorageModule.exceptions;

import StorageModule.constants.ExceptionMessages;
import StorageModule.model.Position;

public class CellLockedException extends Exception {
    public CellLockedException(Position position) {
        super(ExceptionMessages.CELL_LOCKED + position);
    }

    public CellLockedException() {
        super(ExceptionMessages.CELL_LOCKED);
    }
}
