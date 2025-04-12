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
        setupCategoryComboBox();
        loadMenuItems();
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
    
    private void loadMenuItems() {
        // Load sample data from CustomerViewController's loadMenuItems method
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
        
        menuItemsTable.setItems(menuItems);
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
        try {
            String name = menuNameField.getText().trim();
            double price = Double.parseDouble(menuPriceField.getText().trim());
            String category = categoryComboBox.getValue();
            String imagePath = imagePathField.getText().trim();
            
            if (name.isEmpty() || category == null || imagePath.isEmpty()) {
                showAlert(AlertType.ERROR, "Input Error", "Please fill all fields");
                return;
            }
            
            MenuItem newItem = new MenuItem(nextId++, name, price, category, imagePath);
            menuItems.add(newItem);
            menuItemsTable.refresh();
            
            clearForm();
            statusLabel.setText("Menu item added successfully");
        } catch (NumberFormatException e) {
            showAlert(AlertType.ERROR, "Input Error", "Price must be a valid number");
        }
    }
    
    @FXML
    private void handleUpdateItem(ActionEvent event) {
        if (selectedMenuItem == null) {
            showAlert(AlertType.WARNING, "Selection Error", "No item selected");
            return;
        }
        
        try {
            selectedMenuItem.setName(menuNameField.getText().trim());
            selectedMenuItem.setPrice(Double.parseDouble(menuPriceField.getText().trim()));
            selectedMenuItem.setCategory(categoryComboBox.getValue());
            selectedMenuItem.setImagePath(imagePathField.getText().trim());
            
            menuItemsTable.refresh();
            clearForm();
            statusLabel.setText("Menu item updated successfully");
        } catch (NumberFormatException e) {
            showAlert(AlertType.ERROR, "Input Error", "Price must be a valid number");
        }
    }
    
    @FXML
    private void handleDeleteItem(ActionEvent event) {
        if (selectedMenuItem == null) {
            showAlert(AlertType.WARNING, "Selection Error", "No item selected");
            return;
        }
        
        Alert confirmAlert = new Alert(AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Deletion");
        confirmAlert.setHeaderText("Delete " + selectedMenuItem.getName());
        confirmAlert.setContentText("Are you sure you want to delete this menu item?");
        
        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            menuItems.remove(selectedMenuItem);
            menuItemsTable.refresh();
            clearForm();
            statusLabel.setText("Menu item deleted successfully");
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
} 