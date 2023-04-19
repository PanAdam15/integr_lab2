package org.example;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.util.List;
import java.util.Set;

class MyTableCellRenderer extends DefaultTableCellRenderer {
    private Set<Integer> rowsToHighlight;
    private Set<Integer> editedRows;

    public MyTableCellRenderer(Set<Integer> rowsToHighlight, Set<Integer> editedRows) {
        this.rowsToHighlight = rowsToHighlight;
        this.editedRows = editedRows;
    }

    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

        // If the row has been edited, set its background color to white
        if (editedRows.contains(row)) {
            c.setBackground(Color.WHITE);
        }
        // If the row is a duplicate, set its background color to red
        else if (rowsToHighlight.contains(row)) {
            c.setBackground(Color.RED);
        }
        // Otherwise, set the background color to the table's default color
        else {
            c.setBackground(Color.GRAY);
        }

        return c;
    }
}