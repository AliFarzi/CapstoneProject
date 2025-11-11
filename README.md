# Warehouse Management System (Team 11)

[![Java](https://img.shields.io/badge/Java-17+-orange.svg)](https://www.oracle.com/java/)
[![Thread-Safe](https://img.shields.io/badge/Thread--Safe-✓-green.svg)](https://github.com)

> A Thread-safe warehouse management system with automated AGV coordination, real-time storage management, and intelligent charging orchestration.

---

##  Features

###  **Core Capabilities**
-  **Thread-Safe Operations** - Handles concurrent AGV operations without race conditions
-  **3D Warehouse Grid** - Full 3D storage with rows × columns × levels
- **Automated AGVs** - Self-coordinating autonomous guided vehicles
-  **Smart Charging** - Intelligent battery management and charging queue
- **Real-Time Logging** - Comprehensive activity tracking
- **Exception Handling** - Robust error management

---

## 📦 Project Structure
```
CAPSTONE_FINAL/
│
├──  StorageModule/          # Warehouse storage management
│   ├── model/
│   │   ├── Cell.java          # Individual storage cell
│   │   ├── Item.java          # Stored items
│   │   ├── Position.java      # 3D coordinates
│   │   └── Storage.java       # Warehouse grid
│   ├── service/
│   │   └── StorageManager.java # Thread-safe operations
│   └── exceptions/
│
├──  EquipmentModule/        # AGV and equipment management
│   ├── model/
│   │   ├── AGV.java           # Autonomous Guided Vehicle
│   │   ├── Equipment.java     # Base equipment class
│   │   ├── ChargingStation.java
│   │   ├── Crane.java
│   │   └── Shuttle.java
│   └── service/
│       └── EquipmentManager.java # Thread-safe equipment control
│
├──  TaskModule/             # Task orchestration
│   ├── ChargingTask.java      # Battery charging operations
│   ├── WarehouseTask.java     # Storage operations
│   ├── TaskManager.java       # Main orchestrator
│   └── SimulateProject.java   # Testing suite
│
└──  LoggingModule/          # Activity logging
    └── LoggingManager.java
```

---

## Thread Safety Architecture

### **Synchronization Strategy**

Our system uses **single-layer synchronization** at the service layer for optimal performance and simplicity.

#### **StorageManager (4 synchronized methods)**
```java
 synchronized void addItem(Item, Position)      // Manual placement
 synchronized void addItem(Item)                 // Auto placement + cell locking
 synchronized Item retrieveItem(Position)        // Item retrieval
 synchronized void moveItem(Position, Position)  // Item relocation
```

@everyone (we can add each synch methods here)
or we can remove this whole section


### **Why This Works**
```
Multiple AGVs (Threads)
         ↓
    [Synchronized Methods] ← Single gatekeeper
         ↓
    Only ONE AGV enters at a time
         ↓
    Model classes (Cell, Item, etc.) ← No synchronization needed
```
---

##  Quick Start

### **Prerequisites**
- Java 17 or higher
- No external dependencies required!

### **Compilation**
```bash
Command @Everyone
```

### **Running Tests**
```bash
Commands @Everyone
```

---

##  Test Results

Here we can put screenshot of test results!
@Everyone


---

---

##  Architecture Decisions

### **Why Cell Locking?**
```java
// Find and lock cell atomically
for (Cell c : cells) {
    if (c.isAvailable()) {
        c.lock();  // Reserve immediately
        cell = c;
        break;
    }
}
```
- Prevents double-booking even after synchronized method exits
- Defensive programming for safety
- Minimal performance impact


---


## Team
**Contributors:**
- Ali Farzizada MDT - 7223836
- Usman Rangrez  MDT - 7224089
- Ali Shaaban MDT - 7224591
- Priyanka Gupta MDT - 7224279

---




## Quick Reference

### **Common Exceptions**
@everyone (same we can remove it whole or add common exceptions here)

```java
StorageFullException       // No available cells
CellOccupiedException     // Cell already has item
CellLockedException       // Cell is locked by another AGV
CellEmptyException        // Trying to retrieve from empty cell
CellNotFoundException     // Invalid position
```

### **Item Status Flow**
```
STORED → MOVING → STORED → ... → RETRIEVED
```

### **Equipment Status Flow**
```
IDLE → BUSY → IDLE → CHARGING → IDLE
```

---
