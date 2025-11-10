# Storage Submodule

A reusable Java module to manage warehouse storage in a 3D grid.  
It provides an easy-to-use API to add, move, and retrieve items from storage cells.

---

## Features

- 3D warehouse grid (rows × columns × levels)
- Cells can store one item each
- Automatic or manual item placement
- Move and retrieve items
- Track item status (`STORED`, `MOVING`, `RETRIEVED`)
- Provides storage information and available cell count
- Designed for integration with other submodules (Task, Equipment, HMI)

---

## Folder Structure

```
/storage-submodule
│
├─ model/                      # Data classes (Storage, Cell, Item, Position)
├─ service/                    # Business logic (StorageManager, CellAllocator)
├─ exceptions/                 # Custom exceptions (CellLockedException, StorageFullException, etc.)
├─ constants/                  # Constants used in the module.
```

---

## Usage

### Step 1: Initialize Storage

```java
import StorageModule.*;
import StorageModule.model.*;

Storage storage = new Storage("WH1", "Main Warehouse", 5, 5, 3);
StorageSubmodule storageModule = new StorageSubmodule(storage);
```

### Step 2: Create Items

```java
Item item1 = new Item("I001", "Laptop", 2.5, new Position(1, 1, 1));
Item item2 = new Item("I002", "Monitor", 4.0, new Position(1, 2, 1));
```

### Step 3: Add Items

```java
// Add to specific position
storageModule.addItem(item1, new Position(1, 1, 1));

// Add to first available cell
storageModule.addItem(item2);
```

### Step 4: Move Items

```java
storageModule.moveItem(new Position(1, 1, 1), new Position(2, 2, 1));
```

### Step 5: Retrieve Items

```java
Item retrieved = storageModule.retrieveItem(new Position(2, 2, 1));
System.out.println("Retrieved: " + retrieved);
```

### Step 6: Check Storage Info

```java
storageModule.printStorageInfo();
int available = storageModule.getAvailableCells();
System.out.println("Available cells: " + available);
```

---

## Exceptions

- **CellLockedException** – Trying to access a locked cell
- **CellOccupiedException** – Trying to add an item to an occupied cell
- **CellEmptyException** – Trying to retrieve from an empty cell
- **CellNotFoundException** – Position does not exist in storage
- **StorageFullException** – No empty cells available

---

## Notes

- Use `StorageSubmodule` as the single entry point for all operations
- Internal classes like `StorageManager` and `CellAllocator` are not required by users
- Position coordinates start at `(1, 1, 1)`
