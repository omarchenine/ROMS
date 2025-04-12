package ROMS;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

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
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javafx.scene.paint.Color;

public class AdminDashboardController implements Initializable {
    
    // Menu Items Table
    @FXML private TableView<MenuItem> menuItemsTable;
    @FXML private TableColumn<MenuItem, Integer> idColumn;
    @FXML private TableColumn<MenuItem, String> nameColumn;
    @FXML private TableColumn<MenuItem, Double> priceColumn;
    @FXML private TableColumn<MenuItem, String> categoryColumn;
    @FXML private TableColumn<MenuItem, String> imagePathColumn;
    
    // Form Fields
    @FXML private TextField menuNameField;
    @FXML private TextField menuPriceField;
    @FXML private ComboBox<String> categoryComboBox;
    @FXML private TextField imagePathField;
    @FXML private Label statusLabel;
    
    // Buttons
    @FXML private Button addButton;
    @FXML private Button updateButton;
    @FXML private Button deleteButton;
    @FXML private Button clearButton;
    
    // Data
    private ObservableList<MenuItem> menuItems = FXCollections.observableArrayList();
    private MenuItem selectedMenuItem;
    private int nextId = 13; // Start from 13 as existing items are 1-12

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTable();
        loadCategories();
        loadMenuItems();
        
        // Test database connection
        boolean connected = DatabaseConnection.testConnection();
        if (!connected) {
            statusLabel.setText("Warning: Database connection failed. Using sample data.");
            statusLabel.setTextFill(Color.RED);
        } else {
            statusLabel.setText("Connected to database successfully");
            statusLabel.setTextFill(Color.GREEN);
        }
        
        setupCategoryComboBox();
        clearForm();
    }
    
    private void setupTable() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));
        imagePathColumn.setCellValueFactory(new PropertyValueFactory<>("imagePath"));
        
        menuItemsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                selectedMenuItem = newSelection;
                populateForm(selectedMenuItem);
            }
        });
    }
    
    private void setupCategoryComboBox() {
        ObservableList<String> categories = FXCollections.observableArrayList(
            "Burger", "Coffee", "Drinks", "Italian", "Mexican", "Chinese", "Hotdog", "Snack", "Seafood", "Soup"
        );
        categoryComboBox.setItems(categories);
    }
    
    private void loadCategories() {
        try {
            // Load categories from database
            MenuItemDAO menuItemDAO = new MenuItemDAO();
            List<String> categories = menuItemDAO.getAllCategories();
            
            // If database returned categories, use them
            if (!categories.isEmpty()) {
                categoryComboBox.getItems().clear();
                categoryComboBox.getItems().addAll(categories);
                return;
            }
        } catch (Exception e) {
            System.err.println("Error loading categories from database: " + e.getMessage());
        }
        
        // Fall back to sample categories if database fails or returns empty list
        categoryComboBox.getItems().clear();
        categoryComboBox.getItems().addAll("Burger", "Coffee", "Drinks", "Italian", "Mexican", "Chinese", "Hotdog", "Snack");
    }
    
    private void loadMenuItems() {
        try {
            // Clear existing items
            menuItems.clear();
            
            // Load menu items from database
            MenuItemDAO menuItemDAO = new MenuItemDAO();
            List<MenuItem> dbMenuItems = menuItemDAO.getAllMenuItems();
            
            // If database returned items, use them
            if (!dbMenuItems.isEmpty()) {
                menuItems.addAll(dbMenuItems);
                menuItemsTable.setItems(FXCollections.observableArrayList(menuItems));
                return;
            }
        } catch (Exception e) {
            System.err.println("Error loading menu items from database: " + e.getMessage());
        }
        
        // Fall back to sample data if database fails or returns empty list
        loadSampleMenuItems();
    }
    
    private void populateForm(MenuItem item) {
        menuNameField.setText(item.getName());
        menuPriceField.setText(String.valueOf(item.getPrice()));
        categoryComboBox.setValue(item.getCategory());
        imagePathField.setText(item.getImagePath());
        
        addButton.setDisable(true);
        updateButton.setDisable(false);
        deleteButton.setDisable(false);
    }
    
    @FXML
    private void handleAddItem(ActionEvent event) {
        if (validateInputs()) {
            try {
                // Create new menu item from form data
                String name = menuNameField.getText();
                double price = Double.parseDouble(menuPriceField.getText());
                String category = categoryComboBox.getValue();
                String imagePath = imagePathField.getText();
                
                MenuItem newItem = new MenuItem();
                newItem.setName(name);
                newItem.setPrice(price);
                newItem.setCategory(category);
                newItem.setImagePath(imagePath);
                
                // Add to database
                MenuItemDAO menuItemDAO = new MenuItemDAO();
                int newId = menuItemDAO.insertMenuItem(newItem);
                
                if (newId > 0) {
                    // Database operation successful
                    newItem.setId(newId);
                    menuItems.add(newItem);
                    menuItemsTable.setItems(FXCollections.observableArrayList(menuItems));
                    
                    statusLabel.setText("Menu item added successfully with ID: " + newId);
                    statusLabel.setTextFill(Color.GREEN);
                    clearForm();
                } else {
                    // Database operation failed
                    statusLabel.setText("Failed to add item to database. Adding to local list only.");
                    statusLabel.setTextFill(Color.ORANGE);
                    
                    // Add to local list as fallback
                    newItem.setId(getNextAvailableId());
                    menuItems.add(newItem);
                    menuItemsTable.setItems(FXCollections.observableArrayList(menuItems));
                }
            } catch (Exception e) {
                statusLabel.setText("Error: " + e.getMessage());
                statusLabel.setTextFill(Color.RED);
            }
        }
    }
    
    @FXML
    private void handleUpdateItem(ActionEvent event) {
        MenuItem selectedItem = menuItemsTable.getSelectionModel().getSelectedItem();
        if (selectedItem != null && validateInputs()) {
            try {
                // Update menu item with form data
                String name = menuNameField.getText();
                double price = Double.parseDouble(menuPriceField.getText());
                String category = categoryComboBox.getValue();
                String imagePath = imagePathField.getText();
                
                // Update the selected item
                selectedItem.setName(name);
                selectedItem.setPrice(price);
                selectedItem.setCategory(category);
                selectedItem.setImagePath(imagePath);
                
                // Update in database
                MenuItemDAO menuItemDAO = new MenuItemDAO();
                boolean updated = menuItemDAO.updateMenuItem(selectedItem);
                
                if (updated) {
                    // Database operation successful
                    menuItemsTable.refresh();
                    statusLabel.setText("Menu item updated successfully");
                    statusLabel.setTextFill(Color.GREEN);
                } else {
                    // Database operation failed
                    menuItemsTable.refresh();
                    statusLabel.setText("Failed to update item in database. Updated in local list only.");
                    statusLabel.setTextFill(Color.ORANGE);
                }
                
                clearForm();
            } catch (Exception e) {
                statusLabel.setText("Error: " + e.getMessage());
                statusLabel.setTextFill(Color.RED);
            }
        } else {
            statusLabel.setText("Please select an item to update and provide valid inputs");
            statusLabel.setTextFill(Color.RED);
        }
    }
    
    @FXML
    private void handleDeleteItem(ActionEvent event) {
        MenuItem selectedItem = menuItemsTable.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
            try {
                // Delete from database first
                MenuItemDAO menuItemDAO = new MenuItemDAO();
                boolean deleted = menuItemDAO.deleteMenuItem(selectedItem.getId());
                
                if (deleted) {
                    // Database operation successful
                    menuItems.remove(selectedItem);
                    menuItemsTable.setItems(FXCollections.observableArrayList(menuItems));
                    
                    statusLabel.setText("Menu item deleted successfully");
                    statusLabel.setTextFill(Color.GREEN);
                } else {
                    // Database operation failed
                    menuItems.remove(selectedItem);
                    menuItemsTable.setItems(FXCollections.observableArrayList(menuItems));
                    
                    statusLabel.setText("Failed to delete from database. Removed from local list only.");
                    statusLabel.setTextFill(Color.ORANGE);
                }
                
                clearForm();
            } catch (Exception e) {
                statusLabel.setText("Error: " + e.getMessage());
                statusLabel.setTextFill(Color.RED);
            }
        } else {
            statusLabel.setText("Please select an item to delete");
            statusLabel.setTextFill(Color.RED);
        }
    }
    
    @FXML
    private void handleClearForm(ActionEvent event) {
        clearForm();
    }
    
    private void clearForm() {
        menuNameField.clear();
        menuPriceField.clear();
        categoryComboBox.setValue(null);
        imagePathField.clear();
        statusLabel.setText("");
        selectedMenuItem = null;
        
        addButton.setDisable(false);
        updateButton.setDisable(true);
        deleteButton.setDisable(true);
    }
    
    @FXML
    private void handleLogout(ActionEvent event) {
        try {
            // Load the login screen
            FXMLLoader loader = new FXMLLoader(getClass().getResource("admin_login.fxml"));
            Parent root = loader.load();
            
            Scene scene = new Scene(root);
            Stage stage = (Stage) addButton.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Admin Login");
            stage.show();
        } catch (IOException e) {
            showAlert(AlertType.ERROR, "Navigation Error", "Error returning to login: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @FXML
    private void handleBackToCustomerView(ActionEvent event) {
        try {
            // Load the customer view
            FXMLLoader loader = new FXMLLoader(getClass().getResource("customer_view.fxml"));
            Parent root = loader.load();
            
            Scene scene = new Scene(root);
            Stage stage = (Stage) addButton.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Restaurant Customer View");
            stage.show();
        } catch (IOException e) {
            showAlert(AlertType.ERROR, "Navigation Error", "Error returning to customer view: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @FXML
    private void handleKitchenDashboard(ActionEvent event) {
        try {
            // Load the kitchen dashboard
            FXMLLoader loader = new FXMLLoader(getClass().getResource("kitchen_dashboard.fxml"));
            Parent root = loader.load();
            
            Scene scene = new Scene(root);
            Stage stage = (Stage) addButton.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Kitchen Dashboard");
            stage.show();
        } catch (IOException e) {
            showAlert(AlertType.ERROR, "Navigation Error", "Error loading kitchen dashboard: " + e.getMessage());
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
    
    // Method to load sample menu items if database fails
    private void loadSampleMenuItems() {
        // Clear existing items
        menuItems.clear();
        
        // Add sample data
        menuItems.add(new MenuItem(1, "Cappuccino", 4.95, "Coffee", "images/cappuccino-jpg-.png"));
        menuItems.add(new MenuItem(2, "Mushroom Pizza", 9.95, "Italian", "images/mushroom-pizza-jpg-.png"));
        menuItems.add(new MenuItem(3, "Tacos Salsa", 5.95, "Mexican", "images/tacos-jpg-.png"));
        menuItems.add(new MenuItem(4, "Meat burger", 5.95, "Burger", "images/meat-burger-jpg-.png"));
        menuItems.add(new MenuItem(5, "Fresh melon juice", 3.95, "Drinks", "images/melon-juice-jpg-.png"));
        menuItems.add(new MenuItem(6, "Vegetable salad", 4.95, "Snack", "images/users-icon-png-vegetable-salad-jpg.png"));
        menuItems.add(new MenuItem(7, "Black chicken Burger", 6.95, "Burger", "images/black-chicken-jpg-.png"));
        menuItems.add(new MenuItem(8, "Bakso Kuah sapi", 5.95, "Soup", "images/bakso-jpg-.png"));
        menuItems.add(new MenuItem(9, "Italian Pizza", 9.95, "Italian", "images/italian-pizza-jpg-.png"));
        menuItems.add(new MenuItem(10, "Sausage Pizza", 8.95, "Italian", "images/sausage-pizza-jpg-.png"));
        menuItems.add(new MenuItem(11, "Seafood Paella", 12.95, "Seafood", "images/seafood-paella-jpg-.png"));
        menuItems.add(new MenuItem(12, "Ranch Burger", 7.95, "Burger", "images/ranch-burger-jpg-.png"));
        
        menuItemsTable.setItems(FXCollections.observableArrayList(menuItems));
    }

    // Method to validate inputs from form fields
    private boolean validateInputs() {
        if (menuNameField.getText().trim().isEmpty()) {
            statusLabel.setText("Please enter a menu item name");
            statusLabel.setTextFill(Color.RED);
            return false;
        }
        
        try {
            double price = Double.parseDouble(menuPriceField.getText().trim());
            if (price <= 0) {
                statusLabel.setText("Please enter a valid price (greater than 0)");
                statusLabel.setTextFill(Color.RED);
                return false;
            }
        } catch (NumberFormatException e) {
            statusLabel.setText("Please enter a valid price");
            statusLabel.setTextFill(Color.RED);
            return false;
        }
        
        if (categoryComboBox.getValue() == null || categoryComboBox.getValue().trim().isEmpty()) {
            statusLabel.setText("Please select a category");
            statusLabel.setTextFill(Color.RED);
            return false;
        }
        
        return true;
    }
    
    // Method to get the next available ID for a new menu item
    private int getNextAvailableId() {
        return menuItems.stream()
                .mapToInt(MenuItem::getId)
                .max()
                .orElse(0) + 1;
    }
} 