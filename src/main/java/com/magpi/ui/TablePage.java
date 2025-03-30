package com.magpi.ui;

import com.magpi.model.Measurement;
import com.magpi.model.TestPart;
import com.magpi.model.TestSession;
import com.magpi.ui.table.CustomCellRenderer;
import com.magpi.ui.table.PersistentColorTableModel;
import com.magpi.util.SerialPortManager;
import com.magpi.video.VLCJVideoStream;
import com.magpi.util.PersistentLibrary;

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
    private JLabel Part_D;
    private TestSession session;
    private SerialPortManager serialPortManager;

    /**
     * Creates a new table page
     * @param session The test session
     */
    public TablePage(TestSession session) {
        this.session = session;
        initializeComponents();
        setupUI();
        setupSerialPort();
        updateParameters(); // Update parameter display
    }

    private void initializeComponents() {
        String[] columnNames = {
                "Part No", "Current 1", "T 1", "Current 2", "T 2",
                "Current 3", "T 3", "Current 4", "T 4",
                "Current 5", "T 5", "Status"
        };

        // Initialize table models
        headshotTableModel = new PersistentColorTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make all cells non-editable
            }
        };

        coilshotTableModel = new PersistentColorTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make all cells non-editable
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
        parametersLabel = new JLabel("Parameters: Not Set");
        Part_D = new JLabel("Part: "+session.getPartDescription());

        // Initialize serial port manager
        serialPortManager = new SerialPortManager();
    }

    private void setupUI() {
        setLayout(new BorderLayout());

        // Set up the header panel with a more modern look
        JPanel headerPanel = new JPanel(new GridBagLayout());
        headerPanel.setBackground(new Color(240, 240, 240));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        // First row of header - Date and time info
        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 0));
        infoPanel.setOpaque(false);
        infoPanel.add(createStyledLabel(dateLabel, new Font("Segoe UI", Font.PLAIN, 14)));
        infoPanel.add(createStyledLabel(startTimeLabel, new Font("Segoe UI", Font.PLAIN, 14)));
        infoPanel.add(createStyledLabel(endTimeLabel, new Font("Segoe UI", Font.PLAIN, 14)));

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        gbc.gridwidth = 3;
        headerPanel.add(infoPanel, gbc);

        // Reset gridwidth
        gbc.gridwidth = 1;

        // Second row of header - Parameters and part info
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0.7;
        parametersLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        headerPanel.add(parametersLabel, gbc);

        // Part description
        gbc.gridx = 1;
        gbc.weightx = 0.3;
        Part_D.setFont(new Font("Segoe UI", Font.BOLD, 14));
        headerPanel.add(Part_D, gbc);

        // Button panel in header
        gbc.gridx = 2;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.weightx = 0.3;
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonsPanel.setOpaque(false);

        // Next Part button with modern styling
        JButton addPartButton = new JButton("Next Part");
        styleButton(addPartButton, new Color(41, 128, 185), Color.WHITE);
        addPartButton.addActionListener(e -> addNewPart());
        addPartButton.setPreferredSize(new Dimension(180, 40));

        // Video capture button with modern styling
        JButton videoStreamButton = new JButton("Capture Video");
        styleButton(videoStreamButton, new Color(46, 204, 113), Color.WHITE);
        videoStreamButton.addActionListener(e -> openVideoStream());
        videoStreamButton.setPreferredSize(new Dimension(150, 40));

        buttonsPanel.add(addPartButton);
        buttonsPanel.add(videoStreamButton);
        headerPanel.add(buttonsPanel, gbc);

        add(headerPanel, BorderLayout.NORTH);

        // Set up table panel with more padding and modern styling
        JPanel tablesPanel = new JPanel(new GridLayout(1, 2, 20, 0));
        tablesPanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        tablesPanel.setBackground(new Color(230, 230, 230));

        // Headshot table panel
        JPanel headshotPanel = createTablePanel(headshotTable, "Headshot Table");
        tablesPanel.add(headshotPanel);

        // Coilshot table panel
        JPanel coilshotPanel = createTablePanel(coilshotTable, "Coilshot Table");
        tablesPanel.add(coilshotPanel);

        add(tablesPanel, BorderLayout.CENTER);

        // Add action buttons at the bottom with modern styling
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 15));
        actionPanel.setBackground(new Color(255, 255, 255));

        JButton endButton = new JButton("End Session");
        styleButton(endButton, new Color(231, 76, 60), Color.WHITE);
        endButton.addActionListener(e -> endSession());
        endButton.setPreferredSize(new Dimension(150, 40));
        actionPanel.add(endButton);

        add(actionPanel, BorderLayout.SOUTH);

        // Set renderers for the tables
        updateTableRenderers();

        // Pre-color the status column cells
        initializeStatusColumn(headshotTable);
        initializeStatusColumn(coilshotTable);
    }

    /**
     * Creates a styled JPanel containing a table with a header
     */
    private JPanel createTablePanel(JTable table, String title) {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setOpaque(false);

        // Create a styled title label
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(new Color(44, 62, 80));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

        // Style the table
        styleTable(table);

        // Create a custom scrollpane with padding and border
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(180, 180, 180)),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        scrollPane.getViewport().setBackground(Color.WHITE);

        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    /**
     * Styles a JLabel with the given font
     */
    private JLabel createStyledLabel(JLabel label, Font font) {
        label.setFont(font);
        label.setForeground(new Color(44, 62, 80));
        return label;
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
     * Pre-colors the status column cells in the table
     */
    private void initializeStatusColumn(JTable table) {
        PersistentColorTableModel model = (PersistentColorTableModel) table.getModel();
        int statusColumn = model.getColumnCount() - 1;

        for (int row = 0; row < model.getRowCount(); row++) {
            // Default color is light gray (neutral)
            model.setCellColor(row, statusColumn, new Color(224, 224, 224));
        }
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
                updateStatusColor(headshotTableModel, currentPartNumber);
            } else if ("Coilshot".equals(measurement.getMeterType())) {
                part.addCoilshotMeasurement(measurement);
                updateTableWithMeasurement(coilshotTableModel, currentPartNumber,
                        measurement.getCurrent(), measurement.getDuration());
                updateStatusColor(coilshotTableModel, currentPartNumber);
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
        for (int i = 1; i < rowData.length - 1; i++) {
            rowData[i] = "";
        }
        rowData[rowData.length - 1] = ""; // Status column

        tableModel.addRow(rowData);
    }

    private void updateTableWithMeasurement(PersistentColorTableModel tableModel,
                                            int partNumber, double current, double duration) {
        // Find the row for this part
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            if (tableModel.getValueAt(i, 0).equals(partNumber)) {
                // Find the first empty current column
                for (int col = 1; col < tableModel.getColumnCount() - 1; col += 2) {
                    if (tableModel.getValueAt(i, col).equals("")) {
                        tableModel.setValueAt(current, i, col);
                        tableModel.setValueAt(String.format("%.3f", duration), i, col + 1);
                        return;
                    }
                }
            }
        }
    }

    private void updateStatusColor(PersistentColorTableModel tableModel, int partNumber) {
        // Find the row for this part
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            if (tableModel.getValueAt(i, 0).equals(partNumber)) {
                int statusCol = tableModel.getColumnCount() - 1;
                boolean hasRedValue = false;
                boolean hasValidValue = false;

                // Check if any current value is red
                for (int col = 1; col < tableModel.getColumnCount() - 1; col += 2) {
                    Object value = tableModel.getValueAt(i, col);
                    if (value != null && !value.equals("")) {
                        hasValidValue = true;
                        Color color = tableModel.getCellColor(i, col);
                        if (color != null && color.equals(Color.RED)) {
                            hasRedValue = true;
                            break;
                        }
                    }
                }

                // Update status cell color
                if (hasValidValue) {
                    if (hasRedValue) {
                        tableModel.setCellColor(i, statusCol, Color.ORANGE);
                    } else {
                        tableModel.setCellColor(i, statusCol, Color.GREEN);
                    }
                }

                // Update part status in the model
                TestPart part = session.getPartByNumber(partNumber);
                if (part != null) {
                    part.setStatus(hasRedValue ? "FAIL" : "PASS");
                }

                return;
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
        // Get the current part number
        int currentPartNumber = getCurrentPartNumber();

        // Create video stream with current part number
        VLCJVideoStream videoStream = new VLCJVideoStream(currentPartNumber);
        videoStream.show();

        // Notify user
        JOptionPane.showMessageDialog(this,
                "Recording video for Part #" + currentPartNumber + "\n" +
                        "Videos will be saved in: " + VLCJVideoStream.saveLocation,
                "Video Recording",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void updateParameters() {
        // Load part-specific parameters if available
        String partDescription = session.getPartDescription();
        PersistentLibrary.PartParameters params = PersistentLibrary.getInstance().getPartParameters(partDescription);

        if (params != null) {
            session.setHeadShotThreshold(params.getHeadshotThreshold());
            session.setCoilShotThreshold(params.getCoilshotThreshold());
        }

        parametersLabel.setText(String.format("Parameters: Headshot (%.2f), Coilshot (%.2f)",
                session.getHeadShotThreshold(), session.getCoilShotThreshold()));

        // Update table renderers with new thresholds
        updateTableRenderers();
    }

    private void updateTableRenderers() {
        headshotTable.setDefaultRenderer(Object.class,
                new CustomCellRenderer(session.getHeadShotThreshold(), headshotTableModel));
        coilshotTable.setDefaultRenderer(Object.class,
                new CustomCellRenderer(session.getCoilShotThreshold(), coilshotTableModel));
    }

    private void endSession() {
        // Copy all parts to history at once
        for (TestPart part : session.getParts()) {
            int partNumber = part.getPartNumber();
            for (int i = 0; i < headshotTableModel.getRowCount(); i++) {
                if (headshotTableModel.getValueAt(i, 0).equals(partNumber)) {
                    copyRowToHistoryTable(headshotTableModel, i, session.getHistoryPanel().getHeadshotHistoryTableModel());
                    copyRowToHistoryTable(coilshotTableModel, i, session.getHistoryPanel().getCoilshotHistoryTableModel());
                    break;
                }
            }
        }

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
        Object[] rowData = new Object[sourceModel.getColumnCount()];
        for (int i = 0; i < sourceModel.getColumnCount(); i++) {
            rowData[i] = sourceModel.getValueAt(sourceRow, i);
        }

        // Add the row to the history table
        historyModel.addRow(rowData);

        // Copy cell colors
        int historyRow = historyModel.getRowCount() - 1;
        for (int col = 0; col < sourceModel.getColumnCount(); col++) {
            Color cellColor = sourceModel.getCellColor(sourceRow, col);
            if (cellColor != null) {
                historyModel.setCellColor(historyRow, col, cellColor);
            }
        }
    }

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
            // Current columns
            table.getColumnModel().getColumn(i).setPreferredWidth(100);
            // Time columns
            table.getColumnModel().getColumn(i + 1).setPreferredWidth(80);
        }
        table.getColumnModel().getColumn(table.getColumnCount() - 1).setPreferredWidth(100); // Status

        // Prevent column resizing and reordering
        table.getTableHeader().setResizingAllowed(false);
        table.getTableHeader().setReorderingAllowed(false);

        // Set default renderer for better number formatting and borders
        table.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public java.awt.Component getTableCellRendererComponent(JTable table, Object value,
                                                                    boolean isSelected, boolean hasFocus, int row, int column) {
                JComponent c = (JComponent) super.getTableCellRendererComponent(table, value,
                        isSelected, hasFocus, row, column);

                // Add border to each cell
                c.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 1, 1, new Color(180, 180, 180)), // Bottom and right borders
                        BorderFactory.createEmptyBorder(2, 5, 2, 5) // Padding
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

                // Set background color
                if (!isSelected) {
                    setBackground(row % 2 == 0 ? Color.WHITE : new Color(245, 245, 245));
                }

                return c;
            }
        });
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
