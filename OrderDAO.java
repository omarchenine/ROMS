package ROMS;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object (DAO) for the Order entity.
 * Handles database operations related to orders.
 */
public class OrderDAO {
    
    /**
     * Insert a new order into the database.
     * 
     * @param order The order to insert
     * @return The generated order ID if successful, -1 otherwise
     */
    public int insertOrder(Order order) {
        String sql = "INSERT INTO `Order` (Status, Date, customer_ID, kitchen_id, managerID) VALUES (?, ?, ?, ?, ?)";
        Connection conn = null;
        
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false); // Start transaction
            
            // Insert the order first
            try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setString(1, order.getStatus().toString());
                pstmt.setDate(2, new Date(System.currentTimeMillis())); // Current date
                pstmt.setInt(3, order.getCustomerId());
                
                // Kitchen ID and Manager ID are optional
                if (order.getKitchenId() > 0) {
                    pstmt.setInt(4, order.getKitchenId());
                } else {
                    pstmt.setNull(4, java.sql.Types.INTEGER);
                }
                
                if (order.getManagerId() != null && !order.getManagerId().isEmpty()) {
                    pstmt.setString(5, order.getManagerId());
                } else {
                    pstmt.setNull(5, java.sql.Types.VARCHAR);
                }
                
                int affectedRows = pstmt.executeUpdate();
                
                if (affectedRows > 0) {
                    try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                        if (generatedKeys.next()) {
                            int orderId = generatedKeys.getInt(1);
                            
                            // Now insert the order items
                            if (insertOrderItems(conn, orderId, order.getItems())) {
                                conn.commit(); // Commit transaction
                                return orderId;
                            } else {
                                conn.rollback(); // Rollback on failure
                                return -1;
                            }
                        }
                    }
                }
                conn.rollback(); // Rollback on failure
                return -1;
            }
        } catch (SQLException e) {
            System.err.println("Error inserting order: " + e.getMessage());
            try {
                if (conn != null) conn.rollback();
            } catch (SQLException ex) {
                System.err.println("Error rolling back transaction: " + ex.getMessage());
            }
            return -1;
        } finally {
            try {
                if (conn != null) {
                    conn.setAutoCommit(true);
                }
            } catch (SQLException e) {
                System.err.println("Error resetting auto-commit: " + e.getMessage());
            }
        }
    }
    
    /**
     * Insert order items for an order.
     * 
     * @param conn The database connection
     * @param orderId The order ID to associate items with
     * @param items The list of items to insert
     * @return true if successful, false otherwise
     * @throws SQLException if a database error occurs
     */
    private boolean insertOrderItems(Connection conn, int orderId, List<MenuItem> items) throws SQLException {
        String sql = "INSERT INTO Order_MenuItem (OrderID, item_id, quantity) VALUES (?, ?, ?)";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            for (MenuItem item : items) {
                pstmt.setInt(1, orderId);
                pstmt.setInt(2, item.getId());
                pstmt.setInt(3, item.getQuantity());
                pstmt.addBatch();
            }
            
            int[] results = pstmt.executeBatch();
            for (int result : results) {
                if (result <= 0) {
                    return false;
                }
            }
            return true;
        }
    }
    
    /**
     * Update an order's status.
     * 
     * @param orderId The ID of the order to update
     * @param status The new status
     * @return true if successful, false otherwise
     */
    public boolean updateOrderStatus(int orderId, Order.OrderStatus status) {
        String sql = "UPDATE `Order` SET Status = ? WHERE OrderID = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, status.toString());
            pstmt.setInt(2, orderId);
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Error updating order status: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Get an order by its ID.
     * 
     * @param orderId The ID of the order to retrieve
     * @return The order if found, null otherwise
     */
    public Order getOrderById(int orderId) {
        String sql = "SELECT * FROM `Order` WHERE OrderID = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, orderId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Order order = new Order();
                    order.setId(rs.getInt("OrderID"));
                    order.setStatus(Order.OrderStatus.valueOf(rs.getString("Status")));
                    order.setDate(rs.getDate("Date"));
                    order.setCustomerId(rs.getInt("customer_ID"));
                    
                    // Kitchen ID is optional
                    if (rs.getObject("kitchen_id") != null) {
                        order.setKitchenId(rs.getInt("kitchen_id"));
                    }
                    
                    // Manager ID is optional
                    if (rs.getObject("managerID") != null) {
                        order.setManagerId(rs.getString("managerID"));
                    }
                    
                    // Get order items
                    order.setItems(getOrderItems(conn, orderId));
                    
                    return order;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving order: " + e.getMessage());
        }
        return null;
    }
    
    /**
     * Get items for a specific order.
     * 
     * @param conn The database connection
     * @param orderId The order ID to get items for
     * @return A list of menu items in the order
     * @throws SQLException if a database error occurs
     */
    private List<MenuItem> getOrderItems(Connection conn, int orderId) throws SQLException {
        List<MenuItem> items = new ArrayList<>();
        String sql = "SELECT m.item_id, m.title, m.price, m.category_title, om.quantity " +
                     "FROM Order_MenuItem om " +
                     "JOIN MenuItem m ON om.item_id = m.item_id " +
                     "WHERE om.OrderID = ?";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, orderId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    MenuItem item = new MenuItem(
                        rs.getInt("item_id"),
                        rs.getString("title"),
                        rs.getDouble("price"),
                        rs.getInt("quantity"),
                        rs.getString("category_title"),
                        "" // No image path in database, set empty string
                    );
                    items.add(item);
                }
            }
        }
        return items;
    }
    
    /**
     * Get all orders from the database.
     * 
     * @return A list of all orders
     */
    public List<Order> getAllOrders() {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT * FROM `Order`";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                int orderId = rs.getInt("OrderID");
                Order order = new Order();
                order.setId(orderId);
                order.setStatus(Order.OrderStatus.valueOf(rs.getString("Status")));
                order.setDate(rs.getDate("Date"));
                order.setCustomerId(rs.getInt("customer_ID"));
                
                // Kitchen ID is optional
                if (rs.getObject("kitchen_id") != null) {
                    order.setKitchenId(rs.getInt("kitchen_id"));
                }
                
                // Manager ID is optional
                if (rs.getObject("managerID") != null) {
                    order.setManagerId(rs.getString("managerID"));
                }
                
                // Get order items
                order.setItems(getOrderItems(conn, orderId));
                
                orders.add(order);
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving orders: " + e.getMessage());
        }
        return orders;
    }
    
    /**
     * Get orders with a specific status.
     * 
     * @param status The status to filter by
     * @return A list of orders with the specified status
     */
    public List<Order> getOrdersByStatus(Order.OrderStatus status) {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT * FROM `Order` WHERE Status = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, status.toString());
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    int orderId = rs.getInt("OrderID");
                    Order order = new Order();
                    order.setId(orderId);
                    order.setStatus(Order.OrderStatus.valueOf(rs.getString("Status")));
                    order.setDate(rs.getDate("Date"));
                    order.setCustomerId(rs.getInt("customer_ID"));
                    
                    // Kitchen ID is optional
                    if (rs.getObject("kitchen_id") != null) {
                        order.setKitchenId(rs.getInt("kitchen_id"));
                    }
                    
                    // Manager ID is optional
                    if (rs.getObject("managerID") != null) {
                        order.setManagerId(rs.getString("managerID"));
                    }
                    
                    // Get order items
                    order.setItems(getOrderItems(conn, orderId));
                    
                    orders.add(order);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving orders by status: " + e.getMessage());
        }
        return orders;
    }
} 