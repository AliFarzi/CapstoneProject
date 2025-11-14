package StorageModule.service;

import StorageModule.model.*;
import StorageModule.exceptions.*;
import java.util.List;

public class StorageManager {

    private Storage storage;

    public StorageManager(Storage storage) {
        this.storage = storage;
    }

    public Storage getStorage() {
        return storage;
    }

    // Synchronized - prevents multiple AGVs storing to same position
    public void addItem(Item item, Position position)
            throws CellOccupiedException, CellLockedException, CellNotFoundException {

        Cell cell = storage.getCell(position);
        if (cell == null)
            throw new CellNotFoundException(position);

        synchronized (cell) {
            if (cell.isAvailable()) {
                cell.lock(); // Lock immediately when found!
            } else if (cell.isLocked()) {
                throw new CellLockedException(position);
            }
        }
        try {
            cell.store(item);
            item.moveTo(position);
        } finally {
            synchronized (cell) {
                cell.unlock(); // Always unlock, even if exception
            }
        }
    }

    /**
     * AUTO PLACEMENT
     * Synchronized + lock cell immediately
     */
    public void addItem(Item item)
            throws StorageFullException, CellOccupiedException, CellLockedException, CellNotFoundException {

        // Find AND lock cell in one atomic operation
        Cell cell = null;
        List<Cell> cells = storage.getCells();

        for (Cell c : cells) {
            synchronized (c) {
                if (c.isAvailable()) {
                    c.lock(); // Lock immediately when found!
                    cell = c;
                    break;
                }

            }
        }

        if (cell == null) {
            throw new StorageFullException();
        }

        try {
            cell.store(item);
            item.moveTo(cell.getPosition());
        } finally {
            synchronized (cell) {
                cell.unlock(); // Always unlock, even if exception
            }
        }
    }

    // Synchronized - prevents multiple AGVs retrieving from same cell
    public Item retrieveItem(Position position)
            throws CellEmptyException, CellLockedException, CellNotFoundException {

        Cell cell = storage.getCell(position);
        if (cell == null)
            throw new CellNotFoundException(position);

        synchronized (cell) {
            if (cell.isAvailable()) {
                cell.lock();
            }
        }

        try {
            Item item = cell.retrieve();
            return item;
        } finally {
            cell.unlock();
        }

    }

    // Synchronized - prevents multiple AGVs moving to same destination
    public synchronized void moveItem(Position from, Position to)
            throws CellEmptyException, CellOccupiedException, CellLockedException, CellNotFoundException {

        Cell fromCell = storage.getCell(from);
        Cell toCell = storage.getCell(to);

        if (fromCell == null || toCell == null)
            throw new CellNotFoundException();

        if (fromCell.isEmpty())
            throw new CellEmptyException(from);
        if (!toCell.isEmpty())
            throw new CellOccupiedException(to);
        if (fromCell.isLocked() || toCell.isLocked())
            throw new CellLockedException();

        Item item = fromCell.retrieve();
        toCell.store(item);
        item.moveTo(to);
    }

    // NO synchronization - not called directly by AGVs
    // Logic integrated into addItem(Item) with proper locking @AliFarzi(Please
    // check this comment)
    public Cell findFirstAvailableCell() {
        List<Cell> cells = storage.getCells();
        for (Cell cell : cells) {
            if (cell.isAvailable()) {
                return cell;
            }
        }
        return null;
    }

    public int countAvailableCells() {
        int count = 0;
        for (Cell cell : storage.getCells()) {
            if (cell.isAvailable())
                count++;
        }
        return count;
    }

    public void printStorageInfo() {
        System.out.println("Storage: " + storage.getName());
        System.out.println("Total cells: " + storage.getCells().size());
        System.out.println("Available cells: " + countAvailableCells());
    }
}