package LoggingModule;

import java.io.File;
import java.util.Scanner;

public class LogApp {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        LoggingManager logger = LoggingManager.getInstance();

        // must match LoggingManager.baseDir
        File logsBase = new File("src/main/java/logs");

        // List top-level folders dynamically
        String[] topFolders = logsBase.list((current, name) -> new File(current, name).isDirectory());
        if (topFolders == null || topFolders.length == 0) {
            System.out.println("No logs found.");
            return;
        }

        System.out.println("Available sources:");
        for (int i = 0; i < topFolders.length; i++) {
            System.out.println((i + 1) + ". " + topFolders[i]);
        }

        System.out.print("Select source (number): ");
        int choice = scanner.nextInt();
        scanner.nextLine(); // consume newline
        File selectedFolder = new File(logsBase, topFolders[choice - 1]);

        // Check if selected folder has subfolders
        File[] subFolders = selectedFolder.listFiles(File::isDirectory);
        File logFolder;

        if (subFolders != null && subFolders.length > 0) {
            System.out.println("Available subfolders:");
            for (int i = 0; i < subFolders.length; i++) {
                System.out.println((i + 1) + ". " + subFolders[i].getName());
            }

            System.out.print("Select subfolder (number): ");
            int subChoice = scanner.nextInt();
            scanner.nextLine();
            logFolder = subFolders[subChoice - 1];
        } else {
            logFolder = selectedFolder;
        }

        // List log files in the selected folder
        String[] logFiles = logFolder.list((dir, name) -> name.endsWith(".log"));
        if (logFiles == null || logFiles.length == 0) {
            System.out.println("No log files found in this folder.");
            return;
        }

        System.out.println("Available log files:");
        for (int i = 0; i < logFiles.length; i++) {
            System.out.println((i + 1) + ". " + logFiles[i]);
        }

        System.out.print("Select log file (number): ");
        int fileChoice = scanner.nextInt();
        scanner.nextLine();
        String selectedFile = logFiles[fileChoice - 1];

        System.out.println("For opening choose 1 or deleting choose 2");
        int actionChoice = scanner.nextInt();
        scanner.nextLine(); // consume newline

        // Build the source string relative to logsBase
        String relativeSource =
                logsBase.toPath().relativize(logFolder.toPath()).toString().replace("\\", "/");

        // Remove ".log" extension for the date
        String date = selectedFile.replace(".log", "");

        // catch custom exception from LoggingManager
        try {
            if (actionChoice == 2) {
                logger.deleteLog(relativeSource, date);
            } else {
                logger.openLog(relativeSource, date);
                System.out.println(relativeSource + " log for " + date + " opened.");
            }
        } catch (LoggingException e) {
            System.out.println("Logging error: " + e.getMessage());
        }
    }
}
