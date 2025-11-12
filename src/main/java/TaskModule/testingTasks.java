package TaskModule;

import EqiupmentModule.model.*;
import EqiupmentModule.service.EquipmentManager;
import StorageModule.model.Position;

import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class testingTasks {
    
    public static void main(String[] args) {
        

        Random random = new Random();

        ChargingStation station = new ChargingStation("CS001", new Position(10, 10, 0), 3.0);
        Shuttle shuttle = new Shuttle("Sh001", new Position(0, 0, 0), 5.0, 50.0, 100.0);

        EquipmentManager equipmentManager = new EquipmentManager();

        equipmentManager.addEquipment(shuttle);

        ExecutorService executor = Executors.newFixedThreadPool(5);

        ChargingTask chargingTask = new ChargingTask(equipmentManager, station, shuttle, "Task001");
        ChargingTask chargingTask2 = new ChargingTask(equipmentManager, station, shuttle, "Task002");
        executor.submit(chargingTask);
        executor.submit(chargingTask2); 
        executor.shutdown();


    }
}
