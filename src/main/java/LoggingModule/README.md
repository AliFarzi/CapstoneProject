# Logging Module

A simple Java module that handles all project logging.
It creates daily log files, separates system and module logs, and comes with a small CLI to view and delete logs.
The logger uses a Singleton pattern, and all file operations are wrapped in custom exceptions for clearer error messages.

---

# Features

* One shared logger for the whole project (Singleton)
* Daily log file creation
* Separate folders for system + module logs
* Log levels: INFO, WARNING, ERROR
* CLI tool for browsing and deleting logs
* Custom exceptions for read/write/delete issues
* Easy to plug into any other module

---

# Folder Structure

```
/logging-module
│
├─ LoggingManager.java          # Core logger
├─ LogApp.java                  # CLI interface
├─ LoggingException.java        # Base exception
├─ exceptions/                  # Read/Write/Delete exceptions
├─ LogLevel.java                # Enum
└─ logs/
    ├─ system/
    └─ modules/                 # storage / equipment / task / simulation
```

---

## Quick Usage

# Write a log

```java
LoggingManager logger = LoggingManager.getInstance();
logger.log("Storage module initialized", LogLevel.INFO);
```

# Logs folder layout

```
logs/
   system/2025-11-14.log
   modules/storage/2025-11-14.log
```

# Run CLI

```
> View system logs
> View module logs
> Delete logs
```

---

# Exceptions

* **LogReadException** – could not read a file
* **LogWriteException** – writing failed
* **LogDeleteException** – delete operation failed

All of them extend `LoggingException`.

---

# Notes

* Always access the logger through `LoggingManager.getInstance()`
* File paths and directories are handled internally
* The module is kept lightweight and easy to understand


If you want, I can also create a **short version**, **presentation version**, or a **profile README style**.
