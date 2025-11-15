# StorageModule

A thread-safe warehouse storage management system for automated guided vehicles (AGVs). This module handles 3D grid-based storage with position tracking, cell locking mechanisms, and exception handling.

## Project Structure

```
StorageModule/
├── constants/
│   └── ExceptionMessages.java          # Centralized error messages
├── exceptionhandling/
│   ├── ExceptionRethrower.java         # Exception transformation examples
│   ├── MultipleExceptionHandler.java   # Multi-catch pattern demos
│   └── ResourceManager.java            # Try-with-resources file handling
├── exceptions/
│   ├── CellEmptyException.java
│   ├── CellLockedException.java
│   ├── CellNotFoundException.java
│   ├── CellOccupiedException.java
│   └── StorageFullException.java
├── model/
│   ├── Cell.java                       # Individual storage slot
│   ├── Item.java                       # Storable warehouse items
│   ├── Position.java                   # 3D coordinates (x, y, level)
│   └── Storage.java                    # Complete warehouse grid
└── service/
    └── StorageManager.java             # Main storage operations controller
```

## Core Components

### Model Layer

**Position** - Three-dimensional coordinates representing a cell location in the warehouse. Uses (x, y, level) format where level represents floor height.

**Item** - Physical objects stored in cells. Each item tracks its own position and has three possible states:
- `STORED` - Currently in a warehouse cell
- `MOVING` - Being transported by an AGV
- `RETRIEVED` - Removed from storage

**Cell** - Individual storage slots. Cells can be:
- Empty or occupied
- Locked or unlocked (prevents concurrent access)
- Available (empty AND unlocked)

**Storage** - The complete warehouse, consisting of a 3D grid of cells. Initialized with dimensions (rows × columns × levels).

### Service Layer

**StorageManager** - Handles all storage operations with thread-safety guarantees:

- `addItem(Item, Position)` - Store an item at a specific location
- `addItem(Item)` - Auto-find first available cell and store
- `retrieveItem(Position)` - Remove and return item from a cell
- `moveItem(Position from, Position to)` - Transfer item between cells
- `findFirstAvailableCell()` - Locate next empty, unlocked cell
- `countAvailableCells()` - Get total free space

All operations use synchronized blocks and cell-level locking to prevent race conditions when multiple AGVs operate simultaneously.

### Exception Handling

The module uses custom checked exceptions for all storage-related errors:

- **CellEmptyException** - Attempted to retrieve from an empty cell
- **CellLockedException** - Tried to access a locked cell
- **CellNotFoundException** - Position doesn't exist in storage grid
- **CellOccupiedException** - Attempted to store in an occupied cell
- **StorageFullException** - No available cells remain

All exception messages are centralized in `ExceptionMessages.java` for easy maintenance.

### Additional Utilities

**ExceptionRethrower** - Demonstrates catching one exception type and throwing another while preserving the original cause chain.

**MultipleExceptionHandler** - Shows multi-catch syntax for handling several exception types in a single catch block.

**ResourceManager** - Implements `AutoCloseable` for safe file operations using try-with-resources. Handles item serialization to/from files.

## Usage Examples

### Basic Storage Operations

```java
// Create a warehouse: 5 rows × 4 columns × 3 levels
Storage warehouse = new Storage("WH001", "Main Warehouse", 5, 4, 3);
StorageManager manager = new StorageManager(warehouse);

// Create an item
Position startPos = new Position(1, 1, 1);
Item box = new Item("BOX123", "Electronics", 15.5, startPos);

// Store at specific location
try {
    Position target = new Position(2, 3, 1);
    manager.addItem(box, target);
} catch (CellOccupiedException | CellLockedException | CellNotFoundException e) {
    System.err.println("Storage failed: " + e.getMessage());
}
```

### Auto-Placement

```java
// Let the system find an available cell
Item package = new Item("PKG456", "Books", 8.2, new Position(0, 0, 0));

try {
    manager.addItem(package);  // Automatically places in first free cell
    System.out.println("Stored at: " + package.getPosition());
} catch (StorageFullException e) {
    System.err.println("Warehouse is full!");
}
```

### Retrieval

```java
try {
    Position location = new Position(2, 3, 1);
    Item retrieved = manager.retrieveItem(location);
    System.out.println("Retrieved: " + retrieved.getId());
} catch (CellEmptyException e) {
    System.err.println("Nothing at that position");
} catch (CellLockedException e) {
    System.err.println("Cell is currently locked by another AGV");
}
```

### Moving Items

```java
try {
    Position from = new Position(2, 3, 1);
    Position to = new Position(4, 1, 2);
    manager.moveItem(from, to);
} catch (CellEmptyException e) {
    System.err.println("Source cell is empty");
} catch (CellOccupiedException e) {
    System.err.println("Destination cell is occupied");
}
```

## Thread Safety

The module is designed for concurrent access by multiple AGVs:

1. **Cell-level locking** - Each cell has its own lock, allowing multiple operations on different cells simultaneously
2. **Synchronized blocks** - Critical sections use synchronized blocks rather than synchronized methods for finer control
3. **Lock-then-operate pattern** - Cells are locked before operations and unlocked in finally blocks to guarantee release
4. **Atomic check-and-lock** - Finding and locking available cells happens atomically to prevent double-booking

## Exception Philosophy

This module uses checked exceptions rather than runtime exceptions because storage failures are expected, recoverable conditions that calling code should explicitly handle. Each exception provides detailed context about what went wrong and where.

## File Operations

The `ResourceManager` class demonstrates proper resource handling for persisting items to disk:

```java
try (ResourceManager rm = new ResourceManager("resource-1")) {
    Item item = new Item("ITEM001", "Sample", 10.0, new Position(1, 1, 1));
    rm.writeItemToFile(item, "items.txt");
}  // Automatically closes, even if exception occurs
```

## Design 

- Cell IDs follow the pattern: `C_{x}_{y}_{level}`
- Position coordinates start at 1, not 0
- Items default to `RETRIEVED` status when first created<img width="2424" height="2250" alt="Picture2" src="https://github.com/user-attachments/assets/a70cfac2-e231-491d-b22a-8ff5d43f5381" />
- Storage grids are fully initialized at construction time

<img width="123" height="500" alt="Picture1" src="https://github.com/user-attachments/assets/231f2bf4-7b01-42fb-beaf-c91a070aa7e6" />


<img width="2424" height="2250" alt="Picture2" src="https://github.com/user-attachments/assets/178b9785-f0be-46b6-9753-df36191b164d" />




