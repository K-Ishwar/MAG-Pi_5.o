package com.magpi.ui.table;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

/**
 * Custom cell renderer that applies color based on measurement thresholds
 */
public class CustomCellRenderer extends DefaultTableCellRenderer {
    private final double thresholdValue;
    private final PersistentColorTableModel tableModel;

    /**
     * Creates a new CustomCellRenderer
     * @param thresholdValue The threshold value for pass/fail coloring
     * @param tableModel The model containing color information
     */
    public CustomCellRenderer(double thresholdValue, PersistentColorTableModel tableModel) {
        this.thresholdValue = thresholdValue;
        this.tableModel = tableModel;
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
                                                   boolean isSelected, boolean hasFocus,
                                                   int row, int column) {
        Component cell = super.getTableCellRendererComponent(
                table, value, isSelected, hasFocus, row, column);

        // First check if there's a previously set color for this cell
        Color savedColor = tableModel.getCellColor(row, column);
        if (savedColor != null) {
            cell.setBackground(savedColor);
            return cell;
        }

        // Handle pass/fail column
        if (column == table.getColumnCount() - 1) {
            if ("Accept".equals(value)) {
                cell.setBackground(new Color(144, 238, 144, 150)); // Light Green
            } else if ("Reject".equals(value)) {
                cell.setBackground(new Color(255, 160, 122, 150)); // Light Red
            } else {
                cell.setBackground(Color.WHITE);
            }
            return cell;
        }

        // Only color measurement columns (odd-indexed columns after the first)
        if (column % 2 == 1 && column > 0 && value != null && !value.toString().isEmpty()) {
            try {
                double currentValue = Double.parseDouble(value.toString());
                Color newColor;

                // Apply coloring based on parameter value
                if (currentValue > thresholdValue) {
                    newColor = new Color(144, 238, 144, 150); // Light Green with reduced opacity
                } else {
                    newColor = new Color(255, 160, 122, 150); // Light Red with reduced opacity
                }

                // Save the color in the table model for persistence
                tableModel.setCellColor(row, column, newColor);
                cell.setBackground(newColor);
                cell.setForeground(Color.BLACK);
            } catch (NumberFormatException e) {
                cell.setBackground(Color.WHITE);
                cell.setForeground(Color.BLACK);
            }
        } else {
            cell.setBackground(Color.WHITE);
            cell.setForeground(Color.BLACK);
        }
        
        return cell;
    }
} 