Logging Module

This module handles all log creation, reading, and deletion inside the project.
It collects messages from different subsystems (storage, equipment, simulation, tasks) and saves them into a clear folder structure.
The module is built with plain Java I/O and uses one small custom exception to make error handling easier to understand.

Features

Creates a daily log file automatically

Separate folders for each module or equipment

System-level log file for overall events

Read log files through a simple console interface

Delete old logs

Clear custom error messages through LoggingException

Folder Structure
/LoggingModule
│
├─ LoggingManager.java      // core logging logic
├─ LogApp.java              // console interface
├─ LoggingException.java    // custom exception
└─ LogLevel.java            // log level enum


Logs are stored under:

src/main/java/logs/system
src/main/java/logs/modules/<moduleName>/


Each folder contains one log file per day:

2025-11-14.log

How It Works
1. Write a log
LoggingManager logger = LoggingManager.getInstance();
logger.log("Crane started lifting", LogLevel.INFO, "Crane1");


This automatically:

creates the folder if needed

creates or appends the daily .log file

writes a timestamped message

2. Read or delete logs (CLI)

LogApp.java lets you:

choose a module folder

pick a log file

open or delete it

The CLI now catches LoggingException and prints clean messages instead of stack traces.

Custom Exception

The module uses one custom exception:

LoggingException


It is thrown when:

a folder cannot be created

a log file cannot be read

a log file cannot be written

a log file does not exist

deletion fails

This keeps the module simple while still meeting the project’s requirement for exception handling.

Notes

The module is self-contained and does not depend on other subsystems.

All file operations use plain Java I/O (File, BufferedWriter, BufferedReader).

The exception class helps the CLI show clear messages when something goes wrong.

No external libraries are used.
