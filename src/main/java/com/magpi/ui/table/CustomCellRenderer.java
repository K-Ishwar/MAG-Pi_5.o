package com.magpi.ui.table;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

/**
 * Custom renderer for table cells that applies color based on threshold values
 */
public class CustomCellRenderer extends DefaultTableCellRenderer {
    private final double threshold;
    private final PersistentColorTableModel tableModel;

    /**
     * Creates a new custom cell renderer
     * @param threshold The threshold value for coloring cells
     * @param tableModel The table model that stores cell colors
     */
    public CustomCellRenderer(double threshold, PersistentColorTableModel tableModel) {
        this.threshold = threshold;
        this.tableModel = tableModel;
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
                                                   boolean isSelected, boolean hasFocus,
                                                   int row, int column) {
        Component cell = super.getTableCellRendererComponent(
                table, value, isSelected, hasFocus, row, column);

        // Reset background
        cell.setBackground(Color.WHITE);

        // Get saved cell color if it exists
        Color savedColor = tableModel.getCellColor(row, column);
        if (savedColor != null) {
            cell.setBackground(savedColor);
            return cell;
        }

        // Only color numeric cells (current values) that are in odd columns (1, 3, 5, 7, 9)
        if (column % 2 == 1 && column > 0 && column < table.getColumnCount() - 1) {
            if (value != null && !value.toString().trim().isEmpty()) {
                try {
                    double current = Double.parseDouble(value.toString());
                    if (current >= threshold) {
                        cell.setBackground(Color.GREEN);
                        tableModel.setCellColor(row, column, Color.GREEN);
                    } else {
                        cell.setBackground(Color.RED);
                        tableModel.setCellColor(row, column, Color.RED);

                        // Auto-update status column when a red value is found
                        int statusColumn = table.getColumnCount() - 1;
                        tableModel.setCellColor(row, statusColumn, Color.ORANGE);
                    }
                } catch (NumberFormatException e) {
                    // Not a number, don't color
                }
            }
        }
        // Status column has special coloring handled by updateStatusColor method in TablePage
        else if (column == table.getColumnCount() - 1) {
            // Status column - color will be set by the updateStatusColor method
            // If no color is set yet, just keep it white
            cell.setBackground(Color.WHITE);
        }

        // First column (part number) should be gray
        if (column == 0) {
            cell.setBackground(new Color(220, 220, 220)); // Light gray
        }

        return cell;
    }
}