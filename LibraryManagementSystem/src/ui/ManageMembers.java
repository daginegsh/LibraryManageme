package ui;

import utils.DBConnection;
import utils.UserSession; // Import UserSession for access control
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.time.LocalDate;

public class ManageMembers extends JFrame {
    
    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField txtMemberId, txtMemberCode, txtFullName, txtEmail, txtPhone, txtUserId;
    private JComboBox<String> cmbStatus;
    private JButton btnAdd, btnUpdate, btnDelete, btnClear, btnRefresh, btnGenerateMemberId, btnGenerateUserId;
    private JLabel statsLabel;
    private int selectedMemberId = -1;
    
    public ManageMembers() {
        setTitle("👥 Manage Members");
        setSize(1100, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        
        // Access Control Check
        if (!UserSession.canPerform("MANAGE_MEMBERS")) {
            JOptionPane.showMessageDialog(this, 
                "⚠️ Access Denied! Only Librarians and Administrators can manage members.", 
                "Access Restricted", JOptionPane.ERROR_MESSAGE);
        }
        
        createUI();
        applyAccessControl();
        loadMembers();
    }
    
    private void createUI() {
        // Main panel
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // ===== FORM PANEL (Top) =====
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("👥 Member Information"));
        formPanel.setBackground(new Color(248, 249, 250));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Title
        JLabel titleLabel = new JLabel("ADD / EDIT MEMBERS");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        titleLabel.setForeground(new Color(0, 102, 204));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 4;
        formPanel.add(titleLabel, gbc);
        
        // Row 1: Member ID with Generate button
        gbc.gridwidth = 1;
        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(createLabel("Member ID*:"), gbc);
        
        JPanel memberIdPanel = new JPanel(new BorderLayout(5, 0));
        txtMemberId = new JTextField(10);
        txtMemberId.setEditable(false); // Read-only, use Generate button
        memberIdPanel.add(txtMemberId, BorderLayout.CENTER);
        btnGenerateMemberId = new JButton("Generate");
        btnGenerateMemberId.setBackground(new Color(100, 149, 237));
        btnGenerateMemberId.setForeground(Color.WHITE);
        memberIdPanel.add(btnGenerateMemberId, BorderLayout.EAST);
        gbc.gridx = 1;
        formPanel.add(memberIdPanel, gbc);
        
        // Row 1 continued: Member Code
        gbc.gridx = 2;
        formPanel.add(createLabel("Member Code*:"), gbc);
        
        gbc.gridx = 3;
        txtMemberCode = new JTextField(15);
        formPanel.add(txtMemberCode, gbc);
        
        // Row 2: Full Name and Email
        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(createLabel("Full Name*:"), gbc);
        
        gbc.gridx = 1;
        txtFullName = new JTextField(20);
        formPanel.add(txtFullName, gbc);
        
        gbc.gridx = 2;
        formPanel.add(createLabel("Email:"), gbc);
        
        gbc.gridx = 3;
        txtEmail = new JTextField(20);
        formPanel.add(txtEmail, gbc);
        
        // Row 3: Phone and User ID with Generate button
        gbc.gridx = 0; gbc.gridy = 3;
        formPanel.add(createLabel("Phone:"), gbc);
        
        gbc.gridx = 1;
        txtPhone = new JTextField(15);
        formPanel.add(txtPhone, gbc);
        
        gbc.gridx = 2;
        formPanel.add(createLabel("User ID:"), gbc);
        
        JPanel userIdPanel = new JPanel(new BorderLayout(5, 0));
        txtUserId = new JTextField(10);
        txtUserId.setToolTipText("Link to users table (optional - enter any number or leave empty)");
        userIdPanel.add(txtUserId, BorderLayout.CENTER);
        btnGenerateUserId = new JButton("Generate");
        btnGenerateUserId.setBackground(new Color(100, 149, 237));
        btnGenerateUserId.setForeground(Color.WHITE);
        userIdPanel.add(btnGenerateUserId, BorderLayout.EAST);
        gbc.gridx = 3;
        formPanel.add(userIdPanel, gbc);
        
        // Row 4: Status
        gbc.gridx = 0; gbc.gridy = 4;
        formPanel.add(createLabel("Status:"), gbc);
        
        gbc.gridx = 1;
        cmbStatus = new JComboBox<>(new String[]{"ACTIVE", "INACTIVE", "SUSPENDED"});
        cmbStatus.setPreferredSize(new Dimension(150, 25));
        formPanel.add(cmbStatus, gbc);
        
        // ===== BUTTON PANEL =====
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        buttonPanel.setBackground(new Color(248, 249, 250));
        
        btnAdd = createButton("➕ Add Member", new Color(40, 167, 69));
        btnUpdate = createButton("✏️ Update", new Color(0, 123, 255));
        btnDelete = createButton("🗑️ Delete", new Color(220, 53, 69));
        btnClear = createButton("🧹 Clear", new Color(108, 117, 125));
        btnRefresh = createButton("🔄 Refresh", new Color(23, 162, 184));
        
        buttonPanel.add(btnAdd);
        buttonPanel.add(btnUpdate);
        buttonPanel.add(btnDelete);
        buttonPanel.add(btnClear);
        buttonPanel.add(btnRefresh);
        
        // ===== TABLE PANEL =====
        String[] columns = {"Member ID", "Member Code", "Full Name", "Email", "Phone", 
                           "User ID", "Join Date", "Status", "Borrowed"};
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
                loadSelectedMember();
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createTitledBorder("📋 Members List"));
        
        // ===== STATS PANEL =====
        JPanel statsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statsPanel.setBackground(new Color(233, 236, 239));
        statsPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        
        statsLabel = new JLabel("📊 Total Members: 0 | Active: 0 | Borrowing History: 0 books");
        statsLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        statsPanel.add(statsLabel);
        
        // ===== ASSEMBLE =====
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(formPanel, BorderLayout.CENTER);
        topPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(statsPanel, BorderLayout.SOUTH);
        
        add(mainPanel);
        
        // ===== ACTION LISTENERS =====
        btnAdd.addActionListener(e -> addMember());
        btnUpdate.addActionListener(e -> updateMember());
        btnDelete.addActionListener(e -> deleteMember());
        btnClear.addActionListener(e -> clearForm());
        btnRefresh.addActionListener(e -> loadMembers());
        btnGenerateMemberId.addActionListener(e -> generateMemberId());
        btnGenerateUserId.addActionListener(e -> generateUserId());
    }
    
    private void generateMemberId() {
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = DBConnection.getConnection();
            String sql = "SELECT COALESCE(MAX(member_id), 0) + 1 as next_id FROM members";
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);
            
            if (rs.next()) {
                int nextId = rs.getInt("next_id");
                txtMemberId.setText(String.valueOf(nextId));
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, 
                "Error generating Member ID: " + e.getMessage(),
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
    
    private void generateUserId() {
        // Simply generate a suggested User ID without checking users table
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = DBConnection.getConnection();
            
            // Try to get max user_id from members table for suggestion
            String sql = "SELECT COALESCE(MAX(user_id), 0) + 1 as next_id FROM members WHERE user_id IS NOT NULL";
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);
            
            if (rs.next()) {
                int nextId = rs.getInt("next_id");
                if (nextId > 0) {
                    txtUserId.setText(String.valueOf(nextId));
                } else {
                    // If no user_id in members, suggest 1
                    txtUserId.setText("1");
                }
            } else {
                txtUserId.setText("1");
            }
        } catch (SQLException e) {
            // If error, just suggest 1
            txtUserId.setText("1");
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
        boolean canManage = UserSession.canPerform("MANAGE_MEMBERS");
        
        btnAdd.setEnabled(canManage);
        btnUpdate.setEnabled(false); // Initially disabled
        btnDelete.setEnabled(false); // Initially disabled
        btnGenerateMemberId.setEnabled(canManage);
        btnGenerateUserId.setEnabled(canManage);
        
        txtMemberId.setEditable(false); // Always read-only, use Generate button
        txtMemberCode.setEditable(canManage);
        txtFullName.setEditable(canManage);
        txtEmail.setEditable(canManage);
        txtPhone.setEditable(canManage);
        txtUserId.setEditable(canManage);
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
    
    private void loadMembers() {
        tableModel.setRowCount(0);
        Connection conn = null;
        PreparedStatement pst = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            
            String sql = "SELECT member_id, member_code, full_name, email, phone, " +
                        "user_id, join_date, status, total_borrowed " +
                        "FROM members ORDER BY member_code";
            
            pst = conn.prepareStatement(sql);
            rs = pst.executeQuery();

            int totalMembers = 0;
            int activeMembers = 0;
            int totalBorrowed = 0;

            while (rs.next()) {
                totalMembers++;
                String status = rs.getString("status");
                if ("ACTIVE".equals(status)) activeMembers++;
                totalBorrowed += rs.getInt("total_borrowed");

                Object[] row = {
                    rs.getInt("member_id"),
                    rs.getString("member_code"),
                    rs.getString("full_name"),
                    rs.getString("email") != null ? rs.getString("email") : "N/A",
                    rs.getString("phone") != null ? rs.getString("phone") : "N/A",
                    rs.getObject("user_id") != null ? rs.getInt("user_id") : "N/A",
                    rs.getDate("join_date"),
                    status,
                    rs.getInt("total_borrowed")
                };
                tableModel.addRow(row);
            }

            // Update stats
            updateStats(totalMembers, activeMembers, totalBorrowed);

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, 
                "Error loading members: " + e.getMessage(), 
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
    
    private void updateStats(int totalMembers, int activeMembers, int totalBorrowed) {
        statsLabel.setText(
            "📊 Total Members: " + totalMembers + 
            " | Active: " + activeMembers + 
            " | Borrowing History: " + totalBorrowed + " books"
        );
    }
    
    private void loadSelectedMember() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow != -1) {
            selectedMemberId = Integer.parseInt(tableModel.getValueAt(selectedRow, 0).toString());
            txtMemberId.setText(String.valueOf(selectedMemberId));
            txtMemberCode.setText(tableModel.getValueAt(selectedRow, 1).toString());
            txtFullName.setText(tableModel.getValueAt(selectedRow, 2).toString());
            
            String email = tableModel.getValueAt(selectedRow, 3).toString();
            txtEmail.setText(email.equals("N/A") ? "" : email);
            
            String phone = tableModel.getValueAt(selectedRow, 4).toString();
            txtPhone.setText(phone.equals("N/A") ? "" : phone);
            
            Object userIdObj = tableModel.getValueAt(selectedRow, 5);
            if (userIdObj != null && !userIdObj.toString().equals("N/A")) {
                txtUserId.setText(userIdObj.toString());
            } else {
                txtUserId.setText("");
            }
            
            String status = tableModel.getValueAt(selectedRow, 7).toString();
            cmbStatus.setSelectedItem(status);
            
            // Enable update/delete buttons if user has permission
            boolean canManage = UserSession.canPerform("MANAGE_MEMBERS");
            btnAdd.setEnabled(false);
            btnUpdate.setEnabled(canManage);
            btnDelete.setEnabled(canManage);
        }
    }
    
    private void addMember() {
        if (!validateFields(true)) return;
        
        // Check if Member ID is generated
        if (txtMemberId.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "❌ Please generate a Member ID first!", 
                "Validation Error", JOptionPane.WARNING_MESSAGE);
            btnGenerateMemberId.requestFocus();
            return;
        }
        
        Connection conn = null;
        PreparedStatement pst = null;
        
        try {
            conn = DBConnection.getConnection();
            
            // Check if Member ID already exists
            String checkIdSql = "SELECT COUNT(*) FROM members WHERE member_id = ?";
            PreparedStatement checkIdStmt = conn.prepareStatement(checkIdSql);
            checkIdStmt.setInt(1, Integer.parseInt(txtMemberId.getText().trim()));
            ResultSet idRs = checkIdStmt.executeQuery();
            idRs.next();
            if (idRs.getInt(1) > 0) {
                JOptionPane.showMessageDialog(this, 
                    "❌ Member ID already exists! Please generate a new one.", 
                    "Duplicate ID", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Check if Member Code already exists
            String checkCodeSql = "SELECT COUNT(*) FROM members WHERE member_code = ?";
            PreparedStatement checkCodeStmt = conn.prepareStatement(checkCodeSql);
            checkCodeStmt.setString(1, txtMemberCode.getText().trim());
            ResultSet codeRs = checkCodeStmt.executeQuery();
            codeRs.next();
            if (codeRs.getInt(1) > 0) {
                JOptionPane.showMessageDialog(this, 
                    "❌ Member Code already exists!", 
                    "Duplicate Code", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Insert with specified member_id - NO FOREIGN KEY CHECK
            String sql = "INSERT INTO members " +
                        "(member_id, member_code, full_name, email, phone, user_id, join_date, status) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            pst = conn.prepareStatement(sql);
            
            pst.setInt(1, Integer.parseInt(txtMemberId.getText().trim()));
            pst.setString(2, txtMemberCode.getText().trim());
            pst.setString(3, txtFullName.getText().trim());
            
            String email = txtEmail.getText().trim();
            if (!email.isEmpty()) {
                pst.setString(4, email);
            } else {
                pst.setNull(4, Types.VARCHAR);
            }
            
            String phone = txtPhone.getText().trim();
            if (!phone.isEmpty()) {
                pst.setString(5, phone);
            } else {
                pst.setNull(5, Types.VARCHAR);
            }
            
            String userIdStr = txtUserId.getText().trim();
            if (!userIdStr.isEmpty()) {
                try {
                    int userId = Integer.parseInt(userIdStr);
                    pst.setInt(6, userId);
                } catch (NumberFormatException e) {
                    // If not a valid number, set to NULL
                    pst.setNull(6, Types.INTEGER);
                }
            } else {
                pst.setNull(6, Types.INTEGER);
            }
            
            // Set join date to today
            pst.setDate(7, Date.valueOf(LocalDate.now()));
            
            pst.setString(8, (String) cmbStatus.getSelectedItem());
            
            int rows = pst.executeUpdate();
            if (rows > 0) {
                UserSession.logActivity("MEMBER_ADD", 
                    "Added member ID " + txtMemberId.getText() + ": " + txtFullName.getText());
                
                JOptionPane.showMessageDialog(this, 
                    "✅ Member added successfully!\nMember ID: " + txtMemberId.getText(), 
                    "Success", JOptionPane.INFORMATION_MESSAGE);
                loadMembers();
                clearForm();
            }
            
        } catch (SQLException e) {
            if (e.getErrorCode() == 1062) { // Duplicate entry error
                JOptionPane.showMessageDialog(this, 
                    "❌ Member Code already exists!", 
                    "Duplicate Entry", JOptionPane.ERROR_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, 
                    "Error: " + e.getMessage(), 
                    "Database Error", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, 
                "❌ Member ID must be a valid number!", 
                "Validation Error", JOptionPane.WARNING_MESSAGE);
        } finally {
            try {
                if (pst != null) pst.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
    
    private void updateMember() {
        if (selectedMemberId == -1) {
            JOptionPane.showMessageDialog(this, 
                "❌ Please select a member to update!", 
                "Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        if (!validateFields(false)) return;
        
        Connection conn = null;
        PreparedStatement pst = null;
        
        try {
            conn = DBConnection.getConnection();
            String sql = "UPDATE members SET " +
                        "member_code=?, full_name=?, email=?, phone=?, " +
                        "user_id=?, status=? " +
                        "WHERE member_id=?";
            pst = conn.prepareStatement(sql);
            
            pst.setString(1, txtMemberCode.getText().trim());
            pst.setString(2, txtFullName.getText().trim());
            
            String email = txtEmail.getText().trim();
            if (!email.isEmpty()) {
                pst.setString(3, email);
            } else {
                pst.setNull(3, Types.VARCHAR);
            }
            
            String phone = txtPhone.getText().trim();
            if (!phone.isEmpty()) {
                pst.setString(4, phone);
            } else {
                pst.setNull(4, Types.VARCHAR);
            }
            
            String userIdStr = txtUserId.getText().trim();
            if (!userIdStr.isEmpty()) {
                try {
                    int userId = Integer.parseInt(userIdStr);
                    pst.setInt(5, userId);
                } catch (NumberFormatException e) {
                    // If not a valid number, set to NULL
                    pst.setNull(5, Types.INTEGER);
                }
            } else {
                pst.setNull(5, Types.INTEGER);
            }
            
            pst.setString(6, (String) cmbStatus.getSelectedItem());
            pst.setInt(7, selectedMemberId);
            
            int rows = pst.executeUpdate();
            if (rows > 0) {
                UserSession.logActivity("MEMBER_UPDATE", 
                    "Updated member ID " + selectedMemberId + ": " + txtFullName.getText());
                JOptionPane.showMessageDialog(this, 
                    "✅ Member updated successfully!", 
                    "Success", JOptionPane.INFORMATION_MESSAGE);
                loadMembers();
                clearForm();
            }
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, 
                "Error updating member: " + e.getMessage(), 
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
    
    private void deleteMember() {
        if (selectedMemberId == -1) {
            JOptionPane.showMessageDialog(this, 
                "❌ Please select a member to delete!", 
                "Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String memberName = txtFullName.getText();
        int borrowedBooks = getMemberBorrowedBooks(selectedMemberId);
        
        String warningMessage;
        if (borrowedBooks > 0) {
            warningMessage = "Are you sure you want to delete:\n" +
                           "Member ID: " + selectedMemberId + "\n" +
                           "Name: " + memberName + "\n\n" +
                           "⚠️ WARNING: This member has " + borrowedBooks + " borrowed book(s)!\n" +
                           "Member cannot be deleted while they have outstanding books.";
            JOptionPane.showMessageDialog(this, warningMessage, 
                "Cannot Delete", JOptionPane.WARNING_MESSAGE);
            return;
        } else {
            warningMessage = "Are you sure you want to delete:\n" +
                           "Member ID: " + selectedMemberId + "\n" +
                           "Name: " + memberName + "?";
        }
        
        int confirm = JOptionPane.showConfirmDialog(this,
            warningMessage,
            "Confirm Delete",
            JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        
        if (confirm == JOptionPane.YES_OPTION) {
            Connection conn = null;
            PreparedStatement pst = null;
            
            try {
                conn = DBConnection.getConnection();
                String sql = "DELETE FROM members WHERE member_id = ?";
                pst = conn.prepareStatement(sql);
                pst.setInt(1, selectedMemberId);
                
                int rows = pst.executeUpdate();
                if (rows > 0) {
                    UserSession.logActivity("MEMBER_DELETE", 
                        "Deleted member ID " + selectedMemberId + ": " + memberName);
                    JOptionPane.showMessageDialog(this, 
                        "✅ Member deleted successfully!", 
                        "Success", JOptionPane.INFORMATION_MESSAGE);
                    loadMembers();
                    clearForm();
                }
                
            } catch (SQLException e) {
                if (e.getErrorCode() == 1451) {
                    JOptionPane.showMessageDialog(this, 
                        "❌ Cannot delete member!\n" +
                        "Member has borrowing history or is linked to other records.", 
                        "Foreign Key Constraint", JOptionPane.ERROR_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, 
                        "Error deleting member: " + e.getMessage(), 
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
    
    private int getMemberBorrowedBooks(int memberId) {
        Connection conn = null;
        PreparedStatement pst = null;
        ResultSet rs = null;
        
        try {
            conn = DBConnection.getConnection();
            // Check if member has any issued books
            String sql = "SELECT COUNT(*) as issued_count FROM circulation " +
                        "WHERE member_id = ? AND status IN ('ISSUED', 'OVERDUE', 'LATE')";
            pst = conn.prepareStatement(sql);
            pst.setInt(1, memberId);
            rs = pst.executeQuery();
            
            if (rs.next()) {
                return rs.getInt("issued_count");
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
        return 0;
    }
    
    private boolean validateFields(boolean isNewMember) {
        // Check Member ID for new members
        if (isNewMember && txtMemberId.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "❌ Please generate a Member ID!", 
                "Validation Error", JOptionPane.WARNING_MESSAGE);
            btnGenerateMemberId.requestFocus();
            return false;
        }
        
        String memberCode = txtMemberCode.getText().trim();
        String fullName = txtFullName.getText().trim();
        String email = txtEmail.getText().trim();
        String phone = txtPhone.getText().trim();
        String userIdStr = txtUserId.getText().trim();
        
        if (memberCode.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "❌ Member Code is required!", 
                "Validation Error", JOptionPane.WARNING_MESSAGE);
            txtMemberCode.requestFocus();
            return false;
        }
        
        if (fullName.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "❌ Full Name is required!", 
                "Validation Error", JOptionPane.WARNING_MESSAGE);
            txtFullName.requestFocus();
            return false;
        }
        
        // Validate member code length
        if (memberCode.length() > 20) {
            JOptionPane.showMessageDialog(this, 
                "❌ Member Code cannot exceed 20 characters!", 
                "Validation Error", JOptionPane.WARNING_MESSAGE);
            txtMemberCode.requestFocus();
            return false;
        }
        
        // Validate full name length
        if (fullName.length() > 100) {
            JOptionPane.showMessageDialog(this, 
                "❌ Full Name cannot exceed 100 characters!", 
                "Validation Error", JOptionPane.WARNING_MESSAGE);
            txtFullName.requestFocus();
            return false;
        }
        
        // Validate email format if provided
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
        
        // Validate phone format if provided
        if (!phone.isEmpty()) {
            if (phone.length() > 20) {
                JOptionPane.showMessageDialog(this, 
                    "❌ Phone number cannot exceed 20 characters!", 
                    "Validation Error", JOptionPane.WARNING_MESSAGE);
                txtPhone.requestFocus();
                return false;
            }
            if (!phone.matches("^[0-9+\\-\\s()]+$")) {
                JOptionPane.showMessageDialog(this, 
                    "❌ Phone number can only contain digits, +, -, spaces, and parentheses!", 
                    "Validation Error", JOptionPane.WARNING_MESSAGE);
                txtPhone.requestFocus();
                return false;
            }
        }
        
        return true;
    }
    
    private void clearForm() {
        txtMemberId.setText("");
        txtMemberCode.setText("");
        txtFullName.setText("");
        txtEmail.setText("");
        txtPhone.setText("");
        txtUserId.setText("");
        cmbStatus.setSelectedIndex(0); // Set to "ACTIVE"
        selectedMemberId = -1;
        table.clearSelection();
        
        // Restore button states
        boolean canManage = UserSession.canPerform("MANAGE_MEMBERS");
        btnAdd.setEnabled(canManage);
        btnUpdate.setEnabled(false);
        btnDelete.setEnabled(false);
        btnGenerateMemberId.setEnabled(canManage);
        btnGenerateUserId.setEnabled(canManage);
    }
    
    public static void main(String[] args) {
        // For testing purposes
        UserSession.setRole("LIBRARIAN");
        
        SwingUtilities.invokeLater(() -> {
            new ManageMembers().setVisible(true);
        });
    }
}