package ui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import utils.DBConnection;

public class MemberDashboard extends JFrame {
    
    private Connection connection;
    private int memberId;
    private DefaultTableModel searchModel;
    private JTable searchTable;
    
    public MemberDashboard(int memberId) {
        this.memberId = memberId;
        initializeDatabaseConnection();
        initComponents();
        setTitle("Member Dashboard - Library Management System");
        setSize(1200, 800);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
    }
    
    private void initializeDatabaseConnection() {
        try {
            connection = DBConnection.getConnection();
            if (connection != null) {
                System.out.println("Member dashboard connected to database");
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "Database connection failed: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void initComponents() {
        // Create main panel with BorderLayout
        JPanel mainPanel = new JPanel(new BorderLayout());
        
        // Header Panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(0, 102, 204));
        headerPanel.setPreferredSize(new Dimension(getWidth(), 80));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        
        // Welcome label
        JLabel welcomeLabel = new JLabel("Welcome Member");
        welcomeLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        welcomeLabel.setForeground(Color.WHITE);
        
        try {
            String query = "SELECT full_name FROM members WHERE member_id = ?";
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setInt(1, memberId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                welcomeLabel.setText("Welcome, " + rs.getString("full_name"));
            }
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            System.out.println("Error loading member name: " + e.getMessage());
        }
        
        // Logout button
        JButton logoutBtn = new JButton("Logout");
        logoutBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        logoutBtn.setBackground(new Color(204, 0, 0));
        logoutBtn.setForeground(Color.WHITE);
        logoutBtn.addActionListener(e -> {
            this.dispose();
            new LoginForm().setVisible(true);
        });
        
        headerPanel.add(welcomeLabel, BorderLayout.WEST);
        headerPanel.add(logoutBtn, BorderLayout.EAST);
        
        // Tabbed Pane
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        
        tabbedPane.addTab("📊 Dashboard", createDashboardPanel());
        tabbedPane.addTab("👤 Profile", createProfilePanel());
        tabbedPane.addTab("📚 Borrowed Books", createBorrowedBooksPanel());
        tabbedPane.addTab("📖 History", createHistoryPanel());
        tabbedPane.addTab("💰 Fines", createFinesPanel());
        tabbedPane.addTab("🔍 Search & Borrow", createSearchAndBorrowPanel());
        tabbedPane.addTab("📋 Book Requests", createBookRequestsPanel());
        
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(tabbedPane, BorderLayout.CENTER);
        
        add(mainPanel);
    }
    
    // ========== DASHBOARD PANEL (unchanged) ==========
    private JPanel createDashboardPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JPanel statsPanel = new JPanel(new GridLayout(2, 3, 15, 15));
        
        try {
            String query = "SELECT " +
                "(SELECT COUNT(*) FROM circulation WHERE member_id = ? AND status IN ('ISSUED', 'OVERDUE')) as current_borrowed, " +
                "(SELECT COALESCE(SUM(fine_amount), 0) FROM circulation WHERE member_id = ? AND fine_paid = FALSE) as pending_fines, " +
                "(SELECT COUNT(*) FROM reservations WHERE member_id = ? AND status = 'ACTIVE') as active_reservations, " +
                "(SELECT COUNT(*) FROM circulation WHERE member_id = ? AND status = 'OVERDUE') as overdue_count, " +
                "(SELECT total_borrowed FROM members WHERE member_id = ?) as total_borrowed, " +
                "(SELECT COUNT(*) FROM book_requests WHERE member_id = ? AND status = 'PENDING') as pending_requests";
            
            PreparedStatement stmt = connection.prepareStatement(query);
            for (int i = 1; i <= 6; i++) {
                stmt.setInt(i, memberId);
            }
            
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                statsPanel.add(createStatCard("📚 Current Borrowed", rs.getString("current_borrowed"), Color.BLUE));
                statsPanel.add(createStatCard("💰 Pending Fines", "$" + rs.getDouble("pending_fines"), Color.RED));
                statsPanel.add(createStatCard("⏰ Reservations", rs.getString("active_reservations"), Color.GREEN));
                statsPanel.add(createStatCard("📅 Overdue", rs.getString("overdue_count"), Color.ORANGE));
                statsPanel.add(createStatCard("✅ Total Borrowed", rs.getString("total_borrowed"), Color.MAGENTA));
                statsPanel.add(createStatCard("📖 Requests", rs.getString("pending_requests"), Color.CYAN));
            }
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            statsPanel.add(new JLabel("Error loading statistics"));
        }
        
        panel.add(statsPanel, BorderLayout.CENTER);
        return panel;
    }
    
    private JPanel createStatCard(String title, String value, Color color) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBorder(BorderFactory.createLineBorder(color, 2));
        card.setBackground(Color.WHITE);
        card.setPreferredSize(new Dimension(150, 100));
        
        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        
        JLabel valueLabel = new JLabel(value, SwingConstants.CENTER);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        valueLabel.setForeground(color);
        
        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        
        return card;
    }
    
    // ========== SEARCH AND BORROW PANEL (NEW WITH ALL FUNCTIONALITY) ==========
    private JPanel createSearchAndBorrowPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Top Panel: Search Controls
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        searchPanel.setBorder(BorderFactory.createTitledBorder("Search Books"));
        
        JTextField searchField = new JTextField(25);
        JComboBox<String> searchType = new JComboBox<>(new String[]{"Title", "Author", "ISBN", "All Books"});
        JButton searchBtn = new JButton("Search");
        JButton viewAllBtn = new JButton("View All Available");
        
        searchPanel.add(new JLabel("Search by:"));
        searchPanel.add(searchType);
        searchPanel.add(new JLabel("Keyword:"));
        searchPanel.add(searchField);
        searchPanel.add(searchBtn);
        searchPanel.add(viewAllBtn);
        
        // Book Details Panel
        JPanel detailsPanel = new JPanel(new GridBagLayout());
        detailsPanel.setBorder(BorderFactory.createTitledBorder("Book Details"));
        detailsPanel.setPreferredSize(new Dimension(300, 400));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        JLabel bookIdLabel = new JLabel("Book ID:");
        JLabel titleLabel = new JLabel("Title:");
        JLabel authorLabel = new JLabel("Author:");
        JLabel isbnLabel = new JLabel("ISBN:");
        JLabel availableLabel = new JLabel("Available:");
        JLabel locationLabel = new JLabel("Location:");
        JLabel yearLabel = new JLabel("Year:");
        
        JTextField bookIdField = new JTextField(15);
        JTextField titleField = new JTextField(20);
        JTextField authorField = new JTextField(20);
        JTextField isbnField = new JTextField(15);
        JTextField availableField = new JTextField(10);
        JTextField locationField = new JTextField(15);
        JTextField yearField = new JTextField(10);
        
        bookIdField.setEditable(false);
        titleField.setEditable(false);
        authorField.setEditable(false);
        isbnField.setEditable(false);
        availableField.setEditable(false);
        locationField.setEditable(false);
        yearField.setEditable(false);
        
        // Action Buttons
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        JButton borrowBtn = new JButton("Borrow This Book");
        JButton requestBtn = new JButton("Request This Book");
        JButton viewDetailsBtn = new JButton("View Full Details");
        
        borrowBtn.setEnabled(false);
        requestBtn.setEnabled(false);
        viewDetailsBtn.setEnabled(false);
        
        actionPanel.add(borrowBtn);
        actionPanel.add(requestBtn);
        actionPanel.add(viewDetailsBtn);
        
        // Add components to details panel
        gbc.gridx = 0; gbc.gridy = 0;
        detailsPanel.add(bookIdLabel, gbc);
        gbc.gridx = 1;
        detailsPanel.add(bookIdField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 1;
        detailsPanel.add(titleLabel, gbc);
        gbc.gridx = 1;
        detailsPanel.add(titleField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 2;
        detailsPanel.add(authorLabel, gbc);
        gbc.gridx = 1;
        detailsPanel.add(authorField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 3;
        detailsPanel.add(isbnLabel, gbc);
        gbc.gridx = 1;
        detailsPanel.add(isbnField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 4;
        detailsPanel.add(availableLabel, gbc);
        gbc.gridx = 1;
        detailsPanel.add(availableField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 5;
        detailsPanel.add(locationLabel, gbc);
        gbc.gridx = 1;
        detailsPanel.add(locationField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 6;
        detailsPanel.add(yearLabel, gbc);
        gbc.gridx = 1;
        detailsPanel.add(yearField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 7; gbc.gridwidth = 2;
        detailsPanel.add(actionPanel, gbc);
        
        // Results Table
        String[] columns = {"Book ID", "Title", "Author", "Available", "Location", "ISBN"};
        searchModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        searchTable = new JTable(searchModel);
        searchTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        searchTable.setRowHeight(25);
        JScrollPane scrollPane = new JScrollPane(searchTable);
        
        // Table selection listener
        searchTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && searchTable.getSelectedRow() != -1) {
                int selectedRow = searchTable.getSelectedRow();
                bookIdField.setText(searchModel.getValueAt(selectedRow, 0).toString());
                titleField.setText(searchModel.getValueAt(selectedRow, 1).toString());
                authorField.setText(searchModel.getValueAt(selectedRow, 2).toString());
                availableField.setText(searchModel.getValueAt(selectedRow, 3).toString());
                locationField.setText(searchModel.getValueAt(selectedRow, 4).toString());
                isbnField.setText(searchModel.getValueAt(selectedRow, 5).toString());
                
                // Enable action buttons
                boolean isAvailable = Integer.parseInt(availableField.getText()) > 0;
                borrowBtn.setEnabled(isAvailable);
                requestBtn.setEnabled(true);
                viewDetailsBtn.setEnabled(true);
                
                // Load additional details
                loadBookDetails(Integer.parseInt(bookIdField.getText()), yearField);
            }
        });
        
        // Button Actions
        searchBtn.addActionListener(e -> performSearch(searchField.getText(), (String) searchType.getSelectedItem()));
        viewAllBtn.addActionListener(e -> loadAllAvailableBooks());
        
        borrowBtn.addActionListener(e -> {
            int bookId = Integer.parseInt(bookIdField.getText());
            borrowBook(bookId);
        });
        
        requestBtn.addActionListener(e -> {
            String bookTitle = titleField.getText();
            String author = authorField.getText();
            requestBook(bookTitle, author);
        });
        
        viewDetailsBtn.addActionListener(e -> {
            int bookId = Integer.parseInt(bookIdField.getText());
            showBookDetails(bookId);
        });
        
        // Layout
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.add(scrollPane, BorderLayout.CENTER);
        
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.add(detailsPanel, BorderLayout.NORTH);
        
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, rightPanel);
        splitPane.setDividerLocation(600);
        
        panel.add(searchPanel, BorderLayout.NORTH);
        panel.add(splitPane, BorderLayout.CENTER);
        
        // Load initial data
        loadAllAvailableBooks();
        
        return panel;
    }
    
    private void loadBookDetails(int bookId, JTextField yearField) {
        try {
            String query = "SELECT publication_year FROM books WHERE book_id = ?";
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setInt(1, bookId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                yearField.setText(rs.getString("publication_year"));
            }
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            yearField.setText("N/A");
        }
    }
    
    private void performSearch(String searchText, String type) {
        searchModel.setRowCount(0);
        
        try {
            String query = "";
            PreparedStatement stmt = null;
            
            switch (type) {
                case "Title":
                    query = "SELECT b.book_id, b.title, b.author, b.available_quantity, b.location, b.isbn " +
                           "FROM books b WHERE b.title LIKE ? AND b.available_quantity > 0 ORDER BY b.title";
                    stmt = connection.prepareStatement(query);
                    stmt.setString(1, "%" + searchText + "%");
                    break;
                    
                case "Author":
                    query = "SELECT b.book_id, b.title, b.author, b.available_quantity, b.location, b.isbn " +
                           "FROM books b WHERE b.author LIKE ? AND b.available_quantity > 0 ORDER BY b.title";
                    stmt = connection.prepareStatement(query);
                    stmt.setString(1, "%" + searchText + "%");
                    break;
                    
                case "ISBN":
                    query = "SELECT b.book_id, b.title, b.author, b.available_quantity, b.location, b.isbn " +
                           "FROM books b WHERE b.isbn = ? AND b.available_quantity > 0";
                    stmt = connection.prepareStatement(query);
                    stmt.setString(1, searchText);
                    break;
                    
                case "All Books":
                    query = "SELECT b.book_id, b.title, b.author, b.available_quantity, b.location, b.isbn " +
                           "FROM books b WHERE b.available_quantity > 0 ORDER BY b.title LIMIT 50";
                    stmt = connection.prepareStatement(query);
                    break;
            }
            
            if (stmt != null) {
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    searchModel.addRow(new Object[]{
                        rs.getInt("book_id"),
                        rs.getString("title"),
                        rs.getString("author"),
                        rs.getInt("available_quantity"),
                        rs.getString("location"),
                        rs.getString("isbn")
                    });
                }
                rs.close();
                stmt.close();
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error searching books: " + e.getMessage());
        }
    }
    
    private void loadAllAvailableBooks() {
        performSearch("", "All Books");
    }
    
    private void borrowBook(int bookId) {
        try {
            // Check if member can borrow more books
            String checkQuery = "SELECT COUNT(*) as current_count FROM circulation " +
                               "WHERE member_id = ? AND status IN ('ISSUED', 'OVERDUE')";
            PreparedStatement checkStmt = connection.prepareStatement(checkQuery);
            checkStmt.setInt(1, memberId);
            ResultSet rs = checkStmt.executeQuery();
            
            if (rs.next()) {
                int currentCount = rs.getInt("current_count");
                // Get max books per member from settings
                String maxQuery = "SELECT setting_value FROM settings WHERE setting_key = 'max_books_per_member'";
                Statement maxStmt = connection.createStatement();
                ResultSet maxRs = maxStmt.executeQuery(maxQuery);
                int maxBooks = 5; // default
                if (maxRs.next()) {
                    maxBooks = Integer.parseInt(maxRs.getString("setting_value"));
                }
                maxRs.close();
                maxStmt.close();
                
                if (currentCount >= maxBooks) {
                    JOptionPane.showMessageDialog(this, 
                        "You cannot borrow more than " + maxBooks + " books at a time.\n" +
                        "You currently have " + currentCount + " books borrowed.",
                        "Borrow Limit Reached", JOptionPane.WARNING_MESSAGE);
                    return;
                }
            }
            rs.close();
            checkStmt.close();
            
            // Get borrowing days from settings
            String daysQuery = "SELECT setting_value FROM settings WHERE setting_key = 'max_borrow_days'";
            Statement daysStmt = connection.createStatement();
            ResultSet daysRs = daysStmt.executeQuery(daysQuery);
            int borrowDays = 14; // default
            if (daysRs.next()) {
                borrowDays = Integer.parseInt(daysRs.getString("setting_value"));
            }
            daysRs.close();
            daysStmt.close();
            
            // Call the issue_book procedure
            String callProcedure = "{CALL issue_book(?, ?, ?)}";
            CallableStatement cstmt = connection.prepareCall(callProcedure);
            cstmt.setInt(1, bookId);
            cstmt.setInt(2, memberId);
            cstmt.setInt(3, borrowDays);
            cstmt.execute();
            
            JOptionPane.showMessageDialog(this, 
                "Book borrowed successfully!\nDue date: " + 
                java.time.LocalDate.now().plusDays(borrowDays),
                "Success", JOptionPane.INFORMATION_MESSAGE);
            
            // Refresh the search table
            loadAllAvailableBooks();
            
        } catch (SQLException e) {
            if (e.getMessage().contains("Book is not available")) {
                JOptionPane.showMessageDialog(this, 
                    "This book is currently not available for borrowing.",
                    "Book Unavailable", JOptionPane.WARNING_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, 
                    "Error borrowing book: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void requestBook(String bookTitle, String author) {
        String notes = JOptionPane.showInputDialog(this, 
            "Enter any additional notes for your request:", 
            "Request Book", JOptionPane.QUESTION_MESSAGE);
        
        if (notes == null) return; // User cancelled
        
        try {
            // Get next request ID
            String maxQuery = "SELECT COALESCE(MAX(request_id), 0) + 1 as next_id FROM book_requests";
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(maxQuery);
            int nextId = 1;
            if (rs.next()) {
                nextId = rs.getInt("next_id");
            }
            rs.close();
            stmt.close();
            
            // Insert request
            String insertQuery = "INSERT INTO book_requests (request_id, member_id, book_title, author, request_date, status, notes) " +
                               "VALUES (?, ?, ?, ?, CURDATE(), 'PENDING', ?)";
            PreparedStatement pstmt = connection.prepareStatement(insertQuery);
            pstmt.setInt(1, nextId);
            pstmt.setInt(2, memberId);
            pstmt.setString(3, bookTitle);
            pstmt.setString(4, author);
            pstmt.setString(5, notes);
            pstmt.executeUpdate();
            pstmt.close();
            
            JOptionPane.showMessageDialog(this, 
                "Book request submitted successfully!\nOur librarians will process your request.",
                "Request Submitted", JOptionPane.INFORMATION_MESSAGE);
                
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, 
                "Error submitting request: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void showBookDetails(int bookId) {
        try {
            String query = "SELECT * FROM books WHERE book_id = ?";
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setInt(1, bookId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                StringBuilder details = new StringBuilder();
                details.append("Book Details:\n");
                details.append("=============\n");
                details.append("Title: ").append(rs.getString("title")).append("\n");
                details.append("Author: ").append(rs.getString("author")).append("\n");
                details.append("ISBN: ").append(rs.getString("isbn")).append("\n");
                details.append("Publisher: ").append(rs.getString("publisher")).append("\n");
                details.append("Publication Year: ").append(rs.getInt("publication_year")).append("\n");
                details.append("Genre: ").append(rs.getString("genre")).append("\n");
                details.append("Location: ").append(rs.getString("location")).append("\n");
                details.append("Available: ").append(rs.getInt("available_quantity")).append(" of ").append(rs.getInt("quantity")).append("\n");
                details.append("Price: $").append(rs.getDouble("price")).append("\n");
                
                String description = rs.getString("description");
                if (description != null && !description.trim().isEmpty()) {
                    details.append("\nDescription:\n").append(description);
                }
                
                JTextArea textArea = new JTextArea(details.toString());
                textArea.setEditable(false);
                textArea.setLineWrap(true);
                textArea.setWrapStyleWord(true);
                JScrollPane scrollPane = new JScrollPane(textArea);
                scrollPane.setPreferredSize(new Dimension(400, 300));
                
                JOptionPane.showMessageDialog(this, scrollPane, 
                    "Book Details", JOptionPane.INFORMATION_MESSAGE);
            }
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, 
                "Error loading book details: " + e.getMessage());
        }
    }
    
    // ========== BOOK REQUESTS PANEL (NEW) ==========
    private JPanel createBookRequestsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Table for book requests
        String[] columns = {"Request ID", "Book Title", "Author", "Request Date", "Status", "Notes", "Fulfilled Date"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        JTable table = new JTable(model);
        table.setRowHeight(25);
        JScrollPane scrollPane = new JScrollPane(table);
        
        // Button Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        JButton refreshBtn = new JButton("Refresh");
        JButton cancelBtn = new JButton("Cancel Selected Request");
        JButton newRequestBtn = new JButton("Make New Request");
        
        refreshBtn.addActionListener(e -> refreshBookRequests(model));
        cancelBtn.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow != -1) {
                int requestId = (int) table.getValueAt(selectedRow, 0);
                cancelBookRequest(requestId, model);
            } else {
                JOptionPane.showMessageDialog(this, "Please select a request to cancel!");
            }
        });
        
        newRequestBtn.addActionListener(e -> {
            // Open dialog for new request
            String bookTitle = JOptionPane.showInputDialog(this, 
                "Enter book title:", "New Book Request", JOptionPane.QUESTION_MESSAGE);
            if (bookTitle != null && !bookTitle.trim().isEmpty()) {
                String author = JOptionPane.showInputDialog(this, 
                    "Enter author (optional):", "New Book Request", JOptionPane.QUESTION_MESSAGE);
                requestBook(bookTitle, author != null ? author : "");
                refreshBookRequests(model);
            }
        });
        
        buttonPanel.add(refreshBtn);
        buttonPanel.add(cancelBtn);
        buttonPanel.add(newRequestBtn);
        
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        // Load initial data
        refreshBookRequests(model);
        
        return panel;
    }
    
    private void refreshBookRequests(DefaultTableModel model) {
        model.setRowCount(0);
        
        try {
            String query = "SELECT request_id, book_title, author, request_date, status, notes, fulfilled_date " +
                          "FROM book_requests WHERE member_id = ? ORDER BY request_date DESC";
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setInt(1, memberId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt("request_id"),
                    rs.getString("book_title"),
                    rs.getString("author"),
                    rs.getDate("request_date"),
                    rs.getString("status"),
                    rs.getString("notes"),
                    rs.getDate("fulfilled_date")
                });
            }
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading book requests: " + e.getMessage());
        }
    }
    
    private void cancelBookRequest(int requestId, DefaultTableModel model) {
        int confirm = JOptionPane.showConfirmDialog(this, 
            "Are you sure you want to cancel this request?", 
            "Confirm Cancellation", JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                String query = "UPDATE book_requests SET status = 'CANCELLED' WHERE request_id = ? AND member_id = ?";
                PreparedStatement stmt = connection.prepareStatement(query);
                stmt.setInt(1, requestId);
                stmt.setInt(2, memberId);
                int updated = stmt.executeUpdate();
                stmt.close();
                
                if (updated > 0) {
                    JOptionPane.showMessageDialog(this, "Request cancelled successfully!");
                    refreshBookRequests(model);
                } else {
                    JOptionPane.showMessageDialog(this, "Request not found or already cancelled!");
                }
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Error cancelling request: " + e.getMessage());
            }
        }
    }
    
    // ========== BORROWED BOOKS PANEL (MODIFIED WITH RETURN FUNCTIONALITY) ==========
    private JPanel createBorrowedBooksPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        String[] columns = {"ID", "Book Title", "Author", "Issue Date", "Due Date", 
                           "Days Left", "Status", "Fine", "Fine Paid"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        JTable table = new JTable(model);
        table.setRowHeight(25);
        
        // Load data
        loadBorrowedBooks(model);
        
        JScrollPane scrollPane = new JScrollPane(table);
        
        // Button Panel with Return functionality
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        JButton renewBtn = new JButton("Renew Selected");
        JButton returnBtn = new JButton("Return Selected");
        JButton refreshBtn = new JButton("Refresh");
        
        renewBtn.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow != -1) {
                int circulationId = (int) table.getValueAt(selectedRow, 0);
                renewBook(circulationId);
                loadBorrowedBooks(model);
            } else {
                JOptionPane.showMessageDialog(this, "Please select a book to renew!");
            }
        });
        
        returnBtn.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow != -1) {
                int circulationId = (int) table.getValueAt(selectedRow, 0);
                String status = table.getValueAt(selectedRow, 6).toString();
                
                if ("RETURNED".equals(status)) {
                    JOptionPane.showMessageDialog(this, "This book has already been returned!");
                    return;
                }
                
                int confirm = JOptionPane.showConfirmDialog(this, 
                    "Are you sure you want to return this book?\n" +
                    "Any overdue fines will be calculated.", 
                    "Confirm Return", JOptionPane.YES_NO_OPTION);
                
                if (confirm == JOptionPane.YES_OPTION) {
                    returnBook(circulationId);
                    loadBorrowedBooks(model);
                }
            } else {
                JOptionPane.showMessageDialog(this, "Please select a book to return!");
            }
        });
        
        refreshBtn.addActionListener(e -> loadBorrowedBooks(model));
        
        buttonPanel.add(renewBtn);
        buttonPanel.add(returnBtn);
        buttonPanel.add(refreshBtn);
        
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private void loadBorrowedBooks(DefaultTableModel model) {
        model.setRowCount(0);
        
        try {
            String query = "SELECT c.id, b.title, b.author, c.issue_date, " +
                          "c.return_date, DATEDIFF(c.return_date, CURDATE()) as days_left, " +
                          "c.status, c.fine_amount, c.fine_paid " +
                          "FROM circulation c " +
                          "JOIN books b ON c.book_id = b.book_id " +
                          "WHERE c.member_id = ? AND c.status IN ('ISSUED', 'OVERDUE', 'LATE') " +
                          "ORDER BY c.return_date";
            
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setInt(1, memberId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                String daysLeft = rs.getInt("days_left") >= 0 ? 
                    String.valueOf(rs.getInt("days_left")) : "OVERDUE";
                
                model.addRow(new Object[]{
                    rs.getInt("id"),
                    rs.getString("title"),
                    rs.getString("author"),
                    rs.getDate("issue_date"),
                    rs.getDate("return_date"),
                    daysLeft,
                    rs.getString("status"),
                    "$" + rs.getDouble("fine_amount"),
                    rs.getBoolean("fine_paid") ? "Yes" : "No"
                });
            }
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading borrowed books: " + e.getMessage());
        }
    }
    
    private void returnBook(int circulationId) {
        try {
            // Call the return_book procedure
            String callProcedure = "{CALL return_book(?)}";
            CallableStatement cstmt = connection.prepareCall(callProcedure);
            cstmt.setInt(1, circulationId);
            cstmt.execute();
            
            JOptionPane.showMessageDialog(this, 
                "Book returned successfully!\n" +
                "Thank you for returning the book on time.",
                "Book Returned", JOptionPane.INFORMATION_MESSAGE);
                
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, 
                "Error returning book: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    // ========== REMAINING PANELS (UNCHANGED) ==========
    private JPanel createProfilePanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        try {
            String query = "SELECT m.member_code, m.full_name, m.email, m.phone, " +
                          "m.join_date, m.status, m.total_borrowed, u.username, u.last_login " +
                          "FROM members m LEFT JOIN users u ON m.user_id = u.user_id " +
                          "WHERE m.member_id = ?";
            
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setInt(1, memberId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                String[][] data = {
                    {"Member Code:", rs.getString("member_code")},
                    {"Full Name:", rs.getString("full_name")},
                    {"Email:", rs.getString("email")},
                    {"Phone:", rs.getString("phone")},
                    {"Join Date:", rs.getDate("join_date").toString()},
                    {"Status:", rs.getString("status")},
                    {"Total Borrowed:", rs.getString("total_borrowed")},
                    {"Username:", rs.getString("username")},
                    {"Last Login:", rs.getTimestamp("last_login") != null ? 
                        rs.getTimestamp("last_login").toString() : "Never"}
                };
                
                for (int i = 0; i < data.length; i++) {
                    gbc.gridx = 0; gbc.gridy = i;
                    panel.add(new JLabel(data[i][0]), gbc);
                    
                    gbc.gridx = 1;
                    JLabel valueLabel = new JLabel(data[i][1] != null ? data[i][1] : "N/A");
                    valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
                    panel.add(valueLabel, gbc);
                }
            }
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            panel.add(new JLabel("Error loading profile: " + e.getMessage()));
        }
        
        return panel;
    }
    
    private void renewBook(int circulationId) {
        try {
            String callProcedure = "CALL renew_book(?, ?)";
            CallableStatement cstmt = connection.prepareCall(callProcedure);
            cstmt.setInt(1, circulationId);
            cstmt.setInt(2, 7); // Renew for 7 days
            cstmt.execute();
            JOptionPane.showMessageDialog(this, "Book renewed successfully!");
        } catch (SQLException e) {
            if (e.getMessage().contains("Maximum renewals")) {
                JOptionPane.showMessageDialog(this, "Maximum renewals reached!");
            } else {
                JOptionPane.showMessageDialog(this, "Error renewing book: " + e.getMessage());
            }
        }
    }
    
    private JPanel createHistoryPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        String[] columns = {"ID", "Book Title", "Author", "Issued", "Due", 
                           "Returned", "Status", "Fine"};
        DefaultTableModel model = new DefaultTableModel(columns, 0);
        
        JTable table = new JTable(model);
        table.setRowHeight(25);
        
        try {
            String query = "SELECT c.id, b.title, b.author, c.issue_date, " +
                          "c.return_date, c.actual_return_date, c.status, c.fine_amount " +
                          "FROM circulation c " +
                          "JOIN books b ON c.book_id = b.book_id " +
                          "WHERE c.member_id = ? " +
                          "ORDER BY c.issue_date DESC LIMIT 50";
            
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setInt(1, memberId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt("id"),
                    rs.getString("title"),
                    rs.getString("author"),
                    rs.getDate("issue_date"),
                    rs.getDate("return_date"),
                    rs.getDate("actual_return_date"),
                    rs.getString("status"),
                    "$" + rs.getDouble("fine_amount")
                });
            }
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            System.out.println("Error loading history: " + e.getMessage());
        }
        
        JScrollPane scrollPane = new JScrollPane(table);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createFinesPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JPanel summaryPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        summaryPanel.setBorder(BorderFactory.createTitledBorder("Fine Summary"));
        
        try {
            String query = "SELECT " +
                "COALESCE(SUM(fine_amount), 0) as total_fines, " +
                "COALESCE(SUM(CASE WHEN fine_paid = TRUE THEN fine_amount ELSE 0 END), 0) as paid_fines, " +
                "COALESCE(SUM(CASE WHEN fine_paid = FALSE THEN fine_amount ELSE 0 END), 0) as pending_fines, " +
                "COUNT(CASE WHEN fine_paid = FALSE AND fine_amount > 0 THEN 1 END) as pending_count " +
                "FROM circulation WHERE member_id = ?";
            
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setInt(1, memberId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                summaryPanel.add(new JLabel("Total Fines: $" + rs.getDouble("total_fines"), SwingConstants.CENTER));
                summaryPanel.add(new JLabel("Paid Fines: $" + rs.getDouble("paid_fines"), SwingConstants.CENTER));
                summaryPanel.add(new JLabel("Pending Fines: $" + rs.getDouble("pending_fines"), SwingConstants.CENTER));
                summaryPanel.add(new JLabel("Pending Books: " + rs.getInt("pending_count"), SwingConstants.CENTER));
            }
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            summaryPanel.add(new JLabel("Error loading fines"));
        }
        
        String[] columns = {"ID", "Book Title", "Due Date", "Days Late", "Amount", "Status"};
        DefaultTableModel model = new DefaultTableModel(columns, 0);
        JTable table = new JTable(model);
        
        try {
            String query = "SELECT c.id, b.title, c.return_date, " +
                          "DATEDIFF(COALESCE(c.actual_return_date, CURDATE()), c.return_date) as days_late, " +
                          "c.fine_amount, c.status " +
                          "FROM circulation c " +
                          "JOIN books b ON c.book_id = b.book_id " +
                          "WHERE c.member_id = ? AND c.fine_amount > 0 " +
                          "ORDER BY c.fine_amount DESC";
            
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setInt(1, memberId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt("id"),
                    rs.getString("title"),
                    rs.getDate("return_date"),
                    rs.getInt("days_late"),
                    "$" + rs.getDouble("fine_amount"),
                    rs.getString("status")
                });
            }
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            System.out.println("Error loading fine details: " + e.getMessage());
        }
        
        JScrollPane scrollPane = new JScrollPane(table);
        
        panel.add(summaryPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new MemberDashboard(506); // Test with member ID 506
        });
    }
}

// You'll also need this LoginForm class for the logout functionality

    