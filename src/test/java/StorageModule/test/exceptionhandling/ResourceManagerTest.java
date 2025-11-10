package StorageModule.test.exceptionhandling;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import StorageModule.model.Item;
import StorageModule.model.Position;
import StorageModule.exceptionhandling.ResourceManager;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class ResourceManagerTest {
    private static final String TEST_FILE = "test_item.txt";
    private ResourceManager resourceManager;

    @BeforeEach
    void setUp() {
        resourceManager = new ResourceManager("test-resource");
    }

    @AfterEach
    void tearDown() throws IOException {
        resourceManager.close();
        try {
            Files.deleteIfExists(Path.of(TEST_FILE));
        } catch (IOException e) {
        }
    }

    @Test
    void testWriteAndReadItem() throws IOException {
        Item testItem = new Item("1", "Test Item", 10.5, new Position(1, 2, 3));
        resourceManager.writeItemToFile(testItem, TEST_FILE);
        assertTrue(Files.exists(Path.of(TEST_FILE)));
        
        Item readItem = resourceManager.readItemFromFile(TEST_FILE);
        assertNotNull(readItem);
    }

    @Test
    void testWriteToFileFailsForInvalidPath() {
        assertThrows(IOException.class, () -> 
            resourceManager.writeItemToFile(
                new Item("1", "Test", 1.0, new Position(0,0,0)), 
                "/invalid/path/test.txt"
            )
        );
    }

    @Test
    void testReadFromNonExistentFile() {
        assertThrows(IOException.class, () -> 
            resourceManager.readItemFromFile("nonexistent.txt")
        );
    }

    @Test
    void testOperationsAfterClose() {
        resourceManager.close();
        assertThrows(IllegalStateException.class,
            () -> resourceManager.writeItemToFile(
                new Item("1", "Test", 1.0, new Position(0,0,0)), 
                TEST_FILE
            )
        );
    }

}