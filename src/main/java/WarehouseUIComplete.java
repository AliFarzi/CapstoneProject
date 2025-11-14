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
import TaskModule.*;

import java.util.concurrent.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.extension.ExtensionContext.Store;

import java.io.File;

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
    
    // Task Queue UI
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
    
    // Task Queue Structure
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
    
    // Log File Item Class
    static class LogFileItem {
        String fileName;
        String folderPath;
        File file;
        long fileSize;
        
        public LogFileItem(String fileName, String folderPath, File file) {
            this.fileName = fileName;
            this.folderPath = folderPath;
            this.file = file;
            this.fileSize = file.length();
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
        
        HBox topRow = new HBox();
        topRow.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(topRow, Priority.ALWAYS);
        
        Label title = new Label("🏭 Smart Warehouse Management System");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 28));
        title.setTextFill(Color.WHITE);
        HBox.setHgrow(title, Priority.ALWAYS);
        
        Button checkLogsBtn = new Button("📋 Check Logs");
        checkLogsBtn.setStyle("""
            -fx-background-color: #FFD700;
            -fx-text-fill: #333;
            -fx-font-size: 14px;
            -fx-font-weight: bold;
            -fx-padding: 10 20;
            -fx-background-radius: 20;
            -fx-cursor: hand;
        """);
        checkLogsBtn.setOnAction(e -> openLogsViewer());
        
        topRow.getChildren().addAll(title, checkLogsBtn);
        
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
        
        header.getChildren().addAll(topRow, subtitle, activeTasksLabel);
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
            createTaskQueueSection(),
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
    
    private void executeQueuedTask(String taskId, QueuedTask qTask) throws Exception {
        String vehicleId = qTask.vehicle.split(" - ")[0];
        
        switch (qTask.task) {
            case "Store Item (Auto)":
                performStoreTaskAuto(taskId, qTask.item, vehicleId);
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
    
    // ============ LOG VIEWER METHODS ============
    
    private void openLogsViewer() {
        Stage logStage = new Stage();
        logStage.setTitle("📋 Log Management System");
        
        BorderPane logLayout = new BorderPane();
        logLayout.setStyle("-fx-background-color: #F5F5F5;");
        
        TreeView<String> folderTree = createLogFolderTree();
        folderTree.setPrefWidth(300);
        
        VBox leftPanel = new VBox(10);
        leftPanel.setPadding(new Insets(15));
        leftPanel.setStyle("-fx-background-color: white;");
        
        Label treeTitle = new Label("📁 Log Folders");
        treeTitle.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        
        leftPanel.getChildren().addAll(treeTitle, folderTree);
        
        VBox rightPanel = new VBox(15);
        rightPanel.setPadding(new Insets(15));
        
        Label filesTitle = new Label("📄 Log Files (Multi-Select Enabled)");
        filesTitle.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        
        ListView<LogFileItem> logFilesList = new ListView<>();
        logFilesList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        logFilesList.setPrefHeight(400);
        logFilesList.setPlaceholder(new Label("Select a folder to view log files"));
        
        logFilesList.setCellFactory(param -> new ListCell<LogFileItem>() {
            @Override
            protected void updateItem(LogFileItem item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("📄 %s (%.2f KB) - %s", 
                        item.fileName, 
                        item.fileSize / 1024.0,
                        item.folderPath));
                }
            }
        });
        
        Label selectionLabel = new Label("Selected: 0 files");
        selectionLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        selectionLabel.setTextFill(Color.web("#2196F3"));
        
        HBox actionButtons = new HBox(10);
        actionButtons.setAlignment(Pos.CENTER);
        
        Button showLogBtn = new Button("👁️ Show Log");
        showLogBtn.setStyle(getButtonStyle("#2196F3"));
        showLogBtn.setDisable(true);
        
        Button archiveSelectedBtn = new Button("📦 Archive Selected");
        archiveSelectedBtn.setStyle(getButtonStyle("#FF9800"));
        archiveSelectedBtn.setDisable(true);
        
        Button deleteSelectedBtn = new Button("🗑️ Delete Selected");
        deleteSelectedBtn.setStyle(getButtonStyle("#f44336"));
        deleteSelectedBtn.setDisable(true);
        
        Button selectAllBtn = new Button("✅ Select All");
        selectAllBtn.setStyle(getButtonStyle("#4CAF50"));
        selectAllBtn.setDisable(true);
        
        Button clearSelectionBtn = new Button("❌ Clear Selection");
        clearSelectionBtn.setStyle(getButtonStyle("#9E9E9E"));
        clearSelectionBtn.setDisable(true);
        
        actionButtons.getChildren().addAll(showLogBtn, archiveSelectedBtn, deleteSelectedBtn);
        
        HBox selectionButtons = new HBox(10);
        selectionButtons.setAlignment(Pos.CENTER);
        selectionButtons.getChildren().addAll(selectAllBtn, clearSelectionBtn);
        
        rightPanel.getChildren().addAll(filesTitle, selectionLabel, logFilesList, selectionButtons, actionButtons);
        
        folderTree.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && newVal.isLeaf()) {
                updateLogFilesList(newVal.getValue(), logFilesList);
            }
        });
        
        logFilesList.getSelectionModel().getSelectedItems().addListener(
            (javafx.collections.ListChangeListener.Change<? extends LogFileItem> c) -> {
                int selectedCount = logFilesList.getSelectionModel().getSelectedItems().size();
                selectionLabel.setText("Selected: " + selectedCount + " file(s)");
                
                boolean hasSelection = selectedCount > 0;
                showLogBtn.setDisable(selectedCount != 1);
                archiveSelectedBtn.setDisable(!hasSelection);
                deleteSelectedBtn.setDisable(!hasSelection);
                clearSelectionBtn.setDisable(!hasSelection);
            });
        
        logFilesList.getItems().addListener((javafx.collections.ListChangeListener.Change<? extends LogFileItem> c) -> {
            selectAllBtn.setDisable(logFilesList.getItems().isEmpty());
        });
        
        showLogBtn.setOnAction(e -> {
            LogFileItem selected = logFilesList.getSelectionModel().getSelectedItem();
            if (selected != null) {
                showLogContent(selected);
            }
        });
        
        selectAllBtn.setOnAction(e -> {
            logFilesList.getSelectionModel().selectAll();
        });
        
        clearSelectionBtn.setOnAction(e -> {
            logFilesList.getSelectionModel().clearSelection();
        });
        
        archiveSelectedBtn.setOnAction(e -> {
            List<LogFileItem> selectedFiles = new ArrayList<>(
                logFilesList.getSelectionModel().getSelectedItems());
            if (!selectedFiles.isEmpty()) {
                archiveMultipleLogFiles(selectedFiles);
                TreeItem<String> currentFolder = folderTree.getSelectionModel().getSelectedItem();
                if (currentFolder != null) {
                    updateLogFilesList(currentFolder.getValue(), logFilesList);
                }
            }
        });
        
        deleteSelectedBtn.setOnAction(e -> {
            List<LogFileItem> selectedFiles = new ArrayList<>(
                logFilesList.getSelectionModel().getSelectedItems());
            if (!selectedFiles.isEmpty()) {
                deleteMultipleLogFiles(selectedFiles);
                TreeItem<String> currentFolder = folderTree.getSelectionModel().getSelectedItem();
                if (currentFolder != null) {
                    updateLogFilesList(currentFolder.getValue(), logFilesList);
                }
            }
        });
        
        logLayout.setLeft(leftPanel);
        logLayout.setCenter(rightPanel);
        
        Scene logScene = new Scene(logLayout, 1000, 650);
        logStage.setScene(logScene);
        logStage.show();
    }
    
    private TreeView<String> createLogFolderTree() {
        TreeItem<String> root = new TreeItem<>("📁 Logs");
        root.setExpanded(true);
        
        File logsDir = new File("src/main/java/logs");
        
        if (logsDir.exists() && logsDir.isDirectory()) {
            populateTreeView(logsDir, root);
        } else {
            showToast("⚠️ Logs directory not found at: " + logsDir.getAbsolutePath(), "warning");
        }
        
        TreeView<String> treeView = new TreeView<>(root);
        return treeView;
    }
    
    private void populateTreeView(File directory, TreeItem<String> parent) {
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    TreeItem<String> item = new TreeItem<>("📁 " + file.getName());
                    parent.getChildren().add(item);
                    populateTreeView(file, item);
                }
            }
        }
    }
    
    private void updateLogFilesList(String folderName, ListView<LogFileItem> listView) {
        listView.getItems().clear();
        folderName = folderName.replace("📁 ", "");
        
        File logsDir = new File("src/main/java/logs");
        File targetFolder = findFolder(logsDir, folderName);
        
        if (targetFolder != null && targetFolder.exists()) {
            File[] files = targetFolder.listFiles((dir, name) -> name.toLowerCase().endsWith(".log"));
            if (files != null && files.length > 0) {
                for (File file : files) {
                    listView.getItems().add(new LogFileItem(file.getName(), folderName, file));
                }
                listView.setPlaceholder(new Label("Loaded " + files.length + " log file(s)"));
            } else {
                listView.setPlaceholder(new Label("No .log files found in this folder"));
            }
        } else {
            listView.setPlaceholder(new Label("Folder not found: " + folderName));
        }
    }
    
    private File findFolder(File directory, String folderName) {
        if (directory.getName().equals(folderName)) {
            return directory;
        }
        
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    File found = findFolder(file, folderName);
                    if (found != null) return found;
                }
            }
        }
        return null;
    }
    
    private void showLogContent(LogFileItem logItem) {
        if (!logItem.file.exists()) {
            showToast("❌ File not found: " + logItem.fileName, "error");
            return;
        }
        
        Stage contentStage = new Stage();
        contentStage.setTitle("📄 " + logItem.fileName);
        
        VBox layout = new VBox(10);
        layout.setPadding(new Insets(15));
        
        HBox infoBox = new HBox(20);
        infoBox.setStyle("-fx-background-color: #f5f5f5; -fx-padding: 10; -fx-background-radius: 5;");
        Label pathLabel = new Label("📁 " + logItem.folderPath + "/" + logItem.fileName);
        Label sizeLabel = new Label(String.format("📊 Size: %.2f KB", logItem.fileSize / 1024.0));
        infoBox.getChildren().addAll(pathLabel, sizeLabel);
        
        TextArea contentArea = new TextArea();
        contentArea.setEditable(false);
        contentArea.setStyle("-fx-font-family: 'Courier New'; -fx-font-size: 12px;");
        VBox.setVgrow(contentArea, Priority.ALWAYS);
        
        try {
            String content = new String(java.nio.file.Files.readAllBytes(logItem.file.toPath()));
            contentArea.setText(content);
            
            long lineCount = content.lines().count();
            Label lineLabel = new Label("📝 Lines: " + lineCount);
            infoBox.getChildren().add(lineLabel);
            
        } catch (Exception e) {
            contentArea.setText("❌ Error reading file: " + e.getMessage());
            e.printStackTrace();
        }
        
        layout.getChildren().addAll(infoBox, contentArea);
        
        Scene scene = new Scene(layout, 900, 650);
        contentStage.setScene(scene);
        contentStage.show();
    }
    
    private void archiveMultipleLogFiles(List<LogFileItem> logItems) {
        if (logItems.isEmpty()) return;
        
        String zipFileName = "archived_logs_" + System.currentTimeMillis() + ".zip";
        File zipFile = new File(logItems.get(0).file.getParentFile(), zipFileName);
        
        try (java.util.zip.ZipOutputStream zos = new java.util.zip.ZipOutputStream(
                new java.io.FileOutputStream(zipFile))) {
            
            for (LogFileItem logItem : logItems) {
                if (!logItem.file.exists()) {
                    logTask("⚠️ Skipping missing file: " + logItem.fileName);
                    continue;
                }
                
                String entryName = logItem.folderPath + "/" + logItem.fileName;
                java.util.zip.ZipEntry entry = new java.util.zip.ZipEntry(entryName);
                zos.putNextEntry(entry);
                
                byte[] bytes = java.nio.file.Files.readAllBytes(logItem.file.toPath());
                zos.write(bytes, 0, bytes.length);
                zos.closeEntry();
                
                logTask("📦 Added to archive: " + entryName);
            }
            
            showToast(String.format("✅ Archived %d files → %s", logItems.size(), zipFileName), "success");
            logTask(String.format("📦 Created archive: %s with %d files", zipFileName, logItems.size()));
            
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Archive Created");
                alert.setHeaderText("Archive created successfully!");
                alert.setContentText("Archive: " + zipFile.getAbsolutePath() + 
                                   "\nSize: " + String.format("%.2f KB", zipFile.length() / 1024.0));
                alert.showAndWait();
            });
            
        } catch (Exception e) {
            showToast("❌ Archive failed: " + e.getMessage(), "error");
            e.printStackTrace();
        }
    }
    
    private void deleteMultipleLogFiles(List<LogFileItem> logItems) {
        if (logItems.isEmpty()) return;
        
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("⚠️ Confirm Delete");
        confirmAlert.setHeaderText("Delete " + logItems.size() + " log file(s)?");
        
        StringBuilder fileList = new StringBuilder("Files to be deleted:\n\n");
        for (LogFileItem item : logItems) {
            fileList.append("• ").append(item.folderPath).append("/").append(item.fileName).append("\n");
        }
        fileList.append("\nThis action cannot be undone!");
        
        confirmAlert.setContentText(fileList.toString());
        
        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                int successCount = 0;
                int failCount = 0;
                
                for (LogFileItem logItem : logItems) {
                    if (logItem.file.exists() && logItem.file.delete()) {
                        successCount++;
                        logTask("🗑️ Deleted: " + logItem.folderPath + "/" + logItem.fileName);
                    } else {
                        failCount++;
                        logTask("❌ Failed to delete: " + logItem.fileName);
                    }
                }
                
                if (failCount == 0) {
                    showToast(String.format("✅ Deleted %d file(s)", successCount), "success");
                } else {
                    showToast(String.format("⚠️ Deleted %d, Failed %d", successCount, failCount), "warning");
                }
            }
        });
    }
    
    private String getButtonStyle(String color) {
        return String.format("""
            -fx-background-color: %s;
            -fx-text-fill: white;
            -fx-font-size: 13px;
            -fx-font-weight: bold;
            -fx-padding: 10 20;
            -fx-background-radius: 15;
            -fx-cursor: hand;
        """, color);
    }
    
    // ============ UTILITY METHODS ============
    
    private void performStoreTaskAuto(String taskId, String itemStr, String vehicleId) throws Exception {


        if (itemStr == null) {
            throw new Exception("Please select an item!");
        }
        
        String itemId = itemStr.split(" - ")[0];
        Item item = itemsMap.get(itemId);

        if (item == null) {
            throw new Exception("Item not found: " + itemId);
        }
        Thread.sleep(1000);
        logTask(String.format("  [%s] Assigning AGV  %s...", taskId, vehicleId));

        equipmentManager.assignToTask(vehicleId); 
             synchronized (item) {
                if (item.getStatus() == Item.Status.MOVING) {
                throw new Exception("Item is Moving: " + itemId);
                }
                if (item.getStatus() == Item.Status.STORED) {
                throw new Exception("Item is Already Stored: " + itemId);
            
                }
        
        // logTask(String.format("  [%s] AGV picking up %s...", taskId, itemId));
        // Thread.sleep(1000);
        
        // logTask(String.format("  [%s] Finding available cell...", taskId));
        // Thread.sleep(500);
        
        // warehouse.addItem(item);

                StoreAutoTask storeAutoTask = new StoreAutoTask(taskId, equipmentManager, warehouse, item);
                storeAutoTask.run();
        
                logTask(String.format("  [%s] Stored %s at %s", taskId, itemId, item.getPosition()));
                Thread.sleep(500);
                equipmentManager.release(vehicleId);
       
            }
            
       
        

       
    }
    
    private void performChargeTask(String taskId, String vehicleId) throws Exception {
        Equipment equipment = equipmentManager.requireById(vehicleId);
        logTask(String.format("  [%s] %s moving to charging station...", taskId, vehicleId));

        ChargingStation station = null;
        while(station == null) {
            System.out.println("looking for available station...");
            for (ChargingStation cs : chargingStations) {
                synchronized (cs) {
                    if (!cs.isOccupied()) {
                        System.out.println("found available station: " + cs.getId());
                        cs.setOccupied(true);
                        station = cs;
                        break;
                    }
                }
            }    
            if (station == null) {
                logTask(String.format("  [%s] %s waiting for available charging station...", taskId, vehicleId));
                Thread.sleep(1000);
                System.out.println("no available station, retrying...");
            }
        }
        
        ChargingTask chargingTask = new ChargingTask(equipmentManager, station, equipment, taskId);
        chargingTask.run();
        station.setOccupied(false);
        if(chargingTask.getException() != null) {
            throw chargingTask.getException();
        }else {
            logTask(String.format("  [%s] %s fully charged!", taskId, vehicleId));
        }
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