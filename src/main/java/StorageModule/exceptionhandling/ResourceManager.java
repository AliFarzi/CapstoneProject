package StorageModule.exceptionhandling;

import StorageModule.model.Item;
import StorageModule.model.Position;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

public class ResourceManager implements AutoCloseable {
    private final String resourceId;
    private boolean isClosed = false;

    public ResourceManager(String resourceId) {
        this.resourceId = resourceId;
    }

    public void writeItemToFile(Item item, String filename) throws IOException {
        if (isClosed) {
            throw new IllegalStateException("Resource is closed");
        }
        try (FileWriter writer = new FileWriter(filename)) {
            writer.write(item.toString());
        }
    }

    public Item readItemFromFile(String filename) throws IOException {
        if (isClosed) {
            throw new IllegalStateException("Resource is closed");
        }
        try (BufferedReader reader = Files.newBufferedReader(Path.of(filename))) {
            String line = reader.readLine();
            return new Item("temp", "Temporary", 1.0, new Position(0, 0, 0));
        }
    }

    @Override
    public void close() {
        isClosed = true;
    }
}