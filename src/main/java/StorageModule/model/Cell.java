package StorageModule.model;

import StorageModule.exceptions.CellEmptyException;

//  Represents a single storage slot (cell) in the warehouse.
public class Cell {
    private String id;
    private Item content;
    private Position position;
    private boolean locked;

    public Cell(String id, Position position) {
        this.id = id;
        this.position = position;
        this.content = null;
        this.locked = false;
    }

    public boolean isEmpty() {
        return content == null;
    }

    public boolean isAvailable() {
        return !locked && isEmpty();
    }

    public void store(Item item) {
        if (!isEmpty()) { 
            throw new IllegalStateException("Cell " + id + " is already occupied!");
        }
        this.content = item;
        item.moveTo(this.position);
        item.updateStatus(Item.Status.STORED);
    }

    public Item retrieve() throws CellEmptyException {
        if (isEmpty()) {
            throw new CellEmptyException();
        }
        Item temp = content;
        content = null;
        temp.updateStatus(Item.Status.RETRIEVED);
        return temp;
    }

    public void lock() {
        this.locked = true;
    }

    public void unlock() {
        this.locked = false;
    }

    public boolean isLocked() {
        return locked;
    }

    public String getId() {
        return id;
    }

    public Position getPosition() {
        return position;
    }

    public Item getContent() {
        return content;
    }

    @Override
    public String toString() {
        return "Cell{" +
                "id='" + id + '\'' +
                ", position=" + position +
                ", locked=" + locked +
                ", occupied=" + !isEmpty() +
                '}';
    }
}
