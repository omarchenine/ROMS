import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.*;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.event.ActionEvent;
import javafx.stage.Stage;

public class CustomerViewController implements Initializable {
    @FXML private VBox cartItemsContainer;
    @FXML private ComboBox<String> deliveryPartnerComboBox;
    @FXML private Label discountLabel;
    @FXML private FlowPane menuItemsContainer;
    @FXML private HBox menuTypesContainer;
    @FXML private TextField searchField;
    @FXML private Label subTotalLabel;
    @FXML private Label taxLabel;
    @FXML private Label totalLabel;
    @FXML private Button cancelBtn;
    @FXML private Button placeOrderBtn;

    private List<MenuItem> menuItems = new ArrayList<>();
    private Map<Integer, CartItem> cartItems = new HashMap<>();
    private Map<Integer, Spinner<Integer>> menuSpinners = new HashMap<>();
    private String currentCategory = "All";
    private double subTotal = 0.0;
    private double discount = 0.0;
    private double tax = 0.0;
    private double total = 0.0;
    
    // Static list to store orders across the application
    private static List<Order> allOrders = new ArrayList<>();

    @Override
    public void initialize(URL location, @SuppressWarnings("unused") ResourceBundle resources) {
        loadMenuItems();
        setupSearch();
        setupCategoryButtons();
        setupDeliveryPartners();
        displayMenuItems();
    }
    
    // Method to access orders from other controllers
    public static List<Order> getAllOrders() {
        return allOrders;
    }

    private void setupSearch() {
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filterAndDisplayMenuItems();
        });
    }

    private void setupCategoryButtons() {
        menuTypesContainer.getChildren().forEach(node -> {
            if (node instanceof Button) {
                Button btn = (Button) node;
                btn.setOnAction(e -> {
                    currentCategory = btn.getText();
                    filterAndDisplayMenuItems();
                    
                    // Update active state
                    menuTypesContainer.getChildren().forEach(n -> {
                        if (n instanceof Button) {
                             Button b = (Button) n;
                            if (b.getText().equals(currentCategory)) {
                                b.getStyleClass().add("active");
                            } else {
                                b.getStyleClass().remove("active");
                            }
                        }
                    });
                });
            }
        });
    }

    private void setupDeliveryPartners() {
        List<String> partners = new ArrayList<>();
        partners.add("Yassir");
        partners.add("WhatsApp - Ahmed (+213 555 12 34 56)");
        partners.add("WhatsApp - Karim (+213 555 78 90 12)");
        partners.add("WhatsApp - Mohamed (+213 555 34 56 78)");
        partners.add("Self Pickup");
        
        deliveryPartnerComboBox.getItems().addAll(partners);
        deliveryPartnerComboBox.setValue(partners.get(0)); // Default to Yassir
    }

    private void displayMenuItems() {
        menuItemsContainer.getChildren().clear();
        for (MenuItem item : menuItems) {
            displayMenuItem(item);
        }
    }

    private void filterAndDisplayMenuItems() {
        String searchText = searchField.getText().toLowerCase();
        menuItemsContainer.getChildren().clear();
        
        for (MenuItem item : menuItems) {
            if ((currentCategory.equals("All") || item.getCategory().equals(currentCategory)) &&
                (searchText.isEmpty() || item.getName().toLowerCase().contains(searchText))) {
                displayMenuItem(item);
            }
        }
    }

    private void displayMenuItem(MenuItem item) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("menuItem.fxml"));
            if (loader.getLocation() == null) {
                System.err.println("Error: Could not find menuItem.fxml");
                return;
            }
            
            VBox menuCard = loader.load();
            
            ImageView imageView = (ImageView) menuCard.lookup("#menuImage");
            Label nameLabel = (Label) menuCard.lookup("#menuName");
            Label priceLabel = (Label) menuCard.lookup("#menuPrice");
            Spinner<Integer> quantitySpinner = (Spinner<Integer>) menuCard.lookup("#addToOrder");
            Button submitBtn = (Button) menuCard.lookup("#submitQuantityBtn");
            
            // Try to load the image with absolute file paths
            String imagePath = item.getImagePath();
            try {
                // Try with absolute path
                java.io.File file = new java.io.File(imagePath);
                if (file.exists()) {
                    Image image = new Image(file.toURI().toString());
                    imageView.setImage(image);
                } else {
                    // Try with bin/images path
                    file = new java.io.File("bin/" + imagePath);
                    if (file.exists()) {
                        Image image = new Image(file.toURI().toString());
                        imageView.setImage(image);
                    } else {
                        // Use colored background as fallback
                        createColoredPlaceholder(imageView, item.getCategory());
                    }
                }
            } catch (Exception e) {
                System.err.println("Failed to load image: " + imagePath + " - " + e.getMessage());
                // Use colored background as fallback
                createColoredPlaceholder(imageView, item.getCategory());
            }
            
            nameLabel.setText(item.getName());
            priceLabel.setText(String.format("$%.2f", item.getPrice()));
            
            // Configure spinner with proper bounds
            SpinnerValueFactory<Integer> valueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 10, 0);
            quantitySpinner.setValueFactory(valueFactory);
            quantitySpinner.setEditable(true);
            menuSpinners.put(item.getId(), quantitySpinner);
            
            // Restore quantity if item is in cart
            CartItem cartItem = cartItems.get(item.getId());
            if (cartItem != null) {
                quantitySpinner.getValueFactory().setValue(cartItem.getQuantity());
            }
            
            // Add to cart action
            submitBtn.setOnAction(e -> {
                int quantity = quantitySpinner.getValue();
                if (quantity > 0) {
                    addToCart(item, quantity);
                }
            });
            
            menuItemsContainer.getChildren().add(menuCard);
            
        } catch (IOException e) {
            System.err.println("Error loading menu item: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // Helper method to create a colored placeholder for missing images
    private void createColoredPlaceholder(ImageView imageView, String category) {
        // Use a color based on category
        String color;
        switch(category) {
            case "Coffee":
                color = "#8B4513"; // Brown
                break;
            case "Burger":
                color = "#CD5C5C"; // Indian Red
                break;
            case "Italian":
                color = "#FF6347"; // Tomato
                break;
            case "Mexican":
                color = "#3CB371"; // Medium Sea Green
                break;
            case "Drinks":
                color = "#4682B4"; // Steel Blue
                break;
            case "Snack":
                color = "#9ACD32"; // Yellow Green
                break;
            case "Soup":
                color = "#DEB887"; // Burlywood
                break;
            case "Seafood":
                color = "#4169E1"; // Royal Blue
                break;
            default:
                color = "#6A5ACD"; // Slate Blue
        }
        
        // Create a colored background for the ImageView
        Rectangle rect = new Rectangle(
            0, 0, imageView.getFitWidth(), imageView.getFitHeight());
        rect.setFill(Color.web(color));
        rect.setArcWidth(15);
        rect.setArcHeight(15);
        
        // Create a snapshot of the rectangle and set it as the image
        SnapshotParameters params = new SnapshotParameters();
        params.setFill(Color.TRANSPARENT);
        WritableImage image = rect.snapshot(params, null);
        imageView.setImage(image);
    }

    private void loadMenuItems() {
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
    }

    private void addToCart(MenuItem item, int quantity) {
        CartItem cartItem = cartItems.get(item.getId());
        if (cartItem == null) {
            cartItem = new CartItem(item, quantity);
            cartItems.put(item.getId(), cartItem);
        } else {
            cartItem.setQuantity(quantity);
        }
        
        updateCartDisplay();
        updateCartSummary();
    }

    private void updateCartDisplay() {
        cartItemsContainer.getChildren().clear();
        for (CartItem cartItem : cartItems.values()) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("cart_item.fxml"));
                VBox cartItemBox = loader.load();
                
                MenuItem item = cartItem.getMenuItem();
                
                Label foodNameLabel = (Label) cartItemBox.lookup("#foodNameLabel");
                Label priceLabel = (Label) cartItemBox.lookup("#priceLabel");
                Label quantityLabel = (Label) cartItemBox.lookup("#quantityLabel");
                Button plusBtn = (Button) cartItemBox.lookup("#plusBtn");
                Button minusBtn = (Button) cartItemBox.lookup("#minusBtn");
                Button deleteButton = (Button) cartItemBox.lookup("#deleteButton");
                
                foodNameLabel.setText(item.getName());
                priceLabel.setText(String.format("$%.2f", cartItem.getTotal()));
                quantityLabel.setText(String.valueOf(cartItem.getQuantity()));
                
                Spinner<Integer> menuSpinner = menuSpinners.get(item.getId());
                
                plusBtn.setOnAction(e -> {
                    int newQuantity = cartItem.getQuantity() + 1;
                    if (newQuantity <= 10) {
                        cartItem.setQuantity(newQuantity);
                        if (menuSpinner != null) {
                            menuSpinner.getValueFactory().setValue(newQuantity);
                        }
                        updateCartDisplay();
                        updateCartSummary();
                    }
                });
                
                minusBtn.setOnAction(e -> {
                    if (cartItem.getQuantity() > 1) {
                        int newQuantity = cartItem.getQuantity() - 1;
                        cartItem.setQuantity(newQuantity);
                        if (menuSpinner != null) {
                            menuSpinner.getValueFactory().setValue(newQuantity);
                        }
                        updateCartDisplay();
                        updateCartSummary();
                    }
                });
                
                deleteButton.setOnAction(e -> {
                    cartItems.remove(item.getId());
                    if (menuSpinner != null) {
                        menuSpinner.getValueFactory().setValue(0);
                    }
                    updateCartDisplay();
                    updateCartSummary();
                });
                
                cartItemsContainer.getChildren().add(cartItemBox);
                
            } catch (IOException e) {
                System.out.println("Error loading cart item: " + e.getMessage());
            }
        }
    }

    private void updateCartSummary() {
        subTotal = cartItems.values().stream()
                .mapToDouble(CartItem::getTotal)
                .sum();
        
        tax = subTotal * 0.1;
        total = subTotal + tax - discount;
        
        subTotalLabel.setText(String.format("$%.2f", subTotal));
        taxLabel.setText(String.format("$%.2f", tax));
        discountLabel.setText(String.format("$%.2f", discount));
        totalLabel.setText(String.format("$%.2f", total));
    }

    @FXML
    void cancelOrder(@SuppressWarnings("unused") ActionEvent event) {
        cartItems.clear();
        menuSpinners.values().forEach(spinner -> 
            spinner.getValueFactory().setValue(0));
        updateCartDisplay();
        updateCartSummary();
    }

    @FXML
    void placeOrder(ActionEvent event) {
        String deliveryPartner = deliveryPartnerComboBox.getValue();
        if (deliveryPartner == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Delivery Partner Required");
            alert.setHeaderText(null);
            alert.setContentText("Please select a delivery partner before placing your order.");
            alert.showAndWait();
            return;
        }
        
        if (cartItems.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Empty Cart");
            alert.setHeaderText(null);
            alert.setContentText("Your cart is empty. Please add items before placing an order.");
            alert.showAndWait();
            return;
        }
        
        // Create a new Order object
        Order newOrder = new Order(new ArrayList<>(cartItems.values()), total, deliveryPartner);
        
        // Add to our orders list
        allOrders.add(newOrder);
        
        // Show order confirmation
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Order Placed");
        alert.setHeaderText("Thank you for your order!");
        
        StringBuilder orderDetails = new StringBuilder();
        orderDetails.append("Your order has been placed successfully.\n\n");
        orderDetails.append("Order ID: ").append(newOrder.getId()).append("\n");
        orderDetails.append("Delivery Partner: ").append(deliveryPartner).append("\n");
        orderDetails.append("Total Items: ").append(cartItems.size()).append("\n");
        orderDetails.append("Total Amount: ").append(String.format("$%.2f", total)).append("\n\n");
        orderDetails.append("A delivery agent will contact you shortly.");
        
        alert.setContentText(orderDetails.toString());
        alert.showAndWait();
        
        // Clear cart after order placement
        cancelOrder(null);
    }
    
    @FXML
    void goToAdminLogin(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("admin_login.fxml"));
            Parent root = loader.load();
            
            Scene scene = new Scene(root);
            Stage stage = (Stage) placeOrderBtn.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Admin Login");
            stage.show();
        } catch (IOException e) {
            System.err.println("Error loading admin login: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
