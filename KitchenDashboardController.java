package ROMS;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Callback;

public class KitchenDashboardController implements Initializable {
    
    // Orders Tab
    @FXML private TableView<Order> ordersTable;
    @FXML private TableColumn<Order, String> orderIdColumn;
    @FXML private TableColumn<Order, String> orderTimeColumn;
    @FXML private TableColumn<Order, String> itemsColumn;
    @FXML private TableColumn<Order, String> deliveryPartnerColumn;
    @FXML private TableColumn<Order, Order.OrderStatus> statusColumn;
    @FXML private TableColumn<Order, Void> actionsColumn;
    
    // Order Details
    @FXML private Label selectedOrderIdLabel;
    @FXML private TextArea orderNotesField;
    @FXML private ComboBox<Order.OrderStatus> statusComboBox;
    @FXML private Button updateOrderButton;
    
    // Inventory Tab
    @FXML private TableView<Ingredient> inventoryTable;
    @FXML private TableColumn<Ingredient, Integer> ingredientIdColumn;
    @FXML private TableColumn<Ingredient, String> ingredientNameColumn;
    @FXML private TableColumn<Ingredient, Double> quantityColumn;
    @FXML private TableColumn<Ingredient, String> unitColumn;
    @FXML private TableColumn<Ingredient, Double> minQuantityColumn;
    @FXML private TableColumn<Ingredient, String> categoryColumn;
    @FXML private TableColumn<Ingredient, Void> inventoryActionsColumn;
    
    // Inventory Form
    @FXML private TextField ingredientNameField;
    @FXML private TextField quantityField;
    @FXML private TextField minQuantityField;
    @FXML private TextField unitField;
    @FXML private ComboBox<String> ingredientCategoryComboBox;
    @FXML private Button addIngredientButton;
    @FXML private Button updateIngredientButton;
    @FXML private Button clearIngredientFormButton;
    
    // Low Stock Notifications
    @FXML private VBox lowStockContainer;
    
    // Data
    private ObservableList<Order> orders = FXCollections.observableArrayList();
    private ObservableList<Ingredient> ingredients = FXCollections.observableArrayList();
    private List<MenuItemIngredient> menuItemIngredients = new ArrayList<>();
    private Order selectedOrder;
    private Ingredient selectedIngredient;
    private int nextIngredientId = 1;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupOrdersTable();
        setupInventoryTable();
        setupStatusComboBox();
        setupIngredientCategoryComboBox();
        loadOrders();
        loadSampleIngredients();
        setupIngredientMappings();
        checkLowStockIngredients();
        
        // Start a thread to periodically refresh orders (every 30 seconds)
        startOrderRefreshThread();
    }
    
    private void setupOrdersTable() {
        orderIdColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getId()));
        
        orderTimeColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getFormattedOrderTime()));
        
        itemsColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getItemsSummary()));
        
        deliveryPartnerColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getDeliveryPartner()));
        
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        statusColumn.setCellFactory(column -> new TableCell<Order, Order.OrderStatus>() {
            @Override
            protected void updateItem(Order.OrderStatus status, boolean empty) {
                super.updateItem(status, empty);
                
                if (empty || status == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(status.getDisplayName());
                    
                    // Set style based on status
                    switch (status) {
                        case QUEUED:
                            setStyle("-fx-text-fill: #e67e22;"); // Orange
                            break;
                        case IN_PROGRESS:
                            setStyle("-fx-text-fill: #3498db;"); // Blue
                            break;
                        case READY:
                            setStyle("-fx-text-fill: #2ecc71;"); // Green
                            break;
                        case DELIVERED:
                            setStyle("-fx-text-fill: #7f8c8d;"); // Gray
                            break;
                        case CANCELLED:
                            setStyle("-fx-text-fill: #e74c3c;"); // Red
                            break;
                        default:
                            setStyle("");
                            break;
                    }
                }
            }
        });
        
        // Setup actions column with buttons
        actionsColumn.setCellFactory(param -> new TableCell<Order, Void>() {
            private final Button progressBtn = new Button("Progress");
            private final Button readyBtn = new Button("Ready");
            private final HBox pane = new HBox(5, progressBtn, readyBtn);

            {
                progressBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white;");
                readyBtn.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white;");
                
                progressBtn.setOnAction(event -> {
                    Order order = getTableView().getItems().get(getIndex());
                    order.setStatus(Order.OrderStatus.IN_PROGRESS);
                    ordersTable.refresh();
                });
                
                readyBtn.setOnAction(event -> {
                    Order order = getTableView().getItems().get(getIndex());
                    order.setStatus(Order.OrderStatus.READY);
                    ordersTable.refresh();
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                
                if (empty) {
                    setGraphic(null);
                } else {
                    Order order = getTableView().getItems().get(getIndex());
                    
                    // Only show relevant buttons based on current status
                    switch (order.getStatus()) {
                        case QUEUED:
                            progressBtn.setVisible(true);
                            readyBtn.setVisible(false);
                            break;
                        case IN_PROGRESS:
                            progressBtn.setVisible(false);
                            readyBtn.setVisible(true);
                            break;
                        default:
                            progressBtn.setVisible(false);
                            readyBtn.setVisible(false);
                            break;
                    }
                    
                    setGraphic(pane);
                }
            }
        });
        
        // Handle row selection
        ordersTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                selectedOrder = newSelection;
                populateOrderDetails(selectedOrder);
            }
        });
    }
    
    private void setupInventoryTable() {
        ingredientIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        ingredientNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        unitColumn.setCellValueFactory(new PropertyValueFactory<>("unit"));
        minQuantityColumn.setCellValueFactory(new PropertyValueFactory<>("minQuantity"));
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));
        
        // Custom cell factory for quantity column to highlight low stock
        quantityColumn.setCellFactory(column -> new TableCell<Ingredient, Double>() {
            @Override
            protected void updateItem(Double quantity, boolean empty) {
                super.updateItem(quantity, empty);
                
                if (empty || quantity == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(String.format("%.2f", quantity));
                    
                    // Get the current row's ingredient
                    if (getTableRow() != null && getTableRow().getItem() != null) {
                        Ingredient ingredient = (Ingredient) getTableRow().getItem();
                        if (ingredient.isLowStock()) {
                            setStyle("-fx-text-fill: #e74c3c;"); // Red for low stock
                        } else {
                            setStyle("");
                        }
                    }
                }
            }
        });
        
        // Setup actions column with buttons for inventory
        inventoryActionsColumn.setCellFactory(param -> new TableCell<Ingredient, Void>() {
            private final Button editBtn = new Button("Edit");
            private final Button addStockBtn = new Button("+");
            private final HBox pane = new HBox(5, editBtn, addStockBtn);

            {
                editBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white;");
                addStockBtn.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white;");
                
                editBtn.setOnAction(event -> {
                    Ingredient ingredient = getTableView().getItems().get(getIndex());
                    populateIngredientForm(ingredient);
                });
                
                addStockBtn.setOnAction(event -> {
                    Ingredient ingredient = getTableView().getItems().get(getIndex());
                    showAddStockDialog(ingredient);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });
        
        // Handle row selection
        inventoryTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                selectedIngredient = newSelection;
                populateIngredientForm(selectedIngredient);
            }
        });
    }
    
    private void setupStatusComboBox() {
        statusComboBox.getItems().addAll(Order.OrderStatus.values());
    }
    
    private void setupIngredientCategoryComboBox() {
        ObservableList<String> categories = FXCollections.observableArrayList(
            "Meat", "Dairy", "Produce", "Bakery", "Spices", "Beverages", "Dry Goods", "Other"
        );
        ingredientCategoryComboBox.setItems(categories);
    }
    
    private void loadOrders() {
        // Get orders from the CustomerViewController
        List<Order> existingOrders = CustomerViewController.getAllOrders();
        orders.addAll(existingOrders);
        
        // If no orders exist yet, create some sample orders
        if (orders.isEmpty()) {
            createSampleOrders();
        }
        
        ordersTable.setItems(orders);
    }
    
    private void createSampleOrders() {
        // Create some sample orders for testing
        List<CartItem> items1 = new ArrayList<>();
        items1.add(new CartItem(new MenuItem(1, "Cappuccino", 4.95, "Coffee", ""), 2));
        items1.add(new CartItem(new MenuItem(2, "Mushroom Pizza", 9.95, "Italian", ""), 1));
        
        List<CartItem> items2 = new ArrayList<>();
        items2.add(new CartItem(new MenuItem(4, "Meat burger", 5.95, "Burger", ""), 3));
        items2.add(new CartItem(new MenuItem(5, "Fresh melon juice", 3.95, "Drinks", ""), 3));
        
        Order order1 = new Order(items1, 19.85, "Yassir");
        Order order2 = new Order(items2, 29.7, "Self Pickup");
        
        // Add sample orders
        orders.add(order1);
        orders.add(order2);
    }
    
    private void loadSampleIngredients() {
        // Add sample ingredients
        ingredients.add(new Ingredient(nextIngredientId++, "Flour", 10.0, 5.0, "kg", "Bakery"));
        ingredients.add(new Ingredient(nextIngredientId++, "Tomatoes", 5.0, 2.0, "kg", "Produce"));
        ingredients.add(new Ingredient(nextIngredientId++, "Ground Beef", 8.0, 3.0, "kg", "Meat"));
        ingredients.add(new Ingredient(nextIngredientId++, "Mozzarella Cheese", 4.0, 1.5, "kg", "Dairy"));
        ingredients.add(new Ingredient(nextIngredientId++, "Coffee Beans", 3.0, 1.0, "kg", "Beverages"));
        ingredients.add(new Ingredient(nextIngredientId++, "Lettuce", 1.5, 1.0, "kg", "Produce"));
        ingredients.add(new Ingredient(nextIngredientId++, "Chicken Breast", 6.0, 2.0, "kg", "Meat"));
        ingredients.add(new Ingredient(nextIngredientId++, "Burger Buns", 40.0, 10.0, "pcs", "Bakery"));
        ingredients.add(new Ingredient(nextIngredientId++, "Milk", 7.0, 2.0, "L", "Dairy"));
        
        // For demonstration, set one ingredient to low stock
        ingredients.add(new Ingredient(nextIngredientId++, "Mushrooms", 0.5, 1.0, "kg", "Produce"));
        
        inventoryTable.setItems(ingredients);
    }
    
    private void setupIngredientMappings() {
        // Setup mappings between menu items and ingredients
        for (Ingredient ingredient : ingredients) {
            // Find matching menu items that might use this ingredient (simplified mapping)
            for (Order order : orders) {
                for (CartItem cartItem : order.getItems()) {
                    MenuItem menuItem = cartItem.getMenuItem();
                    
                    // Simple matching based on name (in a real app, would be more sophisticated)
                    if (menuItem.getName().toLowerCase().contains(ingredient.getName().toLowerCase())) {
                        menuItemIngredients.add(new MenuItemIngredient(menuItem, ingredient, 0.1));
                    }
                }
            }
        }
    }
    
    private void checkLowStockIngredients() {
        lowStockContainer.getChildren().clear();
        
        // Filter low stock ingredients
        List<Ingredient> lowStock = ingredients.stream()
                .filter(Ingredient::isLowStock)
                .collect(Collectors.toList());
        
        if (lowStock.isEmpty()) {
            Label noLowStockLabel = new Label("No low stock ingredients");
            noLowStockLabel.setStyle("-fx-text-fill: #2ecc71;"); // Green
            lowStockContainer.getChildren().add(noLowStockLabel);
            return;
        }
        
        // Add warning for each low stock ingredient
        for (Ingredient ingredient : lowStock) {
            Label lowStockLabel = new Label(ingredient.getName() + " is low in stock! " +
                    String.format("%.2f%s remaining (minimum: %.2f%s)",
                        ingredient.getQuantity(),
                        ingredient.getUnit(),
                        ingredient.getMinQuantity(),
                        ingredient.getUnit()));
            
            lowStockLabel.setStyle("-fx-text-fill: #e74c3c;"); // Red
            lowStockLabel.setWrapText(true);
            lowStockContainer.getChildren().add(lowStockLabel);
        }
    }
    
    private void populateOrderDetails(Order order) {
        selectedOrderIdLabel.setText("Order: " + order.getId());
        orderNotesField.setText(order.getNotes());
        statusComboBox.setValue(order.getStatus());
        updateOrderButton.setDisable(false);
    }
    
    private void populateIngredientForm(Ingredient ingredient) {
        ingredientNameField.setText(ingredient.getName());
        quantityField.setText(String.valueOf(ingredient.getQuantity()));
        minQuantityField.setText(String.valueOf(ingredient.getMinQuantity()));
        unitField.setText(ingredient.getUnit());
        ingredientCategoryComboBox.setValue(ingredient.getCategory());
        
        addIngredientButton.setDisable(true);
        updateIngredientButton.setDisable(false);
    }
    
    private void showAddStockDialog(Ingredient ingredient) {
        TextInputDialog dialog = new TextInputDialog("1.0");
        dialog.setTitle("Add Stock");
        dialog.setHeaderText("Add stock to " + ingredient.getName());
        dialog.setContentText("Enter amount to add:");
        
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(amount -> {
            try {
                double amountValue = Double.parseDouble(amount);
                if (amountValue > 0) {
                    ingredient.increaseQuantity(amountValue);
                    inventoryTable.refresh();
                    checkLowStockIngredients();
                }
            } catch (NumberFormatException e) {
                showAlert(AlertType.ERROR, "Input Error", "Please enter a valid number.");
            }
        });
    }
    
    private void startOrderRefreshThread() {
        Thread refreshThread = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(30000); // 30 seconds
                    
                    // Update UI on JavaFX thread
                    Platform.runLater(() -> {
                        // Refresh orders from CustomerViewController
                        List<Order> currentOrders = CustomerViewController.getAllOrders();
                        
                        // Add any new orders that aren't in our list
                        for (Order newOrder : currentOrders) {
                            if (!orders.contains(newOrder)) {
                                orders.add(newOrder);
                            }
                        }
                        
                        ordersTable.refresh();
                    });
                } catch (InterruptedException e) {
                    // Thread interrupted
                    break;
                }
            }
        });
        
        refreshThread.setDaemon(true); // Make it a daemon thread so it doesn't prevent app exit
        refreshThread.start();
    }
    
    @FXML
    private void handleUpdateOrder() {
        if (selectedOrder == null) {
            showAlert(AlertType.WARNING, "No Selection", "Please select an order to update.");
            return;
        }
        
        selectedOrder.setStatus(statusComboBox.getValue());
        selectedOrder.setNotes(orderNotesField.getText());
        
        ordersTable.refresh();
        showAlert(AlertType.INFORMATION, "Success", "Order updated successfully.");
    }
    
    @FXML
    private void handleAddIngredient() {
        try {
            String name = ingredientNameField.getText().trim();
            double quantity = Double.parseDouble(quantityField.getText().trim());
            double minQuantity = Double.parseDouble(minQuantityField.getText().trim());
            String unit = unitField.getText().trim();
            String category = ingredientCategoryComboBox.getValue();
            
            if (name.isEmpty() || unit.isEmpty() || category == null) {
                showAlert(AlertType.ERROR, "Input Error", "Please fill all fields.");
                return;
            }
            
            Ingredient newIngredient = new Ingredient(nextIngredientId++, name, quantity, minQuantity, unit, category);
            ingredients.add(newIngredient);
            
            clearIngredientForm();
            checkLowStockIngredients();
            showAlert(AlertType.INFORMATION, "Success", "Ingredient added successfully.");
        } catch (NumberFormatException e) {
            showAlert(AlertType.ERROR, "Input Error", "Please enter valid numbers for quantity fields.");
        }
    }
    
    @FXML
    private void handleUpdateIngredient() {
        if (selectedIngredient == null) {
            showAlert(AlertType.WARNING, "No Selection", "Please select an ingredient to update.");
            return;
        }
        
        try {
            selectedIngredient.setName(ingredientNameField.getText().trim());
            selectedIngredient.setQuantity(Double.parseDouble(quantityField.getText().trim()));
            selectedIngredient.setMinQuantity(Double.parseDouble(minQuantityField.getText().trim()));
            selectedIngredient.setUnit(unitField.getText().trim());
            selectedIngredient.setCategory(ingredientCategoryComboBox.getValue());
            
            inventoryTable.refresh();
            clearIngredientForm();
            checkLowStockIngredients();
            showAlert(AlertType.INFORMATION, "Success", "Ingredient updated successfully.");
        } catch (NumberFormatException e) {
            showAlert(AlertType.ERROR, "Input Error", "Please enter valid numbers for quantity fields.");
        }
    }
    
    @FXML
    private void handleClearIngredientForm() {
        clearIngredientForm();
    }
    
    private void clearIngredientForm() {
        ingredientNameField.clear();
        quantityField.clear();
        minQuantityField.clear();
        unitField.clear();
        ingredientCategoryComboBox.setValue(null);
        
        selectedIngredient = null;
        addIngredientButton.setDisable(false);
        updateIngredientButton.setDisable(true);
    }
    
    @FXML
    private void handleLogout() {
        try {
            // Load the login screen
            FXMLLoader loader = new FXMLLoader(getClass().getResource("admin_login.fxml"));
            Parent root = loader.load();
            
            Scene scene = new Scene(root);
            Stage stage = (Stage) ordersTable.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Admin Login");
            stage.show();
        } catch (IOException e) {
            showAlert(AlertType.ERROR, "Navigation Error", "Error returning to login: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @FXML
    private void handleBackToCustomerView() {
        try {
            // Load the customer view
            FXMLLoader loader = new FXMLLoader(getClass().getResource("customer_view.fxml"));
            Parent root = loader.load();
            
            Scene scene = new Scene(root);
            Stage stage = (Stage) ordersTable.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Restaurant Customer View");
            stage.show();
        } catch (IOException e) {
            showAlert(AlertType.ERROR, "Navigation Error", "Error returning to customer view: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void showAlert(AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
} 