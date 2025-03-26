package com.magpi.ui;

import com.magpi.model.Measurement;
import com.magpi.model.TestPart;
import com.magpi.model.TestSession;
import com.magpi.ui.table.CustomCellRenderer;
import com.magpi.ui.table.CustomStatusButtonEditor;
import com.magpi.ui.table.JButtonRenderer;
import com.magpi.ui.table.PersistentColorTableModel;
import com.magpi.util.SerialPortManager;
import com.magpi.video.VLCJVideoStream;

import javax.swing.*;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.function.BiConsumer;

/**
 * Panel that displays the measurement tables
 */
public class TablePage extends JPanel {
    private JTable headshotTable;
    private JTable coilshotTable;
    private PersistentColorTableModel headshotTableModel;
    private PersistentColorTableModel coilshotTableModel;
    private JLabel dateLabel;
    private JLabel startTimeLabel;
    private JLabel endTimeLabel;
    private JLabel parametersLabel;
    private TestSession session;
    private SerialPortManager serialPortManager;
    
    // Callback for when a part's status is updated
    private BiConsumer<TableModel, Integer> statusUpdateHandler = (model, row) -> {
        if (model instanceof PersistentColorTableModel) {
            PersistentColorTableModel tableModel = (PersistentColorTableModel) model;
            int partNumber = Integer.parseInt(tableModel.getValueAt(row, 0).toString());
            String status = tableModel.getValueAt(row, tableModel.getColumnCount() - 2).toString();
            
            TestPart part = session.getPartByNumber(partNumber);
            if (part != null) {
                part.setStatus(status);
            }
            
            // Also copy to history table if needed
            if (model == headshotTableModel) {
                copyRowToHistoryTable(tableModel, row, session.getHistoryPanel().getHeadshotHistoryTableModel());
            } else if (model == coilshotTableModel) {
                copyRowToHistoryTable(tableModel, row, session.getHistoryPanel().getCoilshotHistoryTableModel());
            }
        }
    };
    
    /**
     * Creates a new table page
     * @param session The test session
     */
    public TablePage(TestSession session) {
        this.session = session;
        initializeComponents();
        setupUI();
        setupSerialPort();
    }
    
    private void initializeComponents() {
        String[] columnNames = {
                "Part No", "Current 1", "T 1", "Current 2", "T 2",
                "Current 3", "T 3", "Current 4", "T 4",
                "Current 5", "T 5", "Status", "Action"
        };
        
        // Initialize table models
        headshotTableModel = new PersistentColorTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == columnNames.length - 1; // Only Action column is editable
            }
        };
        
        coilshotTableModel = new PersistentColorTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == columnNames.length - 1; // Only Action column is editable
            }
        };
        
        // Initialize tables
        headshotTable = new JTable(headshotTableModel);
        coilshotTable = new JTable(coilshotTableModel);
        
        // Initialize labels
        dateLabel = new JLabel("Date: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        startTimeLabel = new JLabel("Start Time: " + session.getStartTime().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
        endTimeLabel = new JLabel("End Time: Not Ended");
        parametersLabel = new JLabel("Parameters: Not Set");
        
        // Initialize serial port manager
        serialPortManager = new SerialPortManager();
    }
    
    private void setupUI() {
        setLayout(new BorderLayout());
        
        // Set up the header panel
        JPanel headerPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // First row of header
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        headerPanel.add(dateLabel, gbc);
        
        gbc.gridx = 1;
        headerPanel.add(startTimeLabel, gbc);
        
        gbc.gridx = 2;
        headerPanel.add(endTimeLabel, gbc);
        
        // Second row of header
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        headerPanel.add(parametersLabel, gbc);
        
        // Button panel in header
        gbc.gridx = 2;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        
        JButton addPartButton = new JButton("Next Part");
        addPartButton.addActionListener(e -> addNewPart());
        
        JButton videoStreamButton = new JButton("Capture Video");
        videoStreamButton.addActionListener(e -> openVideoStream());
        
        buttonsPanel.add(addPartButton);
        buttonsPanel.add(videoStreamButton);
        headerPanel.add(buttonsPanel, gbc);
        
        add(headerPanel, BorderLayout.NORTH);
        
        // Set up table panel
        JPanel tablesPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        
        // Headshot table panel
        JPanel headshotPanel = createTablePanel(headshotTable, "Headshot Table");
        tablesPanel.add(headshotPanel);
        
        // Coilshot table panel
        JPanel coilshotPanel = createTablePanel(coilshotTable, "Coilshot Table");
        tablesPanel.add(coilshotPanel);
        
        add(tablesPanel, BorderLayout.CENTER);
        
        // Add action buttons at the bottom
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        
        JButton setParametersButton = new JButton("Set Parameters");
        setParametersButton.addActionListener(e -> showParameterDialog());
        setParametersButton.setBackground(new Color(153, 153, 255));
        
        JButton endButton = new JButton("End");
        endButton.addActionListener(e -> endSession());
        endButton.setBackground(new Color(255, 102, 102));
        
        actionPanel.add(setParametersButton);
        actionPanel.add(endButton);
        
        add(actionPanel, BorderLayout.SOUTH);
        
        // Add button columns to tables
        addButtonColumn(headshotTable);
        addButtonColumn(coilshotTable);
        
        // Set renderers for the tables
        updateTableRenderers();
    }
    
    private JPanel createTablePanel(JTable table, String title) {
        JPanel panel = new JPanel(new BorderLayout());
        JLabel titleLabel = new JLabel(title, JLabel.LEFT);
        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }
    
    private void addButtonColumn(JTable table) {
        int actionColumn = table.getColumnCount() - 1;
        TableColumn column = table.getColumnModel().getColumn(actionColumn);
        
        column.setCellRenderer(new JButtonRenderer());
        
        PersistentColorTableModel model = (PersistentColorTableModel) table.getModel();
        column.setCellEditor(new CustomStatusButtonEditor(new JCheckBox(), model, statusUpdateHandler));
    }
    
    private void setupSerialPort() {
        String portName = serialPortManager.detectArduinoPort();
        if (portName == null) {
            JOptionPane.showMessageDialog(this, 
                    "No Arduino port detected. Serial communication will not be available.", 
                    "Port Not Found", 
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        if (!serialPortManager.openConnection(portName)) {
            JOptionPane.showMessageDialog(this, 
                    "Failed to open serial port " + portName, 
                    "Connection Error", 
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Start reading data
        serialPortManager.startReading(this::processMeasurement);
    }
    
    /**
     * Process incoming measurements from the serial port
     * @param measurement The measurement received
     */
    private void processMeasurement(Measurement measurement) {
        SwingUtilities.invokeLater(() -> {
            // Get the current part (most recent part or create new if none exists)
            int currentPartNumber = getCurrentPartNumber();
            TestPart part = session.getPartByNumber(currentPartNumber);
            
            if (part == null) {
                // Create a new part if needed
                part = new TestPart(currentPartNumber, session.getPartDescription());
                session.addPart(part);
                insertNewPartRow(headshotTableModel, currentPartNumber);
                insertNewPartRow(coilshotTableModel, currentPartNumber);
            }
            
            // Add the measurement to the appropriate table
            if ("Headshot".equals(measurement.getMeterType())) {
                part.addHeadshotMeasurement(measurement);
                updateTableWithMeasurement(headshotTableModel, currentPartNumber, 
                        measurement.getCurrent(), measurement.getDuration());
            } else if ("Coilshot".equals(measurement.getMeterType())) {
                part.addCoilshotMeasurement(measurement);
                updateTableWithMeasurement(coilshotTableModel, currentPartNumber, 
                        measurement.getCurrent(), measurement.getDuration());
            }
        });
    }
    
    private int getCurrentPartNumber() {
        if (session.getParts().isEmpty()) {
            return 1; // Start with part number 1
        } else {
            return session.getParts().get(session.getParts().size() - 1).getPartNumber();
        }
    }
    
    private void insertNewPartRow(PersistentColorTableModel tableModel, int partNumber) {
        Object[] rowData = new Object[tableModel.getColumnCount()];
        rowData[0] = partNumber;
        for (int i = 1; i < rowData.length - 2; i++) {
            rowData[i] = "";
        }
        rowData[rowData.length - 2] = ""; // Status column
        rowData[rowData.length - 1] = "Set Status"; // Action column
        
        tableModel.addRow(rowData);
    }
    
    private void updateTableWithMeasurement(PersistentColorTableModel tableModel, 
                                           int partNumber, double current, double duration) {
        // Find the row for this part
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            if (tableModel.getValueAt(i, 0).equals(partNumber)) {
                // Find the first empty current column
                for (int col = 1; col < tableModel.getColumnCount() - 2; col += 2) {
                    if (tableModel.getValueAt(i, col).equals("")) {
                        tableModel.setValueAt(current, i, col);
                        tableModel.setValueAt(String.format("%.3f", duration), i, col + 1);
                        return;
                    }
                }
            }
        }
    }
    
    private void addNewPart() {
        String input = JOptionPane.showInputDialog(this, 
                "Enter Part Number:", 
                "New Part", 
                JOptionPane.QUESTION_MESSAGE);
        
        if (input != null && !input.trim().isEmpty()) {
            try {
                int partNumber = Integer.parseInt(input.trim());
                
                // Check if part already exists
                if (session.getPartByNumber(partNumber) != null) {
                    JOptionPane.showMessageDialog(this, 
                            "Part number " + partNumber + " already exists.", 
                            "Duplicate Part", 
                            JOptionPane.WARNING_MESSAGE);
                    return;
                }
                
                // Create new part
                TestPart part = new TestPart(partNumber, session.getPartDescription());
                session.addPart(part);
                
                // Add to tables
                insertNewPartRow(headshotTableModel, partNumber);
                insertNewPartRow(coilshotTableModel, partNumber);
                
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, 
                        "Please enter a valid number.", 
                        "Invalid Input", 
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void openVideoStream() {
        VLCJVideoStream videoStream = new VLCJVideoStream();
        videoStream.show();
    }
    
    private void showParameterDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), 
                "Set Parameters", true);
        dialog.setLayout(new GridLayout(3, 2, 10, 10));
        dialog.setSize(300, 150);
        
        JTextField headShotField = new JTextField(
                String.valueOf(session.getHeadShotThreshold()));
        JTextField coilShotField = new JTextField(
                String.valueOf(session.getCoilShotThreshold()));
        
        dialog.add(new JLabel("Head Shot Parameter:"));
        dialog.add(headShotField);
        dialog.add(new JLabel("Coil Shot Parameter:"));
        dialog.add(coilShotField);
        
        JButton saveButton = new JButton("Save");
        saveButton.addActionListener(e -> {
            try {
                double headShotValue = Double.parseDouble(headShotField.getText());
                double coilShotValue = Double.parseDouble(coilShotField.getText());
                
                session.setHeadShotThreshold(headShotValue);
                session.setCoilShotThreshold(coilShotValue);
                
                // Update parameter label
                updateParametersLabel();
                
                // Update table renderers with new thresholds
                updateTableRenderers();
                
                dialog.dispose();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, 
                        "Please enter valid numbers for parameters.", 
                        "Invalid Input", 
                        JOptionPane.ERROR_MESSAGE);
            }
        });
        
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> dialog.dispose());
        
        dialog.add(saveButton);
        dialog.add(cancelButton);
        
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }
    
    private void updateParametersLabel() {
        parametersLabel.setText(String.format("Parameters: Headshot (%.2f), Coilshot (%.2f)", 
                session.getHeadShotThreshold(), session.getCoilShotThreshold()));
    }
    
    private void updateTableRenderers() {
        headshotTable.setDefaultRenderer(Object.class, 
                new CustomCellRenderer(session.getHeadShotThreshold(), headshotTableModel));
        coilshotTable.setDefaultRenderer(Object.class, 
                new CustomCellRenderer(session.getCoilShotThreshold(), coilshotTableModel));
    }
    
    private void endSession() {
        session.endSession();
        endTimeLabel.setText("End Time: " + 
                session.getEndTime().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
        
        JOptionPane.showMessageDialog(this, 
                "Session ended. You can now view the history and export reports.", 
                "Session Ended", 
                JOptionPane.INFORMATION_MESSAGE);
    }
    
    /**
     * Copies a row from a source table to a history table
     * @param sourceModel The source table model
     * @param sourceRow The row to copy
     * @param historyModel The destination table model
     */
    private void copyRowToHistoryTable(PersistentColorTableModel sourceModel, 
                                      int sourceRow, 
                                      PersistentColorTableModel historyModel) {
        // Create a new row with the same data
        Object[] rowData = new Object[sourceModel.getColumnCount() - 1]; // Exclude action column
        for (int i = 0; i < sourceModel.getColumnCount() - 1; i++) {
            rowData[i] = sourceModel.getValueAt(sourceRow, i);
        }
        
        // Add the row to the history table
        historyModel.addRow(rowData);
        
        // Copy cell colors
        int historyRow = historyModel.getRowCount() - 1;
        for (int col = 0; col < sourceModel.getColumnCount() - 1; col++) {
            Color cellColor = sourceModel.getCellColor(sourceRow, col);
            if (cellColor != null) {
                historyModel.setCellColor(historyRow, col, cellColor);
            }
        }
    }
    
    /**
     * Shuts down the table page and releases resources
     */
    public void shutdown() {
        if (serialPortManager != null) {
            serialPortManager.stopReading();
            serialPortManager.closeConnection();
        }
    }
} 