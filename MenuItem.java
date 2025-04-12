package ROMS;

public class MenuItem {
    private int id;
    private String name;
    private double price;
    private int quantity = 1;
    private String category; //to check
    private String imagePath;
    
    // Default constructor
    public MenuItem() {
        this.id = 0;
        this.name = "";
        this.price = 0.0;
        this.category = "";
        this.imagePath = "";
    }
    
    // Existing constructor
    public MenuItem(int id, String name, double price, String category, String imagePath) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.category = category;
        this.imagePath = imagePath;
    }
    
    // Constructor with quantity
    public MenuItem(int id, String name, double price, int quantity, String category, String imagePath) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.quantity = quantity;
        this.category = category;
        this.imagePath = imagePath;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public double getPrice() { return price; }
    public String getCategory() { return category; }
    public String getImagePath() { return imagePath; }
    public int getQuantity() { return quantity; }
    
    // Add setters for admin dashboard functionality
    public void setId(int id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setPrice(double price) { this.price = price; }
    public void setCategory(String category) { this.category = category; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
}
