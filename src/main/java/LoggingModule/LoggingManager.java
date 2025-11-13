package LoggingModule;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LoggingManager {

    private static LoggingManager instance;
    private static final Object lock = new Object();

    private final String baseDir = "src/main/java/logs"; // base folder for all logs
    private final String modulesDir = baseDir + "/modules";

    private LoggingManager() {
        initializeFolders();
    }

    // Singleton pattern
    public static LoggingManager getInstance() {
        if (instance == null) {
            synchronized (lock) {
                if (instance == null) {
                    instance = new LoggingManager();
                }
            }
        }
        return instance;
    }

    // Create base folders
    private void initializeFolders() {
        new File(baseDir).mkdirs();
        new File(modulesDir).mkdirs();
    }

    // Main log method
    public void log(String message, LogLevel level, String source) {

        LocalDateTime now = LocalDateTime.now();
        String timestamp = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String logMessage = String.format("[%s][%s][%s] %s", timestamp, level, source, message);

        // Write to system log
        writeToFile(baseDir, "system", logMessage);

        // Write to module/equipment-specific log
        writeToFile(modulesDir, source, logMessage);
    }

    // Write log message to daily file
    private void writeToFile(String parentDir, String folderName, String message) {
        try {
            File folder = new File(parentDir + "/" + folderName);
            folder.mkdirs(); // creates folder if it doesn't exist

            String fileName = LocalDate.now().toString() + ".log"; // daily log file
            File logFile = new File(folder, fileName);

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(logFile, true))) {
                writer.write(message);
                writer.newLine();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void openLog(String source, String date) {
        File logFile = new File(baseDir + "/" + source + "/" + date + ".log");
        if (!logFile.exists()) {
            System.out.println("Log file does not exist.");
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(logFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean deleteLog(String source, String date) {
        File logFile = new File(baseDir + "/" + source + "/" + date + ".log");

        if (!logFile.exists()) {
            System.out.println("Log file not found: " + logFile.getAbsolutePath());
            return false;
        }

        boolean deleted = logFile.delete();

        if (deleted) {
            System.out.println("Log file deleted: " + logFile.getAbsolutePath());
        } else {
            System.out.println(" Failed to delete log file: " + logFile.getAbsolutePath());
        }

        return deleted;
    }

    /*
     * // Simple test
     * public static void main(String[] args) {
     * LoggingManager logger = LoggingManager.getInstance();
     * 
     * logger.log("Starting StorageManager task", LogLevel.INFO, "StorageManager");
     * logger.log("Crane1 is busy", LogLevel.WARN, "Crane1");
     * logger.log("Failed to move item 12 to Cell(5,3)", LogLevel.ERROR,
     * "StorageManager");
     * }
     */
}
