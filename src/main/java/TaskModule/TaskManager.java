package TaskModule;

import EqiupmentModule.model.*;
import EqiupmentModule.service.EquipmentManager;
import StorageModule.model.Position;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TaskManager {

    public static void main(String[] args) {



        double waitTime = 2*1000; // 2 seconds in milliseconds

        Random random = new Random();

        EquipmentManager equipmentManager = new EquipmentManager();

        int K = 3; // create 3 stations
        List<ChargingStation> stations = new ArrayList<>();

        for (int i = 1; i <= K; i++) {
            String stationId = "CS" + String.format("%03d", i);

            // Random position (x, y, z)
            Position position = new Position(
            random.nextInt(20),  // x coordinate 0-19
            random.nextInt(20),  // y coordinate 0-19
            0                    // z = 0
        );

        // Random power between 1 and 5 kW
        double powerKW = 1 + random.nextInt(5);

        ChargingStation station = new ChargingStation(stationId, position, powerKW);
        stations.add(station);
}

        ExecutorService executor = Executors.newFixedThreadPool(stations.size());

      
        int N = 10; // create 10 AGVs

        System.out.println("Creating and assigning AGVs to Charging Stations..." + N + " AGVs to be created, and " + K + " Stations available.");

         // Step 3: Dynamically create AGVs and assign if queue time < waitTime
        int agvCounter = 1;

        while (agvCounter <= N) {
            // Random battery between 30% and 80%
            double batteryLevel = 30 + random.nextInt(51);

            // Random speed between 20 and 40
            double speed = 20 + random.nextInt(21);

            AGV newAgv = new AGV(
                    "AGV" + String.format("%03d", agvCounter),
                    new Position(random.nextInt(5), random.nextInt(5), 0),
                    speed,
                    batteryLevel,
                    100, 100
            );

            equipmentManager.addEquipment(newAgv);

            // Find first station with queue time < waitTime
            boolean assigned = false;
            for (ChargingStation station : stations) {
                if (station.calculateQueueTime() < waitTime) {
                    station.assignEquipment(newAgv);

                    // Submit the charging task immediately
                    ChargingTask task = new ChargingTask(equipmentManager, station, newAgv, "Task-" + newAgv.getId());
                    executor.submit(task);
                    assigned = true;
                    System.out.println(newAgv.getId() + " assigned to " + station.getId());
                    break;
                }
            }

            if (!assigned) {
                System.err.println(newAgv.getId() + " could not be assigned — wait time exceeded.");
            }

            agvCounter++;

            // simulate random arrival delay
            try {
                Thread.sleep(500 + random.nextInt(500)); // 0.5-1 seconds between AGVs
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        // Shutdown executor after tasks complete
        executor.shutdown();

    }
}