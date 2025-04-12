package ROMS;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.sql.Date;

public class Order {
    private String id;
    private List<CartItem> items;
    private double total;
    private String deliveryPartner;
    private LocalDateTime orderTime;
    private OrderStatus status;
    private String notes;
    private int customerId;
    private int kitchenId;
    private String managerId;
    private Date date;
    
    public enum OrderStatus {
        QUEUED("Queued"),
        IN_PROGRESS("In Progress"),
        READY("Ready for Delivery"),
        DELIVERED("Delivered"),
        CANCELLED("Cancelled");
        
        private final String displayName;
        
        OrderStatus(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
        
        @Override
        public String toString() {
            return displayName;
        }
    }
    
    public Order() {
        this.id = generateOrderId();
        this.items = new ArrayList<>();
        this.status = OrderStatus.QUEUED;
        this.orderTime = LocalDateTime.now();
    }
    
    public Order(List<CartItem> items, double total, String deliveryPartner) {
        this.id = generateOrderId();
        this.items = new ArrayList<>(items);
        this.total = total;
        this.deliveryPartner = deliveryPartner;
        this.orderTime = LocalDateTime.now();
        this.status = OrderStatus.QUEUED;
        this.notes = "";
    }
    
    private String generateOrderId() {
        // Generate first 8 characters of a UUID for a shorter ID
        return "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
    
    public String getId() {
        return id;
    }
    
    // Add method to set ID from database (integer ID)
    public void setId(int id) {
        this.id = "ORD-" + id;
    }
    
    public List<CartItem> getItems() {
        return items;
    }
    
    // Add method to set items as MenuItem list from database
    public void setItems(List<MenuItem> menuItems) {
        this.items = new ArrayList<>();
        for (MenuItem menuItem : menuItems) {
            CartItem cartItem = new CartItem(menuItem, menuItem.getQuantity());
            this.items.add(cartItem);
        }
    }
    
    public double getTotal() {
        return total;
    }
    
    public String getDeliveryPartner() {
        return deliveryPartner;
    }
    
    public LocalDateTime getOrderTime() {
        return orderTime;
    }
    
    public String getFormattedOrderTime() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        return orderTime.format(formatter);
    }
    
    public OrderStatus getStatus() {
        return status;
    }
    
    public void setStatus(OrderStatus status) {
        this.status = status;
    }
    
    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
    }
    
    public String getItemsSummary() {
        StringBuilder summary = new StringBuilder();
        for (int i = 0; i < items.size(); i++) {
            CartItem item = items.get(i);
            summary.append(item.getQuantity())
                  .append("x ")
                  .append(item.getMenuItem().getName());
            
            if (i < items.size() - 1) {
                summary.append(", ");
            }
        }
        return summary.toString();
    }
    
    public int getCustomerId() {
        return customerId;
    }
    
    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }
    
    public int getKitchenId() {
        return kitchenId;
    }
    
    public void setKitchenId(int kitchenId) {
        this.kitchenId = kitchenId;
    }
    
    public String getManagerId() {
        return managerId;
    }
    
    public void setManagerId(String managerId) {
        this.managerId = managerId;
    }
    
    public Date getDate() {
        return date;
    }
    
    public void setDate(Date date) {
        this.date = date;
    }
    
    // Method to convert CartItems to MenuItems for database operations
    public List<MenuItem> getMenuItems() {
        List<MenuItem> menuItems = new ArrayList<>();
        for (CartItem item : items) {
            MenuItem menuItem = item.getMenuItem();
            menuItem.setQuantity(item.getQuantity());
            menuItems.add(menuItem);
        }
        return menuItems;
    }
} 