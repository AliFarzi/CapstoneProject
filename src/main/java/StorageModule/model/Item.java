package StorageModule.model;

//  * Represents a physical object stored in a warehouse cell.
public class Item {

    public enum Status {
        STORED,
        MOVING,
        RETRIEVED
    }

    private String id;
    private String description;
    private double weight;
    private Status status;
    private Position position;

    public Item(String id, String description, double weight, Position position) {
        this.id = id;
        this.description = description;
        this.weight = weight;
        this.position = position;
        this.status = Status.STORED; // default status when created
    }

    public String getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public double getWeight() {
        return weight;
    }

    public Status getStatus() {
        return status;
    }

    public Position getPosition() {
        return position;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public void updateStatus(Status status) {
        this.status = status;
    }

    public void moveTo(Position newPosition) {
        this.position = newPosition;
        this.status = Status.MOVING;
    }

    @Override
    public String toString() {
        return "Item{" +
                "id='" + id + '\'' +
                ", description='" + description + '\'' +
                ", weight=" + weight +
                ", status=" + status +
                ", position=" + position +
                '}';
    }
}
