package com.magpi.ui.table;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

/**
 * Renderer for buttons in table cells
 */
public class JButtonRenderer extends JButton implements TableCellRenderer {
    
    /**
     * Creates a new button renderer
     */
    public JButtonRenderer() {
        setOpaque(true);
    }
    
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
                                                  boolean isSelected, boolean hasFocus,
                                                  int row, int column) {
        setText(value != null ? value.toString() : "Set Status");
        return this;
    }
} 