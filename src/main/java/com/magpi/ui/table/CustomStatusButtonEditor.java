package com.magpi.ui.table;

import javax.swing.*;
import javax.swing.table.TableModel;
import java.awt.*;
import java.util.function.BiConsumer;



/**
 * Custom editor for status buttons in table cells
 */
public class CustomStatusButtonEditor extends DefaultCellEditor {
    private JButton button;
    private boolean isPushed;
    private int row;
    private TableModel tableModel;
    private BiConsumer<TableModel, Integer> statusChangeHandler;

    /**
     * Creates a new button editor
     * @param checkBox The checkbox for the editor
     * @param tableModel The table model containing the data
     * @param statusChangeHandler Callback for when status changes
     */
    public CustomStatusButtonEditor(JCheckBox checkBox, TableModel tableModel, 
                                   BiConsumer<TableModel, Integer> statusChangeHandler) {
        super(checkBox);
        this.tableModel = tableModel;
        this.statusChangeHandler = statusChangeHandler;
        
        button = new JButton();
        button.setOpaque(true);
        button.addActionListener(e -> fireEditingStopped());
    }
    
    /**
     * Simpler constructor that doesn't require a status change handler
     * @param checkBox The checkbox for the editor
     * @param tableModel The table model containing the data
     */
    public CustomStatusButtonEditor(JCheckBox checkBox, TableModel tableModel) {
        this(checkBox, tableModel, null);
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value,
                                               boolean isSelected, int row, int column) {
        this.row = row;
        button.setText("Set Status");
        isPushed = true;
        return button;
    }

    @Override
    public Object getCellEditorValue() {
        if (isPushed) {
            // Ensure the row has some data before allowing status setting
            boolean hasData = false;
            for (int col = 1; col < tableModel.getColumnCount() - 2; col++) {
                Object cellValue = tableModel.getValueAt(row, col);
                if (cellValue != null && !cellValue.toString().trim().isEmpty()) {
                    hasData = true;
                    break;
                }
            }

            if (!hasData) {
                JOptionPane.showMessageDialog(null,
                        "Please enter at least one current value before setting status.",
                        "Invalid Entry",
                        JOptionPane.WARNING_MESSAGE);
                isPushed = false;
                return "Set Status";
            }

            // Show dialog to set status
            String[] options = {"Accept", "Reject"};
            int choice = JOptionPane.showOptionDialog(
                    null,
                    "Set Status for Part " + tableModel.getValueAt(row, 0),
                    "Status Selection",
                    JOptionPane.DEFAULT_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    options,
                    options[0]
            );

            // Update status
            if (choice != JOptionPane.CLOSED_OPTION) {
                String status = choice == 0 ? "Accept" : "Reject";
                tableModel.setValueAt(status, row, tableModel.getColumnCount() - 2);
                
                // Call the status change handler if provided
                if (statusChangeHandler != null) {
                    statusChangeHandler.accept(tableModel, row);
                }
            }
        }
        isPushed = false;
        return "Set Status";
    }

    @Override
    public boolean stopCellEditing() {
        isPushed = false;
        return super.stopCellEditing();
    }
} 