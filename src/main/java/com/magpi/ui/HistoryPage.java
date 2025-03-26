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
    private JLabel totalPartsLabel;
    private JLabel acceptedPartsLabel;
    private JLabel rejectedPartsLabel;
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
        totalPartsLabel = new JLabel("Total Parts Tested: 0");
        acceptedPartsLabel = new JLabel("Accepted Parts: 0");
        rejectedPartsLabel = new JLabel("Rejected Parts: 0");
    }
    
    private void setupUI() {
        setLayout(new BorderLayout());
        
        // Create header panel
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        headerPanel.add(new JLabel("Operator: " + session.getOperatorName()));
        headerPanel.add(new JLabel("Machine ID: " + session.getMachineId()));
        headerPanel.add(totalPartsLabel);
        headerPanel.add(acceptedPartsLabel);
        headerPanel.add(rejectedPartsLabel);
        
        add(headerPanel, BorderLayout.NORTH);
        
        // Create tables panel
        JPanel tablesPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        
        // Headshot history panel
        JPanel headshotPanel = new JPanel(new BorderLayout());
        headshotPanel.add(new JLabel("Headshot History", JLabel.LEFT), BorderLayout.NORTH);
        headshotPanel.add(new JScrollPane(headshotHistoryTable), BorderLayout.CENTER);
        tablesPanel.add(headshotPanel);
        
        // Coilshot history panel
        JPanel coilshotPanel = new JPanel(new BorderLayout());
        coilshotPanel.add(new JLabel("Coilshot History", JLabel.LEFT), BorderLayout.NORTH);
        coilshotPanel.add(new JScrollPane(coilshotHistoryTable), BorderLayout.CENTER);
        tablesPanel.add(coilshotPanel);
        
        add(tablesPanel, BorderLayout.CENTER);
        
        // Create controls panel
        JPanel controlsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        
        // Add search controls
        JTextField searchField = new JTextField(15);
        JButton searchButton = new JButton("Search");
        searchButton.addActionListener(e -> performSearch(searchField.getText()));
        
        // Add filter controls
        JComboBox<String> filterComboBox = new JComboBox<>(new String[]{"All", "Accept", "Reject"});
        filterComboBox.addActionListener(e -> filterResults((String) filterComboBox.getSelectedItem()));
        
        // Add export button
        JButton exportButton = new JButton("Export to PDF");
        exportButton.addActionListener(e -> exportToPdf());
        
        // Add view recordings button (placeholder for video functionality)
        JButton viewRecordingsButton = new JButton("View Recordings");
        viewRecordingsButton.addActionListener(e -> viewRecordings());
        
        // Add an end process button
        JButton endProcessButton = new JButton("End Process");
        endProcessButton.setBackground(new Color(255, 102, 102));
        endProcessButton.addActionListener(e -> restartApplication());
        
        // Add components to controls panel
        controlsPanel.add(new JLabel("Search:"));
        controlsPanel.add(searchField);
        controlsPanel.add(searchButton);
        controlsPanel.add(new JLabel("Filter:"));
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
            // Filter on the status column (second to last column)
            final int statusColumnIndex = headshotHistoryTableModel.getColumnCount() - 1;
            filter = new RowFilter<Object, Object>() {
                public boolean include(Entry entry) {
                    return filterOption.equals(entry.getValue(statusColumnIndex));
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
        
        totalPartsLabel.setText("Total Parts Tested: " + totalParts);
        acceptedPartsLabel.setText("Accepted Parts: " + acceptedParts);
        rejectedPartsLabel.setText("Rejected Parts: " + rejectedParts);
    }
    
    private void updateTableRenderers() {
        headshotHistoryTable.setDefaultRenderer(Object.class, 
                new CustomCellRenderer(session.getHeadShotThreshold(), headshotHistoryTableModel));
        coilshotHistoryTable.setDefaultRenderer(Object.class, 
                new CustomCellRenderer(session.getCoilShotThreshold(), coilshotHistoryTableModel));
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