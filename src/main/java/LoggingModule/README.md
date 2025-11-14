Logging Module

This module manages all log creation, reading, and deletion in the project.
It collects messages from different subsystems (storage, equipment, simulation, tasks) and organizes them into a clear folder structure.
The module uses plain Java file I/O and one custom exception to make error handling cleaner.

Features

Creates a daily log file automatically

Separate folders for each module or equipment

System-level log file for general events

Read log files through a simple console interface

Delete existing log files

Clean error messages through LoggingException

Folder Structure
/LoggingModule
│
├─ LoggingManager.java      // core logging logic
├─ LogApp.java              // console interface
├─ LoggingException.java    // custom exception
└─ LogLevel.java            // log level enum

Log storage structure:
src/main/java/logs/
│
├─ system/                  // global logs
└─ modules/<source>/        // logs for each module/equipment


Each folder contains one log file per day:

2025-11-14.log

How It Works
Write a log
LoggingManager logger = LoggingManager.getInstance();
logger.log("Crane started lifting", LogLevel.INFO, "Crane1");


This will:

create the folder if needed

create or append the daily .log file

write a timestamped log entry

Read or Delete Logs (CLI)

LogApp.java allows you to:

pick a module folder

select a daily log file

open it or delete it

The CLI catches LoggingException and prints clear messages instead of stack traces.

Custom Exception

The module uses one custom exception:

LoggingException

This exception is thrown when:

a folder cannot be created

a log file cannot be written

a log file cannot be read

a log file does not exist

deletion fails

This keeps the module simple while still following the project’s requirement for exception handling.

Notes

The module is self-contained and does not depend on other subsystems.

All file operations use standard Java I/O (File, BufferedWriter, BufferedReader).

The custom exception keeps the CLI output clean and understandable.

No external libraries are used.
