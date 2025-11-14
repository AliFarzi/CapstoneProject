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

    // base folder for all logs
    private final String baseDir = "src/main/java/logs";
    private final String modulesDir = baseDir + "/modules";

    private LoggingManager() {
        initializeFolders();
    }

    // Singleton
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

    // create base folders, throw if something goes wrong
    private void initializeFolders() {
        File base = new File(baseDir);
        if (!base.exists() && !base.mkdirs()) {
            throw new LoggingException("Could not create base log directory: " + base.getAbsolutePath());
        }

        File modules = new File(modulesDir);
        if (!modules.exists() && !modules.mkdirs()) {
            throw new LoggingException("Could not create modules log directory: " + modules.getAbsolutePath());
        }
    }

    // main log method
    public void log(String message, LogLevel level, String source) {

        LocalDateTime now = LocalDateTime.now();
        String timestamp = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String logMessage = String.format("[%s][%s][%s] %s", timestamp, level, source, message);

        // write to system log
        writeToFile(baseDir, "system", logMessage);

        // write to module/equipment-specific log
        writeToFile(modulesDir, source, logMessage);
    }

    // write log message to daily file
    private void writeToFile(String parentDir, String folderName, String message) {

        // 1) make sure folder exists
        File folder = new File(parentDir + "/" + folderName);
        if (!folder.exists() && !folder.mkdirs()) {
            throw new LoggingException("Could not create log folder: " + folder.getAbsolutePath());
        }

        // 2) write to the file
        String fileName = LocalDate.now().toString() + ".log";   // daily log file
        File logFile = new File(folder, fileName);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(logFile, true))) {
            writer.write(message);
            writer.newLine();
        } catch (IOException e) {
            // rethrow as our own exception with a clear message
            throw new LoggingException("Failed to write to log file: " + logFile.getAbsolutePath(), e);
        }
    }

    public void openLog(String source, String date) {
        File logFile = new File(baseDir + "/" + source + "/" + date + ".log");

        if (!logFile.exists()) {
            throw new LoggingException("Log file does not exist: " + logFile.getAbsolutePath());
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(logFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
        } catch (IOException e) {
            throw new LoggingException("Failed to read log file: " + logFile.getAbsolutePath(), e);
        }
    }

    public boolean deleteLog(String source, String date) {
        File logFile = new File(baseDir + "/" + source + "/" + date + ".log");

        if (!logFile.exists()) {
            throw new LogNotFoundException(logFile.getAbsolutePath());
        }

        boolean deleted = logFile.delete();

        if (!deleted) {
            throw new LogDeleteException(logFile.getAbsolutePath());
        }

        System.out.println("Log file deleted: " + logFile.getAbsolutePath());
        return true;
    }
}
