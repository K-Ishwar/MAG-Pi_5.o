package com.magpi.ui.table;

import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Table model that preserves cell colors between renderings
 */
public class PersistentColorTableModel extends DefaultTableModel {
    private Map<Point, Color> cellColors = new HashMap<>();
    
    /**
     * Creates a new PersistentColorTableModel with the given column names and row count
     * @param columnNames Array of column names
     * @param rowCount Initial number of rows
     */
    public PersistentColorTableModel(String[] columnNames, int rowCount) {
        super(columnNames, rowCount);
    }
    
    /**
     * Sets a color for a specific cell
     * @param row The row index
     * @param col The column index
     * @param color The color to set
     */
    public void setCellColor(int row, int col, Color color) {
        cellColors.put(new Point(row, col), color);
    }
    
    /**
     * Gets the color for a specific cell
     * @param row The row index
     * @param col The column index
     * @return The color of the cell, or null if no color is set
     */
    public Color getCellColor(int row, int col) {
        return cellColors.get(new Point(row, col));
    }
    
    /**
     * Clears all cell colors
     */
    public void clearCellColors() {
        cellColors.clear();
    }
    
    /**
     * Removes a specific cell color
     * @param row The row index
     * @param col The column index
     */
    public void removeCellColor(int row, int col) {
        cellColors.remove(new Point(row, col));
    }
} 