Logging Module

A small logging component used across the project to record events, errors, and module activity.
It creates daily log files, stores them in separate folders, and allows opening or deleting logs through a simple console tool.

What this module does

Creates a main logs directory automatically

Creates log folders for each source/module

Writes log entries with

timestamp

log level (INFO / WARN / ERROR)

source name

message

Keeps one log file per day

CLI tool (LogApp) to:

browse available log folders

open a selected log file

delete a selected log file

Structure
LoggingModule
 ├── LogApp.java
 ├── LogLevel.java
 └── LoggingManager.java

LoggingManager

Handles all file operations:

sets up folders inside src/main/java/logs/

writes messages into the correct subfolder

one shared instance (singleton) to avoid conflicts

separate folder for each source, plus a system-wide log

LogLevel

Simple enum with the three levels used throughout the project:

INFO
WARN
ERROR

LogApp

Small console program that lets you interact with the logs:

Shows available log folders

Shows subfolders (if any)

Lists log files for that date

Option to open or delete the file

Useful mainly for checking logs while testing.

How to use the logger in other modules
LoggingManager logger = LoggingManager.getInstance();
logger.log("message here", LogLevel.INFO, "ModuleName");


ModuleName becomes the folder where the logs are stored.

Daily Log Files

Files follow this format:

YYYY-MM-DD.log


Example entry:

[2025-12-11 15:43:12][INFO][StorageModule] Item moved successfully

Notes

The logger creates missing folders automatically

Deleting logs only removes the file for the selected date

Open action prints the log contents in the console

More exceptions and test cases can be added (still to be completed)
