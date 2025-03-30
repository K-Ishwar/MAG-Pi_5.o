package com.magpi.ui;

import com.magpi.model.TestSession;
import com.magpi.ui.table.CustomCellRenderer;
import com.magpi.ui.table.PersistentColorTableModel;
import com.magpi.util.PdfExporter;
import com.magpi.video.RecordedVideosPage;
import com.magpi.video.VLCJVideoStream;

import javax.swing.*;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.regex.Pattern;

/**
 * Panel for displaying historical test data
 */
public class HistoryPage extends JPanel {
    private TestSession session;
    private JTable headshotHistoryTable;
    private JTable coilshotHistoryTable;
    private PersistentColorTableModel headshotHistoryTableModel;
    private PersistentColorTableModel coilshotHistoryTableModel;
//    private JLabel totalPartsLabel;
//    private JLabel acceptedPartsLabel;
//    private JLabel rejectedPartsLabel;
    private RecordedVideosPage recordedVideosPage;

    /**
     * Creates a new history page
     * @param session The test session
     */
    public HistoryPage(TestSession session) {
        this.session = session;
        initializeComponents();
        setupUI();
    }



    private void initializeComponents() {
        // Define column names excluding the action column
        String[] columnNames = {
                "Part No", "Current 1", "T 1", "Current 2", "T 2",
                "Current 3", "T 3", "Current 4", "T 4",
                "Current 5", "T 5", "Status"
        };

        // Initialize table models
        headshotHistoryTableModel = new PersistentColorTableModel(columnNames, 0);
        coilshotHistoryTableModel = new PersistentColorTableModel(columnNames, 0);

        // Initialize tables
        headshotHistoryTable = new JTable(headshotHistoryTableModel);
        coilshotHistoryTable = new JTable(coilshotHistoryTableModel);

        // Set renderers for the tables
        updateTableRenderers();

        // Initialize labels
//        totalPartsLabel = new JLabel("Total Parts Tested: 0");
//        acceptedPartsLabel = new JLabel("Accepted Parts: 0");
//        rejectedPartsLabel = new JLabel("Rejected Parts: 0");
    }

    private void setupUI() {
        setLayout(new BorderLayout());

        // Create header panel with improved styling
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        headerPanel.setBackground(new Color(240, 240, 240));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));

        // Style the labels in the header
        JLabel operatorLabel = new JLabel("Operator: " + session.getOperatorName());
        operatorLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        operatorLabel.setForeground(new Color(44, 62, 80));

        JLabel machineIdLabel = new JLabel("Machine ID: " + session.getMachineId());
        machineIdLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        machineIdLabel.setForeground(new Color(44, 62, 80));

        // Style the counter labels
//        totalPartsLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
//        totalPartsLabel.setForeground(new Color(44, 62, 80));
//
//        acceptedPartsLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
//        acceptedPartsLabel.setForeground(new Color(39, 174, 96)); // Green for accepted
//
//        rejectedPartsLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
//        rejectedPartsLabel.setForeground(new Color(231, 76, 60)); // Red for rejected

        headerPanel.add(operatorLabel);
        headerPanel.add(machineIdLabel);
//        headerPanel.add(totalPartsLabel);
//        headerPanel.add(acceptedPartsLabel);
//        headerPanel.add(rejectedPartsLabel);

        add(headerPanel, BorderLayout.NORTH);

        // Create tables panel with improved styling
        JPanel tablesPanel = new JPanel(new GridLayout(1, 2, 20, 0));
        tablesPanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        tablesPanel.setBackground(new Color(245, 245, 245));

        // Headshot history panel
        JPanel headshotPanel = new JPanel(new BorderLayout(0, 10));
        headshotPanel.setOpaque(false);

        JLabel headshotTitleLabel = new JLabel("Headshot History");
        headshotTitleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        headshotTitleLabel.setForeground(new Color(44, 62, 80));
        headshotTitleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0));

        JScrollPane headshotScrollPane = new JScrollPane(headshotHistoryTable);
        headshotScrollPane.setBorder(BorderFactory.createEmptyBorder());
        headshotScrollPane.getViewport().setBackground(Color.WHITE);

        headshotPanel.add(headshotTitleLabel, BorderLayout.NORTH);
        headshotPanel.add(headshotScrollPane, BorderLayout.CENTER);
        tablesPanel.add(headshotPanel);

        // Coilshot history panel
        JPanel coilshotPanel = new JPanel(new BorderLayout(0, 10));
        coilshotPanel.setOpaque(false);

        JLabel coilshotTitleLabel = new JLabel("Coilshot History");
        coilshotTitleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        coilshotTitleLabel.setForeground(new Color(44, 62, 80));
        coilshotTitleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0));

        JScrollPane coilshotScrollPane = new JScrollPane(coilshotHistoryTable);
        coilshotScrollPane.setBorder(BorderFactory.createEmptyBorder());
        coilshotScrollPane.getViewport().setBackground(Color.WHITE);

        coilshotPanel.add(coilshotTitleLabel, BorderLayout.NORTH);
        coilshotPanel.add(coilshotScrollPane, BorderLayout.CENTER);
        tablesPanel.add(coilshotPanel);

        add(tablesPanel, BorderLayout.CENTER);

        // Create controls panel with improved styling
        JPanel controlsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        controlsPanel.setBackground(new Color(240, 240, 240));
        controlsPanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));

        // Style search controls
        JLabel searchLabel = new JLabel("Search:");
        searchLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        JTextField searchField = new JTextField(15);
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        JButton searchButton = new JButton("Search");
        styleButton(searchButton, new Color(41, 128, 185), Color.WHITE);
        searchButton.addActionListener(e -> performSearch(searchField.getText()));

        // Style filter controls
        JLabel filterLabel = new JLabel("Filter:");
        filterLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        JComboBox<String> filterComboBox = new JComboBox<>(new String[]{"All", "PASS", "FAIL"});
        filterComboBox.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        filterComboBox.setBackground(Color.WHITE);
        filterComboBox.addActionListener(e -> filterResults((String) filterComboBox.getSelectedItem()));

        // Style action buttons
        JButton exportButton = new JButton("Export to PDF");
        styleButton(exportButton, new Color(41, 128, 185), Color.WHITE);
        exportButton.addActionListener(e -> exportToPdf());

        JButton viewRecordingsButton = new JButton("View Recordings");
        styleButton(viewRecordingsButton, new Color(46, 204, 113), Color.WHITE);
        viewRecordingsButton.addActionListener(e -> viewRecordings());

        JButton endProcessButton = new JButton("End Process");
        styleButton(endProcessButton, new Color(231, 76, 60), Color.WHITE);
        endProcessButton.addActionListener(e -> restartApplication());

        // Add components to controls panel
        controlsPanel.add(searchLabel);
        controlsPanel.add(searchField);
        controlsPanel.add(searchButton);
        controlsPanel.add(filterLabel);
        controlsPanel.add(filterComboBox);
        controlsPanel.add(exportButton);
        controlsPanel.add(viewRecordingsButton);
        controlsPanel.add(endProcessButton);

        add(controlsPanel, BorderLayout.SOUTH);
    }

    /**
     * Performs a search across both tables
     * @param searchText The text to search for
     */
    private void performSearch(String searchText) {
        // Create row filter and apply to headshot table
        RowFilter<Object, Object> filter = null;
        if (searchText.length() > 0) {
            try {
                // Create regex-based filter that searches all columns
                filter = RowFilter.regexFilter("(?i)" + Pattern.quote(searchText));
            } catch (java.util.regex.PatternSyntaxException e) {
                // If an error in the regular expression, just return
                return;
            }
        }

        // Apply filter to headshot table
        TableRowSorter<PersistentColorTableModel> headshotSorter =
                new TableRowSorter<>(headshotHistoryTableModel);
        headshotSorter.setRowFilter(filter);
        headshotHistoryTable.setRowSorter(headshotSorter);

        // Apply filter to coilshot table
        TableRowSorter<PersistentColorTableModel> coilshotSorter =
                new TableRowSorter<>(coilshotHistoryTableModel);
        coilshotSorter.setRowFilter(filter);
        coilshotHistoryTable.setRowSorter(coilshotSorter);
    }

    /**
     * Filters the tables by status
     * @param filterOption The status to filter by
     */
    private void filterResults(String filterOption) {
        RowFilter<Object, Object> filter = null;

        if (!"All".equals(filterOption)) {
            // Filter on the status column (last column)
            final int statusColumnIndex = headshotHistoryTableModel.getColumnCount() - 1;
            filter = new RowFilter<Object, Object>() {
                public boolean include(Entry entry) {
                    // Use cell color instead of text for filtering
                    Color cellColor = headshotHistoryTableModel.getCellColor(
                            (int)entry.getIdentifier(), statusColumnIndex);

                    if ("PASS".equals(filterOption)) {
                        return cellColor != null && cellColor.equals(Color.GREEN);
                    } else if ("FAIL".equals(filterOption)) {
                        return cellColor != null && cellColor.equals(Color.ORANGE);
                    }
                    return true;
                }
            };
        }

        // Apply filter to headshot table
        TableRowSorter<PersistentColorTableModel> headshotSorter =
                new TableRowSorter<>(headshotHistoryTableModel);
        headshotSorter.setRowFilter(filter);
        headshotHistoryTable.setRowSorter(headshotSorter);

        // Apply filter to coilshot table
        TableRowSorter<PersistentColorTableModel> coilshotSorter =
                new TableRowSorter<>(coilshotHistoryTableModel);
        coilshotSorter.setRowFilter(filter);
        coilshotHistoryTable.setRowSorter(coilshotSorter);
    }

    /**
     * Exports the session data to PDF
     */
    private void exportToPdf() {
        PdfExporter.exportToPdf(session, headshotHistoryTable, coilshotHistoryTable, this);
    }

    private void viewRecordings() {
        if (recordedVideosPage == null || !recordedVideosPage.isVisible()) {
            recordedVideosPage = new RecordedVideosPage(VLCJVideoStream.saveLocation);
            recordedVideosPage.addWindowListener(new java.awt.event.WindowAdapter() {
                @Override
                public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                    recordedVideosPage.cleanup();
                }
            });
            recordedVideosPage.setVisible(true);
        } else {
            recordedVideosPage.toFront();
        }
    }

    private void restartApplication() {
        int option = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to end the current process and start a new one?",
                "Confirm Restart",
                JOptionPane.YES_NO_OPTION);

        if (option == JOptionPane.YES_OPTION) {
            JFrame topFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
            topFrame.dispose();

            // Restart the application
            SwingUtilities.invokeLater(() -> {
                try {
                    // This assumes you have a Main class with a main method
                    // that creates a new application instance
                    Class.forName("com.magpi.Main").getMethod("main", String[].class)
                            .invoke(null, (Object) new String[0]);
                } catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(null,
                            "Failed to restart the application: " + e.getMessage(),
                            "Restart Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            });
        }
    }

    /**
     * Updates the displayed statistics
     */
    public void updateStatistics() {
        int totalParts = session.getTotalPartsCount();
        int acceptedParts = session.getAcceptedPartsCount();
        int rejectedParts = session.getRejectedPartsCount();
//
//        totalPartsLabel.setText("Total Parts Tested: " + totalParts);
//        acceptedPartsLabel.setText("Accepted Parts: " + acceptedParts);
//        rejectedPartsLabel.setText("Rejected Parts: " + rejectedParts);
    }

    private void updateTableRenderers() {
        headshotHistoryTable.setDefaultRenderer(Object.class,
                new CustomCellRenderer(session.getHeadShotThreshold(), headshotHistoryTableModel));
        coilshotHistoryTable.setDefaultRenderer(Object.class,
                new CustomCellRenderer(session.getCoilShotThreshold(), coilshotHistoryTableModel));

        // Improve table appearance
        styleTable(headshotHistoryTable);
        styleTable(coilshotHistoryTable);
    }

    /**
     * Applies modern styling to the given table
     */
    private void styleTable(JTable table) {
        // Set row height and spacing
        table.setRowHeight(30);
        table.setIntercellSpacing(new Dimension(5, 5));
        table.setShowGrid(true);
        table.setGridColor(new Color(120, 120, 120)); // Darker grid lines
        table.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(100, 100, 100), 2),
                BorderFactory.createEmptyBorder(1, 1, 1, 1)
        ));

        // Style the header
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        table.getTableHeader().setBackground(new Color(230, 230, 230));
        table.getTableHeader().setForeground(new Color(44, 62, 80));
        table.getTableHeader().setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(100, 100, 100), 2),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));

        // Set column widths
        table.getColumnModel().getColumn(0).setPreferredWidth(80); // Part No
        for (int i = 1; i < table.getColumnCount() - 1; i += 2) {
            table.getColumnModel().getColumn(i).setPreferredWidth(100); // Current columns
            table.getColumnModel().getColumn(i + 1).setPreferredWidth(80); // Time columns
        }
        table.getColumnModel().getColumn(table.getColumnCount() - 1).setPreferredWidth(100); // Status

        // Prevent column resizing and reordering
        table.getTableHeader().setResizingAllowed(false);
        table.getTableHeader().setReorderingAllowed(false);

        // Set custom renderer that preserves cell colors
        table.setDefaultRenderer(Object.class, new CustomCellRenderer(
                table == headshotHistoryTable ? session.getHeadShotThreshold() : session.getCoilShotThreshold(),
                (PersistentColorTableModel) table.getModel()) {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {
                JComponent c = (JComponent) super.getTableCellRendererComponent(table, value,
                        isSelected, hasFocus, row, column);

                // Add border to each cell
                c.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 1, 1, new Color(180, 180, 180)),
                        BorderFactory.createEmptyBorder(2, 5, 2, 5)
                ));

                // Add left border for first column
                if (column == 0) {
                    c.setBorder(BorderFactory.createCompoundBorder(
                            BorderFactory.createMatteBorder(0, 1, 1, 1, new Color(180, 180, 180)),
                            BorderFactory.createEmptyBorder(2, 5, 2, 5)
                    ));
                }

                // Format current values (odd columns)
                if (column > 0 && column < table.getColumnCount() - 1 && column % 2 == 1) {
                    if (value instanceof Number) {
                        setText(String.format("%.2f", ((Number) value).doubleValue()));
                    }
                }
                // Format time values (even columns)
                else if (column > 0 && column < table.getColumnCount() - 1 && column % 2 == 0) {
                    if (value instanceof Number) {
                        setText(String.format("%.3f", ((Number) value).doubleValue()));
                    }
                }

                // Center align all cells
                setHorizontalAlignment(javax.swing.SwingConstants.CENTER);

                return c;
            }
        });
    }

    /**
     * Styles a button with the given background and foreground colors
     */
    private void styleButton(JButton button, Color background, Color foreground) {
        button.setBackground(background);
        button.setForeground(foreground);
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    /**
     * Gets the headshot history table model
     * @return The headshot history table model
     */
    public PersistentColorTableModel getHeadshotHistoryTableModel() {
        return headshotHistoryTableModel;
    }

    /**
     * Gets the coilshot history table model
     * @return The coilshot history table model
     */
    public PersistentColorTableModel getCoilshotHistoryTableModel() {
        return coilshotHistoryTableModel;
    }
}
