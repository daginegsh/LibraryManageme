package ui;

import utils.DBConnection;
import utils.UserSession;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ManageGenres extends JFrame {
    
    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField txtGenreName;
    private JTextArea txtDescription;
    private JButton btnAdd, btnUpdate, btnDelete, btnClear, btnRefresh, btnGenerate;
    private JLabel statsLabel;
    private int selectedGenreId = -1;
    
    public ManageGenres() {
        setTitle("📚 Manage Book Genres");
        setSize(950, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        
        // Access Control Check
        if (!UserSession.canPerform("MANAGE_BOOKS")) {
            JOptionPane.showMessageDialog(this, 
                "⚠️ Access Denied! Only Librarians and Administrators can manage genres.", 
                "Access Restricted", JOptionPane.ERROR_MESSAGE);
        }
        
        createUI();
        applyAccessControl();
        loadGenres();
    }
    
    private void createUI() {
        // Main panel
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // ===== FORM PANEL (Left) =====
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("📖 Genre Information"));
        formPanel.setPreferredSize(new Dimension(400, 0));
        formPanel.setBackground(new Color(248, 249, 250));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Title
        JLabel titleLabel = new JLabel("ADD / EDIT / GENERATE GENRES");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        titleLabel.setForeground(new Color(0, 102, 204));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        formPanel.add(titleLabel, gbc);
        
        // Genre Name
        gbc.gridwidth = 1;
        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(createLabel("Genre Name*:"), gbc);
        
        gbc.gridx = 1;
        txtGenreName = new JTextField(25);
        txtGenreName.setToolTipText("Maximum 50 characters");
        formPanel.add(txtGenreName, gbc);
        
        // Description
        gbc.gridx = 0; gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        formPanel.add(createLabel("Description:"), gbc);
        
        gbc.gridx = 1; gbc.gridheight = 3;
        txtDescription = new JTextArea(8, 25);
        txtDescription.setLineWrap(true);
        txtDescription.setWrapStyleWord(true);
        JScrollPane descScroll = new JScrollPane(txtDescription);
        descScroll.setPreferredSize(new Dimension(250, 150));
        formPanel.add(descScroll, gbc);
        
        // Reset gridheight for next rows
        gbc.gridheight = 1;
        
        // ===== ACTION BUTTONS PANEL =====
        JPanel actionButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        actionButtonPanel.setBackground(new Color(248, 249, 250));
        
        btnAdd = createButton("➕ Add Genre", new Color(40, 167, 69));
        btnUpdate = createButton("✏️ Update", new Color(0, 123, 255));
        btnDelete = createButton("🗑️ Delete", new Color(220, 53, 69));
        btnClear = createButton("🧹 Clear", new Color(108, 117, 125));
        btnRefresh = createButton("🔄 Refresh", new Color(23, 162, 184));
        
        actionButtonPanel.add(btnAdd);
        actionButtonPanel.add(btnUpdate);
        actionButtonPanel.add(btnDelete);
        actionButtonPanel.add(btnClear);
        actionButtonPanel.add(btnRefresh);
        
        // ===== GENERATE BUTTON PANEL =====
        JPanel generatePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        generatePanel.setBackground(new Color(248, 249, 250));
        generatePanel.setBorder(BorderFactory.createTitledBorder("⚡ Quick Actions"));
        
        btnGenerate = createButton("🎲 Generate Sample Genres", new Color(111, 66, 193));
        btnGenerate.setFont(new Font("Arial", Font.BOLD, 13));
        btnGenerate.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        generatePanel.add(btnGenerate);
        
        // ===== TABLE PANEL (Right) =====
        String[] columns = {"ID", "Genre Name", "Description", "Books Count", "Created"};
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
                loadSelectedGenre();
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createTitledBorder("📚 Genres List"));
        
        // ===== STATS PANEL =====
        JPanel statsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statsPanel.setBackground(new Color(233, 236, 239));
        statsPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        
        statsLabel = new JLabel("📊 Total Genres: 0 | Total Books: 0");
        statsLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        statsPanel.add(statsLabel);
        
        // ===== ASSEMBLE LEFT PANEL =====
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.add(formPanel, BorderLayout.NORTH);
        leftPanel.add(actionButtonPanel, BorderLayout.CENTER);
        leftPanel.add(generatePanel, BorderLayout.SOUTH);
        
        mainPanel.add(leftPanel, BorderLayout.WEST);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(statsPanel, BorderLayout.SOUTH);
        
        add(mainPanel);
        
        // ===== ACTION LISTENERS =====
        btnAdd.addActionListener(e -> addGenre());
        btnUpdate.addActionListener(e -> updateGenre());
        btnDelete.addActionListener(e -> deleteGenre());
        btnClear.addActionListener(e -> clearForm());
        btnRefresh.addActionListener(e -> loadGenres());
        btnGenerate.addActionListener(e -> generateSampleGenres());
    }
    
    private void applyAccessControl() {
        boolean canManage = UserSession.canPerform("MANAGE_BOOKS");
        
        btnAdd.setEnabled(canManage);
        btnUpdate.setEnabled(false); // Initially disabled
        btnDelete.setEnabled(false); // Initially disabled
        btnGenerate.setEnabled(canManage); // Only authorized users can generate
        
        txtGenreName.setEditable(canManage);
        txtDescription.setEditable(canManage);
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
    
    private void loadGenres() {
        tableModel.setRowCount(0);
        Connection conn = null;
        PreparedStatement pst = null;
        ResultSet rs = null;
        
        try {
            conn = DBConnection.getConnection();
            
            String sql = "SELECT g.genre_id, g.genre_name, g.description, " +
                        "g.created_at, COUNT(b.book_id) as book_count " +
                        "FROM genres g " +
                        "LEFT JOIN books b ON g.genre_id = b.genre_id " +
                        "GROUP BY g.genre_id, g.genre_name, g.description, g.created_at " +
                        "ORDER BY g.genre_name";
            
            pst = conn.prepareStatement(sql);
            rs = pst.executeQuery();
            
            int totalGenres = 0;
            int totalBooks = 0;
            
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
            
            while (rs.next()) {
                totalGenres++;
                int bookCount = rs.getInt("book_count");
                totalBooks += bookCount;
                
                // Format created_at date
                Timestamp createdAt = rs.getTimestamp("created_at");
                String formattedDate = "N/A";
                if (createdAt != null) {
                    LocalDateTime dateTime = createdAt.toLocalDateTime();
                    formattedDate = dateTime.format(formatter);
                }
                
                Object[] row = {
                    rs.getInt("genre_id"),
                    rs.getString("genre_name"),
                    rs.getString("description") != null ? rs.getString("description") : "N/A",
                    bookCount,
                    formattedDate
                };
                tableModel.addRow(row);
            }
            
            // Update stats
            updateStats(totalGenres, totalBooks);
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, 
                "Error loading genres: " + e.getMessage(), 
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
    
    private void updateStats(int totalGenres, int totalBooks) {
        statsLabel.setText(
            "📊 Total Genres: " + totalGenres + 
            " | Total Books in Genres: " + totalBooks
        );
    }
    
    private void loadSelectedGenre() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow != -1) {
            selectedGenreId = Integer.parseInt(tableModel.getValueAt(selectedRow, 0).toString());
            txtGenreName.setText(tableModel.getValueAt(selectedRow, 1).toString());
            
            String description = tableModel.getValueAt(selectedRow, 2).toString();
            txtDescription.setText(description.equals("N/A") ? "" : description);
            
            // Enable update/delete buttons if user has permission
            boolean canManage = UserSession.canPerform("MANAGE_BOOKS");
            btnAdd.setEnabled(false);
            btnUpdate.setEnabled(canManage);
            btnDelete.setEnabled(canManage);
        }
    }
    
    private void addGenre() {
        if (!validateFields()) return;
        
        Connection conn = null;
        PreparedStatement pst = null;
        
        try {
            conn = DBConnection.getConnection();
            
            String sql = "INSERT INTO genres (genre_name, description) VALUES (?, ?)";
            pst = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            
            pst.setString(1, txtGenreName.getText().trim());
            
            String description = txtDescription.getText().trim();
            if (!description.isEmpty()) {
                pst.setString(2, description);
            } else {
                pst.setNull(2, Types.VARCHAR);
            }
            
            int rows = pst.executeUpdate();
            if (rows > 0) {
                // Get the generated genre_id
                ResultSet generatedKeys = pst.getGeneratedKeys();
                if (generatedKeys.next()) {
                    int newGenreId = generatedKeys.getInt(1);
                    UserSession.logActivity("GENRE_ADD", "Added genre ID " + newGenreId + ": " + txtGenreName.getText());
                }
                
                JOptionPane.showMessageDialog(this, 
                    "✅ Genre added successfully!", 
                    "Success", JOptionPane.INFORMATION_MESSAGE);
                loadGenres();
                clearForm();
            }
            
        } catch (SQLException e) {
            if (e.getErrorCode() == 1062) { // Duplicate entry error
                JOptionPane.showMessageDialog(this, 
                    "❌ Genre name already exists!", 
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
    
    private void updateGenre() {
        if (selectedGenreId == -1) {
            JOptionPane.showMessageDialog(this, 
                "❌ Please select a genre to update!", 
                "Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        if (!validateFields()) return;
        
        Connection conn = null;
        PreparedStatement pst = null;
        
        try {
            conn = DBConnection.getConnection();
            String sql = "UPDATE genres SET genre_name=?, description=? WHERE genre_id=?";
            pst = conn.prepareStatement(sql);
            
            pst.setString(1, txtGenreName.getText().trim());
            
            String description = txtDescription.getText().trim();
            if (!description.isEmpty()) {
                pst.setString(2, description);
            } else {
                pst.setNull(2, Types.VARCHAR);
            }
            
            pst.setInt(3, selectedGenreId);
            
            int rows = pst.executeUpdate();
            if (rows > 0) {
                UserSession.logActivity("GENRE_UPDATE", 
                    "Updated genre ID " + selectedGenreId + ": " + txtGenreName.getText());
                JOptionPane.showMessageDialog(this, 
                    "✅ Genre updated successfully!", 
                    "Success", JOptionPane.INFORMATION_MESSAGE);
                loadGenres();
                clearForm();
            }
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, 
                "Error updating genre: " + e.getMessage(), 
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
    
    private void deleteGenre() {
        if (selectedGenreId == -1) {
            JOptionPane.showMessageDialog(this, 
                "❌ Please select a genre to delete!", 
                "Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String genreName = txtGenreName.getText();
        int bookCount = getGenreBookCount(selectedGenreId);
        
        String warningMessage;
        if (bookCount > 0) {
            warningMessage = "Are you sure you want to delete:\n" + genreName + "?\n\n" +
                           "⚠️ WARNING: This genre has " + bookCount + " book(s) in the system!\n" +
                           "Deleting will set those books' genre_id to NULL.";
        } else {
            warningMessage = "Are you sure you want to delete:\n" + genreName + "?";
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
                
                // First, set books' genre_id to NULL to maintain referential integrity
                String updateBooksSql = "UPDATE books SET genre_id = NULL WHERE genre_id = ?";
                pst = conn.prepareStatement(updateBooksSql);
                pst.setInt(1, selectedGenreId);
                pst.executeUpdate();
                pst.close();
                
                // Then delete the genre
                String deleteGenreSql = "DELETE FROM genres WHERE genre_id = ?";
                pst = conn.prepareStatement(deleteGenreSql);
                pst.setInt(1, selectedGenreId);
                
                int rows = pst.executeUpdate();
                if (rows > 0) {
                    UserSession.logActivity("GENRE_DELETE", 
                        "Deleted genre ID " + selectedGenreId + ": " + genreName);
                    JOptionPane.showMessageDialog(this, 
                        "✅ Genre deleted successfully!\n" + 
                        (bookCount > 0 ? "Associated books now have NULL genre_id." : ""), 
                        "Success", JOptionPane.INFORMATION_MESSAGE);
                    loadGenres();
                    clearForm();
                }
                
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, 
                    "Error deleting genre: " + e.getMessage(), 
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
    
    // ===== GENERATE SAMPLE GENRES METHOD =====
    private void generateSampleGenres() {
        int confirm = JOptionPane.showConfirmDialog(this,
            "🎲 Generate Sample Genres\n\n" +
            "This will add 10 popular book genres to the database.\n" +
            "Genres will only be added if they don't already exist.\n\n" +
            "Do you want to continue?",
            "Generate Sample Genres",
            JOptionPane.YES_NO_OPTION, 
            JOptionPane.QUESTION_MESSAGE);
        
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }
        
        Connection conn = null;
        PreparedStatement pst = null;
        int addedCount = 0;
        
        try {
            conn = DBConnection.getConnection();
            
            // Array of sample genres with their descriptions
            Object[][] sampleGenres = {
                {"Fiction", "Imaginary stories and characters"},
                {"Non-Fiction", "Factual and informative works"},
                {"Mystery", "Stories involving puzzles and investigations"},
                {"Science Fiction", "Futuristic and speculative technology"},
                {"Fantasy", "Magical worlds and mythical creatures"},
                {"Romance", "Love stories and relationships"},
                {"Biography", "Accounts of people's lives"},
                {"History", "Records of past events"},
                {"Self-Help", "Personal development and improvement"},
                {"Young Adult", "Books targeted at teenage readers"}
            };
            
            String checkSql = "SELECT COUNT(*) FROM genres WHERE genre_name = ?";
            String insertSql = "INSERT INTO genres (genre_name, description) VALUES (?, ?)";
            
            // Disable auto-commit for batch processing
            conn.setAutoCommit(false);
            
            pst = conn.prepareStatement(checkSql);
            
            for (Object[] genre : sampleGenres) {
                String genreName = (String) genre[0];
                String description = (String) genre[1];
                
                // Check if genre already exists
                pst.setString(1, genreName);
                ResultSet rs = pst.executeQuery();
                if (rs.next() && rs.getInt(1) > 0) {
                    // Genre already exists, skip
                    continue;
                }
                
                // Close the check statement and prepare insert statement
                pst.close();
                pst = conn.prepareStatement(insertSql);
                pst.setString(1, genreName);
                pst.setString(2, description);
                pst.executeUpdate();
                addedCount++;
                
                // Re-open check statement for next iteration
                pst.close();
                pst = conn.prepareStatement(checkSql);
            }
            
            // Commit the transaction
            conn.commit();
            
            UserSession.logActivity("GENRE_GENERATE", 
                "Generated " + addedCount + " sample genres");
            
            // Show results
            String message;
            if (addedCount == 0) {
                message = "✅ All sample genres already exist in the database.";
            } else if (addedCount == sampleGenres.length) {
                message = "✅ Successfully added all " + addedCount + " sample genres!";
            } else {
                message = "✅ Added " + addedCount + " new genres.\n" +
                         (sampleGenres.length - addedCount) + " genres already existed.";
            }
            
            JOptionPane.showMessageDialog(this,
                message,
                "Generation Complete",
                JOptionPane.INFORMATION_MESSAGE);
            
            // Refresh the table
            loadGenres();
            
        } catch (SQLException e) {
            try {
                if (conn != null) {
                    conn.rollback();
                }
            } catch (SQLException rollbackEx) {
                rollbackEx.printStackTrace();
            }
            
            JOptionPane.showMessageDialog(this,
                "❌ Error generating sample genres:\n" + e.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        } finally {
            try {
                if (conn != null) {
                    conn.setAutoCommit(true); // Restore auto-commit
                }
                if (pst != null) {
                    pst.close();
                }
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
    
    private int getGenreBookCount(int genreId) {
        Connection conn = null;
        PreparedStatement pst = null;
        ResultSet rs = null;
        
        try {
            conn = DBConnection.getConnection();
            String sql = "SELECT COUNT(*) as book_count FROM books WHERE genre_id = ?";
            pst = conn.prepareStatement(sql);
            pst.setInt(1, genreId);
            
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
    
    private boolean validateFields() {
        String genreName = txtGenreName.getText().trim();
        
        if (genreName.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "❌ Genre name is required!", 
                "Validation Error", JOptionPane.WARNING_MESSAGE);
            txtGenreName.requestFocus();
            return false;
        }
        
        // Validate genre name length (matches VARCHAR(50) in SQL)
        if (genreName.length() > 50) {
            JOptionPane.showMessageDialog(this, 
                "❌ Genre name cannot exceed 50 characters!", 
                "Validation Error", JOptionPane.WARNING_MESSAGE);
            txtGenreName.requestFocus();
            return false;
        }
        
        // Validate genre name contains only valid characters
        if (!genreName.matches("^[a-zA-Z0-9\\s\\-&,]+$")) {
            JOptionPane.showMessageDialog(this, 
                "❌ Genre name can only contain letters, numbers, spaces, hyphens, commas, and ampersands!", 
                "Validation Error", JOptionPane.WARNING_MESSAGE);
            txtGenreName.requestFocus();
            return false;
        }
        
        return true;
    }
    
    private void clearForm() {
        txtGenreName.setText("");
        txtDescription.setText("");
        selectedGenreId = -1;
        table.clearSelection();
        
        // Restore button states
        boolean canManage = UserSession.canPerform("MANAGE_BOOKS");
        btnAdd.setEnabled(canManage);
        btnUpdate.setEnabled(false);
        btnDelete.setEnabled(false);
    }
    
    public static void main(String[] args) {
        // For testing purposes
        UserSession.setRole("LIBRARIAN");
        
        SwingUtilities.invokeLater(() -> {
            new ManageGenres().setVisible(true);
        });
    }
}