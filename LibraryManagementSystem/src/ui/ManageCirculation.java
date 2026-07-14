package ui;

import utils.DBConnection;
import utils.UserSession;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class ManageCirculation extends JFrame {

    private JTable tblCirculation;
    private DefaultTableModel model;
    private JTextField txtBookId, txtMemberId, txtSearch;
    private JButton btnIssue, btnReturn, btnRenew, btnSearch, btnCalculateFine, btnPayFine;
    private JLabel lblStatus;

    public ManageCirculation() {
        setTitle("📚 Manage Circulation - User: " + UserSession.getFullName());
        setSize(1000, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // Access Control Check
        if (!UserSession.canPerform("MANAGE_CIRCULATION")) {
            JOptionPane.showMessageDialog(this, 
                "⚠️ Access Denied! Only Librarians and Administrators can manage circulation.", 
                "Access Restricted", JOptionPane.ERROR_MESSAGE);
        }

        initUI();
        applyAccessControl();
        updateOverdueStatus(); // Update overdue status on load
        loadCirculation();
    }

    // ---------------------------------------
    //               UI SETUP
    // ---------------------------------------
    private void initUI() {
        // Main panel with BorderLayout
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // ===== TOP PANEL: Issue/Renew/Return =====
        JPanel topPanel = new JPanel(new GridBagLayout());
        topPanel.setBorder(BorderFactory.createTitledBorder("📖 Issue/Return Book"));
        topPanel.setBackground(new Color(248, 249, 250));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Row 1: Book ID and Member ID
        gbc.gridx = 0; gbc.gridy = 0;
        topPanel.add(createLabel("Book ID*:"), gbc);
        
        gbc.gridx = 1;
        txtBookId = new JTextField(15);
        topPanel.add(txtBookId, gbc);
        
        gbc.gridx = 2;
        topPanel.add(createLabel("Member ID*:"), gbc);
        
        gbc.gridx = 3;
        txtMemberId = new JTextField(15);
        topPanel.add(txtMemberId, gbc);
        
        // Row 2: Buttons
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 4;
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 5));
        buttonPanel.setBackground(new Color(248, 249, 250));
        
        btnIssue = createButton("📥 Issue Book", new Color(40, 167, 69));
        btnReturn = createButton("📤 Return Book", new Color(0, 123, 255));
        btnRenew = createButton("🔄 Renew Book", new Color(255, 193, 7));
        btnCalculateFine = createButton("💰 Calculate Fine", new Color(108, 117, 125));
        btnPayFine = createButton("💳 Pay Fine", new Color(111, 66, 193));
        
        buttonPanel.add(btnIssue);
        buttonPanel.add(btnReturn);
        buttonPanel.add(btnRenew);
        buttonPanel.add(btnCalculateFine);
        buttonPanel.add(btnPayFine);
        
        topPanel.add(buttonPanel, gbc);
        
        // ===== CENTER PANEL: Table =====
        String[] columns = {
            "ID", "Book ID", "Book Title", "Member ID", "Member Name", 
            "Issue Date", "Return Date", "Actual Return", "Status", 
            "Fine", "Paid", "Renewed"
        };
        
        model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
            
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 10) return Boolean.class; // Paid column
                return String.class;
            }
        };
        
        tblCirculation = new JTable(model);
        tblCirculation.setRowHeight(25);
        tblCirculation.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblCirculation.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                updateButtonStates();
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(tblCirculation);
        scrollPane.setBorder(BorderFactory.createTitledBorder("📋 Circulation Records"));
        
        // ===== BOTTOM PANEL: Search and Status =====
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBackground(new Color(233, 236, 239));
        
        // Search panel (left)
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        searchPanel.setBackground(new Color(233, 236, 239));
        searchPanel.add(createLabel("🔍 Search:"));
        txtSearch = new JTextField(20);
        searchPanel.add(txtSearch);
        btnSearch = createButton("Search", new Color(23, 162, 184));
        searchPanel.add(btnSearch);
        
        JButton btnClearSearch = new JButton("Clear");
        btnClearSearch.addActionListener(e -> {
            txtSearch.setText("");
            loadCirculation();
        });
        searchPanel.add(btnClearSearch);
        
        // Status label (right)
        lblStatus = new JLabel("📊 Loading...");
        lblStatus.setFont(new Font("Arial", Font.PLAIN, 12));
        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        statusPanel.setBackground(new Color(233, 236, 239));
        statusPanel.add(lblStatus);
        
        bottomPanel.add(searchPanel, BorderLayout.WEST);
        bottomPanel.add(statusPanel, BorderLayout.EAST);
        
        // ===== ASSEMBLE =====
        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);
        
        add(mainPanel);
        
        // ===== ACTION LISTENERS =====
        btnIssue.addActionListener(e -> issueBook());
        btnReturn.addActionListener(e -> returnBook());
        btnRenew.addActionListener(e -> renewBook());
        btnSearch.addActionListener(e -> search());
        btnCalculateFine.addActionListener(e -> calculateFine());
        btnPayFine.addActionListener(e -> payFine());
    }
    
    private void applyAccessControl() {
        boolean canManage = UserSession.canPerform("MANAGE_CIRCULATION");
        
        btnIssue.setEnabled(canManage);
        btnReturn.setEnabled(false); // Initially disabled, enabled on selection
        btnRenew.setEnabled(false);  // Initially disabled, enabled on selection
        btnCalculateFine.setEnabled(canManage);
        btnPayFine.setEnabled(canManage);
        txtBookId.setEditable(canManage);
        txtMemberId.setEditable(canManage);
    }
    
    private void updateButtonStates() {
        int row = tblCirculation.getSelectedRow();
        boolean canManage = UserSession.canPerform("MANAGE_CIRCULATION");
        
        if (row != -1 && canManage) {
            String status = model.getValueAt(row, 8).toString();
            boolean isPaid = Boolean.TRUE.equals(model.getValueAt(row, 10));
            
            btnReturn.setEnabled("ISSUED".equals(status) || "OVERDUE".equals(status));
            btnRenew.setEnabled("ISSUED".equals(status));
            btnCalculateFine.setEnabled("OVERDUE".equals(status) || "LATE".equals(status));
            btnPayFine.setEnabled(("OVERDUE".equals(status) || "LATE".equals(status)) && !isPaid);
        } else {
            btnReturn.setEnabled(false);
            btnRenew.setEnabled(false);
            btnCalculateFine.setEnabled(false);
            btnPayFine.setEnabled(false);
        }
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

    // ---------------------------------------
    //           LOAD TABLE DATA
    // ---------------------------------------
    private void loadCirculation() {
        model.setRowCount(0);
        Connection conn = null;
        PreparedStatement pst = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            
            String query = "SELECT c.id, c.book_id, b.title, c.member_id, m.full_name, " +
                          "c.issue_date, c.return_date, c.actual_return_date, c.status, " +
                          "c.fine_amount, c.fine_paid, c.renewed_count " +
                          "FROM circulation c " +
                          "JOIN books b ON c.book_id = b.book_id " +
                          "JOIN members m ON c.member_id = m.member_id " +
                          "ORDER BY c.id DESC";
            
            pst = conn.prepareStatement(query);
            rs = pst.executeQuery();

            int totalRecords = 0;
            int issuedCount = 0;
            int overdueCount = 0;
            double totalFines = 0;

            while (rs.next()) {
                totalRecords++;
                String status = rs.getString("status");
                if ("ISSUED".equals(status)) issuedCount++;
                if ("OVERDUE".equals(status) || "LATE".equals(status)) overdueCount++;
                totalFines += rs.getDouble("fine_amount");

                Object[] row = {
                    rs.getInt("id"),
                    rs.getInt("book_id"),
                    rs.getString("title"),
                    rs.getInt("member_id"),
                    rs.getString("full_name"),
                    rs.getDate("issue_date"),
                    rs.getDate("return_date"),
                    rs.getDate("actual_return_date"),
                    status,
                    String.format("$%.2f", rs.getDouble("fine_amount")),
                    rs.getBoolean("fine_paid"),
                    rs.getInt("renewed_count")
                };
                model.addRow(row);
            }

            // Update status label
            lblStatus.setText(String.format(
                "📊 Total: %d | Issued: %d | Overdue: %d | Total Fines: $%.2f",
                totalRecords, issuedCount, overdueCount, totalFines
            ));

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, 
                "Error loading circulation: " + ex.getMessage(), 
                "Database Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
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

    // ---------------------------------------
    //            ISSUE BOOK
    // ---------------------------------------
    private void issueBook() {
        String bookID = txtBookId.getText().trim();
        String memberID = txtMemberId.getText().trim();

        if (bookID.isEmpty() || memberID.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "❌ Please enter both Book ID and Member ID!", 
                "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            int bookId = Integer.parseInt(bookID);
            int memberId = Integer.parseInt(memberID);
            
            // Validate book exists
            if (!bookExists(bookId)) {
                JOptionPane.showMessageDialog(this, 
                    "❌ Book not found!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Validate member exists
            if (!memberExists(memberId)) {
                JOptionPane.showMessageDialog(this, 
                    "❌ Member not found!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Check if book is available
            if (!isBookAvailable(bookId)) {
                JOptionPane.showMessageDialog(this, 
                    "❌ Book is currently not available!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Check if member has any unpaid fines
            if (memberHasUnpaidFines(memberId)) {
                int choice = JOptionPane.showConfirmDialog(this,
                    "⚠️ This member has unpaid fines! Do you want to proceed anyway?",
                    "Unpaid Fines Warning", JOptionPane.YES_NO_OPTION);
                if (choice != JOptionPane.YES_OPTION) return;
            }

            LocalDate issueDate = LocalDate.now();
            LocalDate returnDate = issueDate.plusDays(14); // Standard 2-week loan period

            Connection conn = null;
            PreparedStatement pst = null;

            try {
                conn = DBConnection.getConnection();
                
                // Start transaction
                conn.setAutoCommit(false);
                
                // Insert circulation record
                String sql = "INSERT INTO circulation " +
                            "(book_id, member_id, issue_date, return_date, status) " +
                            "VALUES (?, ?, ?, ?, 'ISSUED')";
                pst = conn.prepareStatement(sql);
                pst.setInt(1, bookId);
                pst.setInt(2, memberId);
                pst.setDate(3, Date.valueOf(issueDate));
                pst.setDate(4, Date.valueOf(returnDate));
                
                int rows = pst.executeUpdate();
                pst.close();
                
                if (rows > 0) {
                    // Update book's available quantity
                    String updateBookSql = "UPDATE books SET available_quantity = available_quantity - 1 " +
                                          "WHERE book_id = ? AND available_quantity > 0";
                    pst = conn.prepareStatement(updateBookSql);
                    pst.setInt(1, bookId);
                    pst.executeUpdate();
                    
                    // Commit transaction
                    conn.commit();
                    
                    UserSession.logActivity("BOOK_ISSUE", 
                        "Issued book ID " + bookId + " to member ID " + memberId);
                    
                    JOptionPane.showMessageDialog(this, 
                        "✅ Book issued successfully!\nReturn Date: " + returnDate, 
                        "Success", JOptionPane.INFORMATION_MESSAGE);
                    
                    txtBookId.setText("");
                    txtMemberId.setText("");
                    loadCirculation();
                }
                
            } catch (SQLException ex) {
                if (conn != null) {
                    try {
                        conn.rollback();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
                JOptionPane.showMessageDialog(this, 
                    "Database error: " + ex.getMessage(), 
                    "Error", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            } finally {
                try {
                    if (pst != null) pst.close();
                    if (conn != null) {
                        conn.setAutoCommit(true);
                        conn.close();
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, 
                "❌ Book ID and Member ID must be numbers!", 
                "Validation Error", JOptionPane.WARNING_MESSAGE);
        }
    }

    // ---------------------------------------
    //            RETURN BOOK
    // ---------------------------------------
    private void returnBook() {
        int row = tblCirculation.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, 
                "❌ Please select a record to return!", 
                "Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Integer idObj = (Integer) model.getValueAt(row, 0);
        if (idObj == null) {
            JOptionPane.showMessageDialog(this, 
                "❌ Invalid record!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        int circulationId = idObj;
        int bookId = (Integer) model.getValueAt(row, 1);
        
        // Calculate fine if overdue
        Date returnDate = (Date) model.getValueAt(row, 6);
        LocalDate actualReturnDate = LocalDate.now();
        long daysOverdue = ChronoUnit.DAYS.between(
            returnDate.toLocalDate(), actualReturnDate);
        
        double fineAmount = 0.0;
        if (daysOverdue > 0) {
            fineAmount = daysOverdue * 5.0; // $5 per day fine
            int confirm = JOptionPane.showConfirmDialog(this,
                String.format("⚠️ Book is %d days overdue!\nFine amount: $%.2f\nProceed with return?", 
                            daysOverdue, fineAmount),
                "Overdue Book", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (confirm != JOptionPane.YES_OPTION) return;
        }

        Connection conn = null;
        PreparedStatement pst = null;

        try {
            conn = DBConnection.getConnection();
            
            // Start transaction
            conn.setAutoCommit(false);
            
            // Update circulation record
            String sql = "UPDATE circulation SET " +
                        "actual_return_date = ?, " +
                        "status = 'RETURNED', " +
                        "fine_amount = ?, " +
                        "updated_at = CURRENT_TIMESTAMP " +
                        "WHERE id = ?";
            pst = conn.prepareStatement(sql);
            pst.setDate(1, Date.valueOf(actualReturnDate));
            pst.setDouble(2, fineAmount);
            pst.setInt(3, circulationId);
            
            int rows = pst.executeUpdate();
            pst.close();
            
            if (rows > 0) {
                // Update book's available quantity
                String updateBookSql = "UPDATE books SET available_quantity = available_quantity + 1 " +
                                     "WHERE book_id = ?";
                pst = conn.prepareStatement(updateBookSql);
                pst.setInt(1, bookId);
                pst.executeUpdate();
                
                // Commit transaction
                conn.commit();
                
                UserSession.logActivity("BOOK_RETURN", 
                    "Returned book ID " + bookId + " from circulation ID " + circulationId);
                
                JOptionPane.showMessageDialog(this, 
                    "✅ Book returned successfully!" + 
                    (fineAmount > 0 ? String.format("\nFine amount: $%.2f", fineAmount) : ""), 
                    "Success", JOptionPane.INFORMATION_MESSAGE);
                
                loadCirculation();
            }
            
        } catch (SQLException ex) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            JOptionPane.showMessageDialog(this, 
                "Database error: " + ex.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        } finally {
            try {
                if (pst != null) pst.close();
                if (conn != null) {
                    conn.setAutoCommit(true);
                    conn.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    // ---------------------------------------
    //            RENEW BOOK
    // ---------------------------------------
    private void renewBook() {
        int row = tblCirculation.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, 
                "❌ Please select a record to renew!", 
                "Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Integer idObj = (Integer) model.getValueAt(row, 0);
        if (idObj == null) {
            JOptionPane.showMessageDialog(this, 
                "❌ Invalid record!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        int circulationId = idObj;
        int renewedCount = (Integer) model.getValueAt(row, 11);
        
        if (renewedCount >= 2) { // Maximum 2 renewals
            JOptionPane.showMessageDialog(this, 
                "❌ Maximum renewals (2) reached!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        LocalDate currentReturnDate = ((Date) model.getValueAt(row, 6)).toLocalDate();
        LocalDate newReturnDate = currentReturnDate.plusDays(7); // Add 7 days

        Connection conn = null;
        PreparedStatement pst = null;

        try {
            conn = DBConnection.getConnection();
            
            String sql = "UPDATE circulation SET " +
                        "return_date = ?, " +
                        "renewed_count = renewed_count + 1, " +
                        "updated_at = CURRENT_TIMESTAMP " +
                        "WHERE id = ? AND status = 'ISSUED'";
            
            pst = conn.prepareStatement(sql);
            pst.setDate(1, Date.valueOf(newReturnDate));
            pst.setInt(2, circulationId);

            int updated = pst.executeUpdate();

            if (updated > 0) {
                UserSession.logActivity("BOOK_RENEW", 
                    "Renewed circulation ID " + circulationId + " to " + newReturnDate);
                JOptionPane.showMessageDialog(this, 
                    "✅ Book renewed successfully!\nNew return date: " + newReturnDate, 
                    "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, 
                    "❌ Cannot renew. Book already returned or late.", 
                    "Error", JOptionPane.ERROR_MESSAGE);
            }

            loadCirculation();

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, 
                "Database error: " + ex.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        } finally {
            try {
                if (pst != null) pst.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    // ---------------------------------------
    //            SEARCH
    // ---------------------------------------
    private void search() {
        String keyword = txtSearch.getText().trim();

        Connection conn = null;
        PreparedStatement pst = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            
            String query = "SELECT c.id, c.book_id, b.title, c.member_id, m.full_name, " +
                          "c.issue_date, c.return_date, c.actual_return_date, c.status, " +
                          "c.fine_amount, c.fine_paid, c.renewed_count " +
                          "FROM circulation c " +
                          "JOIN books b ON c.book_id = b.book_id " +
                          "JOIN members m ON c.member_id = m.member_id " +
                          "WHERE b.title LIKE ? OR m.full_name LIKE ? OR c.status LIKE ? " +
                          "OR CAST(c.book_id AS CHAR) LIKE ? OR CAST(c.member_id AS CHAR) LIKE ? " +
                          "ORDER BY c.id DESC";
            
            pst = conn.prepareStatement(query);
            String like = "%" + keyword + "%";
            pst.setString(1, like);
            pst.setString(2, like);
            pst.setString(3, like);
            pst.setString(4, like);
            pst.setString(5, like);

            rs = pst.executeQuery();
            model.setRowCount(0);

            int count = 0;
            while (rs.next()) {
                count++;
                Object[] row = {
                    rs.getInt("id"),
                    rs.getInt("book_id"),
                    rs.getString("title"),
                    rs.getInt("member_id"),
                    rs.getString("full_name"),
                    rs.getDate("issue_date"),
                    rs.getDate("return_date"),
                    rs.getDate("actual_return_date"),
                    rs.getString("status"),
                    String.format("$%.2f", rs.getDouble("fine_amount")),
                    rs.getBoolean("fine_paid"),
                    rs.getInt("renewed_count")
                };
                model.addRow(row);
            }

            lblStatus.setText("🔍 Found " + count + " record(s)");

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, 
                "Search error: " + ex.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
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

    // ---------------------------------------
    //            CALCULATE FINE
    // ---------------------------------------
    private void calculateFine() {
        int row = tblCirculation.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, 
                "❌ Please select a record to calculate fine!", 
                "Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String status = model.getValueAt(row, 8).toString();
        if (!("OVERDUE".equals(status) || "LATE".equals(status))) {
            JOptionPane.showMessageDialog(this, 
                "❌ Only overdue/late books have fines!", 
                "Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Date returnDate = (Date) model.getValueAt(row, 6);
        LocalDate today = LocalDate.now();
        long daysOverdue = ChronoUnit.DAYS.between(returnDate.toLocalDate(), today);
        
        if (daysOverdue <= 0) {
            JOptionPane.showMessageDialog(this, 
                "ℹ️ Book is not overdue!", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        double fineAmount = daysOverdue * 5.0; // $5 per day
        JOptionPane.showMessageDialog(this,
            String.format("📊 Fine Calculation:\n" +
                         "Return Date: %s\n" +
                         "Today: %s\n" +
                         "Days Overdue: %d\n" +
                         "Fine Rate: $5.00 per day\n" +
                         "Total Fine: $%.2f",
                         returnDate, today, daysOverdue, fineAmount),
            "Fine Calculation", JOptionPane.INFORMATION_MESSAGE);
    }

    // ---------------------------------------
    //            PAY FINE
    // ---------------------------------------
    private void payFine() {
        int row = tblCirculation.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, 
                "❌ Please select a record to pay fine!", 
                "Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Integer idObj = (Integer) model.getValueAt(row, 0);
        if (idObj == null) {
            JOptionPane.showMessageDialog(this, 
                "❌ Invalid record!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        int circulationId = idObj;
        double fineAmount = Double.parseDouble(
            model.getValueAt(row, 9).toString().replace("$", ""));
        
        if (fineAmount <= 0) {
            JOptionPane.showMessageDialog(this, 
                "ℹ️ No fine to pay!", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        if (Boolean.TRUE.equals(model.getValueAt(row, 10))) {
            JOptionPane.showMessageDialog(this, 
                "ℹ️ Fine already paid!", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
            String.format("Pay fine of $%.2f for this record?", fineAmount),
            "Confirm Payment", JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            Connection conn = null;
            PreparedStatement pst = null;

            try {
                conn = DBConnection.getConnection();
                String sql = "UPDATE circulation SET fine_paid = TRUE, updated_at = CURRENT_TIMESTAMP " +
                            "WHERE id = ?";
                pst = conn.prepareStatement(sql);
                pst.setInt(1, circulationId);
                
                int rows = pst.executeUpdate();
                if (rows > 0) {
                    UserSession.logActivity("FINE_PAYMENT", 
                        "Paid fine for circulation ID " + circulationId + ": $" + fineAmount);
                    JOptionPane.showMessageDialog(this, 
                        "✅ Fine payment recorded successfully!", 
                        "Success", JOptionPane.INFORMATION_MESSAGE);
                    loadCirculation();
                }
                
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, 
                    "Database error: " + ex.getMessage(), 
                    "Error", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
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

    // ---------------------------------------
    //   AUTO UPDATE STATUS FOR OVERDUE BOOKS
    // ---------------------------------------
    private void updateOverdueStatus() {
        Connection conn = null;
        Statement st = null;

        try {
            conn = DBConnection.getConnection();
            st = conn.createStatement();
            
            // Update to OVERDUE if return date has passed
            String query = "UPDATE circulation SET status='OVERDUE' " +
                         "WHERE return_date < CURDATE() AND status='ISSUED'";
            st.executeUpdate(query);
            
        } catch (SQLException ex) {
            System.err.println("Error updating overdue status: " + ex.getMessage());
        } finally {
            try {
                if (st != null) st.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    // ---------------------------------------
    //   VALIDATION HELPERS
    // ---------------------------------------
    private boolean memberExists(int memberId) {
        Connection conn = null;
        PreparedStatement pst = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            String q = "SELECT member_id FROM members WHERE member_id=?";
            pst = conn.prepareStatement(q);
            pst.setInt(1, memberId);
            rs = pst.executeQuery();
            return rs.next();

        } catch (SQLException e) {
            System.err.println("Error checking member: " + e.getMessage());
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

    private boolean bookExists(int bookId) {
        Connection conn = null;
        PreparedStatement pst = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            String q = "SELECT book_id FROM books WHERE book_id=?";
            pst = conn.prepareStatement(q);
            pst.setInt(1, bookId);
            rs = pst.executeQuery();
            return rs.next();

        } catch (SQLException e) {
            System.err.println("Error checking book: " + e.getMessage());
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

    private boolean isBookAvailable(int bookId) {
        Connection conn = null;
        PreparedStatement pst = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            // Check available quantity in books table
            String q = "SELECT available_quantity FROM books WHERE book_id=?";
            pst = conn.prepareStatement(q);
            pst.setInt(1, bookId);
            rs = pst.executeQuery();
            
            if (rs.next()) {
                return rs.getInt("available_quantity") > 0;
            }
            return false;

        } catch (SQLException e) {
            System.err.println("Error checking availability: " + e.getMessage());
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

    private boolean memberHasUnpaidFines(int memberId) {
        Connection conn = null;
        PreparedStatement pst = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            String q = "SELECT SUM(fine_amount) as total_fine FROM circulation " +
                      "WHERE member_id=? AND fine_paid=FALSE AND fine_amount > 0";
            pst = conn.prepareStatement(q);
            pst.setInt(1, memberId);
            rs = pst.executeQuery();
            
            if (rs.next()) {
                double totalFine = rs.getDouble("total_fine");
                return totalFine > 0;
            }
            return false;

        } catch (SQLException e) {
            System.err.println("Error checking fines: " + e.getMessage());
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
    
    public static void main(String[] args) {
        // For testing purposes
        UserSession.setRole("LIBRARIAN");
        
        SwingUtilities.invokeLater(() -> {
            new ManageCirculation().setVisible(true);
        });
    }
}