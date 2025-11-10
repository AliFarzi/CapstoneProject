package StorageModule.exceptionhandling;

import StorageModule.exceptions.*;
import StorageModule.model.Position;

public class MultipleExceptionHandler {
    
    public static void handleCellOperations(Position position) 
            throws CellEmptyException, CellLockedException, CellOccupiedException {
        // Simulate different exceptions based on position values
        if (position.getX() == 0) {
            throw new CellEmptyException(position);
        } else if (position.getY() == 0) {
            throw new CellLockedException(position);
        } else if (position.getLevel() == 0) {
            throw new CellOccupiedException(position);
        }
    }
    
    public static void processCellOperations(Position position) {
        try {
            handleCellOperations(position);
        } catch (CellEmptyException | CellLockedException | CellOccupiedException e) {
            System.err.println("Cell operation failed: " + e.getMessage());
            throw new RuntimeException("Failed to process cell operation", e);
        }
    }
}