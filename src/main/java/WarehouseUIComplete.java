import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.collections.FXCollections;
import javafx.animation.FadeTransition;
import javafx.util.Duration;

import StorageModule.model.*;
import StorageModule.service.StorageManager;
import StorageModule.exceptions.*;
import EqiupmentModule.model.*;
import EqiupmentModule.service.EquipmentManager;
import TaskModule.ChargingTask;

import java.util.concurrent.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class WarehouseUIComplete extends Application {
    
    // Backend
    private StorageManager warehouse;
    private EquipmentManager equipmentManager;
    private ExecutorService executor;
    private List<ChargingStation> chargingStations;
    
    // UI
    private ComboBox<String> vehicleDropdown;
    private ComboBox<String> itemDropdown;
    private ComboBox<String> taskDropdown;
    private ComboBox<String> positionDropdown;
    private ComboBox<String> fromPositionDropdown;
    private ComboBox<String> toPositionDropdown;
    private TextArea runningTasksArea;
    private TextArea chargingStationArea;
    private GridPane warehouseGrid;
    private Label statusLabel;
    private Label activeTasksLabel;
    private VBox positionSection;
    private VBox toastContainer;
    
    // ✅ Task Queue UI
    private ListView<String> taskQueueList;
    private List<QueuedTask> taskQueue = new ArrayList<>();
    
    // Stats labels
    private Label totalCellsLabel;
    private Label availableLabel;
    private Label occupiedLabel;
    private Label usageLabel;
    private Label equipmentLabel;
    
    // Data
    private Map<String, Item> itemsMap = new ConcurrentHashMap<>();
    private int gridX, gridY, gridZ;
    private AtomicInteger activeTasks = new AtomicInteger(0);
    
    @Override
    public void start(Stage primaryStage) {
        showSetupDialog(primaryStage);
    }
    
    // ✅ Task Queue Structure
    static class QueuedTask {
        String vehicle;
        String item;
        String task;
        String position;
        String fromPosition;
        String toPosition;
        
        public QueuedTask(String vehicle, String item, String task, String position, String fromPosition, String toPosition) {
            this.vehicle = vehicle;
            this.item = item;
            this.task = task;
            this.position = position;
            this.fromPosition = fromPosition;
            this.toPosition = toPosition;
        }
        
        @Override
        public String toString() {
            String vehicleId = vehicle != null ? vehicle.split(" - ")[0] : "N/A";
            String itemId = item != null ? item.split(" - ")[0] : "N/A";
            
            if (task.contains("Charge")) {
                return String.format("🔋 %s: Charge %s", vehicleId, vehicleId);
            } else if (task.contains("Move")) {
                return String.format("🚚 %s: Move item %s → %s", vehicleId, fromPosition, toPosition);
            } else if (task.contains("Store")) {
                String pos = position != null ? position : "Auto";
                return String.format("📦 %s: Store %s at %s", vehicleId, itemId, pos);
            } else if (task.contains("Retrieve")) {
                return String.format("📤 %s: Retrieve from %s", vehicleId, position);
            }
            return task;
        }
    }
    
    private void showSetupDialog(Stage primaryStage) {
        Stage dialogStage = new Stage();
        dialogStage.setTitle("🏭 Warehouse Setup");
        
        VBox dialogContent = new VBox(25);
        dialogContent.setPadding(new Insets(30));
        dialogContent.setAlignment(Pos.CENTER);
        dialogContent.setStyle("-fx-background-color: linear-gradient(to bottom, #667eea 0%, #764ba2 100%);");
        
        Label title = new Label("🏗️ Complete Warehouse Setup");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        title.setTextFill(Color.WHITE);
        
        Label gridLabel = new Label("Warehouse Dimensions");
        gridLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        gridLabel.setTextFill(Color.web("#FFD700"));
        
        HBox gridBox = new HBox(15);
        gridBox.setAlignment(Pos.CENTER);
        
        TextField xField = createStyledTextField("5");
        TextField yField = createStyledTextField("5");
        TextField zField = createStyledTextField("2");
        
        gridBox.getChildren().addAll(
            createInputGroup("Rows:", xField),
            createInputGroup("Columns:", yField),
            createInputGroup("Levels:", zField)
        );
        
        Label vehicleLabel = new Label("Equipment Configuration");
        vehicleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        vehicleLabel.setTextFill(Color.web("#FFD700"));
        
        HBox vehicleBox = new HBox(15);
        vehicleBox.setAlignment(Pos.CENTER);
        
        TextField agvField = createStyledTextField("5");
        TextField shuttleField = createStyledTextField("2");
        TextField craneField = createStyledTextField("1");
        
        vehicleBox.getChildren().addAll(
            createInputGroup("AGVs:", agvField),
            createInputGroup("Shuttles:", shuttleField),
            createInputGroup("Cranes:", craneField)
        );
        
        Label itemLabel = new Label("Inventory Configuration");
        itemLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        itemLabel.setTextFill(Color.web("#FFD700"));
        
        HBox itemBox = new HBox(15);
        itemBox.setAlignment(Pos.CENTER);
        
        TextField itemField = createStyledTextField("20");
        itemBox.getChildren().add(createInputGroup("Number of Items:", itemField));
        
        Label stationLabel = new Label("Charging Stations");
        stationLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        stationLabel.setTextFill(Color.web("#FFD700"));
        
        HBox stationBox = new HBox(15);
        stationBox.setAlignment(Pos.CENTER);
        
        TextField stationField = createStyledTextField("3");
        stationBox.getChildren().add(createInputGroup("Stations:", stationField));
        
        Button createBtn = new Button("🚀 Initialize System");
        createBtn.setStyle("""
            -fx-background-color: #4CAF50;
            -fx-text-fill: white;
            -fx-font-size: 16px;
            -fx-font-weight: bold;
            -fx-padding: 15 40;
            -fx-background-radius: 25;
            -fx-cursor: hand;
        """);
        
        createBtn.setOnAction(e -> {
            try {
                gridX = Integer.parseInt(xField.getText());
                gridY = Integer.parseInt(yField.getText());
                gridZ = Integer.parseInt(zField.getText());
                
                int numAGVs = Integer.parseInt(agvField.getText());
                int numShuttles = Integer.parseInt(shuttleField.getText());
                int numCranes = Integer.parseInt(craneField.getText());
                int numItems = Integer.parseInt(itemField.getText());
                int numStations = Integer.parseInt(stationField.getText());
                
                if (gridX <= 0 || gridY <= 0 || gridZ <= 0) {
                    showError("Grid dimensions must be positive!");
                    return;
                }
                
                dialogStage.close();
                initializeWarehouse(numAGVs, numShuttles, numCranes, numItems, numStations);
                showMainUI(primaryStage);
                
            } catch (NumberFormatException ex) {
                showError("Please enter valid numbers!");
            }
        });
        
        dialogContent.getChildren().addAll(
            title, 
            gridLabel, gridBox,
            vehicleLabel, vehicleBox,
            itemLabel, itemBox,
            stationLabel, stationBox,
            createBtn
        );
        
        ScrollPane scrollPane = new ScrollPane(dialogContent);
        scrollPane.setFitToWidth(true);
        
        Scene dialogScene = new Scene(scrollPane, 700, 650);
        dialogStage.setScene(dialogScene);
        dialogStage.setResizable(false);
        dialogStage.show();
    }
    
    private VBox createInputGroup(String label, TextField field) {
        VBox group = new VBox(5);
        group.setAlignment(Pos.CENTER);
        
        Label lbl = new Label(label);
        lbl.setTextFill(Color.WHITE);
        lbl.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        
        group.getChildren().addAll(lbl, field);
        return group;
    }
    
    private TextField createStyledTextField(String defaultValue) {
        TextField field = new TextField(defaultValue);
        field.setPrefWidth(100);
        field.setStyle("""
            -fx-background-color: white;
            -fx-background-radius: 10;
            -fx-padding: 10;
            -fx-font-size: 14px;
        """);
        return field;
    }
    
    private void initializeWarehouse(int numAGVs, int numShuttles, int numCranes, int numItems, int numStations) {
        Storage storage = new Storage("WH-MAIN", "Main Warehouse", gridX, gridY, gridZ);
        warehouse = new StorageManager(storage);
        equipmentManager = new EquipmentManager();
        executor = Executors.newFixedThreadPool(20);
        chargingStations = new ArrayList<>();
        
        Random random = new Random();
        
        for (int i = 1; i <= numStations; i++) {
            ChargingStation station = new ChargingStation(
                "CS" + String.format("%03d", i),
                new Position(random.nextInt(20), random.nextInt(20), 0),
                2.0 + random.nextInt(3)
            );
            chargingStations.add(station);
        }
        
        for (int i = 1; i <= numAGVs; i++) {
            AGV agv = new AGV(
                "AGV" + String.format("%03d", i),
                new Position(random.nextInt(gridX), random.nextInt(gridY), 0),
                20 + random.nextInt(20),
                50 + random.nextInt(40),
                100, 100
            );
            equipmentManager.addEquipment(agv);
        }
        
        for (int i = 1; i <= numShuttles; i++) {
            Shuttle shuttle = new Shuttle(
                "SH" + String.format("%03d", i),
                new Position(random.nextInt(gridX), random.nextInt(gridY), 0),
                15 + random.nextInt(15),
                60 + random.nextInt(30),
                80
            );
            equipmentManager.addEquipment(shuttle);
        }
        
        for (int i = 1; i <= numCranes; i++) {
            Crane crane = new Crane(
                "CR" + String.format("%03d", i),
                new Position(random.nextInt(gridX), random.nextInt(gridY), 0),
                10 + random.nextInt(10),
                70 + random.nextInt(20),
                200
            );
            equipmentManager.addEquipment(crane);
        }
        
        for (int i = 1; i <= numItems; i++) {
            Item item = new Item(
                "ITEM-" + String.format("%03d", i),
                "Product " + i,
                1.0 + random.nextDouble() * 5.0,
                new Position(0, 0, 0)
            );
            itemsMap.put(item.getId(), item);
        }
    }
    
    private void showMainUI(Stage primaryStage) {
        StackPane root = new StackPane();
        
        BorderPane mainLayout = new BorderPane();
        mainLayout.setStyle("-fx-background-color: #F5F5F5;");
        
        mainLayout.setTop(createHeader());
        mainLayout.setCenter(createMainContent());
        mainLayout.setBottom(createStatusBar());
        
        toastContainer = new VBox(10);
        toastContainer.setAlignment(Pos.TOP_RIGHT);
        toastContainer.setPadding(new Insets(20));
        toastContainer.setPickOnBounds(false);
        
        root.getChildren().addAll(mainLayout, toastContainer);
        StackPane.setAlignment(toastContainer, Pos.TOP_RIGHT);
        
        Scene scene = new Scene(root, 1600, 950);
        primaryStage.setTitle("🏭 Smart Warehouse Management System");
        primaryStage.setScene(scene);
        primaryStage.setMaximized(true);
        primaryStage.show();
        
        startUIUpdates();
    }
    
    private VBox createHeader() {
        VBox header = new VBox(10);
        header.setPadding(new Insets(20));
        header.setStyle("-fx-background-color: linear-gradient(to right, #667eea 0%, #764ba2 100%);");
        
        Label title = new Label("🏭 Smart Warehouse Management System");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 28));
        title.setTextFill(Color.WHITE);
        
        int numAGVs = (int) equipmentManager.getAll().stream()
            .filter(e -> e instanceof AGV).count();
        int numShuttles = (int) equipmentManager.getAll().stream()
            .filter(e -> e instanceof Shuttle).count();
        int numCranes = (int) equipmentManager.getAll().stream()
            .filter(e -> e instanceof Crane).count();
        
        Label subtitle = new Label(
            String.format("Grid: %d×%d×%d | Capacity: %d cells | AGVs: %d | Shuttles: %d | Cranes: %d | Items: %d", 
            gridX, gridY, gridZ, gridX * gridY * gridZ, 
            numAGVs, numShuttles, numCranes, itemsMap.size())
        );
        subtitle.setFont(Font.font("Arial", 14));
        subtitle.setTextFill(Color.web("#E0E0E0"));
        
        activeTasksLabel = new Label("⚡ Active Tasks: 0");
        activeTasksLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        activeTasksLabel.setTextFill(Color.web("#FFD700"));
        
        header.getChildren().addAll(title, subtitle, activeTasksLabel);
        return header;
    }
    
    private HBox createMainContent() {
        HBox mainContent = new HBox(20);
        mainContent.setPadding(new Insets(20));
        
        VBox leftPanel = createControlPanel();
        leftPanel.setPrefWidth(450);
        
        VBox rightPanel = createVisualizationPanel();
        HBox.setHgrow(rightPanel, Priority.ALWAYS);
        
        mainContent.getChildren().addAll(leftPanel, rightPanel);
        return mainContent;
    }
    
    private VBox createControlPanel() {
        VBox panel = new VBox(20);
        panel.setStyle("""
            -fx-background-color: white;
            -fx-background-radius: 10;
            -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 0);
        """);
        panel.setPadding(new Insets(20));
        
        panel.getChildren().addAll(
            createDropdownSection(),
            createTaskQueueSection(), // ✅ NEW: Task Queue
            createRunningTasksSection(),
            createChargingSection()
        );
        
        return panel;
    }
    
    private VBox createDropdownSection() {
        VBox section = new VBox(15);
        
        Label sectionTitle = createSectionTitle("⚙️ Task Configuration");
        
        vehicleDropdown = new ComboBox<>();
        vehicleDropdown.setPromptText("Select Equipment");
        vehicleDropdown.setPrefWidth(Double.MAX_VALUE);
        updateVehicleDropdown();
        
        itemDropdown = new ComboBox<>();
        itemDropdown.setPromptText("Select Item");
        itemDropdown.setPrefWidth(Double.MAX_VALUE);
        updateItemDropdown();
        
        taskDropdown = new ComboBox<>();
        taskDropdown.setItems(FXCollections.observableArrayList(
            "Store Item (Auto)",
            "Store Item (Manual Position)",
            "Retrieve Item",
            "Move Item",
            "Charge Equipment"
        ));
        taskDropdown.setPromptText("Select Task");
        taskDropdown.setPrefWidth(Double.MAX_VALUE);
        
        positionSection = new VBox(10);
        positionSection.setVisible(false);
        positionSection.setManaged(false);
        
        positionDropdown = new ComboBox<>();
        positionDropdown.setPromptText("Select Position");
        positionDropdown.setPrefWidth(Double.MAX_VALUE);
        updatePositionDropdown();
        
        fromPositionDropdown = new ComboBox<>();
        fromPositionDropdown.setPromptText("From Position");
        fromPositionDropdown.setPrefWidth(Double.MAX_VALUE);
        updatePositionDropdown(fromPositionDropdown);
        
        toPositionDropdown = new ComboBox<>();
        toPositionDropdown.setPromptText("To Position");
        toPositionDropdown.setPrefWidth(Double.MAX_VALUE);
        updatePositionDropdown(toPositionDropdown);
        
        taskDropdown.setOnAction(e -> updatePositionFields());
        
        section.getChildren().addAll(
            sectionTitle,
            createLabeledControl("🚗 Equipment:", vehicleDropdown),
            createLabeledControl("📦 Item:", itemDropdown),
            createLabeledControl("⚡ Task:", taskDropdown),
            positionSection
        );
        
        return section;
    }
    
    // ✅ NEW: Task Queue Section
    private VBox createTaskQueueSection() {
        VBox section = new VBox(10);
        
        Label sectionTitle = createSectionTitle("📋 Task Queue (Concurrent Execution)");
        
        taskQueueList = new ListView<>();
        taskQueueList.setPrefHeight(120);
        taskQueueList.setPlaceholder(new Label("No tasks queued. Add tasks above."));
        
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);
        
        Button addBtn = new Button("➕ Add to Queue");
        addBtn.setStyle("""
            -fx-background-color: #2196F3;
            -fx-text-fill: white;
            -fx-font-size: 12px;
            -fx-font-weight: bold;
            -fx-padding: 10 20;
            -fx-background-radius: 15;
            -fx-cursor: hand;
        """);
        addBtn.setOnAction(e -> addTaskToQueue());
        
        Button executeAllBtn = new Button("🚀 Execute All (" + taskQueue.size() + ")");
        executeAllBtn.setStyle("""
            -fx-background-color: #4CAF50;
            -fx-text-fill: white;
            -fx-font-size: 12px;
            -fx-font-weight: bold;
            -fx-padding: 10 20;
            -fx-background-radius: 15;
            -fx-cursor: hand;
        """);
        executeAllBtn.setOnAction(e -> executeAllTasks());
        
        Button clearQueueBtn = new Button("🗑️ Clear Queue");
        clearQueueBtn.setStyle("""
            -fx-background-color: #f44336;
            -fx-text-fill: white;
            -fx-font-size: 12px;
            -fx-font-weight: bold;
            -fx-padding: 10 20;
            -fx-background-radius: 15;
            -fx-cursor: hand;
        """);
        clearQueueBtn.setOnAction(e -> clearQueue());
        
        buttonBox.getChildren().addAll(addBtn, executeAllBtn, clearQueueBtn);
        
        section.getChildren().addAll(sectionTitle, taskQueueList, buttonBox);
        return section;
    }
    
    // ✅ Add task to queue
    private void addTaskToQueue() {
        String vehicle = vehicleDropdown.getValue();
        String item = itemDropdown.getValue();
        String task = taskDropdown.getValue();
        
        if (task == null) {
            showToast("⚠️ Please select a task!", "warning");
            return;
        }
        
        if (vehicle == null) {
            showToast("⚠️ Please select equipment!", "warning");
            return;
        }
        
        if ((task.contains("Store") || task.contains("Retrieve")) && item == null) {
            showToast("⚠️ Please select an item!", "warning");
            return;
        }
        
        String position = positionDropdown.getValue();
        String fromPosition = fromPositionDropdown.getValue();
        String toPosition = toPositionDropdown.getValue();
        
        QueuedTask qTask = new QueuedTask(vehicle, item, task, position, fromPosition, toPosition);
        taskQueue.add(qTask);
        
        taskQueueList.setItems(FXCollections.observableArrayList(
            taskQueue.stream().map(QueuedTask::toString).toList()
        ));
        
        showToast("✅ Task added to queue! Total: " + taskQueue.size(), "success");
        logTask("📋 Added to queue: " + qTask.toString());
    }
    
    // ✅ Execute all tasks concurrently
    private void executeAllTasks() {
        if (taskQueue.isEmpty()) {
            showToast("⚠️ Queue is empty!", "warning");
            return;
        }
        
        int numTasks = taskQueue.size();
        logTask(String.format("🚀 Starting %d concurrent tasks...", numTasks));
        showToast(String.format("🚀 Executing %d tasks concurrently!", numTasks), "info");
        
        CountDownLatch latch = new CountDownLatch(numTasks);
        List<QueuedTask> tasksToExecute = new ArrayList<>(taskQueue);
        taskQueue.clear();
        taskQueueList.getItems().clear();
        
        for (int i = 0; i < tasksToExecute.size(); i++) {
            final QueuedTask qTask = tasksToExecute.get(i);
            final int taskIndex = i + 1;
            
            executor.submit(() -> {
                String taskId = "CONCURRENT-" + taskIndex;
                activeTasks.incrementAndGet();
                updateActiveTasksLabel();
                
                try {
                    String vehicleId = qTask.vehicle.split(" - ")[0];
                    logTask(String.format("🚀 [%s] %s starting...", taskId, vehicleId));
                    
                    executeQueuedTask(taskId, qTask);
                    
                    logTask(String.format("✅ [%s] %s completed", taskId, vehicleId));
                    
                } catch (Exception e) {
                    logTask(String.format("❌ [%s] Failed: %s", taskId, e.getMessage()));
                } finally {
                    activeTasks.decrementAndGet();
                    updateActiveTasksLabel();
                    latch.countDown();
                }
            });
        }
        
        executor.submit(() -> {
            try {
                latch.await();
                logTask("🎉 All concurrent tasks completed!");
                showToast("🎉 All tasks completed!", "success");
                Platform.runLater(() -> {
                    updateWarehouseGrid();
                    updateStatistics();
                    updateChargingStations();
                });
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
    }
    
    // ✅ Execute a queued task
    private void executeQueuedTask(String taskId, QueuedTask qTask) throws Exception {
        String vehicleId = qTask.vehicle.split(" - ")[0];
        
        switch (qTask.task) {
            case "Store Item (Auto)":
                performStoreTaskAuto(taskId, qTask.item);
                break;
                
            case "Store Item (Manual Position)":
                String itemId = qTask.item.split(" - ")[0];
                Item item = itemsMap.get(itemId);
                Position position = parsePosition(qTask.position);
                
                logTask(String.format("  [%s] %s moving to position %s...", taskId, vehicleId, position));
                Thread.sleep(1500);
                
                warehouse.addItem(item, position);
                
                logTask(String.format("  [%s] Stored %s at %s", taskId, itemId, position));
                Thread.sleep(500);
                break;
                
            case "Retrieve Item":
                Position retrievePos = parsePosition(qTask.position);
                
                logTask(String.format("  [%s] %s moving to %s...", taskId, vehicleId, retrievePos));
                Thread.sleep(1500);
                
                Item retrievedItem = warehouse.retrieveItem(retrievePos);
                
                logTask(String.format("  [%s] Retrieved %s from %s", taskId, retrievedItem.getId(), retrievePos));
                Thread.sleep(1000);
                break;
                
            case "Move Item":
                Position from = parsePosition(qTask.fromPosition);
                Position to = parsePosition(qTask.toPosition);
                
                logTask(String.format("  [%s] %s moving to %s...", taskId, vehicleId, from));
                Thread.sleep(1500);
                
                logTask(String.format("  [%s] Picking up item...", taskId));
                Thread.sleep(1000);
                
                warehouse.moveItem(from, to);
                
                logTask(String.format("  [%s] Moved item: %s → %s", taskId, from, to));
                Thread.sleep(1500);
                break;
                
            case "Charge Equipment":
                performChargeTask(taskId, vehicleId);
                break;
        }
    }
    
    private void clearQueue() {
        taskQueue.clear();
        taskQueueList.getItems().clear();
        showToast("🗑️ Queue cleared!", "info");
    }
    
    private void updatePositionFields() {
        String task = taskDropdown.getValue();
        positionSection.getChildren().clear();
        positionSection.setVisible(false);
        positionSection.setManaged(false);
        
        if (task != null && task.contains("Charge")) {
            itemDropdown.setVisible(false);
            itemDropdown.setManaged(false);
        } else {
            itemDropdown.setVisible(true);
            itemDropdown.setManaged(true);
        }
        
        if (task == null) return;
        
        if (task.equals("Store Item (Manual Position)")) {
            positionSection.getChildren().add(createLabeledControl("📍 Position:", positionDropdown));
            positionSection.setVisible(true);
            positionSection.setManaged(true);
        } else if (task.equals("Retrieve Item")) {
            positionSection.getChildren().add(createLabeledControl("📍 From Position:", positionDropdown));
            positionSection.setVisible(true);
            positionSection.setManaged(true);
        } else if (task.equals("Move Item")) {
            positionSection.getChildren().addAll(
                createLabeledControl("📍 From:", fromPositionDropdown),
                createLabeledControl("📍 To:", toPositionDropdown)
            );
            positionSection.setVisible(true);
            positionSection.setManaged(true);
        }
    }
    
    private void updatePositionDropdown() {
        updatePositionDropdown(positionDropdown);
    }
    
    private void updatePositionDropdown(ComboBox<String> dropdown) {
        List<String> positions = new ArrayList<>();
        for (int level = 1; level <= gridZ; level++) {
            for (int row = 1; row <= gridX; row++) {
                for (int col = 1; col <= gridY; col++) {
                    positions.add(String.format("(%d,%d,%d)", row, col, level));
                }
            }
        }
        dropdown.setItems(FXCollections.observableArrayList(positions));
    }
    
    private VBox createRunningTasksSection() {
        VBox section = new VBox(10);
        
        Label sectionTitle = createSectionTitle("🔄 Running Tasks");
        
        runningTasksArea = new TextArea();
        runningTasksArea.setEditable(false);
        runningTasksArea.setPrefRowCount(6);
        runningTasksArea.setStyle("""
            -fx-control-inner-background: #F9F9F9;
            -fx-font-family: 'Courier New';
            -fx-font-size: 10px;
        """);
        runningTasksArea.setText("System ready. No active tasks.\n");
        
        section.getChildren().addAll(sectionTitle, runningTasksArea);
        return section;
    }
    
    private VBox createChargingSection() {
        VBox section = new VBox(10);
        
        Label sectionTitle = createSectionTitle("⚡ Charging Stations");
        
        chargingStationArea = new TextArea();
        chargingStationArea.setEditable(false);
        chargingStationArea.setPrefRowCount(4);
        chargingStationArea.setStyle("""
            -fx-control-inner-background: #F9F9F9;
            -fx-font-family: 'Courier New';
            -fx-font-size: 10px;
        """);
        updateChargingStations();
        
        section.getChildren().addAll(sectionTitle, chargingStationArea);
        return section;
    }
    
    private VBox createVisualizationPanel() {
        VBox panel = new VBox(20);
        
        panel.getChildren().addAll(
            createStatisticsSection(),
            createWarehouseGridSection()
        );
        
        return panel;
    }
    
    private VBox createWarehouseGridSection() {
        VBox section = new VBox(15);
        section.setStyle("""
            -fx-background-color: white;
            -fx-background-radius: 10;
            -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 0);
        """);
        section.setPadding(new Insets(20));
        
        Label sectionTitle = createSectionTitle("📦 Warehouse Grid (Level 1)");
        
        warehouseGrid = new GridPane();
        warehouseGrid.setHgap(5);
        warehouseGrid.setVgap(5);
        warehouseGrid.setAlignment(Pos.CENTER);
        
        updateWarehouseGrid();
        
        ScrollPane scrollPane = new ScrollPane(warehouseGrid);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setPrefHeight(450);
        
        section.getChildren().addAll(sectionTitle, scrollPane);
        return section;
    }
    
    private void updateWarehouseGrid() {
        warehouseGrid.getChildren().clear();
        
        for (int row = 1; row <= gridX; row++) {
            for (int col = 1; col <= gridY; col++) {
                Position pos = new Position(row, col, 1);
                
                try {
                    StorageModule.model.Cell cell = warehouse.getStorage().getCell(pos);
                    StackPane cellPane = createCellPane(cell);
                    warehouseGrid.add(cellPane, col - 1, row - 1);
                } catch (Exception e) {
                    // Cell not found
                }
            }
        }
    }
    
    private StackPane createCellPane(StorageModule.model.Cell cell) {
        StackPane pane = new StackPane();
        pane.setPrefSize(50, 50);
        
        String style;
        String icon;
        
        if (cell.isLocked()) {
            style = "-fx-background-color: #FFA726; -fx-border-color: #F57C00; -fx-border-width: 2;";
            icon = "🔒";
        } else if (!cell.isEmpty()) {
            style = "-fx-background-color: #66BB6A; -fx-border-color: #388E3C; -fx-border-width: 2;";
            icon = "📦";
        } else {
            style = "-fx-background-color: #E0E0E0; -fx-border-color: #BDBDBD; -fx-border-width: 1;";
            icon = "";
        }
        
        pane.setStyle(style + " -fx-background-radius: 5;");
        
        Label iconLabel = new Label(icon);
        iconLabel.setFont(Font.font(20));
        
        Label posLabel = new Label(String.format("%d,%d", 
            cell.getPosition().getX(), cell.getPosition().getY()));
        posLabel.setFont(Font.font(7));
        posLabel.setStyle("-fx-text-fill: #666;");
        
        VBox content = new VBox(iconLabel, posLabel);
        content.setAlignment(Pos.CENTER);
        
        pane.getChildren().add(content);
        
        Tooltip tooltip = new Tooltip(
            "Cell: " + cell.getId() + "\n" +
            "Position: " + cell.getPosition() + "\n" +
            "Status: " + (cell.isEmpty() ? "Empty" : "Occupied") + "\n" +
            "Locked: " + cell.isLocked()
        );
        Tooltip.install(pane, tooltip);
        
        return pane;
    }
    
    private VBox createStatisticsSection() {
        VBox section = new VBox(15);
        section.setStyle("""
            -fx-background-color: white;
            -fx-background-radius: 10;
            -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 0);
        """);
        section.setPadding(new Insets(20));
        
        Label sectionTitle = createSectionTitle("📊 Real-Time Statistics");
        
        HBox statsBox = new HBox(20);
        statsBox.setAlignment(Pos.CENTER);
        
        int totalCells = gridX * gridY * gridZ;
        int availableCells = warehouse.countAvailableCells();
        int occupiedCells = totalCells - availableCells;
        double utilization = totalCells > 0 ? (occupiedCells / (double)totalCells) * 100 : 0;
        
        totalCellsLabel = new Label(String.valueOf(totalCells));
        availableLabel = new Label(String.valueOf(availableCells));
        occupiedLabel = new Label(String.valueOf(occupiedCells));
        usageLabel = new Label(String.format("%.1f%%", utilization));
        equipmentLabel = new Label(String.valueOf(equipmentManager.getAll().size()));
        
        statsBox.getChildren().addAll(
            createStatCard("📦 Total", totalCellsLabel, "#2196F3"),
            createStatCard("✅ Available", availableLabel, "#4CAF50"),
            createStatCard("🔴 Occupied", occupiedLabel, "#FF5722"),
            createStatCard("📈 Usage", usageLabel, "#9C27B0"),
            createStatCard("🚗 Equipment", equipmentLabel, "#FF9800")
        );
        
        section.getChildren().addAll(sectionTitle, statsBox);
        return section;
    }
    
    private VBox createStatCard(String title, Label valueLabel, String color) {
        VBox card = new VBox(8);
        card.setAlignment(Pos.CENTER);
        card.setStyle(String.format("""
            -fx-background-color: %s;
            -fx-background-radius: 10;
            -fx-padding: 15;
            -fx-min-width: 130;
        """, color));
        
        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        titleLabel.setTextFill(Color.WHITE);
        
        valueLabel.setFont(Font.font("Arial", FontWeight.BOLD, 28));
        valueLabel.setTextFill(Color.WHITE);
        
        card.getChildren().addAll(titleLabel, valueLabel);
        return card;
    }
    
    private HBox createStatusBar() {
        HBox statusBar = new HBox();
        statusBar.setPadding(new Insets(10, 20, 10, 20));
        statusBar.setStyle("-fx-background-color: #37474F;");
        
        statusLabel = new Label("✅ System Ready | Thread-Safe Operations Active");
        statusLabel.setTextFill(Color.web("#4CAF50"));
        statusLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        
        statusBar.getChildren().add(statusLabel);
        return statusBar;
    }
    
    private void performStoreTaskAuto(String taskId, String itemStr) throws Exception {
        if (itemStr == null) {
            throw new Exception("Please select an item!");
        }
        
        String itemId = itemStr.split(" - ")[0];
        Item item = itemsMap.get(itemId);
        
        if (item == null) {
            throw new Exception("Item not found: " + itemId);
        }
        
        logTask(String.format("  [%s] AGV picking up %s...", taskId, itemId));
        Thread.sleep(1000);
        
        logTask(String.format("  [%s] Finding available cell...", taskId));
        Thread.sleep(500);
        
        warehouse.addItem(item);
        
        logTask(String.format("  [%s] Stored %s at %s", taskId, itemId, item.getPosition()));
        Thread.sleep(500);
    }
    
    private void performChargeTask(String taskId, String vehicleId) throws Exception {
        Equipment equipment = findEquipmentById(vehicleId);
        if (equipment == null) {
            throw new Exception("Equipment not found!");
        }
        
        ChargingStation station = chargingStations.get(0);
        
        logTask(String.format("  [%s] %s moving to charging station...", taskId, vehicleId));
        Thread.sleep(1000);
        
        ChargingTask chargingTask = new ChargingTask(equipmentManager, station, equipment, taskId);
        chargingTask.run();
        
        logTask(String.format("  [%s] %s fully charged!", taskId, vehicleId));
    }
    
    private Position parsePosition(String posStr) {
        posStr = posStr.replace("(", "").replace(")", "");
        String[] parts = posStr.split(",");
        return new Position(
            Integer.parseInt(parts[0]),
            Integer.parseInt(parts[1]),
            Integer.parseInt(parts[2])
        );
    }
    
    private Equipment findEquipmentById(String id) {
        for (Equipment e : equipmentManager.getAll()) {
            if (e.getId().equals(id)) {
                return e;
            }
        }
        return null;
    }
    
    private Label createSectionTitle(String text) {
        Label label = new Label(text);
        label.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        label.setTextFill(Color.web("#333"));
        return label;
    }
    
    private VBox createLabeledControl(String labelText, Control control) {
        VBox box = new VBox(5);
        Label label = new Label(labelText);
        label.setFont(Font.font("Arial", FontWeight.BOLD, 11));
        box.getChildren().addAll(label, control);
        return box;
    }
    
    private void updateVehicleDropdown() {
        List<String> vehicles = new ArrayList<>();
        for (Equipment e : equipmentManager.getAll()) {
            vehicles.add(String.format("%s - %s (⚡%.0f%%)", 
                e.getId(), 
                e.getClass().getSimpleName(),
                e.getBatteryLevel()
            ));
        }
        vehicleDropdown.setItems(FXCollections.observableArrayList(vehicles));
    }
    
    private void updateItemDropdown() {
        List<String> items = new ArrayList<>();
        
        List<String> sortedIds = new ArrayList<>(itemsMap.keySet());
        Collections.sort(sortedIds);
        
        for (String itemId : sortedIds) {
            Item item = itemsMap.get(itemId);
            String status = item.getStatus() == Item.Status.STORED ? "📦 Stored" : 
                           item.getStatus() == Item.Status.MOVING ? "🚚 Moving" : "✅ Available";
            items.add(String.format("%s - %s (%s)", item.getId(), item.getDescription(), status));
        }
        
        itemDropdown.setItems(FXCollections.observableArrayList(items));
    }
    
    private void updateChargingStations() {
        StringBuilder sb = new StringBuilder();
        for (ChargingStation station : chargingStations) {
            sb.append(String.format("⚡ %s (%.1f kW)\n", 
                station.getId(), station.getPowerKW()));
            sb.append(String.format("   Status: %s\n", 
                station.isOccupied() ? "🔴 Busy" : "🟢 Available"));
            sb.append(String.format("   Queue: %d\n", 
                station.getAssignedEquipments().size()));
            sb.append(String.format("   Time: %.0fs\n\n", 
                station.calculateQueueTime() / 1000.0));
        }
        Platform.runLater(() -> chargingStationArea.setText(sb.toString()));
    }
    
    private void updateStatistics() {
        Platform.runLater(() -> {
            int totalCells = gridX * gridY * gridZ;
            int availableCells = warehouse.countAvailableCells();
            int occupiedCells = totalCells - availableCells;
            double utilization = totalCells > 0 ? (occupiedCells / (double)totalCells) * 100 : 0;
            
            totalCellsLabel.setText(String.valueOf(totalCells));
            availableLabel.setText(String.valueOf(availableCells));
            occupiedLabel.setText(String.valueOf(occupiedCells));
            usageLabel.setText(String.format("%.1f%%", utilization));
            equipmentLabel.setText(String.valueOf(equipmentManager.getAll().size()));
        });
    }
    
    private void logTask(String message) {
        Platform.runLater(() -> {
            String timestamp = String.format("%tT", System.currentTimeMillis());
            runningTasksArea.appendText(String.format("[%s] %s\n", timestamp, message));
            runningTasksArea.setScrollTop(Double.MAX_VALUE);
        });
    }
    
    private void updateActiveTasksLabel() {
        Platform.runLater(() -> {
            activeTasksLabel.setText("⚡ Active Tasks: " + activeTasks.get());
        });
    }
    
    private void startUIUpdates() {
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(() -> {
            Platform.runLater(() -> {
                try {
                    updateWarehouseGrid();
                    updateChargingStations();
                    updateVehicleDropdown();
                    updateItemDropdown();
                    updateStatistics();
                } catch (Exception e) {
                    System.err.println("Error updating UI: " + e.getMessage());
                }
            });
        }, 2, 2, TimeUnit.SECONDS);
    }
    
    private void showToast(String message, String type) {
        Platform.runLater(() -> {
            HBox toast = new HBox(10);
            toast.setAlignment(Pos.CENTER_LEFT);
            toast.setPadding(new Insets(15, 20, 15, 20));
            toast.setMaxWidth(350);
            
            String bgColor;
            String icon;
            
            switch (type) {
                case "success":
                    bgColor = "#4CAF50";
                    icon = "✅";
                    break;
                case "error":
                    bgColor = "#f44336";
                    icon = "❌";
                    break;
                case "warning":
                    bgColor = "#FF9800";
                    icon = "⚠️";
                    break;
                case "info":
                default:
                    bgColor = "#2196F3";
                    icon = "ℹ️";
                    break;
            }
            
            toast.setStyle(String.format("""
                -fx-background-color: %s;
                -fx-background-radius: 10;
                -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 15, 0, 0, 5);
            """, bgColor));
            
            Label iconLabel = new Label(icon);
            iconLabel.setFont(Font.font(20));
            
            Label messageLabel = new Label(message);
            messageLabel.setFont(Font.font("Arial", FontWeight.BOLD, 13));
            messageLabel.setTextFill(Color.WHITE);
            messageLabel.setWrapText(true);
            
            toast.getChildren().addAll(iconLabel, messageLabel);
            
            toastContainer.getChildren().add(toast);
            
            FadeTransition fadeIn = new FadeTransition(Duration.millis(300), toast);
            fadeIn.setFromValue(0);
            fadeIn.setToValue(1);
            fadeIn.play();
            
            FadeTransition fadeOut = new FadeTransition(Duration.millis(300), toast);
            fadeOut.setFromValue(1);
            fadeOut.setToValue(0);
            fadeOut.setDelay(Duration.millis(3000));
            fadeOut.setOnFinished(e -> toastContainer.getChildren().remove(toast));
            fadeOut.play();
        });
    }
    
    private void showError(String message) {
        showToast("❌ " + message, "error");
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}