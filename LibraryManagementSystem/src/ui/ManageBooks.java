package ui;

import utils.DBConnection;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ManageBooks extends JFrame {
    private JTextField txtBookId, txtAuthorId, txtGenreId, txtISBN, txtTitle, txtPublisher, txtYear, txtQuantity, txtPrice, txtLocation;
    private JButton btnAdd, btnUpdate, btnDelete, btnClear, btnSearch, btnGenerateBookId, btnGenerateAuthorId, btnGenerateGenreId;
    private JTable bookTable;
    private DefaultTableModel tableModel;
    
    public ManageBooks() {
        setTitle("Manage Books");
        setSize(1300, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        
        // Main panel
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Left panel - Form
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("ADD/EDIT BOOKS"));
        formPanel.setPreferredSize(new Dimension(500, 0));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // Book ID with Generate button
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Book ID*:"), gbc);
        
        JPanel bookIdPanel = new JPanel(new BorderLayout(5, 0));
        txtBookId = new JTextField(10);
        bookIdPanel.add(txtBookId, BorderLayout.CENTER);
        btnGenerateBookId = new JButton("Generate");
        btnGenerateBookId.setBackground(new Color(100, 149, 237));
        btnGenerateBookId.setForeground(Color.WHITE);
        bookIdPanel.add(btnGenerateBookId, BorderLayout.EAST);
        gbc.gridx = 1;
        formPanel.add(bookIdPanel, gbc);
        
        // Author ID with Generate button
        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("Author ID*:"), gbc);
        
        JPanel authorIdPanel = new JPanel(new BorderLayout(5, 0));
        txtAuthorId = new JTextField(10);
        authorIdPanel.add(txtAuthorId, BorderLayout.CENTER);
        btnGenerateAuthorId = new JButton("Generate");
        btnGenerateAuthorId.setBackground(new Color(100, 149, 237));
        btnGenerateAuthorId.setForeground(Color.WHITE);
        authorIdPanel.add(btnGenerateAuthorId, BorderLayout.EAST);
        gbc.gridx = 1;
        formPanel.add(authorIdPanel, gbc);
        
        // Genre ID with Generate button
        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(new JLabel("Genre ID*:"), gbc);
        
        JPanel genreIdPanel = new JPanel(new BorderLayout(5, 0));
        txtGenreId = new JTextField(10);
        genreIdPanel.add(txtGenreId, BorderLayout.CENTER);
        btnGenerateGenreId = new JButton("Generate");
        btnGenerateGenreId.setBackground(new Color(100, 149, 237));
        btnGenerateGenreId.setForeground(Color.WHITE);
        genreIdPanel.add(btnGenerateGenreId, BorderLayout.EAST);
        gbc.gridx = 1;
        formPanel.add(genreIdPanel, gbc);
        
        // ISBN
        gbc.gridx = 0; gbc.gridy = 3;
        formPanel.add(new JLabel("ISBN*:"), gbc);
        gbc.gridx = 1;
        txtISBN = new JTextField(20);
        formPanel.add(txtISBN, gbc);
        
        // Title
        gbc.gridx = 0; gbc.gridy = 4;
        formPanel.add(new JLabel("Title*:"), gbc);
        gbc.gridx = 1;
        txtTitle = new JTextField(20);
        formPanel.add(txtTitle, gbc);
        
        // Publisher
        gbc.gridx = 0; gbc.gridy = 5;
        formPanel.add(new JLabel("Publisher:"), gbc);
        gbc.gridx = 1;
        txtPublisher = new JTextField(20);
        formPanel.add(txtPublisher, gbc);
        
        // Year
        gbc.gridx = 0; gbc.gridy = 6;
        formPanel.add(new JLabel("Year:"), gbc);
        gbc.gridx = 1;
        txtYear = new JTextField(20);
        formPanel.add(txtYear, gbc);
        
        // Quantity
        gbc.gridx = 0; gbc.gridy = 7;
        formPanel.add(new JLabel("Quantity*:"), gbc);
        gbc.gridx = 1;
        txtQuantity = new JTextField(20);
        txtQuantity.setText("1");
        formPanel.add(txtQuantity, gbc);
        
        // Price
        gbc.gridx = 0; gbc.gridy = 8;
        formPanel.add(new JLabel("Price ($):"), gbc);
        gbc.gridx = 1;
        txtPrice = new JTextField(20);
        txtPrice.setText("0.00");
        formPanel.add(txtPrice, gbc);
        
        // Location
        gbc.gridx = 0; gbc.gridy = 9;
        formPanel.add(new JLabel("Location:"), gbc);
        gbc.gridx = 1;
        txtLocation = new JTextField(20);
        formPanel.add(txtLocation, gbc);
        
        // Buttons panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        btnAdd = new JButton("Add Book");
        btnUpdate = new JButton("Update Book");
        btnDelete = new JButton("Delete Book");
        btnClear = new JButton("Clear");
        btnSearch = new JButton("Search");
        
        // Set button colors
        btnAdd.setBackground(new Color(0, 153, 51));
        btnAdd.setForeground(Color.WHITE);
        btnUpdate.setBackground(new Color(51, 102, 204));
        btnUpdate.setForeground(Color.WHITE);
        btnDelete.setBackground(new Color(204, 0, 0));
        btnDelete.setForeground(Color.WHITE);
        btnClear.setBackground(new Color(153, 153, 153));
        btnClear.setForeground(Color.WHITE);
        btnSearch.setBackground(new Color(255, 153, 0));
        btnSearch.setForeground(Color.WHITE);
        
        buttonPanel.add(btnAdd);
        buttonPanel.add(btnUpdate);
        buttonPanel.add(btnDelete);
        buttonPanel.add(btnClear);
        buttonPanel.add(btnSearch);
        
        gbc.gridx = 0; gbc.gridy = 10; gbc.gridwidth = 2;
        formPanel.add(buttonPanel, gbc);
        
        // Right panel - Table
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBorder(BorderFactory.createTitledBorder("Book List"));
        
        // Table columns
        String[] columns = {"Book ID", "Author ID", "Genre ID", "ISBN", "Title", "Publisher", "Year", "Qty", "Price", "Location"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        bookTable = new JTable(tableModel);
        bookTable.setRowHeight(25);
        bookTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                loadSelectedBook();
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(bookTable);
        scrollPane.setPreferredSize(new Dimension(800, 400));
        tablePanel.add(scrollPane, BorderLayout.CENTER);
        
        // Add panels to main panel
        mainPanel.add(formPanel, BorderLayout.WEST);
        mainPanel.add(tablePanel, BorderLayout.CENTER);
        
        add(mainPanel);
        
        // Add action listeners
        btnAdd.addActionListener(e -> addBook());
        btnUpdate.addActionListener(e -> updateBook());
        btnDelete.addActionListener(e -> deleteBook());
        btnClear.addActionListener(e -> clearForm());
        btnSearch.addActionListener(e -> searchBooks());
        btnGenerateBookId.addActionListener(e -> generateBookId());
        btnGenerateAuthorId.addActionListener(e -> generateAuthorId());
        btnGenerateGenreId.addActionListener(e -> generateGenreId());
        
        // Load books on startup
        loadBooks();
    }
    
    private void generateBookId() {
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = DBConnection.getConnection();
            String sql = "SELECT COALESCE(MAX(book_id), 0) + 1 as next_id FROM books";
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);
            
            if (rs.next()) {
                int nextId = rs.getInt("next_id");
                txtBookId.setText(String.valueOf(nextId));
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error generating Book ID: " + e.getMessage(),
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
    
    private void generateAuthorId() {
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = DBConnection.getConnection();
            String sql = "SELECT COALESCE(MAX(author_id), 0) + 1 as next_id FROM authors";
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);
            
            if (rs.next()) {
                int nextId = rs.getInt("next_id");
                txtAuthorId.setText(String.valueOf(nextId));
            } else {
                // If no authors table or it's empty, start from 1
                txtAuthorId.setText("1");
            }
        } catch (SQLException e) {
            // If authors table doesn't exist, show dialog to enter manually
            String authorId = JOptionPane.showInputDialog(this, 
                    "Enter Author ID manually (authors table not found):",
                    "Enter Author ID", JOptionPane.QUESTION_MESSAGE);
            if (authorId != null && !authorId.trim().isEmpty()) {
                txtAuthorId.setText(authorId.trim());
            }
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
    
    private void generateGenreId() {
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = DBConnection.getConnection();
            String sql = "SELECT COALESCE(MAX(genre_id), 0) + 1 as next_id FROM genres";
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);
            
            if (rs.next()) {
                int nextId = rs.getInt("next_id");
                txtGenreId.setText(String.valueOf(nextId));
            } else {
                // If no genres table or it's empty, start from 1
                txtGenreId.setText("1");
            }
        } catch (SQLException e) {
            // If genres table doesn't exist, show dialog to enter manually
            String genreId = JOptionPane.showInputDialog(this, 
                    "Enter Genre ID manually (genres table not found):",
                    "Enter Genre ID", JOptionPane.QUESTION_MESSAGE);
            if (genreId != null && !genreId.trim().isEmpty()) {
                txtGenreId.setText(genreId.trim());
            }
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
    
    private void loadBooks() {
        Connection conn = null;
        PreparedStatement pst = null;
        ResultSet rs = null;
        
        try {
            // Clear table
            tableModel.setRowCount(0);
            
            conn = DBConnection.getConnection();
            
            // Try to select with all three IDs
            try {
                String sql = "SELECT book_id, author_id, genre_id, isbn, title, publisher, " +
                            "publication_year, quantity, price, location " +
                            "FROM books ORDER BY book_id DESC";
                
                pst = conn.prepareStatement(sql);
                rs = pst.executeQuery();
                
                while (rs.next()) {
                    Object[] row = {
                        rs.getInt("book_id"),
                        rs.getInt("author_id"),
                        rs.getInt("genre_id"),
                        rs.getString("isbn"),
                        rs.getString("title"),
                        rs.getString("publisher"),
                        rs.getInt("publication_year"),
                        rs.getInt("quantity"),
                        formatPrice(rs.getDouble("price")),
                        rs.getString("location")
                    };
                    tableModel.addRow(row);
                }
            } catch (SQLException e) {
                // If error, try different column combinations
                if (rs != null) rs.close();
                if (pst != null) pst.close();
                
                // Check which columns exist
                DatabaseMetaData meta = conn.getMetaData();
                ResultSet columns = meta.getColumns(null, null, "books", null);
                boolean hasAuthorId = false;
                boolean hasGenreId = false;
                
                while (columns.next()) {
                    String columnName = columns.getString("COLUMN_NAME");
                    if (columnName.equalsIgnoreCase("author_id")) hasAuthorId = true;
                    if (columnName.equalsIgnoreCase("genre_id")) hasGenreId = true;
                }
                columns.close();
                
                // Build query based on available columns
                StringBuilder sql = new StringBuilder("SELECT book_id, ");
                if (hasAuthorId) sql.append("author_id, ");
                if (hasGenreId) sql.append("genre_id, ");
                sql.append("isbn, title, publisher, publication_year, quantity, price, location ");
                sql.append("FROM books ORDER BY book_id DESC");
                
                pst = conn.prepareStatement(sql.toString());
                rs = pst.executeQuery();
                
                // Update table columns
                List<String> columnList = new ArrayList<>();
                columnList.add("Book ID");
                if (hasAuthorId) columnList.add("Author ID");
                if (hasGenreId) columnList.add("Genre ID");
                columnList.addAll(Arrays.asList("ISBN", "Title", "Publisher", "Year", "Qty", "Price", "Location"));
                
                String[] columnsArray = columnList.toArray(new String[0]);
                tableModel.setColumnIdentifiers(columnsArray);
                
                while (rs.next()) {
                    List<Object> row = new ArrayList<>();
                    row.add(rs.getInt("book_id"));
                    if (hasAuthorId) row.add(rs.getInt("author_id"));
                    if (hasGenreId) row.add(rs.getInt("genre_id"));
                    row.add(rs.getString("isbn"));
                    row.add(rs.getString("title"));
                    row.add(rs.getString("publisher"));
                    row.add(rs.getInt("publication_year"));
                    row.add(rs.getInt("quantity"));
                    row.add(formatPrice(rs.getDouble("price")));
                    row.add(rs.getString("location"));
                    
                    tableModel.addRow(row.toArray());
                }
            }
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading books: " + e.getMessage(),
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
    
    private String formatPrice(double price) {
        DecimalFormat df = new DecimalFormat("$#,##0.00");
        return df.format(price);
    }
    
    private void loadSelectedBook() {
        int selectedRow = bookTable.getSelectedRow();
        if (selectedRow >= 0) {
            clearForm();
            
            int colIndex = 0;
            txtBookId.setText(tableModel.getValueAt(selectedRow, colIndex++).toString());
            
            // Check if we have author_id column
            if (tableModel.getColumnCount() >= 10) { // With all three IDs
                txtAuthorId.setText(tableModel.getValueAt(selectedRow, colIndex++).toString());
                txtGenreId.setText(tableModel.getValueAt(selectedRow, colIndex++).toString());
            } else if (tableModel.getColumnCount() == 9) { // With author_id only
                txtAuthorId.setText(tableModel.getValueAt(selectedRow, colIndex++).toString());
            }
            
            txtISBN.setText(tableModel.getValueAt(selectedRow, colIndex++).toString());
            txtTitle.setText(tableModel.getValueAt(selectedRow, colIndex++).toString());
            txtPublisher.setText(tableModel.getValueAt(selectedRow, colIndex++).toString());
            
            Object yearValue = tableModel.getValueAt(selectedRow, colIndex++);
            txtYear.setText(yearValue != null ? yearValue.toString() : "");
            
            txtQuantity.setText(tableModel.getValueAt(selectedRow, colIndex++).toString());
            
            String priceStr = tableModel.getValueAt(selectedRow, colIndex++).toString();
            priceStr = priceStr.replace("$", "").replace(",", "");
            txtPrice.setText(priceStr);
            
            txtLocation.setText(tableModel.getValueAt(selectedRow, colIndex).toString());
        }
    }
    
    private void addBook() {
        // Validation
        if (!validateInputs()) return;
        
        Connection conn = null;
        PreparedStatement pst = null;
        
        try {
            conn = DBConnection.getConnection();
            
            // Check if Book ID already exists
            String checkIdSql = "SELECT COUNT(*) FROM books WHERE book_id = ?";
            PreparedStatement checkIdStmt = conn.prepareStatement(checkIdSql);
            checkIdStmt.setInt(1, Integer.parseInt(txtBookId.getText().trim()));
            ResultSet idRs = checkIdStmt.executeQuery();
            idRs.next();
            if (idRs.getInt(1) > 0) {
                JOptionPane.showMessageDialog(this, "Book ID already exists!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Check if ISBN already exists
            String checkIsbnSql = "SELECT COUNT(*) FROM books WHERE isbn = ?";
            PreparedStatement checkIsbnStmt = conn.prepareStatement(checkIsbnSql);
            checkIsbnStmt.setString(1, txtISBN.getText().trim());
            ResultSet isbnRs = checkIsbnStmt.executeQuery();
            isbnRs.next();
            if (isbnRs.getInt(1) > 0) {
                JOptionPane.showMessageDialog(this, "ISBN already exists!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Check which columns exist
            DatabaseMetaData meta = conn.getMetaData();
            ResultSet columns = meta.getColumns(null, null, "books", null);
            boolean hasAuthorId = false;
            boolean hasGenreId = false;
            
            while (columns.next()) {
                String columnName = columns.getString("COLUMN_NAME");
                if (columnName.equalsIgnoreCase("author_id")) hasAuthorId = true;
                if (columnName.equalsIgnoreCase("genre_id")) hasGenreId = true;
            }
            columns.close();
            
            // Build INSERT query dynamically
            StringBuilder sql = new StringBuilder("INSERT INTO books (book_id, ");
            StringBuilder values = new StringBuilder("VALUES (?");
            
            int paramIndex = 2; // book_id is parameter 1
            
            if (hasAuthorId) {
                sql.append("author_id, ");
                values.append(", ?");
                paramIndex++;
            }
            
            if (hasGenreId) {
                sql.append("genre_id, ");
                values.append(", ?");
                paramIndex++;
            }
            
            sql.append("isbn, title, publisher, publication_year, quantity, price, location) ");
            values.append(", ?, ?, ?, ?, ?, ?, ?)");
            
            sql.append(values.toString());
            
            pst = conn.prepareStatement(sql.toString());
            pst.setInt(1, Integer.parseInt(txtBookId.getText().trim()));
            
            int currentParam = 2;
            
            if (hasAuthorId) {
                pst.setInt(currentParam++, Integer.parseInt(txtAuthorId.getText().trim()));
            }
            
            if (hasGenreId) {
                pst.setInt(currentParam++, Integer.parseInt(txtGenreId.getText().trim()));
            }
            
            pst.setString(currentParam++, txtISBN.getText().trim());
            pst.setString(currentParam++, txtTitle.getText().trim());
            pst.setString(currentParam++, txtPublisher.getText().trim());
            
            // Year
            if (txtYear.getText().trim().isEmpty()) {
                pst.setNull(currentParam++, java.sql.Types.INTEGER);
            } else {
                pst.setInt(currentParam++, Integer.parseInt(txtYear.getText().trim()));
            }
            
            // Quantity
            pst.setInt(currentParam++, Integer.parseInt(txtQuantity.getText().trim()));
            
            // Price
            pst.setDouble(currentParam++, Double.parseDouble(txtPrice.getText().trim()));
            
            pst.setString(currentParam, txtLocation.getText().trim());
            
            pst.executeUpdate();
            
            JOptionPane.showMessageDialog(this, "Book added successfully!");
            clearForm();
            loadBooks();
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Database Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter valid numbers!", "Error", JOptionPane.ERROR_MESSAGE);
        } finally {
            try {
                if (pst != null) pst.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
    
    private void updateBook() {
        int selectedRow = bookTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a book to update!");
            return;
        }
        
        if (!validateInputs()) return;
        
        int bookId = Integer.parseInt(txtBookId.getText().trim());
        
        Connection conn = null;
        PreparedStatement pst = null;
        
        try {
            conn = DBConnection.getConnection();
            
            // Check which columns exist
            DatabaseMetaData meta = conn.getMetaData();
            ResultSet columns = meta.getColumns(null, null, "books", null);
            boolean hasAuthorId = false;
            boolean hasGenreId = false;
            
            while (columns.next()) {
                String columnName = columns.getString("COLUMN_NAME");
                if (columnName.equalsIgnoreCase("author_id")) hasAuthorId = true;
                if (columnName.equalsIgnoreCase("genre_id")) hasGenreId = true;
            }
            columns.close();
            
            // Build UPDATE query dynamically
            StringBuilder sql = new StringBuilder("UPDATE books SET ");
            
            if (hasAuthorId) {
                sql.append("author_id = ?, ");
            }
            
            if (hasGenreId) {
                sql.append("genre_id = ?, ");
            }
            
            sql.append("isbn = ?, title = ?, publisher = ?, publication_year = ?, ");
            sql.append("quantity = ?, price = ?, location = ? WHERE book_id = ?");
            
            pst = conn.prepareStatement(sql.toString());
            
            int paramIndex = 1;
            
            if (hasAuthorId) {
                pst.setInt(paramIndex++, Integer.parseInt(txtAuthorId.getText().trim()));
            }
            
            if (hasGenreId) {
                pst.setInt(paramIndex++, Integer.parseInt(txtGenreId.getText().trim()));
            }
            
            pst.setString(paramIndex++, txtISBN.getText().trim());
            pst.setString(paramIndex++, txtTitle.getText().trim());
            pst.setString(paramIndex++, txtPublisher.getText().trim());
            
            // Year
            if (txtYear.getText().trim().isEmpty()) {
                pst.setNull(paramIndex++, java.sql.Types.INTEGER);
            } else {
                pst.setInt(paramIndex++, Integer.parseInt(txtYear.getText().trim()));
            }
            
            // Quantity
            pst.setInt(paramIndex++, Integer.parseInt(txtQuantity.getText().trim()));
            
            // Price
            pst.setDouble(paramIndex++, Double.parseDouble(txtPrice.getText().trim()));
            
            pst.setString(paramIndex++, txtLocation.getText().trim());
            pst.setInt(paramIndex, bookId);
            
            int rows = pst.executeUpdate();
            
            if (rows > 0) {
                JOptionPane.showMessageDialog(this, "Book updated successfully!");
                clearForm();
                loadBooks();
            }
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Database Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter valid numbers!", "Error", JOptionPane.ERROR_MESSAGE);
        } finally {
            try {
                if (pst != null) pst.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
    
    private void deleteBook() {
        int selectedRow = bookTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a book to delete!");
            return;
        }
        
        int bookId = (int) tableModel.getValueAt(selectedRow, 0);
        
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete this book?",
                "Confirm Delete", JOptionPane.YES_NO_OPTION);
        
        if (confirm != JOptionPane.YES_OPTION) return;
        
        Connection conn = null;
        PreparedStatement pst = null;
        
        try {
            conn = DBConnection.getConnection();
            String sql = "DELETE FROM books WHERE book_id = ?";
            pst = conn.prepareStatement(sql);
            pst.setInt(1, bookId);
            
            int rows = pst.executeUpdate();
            
            if (rows > 0) {
                JOptionPane.showMessageDialog(this, "Book deleted successfully!");
                clearForm();
                loadBooks();
            }
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error deleting book: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        } finally {
            try {
                if (pst != null) pst.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
    
    private void searchBooks() {
        String searchTerm = JOptionPane.showInputDialog(this, "Enter search term (ISBN, Title):");
        if (searchTerm == null || searchTerm.trim().isEmpty()) return;
        
        Connection conn = null;
        PreparedStatement pst = null;
        ResultSet rs = null;
        
        try {
            conn = DBConnection.getConnection();
            
            // Check which columns exist
            DatabaseMetaData meta = conn.getMetaData();
            ResultSet columns = meta.getColumns(null, null, "books", null);
            boolean hasAuthorId = false;
            boolean hasGenreId = false;
            
            while (columns.next()) {
                String columnName = columns.getString("COLUMN_NAME");
                if (columnName.equalsIgnoreCase("author_id")) hasAuthorId = true;
                if (columnName.equalsIgnoreCase("genre_id")) hasGenreId = true;
            }
            columns.close();
            
            // Build SELECT query
            StringBuilder sql = new StringBuilder("SELECT book_id, ");
            if (hasAuthorId) sql.append("author_id, ");
            if (hasGenreId) sql.append("genre_id, ");
            sql.append("isbn, title, publisher, publication_year, quantity, price, location ");
            sql.append("FROM books WHERE isbn LIKE ? OR title LIKE ? OR publisher LIKE ? ");
            sql.append("ORDER BY book_id DESC");
            
            pst = conn.prepareStatement(sql.toString());
            String likeTerm = "%" + searchTerm.trim() + "%";
            pst.setString(1, likeTerm);
            pst.setString(2, likeTerm);
            pst.setString(3, likeTerm);
            
            rs = pst.executeQuery();
            
            tableModel.setRowCount(0);
            
            // Update table columns
            List<String> columnList = new ArrayList<>();
            columnList.add("Book ID");
            if (hasAuthorId) columnList.add("Author ID");
            if (hasGenreId) columnList.add("Genre ID");
            columnList.addAll(Arrays.asList("ISBN", "Title", "Publisher", "Year", "Qty", "Price", "Location"));
            
            String[] columnsArray = columnList.toArray(new String[0]);
            tableModel.setColumnIdentifiers(columnsArray);
            
            while (rs.next()) {
                List<Object> row = new ArrayList<>();
                row.add(rs.getInt("book_id"));
                if (hasAuthorId) row.add(rs.getInt("author_id"));
                if (hasGenreId) row.add(rs.getInt("genre_id"));
                row.add(rs.getString("isbn"));
                row.add(rs.getString("title"));
                row.add(rs.getString("publisher"));
                row.add(rs.getInt("publication_year"));
                row.add(rs.getInt("quantity"));
                row.add(formatPrice(rs.getDouble("price")));
                row.add(rs.getString("location"));
                
                tableModel.addRow(row.toArray());
            }
            
            if (tableModel.getRowCount() == 0) {
                JOptionPane.showMessageDialog(this, "No books found matching: " + searchTerm);
            }
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error searching: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
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
    
    private boolean validateInputs() {
        // Required fields
        if (txtBookId.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Book ID is required!", "Error", JOptionPane.ERROR_MESSAGE);
            txtBookId.requestFocus();
            return false;
        }
        if (txtAuthorId.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Author ID is required!", "Error", JOptionPane.ERROR_MESSAGE);
            txtAuthorId.requestFocus();
            return false;
        }
        if (txtGenreId.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Genre ID is required!", "Error", JOptionPane.ERROR_MESSAGE);
            txtGenreId.requestFocus();
            return false;
        }
        if (txtISBN.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "ISBN is required!", "Error", JOptionPane.ERROR_MESSAGE);
            txtISBN.requestFocus();
            return false;
        }
        if (txtTitle.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Title is required!", "Error", JOptionPane.ERROR_MESSAGE);
            txtTitle.requestFocus();
            return false;
        }
        if (txtQuantity.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Quantity is required!", "Error", JOptionPane.ERROR_MESSAGE);
            txtQuantity.requestFocus();
            return false;
        }
        
        // Validate numbers
        try {
            Integer.parseInt(txtBookId.getText().trim());
            Integer.parseInt(txtAuthorId.getText().trim());
            Integer.parseInt(txtGenreId.getText().trim());
            Integer.parseInt(txtQuantity.getText().trim());
            
            if (!txtYear.getText().trim().isEmpty()) {
                Integer.parseInt(txtYear.getText().trim());
            }
            
            Double.parseDouble(txtPrice.getText().trim());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter valid numbers!", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        return true;
    }
    
    private void clearForm() {
        txtBookId.setText("");
        txtAuthorId.setText("");
        txtGenreId.setText("");
        txtISBN.setText("");
        txtTitle.setText("");
        txtPublisher.setText("");
        txtYear.setText("");
        txtQuantity.setText("1");
        txtPrice.setText("0.00");
        txtLocation.setText("");
        bookTable.clearSelection();
        txtBookId.requestFocus();
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ManageBooks form = new ManageBooks();
            form.setVisible(true);
        });
    }
}