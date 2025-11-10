
package StorageModule.exceptionhandling;

import StorageModule.exceptions.StorageFullException;
import StorageModule.model.Position;

public class ExceptionRethrower {
    
    public static void performRiskyOperation(Position position) throws StorageFullException {
        try {
            if (position.getX() < 0) {
                throw new IllegalArgumentException("Invalid position: X cannot be negative");
            }
            // Simulate storage full condition
            throw new StorageFullException();
        } catch (IllegalArgumentException e) {
            System.err.println("Logging exception: " + e.getMessage());
            // Create a new StorageFullException with a custom message and the original exception as the cause
            throw new StorageFullException(e);
        }
    }
    
    public static void handleStorageOperation() {
        try {
            performRiskyOperation(new Position(-1, 1, 1));
        } catch (StorageFullException e) {
            System.err.println("Handled rethrown exception: " + e.getMessage());
            if (e.getCause() != null) {
                System.err.println("Original cause: " + e.getCause().getMessage());
            }
        }
    }
}