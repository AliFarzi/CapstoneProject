package StorageModule.model;

import java.util.Objects;

public class Position {
    private int x;
    private int y;
    private int level;

    public Position(int x, int y, int level) {
        this.x = x;
        this.y = y;
        this.level = level;
    }

    // Getters
    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getLevel() {
        return level;
    }

    // Equals & hashCode for comparing positions
    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Position))
            return false;
        Position pos = (Position) o;
        return x == pos.x && y == pos.y && level == pos.level;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, level);
    }

    @Override
    public String toString() {
        return "(" + x + ", " + y + ", " + level + ")";
    }
}