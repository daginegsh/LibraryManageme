package ui;

import utils.DBConnection;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;

public class SignupForm extends JFrame {
    
    private JTextField txtFullName, txtUsername, txtEmail, txtPhone;
    private JPasswordField txtPassword, txtConfirmPassword;
    private JComboBox<String> cmbRole;
    private JButton btnSignup, btnBack;
    
    public SignupForm() {
        setTitle("📚 Library System - Sign Up");
        setSize(500, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        
        initUI();
    }
    
    private void initUI() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(new Color(240, 245, 250));
        
        // ===== HEADER =====
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setBackground(new Color(240, 245, 250));
        headerPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel lblIcon = new JLabel("📚");
        lblIcon.setFont(new Font("Arial", Font.PLAIN, 40));
        lblIcon.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel lblTitle = new JLabel("Create New Account");
        lblTitle.setFont(new Font("Arial", Font.BOLD, 22));
        lblTitle.setForeground(new Color(0, 102, 204));
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel lblSubtitle = new JLabel("Fill in your details to register as a library member");
        lblSubtitle.setFont(new Font("Arial", Font.PLAIN, 12));
        lblSubtitle.setForeground(Color.GRAY);
        lblSubtitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        headerPanel.add(lblIcon);
        headerPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        headerPanel.add(lblTitle);
        headerPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        headerPanel.add(lblSubtitle);
        headerPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        
        // ===== SIGNUP FORM =====
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(new Color(240, 245, 250));
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        
        // Full Name
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(createLabel("Full Name*:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 2;
        txtFullName = new JTextField(25);
        txtFullName.setFont(new Font("Arial", Font.PLAIN, 14));
        formPanel.add(txtFullName, gbc);
        
        // Username
        gbc.gridwidth = 1;
        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(createLabel("Username*:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 2;
        txtUsername = new JTextField(25);
        txtUsername.setFont(new Font("Arial", Font.PLAIN, 14));
        formPanel.add(txtUsername, gbc);
        
        // Email
        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(createLabel("Email*:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 2;
        txtEmail = new JTextField(25);
        txtEmail.setFont(new Font("Arial", Font.PLAIN, 14));
        formPanel.add(txtEmail, gbc);
        
        // Phone
        gbc.gridx = 0; gbc.gridy = 3;
        formPanel.add(createLabel("Phone:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 2;
        txtPhone = new JTextField(25);
        txtPhone.setFont(new Font("Arial", Font.PLAIN, 14));
        formPanel.add(txtPhone, gbc);
        
        // Password
        gbc.gridx = 0; gbc.gridy = 4;
        formPanel.add(createLabel("Password*:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 2;
        txtPassword = new JPasswordField(25);
        txtPassword.setFont(new Font("Arial", Font.PLAIN, 14));
        formPanel.add(txtPassword, gbc);
        
        // Confirm Password
        gbc.gridx = 0; gbc.gridy = 5;
        formPanel.add(createLabel("Confirm Password*:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 2;
        txtConfirmPassword = new JPasswordField(25);
        txtConfirmPassword.setFont(new Font("Arial", Font.PLAIN, 14));
        formPanel.add(txtConfirmPassword, gbc);
        
        // Role (Only MEMBER for self-signup)
        gbc.gridx = 0; gbc.gridy = 6;
        formPanel.add(createLabel("Account Type:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 2;
        cmbRole = new JComboBox<>(new String[]{"MEMBER"});
        cmbRole.setFont(new Font("Arial", Font.PLAIN, 14));
        cmbRole.setEnabled(false); // Only member signup allowed
        formPanel.add(cmbRole, gbc);
        
        // Info label about role
        gbc.gridx = 0; gbc.gridy = 7; gbc.gridwidth = 3;
        JLabel lblInfo = new JLabel("Note: Self-registration is only available for MEMBER accounts.");
        lblInfo.setFont(new Font("Arial", Font.ITALIC, 11));
        lblInfo.setForeground(Color.GRAY);
        formPanel.add(lblInfo, gbc);
        
        // ===== BUTTONS =====
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        buttonPanel.setBackground(new Color(240, 245, 250));
        
        btnSignup = createButton("✅ Sign Up", new Color(40, 167, 69));
        btnSignup.setPreferredSize(new Dimension(120, 40));
        
        btnBack = createButton("↩️ Back to Login", new Color(108, 117, 125));
        btnBack.setPreferredSize(new Dimension(150, 40));
        
        buttonPanel.add(btnSignup);
        buttonPanel.add(btnBack);
        
        // ===== ASSEMBLE =====
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(formPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        add(mainPanel);
        
        // ===== EVENT LISTENERS =====
        btnSignup.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                registerUser();
            }
        });
        
        btnBack.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                goBackToLogin();
            }
        });
        
        // Add Enter key support
        txtConfirmPassword.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                registerUser();
            }
        });
    }
    
    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Arial", Font.BOLD, 14));
        return label;
    }
    
    private JButton createButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        return button;
    }
    
    private void registerUser() {
        // Get form data
        String fullName = txtFullName.getText().trim();
        String username = txtUsername.getText().trim();
        String email = txtEmail.getText().trim();
        String phone = txtPhone.getText().trim();
        String password = new String(txtPassword.getPassword()).trim();
        String confirmPassword = new String(txtConfirmPassword.getPassword()).trim();
        String role = (String) cmbRole.getSelectedItem();
        
        // Validation
        if (fullName.isEmpty() || username.isEmpty() || email.isEmpty() || 
            password.isEmpty() || confirmPassword.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "❌ Fields marked with * are required!", 
                "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Full name validation
        if (fullName.length() > 100) {
            JOptionPane.showMessageDialog(this, 
                "❌ Full name cannot exceed 100 characters!", 
                "Validation Error", JOptionPane.WARNING_MESSAGE);
            txtFullName.requestFocus();
            return;
        }
        
        // Username validation
        if (username.length() > 50) {
            JOptionPane.showMessageDialog(this, 
                "❌ Username cannot exceed 50 characters!", 
                "Validation Error", JOptionPane.WARNING_MESSAGE);
            txtUsername.requestFocus();
            return;
        }
        
        if (username.length() < 3) {
            JOptionPane.showMessageDialog(this, 
                "❌ Username must be at least 3 characters!", 
                "Validation Error", JOptionPane.WARNING_MESSAGE);
            txtUsername.requestFocus();
            return;
        }
        
        // Email validation
        if (email.length() > 100) {
            JOptionPane.showMessageDialog(this, 
                "❌ Email cannot exceed 100 characters!", 
                "Validation Error", JOptionPane.WARNING_MESSAGE);
            txtEmail.requestFocus();
            return;
        }
        
        if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            JOptionPane.showMessageDialog(this, 
                "❌ Please enter a valid email address!", 
                "Validation Error", JOptionPane.WARNING_MESSAGE);
            txtEmail.requestFocus();
            return;
        }
        
        // Phone validation (optional)
        if (!phone.isEmpty()) {
            if (phone.length() > 20) {
                JOptionPane.showMessageDialog(this, 
                    "❌ Phone number cannot exceed 20 characters!", 
                    "Validation Error", JOptionPane.WARNING_MESSAGE);
                txtPhone.requestFocus();
                return;
            }
            if (!phone.matches("^[0-9+\\-\\s()]+$")) {
                JOptionPane.showMessageDialog(this, 
                    "❌ Phone number can only contain digits, +, -, spaces, and parentheses!", 
                    "Validation Error", JOptionPane.WARNING_MESSAGE);
                txtPhone.requestFocus();
                return;
            }
        }
        
        // Password validation
        if (!password.equals(confirmPassword)) {
            JOptionPane.showMessageDialog(this, 
                "❌ Passwords do not match!", 
                "Validation Error", JOptionPane.WARNING_MESSAGE);
            txtPassword.setText("");
            txtConfirmPassword.setText("");
            txtPassword.requestFocus();
            return;
        }
        
        if (password.length() < 6) {
            JOptionPane.showMessageDialog(this, 
                "❌ Password must be at least 6 characters long!", 
                "Validation Error", JOptionPane.WARNING_MESSAGE);
            txtPassword.requestFocus();
            return;
        }
        
        // Check if username already exists
        if (isUsernameTaken(username)) {
            JOptionPane.showMessageDialog(this, 
                "❌ Username already taken. Please choose another.", 
                "Registration Error", JOptionPane.ERROR_MESSAGE);
            txtUsername.requestFocus();
            return;
        }
        
        // Check if email already exists
        if (isEmailTaken(email)) {
            JOptionPane.showMessageDialog(this, 
                "❌ Email already registered. Please use another email.", 
                "Registration Error", JOptionPane.ERROR_MESSAGE);
            txtEmail.requestFocus();
            return;
        }
        
        // Generate user_id manually
        int userId = generateUserId();
        if (userId == -1) {
            JOptionPane.showMessageDialog(this, 
                "❌ Unable to generate user ID. Please try again.", 
                "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Register user in database
        Connection conn = null;
        PreparedStatement pst = null;
        
        try {
            conn = DBConnection.getConnection();
            
            // Now include user_id in the INSERT statement
            String sql = "INSERT INTO users (user_id, username, password, full_name, email, phone, role, status) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, 'ACTIVE')";
            pst = conn.prepareStatement(sql);
            
            pst.setInt(1, userId);
            pst.setString(2, username);
            
            // IMPORTANT: In production, you should hash the password!
            pst.setString(3, password);
            
            pst.setString(4, fullName);
            pst.setString(5, email);
            
            if (!phone.isEmpty()) {
                pst.setString(6, phone);
            } else {
                pst.setNull(6, Types.VARCHAR);
            }
            
            pst.setString(7, role);
            
            int rowsAffected = pst.executeUpdate();
            
            if (rowsAffected > 0) {
                JOptionPane.showMessageDialog(this, 
                    "🎉 Registration Successful!\n\n" +
                    "User ID: " + userId + "\n" +
                    "Account Type: " + role + "\n" +
                    "Status: ACTIVE\n\n" +
                    "You can now login with your credentials.",
                    "Success", JOptionPane.INFORMATION_MESSAGE);
                
                // Log the registration
                System.out.println("New user registered: " + username + " (User ID: " + userId + ")");
                
                // Ask if user wants to create a member record
                int choice = JOptionPane.showConfirmDialog(this,
                    "Would you like to create a member profile linked to this account?\n" +
                    "This will allow you to borrow books from the library.",
                    "Create Member Profile",
                    JOptionPane.YES_NO_OPTION);
                
                if (choice == JOptionPane.YES_OPTION) {
                    createMemberRecord(userId, fullName, email, phone);
                }
                
                // Go back to login
                goBackToLogin();
                
            } else {
                JOptionPane.showMessageDialog(this, 
                    "❌ Registration failed. Please try again.", 
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
            
        } catch (SQLException e) {
            // Check for duplicate user_id
            if (e.getErrorCode() == 1062 && e.getMessage().contains("user_id")) {
                // Try again with a new ID
                registerUser(); // Recursive call with new ID
            } else {
                JOptionPane.showMessageDialog(this, 
                    "❌ Database error: " + e.getMessage(), 
                    "Error", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        } finally {
            try {
                if (pst != null) pst.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
    
    private int generateUserId() {
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = DBConnection.getConnection();
            stmt = conn.createStatement();
            
            // Get the maximum user_id from the table
            rs = stmt.executeQuery("SELECT MAX(user_id) as max_id FROM users");
            int nextId = 1;
            if (rs.next() && rs.getObject("max_id") != null) {
                nextId = rs.getInt("max_id") + 1;
            }
            
            return nextId;
            
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
    
    private void createMemberRecord(int userId, String fullName, String email, String phone) {
        Connection conn = null;
        PreparedStatement pst = null;
        
        try {
            conn = DBConnection.getConnection();
            
            // Generate a member code (e.g., MEM-001)
            String memberCode = generateMemberCode();
            
            // Generate member_id
            int memberId = generateMemberId();
            if (memberId == -1) return;
            
            String sql = "INSERT INTO members (member_id, member_code, full_name, email, phone, user_id, join_date, status) " +
                        "VALUES (?, ?, ?, ?, ?, ?, CURDATE(), 'ACTIVE')";
            pst = conn.prepareStatement(sql);
            
            pst.setInt(1, memberId);
            pst.setString(2, memberCode);
            pst.setString(3, fullName);
            
            if (email != null && !email.isEmpty()) {
                pst.setString(4, email);
            } else {
                pst.setNull(4, Types.VARCHAR);
            }
            
            if (phone != null && !phone.isEmpty()) {
                pst.setString(5, phone);
            } else {
                pst.setNull(5, Types.VARCHAR);
            }
            
            pst.setInt(6, userId);
            
            int rowsAffected = pst.executeUpdate();
            
            if (rowsAffected > 0) {
                JOptionPane.showMessageDialog(this,
                    "✅ Member profile created successfully!\n" +
                    "Member ID: " + memberId + "\n" +
                    "Member Code: " + memberCode + "\n" +
                    "You can now borrow books from the library.",
                    "Member Profile Created", 
                    JOptionPane.INFORMATION_MESSAGE);
            }
            
        } catch (SQLException e) {
            System.err.println("Error creating member record: " + e.getMessage());
            // Don't show error to user - member creation is optional
        } finally {
            try {
                if (pst != null) pst.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
    
    private int generateMemberId() {
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = DBConnection.getConnection();
            stmt = conn.createStatement();
            
            rs = stmt.executeQuery("SELECT MAX(member_id) as max_id FROM members");
            int nextId = 1;
            if (rs.next() && rs.getObject("max_id") != null) {
                nextId = rs.getInt("max_id") + 1;
            }
            
            return nextId;
            
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
    
    private String generateMemberCode() {
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = DBConnection.getConnection();
            stmt = conn.createStatement();
            
            // Get the next member ID
            rs = stmt.executeQuery("SELECT MAX(member_id) as max_id FROM members");
            int nextId = 1;
            if (rs.next() && rs.getObject("max_id") != null) {
                nextId = rs.getInt("max_id") + 1;
            }
            
            // Format as MEM-001, MEM-002, etc.
            return String.format("MEM-%03d", nextId);
            
        } catch (SQLException e) {
            e.printStackTrace();
            return "MEM-" + System.currentTimeMillis(); // Fallback
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
    
    private boolean isUsernameTaken(String username) {
        Connection conn = null;
        PreparedStatement pst = null;
        ResultSet rs = null;
        
        try {
            conn = DBConnection.getConnection();
            String sql = "SELECT user_id FROM users WHERE username = ?";
            pst = conn.prepareStatement(sql);
            pst.setString(1, username);
            rs = pst.executeQuery();
            
            return rs.next(); // Returns true if username exists
            
        } catch (SQLException e) {
            e.printStackTrace();
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
    
    private boolean isEmailTaken(String email) {
        Connection conn = null;
        PreparedStatement pst = null;
        ResultSet rs = null;
        
        try {
            conn = DBConnection.getConnection();
            String sql = "SELECT user_id FROM users WHERE email = ?";
            pst = conn.prepareStatement(sql);
            pst.setString(1, email);
            rs = pst.executeQuery();
            
            return rs.next(); // Returns true if email exists
            
        } catch (SQLException e) {
            e.printStackTrace();
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
    
    private void goBackToLogin() {
        dispose(); // Close signup form
        // Assuming you have a LoginForm class
        // new LoginForm().setVisible(true);
        // For now, just close
        System.out.println("Please implement LoginForm class");
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new SignupForm().setVisible(true);
            }
        });
    }
}