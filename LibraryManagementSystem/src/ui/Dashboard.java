package ui;

import javax.swing.*;
import java.awt.*;
import javax.swing.border.EmptyBorder;

// Import the necessary utility class
import utils.UserSession; 

public class Dashboard extends JFrame {
    
    // Declare buttons as class fields so they can be accessed in applyAccessControl()
    private JButton btnBooks, btnMembers, btnAuthors, btnGenres, btnCirculation, 
                    btnReports, btnUsers, btnSettings, btnLogout, btnHelp, btnProfile;
    
    // Declare the user label to dynamically update the welcome message
    private JLabel lblUser;
    private JLabel lblWelcome;

    // 1. UPDATED CONSTRUCTOR: No longer needs arguments
    public Dashboard() {
        
        // Use UserSession for display data
        String userName = UserSession.getUsername();
        String roleDisplay = UserSession.getRoleDisplay();

        // Window setup
        setTitle("Library Dashboard - Welcome " + userName);
        setSize(800, 600);
        setLocationRelativeTo(null); // Center window
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        // Check if user is logged in (a safeguard)
        if (!UserSession.isLoggedIn()) {
            JOptionPane.showMessageDialog(this, "Session expired. Please log in again.", "Error", JOptionPane.ERROR_MESSAGE);
            new LoginForm().setVisible(true);
            dispose();
            return;
        }
        
        createUI(userName, roleDisplay);
        applyAccessControl(); // 3. NEW: Apply role restrictions
        
        // Make sure window is visible
        setVisible(true);
        toFront();
    }
    
    // New method to separate UI creation from constructor setup
    private void createUI(String userName, String roleDisplay) {
        // Create main panel
        JPanel mainPanel = new JPanel(new BorderLayout());
        
        // === HEADER ===
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(0, 102, 204));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        
        JLabel lblTitle = new JLabel("📚 LIBRARY MANAGEMENT SYSTEM");
        lblTitle.setFont(new Font("Arial", Font.BOLD, 22));
        lblTitle.setForeground(Color.WHITE);
        
        // Use class field lblUser
        lblUser = new JLabel("User: " + userName + " (" + roleDisplay + ")"); 
        lblUser.setFont(new Font("Arial", Font.PLAIN, 14));
        lblUser.setForeground(Color.WHITE);
        
        headerPanel.add(lblTitle, BorderLayout.WEST);
        headerPanel.add(lblUser, BorderLayout.EAST);
        
        // === MENU BUTTONS ===
        // Increased rows to 6 to fit the new Profile button
        JPanel menuPanel = new JPanel(new GridLayout(6, 2, 10, 10)); 
        menuPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        
        // Define all buttons (Now using class fields)
        btnBooks = createStyledButton("📚 Manage Books", new Color(41, 128, 185));
        btnMembers = createStyledButton("👥 Manage Members", new Color(39, 174, 96));
        btnAuthors = createStyledButton("✍️ Manage Authors", new Color(142, 68, 173));
        btnGenres = createStyledButton("📖 Book Genres", new Color(22, 160, 133));
        btnCirculation = createStyledButton("🔄 Circulation", new Color(230, 126, 34));
        btnReports = createStyledButton("📊 Reports", new Color(111, 66, 193));
        btnUsers = createStyledButton("👨‍💼 Manage Users", new Color(192, 57, 43));
        btnSettings = createStyledButton("⚙️ Settings", new Color(52, 73, 94));
        btnLogout = createStyledButton("🚪 Logout", new Color(220, 53, 69));
        btnHelp = createStyledButton("❓ Help", new Color(44, 62, 80));
        
        // 2. NEW: Profile Button
        btnProfile = createStyledButton("👤 My Profile", new Color(0, 150, 136));
        
        // Add action listeners
        btnBooks.addActionListener(e -> new ui.ManageBooks().setVisible(true)); // Replace with new ManageBooks() when created
        btnMembers.addActionListener(e -> new ui.ManageMembers().setVisible(true));
       btnAuthors.addActionListener(e -> new ui.ManageAuthors().setVisible(true));
        btnGenres.addActionListener(e -> new ui.ManageGenres().setVisible(true));
        btnCirculation.addActionListener(e -> new ui.ManageCirculation().setVisible(true));
        btnReports.addActionListener(e ->  new ui.Reports().setVisible(true));
        // Link to the actual ManageUsers class
        btnUsers.addActionListener(e -> new ui.ManageUsers().setVisible(true)); 
        btnSettings.addActionListener(e -> showComingSoon("Settings"));
        btnHelp.addActionListener(e -> showHelp());
        btnLogout.addActionListener(e -> logout());
        // Inside your Dashboard.java file, in the createUI() or button setup method:

// --- Reports Button ---
btnReports.addActionListener(e -> {
    
    if (UserSession.canPerform("VIEW_REPORTS")) {
        new ui.Reports().setVisible(true);
    } else {
        JOptionPane.showMessageDialog(this, 
            "⚠️ Access Denied! You do not have permission to view reports.", 
            "Permission Error", JOptionPane.ERROR_MESSAGE);
    }
});
// --- Circulation Button ---
btnCirculation.addActionListener(e -> {
    // Check for the specific permission granted to Staff: MANAGE_CIRCULATION
    if (UserSession.canPerform("MANAGE_CIRCULATION")) {
        // Replace 'ui.ManageCirculation()' with the correct name of your Circulation management screen
        new ui.ManageCirculation().setVisible(true); 
    } else {
        JOptionPane.showMessageDialog(this, 
            "⚠️ Access Denied! You do not have permission to manage circulation.", 
            "Permission Error", JOptionPane.ERROR_MESSAGE);
    }
});
        // Action listener for the new profile button
        btnProfile.addActionListener(e -> new ui.UserProfile().setVisible(true)); 
        
        // Add buttons to panel IN ORDER
        menuPanel.add(btnBooks);
        menuPanel.add(btnMembers);
        menuPanel.add(btnAuthors);
        menuPanel.add(btnGenres);
        menuPanel.add(btnCirculation);
        menuPanel.add(btnReports);
        menuPanel.add(btnUsers);
        menuPanel.add(btnProfile); // New profile button added here
        menuPanel.add(btnSettings);
        menuPanel.add(btnLogout);
        menuPanel.add(btnHelp);
        
        // === WELCOME MESSAGE ===
        JPanel welcomePanel = new JPanel(new BorderLayout());
        welcomePanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Use class field lblWelcome
        lblWelcome = new JLabel(
            "<html><center><h1>Welcome to Library System!</h1>" +
            "<p>You are logged in as: <b>" + userName + "</b> (" + roleDisplay + ")</p>" +
            "<p>Click any button above to access library features.</p></center></html>",
            SwingConstants.CENTER
        );
        lblWelcome.setFont(new Font("Arial", Font.PLAIN, 16));
        
        welcomePanel.add(lblWelcome, BorderLayout.CENTER);
        
        // === ASSEMBLE ===
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(menuPanel, BorderLayout.CENTER);
        mainPanel.add(welcomePanel, BorderLayout.SOUTH);
        
        add(mainPanel);
    }
    
    // 3. NEW METHOD: Enforces role-based access control
    private void applyAccessControl() {
        
        // --- ADMIN ONLY ---
        if (!UserSession.canPerform("MANAGE_USERS")) {
            btnUsers.setEnabled(false);
            btnUsers.setBackground(Color.LIGHT_GRAY);
        }
        
        // --- LIBRARIAN (or ADMIN) ONLY ---
        // Assuming Manage Books, Members, Authors, Genres, Circulation, and Reports require Librarian access.
        boolean canManage = UserSession.canPerform("MANAGE_BOOKS"); // Use one check for all management
        
        if (!canManage) {
            btnBooks.setEnabled(false);
            btnBooks.setBackground(Color.LIGHT_GRAY);
            
            btnMembers.setEnabled(false);
            btnMembers.setBackground(Color.LIGHT_GRAY);
            
            btnAuthors.setEnabled(false);
            btnAuthors.setBackground(Color.LIGHT_GRAY);
            
            btnGenres.setEnabled(false);
            btnGenres.setBackground(Color.LIGHT_GRAY);
            
            btnCirculation.setEnabled(false);
            btnCirculation.setBackground(Color.LIGHT_GRAY);
            
            btnReports.setEnabled(false);
            btnReports.setBackground(Color.LIGHT_GRAY);
        }
        
        // Update the header/welcome message dynamically
        lblUser.setText("User: " + UserSession.getFullName() + 
                        " (" + UserSession.getRoleDisplay() + ")");
        lblWelcome.setText(
            "<html><center><h1>Welcome to Library System!</h1>" +
            "<p>You are logged in as: <b>" + UserSession.getFullName() + "</b> (" + UserSession.getRoleDisplay() + ")</p>" +
            "<p>Click any **enabled** button above to access library features.</p></center></html>"
        );
    }
    
    // Helper method to create styled buttons (Keep existing method)
    private JButton createStyledButton(String text, Color color) {
        // ... (Keep your existing createStyledButton logic) ...
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.PLAIN, 14));
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(color.darker(), 1),
            BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));
        
        // Add hover effect
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(color.brighter());
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(color);
            }
        });
        
        return button;
    }
    
    // Other helper methods (showComingSoon, showHelp, logout) - Keep existing logic
    private void showComingSoon(String feature) {
        JOptionPane.showMessageDialog(this,
            "🚧 " + feature + " - Coming Soon!\n" +
            "This feature is currently under development.\n" +
            "Will be available in the next update.",
            "Coming Soon", JOptionPane.INFORMATION_MESSAGE);
    }
    
    
    private void showHelp() {
        JOptionPane.showMessageDialog(this,
            "<html><div style='padding: 10px;'><h2>📚 Library System Help</h2>" +
            "<p><b>📚 Manage Books:</b> Add, edit, delete library books</p>" +
            "<p><b>👥 Manage Members:</b> Register and manage library members</p>" +
            "<p><b>✍️ Manage Authors:</b> Manage book authors</p>" +
            "<p><b>📖 Book Genres:</b> Manage book categories/genres</p>" +
            "<p><b>🔄 Circulation:</b> Issue, return, and renew books</p>" +
            "<p><b>📊 Reports:</b> Generate library statistics and analytics</p>" +
            "<p><b>👨‍💼 Manage Users:</b> Manage system users (Admin Only)</p>" +
            "<p><b>👤 My Profile:</b> View/edit personal information and change password</p>" +
            "<p><b>⚙️ Settings:</b> System configuration (Coming Soon)</p>" +
            "<br><p><i>Need more help? Contact your system administrator.</i></p></div></html>",
            "Help Guide", JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void logout() {
        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to logout?",
            "Confirm Logout",
            JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            UserSession.logActivity("LOGOUT", "User logged out"); // Log the action
            UserSession.clear(); // Clear the session data
            new LoginForm().setVisible(true);
            this.dispose();
        }
    }
    
    public static void main(String[] args) {
        // Simulate a LIBRARIAN login for testing the new Dashboard
        UserSession.setUserId(2);
        UserSession.setUsername("librarian");
        UserSession.setFullName("Jane Librarian");
        UserSession.setRole("LIBRARIAN");
        
        SwingUtilities.invokeLater(() -> {
            new Dashboard().setVisible(true);
        });
    }
}