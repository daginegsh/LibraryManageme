package ui;

import utils.DBConnection;
import utils.UserSession; // Import UserSession for access control
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ManageAuthors extends JFrame {
    
    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField txtAuthorId, txtAuthorName, txtNationality, txtBirthYear;
    private JTextArea txtBiography;
    private JButton btnAdd, btnUpdate, btnDelete, btnClear, btnRefresh, btnGenerateAuthorId;
    private int selectedAuthorId = -1;
    
    public ManageAuthors() {
        setTitle("✍️ Manage Authors");
        setSize(1000, 650);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        
        // Access Control Check
        if (!UserSession.canPerform("MANAGE_BOOKS")) {
            JOptionPane.showMessageDialog(this, 
                "⚠️ Access Denied! Only Librarians and Administrators can manage authors.", 
                "Access Restricted", JOptionPane.ERROR_MESSAGE);
        }
        
        createUI();
        applyAccessControl();
        loadAuthors();
    }
    
    private void createUI() {
        // Main panel
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // ===== FORM PANEL (Left) =====
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("✍️ Author Details"));
        formPanel.setPreferredSize(new Dimension(450, 0));
        formPanel.setBackground(new Color(248, 249, 250));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Title
        JLabel titleLabel = new JLabel("ADD / EDIT AUTHORS");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        titleLabel.setForeground(new Color(0, 102, 204));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        formPanel.add(titleLabel, gbc);
        
        // Author ID with Generate button
        gbc.gridwidth = 1;
        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(createLabel("Author ID*:"), gbc);
        
        JPanel authorIdPanel = new JPanel(new BorderLayout(5, 0));
        txtAuthorId = new JTextField(10);
        txtAuthorId.setEditable(false); // User can't edit it directly
        authorIdPanel.add(txtAuthorId, BorderLayout.CENTER);
        btnGenerateAuthorId = new JButton("Generate");
        btnGenerateAuthorId.setBackground(new Color(100, 149, 237));
        btnGenerateAuthorId.setForeground(Color.WHITE);
        authorIdPanel.add(btnGenerateAuthorId, BorderLayout.EAST);
        gbc.gridx = 1;
        formPanel.add(authorIdPanel, gbc);
        
        // Author Name
        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(createLabel("Author Name*:"), gbc);
        
        gbc.gridx = 1;
        txtAuthorName = new JTextField(25);
        formPanel.add(txtAuthorName, gbc);
        
        // Nationality
        gbc.gridx = 0; gbc.gridy = 3;
        formPanel.add(createLabel("Nationality:"), gbc);
        
        gbc.gridx = 1;
        txtNationality = new JTextField(20);
        formPanel.add(txtNationality, gbc);
        
        // Birth Year
        gbc.gridx = 0; gbc.gridy = 4;
        formPanel.add(createLabel("Birth Year:"), gbc);
        
        gbc.gridx = 1;
        txtBirthYear = new JTextField(10);
        txtBirthYear.setToolTipText("Format: YYYY (e.g., 1965)");
        formPanel.add(txtBirthYear, gbc);
        
        // Biography
        gbc.gridx = 0; gbc.gridy = 5;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        formPanel.add(createLabel("Biography:"), gbc);
        
        gbc.gridx = 1; gbc.gridheight = 3;
        txtBiography = new JTextArea(8, 25);
        txtBiography.setLineWrap(true);
        txtBiography.setWrapStyleWord(true);
        JScrollPane bioScroll = new JScrollPane(txtBiography);
        bioScroll.setPreferredSize(new Dimension(250, 150));
        formPanel.add(bioScroll, gbc);
        
        // Reset gridheight for next rows
        gbc.gridheight = 1;
        
        // ===== BUTTON PANEL =====
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        buttonPanel.setBackground(new Color(248, 249, 250));
        
        btnAdd = createButton("➕ Add Author", new Color(40, 167, 69));
        btnUpdate = createButton("✏️ Update", new Color(0, 123, 255));
        btnDelete = createButton("🗑️ Delete", new Color(220, 53, 69));
        btnClear = createButton("🧹 Clear", new Color(108, 117, 125));
        btnRefresh = createButton("🔄 Refresh", new Color(23, 162, 184));
        
        buttonPanel.add(btnAdd);
        buttonPanel.add(btnUpdate);
        buttonPanel.add(btnDelete);
        buttonPanel.add(btnClear);
        buttonPanel.add(btnRefresh);
        
        // ===== TABLE PANEL (Right) =====
        String[] columns = {"Author ID", "Author Name", "Nationality", "Birth Year", "Books Count", "Created"};
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
                loadSelectedAuthor();
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createTitledBorder("📚 Authors List"));
        
        // ===== STATS PANEL =====
        JPanel statsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statsPanel.setBackground(new Color(233, 236, 239));
        statsPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        
        JLabel statsLabel = new JLabel("👥 Total Authors: 0");
        statsLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        statsPanel.add(statsLabel);
        
        // ===== ASSEMBLE =====
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.add(formPanel, BorderLayout.CENTER);
        leftPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        mainPanel.add(leftPanel, BorderLayout.WEST);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(statsPanel, BorderLayout.SOUTH);
        
        add(mainPanel);
        
        // ===== ACTION LISTENERS =====
        btnAdd.addActionListener(e -> addAuthor());
        btnUpdate.addActionListener(e -> updateAuthor());
        btnDelete.addActionListener(e -> deleteAuthor());
        btnClear.addActionListener(e -> clearForm());
        btnRefresh.addActionListener(e -> loadAuthors());
        btnGenerateAuthorId.addActionListener(e -> generateAuthorId());
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
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, 
                "Error generating Author ID: " + e.getMessage(),
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
        boolean canManage = UserSession.canPerform("MANAGE_BOOKS");
        
        btnAdd.setEnabled(canManage);
        btnUpdate.setEnabled(false); // Initially disabled
        btnDelete.setEnabled(false); // Initially disabled
        btnGenerateAuthorId.setEnabled(canManage);
        
        txtAuthorId.setEditable(false); // Always read-only, use Generate button
        txtAuthorName.setEditable(canManage);
        txtNationality.setEditable(canManage);
        txtBirthYear.setEditable(canManage);
        txtBiography.setEditable(canManage);
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
    
    private void loadAuthors() {
        tableModel.setRowCount(0);
        Connection conn = null;
        PreparedStatement pst = null;
        ResultSet rs = null;
        
        try {
            conn = DBConnection.getConnection();
            
            // Modified query to include book count
            String sql = "SELECT a.author_id, a.author_name, a.nationality, a.birth_year, " +
                        "a.created_at, COUNT(b.book_id) as book_count " +
                        "FROM authors a " +
                        "LEFT JOIN books b ON a.author_id = b.author_id " +
                        "GROUP BY a.author_id, a.author_name, a.nationality, a.birth_year, a.created_at " +
                        "ORDER BY a.author_name";
            
            pst = conn.prepareStatement(sql);
            rs = pst.executeQuery();
            
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
            int authorCount = 0;
            
            while (rs.next()) {
                authorCount++;
                
                // Format created_at date
                Timestamp createdAt = rs.getTimestamp("created_at");
                String formattedDate = "N/A";
                if (createdAt != null) {
                    LocalDateTime dateTime = createdAt.toLocalDateTime();
                    formattedDate = dateTime.format(formatter);
                }
                
                // Handle birth year null
                Object birthYear = rs.getObject("birth_year");
                String birthYearStr = (birthYear != null) ? birthYear.toString() : "N/A";
                
                Object[] row = {
                    rs.getInt("author_id"),
                    rs.getString("author_name"),
                    rs.getString("nationality") != null ? rs.getString("nationality") : "N/A",
                    birthYearStr,
                    rs.getInt("book_count"),
                    formattedDate
                };
                tableModel.addRow(row);
            }
            
            // Update stats
            updateAuthorStats(authorCount);
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, 
                "Error loading authors: " + e.getMessage(), 
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
    
    private void updateAuthorStats(int authorCount) {
        // You can add stats label update here if you add a stats label
        setTitle("✍️ Manage Authors (" + authorCount + " authors)");
    }
    
    private void loadSelectedAuthor() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow != -1) {
            selectedAuthorId = Integer.parseInt(tableModel.getValueAt(selectedRow, 0).toString());
            txtAuthorId.setText(String.valueOf(selectedAuthorId));
            txtAuthorName.setText(tableModel.getValueAt(selectedRow, 1).toString());
            
            String nationality = tableModel.getValueAt(selectedRow, 2).toString();
            txtNationality.setText(nationality.equals("N/A") ? "" : nationality);
            
            String birthYear = tableModel.getValueAt(selectedRow, 3).toString();
            txtBirthYear.setText(birthYear.equals("N/A") ? "" : birthYear);
            
            loadAuthorBiography(selectedAuthorId);
            
            // Enable update/delete buttons if user has permission
            boolean canManage = UserSession.canPerform("MANAGE_BOOKS");
            btnAdd.setEnabled(false);
            btnUpdate.setEnabled(canManage);
            btnDelete.setEnabled(canManage);
        }
    }
    
    private void loadAuthorBiography(int authorId) {
        Connection conn = null;
        PreparedStatement pst = null;
        ResultSet rs = null;
        
        try {
            conn = DBConnection.getConnection();
            String sql = "SELECT biography FROM authors WHERE author_id = ?";
            pst = conn.prepareStatement(sql);
            pst.setInt(1, authorId);
            
            rs = pst.executeQuery();
            if (rs.next()) {
                String biography = rs.getString("biography");
                txtBiography.setText(biography != null ? biography : "");
            } else {
                txtBiography.setText("");
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
    }
    
    private void addAuthor() {
        if (!validateFields(true)) return;
        
        // Check if Author ID is generated
        if (txtAuthorId.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "❌ Please generate an Author ID first!", 
                "Validation Error", JOptionPane.WARNING_MESSAGE);
            btnGenerateAuthorId.requestFocus();
            return;
        }
        
        Connection conn = null;
        PreparedStatement pst = null;
        
        try {
            conn = DBConnection.getConnection();
            
            // Check if Author ID already exists
            String checkIdSql = "SELECT COUNT(*) FROM authors WHERE author_id = ?";
            PreparedStatement checkIdStmt = conn.prepareStatement(checkIdSql);
            checkIdStmt.setInt(1, Integer.parseInt(txtAuthorId.getText().trim()));
            ResultSet idRs = checkIdStmt.executeQuery();
            idRs.next();
            if (idRs.getInt(1) > 0) {
                JOptionPane.showMessageDialog(this, 
                    "❌ Author ID already exists! Please generate a new one.", 
                    "Duplicate ID", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Check if author name already exists
            String checkNameSql = "SELECT COUNT(*) FROM authors WHERE author_name = ?";
            PreparedStatement checkNameStmt = conn.prepareStatement(checkNameSql);
            checkNameStmt.setString(1, txtAuthorName.getText().trim());
            ResultSet nameRs = checkNameStmt.executeQuery();
            nameRs.next();
            if (nameRs.getInt(1) > 0) {
                JOptionPane.showMessageDialog(this, 
                    "❌ Author name already exists!", 
                    "Duplicate Name", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Insert with specified author_id
            String sql = "INSERT INTO authors (author_id, author_name, nationality, birth_year, biography) " +
                        "VALUES (?, ?, ?, ?, ?)";
            pst = conn.prepareStatement(sql);
            
            pst.setInt(1, Integer.parseInt(txtAuthorId.getText().trim()));
            pst.setString(2, txtAuthorName.getText().trim());
            
            String nationality = txtNationality.getText().trim();
            if (!nationality.isEmpty()) {
                pst.setString(3, nationality);
            } else {
                pst.setNull(3, Types.VARCHAR);
            }
            
            String birthYearStr = txtBirthYear.getText().trim();
            if (!birthYearStr.isEmpty()) {
                int birthYear = Integer.parseInt(birthYearStr);
                if (birthYear < 1000 || birthYear > LocalDateTime.now().getYear()) {
                    JOptionPane.showMessageDialog(this, 
                        "❌ Please enter a valid birth year (1000 - " + LocalDateTime.now().getYear() + ")!", 
                        "Validation Error", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                pst.setInt(4, birthYear);
            } else {
                pst.setNull(4, Types.INTEGER);
            }
            
            String biography = txtBiography.getText().trim();
            if (!biography.isEmpty()) {
                pst.setString(5, biography);
            } else {
                pst.setNull(5, Types.VARCHAR);
            }
            
            int rows = pst.executeUpdate();
            if (rows > 0) {
                UserSession.logActivity("AUTHOR_ADD", "Added author ID " + txtAuthorId.getText() + ": " + txtAuthorName.getText());
                
                JOptionPane.showMessageDialog(this, 
                    "✅ Author added successfully!\nAuthor ID: " + txtAuthorId.getText(), 
                    "Success", JOptionPane.INFORMATION_MESSAGE);
                loadAuthors();
                clearForm();
            }
            
        } catch (SQLException e) {
            if (e.getErrorCode() == 1062) { // Duplicate entry
                JOptionPane.showMessageDialog(this, 
                    "❌ Author name already exists!", 
                    "Duplicate Entry", JOptionPane.ERROR_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, 
                    "Error: " + e.getMessage(), 
                    "Database Error", JOptionPane.ERROR_MESSAGE);
            }
            e.printStackTrace();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, 
                "❌ Birth year must be a valid number!", 
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
    
    private void updateAuthor() {
        if (selectedAuthorId == -1) {
            JOptionPane.showMessageDialog(this, 
                "❌ Please select an author to update!", 
                "Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        if (!validateFields(false)) return;
        
        Connection conn = null;
        PreparedStatement pst = null;
        
        try {
            conn = DBConnection.getConnection();
            String sql = "UPDATE authors SET author_name=?, nationality=?, birth_year=?, biography=? " +
                        "WHERE author_id=?";
            pst = conn.prepareStatement(sql);
            
            pst.setString(1, txtAuthorName.getText().trim());
            
            String nationality = txtNationality.getText().trim();
            if (!nationality.isEmpty()) {
                pst.setString(2, nationality);
            } else {
                pst.setNull(2, Types.VARCHAR);
            }
            
            String birthYearStr = txtBirthYear.getText().trim();
            if (!birthYearStr.isEmpty()) {
                int birthYear = Integer.parseInt(birthYearStr);
                if (birthYear < 1000 || birthYear > LocalDateTime.now().getYear()) {
                    JOptionPane.showMessageDialog(this, 
                        "❌ Please enter a valid birth year (1000 - " + LocalDateTime.now().getYear() + ")!", 
                        "Validation Error", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                pst.setInt(3, birthYear);
            } else {
                pst.setNull(3, Types.INTEGER);
            }
            
            String biography = txtBiography.getText().trim();
            if (!biography.isEmpty()) {
                pst.setString(4, biography);
            } else {
                pst.setNull(4, Types.VARCHAR);
            }
            
            pst.setInt(5, selectedAuthorId);
            
            int rows = pst.executeUpdate();
            if (rows > 0) {
                UserSession.logActivity("AUTHOR_UPDATE", "Updated author ID " + selectedAuthorId + ": " + txtAuthorName.getText());
                JOptionPane.showMessageDialog(this, 
                    "✅ Author updated successfully!", 
                    "Success", JOptionPane.INFORMATION_MESSAGE);
                loadAuthors();
                clearForm();
            }
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, 
                "Error updating author: " + e.getMessage(), 
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
    
    private void deleteAuthor() {
        if (selectedAuthorId == -1) {
            JOptionPane.showMessageDialog(this, 
                "❌ Please select an author to delete!", 
                "Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String authorName = txtAuthorName.getText();
        int bookCount = getAuthorBookCount(selectedAuthorId);
        
        String warningMessage;
        if (bookCount > 0) {
            warningMessage = "Are you sure you want to delete:\n" + 
                           "Author ID: " + selectedAuthorId + "\n" +
                           "Name: " + authorName + "\n\n" +
                           "⚠️ WARNING: This author has " + bookCount + " book(s) in the system!\n" +
                           "Deleting will set those books' author_id to NULL.";
        } else {
            warningMessage = "Are you sure you want to delete:\n" +
                           "Author ID: " + selectedAuthorId + "\n" +
                           "Name: " + authorName + "?";
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
                
                // First, set books' author_id to NULL to maintain referential integrity
                String updateBooksSql = "UPDATE books SET author_id = NULL WHERE author_id = ?";
                pst = conn.prepareStatement(updateBooksSql);
                pst.setInt(1, selectedAuthorId);
                pst.executeUpdate();
                pst.close();
                
                // Then delete the author
                String deleteAuthorSql = "DELETE FROM authors WHERE author_id = ?";
                pst = conn.prepareStatement(deleteAuthorSql);
                pst.setInt(1, selectedAuthorId);
                
                int rows = pst.executeUpdate();
                if (rows > 0) {
                    UserSession.logActivity("AUTHOR_DELETE", "Deleted author ID " + selectedAuthorId + ": " + authorName);
                    JOptionPane.showMessageDialog(this, 
                        "✅ Author deleted successfully!\n" + 
                        (bookCount > 0 ? "Associated books now have NULL author_id." : ""), 
                        "Success", JOptionPane.INFORMATION_MESSAGE);
                    loadAuthors();
                    clearForm();
                }
                
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, 
                    "Error deleting author: " + e.getMessage(), 
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
    }
    
    private int getAuthorBookCount(int authorId) {
        Connection conn = null;
        PreparedStatement pst = null;
        ResultSet rs = null;
        
        try {
            conn = DBConnection.getConnection();
            String sql = "SELECT COUNT(*) as book_count FROM books WHERE author_id = ?";
            pst = conn.prepareStatement(sql);
            pst.setInt(1, authorId);
            
            rs = pst.executeQuery();
            if (rs.next()) {
                return rs.getInt("book_count");
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
    
    private boolean validateFields(boolean isNewAuthor) {
        // Check Author ID for new authors
        if (isNewAuthor && txtAuthorId.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "❌ Please generate an Author ID!", 
                "Validation Error", JOptionPane.WARNING_MESSAGE);
            btnGenerateAuthorId.requestFocus();
            return false;
        }
        
        String authorName = txtAuthorName.getText().trim();
        
        if (authorName.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "❌ Author name is required!", 
                "Validation Error", JOptionPane.WARNING_MESSAGE);
            txtAuthorName.requestFocus();
            return false;
        }
        
        // Validate author name length
        if (authorName.length() > 100) {
            JOptionPane.showMessageDialog(this, 
                "❌ Author name cannot exceed 100 characters!", 
                "Validation Error", JOptionPane.WARNING_MESSAGE);
            txtAuthorName.requestFocus();
            return false;
        }
        
        // Validate nationality length
        String nationality = txtNationality.getText().trim();
        if (!nationality.isEmpty() && nationality.length() > 50) {
            JOptionPane.showMessageDialog(this, 
                "❌ Nationality cannot exceed 50 characters!", 
                "Validation Error", JOptionPane.WARNING_MESSAGE);
            txtNationality.requestFocus();
            return false;
        }
        
        // Validate birth year format
        String birthYearStr = txtBirthYear.getText().trim();
        if (!birthYearStr.isEmpty()) {
            try {
                int birthYear = Integer.parseInt(birthYearStr);
                int currentYear = LocalDateTime.now().getYear();
                
                if (birthYear < 1000 || birthYear > currentYear) {
                    JOptionPane.showMessageDialog(this, 
                        "❌ Birth year must be between 1000 and " + currentYear + "!", 
                        "Validation Error", JOptionPane.WARNING_MESSAGE);
                    txtBirthYear.requestFocus();
                    return false;
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, 
                    "❌ Birth year must be a valid number!", 
                    "Validation Error", JOptionPane.WARNING_MESSAGE);
                txtBirthYear.requestFocus();
                return false;
            }
        }
        
        return true;
    }
    
    private void clearForm() {
        txtAuthorId.setText("");
        txtAuthorName.setText("");
        txtNationality.setText("");
        txtBirthYear.setText("");
        txtBiography.setText("");
        selectedAuthorId = -1;
        table.clearSelection();
        
        // Restore button states
        boolean canManage = UserSession.canPerform("MANAGE_BOOKS");
        btnAdd.setEnabled(canManage);
        btnUpdate.setEnabled(false);
        btnDelete.setEnabled(false);
        btnGenerateAuthorId.setEnabled(canManage);
    }
    
    public static void main(String[] args) {
        // For testing purposes
        UserSession.setRole("LIBRARIAN");
        
        SwingUtilities.invokeLater(() -> {
            new ManageAuthors().setVisible(true);
        });
    }
}