package ROMS;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Database connection utility class for the Restaurant Operations Management System.
 * Provides methods to connect to and disconnect from the MySQL database.
 */
public class DatabaseConnection {
    // Database connection parameters
    private static final String DB_URL = "jdbc:mysql://localhost:3306/restaurant_db";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = ""; // Set your database password here
    
    private static Connection connection = null;
    
    /**
     * Get a connection to the database.
     * If a connection already exists and is valid, returns the existing connection.
     * Otherwise, creates a new connection.
     * 
     * @return Connection object to the database
     * @throws SQLException if a database access error occurs
     */
    public static Connection getConnection() throws SQLException {
        try {
            if (connection == null || connection.isClosed()) {
                // Load the MySQL driver
                Class.forName("com.mysql.cj.jdbc.Driver");
                
                // Establish the connection
                connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                System.out.println("Database connection established successfully");
            }
            return connection;
        } catch (ClassNotFoundException e) {
            throw new SQLException("MySQL JDBC Driver not found", e);
        } catch (SQLException e) {
            System.err.println("Database connection error: " + e.getMessage());
            throw e;
        }
    }
    
    /**
     * Close the database connection.
     */
    public static void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                System.out.println("Database connection closed");
            } catch (SQLException e) {
                System.err.println("Error closing database connection: " + e.getMessage());
            } finally {
                connection = null;
            }
        }
    }
    
    /**
     * Test the database connection.
     * 
     * @return true if the connection is successful, false otherwise
     */
    public static boolean testConnection() {
        try {
            getConnection();
            return true;
        } catch (SQLException e) {
            System.err.println("Connection test failed: " + e.getMessage());
            return false;
        } finally {
            closeConnection();
        }
    }
} 