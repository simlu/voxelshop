package com.vitco.app.layout.content;

import com.vitco.app.settings.VitcoSettings;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import java.awt.*;
import java.util.Vector;

/**
 * A custom JTable that unifies the style for the program and changes
 * some basic logic to make things more user friendly and functional.
 */
public class JCustomTable extends JTable {
    // initialize the table
    private void init() {
        // overwrite the header colors
        final TableCellRenderer hr = this.getTableHeader().getDefaultRenderer();
        this.getTableHeader().setDefaultRenderer(new DefaultTableCellRenderer() {
            private JLabel lbl;
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                lbl = (JLabel) hr.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                lbl.setBackground(VitcoSettings.TABLE_HEADER_BG_COLOR);
                lbl.setForeground(VitcoSettings.TABLE_HEADER_COLOR);
                lbl.setFont(lbl.getFont().deriveFont(Font.BOLD));
                Insets inset = lbl.getBorder().getBorderInsets(table);
                lbl.setBorder(BorderFactory.createEmptyBorder(inset.top,inset.left + 10,inset.bottom,inset.right));
                return lbl;
            }
        });

        // set background color
        this.setBackground(VitcoSettings.DEFAULT_BG_COLOR);
        this.setFillsViewportHeight(true);

        // disable reordering of columns
        this.getTableHeader().setReorderingAllowed(false);
        // custom row height
        this.setRowHeight(this.getRowHeight() + VitcoSettings.DEFAULT_TABLE_INCREASE);
        // disable selection in table
        this.setCellSelectionEnabled(false);
        // disable focus on cells
        this.setFocusable(false);
        // stop editing when table looses focus
        this.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
    }

    // ================
    // below are constructors

    public JCustomTable() {
        init();
    }

    public JCustomTable(TableModel dm) {
        super(dm);
        init();
    }

    public JCustomTable(TableModel dm, TableColumnModel cm) {
        super(dm, cm);
        init();
    }

    public JCustomTable(TableModel dm, TableColumnModel cm, ListSelectionModel sm) {
        super(dm, cm, sm);
        init();
    }

    public JCustomTable(int numRows, int numColumns) {
        super(numRows, numColumns);
        init();
    }

    public JCustomTable(Vector rowData, Vector columnNames) {
        super(rowData, columnNames);
        init();
    }

    public JCustomTable(Object[][] rowData, Object[] columnNames) {
        super(rowData, columnNames);
        init();
    }
}
