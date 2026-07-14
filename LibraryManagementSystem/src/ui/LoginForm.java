package ui;

import utils.DBConnection;
import utils.UserSession;
import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class LoginForm extends JFrame {
    
    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JButton btnLogin;
    private JButton btnCancel;
    
    public LoginForm() {
        setTitle("Library Login");
        setSize(430, 350);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        // Gradient background panel
        JPanel bgPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                GradientPaint gp = new GradientPaint(
                        0, 0, new Color(0, 102, 204),
                        0, getHeight(), new Color(0, 204, 204)
                );
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        bgPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        // Card panel
        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(Color.WHITE);
        card.setPreferredSize(new Dimension(330, 250));
        card.setBorder(BorderFactory.createLineBorder(new Color(0, 102, 204), 2));

        // Title
        JLabel title = new JLabel("LIBRARY LOGIN");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(new Color(0, 102, 204));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        card.add(title, gbc);

        gbc.gridwidth = 1;

        gbc.gridx = 0; gbc.gridy = 1;
        JLabel lblUser = new JLabel("Username:");
        lblUser.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        card.add(lblUser, gbc);

        gbc.gridx = 1;
        txtUsername = new JTextField(15);
        txtUsername.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        card.add(txtUsername, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        JLabel lblPass = new JLabel("Password:");
        lblPass.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        card.add(lblPass, gbc);

        gbc.gridx = 1;
        txtPassword = new JPasswordField(15);
        txtPassword.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        card.add(txtPassword, gbc);

        btnLogin = new JButton("LOGIN");
        btnLogin.setBackground(new Color(0, 153, 51));
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setFont(new Font("Segoe UI", Font.BOLD, 14));

        btnCancel = new JButton("CANCEL");
        btnCancel.setBackground(new Color(204, 0, 0));
        btnCancel.setForeground(Color.WHITE);
        btnCancel.setFont(new Font("Segoe UI", Font.BOLD, 14));

        JPanel btnPanel = new JPanel();
        btnPanel.setBackground(Color.WHITE);
        btnPanel.add(btnLogin);
        btnPanel.add(btnCancel);

        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        card.add(btnPanel, gbc);

        bgPanel.add(card);
        add(bgPanel);

        btnLogin.addActionListener(e -> loginToDatabase());
        btnCancel.addActionListener(e -> System.exit(0));
        txtPassword.addActionListener(e -> loginToDatabase());
    }
    
    // ==================== AUTHENTICATION METHOD ====================
    private boolean authenticateUser(String username, String password) {
        Connection conn = null;
        PreparedStatement pst = null;
        ResultSet rs = null;
        
        try {
            conn = DBConnection.getConnection();
            if (conn == null) {
                System.out.println("Database connection is null!");
                return false;
            }
            
            // Get user from database with status check
            String sql = "SELECT user_id, username, password, full_name, role, email, phone, status " +
                        "FROM users WHERE username = ? AND status = 'ACTIVE'";
            pst = conn.prepareStatement(sql);
            pst.setString(1, username);
            
            rs = pst.executeQuery();
            
            if (rs.next()) {
                // Verify password
                String dbPassword = rs.getString("password");
                
                if (dbPassword.equals(password)) {
                    // Set user session
                    UserSession.setUserId(rs.getInt("user_id"));
                    UserSession.setUsername(rs.getString("username"));
                    UserSession.setFullName(rs.getString("full_name"));
                    UserSession.setRole(rs.getString("role"));
                    UserSession.setEmail(rs.getString("email"));
                    UserSession.setPhone(rs.getString("phone"));
                    
                    // Update last login
                    String updateSql = "UPDATE users SET last_login = NOW() WHERE user_id = ?";
                    PreparedStatement updateStmt = conn.prepareStatement(updateSql);
                    updateStmt.setInt(1, UserSession.getUserId());
                    updateStmt.executeUpdate();
                    updateStmt.close();
                    
                    return true;
                }
            }
            return false;
            
        } catch (SQLException e) {
            System.err.println("Database error in authenticateUser: " + e.getMessage());
            return false;
        } finally {
            try {
                if (rs != null) rs.close();
                if (pst != null) pst.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
    
    private void loginToDatabase() {
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword());
        
        // Validation
        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill all fields!");
            return;
        }
        
        // Show loading
        btnLogin.setText("Checking...");
        btnLogin.setEnabled(false);
        
        try {
            System.out.println("\n=== ATTEMPTING LOGIN ===");
            System.out.println("Username: " + username);
            
            boolean authenticated = authenticateUser(username, password);
            
            if (authenticated) {
                JOptionPane.showMessageDialog(this, 
                    "Login successful!\nWelcome, " + UserSession.getFullName() + 
                    "\nRole: " + UserSession.getRole(),
                    "Success", JOptionPane.INFORMATION_MESSAGE);

                this.dispose();
                
                String role = UserSession.getRole();
                System.out.println("User role after login: " + role);
                
                // Handle redirection based on role
                handleUserRedirection(role);
                
            } else {
                // Login failed
                JOptionPane.showMessageDialog(this, 
                    "Invalid username or password!\nNote: User must be ACTIVE status",
                    "Login Failed", JOptionPane.ERROR_MESSAGE);
                txtPassword.setText("");
                txtPassword.requestFocus();
            }
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "Login error:\n" + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
            
        } finally {
            // Restore button
            btnLogin.setText("LOGIN");
            btnLogin.setEnabled(true);
        }
    }
    
    private void handleUserRedirection(String role) {
        if (role == null || role.trim().isEmpty()) {
            JOptionPane.showMessageDialog(null, 
                "User role is empty. Please contact administrator.",
                "Error", JOptionPane.ERROR_MESSAGE);
            new LoginForm().setVisible(true);
            return;
        }
        
        role = role.toUpperCase();
        
        switch (role) {
            case "ADMIN":
            case "LIBRARIAN":
            case "STAFF":
                SwingUtilities.invokeLater(() -> {
                    new Dashboard().setVisible(true);
                });
                break;
                
            case "MEMBER":
                handleMemberLogin();
                break;
                
            default:
                JOptionPane.showMessageDialog(null, 
                    "Unknown role: " + role,
                    "Error", JOptionPane.ERROR_MESSAGE);
                new LoginForm().setVisible(true);
        }
    }
    
    private void handleMemberLogin() {
        System.out.println("=== HANDLING MEMBER LOGIN ===");
        System.out.println("User ID from session: " + UserSession.getUserId());
        
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            System.out.println("DB Connection for member lookup: " + (conn != null));
            
            // Try to find existing member
            String query = "SELECT member_id FROM members WHERE user_id = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, UserSession.getUserId());
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                int memberId = rs.getInt("member_id");
                System.out.println("Found existing member ID: " + memberId);
                
                rs.close();
                stmt.close();
                conn.close();
                
                // Launch member dashboard
                launchMemberDashboard(memberId);
                
            } else {
                System.out.println("No member found for user ID: " + UserSession.getUserId());
                rs.close();
                stmt.close();
                
                // Check if we should create a new member
                int choice = JOptionPane.showConfirmDialog(null,
                    "No member profile found for your account.\n" +
                    "Would you like to create a member profile now?",
                    "Create Member Profile",
                    JOptionPane.YES_NO_OPTION);
                
                if (choice == JOptionPane.YES_OPTION) {
                    createNewMember(conn);
                } else {
                    JOptionPane.showMessageDialog(null,
                        "You need a member profile to access member features.\n" +
                        "Please contact library staff for assistance.",
                        "Member Profile Required",
                        JOptionPane.INFORMATION_MESSAGE);
                    new LoginForm().setVisible(true);
                }
            }
            
        } catch (SQLException ex) {
            System.out.println("SQL Error in handleMemberLogin: " + ex.getMessage());
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null,
                "Database error: " + ex.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
            new LoginForm().setVisible(true);
        }
    }
    
    private void createNewMember(Connection conn) {
        try {
            // Get next member ID
            String maxQuery = "SELECT COALESCE(MAX(member_id), 0) + 1 as next_id FROM members";
            Statement maxStmt = conn.createStatement();
            ResultSet maxRs = maxStmt.executeQuery(maxQuery);
            maxRs.next();
            int newMemberId = maxRs.getInt("next_id");
            maxRs.close();
            maxStmt.close();
            
            // Create member code
            String memberCode = "MEM" + String.format("%03d", newMemberId);
            
            // Get user details for member creation
            String fullName = UserSession.getFullName();
            String email = UserSession.getEmail();
            String phone = UserSession.getPhone();
            
            // Insert new member
            String insertSql = "INSERT INTO members (member_id, member_code, full_name, email, phone, user_id, join_date, status) " +
                             "VALUES (?, ?, ?, ?, ?, ?, CURDATE(), 'ACTIVE')";
            PreparedStatement insertStmt = conn.prepareStatement(insertSql);
            insertStmt.setInt(1, newMemberId);
            insertStmt.setString(2, memberCode);
            insertStmt.setString(3, fullName);
            insertStmt.setString(4, email != null ? email : "");
            insertStmt.setString(5, phone != null ? phone : "");
            insertStmt.setInt(6, UserSession.getUserId());
            
            int rows = insertStmt.executeUpdate();
            insertStmt.close();
            conn.close();
            
            if (rows > 0) {
                System.out.println("Successfully created new member with ID: " + newMemberId);
                JOptionPane.showMessageDialog(null,
                    "Member profile created successfully!\n" +
                    "Your member code: " + memberCode,
                    "Profile Created",
                    JOptionPane.INFORMATION_MESSAGE);
                
                // Launch dashboard with new member ID
                launchMemberDashboard(newMemberId);
            } else {
                JOptionPane.showMessageDialog(null,
                    "Failed to create member profile.\nPlease contact administrator.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
                new LoginForm().setVisible(true);
            }
            
        } catch (SQLException ex) {
            System.out.println("Error creating new member: " + ex.getMessage());
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null,
                "Error creating member profile: " + ex.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
            new LoginForm().setVisible(true);
        }
    }
    
    private void launchMemberDashboard(int memberId) {
        System.out.println("Launching MemberDashboard with ID: " + memberId);
        
        SwingUtilities.invokeLater(() -> {
            try {
                MemberDashboard dashboard = new MemberDashboard(memberId);
                dashboard.setVisible(true);
                System.out.println("MemberDashboard launched successfully!");
            } catch (Exception e) {
                System.out.println("Error launching MemberDashboard: " + e.getMessage());
                e.printStackTrace();
                
                JOptionPane.showMessageDialog(null,
                    "Error opening member dashboard: " + e.getMessage() + 
                    "\nPlease try again or contact support.",
                    "Dashboard Error",
                    JOptionPane.ERROR_MESSAGE);
                    
                new LoginForm().setVisible(true);
            }
        });
    }
    
    // ==================== MAIN METHOD ====================
    public static void main(String[] args) {
        // First, check if users table exists
        checkDatabaseSetup();
        
        SwingUtilities.invokeLater(() -> {
            LoginForm loginForm = new LoginForm();
            loginForm.setVisible(true);
        });
    }
    
    private static void checkDatabaseSetup() {
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            if (conn != null) {
                System.out.println("✅ Database connected successfully!");
                
                // Check if users table exists
                DatabaseMetaData meta = conn.getMetaData();
                ResultSet tables = meta.getTables(null, null, "users", null);
                
                if (tables.next()) {
                    System.out.println("✅ Users table exists");
                    
                    // Check for member test user
                    Statement stmt = conn.createStatement();
                    ResultSet rs = stmt.executeQuery(
                        "SELECT COUNT(*) as count FROM users WHERE role = 'MEMBER'"
                    );
                    rs.next();
                    int memberCount = rs.getInt("count");
                    rs.close();
                    
                    if (memberCount == 0) {
                        System.out.println("⚠️ No MEMBER users found. Adding test member...");
                        
                        // Add test member user
                        stmt.executeUpdate(
                            "INSERT INTO users (username, password, full_name, email, role, status) VALUES " +
                            "('member', 'member123', 'John Member', 'member@library.com', 'MEMBER', 'ACTIVE')"
                        );
                        System.out.println("✅ Test member user added (username: 'member', password: 'member123')");
                    }
                    
                    stmt.close();
                } else {
                    System.out.println("\n⚠️ Users table does not exist!");
                }
                tables.close();
                
            } else {
                System.out.println("❌ Database connection failed!");
            }
            
        } catch (SQLException e) {
            System.out.println("❌ Database check failed: " + e.getMessage());
        } finally {
            try {
                if (conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}