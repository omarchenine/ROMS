package ROMS;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object (DAO) for the MenuItem entity.
 * Handles database operations related to menu items.
 */
public class MenuItemDAO {
    
    /**
     * Insert a new menu item into the database.
     * 
     * @param item The menu item to insert
     * @return The generated item ID if successful, -1 otherwise
     */
    public int insertMenuItem(MenuItem item) {
        String sql = "INSERT INTO MenuItem (title, price, category_title) VALUES (?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setString(1, item.getName());
            pstmt.setDouble(2, item.getPrice());
            pstmt.setString(3, item.getCategory());
            
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        return generatedKeys.getInt(1);
                    }
                }
            }
            return -1;
        } catch (SQLException e) {
            System.err.println("Error inserting menu item: " + e.getMessage());
            return -1;
        }
    }
    
    /**
     * Update an existing menu item in the database.
     * 
     * @param item The menu item to update
     * @return true if successful, false otherwise
     */
    public boolean updateMenuItem(MenuItem item) {
        String sql = "UPDATE MenuItem SET title = ?, price = ?, category_title = ? WHERE item_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, item.getName());
            pstmt.setDouble(2, item.getPrice());
            pstmt.setString(3, item.getCategory());
            pstmt.setInt(4, item.getId());
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Error updating menu item: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Delete a menu item from the database.
     * 
     * @param itemId The ID of the menu item to delete
     * @return true if successful, false otherwise
     */
    public boolean deleteMenuItem(int itemId) {
        String sql = "DELETE FROM MenuItem WHERE item_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, itemId);
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting menu item: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Get a menu item by its ID.
     * 
     * @param itemId The ID of the menu item to retrieve
     * @return The menu item if found, null otherwise
     */
    public MenuItem getMenuItemById(int itemId) {
        String sql = "SELECT * FROM MenuItem WHERE item_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, itemId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    MenuItem item = new MenuItem();
                    item.setId(rs.getInt("item_id"));
                    item.setName(rs.getString("title"));
                    item.setPrice(rs.getDouble("price"));
                    item.setCategory(rs.getString("category_title"));
                    return item;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving menu item: " + e.getMessage());
        }
        return null;
    }
    
    /**
     * Get all menu items from the database.
     * 
     * @return A list of all menu items
     */
    public List<MenuItem> getAllMenuItems() {
        List<MenuItem> menuItems = new ArrayList<>();
        String sql = "SELECT * FROM MenuItem";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                MenuItem item = new MenuItem();
                item.setId(rs.getInt("item_id"));
                item.setName(rs.getString("title"));
                item.setPrice(rs.getDouble("price"));
                item.setCategory(rs.getString("category_title"));
                menuItems.add(item);
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving menu items: " + e.getMessage());
        }
        return menuItems;
    }
    
    /**
     * Get all menu items in a specific category.
     * 
     * @param category The category to filter by
     * @return A list of menu items in the specified category
     */
    public List<MenuItem> getMenuItemsByCategory(String category) {
        List<MenuItem> menuItems = new ArrayList<>();
        String sql = "SELECT * FROM MenuItem WHERE category_title = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, category);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    MenuItem item = new MenuItem();
                    item.setId(rs.getInt("item_id"));
                    item.setName(rs.getString("title"));
                    item.setPrice(rs.getDouble("price"));
                    item.setCategory(rs.getString("category_title"));
                    menuItems.add(item);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving menu items by category: " + e.getMessage());
        }
        return menuItems;
    }
    
    /**
     * Get a list of all categories from the database.
     * 
     * @return A list of all categories
     */
    public List<String> getAllCategories() {
        List<String> categories = new ArrayList<>();
        String sql = "SELECT title FROM Category";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                categories.add(rs.getString("title"));
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving categories: " + e.getMessage());
        }
        return categories;
    }
} 