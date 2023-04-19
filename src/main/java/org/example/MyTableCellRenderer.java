package org.example;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.util.List;
import java.util.Set;

class MyTableCellRenderer extends DefaultTableCellRenderer {
    private Set<Integer> rowsToHighlight;

    public MyTableCellRenderer(Set<Integer> rowsToHighlight) {
        this.rowsToHighlight = rowsToHighlight;
    }

    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        if (rowsToHighlight.contains(row)) {
            c.setBackground(Color.RED);
        } else {
            c.setBackground(table.getBackground());
        }
        return c;
    }
}