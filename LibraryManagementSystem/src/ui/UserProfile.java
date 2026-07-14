package ui;

import javax.swing.*;
import java.awt.*;
import utils.UserSession;

public class UserProfile extends JFrame {

    public UserProfile() {

        setTitle("User Profile");
        setSize(450, 450);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.WHITE);

        // ==== HEADER ====
        JPanel header = new JPanel();
        header.setBackground(new Color(0, 102, 204));
        header.setPreferredSize(new Dimension(450, 60));

        JLabel lblTitle = new JLabel("👤 User Profile");
        lblTitle.setFont(new Font("Arial", Font.BOLD, 22));
        lblTitle.setForeground(Color.WHITE);

        header.add(lblTitle);

        // ==== CONTENT ====
        JPanel content = new JPanel(null);
        content.setBackground(Color.WHITE);

        // Labels and fields
        JLabel lblFullName = new JLabel("Full Name:");
        JLabel lblUsername = new JLabel("Username:");
        JLabel lblRole = new JLabel("Role:");
        JLabel lblEmail = new JLabel("Email:");

        JLabel txtFullName = new JLabel(UserSession.getFullName());
        JLabel txtUsername = new JLabel(UserSession.getUsername());
        JLabel txtRole = new JLabel(UserSession.getRole());
        JLabel txtEmail = new JLabel(UserSession.getEmail());

        Font labelFont = new Font("Arial", Font.PLAIN, 14);
        lblFullName.setFont(labelFont);
        lblUsername.setFont(labelFont);
        lblRole.setFont(labelFont);
        lblEmail.setFont(labelFont);

        txtFullName.setFont(labelFont);
        txtUsername.setFont(labelFont);
        txtRole.setFont(labelFont);
        txtEmail.setFont(labelFont);

        // Positioning
        lblFullName.setBounds(40, 40, 120, 25);
        txtFullName.setBounds(160, 40, 220, 25);

        lblUsername.setBounds(40, 90, 120, 25);
        txtUsername.setBounds(160, 90, 220, 25);

        lblRole.setBounds(40, 140, 120, 25);
        txtRole.setBounds(160, 140, 220, 25);

        lblEmail.setBounds(40, 190, 120, 25);
        txtEmail.setBounds(160, 190, 220, 25);

        content.add(lblFullName);
        content.add(txtFullName);
        content.add(lblUsername);
        content.add(txtUsername);
        content.add(lblRole);
        content.add(txtRole);
        content.add(lblEmail);
        content.add(txtEmail);

        // ==== CLOSE BUTTON ====
        JButton btnClose = new JButton("Close");
        btnClose.setFont(new Font("Arial", Font.BOLD, 14));
        btnClose.setBackground(new Color(220, 53, 69));
        btnClose.setForeground(Color.WHITE);
        btnClose.setFocusPainted(false);
        btnClose.setBounds(150, 260, 140, 35);

        btnClose.addActionListener(e -> dispose());
        content.add(btnClose);

        // Add panels
        mainPanel.add(header, BorderLayout.NORTH);
        mainPanel.add(content, BorderLayout.CENTER);

        add(mainPanel);
        setVisible(true);
    }

    public static void main(String[] args) {

        // test run
        UserSession.setUserId(1);
        UserSession.setUsername("admin");
        UserSession.setFullName("System Administrator");
        UserSession.setRole("ADMIN");
        UserSession.setEmail("admin@gmail.com");

        new UserProfile().setVisible(true);
    }
}


