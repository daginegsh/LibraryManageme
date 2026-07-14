package ui;

import utils.DBConnection;
import utils.UserSession;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ManageUsers extends JFrame {
    
    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField txtUserId, txtUsername, txtFullName, txtEmail, txtPhone, txtSearch;
    private JPasswordField txtPassword, txtConfirmPassword;
    private JComboBox<String> cmbRole, cmbStatus;
    private JButton btnAdd, btnUpdate, btnDelete, btnClear, btnRefresh, btnChangePassword, btnSearch, btnShowAll, btnGenerateUserId;
    private JLabel statsLabel;
    private int selectedUserId = -1;
    
    public ManageUsers() {
        setTitle("👥 Manage Users");
        setSize(1400, 750);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        
        // Access Control Check - Only ADMIN can manage users
        if (!UserSession.canPerform("MANAGE_USERS") && !UserSession.isAdmin()) {
            JOptionPane.showMessageDialog(this, 
                "⚠️ Access Denied!\nOnly Administrators can manage users.", 
                "Access Restricted", JOptionPane.ERROR_MESSAGE);
        }
        
        createUI();
        applyAccessControl();
        loadUsers();
    }
    
    private void createUI() {
        // Main panel with BorderLayout
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // ===== FORM PANEL (Left) =====
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("👤 User Information"));
        formPanel.setPreferredSize(new Dimension(500, 0));
        formPanel.setBackground(new Color(248, 249, 250));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Title
        JLabel titleLabel = new JLabel("ADD / EDIT USER ACCOUNTS");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        titleLabel.setForeground(new Color(0, 102, 204));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 3;
        formPanel.add(titleLabel, gbc);
        
        // Row 1: User ID with Generate button
        gbc.gridwidth = 1;
        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(createLabel("User ID*:"), gbc);
        
        JPanel userIdPanel = new JPanel(new BorderLayout(5, 0));
        txtUserId = new JTextField(10);
        txtUserId.setEditable(false); // User can't edit it directly
        userIdPanel.add(txtUserId, BorderLayout.CENTER);
        btnGenerateUserId = new JButton("Generate");
        btnGenerateUserId.setBackground(new Color(100, 149, 237));
        btnGenerateUserId.setForeground(Color.WHITE);
        userIdPanel.add(btnGenerateUserId, BorderLayout.EAST);
        gbc.gridx = 1; gbc.gridwidth = 2;
        formPanel.add(userIdPanel, gbc);
        
        // Row 2: Username
        gbc.gridwidth = 1;
        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(createLabel("Username*:"), gbc);
        
        gbc.gridx = 1; gbc.gridwidth = 2;
        txtUsername = new JTextField(20);
        txtUsername.setToolTipText("Minimum 3 characters, unique");
        formPanel.add(txtUsername, gbc);
        
        // Row 3: Password
        gbc.gridwidth = 1;
        gbc.gridx = 0; gbc.gridy = 3;
        formPanel.add(createLabel("Password:"), gbc);
        
        gbc.gridx = 1; gbc.gridwidth = 2;
        txtPassword = new JPasswordField(20);
        txtPassword.setToolTipText("Leave blank to keep current password");
        formPanel.add(txtPassword, gbc);
        
        // Row 4: Confirm Password
        gbc.gridx = 0; gbc.gridy = 4;
        formPanel.add(createLabel("Confirm:"), gbc);
        
        gbc.gridx = 1; gbc.gridwidth = 2;
        txtConfirmPassword = new JPasswordField(20);
        formPanel.add(txtConfirmPassword, gbc);
        
        // Row 5: Full Name
        gbc.gridx = 0; gbc.gridy = 5;
        formPanel.add(createLabel("Full Name*:"), gbc);
        
        gbc.gridx = 1; gbc.gridwidth = 2;
        txtFullName = new JTextField(20);
        txtFullName.setToolTipText("Enter user's full name");
        formPanel.add(txtFullName, gbc);
        
        // Row 6: Email
        gbc.gridx = 0; gbc.gridy = 6;
        formPanel.add(createLabel("Email:"), gbc);
        
        gbc.gridx = 1; gbc.gridwidth = 2;
        txtEmail = new JTextField(20);
        txtEmail.setToolTipText("Enter valid email address");
        formPanel.add(txtEmail, gbc);
        
        // Row 7: Phone
        gbc.gridx = 0; gbc.gridy = 7;
        formPanel.add(createLabel("Phone:"), gbc);
        
        gbc.gridx = 1; gbc.gridwidth = 2;
        txtPhone = new JTextField(20);
        txtPhone.setToolTipText("Enter phone number");
        formPanel.add(txtPhone, gbc);
        
        // Row 8: Role
        gbc.gridx = 0; gbc.gridy = 8;
        formPanel.add(createLabel("Role*:"), gbc);
        
        gbc.gridx = 1; gbc.gridwidth = 2;
        cmbRole = new JComboBox<>(new String[]{"ADMIN", "LIBRARIAN", "STAFF", "MEMBER"});
        cmbRole.setPreferredSize(new Dimension(200, 25));
        formPanel.add(cmbRole, gbc);
        
        // Row 9: Status
        gbc.gridx = 0; gbc.gridy = 9;
        formPanel.add(createLabel("Status:"), gbc);
        
        gbc.gridx = 1; gbc.gridwidth = 2;
        cmbStatus = new JComboBox<>(new String[]{"ACTIVE", "INACTIVE", "SUSPENDED"});
        cmbStatus.setPreferredSize(new Dimension(200, 25));
        formPanel.add(cmbStatus, gbc);
        
        // ===== BUTTON PANEL =====
        JPanel buttonPanel = new JPanel(new GridLayout(2, 3, 10, 10));
        buttonPanel.setBackground(new Color(248, 249, 250));
        
        btnAdd = createButton("➕ Add User", new Color(40, 167, 69));
        btnUpdate = createButton("✏️ Update", new Color(0, 123, 255));
        btnDelete = createButton("🗑️ Delete", new Color(220, 53, 69));
        btnClear = createButton("🧹 Clear", new Color(108, 117, 125));
        btnRefresh = createButton("🔄 Refresh", new Color(23, 162, 184));
        btnChangePassword = createButton("🔐 Change Password", new Color(111, 66, 193));
        
        buttonPanel.add(btnAdd);
        buttonPanel.add(btnUpdate);
        buttonPanel.add(btnDelete);
        buttonPanel.add(btnClear);
        buttonPanel.add(btnRefresh);
        buttonPanel.add(btnChangePassword);
        
        // Add button panel to form
        gbc.gridx = 0; gbc.gridy = 10; gbc.gridwidth = 3;
        formPanel.add(buttonPanel, gbc);
        
        // ===== TABLE PANEL (Right) =====
        String[] columns = {
            "User ID", "Username", "Full Name", "Role", "Email", "Phone", 
            "Status", "Last Login", "Created", "Updated"
        };
        
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        table = new JTable(tableModel);
        table.setRowHeight(25);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && table.getSelectedRow() != -1) {
                loadSelectedUser();
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createTitledBorder("📋 User Accounts"));
        
        // ===== SEARCH PANEL =====
        JPanel searchPanel = new JPanel(new GridBagLayout());
        searchPanel.setBorder(BorderFactory.createTitledBorder("🔍 Search Users"));
        searchPanel.setBackground(new Color(233, 236, 239));
        
        GridBagConstraints sgbc = new GridBagConstraints();
        sgbc.insets = new Insets(5, 5, 5, 5);
        sgbc.fill = GridBagConstraints.HORIZONTAL;
        
        sgbc.gridx = 0; sgbc.gridy = 0;
        searchPanel.add(createLabel("Search:"), sgbc);
        
        sgbc.gridx = 1; sgbc.gridwidth = 2;
        txtSearch = new JTextField(30);
        searchPanel.add(txtSearch, sgbc);
        
        sgbc.gridwidth = 1;
        sgbc.gridx = 3;
        btnSearch = createButton("Search", new Color(32, 201, 151));
        searchPanel.add(btnSearch, sgbc);
        
        sgbc.gridx = 4;
        btnShowAll = createButton("Show All", new Color(108, 117, 125));
        searchPanel.add(btnShowAll, sgbc);
        
        // ===== STATS PANEL =====
        JPanel statsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statsPanel.setBackground(new Color(233, 236, 239));
        statsPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        
        statsLabel = new JLabel("📊 Total Users: 0 | Admins: 0 | Active: 0");
        statsLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        statsPanel.add(statsLabel);
        
        // ===== ASSEMBLE =====
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.add(formPanel, BorderLayout.CENTER);
        leftPanel.add(new JPanel(), BorderLayout.SOUTH); // Spacer
        
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, scrollPane);
        splitPane.setResizeWeight(0.35);
        splitPane.setDividerLocation(500);
        
        mainPanel.add(searchPanel, BorderLayout.NORTH);
        mainPanel.add(splitPane, BorderLayout.CENTER);
        mainPanel.add(statsPanel, BorderLayout.SOUTH);
        
        add(mainPanel);
        
        // ===== ACTION LISTENERS =====
        btnAdd.addActionListener(e -> addUser());
        btnUpdate.addActionListener(e -> updateUser());
        btnDelete.addActionListener(e -> deleteUser());
        btnClear.addActionListener(e -> clearForm());
        btnRefresh.addActionListener(e -> loadUsers());
        btnChangePassword.addActionListener(e -> changePassword());
        btnSearch.addActionListener(e -> searchUsers());
        btnShowAll.addActionListener(e -> {
            txtSearch.setText("");
            loadUsers();
        });
        btnGenerateUserId.addActionListener(e -> generateUserId());
    }
    
    private void generateUserId() {
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = DBConnection.getConnection();
            String sql = "SELECT COALESCE(MAX(user_id), 0) + 1 as next_id FROM users";
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);
            
            if (rs.next()) {
                int nextId = rs.getInt("next_id");
                txtUserId.setText(String.valueOf(nextId));
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, 
                "Error generating User ID: " + e.getMessage(),
                "Database Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
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
    
    private void applyAccessControl() {
        boolean canManage = UserSession.canPerform("MANAGE_USERS") || UserSession.isAdmin();
        
        btnAdd.setEnabled(canManage);
        btnUpdate.setEnabled(false); // Initially disabled
        btnDelete.setEnabled(false); // Initially disabled
        btnChangePassword.setEnabled(false); // Initially disabled
        btnGenerateUserId.setEnabled(canManage);
        
        txtUserId.setEditable(false); // Always read-only, use Generate button
        txtUsername.setEditable(canManage);
        txtFullName.setEditable(canManage);
        txtEmail.setEditable(canManage);
        txtPhone.setEditable(canManage);
        txtPassword.setEditable(canManage);
        txtConfirmPassword.setEditable(canManage);
        cmbRole.setEnabled(canManage);
        cmbStatus.setEnabled(canManage);
    }
    
    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Arial", Font.BOLD, 12));
        return label;
    }
    
    private JButton createButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Arial", Font.BOLD, 12));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        return button;
    }
    
    private void loadUsers() {
        tableModel.setRowCount(0);
        Connection conn = null;
        PreparedStatement pst = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            
            String sql = "SELECT user_id, username, full_name, role, email, phone, " +
                        "status, last_login, created_at, updated_at " +
                        "FROM users ORDER BY user_id DESC";
            
            pst = conn.prepareStatement(sql);
            rs = pst.executeQuery();

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
            int totalUsers = 0;
            int adminCount = 0;
            int activeCount = 0;

            while (rs.next()) {
                totalUsers++;
                String role = rs.getString("role");
                String status = rs.getString("status");
                
                if ("ADMIN".equals(role)) adminCount++;
                if ("ACTIVE".equals(status)) activeCount++;

                Object[] row = {
                    rs.getInt("user_id"),
                    rs.getString("username"),
                    rs.getString("full_name"),
                    role,
                    rs.getString("email") != null ? rs.getString("email") : "N/A",
                    rs.getString("phone") != null ? rs.getString("phone") : "N/A",
                    status,
                    formatDateTime(rs.getTimestamp("last_login")),
                    formatDateTime(rs.getTimestamp("created_at")),
                    formatDateTime(rs.getTimestamp("updated_at"))
                };
                tableModel.addRow(row);
            }

            // Update stats
            updateStats(totalUsers, adminCount, activeCount);

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, 
                "Error loading users: " + e.getMessage(), 
                "Database Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
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
    
    private String formatDateTime(Timestamp timestamp) {
        if (timestamp == null) return "Never";
        try {
            return timestamp.toLocalDateTime()
                .format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm"));
        } catch (Exception e) {
            return "N/A";
        }
    }
    
    private void updateStats(int totalUsers, int adminCount, int activeCount) {
        statsLabel.setText(
            "📊 Total Users: " + totalUsers + 
            " | Admins: " + adminCount + 
            " | Active: " + activeCount
        );
    }
    
    private void loadSelectedUser() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow != -1) {
            selectedUserId = Integer.parseInt(tableModel.getValueAt(selectedRow, 0).toString());
            txtUserId.setText(String.valueOf(selectedUserId));
            txtUsername.setText(tableModel.getValueAt(selectedRow, 1).toString());
            txtFullName.setText(tableModel.getValueAt(selectedRow, 2).toString());
            
            cmbRole.setSelectedItem(tableModel.getValueAt(selectedRow, 3).toString());
            
            String email = tableModel.getValueAt(selectedRow, 4).toString();
            txtEmail.setText(email.equals("N/A") ? "" : email);
            
            String phone = tableModel.getValueAt(selectedRow, 5).toString();
            txtPhone.setText(phone.equals("N/A") ? "" : phone);
            
            cmbStatus.setSelectedItem(tableModel.getValueAt(selectedRow, 6).toString());
            
            // Clear passwords
            txtPassword.setText("");
            txtConfirmPassword.setText("");
            
            // Enable buttons if user has permission
            boolean canManage = UserSession.canPerform("MANAGE_USERS") || UserSession.isAdmin();
            if (canManage) {
                btnAdd.setEnabled(false);
                btnUpdate.setEnabled(true);
                btnDelete.setEnabled(true);
                btnChangePassword.setEnabled(true);
            }
        }
    }
    
    private void addUser() {
        if (!validateForm(true)) return;
        
        // Check if User ID is generated
        if (txtUserId.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "❌ Please generate a User ID first!", 
                "Validation Error", JOptionPane.WARNING_MESSAGE);
            btnGenerateUserId.requestFocus();
            return;
        }
        
        String password = new String(txtPassword.getPassword());
        String confirmPassword = new String(txtConfirmPassword.getPassword());
        
        // Password validation for new user
        if (password.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "❌ Password is required for new user!", 
                "Validation Error", JOptionPane.WARNING_MESSAGE);
            txtPassword.requestFocus();
            return;
        }
        
        if (!password.equals(confirmPassword)) {
            JOptionPane.showMessageDialog(this, 
                "❌ Passwords do not match!", 
                "Validation Error", JOptionPane.WARNING_MESSAGE);
            txtPassword.requestFocus();
            return;
        }
        
        if (password.length() < 6) {
            JOptionPane.showMessageDialog(this, 
                "❌ Password must be at least 6 characters!", 
                "Validation Error", JOptionPane.WARNING_MESSAGE);
            txtPassword.requestFocus();
            return;
        }
        
        Connection conn = null;
        PreparedStatement pst = null;
        
        try {
            conn = DBConnection.getConnection();
            
            // Check if User ID already exists
            String checkIdSql = "SELECT COUNT(*) FROM users WHERE user_id = ?";
            PreparedStatement checkIdStmt = conn.prepareStatement(checkIdSql);
            checkIdStmt.setInt(1, Integer.parseInt(txtUserId.getText().trim()));
            ResultSet idRs = checkIdStmt.executeQuery();
            idRs.next();
            if (idRs.getInt(1) > 0) {
                JOptionPane.showMessageDialog(this, 
                    "❌ User ID already exists! Please generate a new one.", 
                    "Duplicate ID", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Check if username already exists
            String checkUserSql = "SELECT COUNT(*) FROM users WHERE username = ?";
            PreparedStatement checkUserStmt = conn.prepareStatement(checkUserSql);
            checkUserStmt.setString(1, txtUsername.getText().trim());
            ResultSet userRs = checkUserStmt.executeQuery();
            userRs.next();
            if (userRs.getInt(1) > 0) {
                JOptionPane.showMessageDialog(this, 
                    "❌ Username already exists!", 
                    "Duplicate Username", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Insert with specified user_id
            String sql = "INSERT INTO users (user_id, username, password, full_name, email, phone, role, status) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            pst = conn.prepareStatement(sql);
            
            pst.setInt(1, Integer.parseInt(txtUserId.getText().trim()));
            pst.setString(2, txtUsername.getText().trim());
            
            // In real application, you should hash the password!
            pst.setString(3, password);
            
            pst.setString(4, txtFullName.getText().trim());
            
            String email = txtEmail.getText().trim();
            if (!email.isEmpty()) {
                pst.setString(5, email);
            } else {
                pst.setNull(5, Types.VARCHAR);
            }
            
            String phone = txtPhone.getText().trim();
            if (!phone.isEmpty()) {
                pst.setString(6, phone);
            } else {
                pst.setNull(6, Types.VARCHAR);
            }
            
            pst.setString(7, (String) cmbRole.getSelectedItem());
            pst.setString(8, (String) cmbStatus.getSelectedItem());
            
            int rows = pst.executeUpdate();
            if (rows > 0) {
                UserSession.logActivity("USER_CREATE", 
                    "Created user ID " + txtUserId.getText() + ": " + txtUsername.getText());
                
                JOptionPane.showMessageDialog(this, 
                    "✅ User added successfully!\nUser ID: " + txtUserId.getText(), 
                    "Success", JOptionPane.INFORMATION_MESSAGE);
                loadUsers();
                clearForm();
            }
            
        } catch (SQLException e) {
            if (e.getErrorCode() == 1062) { // Duplicate entry error
                JOptionPane.showMessageDialog(this, 
                    "❌ User ID or Username already exists!", 
                    "Duplicate Entry", JOptionPane.ERROR_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, 
                    "Error: " + e.getMessage(), 
                    "Database Error", JOptionPane.ERROR_MESSAGE);
            }
            e.printStackTrace();
        } finally {
            try {
                if (pst != null) pst.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
    
    private void updateUser() {
        if (selectedUserId == -1) {
            JOptionPane.showMessageDialog(this, 
                "❌ Please select a user to update!", 
                "Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        if (!validateForm(false)) return;
        
        // Security check: Prevent users from modifying their own role/status
        if (selectedUserId == UserSession.getUserId()) {
            String currentRole = (String) cmbRole.getSelectedItem();
            String currentStatus = (String) cmbStatus.getSelectedItem();
            
            if (!"ADMIN".equals(currentRole)) {
                JOptionPane.showMessageDialog(this, 
                    "⚠️ You cannot change your own role from ADMIN!", 
                    "Security Restriction", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            if (!"ACTIVE".equals(currentStatus)) {
                JOptionPane.showMessageDialog(this, 
                    "⚠️ You cannot deactivate your own account!", 
                    "Security Restriction", JOptionPane.WARNING_MESSAGE);
                return;
            }
        }
        
        Connection conn = null;
        PreparedStatement pst = null;
        
        try {
            conn = DBConnection.getConnection();
            String sql = "UPDATE users SET full_name = ?, email = ?, phone = ?, " +
                        "role = ?, status = ? WHERE user_id = ?";
            pst = conn.prepareStatement(sql);
            
            pst.setString(1, txtFullName.getText().trim());
            
            String email = txtEmail.getText().trim();
            if (!email.isEmpty()) {
                pst.setString(2, email);
            } else {
                pst.setNull(2, Types.VARCHAR);
            }
            
            String phone = txtPhone.getText().trim();
            if (!phone.isEmpty()) {
                pst.setString(3, phone);
            } else {
                pst.setNull(3, Types.VARCHAR);
            }
            
            pst.setString(4, (String) cmbRole.getSelectedItem());
            pst.setString(5, (String) cmbStatus.getSelectedItem());
            pst.setInt(6, selectedUserId);
            
            int rows = pst.executeUpdate();
            if (rows > 0) {
                UserSession.logActivity("USER_UPDATE", 
                    "Updated user ID " + selectedUserId + ": " + txtUsername.getText());
                JOptionPane.showMessageDialog(this, 
                    "✅ User updated successfully!", 
                    "Success", JOptionPane.INFORMATION_MESSAGE);
                loadUsers();
                clearForm();
            }
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, 
                "Error updating user: " + e.getMessage(), 
                "Database Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        } finally {
            try {
                if (pst != null) pst.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
    
    private void deleteUser() {
        if (selectedUserId == -1) {
            JOptionPane.showMessageDialog(this, 
                "❌ Please select a user to delete!", 
                "Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Prevent self-deletion
        if (selectedUserId == UserSession.getUserId()) {
            JOptionPane.showMessageDialog(this, 
                "⚠️ You cannot delete your own account!\n" +
                "Please ask another administrator to delete your account.", 
                "Cannot Delete Self", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String username = txtUsername.getText();
        String fullName = txtFullName.getText();
        
        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to delete this user?\n\n" +
            "User ID: " + selectedUserId + "\n" +
            "Username: " + username + "\n" +
            "Full Name: " + fullName + "\n\n" +
            "⚠️ This action cannot be undone!",
            "Confirm Delete",
            JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        
        if (confirm == JOptionPane.YES_OPTION) {
            Connection conn = null;
            PreparedStatement pst = null;
            
            try {
                conn = DBConnection.getConnection();
                
                // Check if user is referenced in members table
                if (isUserLinkedToMember(selectedUserId)) {
                    int choice = JOptionPane.showConfirmDialog(this,
                        "This user is linked to a member record.\n" +
                        "Do you want to break the link and delete the user anyway?",
                        "Linked User", JOptionPane.YES_NO_OPTION);
                    
                    if (choice != JOptionPane.YES_OPTION) return;
                    
                    // Break the link first
                    String unlinkSql = "UPDATE members SET user_id = NULL WHERE user_id = ?";
                    pst = conn.prepareStatement(unlinkSql);
                    pst.setInt(1, selectedUserId);
                    pst.executeUpdate();
                    pst.close();
                }
                
                String deleteSql = "DELETE FROM users WHERE user_id = ?";
                pst = conn.prepareStatement(deleteSql);
                pst.setInt(1, selectedUserId);
                
                int rows = pst.executeUpdate();
                if (rows > 0) {
                    UserSession.logActivity("USER_DELETE", 
                        "Deleted user ID " + selectedUserId + ": " + username);
                    JOptionPane.showMessageDialog(this, 
                        "✅ User deleted successfully!", 
                        "Success", JOptionPane.INFORMATION_MESSAGE);
                    loadUsers();
                    clearForm();
                }
                
            } catch (SQLException e) {
                if (e.getErrorCode() == 1451) {
                    JOptionPane.showMessageDialog(this, 
                        "❌ Cannot delete user!\n" +
                        "User is referenced in other tables.", 
                        "Foreign Key Constraint", JOptionPane.ERROR_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, 
                        "Error deleting user: " + e.getMessage(), 
                        "Database Error", JOptionPane.ERROR_MESSAGE);
                }
                e.printStackTrace();
            } finally {
                try {
                    if (pst != null) pst.close();
                    if (conn != null) conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    private boolean isUserLinkedToMember(int userId) {
        Connection conn = null;
        PreparedStatement pst = null;
        ResultSet rs = null;
        
        try {
            conn = DBConnection.getConnection();
            String sql = "SELECT COUNT(*) as count FROM members WHERE user_id = ?";
            pst = conn.prepareStatement(sql);
            pst.setInt(1, userId);
            rs = pst.executeQuery();
            
            if (rs.next()) {
                return rs.getInt("count") > 0;
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) rs.close();
                if (pst != null) pst.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return false;
    }
    
    private void changePassword() {
        if (selectedUserId == -1) {
            JOptionPane.showMessageDialog(this, 
                "❌ Please select a user!", 
                "Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String password = new String(txtPassword.getPassword());
        String confirmPassword = new String(txtConfirmPassword.getPassword());
        
        if (password.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "❌ Please enter a new password!", 
                "Validation Error", JOptionPane.WARNING_MESSAGE);
            txtPassword.requestFocus();
            return;
        }
        
        if (!password.equals(confirmPassword)) {
            JOptionPane.showMessageDialog(this, 
                "❌ Passwords do not match!", 
                "Validation Error", JOptionPane.WARNING_MESSAGE);
            txtPassword.requestFocus();
            return;
        }
        
        if (password.length() < 6) {
            JOptionPane.showMessageDialog(this, 
                "❌ Password must be at least 6 characters!", 
                "Validation Error", JOptionPane.WARNING_MESSAGE);
            txtPassword.requestFocus();
            return;
        }
        
        Connection conn = null;
        PreparedStatement pst = null;
        
        try {
            conn = DBConnection.getConnection();
            String sql = "UPDATE users SET password = ? WHERE user_id = ?";
            pst = conn.prepareStatement(sql);
            
            // In real application, you should hash the password!
            pst.setString(1, password);
            pst.setInt(2, selectedUserId);
            
            int rows = pst.executeUpdate();
            if (rows > 0) {
                if (selectedUserId == UserSession.getUserId()) {
                    UserSession.logActivity("PASSWORD_CHANGE", "Changed own password");
                } else {
                    UserSession.logActivity("PASSWORD_RESET", 
                        "Reset password for user ID: " + selectedUserId);
                }
                
                JOptionPane.showMessageDialog(this, 
                    "✅ Password changed successfully!", 
                    "Success", JOptionPane.INFORMATION_MESSAGE);
                txtPassword.setText("");
                txtConfirmPassword.setText("");
            }
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, 
                "Error changing password: " + e.getMessage(), 
                "Database Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        } finally {
            try {
                if (pst != null) pst.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
    
    private void searchUsers() {
        String searchTerm = txtSearch.getText().trim();
        if (searchTerm.isEmpty()) {
            loadUsers();
            return;
        }
        
        tableModel.setRowCount(0);
        Connection conn = null;
        PreparedStatement pst = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            
            String sql = "SELECT user_id, username, full_name, role, email, phone, " +
                        "status, last_login, created_at, updated_at " +
                        "FROM users WHERE user_id LIKE ? OR username LIKE ? OR full_name LIKE ? OR email LIKE ? " +
                        "OR role LIKE ? OR status LIKE ? " +
                        "ORDER BY user_id DESC";
            
            pst = conn.prepareStatement(sql);
            String likeTerm = "%" + searchTerm + "%";
            pst.setString(1, likeTerm);
            pst.setString(2, likeTerm);
            pst.setString(3, likeTerm);
            pst.setString(4, likeTerm);
            pst.setString(5, likeTerm);
            pst.setString(6, likeTerm);
            
            rs = pst.executeQuery();

            while (rs.next()) {
                Object[] row = {
                    rs.getInt("user_id"),
                    rs.getString("username"),
                    rs.getString("full_name"),
                    rs.getString("role"),
                    rs.getString("email") != null ? rs.getString("email") : "N/A",
                    rs.getString("phone") != null ? rs.getString("phone") : "N/A",
                    rs.getString("status"),
                    formatDateTime(rs.getTimestamp("last_login")),
                    formatDateTime(rs.getTimestamp("created_at")),
                    formatDateTime(rs.getTimestamp("updated_at"))
                };
                tableModel.addRow(row);
            }
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, 
                "Error searching users: " + e.getMessage(), 
                "Search Error", JOptionPane.ERROR_MESSAGE);
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
    
    private boolean validateForm(boolean isNewUser) {
        // Check User ID for new users
        if (isNewUser && txtUserId.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "❌ Please generate a User ID!", 
                "Validation Error", JOptionPane.WARNING_MESSAGE);
            btnGenerateUserId.requestFocus();
            return false;
        }
        
        String username = txtUsername.getText().trim();
        String fullName = txtFullName.getText().trim();
        String email = txtEmail.getText().trim();
        
        if (username.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "❌ Username is required!", 
                "Validation Error", JOptionPane.WARNING_MESSAGE);
            txtUsername.requestFocus();
            return false;
        }
        
        if (isNewUser && username.length() < 3) {
            JOptionPane.showMessageDialog(this, 
                "❌ Username must be at least 3 characters!", 
                "Validation Error", JOptionPane.WARNING_MESSAGE);
            txtUsername.requestFocus();
            return false;
        }
        
        if (username.length() > 50) {
            JOptionPane.showMessageDialog(this, 
                "❌ Username cannot exceed 50 characters!", 
                "Validation Error", JOptionPane.WARNING_MESSAGE);
            txtUsername.requestFocus();
            return false;
        }
        
        if (fullName.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "❌ Full name is required!", 
                "Validation Error", JOptionPane.WARNING_MESSAGE);
            txtFullName.requestFocus();
            return false;
        }
        
        if (fullName.length() > 100) {
            JOptionPane.showMessageDialog(this, 
                "❌ Full name cannot exceed 100 characters!", 
                "Validation Error", JOptionPane.WARNING_MESSAGE);
            txtFullName.requestFocus();
            return false;
        }
        
        if (!email.isEmpty()) {
            if (email.length() > 100) {
                JOptionPane.showMessageDialog(this, 
                    "❌ Email cannot exceed 100 characters!", 
                    "Validation Error", JOptionPane.WARNING_MESSAGE);
                txtEmail.requestFocus();
                return false;
            }
            if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                JOptionPane.showMessageDialog(this, 
                    "❌ Please enter a valid email address!", 
                    "Validation Error", JOptionPane.WARNING_MESSAGE);
                txtEmail.requestFocus();
                return false;
            }
        }
        
        return true;
    }
    
    private void clearForm() {
        txtUserId.setText("");
        txtUsername.setText("");
        txtPassword.setText("");
        txtConfirmPassword.setText("");
        txtFullName.setText("");
        txtEmail.setText("");
        txtPhone.setText("");
        txtSearch.setText("");
        cmbRole.setSelectedIndex(0);
        cmbStatus.setSelectedIndex(0);
        selectedUserId = -1;
        table.clearSelection();
        
        // Restore button states
        boolean canManage = UserSession.canPerform("MANAGE_USERS") || UserSession.isAdmin();
        btnAdd.setEnabled(canManage);
        btnUpdate.setEnabled(false);
        btnDelete.setEnabled(false);
        btnChangePassword.setEnabled(false);
        btnGenerateUserId.setEnabled(canManage);
    }
    
    public static void main(String[] args) {
        // For testing purposes
        UserSession.setUserId(1);
        UserSession.setUsername("admin");
        UserSession.setFullName("System Administrator");
        UserSession.setRole("ADMIN");
        
        SwingUtilities.invokeLater(() -> {
            new ManageUsers().setVisible(true);
        });
    }
}