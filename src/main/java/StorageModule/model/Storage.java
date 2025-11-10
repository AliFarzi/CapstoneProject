package StorageModule.model;

import java.util.ArrayList;
import java.util.List;

import StorageModule.exceptions.CellEmptyException;
import StorageModule.exceptions.CellNotFoundException;

//  * Represents the entire warehouse.
public class Storage {
    private String id;
    private String name;
    private int rows;
    private int columns;
    private int levels;
    private List<Cell> cells;

    public Storage(String id, String name, int rows, int columns, int levels) {
        this.id = id;
        this.name = name;
        this.rows = rows;
        this.columns = columns;
        this.levels = levels;
        this.cells = new ArrayList<>();

        // Initialize all cells in the 3D grid
        for (int l = 1; l <= levels; l++) {
            for (int r = 1; r <= rows; r++) {
                for (int c = 1; c <= columns; c++) {
                    Position pos = new Position(r, c, l);
                    String cellId = "C_" + r + "_" + c + "_" + l;
                    cells.add(new Cell(cellId, pos));
                }
            }
        }
    }

    public Cell getCell(Position position) throws CellNotFoundException {
        for (Cell cell : cells) {
            if (cell.getPosition().equals(position)) {
                return cell;
            }
        }
        throw new CellNotFoundException(position);
    }

    // Find the first available (empty and unlocked) cell
    public Cell findEmptyCell() {
        for (Cell cell : cells) {
            if (cell.isAvailable()) {
                return cell;
            }
        }
        return null;
    }

    public void addItem(Item item, Position position) throws CellNotFoundException {
        Cell cell = getCell(position);
        cell.store(item); // no need to check null anymore; getCell will throw if not found
    }

    public Item removeItem(Position position) throws CellNotFoundException, CellEmptyException {
        Cell cell = getCell(position);
        return cell.retrieve();
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getRows() {
        return rows;
    }

    public int getColumns() {
        return columns;
    }

    public int getLevels() {
        return levels;
    }

    public List<Cell> getCells() {
        return cells;
    }

    @Override
    public String toString() {
        return "Storage{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", rows=" + rows +
                ", columns=" + columns +
                ", levels=" + levels +
                ", totalCells=" + cells.size() +
                '}';
    }
}
