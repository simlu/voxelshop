package com.vitco.logic.layer;

import com.vitco.engine.data.Data;
import com.vitco.engine.data.notification.DataChangeAdapter;
import com.vitco.logic.ViewPrototype;
import com.vitco.res.VitcoSettings;
import org.springframework.beans.factory.annotation.Autowired;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import java.awt.*;
import java.awt.event.*;
import java.util.EventObject;

/**
 * Build the layer menu with options.
 */
public class LayerView extends ViewPrototype implements LayerViewInterface {



    // the currently selected layer
    int selectedLayer = -1;

    // layout of cells
    private class TableRenderer extends DefaultTableCellRenderer {
        // mainview table cell
        @Override
        public Component getTableCellRendererComponent(
                JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column
        ) {
            if (selectedLayer == data.getLayers()[row]) {
                if (data.getLayerVisible(data.getLayers()[row])) {
                    setBackground(VitcoSettings.VISIBLE_SELECTED_LAYER_BG);
                } else {
                    setBackground(VitcoSettings.HIDDEN_SELECTED_LAYER_BG);
                }
            } else {
                if (data.getLayerVisible(data.getLayers()[row])) {
                    setBackground(VitcoSettings.VISIBLE_LAYER_BG);
                } else {
                    setBackground(VitcoSettings.HIDDEN_LAYER_BG);
                }
            }
            setForeground(VitcoSettings.DEFAULT_TEXT_COLOR); // set text color
            setBorder(VitcoSettings.DEFAULT_BORDER); // padding
            setValue(value.toString()); // set the value

            setFont(VitcoSettings.TABLE_FONT);

            return this;
        }
    }

    private class LayerTableModel extends AbstractTableModel {

        public int getColumnCount() {
            return 2;
        }

        public int getRowCount() {
            return data.getLayers().length;
        }

        public String getColumnName(int col) {
            switch (col) {
                case 0:
                    return "Name"; // todo localize
                default:
                    return "Visible";
            }
        }

        public Object getValueAt(int row, int col) {
            switch (col) {
                case 0:
                    return data.getLayerNames()[row];
                default:
                    return data.getLayerVisible(data.getLayers()[row]);
            }
        }

        public Class getColumnClass(int c) {
            return getValueAt(0, c).getClass();
        }

        public boolean isCellEditable(int row, int col) {
            return col == 0;
        }

        public void setValueAt(Object value, int row, int col) {
            switch (col) {
                case 0:
                    data.renameLayer(data.getLayers()[row], (String)value);
                    break;
            }
            fireTableCellUpdated(row, col);
        }

    }

    private class CellEditor extends AbstractCellEditor implements TableCellEditor {
        // handles the editing of the cell value
        JTextArea component;

        @Override
        public boolean isCellEditable( EventObject e ) {
            // only edit with double-click
            return !(e instanceof MouseEvent) || ((MouseEvent) e).getClickCount() >= 2;
        }

        // start editing
        public Component getTableCellEditorComponent(final JTable table, Object value,
                                                     boolean isSelected, final int rowIndex, final int vColIndex) {
            component = new JTextArea();
            component.setText((String) value); // set initial text
            component.setBorder(VitcoSettings.DEFAULT_BORDER_EDIT);
            component.setFont(VitcoSettings.TABLE_FONT_BOLD);
            component.addKeyListener(new KeyAdapter() {
                @Override
                public void keyPressed(KeyEvent e) {
                    if (e.getKeyCode() == 10) {
                        component.setFocusable(false);
                    }
                }

                @Override
                public void keyTyped(KeyEvent e) {
                    // handle resize bug
                    table.setValueAt(component.getText(), rowIndex, vColIndex);
                }
            });
            component.addFocusListener(new FocusAdapter() {
                @Override
                public void focusLost(FocusEvent e) {
                    if (table.getCellEditor() != null) {
                        table.getCellEditor().stopCellEditing();
                    }
                }
            });
            component.setBackground(VitcoSettings.EDIT_BG_COLOR); // bg color when edit
            component.setForeground(VitcoSettings.EDIT_TEXT_COLOR); // set text color when edit
            return component;
        }

        // edit complete
        // Important: can not rely on this to fire (resize bug!)
        public Object getCellEditorValue() {
            return component.getText(); // return new value
        }
    }


    // var & setter
    protected Data data;
    @Autowired
    public void setData(Data data) {
        this.data = data;
    }

    @Override
    public final JPanel build() {
        JPanel result = new JPanel();
        result.setLayout(new BorderLayout());

        data.createLayer("layer1");

        data.createLayer("layer2");

        data.createLayer("layer3");

        final JTable table = new JTable(new LayerTableModel());
        // custom row height
        table.setRowHeight(table.getRowHeight()+VitcoSettings.DEFAULT_TABLE_INCREASE);
        // custom layout for cells
        table.getColumnModel().getColumn(0).setCellRenderer(new TableRenderer());
        table.getColumnModel().getColumn(1).setCellRenderer(new TableRenderer());

        result.setBackground(VitcoSettings.DEFAULT_BG_COLOR);

        // update table when data changes
        data.addDataChangeListener(new DataChangeAdapter() {
            @Override
            public void onVoxelDataChanged() {
                // todo only when layer data changes (not voxel data!)
                selectedLayer = data.getSelectedLayer();
                table.updateUI(); // refresh table
            }
        });

        // toggle visibility of layer
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                JTable aTable = (JTable)e.getSource();
                int row = aTable.rowAtPoint(e.getPoint());
                int col = aTable.columnAtPoint(e.getPoint());

                if (e.getClickCount() == 1) {
                        data.selectLayer(data.getLayers()[row]);
                } else if (e.getClickCount() > 1 && e.getClickCount()%2 == 0) {
                        if (col == 1) {
                            data.setVisible(data.getLayers()[row], !data.getLayerVisible(data.getLayers()[row]));
                        }
                }
                if (table.isEditing()) {
                    table.getEditorComponent().setFocusable(false);

                }

            }
        });

        // handle editing
        table.getColumnModel().getColumn(0).setCellEditor(new CellEditor());
        // stop editing when table looses focus
        table.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);

        result.add(new JScrollPane(table), BorderLayout.CENTER);
        return result;
    }
}
