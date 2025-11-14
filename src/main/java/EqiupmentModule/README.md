# Equipment Module

A reusable Java module to manage warehouse equipment (AGVs, Cranes, Charging Stations, and Shuttles).  
It provides an easy-to-use API to track equipment state, battery levels, position, and assign equipment to tasks.

---

## Features

- Multiple equipment types (AGV, Crane, Shuttle, ChargingStation)
- Equipment state management (IDLE, MOVING, BUSY, CHARGING, STOPPED, ERROR)
- Battery level tracking and management
- Position tracking in 3D warehouse space
- Assign equipment to tasks and release when complete
- Charging station integration
- Thread-safe operations with synchronization
- Export equipment data to CSV
- Logging integration for operation tracking

---

## Folder Structure

```
/equipment-module
│
├─ model/                      # Equipment classes (Equipment, AGV, Crane, Shuttle, ChargingStation)
├─ service/                    # Business logic (EquipmentManager)
├─ exceptions/                 # Custom exceptions
```

---

## Usage

### Step 1: Initialize Equipment

```java
import EqiupmentModule.model.*;
import EqiupmentModule.service.*;

EquipmentManager manager = new EquipmentManager();

AGV agv = new AGV("AGV001", new Position(1, 1, 1), 5.0, 100.0);
Crane crane = new Crane("CRANE001", new Position(2, 2, 1), 2.0, 100.0);
Shuttle shuttle = new Shuttle("SHUTTLE001", new Position(1, 2, 1), 3.5, 100.0);
ChargingStation station = new ChargingStation("CS001", new Position(5, 5, 1), 30.0);

manager.addEquipment(agv);
manager.addEquipment(crane);
manager.addEquipment(shuttle);
manager.addEquipment(station);
```

### Step 2: Get Equipment

```java
List<Equipment> all = manager.getAll();

List<Equipment> available = manager.getAvailableEquipment();

Equipment agv = manager.requireById("AGV001");
```

### Step 3: Assign Equipment to Task

```java
try {
    manager.assignToTask("AGV001");

    manager.release("AGV001");
} catch (EquipmentOperationException e) {
    System.out.println("Failed to assign equipment: " + e.getMessage());
}
```

### Step 4: Charge Equipment

```java
try {
    Equipment agv = manager.requireById("AGV001");
    ChargingStation station = (ChargingStation) manager.requireById("CS001");

    manager.sendToCharge(agv, station);
    manager.releaseFromCharge(agv, station);
} catch (EquipmentOperationException e) {
    System.out.println("Charging error: " + e.getMessage());
}
```

### Step 5: Track Equipment Status

```java
manager.printEquipmentInfo();

manager.exportEquipmentCsv(Path.of("equipment_status.csv"));
```

---

## Equipment Types

- **AGV** – Automated Guided Vehicle (high speed)
- **Crane** – Material handling crane (slower, heavy loads)
- **Shuttle** – Compact transport unit
- **ChargingStation** – Stationary charging point for equipment

---

## Equipment States

- **IDLE** – Not in use, ready for assignment
- **MOVING** – In transit to destination
- **BUSY** – Actively assigned to a task
- **CHARGING** – Connected to charging station
- **STOPPED** – Paused or on hold
- **ERROR** – Fault condition

---

## Exceptions

- **EquipmentNotFoundException** – Equipment ID does not exist
- **EquipmentUnavailableException** – Equipment cannot be assigned (busy or charging)
- **InvalidEquipmentStateException** – Invalid state transition or operation
- **EquipmentOperationException** – General operation failure
- **EquipmentChargeFullException** – Charging error or battery full
- **ResourceAccessException** – File I/O errors (CSV export)

All exceptions extend `EquipmentOperationException`.

---

## Notes

- Use `EquipmentManager` as the single entry point for all equipment operations
- Battery level automatically decreases on task release
- Minimum battery of 10% required to assign equipment to tasks
- All operations are thread-safe and synchronized
- Equipment is integrated with the Logging Module for operation tracking
