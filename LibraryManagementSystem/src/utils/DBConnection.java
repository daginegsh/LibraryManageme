package utils;  // ← MUST BE THIS LINE!

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    // Database connection details - CHANGE THESE IF NEEDED
    private static final String URL = "jdbc:mysql://localhost:3306/library_management_system";
    private static final String USERNAME = "root";    // Your MySQL username
    private static final String PASSWORD = "dagi";        // Your MySQL password
    
    public static Connection getConnection() {
        try {
            // 1. Load MySQL Driver
            Class.forName("com.mysql.cj.jdbc.Driver");
            
            // 2. Create connection
            Connection connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
            
            // 3. Success message
            System.out.println("✅ Database connected successfully!");
            System.out.println("Database: " + URL);
            System.out.println("Username: " + USERNAME);
            
            return connection;
            
        } catch (ClassNotFoundException e) {
            System.out.println("❌ ERROR: MySQL Driver not found!");
            System.out.println("Make sure MySQL Connector JAR is added to Libraries");
            e.printStackTrace();
            return null;
            
        } catch (SQLException e) {
            System.out.println("❌ ERROR: Could not connect to database!");
            System.out.println("Error: " + e.getMessage());
            System.out.println("\nCheck these:");
            System.out.println("1. Is MySQL server running?");
            System.out.println("2. Is database 'library_management' created?");
            System.out.println("3. Correct username/password?");
            System.out.println("4. Correct port (3306)?");
            return null;
        }
    }
    
    // Simple test method
    public static void testConnection() {
        System.out.println("\n=== Testing Database Connection ===");
        Connection conn = getConnection();
        if (conn != null) {
            System.out.println("✅ Test PASSED: Database is accessible");
            try {
                conn.close();
                System.out.println("Connection closed.");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("❌ Test FAILED: Could not connect");
        }
    }
    
    // Main method to test directly
    public static void main(String[] args) {
        testConnection();
    }
}