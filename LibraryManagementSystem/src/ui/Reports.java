package ui;

import utils.DBConnection;
import utils.UserSession;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.print.PrinterException;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.io.File;
import java.io.FileWriter;

public class Reports extends JFrame {
    
    private JComboBox<String> cmbReportType;
    private JTable table;
    private DefaultTableModel tableModel;
    private JButton btnGenerate, btnPrint, btnExport, btnRefresh;
    private JPanel chartPanel;
    private JLabel lblSummary;
    
    private static final String[] REPORT_TYPES = {
        "Monthly Borrowing Report",
        "Popular Books Report", 
        "Member Activity Report",
        "Daily Transactions",
        "Genre Distribution",
        "Overdue Books Report",
        "Revenue Summary",
        "Library Statistics"
    };
    
    public Reports() {
        setTitle("📊 Library Reports & Analytics - " + UserSession.getFullName());
        setSize(1300, 750);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        
        createUI();
        checkPermissions();
        loadDefaultReport();
    }
    
    private void createUI() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // ===== TOP CONTROL PANEL =====
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        controlPanel.setBorder(BorderFactory.createTitledBorder("Report Controls"));
        
        controlPanel.add(new JLabel("Report Type:"));
        cmbReportType = new JComboBox<>(REPORT_TYPES);
        cmbReportType.setPreferredSize(new Dimension(200, 30));
        controlPanel.add(cmbReportType);
        
        btnGenerate = createStyledButton("📈 Generate Report", new Color(0, 123, 255));
        btnPrint = createStyledButton("🖨️ Print", new Color(108, 117, 125));
        btnExport = createStyledButton("💾 Export CSV", new Color(40, 167, 69));
        btnRefresh = createStyledButton("🔄 Refresh", new Color(255, 193, 7));
        
        controlPanel.add(btnGenerate);
        controlPanel.add(btnPrint);
        controlPanel.add(btnExport);
        controlPanel.add(btnRefresh);
        
        // ===== SUMMARY PANEL =====
        lblSummary = new JLabel("Select a report type and click Generate");
        lblSummary.setFont(new Font("Arial", Font.BOLD, 14));
        lblSummary.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // ===== TABLE PANEL =====
        tableModel = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(tableModel);
        table.setRowHeight(25);
        table.setFont(new Font("Arial", Font.PLAIN, 12));
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Report Data"));
        
        // ===== CHART PANEL =====
        chartPanel = new JPanel();
        chartPanel.setBorder(BorderFactory.createTitledBorder("Visualization"));
        chartPanel.setPreferredSize(new Dimension(300, 0));
        chartPanel.setBackground(Color.WHITE);
        
        // ===== ASSEMBLE LAYOUT =====
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, scrollPane, chartPanel);
        splitPane.setResizeWeight(0.7);
        splitPane.setDividerLocation(900);
        
        mainPanel.add(controlPanel, BorderLayout.NORTH);
        mainPanel.add(lblSummary, BorderLayout.CENTER);
        mainPanel.add(splitPane, BorderLayout.SOUTH);
        
        add(mainPanel);
        
        // ===== EVENT LISTENERS =====
        btnGenerate.addActionListener(e -> generateReport());
        btnPrint.addActionListener(e -> printReport());
        btnExport.addActionListener(e -> exportToCSV());
        btnRefresh.addActionListener(e -> loadDefaultReport());
    }
    
    // ----------------- PERMISSION CHECK -----------------
    private void checkPermissions() {
        if (!UserSession.canPerform("REPORT")) {
            JOptionPane.showMessageDialog(this,
                    "You do not have permission to view reports.",
                    "Access Denied", JOptionPane.ERROR_MESSAGE);
            btnGenerate.setEnabled(false);
            btnPrint.setEnabled(false);
            btnExport.setEnabled(false);
        }
    }
    
    private JButton createStyledButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Arial", Font.BOLD, 12));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        return button;
    }
    
    private void loadDefaultReport() {
        generateReport();
    }
    
    private void generateReport() {
        String reportType = (String) cmbReportType.getSelectedItem();
        
        try {
            Connection conn = DBConnection.getConnection();
            
            switch (reportType) {
                case "Monthly Borrowing Report":
                    generateMonthlyReport(conn);
                    break;
                case "Popular Books Report":
                    generatePopularBooksReport(conn);
                    break;
                case "Member Activity Report":
                    generateMemberActivityReport(conn);
                    break;
                case "Daily Transactions":
                    generateDailyTransactionsReport(conn);
                    break;
                case "Genre Distribution":
                    generateGenreReport(conn);
                    break;
                case "Overdue Books Report":
                    generateOverdueReport(conn);
                    break;
                case "Revenue Summary":
                    generateRevenueReport(conn);
                    break;
                case "Library Statistics":
                    generateLibraryStats(conn);
                    break;
            }
            
            conn.close();
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error generating report: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void generateMonthlyReport(Connection conn) throws SQLException {
        String sql = "SELECT DATE_FORMAT(issue_date, '%Y-%m') AS month_year, " +
                    "COUNT(*) AS total_borrowed, " +
                    "SUM(CASE WHEN status = 'RETURNED' THEN 1 ELSE 0 END) AS returned_count, " +
                    "SUM(CASE WHEN status = 'OVERDUE' THEN 1 ELSE 0 END) AS overdue_count, " +
                    "COALESCE(SUM(fine_amount), 0) AS total_fines, " +
                    "SUM(CASE WHEN return_date IS NOT NULL THEN fine_amount ELSE 0 END) AS collected_fines " +
                    "FROM circulation " +
                    "GROUP BY DATE_FORMAT(issue_date, '%Y-%m') " +
                    "ORDER BY month_year DESC";
        
        String[] columns = {"Month", "Total Borrowed", "Returned", "Overdue", "Total Fines", "Collected Fines"};
        
        tableModel.setColumnIdentifiers(columns);
        tableModel.setRowCount(0);
        
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(sql);
        
        double totalFines = 0;
        double collectedFines = 0;
        int totalBorrowed = 0;
        
        while (rs.next()) {
            Object[] row = {
                rs.getString("month_year"),
                rs.getInt("total_borrowed"),
                rs.getInt("returned_count"),
                rs.getInt("overdue_count"),
                String.format("$%.2f", rs.getDouble("total_fines")),
                String.format("$%.2f", rs.getDouble("collected_fines"))
            };
            tableModel.addRow(row);
            
            totalBorrowed += rs.getInt("total_borrowed");
            totalFines += rs.getDouble("total_fines");
            collectedFines += rs.getDouble("collected_fines");
        }
        
        lblSummary.setText(String.format(
            "📅 Monthly Report Summary | Total Books Borrowed: %d | Total Fines: $%.2f | Collected: $%.2f",
            totalBorrowed, totalFines, collectedFines
        ));
        
        // Create fresh result set for chart
        ResultSet chartData = stmt.executeQuery(sql);
        updateChart("Monthly Borrowing Trends", chartData, "month_year", "total_borrowed");
        
        rs.close();
        chartData.close();
        stmt.close();
    }
    
    private void generatePopularBooksReport(Connection conn) throws SQLException {
        String sql = "SELECT b.id, b.title, b.author, b.genre, " +
                    "COUNT(c.id) AS times_borrowed, b.quantity, b.available_quantity " +
                    "FROM books b " +
                    "LEFT JOIN circulation c ON b.id = c.book_id " +
                    "GROUP BY b.id, b.title, b.author, b.genre, b.quantity, b.available_quantity " +
                    "ORDER BY times_borrowed DESC " +
                    "LIMIT 20";
        
        String[] columns = {"Book ID", "Title", "Author", "Genre", "Times Borrowed", "Total Copies", "Available Copies"};
        
        tableModel.setColumnIdentifiers(columns);
        tableModel.setRowCount(0);
        
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(sql);
        
        while (rs.next()) {
            Object[] row = {
                rs.getInt("id"),
                rs.getString("title"),
                rs.getString("author"),
                rs.getString("genre"),
                rs.getInt("times_borrowed"),
                rs.getInt("quantity"),
                rs.getInt("available_quantity")
            };
            tableModel.addRow(row);
        }
        
        lblSummary.setText("📚 Most Popular Books - Top 20 Most Borrowed Titles");
        
        // Re-execute query for chart
        rs.close();
        ResultSet chartRs = stmt.executeQuery(sql);
        showSimpleChart("Top 5 Popular Books", chartRs, "title", "times_borrowed");
        chartRs.close();
        stmt.close();
    }
    
    private void generateMemberActivityReport(Connection conn) throws SQLException {
        String sql = "SELECT u.id, u.full_name, u.username, u.email, u.phone, " +
                    "COUNT(c.id) AS total_borrowed, " +
                    "SUM(CASE WHEN c.status = 'Late' THEN 1 ELSE 0 END) AS overdue_books, " +
                    "COALESCE(SUM(c.fine_amount), 0) AS total_fines, " +
                    "SUM(CASE WHEN c.return_date IS NOT NULL THEN c.fine_amount ELSE 0 END) AS paid_fines " +
                    "FROM users u " +
                    "LEFT JOIN circulation c ON u.id = c.member_id " +
                    "WHERE u.role = 'member' " +
                    "GROUP BY u.id, u.full_name, u.username, u.email, u.phone " +
                    "ORDER BY total_borrowed DESC";
        
        String[] columns = {"Member ID", "Name", "Username", "Email", "Phone", "Total Borrowed", "Overdue", "Total Fines", "Paid Fines"};
        
        tableModel.setColumnIdentifiers(columns);
        tableModel.setRowCount(0);
        
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(sql);
        
        int activeMembers = 0;
        int totalOverdue = 0;
        
        while (rs.next()) {
            Object[] row = {
                rs.getInt("id"),
                rs.getString("full_name"),
                rs.getString("username"),
                rs.getString("email"),
                rs.getString("phone"),
                rs.getInt("total_borrowed"),
                rs.getInt("overdue_books"),
                String.format("$%.2f", rs.getDouble("total_fines")),
                String.format("$%.2f", rs.getDouble("paid_fines"))
            };
            tableModel.addRow(row);
            
            if (rs.getInt("total_borrowed") > 0) activeMembers++;
            totalOverdue += rs.getInt("overdue_books");
        }
        
        lblSummary.setText(String.format(
            "👥 Member Activity | Active Members: %d | Total Overdue Books: %d",
            activeMembers, totalOverdue
        ));
        
        rs.close();
        stmt.close();
    }
    
    private void generateDailyTransactionsReport(Connection conn) throws SQLException {
        String sql = "SELECT DATE(issue_date) AS transaction_date, " +
                    "COUNT(*) AS total_transactions, " +
                    "SUM(CASE WHEN status = 'Issued' THEN 1 ELSE 0 END) AS books_issued, " +
                    "SUM(CASE WHEN status = 'Returned' THEN 1 ELSE 0 END) AS books_returned, " +
                    "COALESCE(SUM(fine_amount), 0) AS daily_fines " +
                    "FROM circulation " +
                    "GROUP BY DATE(issue_date) " +
                    "ORDER BY transaction_date DESC " +
                    "LIMIT 30";
        
        String[] columns = {"Date", "Total Transactions", "Books Issued", "Books Returned", "Daily Fines"};
        
        tableModel.setColumnIdentifiers(columns);
        tableModel.setRowCount(0);
        
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(sql);
        
        while (rs.next()) {
            Object[] row = {
                rs.getDate("transaction_date"),
                rs.getInt("total_transactions"),
                rs.getInt("books_issued"),
                rs.getInt("books_returned"),
                String.format("$%.2f", rs.getDouble("daily_fines"))
            };
            tableModel.addRow(row);
        }
        
        lblSummary.setText("📅 Daily Transactions - Last 30 Days");
        
        rs.close();
        stmt.close();
    }
    
    private void generateGenreReport(Connection conn) throws SQLException {
        String sql = "SELECT genre, " +
                    "COUNT(id) AS book_count, " +
                    "COALESCE(SUM(quantity), 0) AS total_copies, " +
                    "COALESCE(SUM(available_quantity), 0) AS available_copies " +
                    "FROM books " +
                    "GROUP BY genre " +
                    "ORDER BY book_count DESC";
        
        String[] columns = {"Genre", "Book Count", "Total Copies", "Available Copies"};
        
        tableModel.setColumnIdentifiers(columns);
        tableModel.setRowCount(0);
        
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(sql);
        
        while (rs.next()) {
            Object[] row = {
                rs.getString("genre"),
                rs.getInt("book_count"),
                rs.getInt("total_copies"),
                rs.getInt("available_copies")
            };
            tableModel.addRow(row);
        }
        
        lblSummary.setText("📚 Genre Distribution - Books by Category");
        
        rs.close();
        stmt.close();
    }
    
    private void generateOverdueReport(Connection conn) throws SQLException {
        String sql = "SELECT c.id, b.title, u.full_name AS member_name, " +
                    "u.email AS member_email, u.phone AS member_phone, " +
                    "c.issue_date, c.return_date, " +
                    "DATEDIFF(CURDATE(), c.return_date) AS days_overdue, " +
                    "COALESCE(c.fine_amount, 0) AS fine_amount, c.status " +
                    "FROM circulation c " +
                    "JOIN books b ON c.book_id = b.id " +
                    "JOIN users u ON c.member_id = u.id " +
                    "WHERE (c.status = 'Late' OR (c.status = 'Issued' AND c.return_date < CURDATE())) " +
                    "ORDER BY days_overdue DESC";
        
        String[] columns = {"Transaction ID", "Book Title", "Member Name", "Email", "Phone", 
                           "Issue Date", "Return Date", "Days Overdue", "Fine Amount", "Status"};
        
        tableModel.setColumnIdentifiers(columns);
        tableModel.setRowCount(0);
        
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(sql);
        
        int totalOverdue = 0;
        double totalFines = 0;
        
        while (rs.next()) {
            Object[] row = {
                rs.getInt("id"),
                rs.getString("title"),
                rs.getString("member_name"),
                rs.getString("member_email"),
                rs.getString("member_phone"),
                rs.getDate("issue_date"),
                rs.getDate("return_date"),
                rs.getInt("days_overdue"),
                String.format("$%.2f", rs.getDouble("fine_amount")),
                rs.getString("status")
            };
            tableModel.addRow(row);
            
            totalOverdue++;
            totalFines += rs.getDouble("fine_amount");
        }
        
        lblSummary.setText(String.format(
            "⚠️ Overdue Books Alert | Total: %d books overdue | Total Fines Due: $%.2f",
            totalOverdue, totalFines
        ));
        
        rs.close();
        stmt.close();
    }
    
    private void generateRevenueReport(Connection conn) throws SQLException {
        String sql = "SELECT YEAR(return_date) AS year, MONTH(return_date) AS month, " +
                    "COUNT(*) AS books_returned, " +
                    "COALESCE(SUM(fine_amount), 0) AS total_revenue " +
                    "FROM circulation " +
                    "WHERE return_date IS NOT NULL AND fine_amount > 0 " +
                    "GROUP BY YEAR(return_date), MONTH(return_date) " +
                    "ORDER BY year DESC, month DESC";
        
        String[] columns = {"Year", "Month", "Books Returned", "Total Revenue"};
        
        tableModel.setColumnIdentifiers(columns);
        tableModel.setRowCount(0);
        
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(sql);
        
        double totalRevenue = 0;
        
        while (rs.next()) {
            Object[] row = {
                rs.getInt("year"),
                getMonthName(rs.getInt("month")),
                rs.getInt("books_returned"),
                String.format("$%.2f", rs.getDouble("total_revenue"))
            };
            tableModel.addRow(row);
            totalRevenue += rs.getDouble("total_revenue");
        }
        
        lblSummary.setText(String.format("💰 Revenue Summary | Total Revenue: $%.2f", totalRevenue));
        
        rs.close();
        stmt.close();
    }
    
    private String getMonthName(int month) {
        String[] months = {"January", "February", "March", "April", "May", "June",
                          "July", "August", "September", "October", "November", "December"};
        return month >= 1 && month <= 12 ? months[month - 1] : "Unknown";
    }
    
    private void generateLibraryStats(Connection conn) throws SQLException {
        String[] columns = {"Statistic", "Value"};
        tableModel.setColumnIdentifiers(columns);
        tableModel.setRowCount(0);
        
        // Get all statistics
        String[] queries = {
            "SELECT 'Total Books' AS stat, COALESCE(SUM(quantity), 0) AS value FROM books",
            "SELECT 'Available Books' AS stat, COALESCE(SUM(available_quantity), 0) AS value FROM books",
            "SELECT 'Total Members' AS stat, COUNT(*) AS value FROM users WHERE role='member'",
            "SELECT 'Active Borrowings' AS stat, COUNT(*) AS value FROM circulation WHERE status='Issued'",
            "SELECT 'Overdue Books' AS stat, COUNT(*) AS value FROM circulation WHERE status='Late'",
            "SELECT 'Total Revenue' AS stat, CONCAT('$', FORMAT(COALESCE(SUM(fine_amount), 0), 2)) AS value FROM circulation WHERE return_date IS NOT NULL",
            "SELECT 'Most Popular Genre' AS stat, COALESCE((SELECT genre FROM books WHERE id IN (SELECT book_id FROM circulation GROUP BY book_id ORDER BY COUNT(*) DESC LIMIT 1)), 'N/A') AS value",
            "SELECT 'Busiest Month' AS stat, COALESCE((SELECT DATE_FORMAT(issue_date, '%M %Y') FROM circulation GROUP BY DATE_FORMAT(issue_date, '%Y-%m') ORDER BY COUNT(*) DESC LIMIT 1), 'N/A') AS value"
        };
        
        for (String query : queries) {
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(query)) {
                if (rs.next()) {
                    tableModel.addRow(new Object[]{rs.getString("stat"), rs.getString("value")});
                }
            } catch (SQLException e) {
                tableModel.addRow(new Object[]{query.substring(query.indexOf("'") + 1, query.indexOf("' AS")), "Error"});
            }
        }
        
        lblSummary.setText("📊 Library Dashboard - Real-time Statistics");
        
        // Draw a simple bar chart
        drawStatsChart(conn);
    }
    
    private void updateChart(String title, ResultSet data, String xColumn, String yColumn) {
        chartPanel.removeAll();
        chartPanel.setLayout(new BorderLayout());
        
        JTextArea chartText = new JTextArea();
        chartText.setEditable(false);
        chartText.setFont(new Font("Monospaced", Font.PLAIN, 12));
        
        StringBuilder chartBuilder = new StringBuilder();
        chartBuilder.append(title).append("\n");
        
        // Create separator line
        for (int i = 0; i < 40; i++) {
            chartBuilder.append("=");
        }
        chartBuilder.append("\n");
        
        try {
            // Process data for chart
            while (data.next()) {
                try {
                    String xValue = data.getString(xColumn);
                    double yValue = data.getDouble(yColumn);
                    int bars = (int) (yValue / 5); // Scale for display
                    
                    // Build bar string
                    StringBuilder barString = new StringBuilder();
                    for (int i = 0; i < Math.max(0, bars); i++) {
                        barString.append("█");
                    }
                    
                    chartBuilder.append(String.format("%-15s: %s (%.0f)\n", 
                        xValue, barString.toString(), yValue));
                } catch (SQLException e) {
                    chartBuilder.append("Error reading data row\n");
                }
            }
        } catch (SQLException e) {
            chartBuilder.append("Error displaying chart: ").append(e.getMessage());
        }
        
        chartText.setText(chartBuilder.toString());
        chartPanel.add(new JScrollPane(chartText), BorderLayout.CENTER);
        chartPanel.revalidate();
        chartPanel.repaint();
    }
    
    private void showSimpleChart(String title, ResultSet data, String xColumn, String yColumn) {
        chartPanel.removeAll();
        chartPanel.setLayout(new BorderLayout());
        
        JTextArea chartText = new JTextArea();
        chartText.setEditable(false);
        chartText.setFont(new Font("Monospaced", Font.PLAIN, 12));
        
        StringBuilder chartBuilder = new StringBuilder();
        chartBuilder.append(title).append("\n");
        
        // Create separator line
        for (int i = 0; i < 40; i++) {
            chartBuilder.append("=");
        }
        chartBuilder.append("\n");
        
        int count = 0;
        try {
            data.beforeFirst(); // Reset cursor
            while (data.next() && count < 5) { // Show top 5 only
                String xValue = data.getString(xColumn);
                double yValue = data.getDouble(yColumn);
                
                // Build bar string
                int bars = (int) (yValue / 2); // Scale for display
                StringBuilder barString = new StringBuilder();
                for (int i = 0; i < Math.max(0, bars); i++) {
                    barString.append("█");
                }
                
                chartBuilder.append(String.format("%-30s: %s (%.0f)\n", 
                    truncateString(xValue, 30), barString.toString(), yValue));
                count++;
            }
        } catch (SQLException e) {
            chartBuilder.append("Error creating chart: ").append(e.getMessage());
        }
        
        if (count == 0) {
            chartBuilder.append("No data available for chart\n");
        }
        
        chartText.setText(chartBuilder.toString());
        chartPanel.add(new JScrollPane(chartText), BorderLayout.CENTER);
        chartPanel.revalidate();
        chartPanel.repaint();
    }
    
    // Helper method to truncate long strings
    private String truncateString(String str, int maxLength) {
        if (str == null || str.length() <= maxLength) {
            return str;
        }
        return str.substring(0, maxLength - 3) + "...";
    }
    
    private void drawStatsChart(Connection conn) throws SQLException {
        chartPanel.removeAll();
        chartPanel.setLayout(new GridLayout(0, 1, 5, 5));
        
        String[] stats = {"Total Books", "Available Books", "Active Members", "Overdue Books"};
        String[] queries = {
            "SELECT COALESCE(SUM(quantity), 0) FROM books",
            "SELECT COALESCE(SUM(available_quantity), 0) FROM books",
            "SELECT COUNT(*) FROM users WHERE role='member'",
            "SELECT COUNT(*) FROM circulation WHERE status='Late'"
        };
        
        for (int i = 0; i < stats.length; i++) {
            JPanel barPanel = new JPanel(new BorderLayout());
            JLabel label = new JLabel(stats[i] + ":");
            label.setFont(new Font("Arial", Font.PLAIN, 12));
            
            // Get value for this stat
            int value = 0;
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(queries[i])) {
                if (rs.next()) value = rs.getInt(1);
            } catch (SQLException e) {
                value = 0;
            }
            
            // Create bar
            int barWidth = Math.min(value * 2, 250);
            JPanel bar = new JPanel();
            bar.setBackground(new Color(70, 130, 180));
            bar.setPreferredSize(new Dimension(barWidth, 20));
            bar.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));
            
            JLabel valueLabel = new JLabel(String.valueOf(value));
            valueLabel.setForeground(Color.BLACK);
            
            barPanel.add(label, BorderLayout.WEST);
            barPanel.add(bar, BorderLayout.CENTER);
            barPanel.add(valueLabel, BorderLayout.EAST);
            
            chartPanel.add(barPanel);
        }
        
        chartPanel.revalidate();
        chartPanel.repaint();
    }
    
    private void printReport() {
        try {
            boolean complete = table.print();
            if (complete) {
                JOptionPane.showMessageDialog(this, "✅ Report printed successfully!");
            } else {
                JOptionPane.showMessageDialog(this, "Printing cancelled");
            }
        } catch (PrinterException e) {
            JOptionPane.showMessageDialog(this, "Error printing: " + e.getMessage());
        }
    }
    
    private void exportToCSV() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Export Report as CSV");
        fileChooser.setSelectedFile(new File("library_report_" + 
            LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".csv"));
        
        int userSelection = fileChooser.showSaveDialog(this);
        
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            
            try (FileWriter writer = new FileWriter(file)) {
                // Write headers
                for (int i = 0; i < tableModel.getColumnCount(); i++) {
                    writer.write("\"" + tableModel.getColumnName(i) + "\"");
                    if (i < tableModel.getColumnCount() - 1) {
                        writer.write(",");
                    }
                }
                writer.write("\n");
                
                // Write data
                for (int row = 0; row < tableModel.getRowCount(); row++) {
                    for (int col = 0; col < tableModel.getColumnCount(); col++) {
                        Object value = tableModel.getValueAt(row, col);
                        if (value != null) {
                            String valueStr = value.toString().replace("\"", "\"\"");
                            writer.write("\"" + valueStr + "\"");
                        } else {
                            writer.write("\"\"");
                        }
                        if (col < tableModel.getColumnCount() - 1) {
                            writer.write(",");
                        }
                    }
                    writer.write("\n");
                }
                
                JOptionPane.showMessageDialog(this, 
                    "✅ Report exported successfully to:\n" + file.getAbsolutePath());
                
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error exporting: " + e.getMessage());
            }
        }
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new Reports().setVisible(true);
        });
    }
}