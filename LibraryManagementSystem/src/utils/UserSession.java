package utils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class UserSession {

    // User information
    private static int userId;
    private static String username;
    private static String fullName;
    private static String role;
    private static String email;
    private static String phone;

    // ==========================
    //      GETTERS
    // ==========================
    public static int getUserId() { return userId; }
    public static String getUsername() { return username; }
    public static String getFullName() { return fullName; }
    public static String getRole() { return role; }
    public static String getEmail() { return email; }
    public static String getPhone() { return phone; }

    // ==========================
    //      SETTERS
    // ==========================
    public static void setUserId(int id) { userId = id; }
    public static void setUsername(String name) { username = name; }
    public static void setFullName(String name) { fullName = name; }
    public static void setRole(String userRole) { role = userRole; }
    public static void setEmail(String userEmail) { email = userEmail; }
    public static void setPhone(String userPhone) { phone = userPhone; }

    // ==========================
    //     ROLE CHECKERS
    // ==========================

    /** Admin = full access */
    public static boolean isAdmin() {
        return "ADMIN".equalsIgnoreCase(role);
    }

    /** Librarian = almost full access */
    public static boolean isLibrarian() {
        return "LIBRARIAN".equalsIgnoreCase(role);
    }

    /** Staff = **ONLY staff** */
    public static boolean isStaff() {
        return "STAFF".equalsIgnoreCase(role);
    }

    /** Member = normal public member */
    public static boolean isMember() {
        return "MEMBER".equalsIgnoreCase(role);
    }

    /** Check login status */
    public static boolean isLoggedIn() {
        return userId > 0 && username != null && role != null;
    }

    // ==========================
    //  HUMAN-READABLE ROLE NAME
    // ==========================
    public static String getRoleDisplay() {
        if (role == null) return "Unknown";

        switch (role.toUpperCase()) {
            case "ADMIN":     return "Administrator";
            case "LIBRARIAN": return "Librarian";
            case "STAFF":     return "Staff";
            case "MEMBER":    return "Library Member";
            default:          return role;
        }
    }

    // ==========================
    //        LOGOUT
    // ==========================
    public static void clear() {
        userId = 0;
        username = null;
        fullName = null;
        role = null;
        email = null;
        phone = null;

        System.out.println("User session cleared.");
    }

    // ==========================
    //   ACTIVITY LOGGING
    // ==========================
    public static void logActivity(String activityType, String description) {
        if (!isLoggedIn()) {
            System.out.println("⚠ Cannot log activity: user not logged in.");
            return;
        }

        try {
            Connection conn = DBConnection.getConnection();
            String sql = "INSERT INTO user_activity_logs (user_id, activity_type, description) VALUES (?, ?, ?)";
            PreparedStatement pst = conn.prepareStatement(sql);

            pst.setInt(1, userId);
            pst.setString(2, activityType);
            pst.setString(3, description);

            pst.executeUpdate();
            pst.close();
            conn.close();

        } catch (SQLException e) {
            // Log to console if table does not exist
            System.out.println("Activity: " + activityType + " - " + description);
        }
    }

    // ==========================
    //   PERMISSION SYSTEM - UPDATED TO MATCH YOUR TABLE
    // ==========================
    /**
     * Permissions based on your table:
     * ADMIN – ✅ ALL permissions
     * LIBRARIAN – ✅ ALL permissions  
     * STAFF – ✅ VIEW BOOKS, ✅ CIRCULATION, ✅ REPORTS, ❌ ADD/EDIT/DELETE
     * MEMBER – ✅ VIEW BOOKS only
     */
    public static boolean canPerform(String action) {
        if (!isLoggedIn()) return false;

        String actionUpper = action.toUpperCase();

        // Debug: Print current permission check
        System.out.println("Checking permission: " + action + " for role: " + role);
        
        switch (actionUpper) {
            // --------------------------
            // BOOK MANAGEMENT PERMISSIONS
            // --------------------------
            case "VIEW_BOOKS":
                // Everyone can view books
                return true;
                
            case "ADD_BOOKS":
            case "EDIT_BOOKS": 
            case "DELETE_BOOKS":
            case "MANAGE_BOOKS":
                // Only Admin and Librarian can add/edit/delete books
                return isAdmin() || isLibrarian();

            // --------------------------
            // CIRCULATION PERMISSIONS
            // --------------------------
            case "CIRCULATION":
            case "MANAGE_CIRCULATION":
                // Admin, Librarian, and Staff can manage circulation
                return isAdmin() || isLibrarian() || isStaff();

            // --------------------------
            // REPORT PERMISSIONS
            // --------------------------
            case "REPORT":
            case "REPORTS":
            case "GENERATE_REPORTS":
            case "VIEW_REPORTS":
                // Admin, Librarian, and Staff can view reports
                // Members CANNOT view reports
                return isAdmin() || isLibrarian() || isStaff();

            // --------------------------
            // USER MANAGEMENT PERMISSIONS
            // --------------------------
            case "MANAGE_USERS":
            case "DELETE_USERS":
            case "CHANGE_ROLES":
                // Only Admin can manage users
                return isAdmin();

            // --------------------------
            // MEMBER ONLY PERMISSIONS
            // --------------------------
            case "MEMBER_VIEW":
            case "VIEW_ONLY":
                // Only members have these basic views
                return isMember();

            // --------------------------
            // DEFAULT PERMISSIONS
            // --------------------------
            default:
                // Default: Admin only
                return isAdmin();
        }
    }

    // ==========================
    //  QUICK PERMISSION CHECKS
    // ==========================
    public static boolean canViewBooks() {
        return canPerform("VIEW_BOOKS");
    }
    
    public static boolean canManageBooks() {
        return canPerform("MANAGE_BOOKS");
    }
    
    public static boolean canManageCirculation() {
        return canPerform("CIRCULATION");
    }
    
    public static boolean canViewReports() {
        return canPerform("REPORT");
    }
    
    public static boolean canManageUsers() {
        return canPerform("MANAGE_USERS");
    }

    // ==========================
    //  USER STRING FOR DISPLAY
    // ==========================
    public static String getUserInfo() {
        return String.format("%s (%s) - %s",
                fullName != null ? fullName : "Unknown",
                username != null ? username : "Unknown",
                getRoleDisplay());
    }

    // ==========================
    //   DEBUG SESSION INFO
    // ==========================
    public static void printSessionInfo() {
        System.out.println("\n===== USER SESSION INFO =====");
        System.out.println("User ID: " + userId);
        System.out.println("Username: " + username);
        System.out.println("Full Name: " + fullName);
        System.out.println("Role: " + role + " (" + getRoleDisplay() + ")");
        System.out.println("Email: " + (email != null ? email : "Not set"));
        System.out.println("Phone: " + (phone != null ? phone : "Not set"));
        System.out.println("=============================\n");
        
        // Print permissions summary
        System.out.println("===== PERMISSIONS SUMMARY =====");
        System.out.println("View Books: " + canViewBooks());
        System.out.println("Manage Books: " + canManageBooks());
        System.out.println("Manage Circulation: " + canManageCirculation());
        System.out.println("View Reports: " + canViewReports());
        System.out.println("Manage Users: " + canManageUsers());
        System.out.println("===============================\n");
    }
}