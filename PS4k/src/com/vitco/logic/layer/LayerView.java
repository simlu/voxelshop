package com.vitco.logic.layer;

import com.jidesoft.action.CommandMenuBar;
import com.vitco.engine.data.Data;
import com.vitco.engine.data.notification.DataChangeAdapter;
import com.vitco.logic.ViewPrototype;
import com.vitco.res.VitcoSettings;
import com.vitco.util.action.types.StateActionPrototype;
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
    private int selectedLayer = -1;
    // layer buffer
    private Integer[] layers = new Integer[]{};
    // true if editing was canceled
    private boolean cancelEdit = false;

    // layout of cells
    private class TableRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(
                JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column
        ) {
            // set the correct background (selected/unselected/visible/invisible)
            if (selectedLayer == layers[row]) {
                setFont(VitcoSettings.TABLE_FONT_BOLD); // set font
                if (data.getLayerVisible(layers[row])) {
                    setBackground(VitcoSettings.VISIBLE_SELECTED_LAYER_BG);
                } else {
                    setBackground(VitcoSettings.HIDDEN_SELECTED_LAYER_BG);
                }
            } else {
                setFont(VitcoSettings.TABLE_FONT); // set font
                if (data.getLayerVisible(layers[row])) {
                    setBackground(VitcoSettings.VISIBLE_LAYER_BG);
                } else {
                    setBackground(VitcoSettings.HIDDEN_LAYER_BG);
                }
            }
            setForeground(VitcoSettings.DEFAULT_TEXT_COLOR); // set text color
            setBorder(VitcoSettings.DEFAULT_CELL_BORDER); // padding
            setValue(value.toString()); // set the value
            return this;
        }
    }

    // construct the table data input
    private class LayerTableModel extends AbstractTableModel {

        public int getColumnCount() {
            return 2;
        }

        public int getRowCount() {
            return layers.length;
        }

        // column names
        public String getColumnName(int col) {
            switch (col) {
                case 0:
                    return langSelector.getString("layer-window_layer-name");
                default:
                    return langSelector.getString("layer-window_layer-visible");
            }
        }

        // display value
        public Object getValueAt(int row, int col) {
            switch (col) {
                case 0:
                    return data.getLayerNames()[row] + " (" + data.getVoxelCount(layers[row])+ ")";
                default:
                    return data.getLayerVisible(layers[row]);
            }
        }

        public Class getColumnClass(int c) {
            return getValueAt(0, c).getClass();
        }

        // only the name is editable
        public boolean isCellEditable(int row, int col) {
            return col == 0;
        }

        public void setValueAt(Object value, int row, int col) {
            if (!cancelEdit) {
                switch (col) {
                    case 0:
                        data.renameLayer(layers[row], (String) value);
                        break;
                }
                fireTableCellUpdated(row, col);
            } else {
                cancelEdit = false;
            }
        }

    }

    private final CellEditor cellEditor = new CellEditor();
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
            component.setText(data.getLayerName(layers[rowIndex])); // set initial text
            component.setBorder(VitcoSettings.DEFAULT_CELL_BORDER_EDIT); // border
            component.setFont(VitcoSettings.TABLE_FONT_BOLD); // font
            component.addKeyListener(new KeyAdapter() {
                @Override
                public void keyPressed(KeyEvent e) {
                    if (e.getKeyCode() == 10) { // apply changes (return key)
                        finishCellEditing(table);
                    }
                }

                @Override
                public void keyReleased(KeyEvent e) {
                    e.consume(); // prevent further use of this keystroke
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

    protected void finishCellEditing(JTable table) {
        if (table.isEditing()) {
            // save the value
            Component editField = table.getEditorComponent();
            if (editField != null) {
                data.renameLayer(layers[table.getEditingRow()], ((JTextArea)editField).getText());
            }
            // cancel all further saving of edits
            cancelEdit = true;
            TableCellEditor tce = table.getCellEditor();
            if (tce != null) {
                tce.stopCellEditing();
            }
            cancelEdit = false;
        }
    }

    @Override
    public final JPanel build() {
        JPanel result = new JPanel();
        result.setLayout(new BorderLayout());

        // create the table
        final JTable table = new JTable(new LayerTableModel());

        // custom row height
        table.setRowHeight(table.getRowHeight()+VitcoSettings.DEFAULT_TABLE_INCREASE);
        // custom layout for cells
        table.getColumnModel().getColumn(0).setCellRenderer(new TableRenderer());
        table.getColumnModel().getColumn(1).setCellRenderer(new TableRenderer());

        // update now
        selectedLayer = data.getSelectedLayer();
        layers = data.getLayers();

        // update table when data changes
        data.addDataChangeListener(new DataChangeAdapter() {
            @Override
            public void onVoxelDataChanged() {
                // todo can optimize this further (only execute when layer updated!)
                selectedLayer = data.getSelectedLayer();
                layers = data.getLayers();
                // refresh "move up" and "move down" button
                actionManager.performWhenActionIsReady("layer-frame_move-layer-up", new Runnable() {
                    @Override
                    public void run() {
                        ((StateActionPrototype)actionManager.getAction("layer-frame_move-layer-up")).refresh();
                    }
                });
                actionManager.performWhenActionIsReady("layer-frame_move-layer-down", new Runnable() {
                    @Override
                    public void run() {
                        ((StateActionPrototype)actionManager.getAction("layer-frame_move-layer-down")).refresh();
                    }
                });
                // refresh merge layers
                actionManager.performWhenActionIsReady("layer-frame_layer-merge", new Runnable() {
                    @Override
                    public void run() {
                        ((StateActionPrototype)actionManager.getAction("layer-frame_layer-merge")).refresh();
                    }
                });
                // refresh table
                table.updateUI();
            }
        });

        // change visibility/selection of layer
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                JTable aTable = (JTable)e.getSource();
                int row = aTable.rowAtPoint(e.getPoint());
                int col = aTable.columnAtPoint(e.getPoint());

                if (e.getClickCount() == 1) { // select layer
                    data.selectLayerSoft(layers[row]);
                } else if (col == 1 && e.getClickCount() > 1 && e.getClickCount()%2 == 0) { // toggle visibility
                    data.setVisible(layers[row], !data.getLayerVisible(layers[row]));
                }
                // cancel editing if we are editing
                if (e.getClickCount() == 1 && table.isEditing()) {
                    finishCellEditing(table);
                }
            }
        });

        // register editing
        table.getColumnModel().getColumn(0).setCellEditor(cellEditor);
        // stop editing when table looses focus
        table.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);

        // container for table
        JScrollPane pane = new JScrollPane(table);
        pane.setBorder(BorderFactory.createLineBorder(VitcoSettings.DEFAULT_BORDER_COLOR));
        result.add(pane, BorderLayout.CENTER);

        // create the menu bar
        CommandMenuBar menuPanel = new CommandMenuBar();
        menuGenerator.buildMenuFromXML(menuPanel, "com/vitco/logic/layer/toolbar.xml");
        menuPanel.setBorder(BorderFactory.createMatteBorder(0,1,1,1,VitcoSettings.DEFAULT_BORDER_COLOR));
        result.add(menuPanel, BorderLayout.SOUTH);

        // register the menu actions
        actionManager.registerAction("layer-frame_add-layer", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                finishCellEditing(table);
                data.createLayer("Layer");
            }
        });
        actionManager.registerAction("layer-frame_remove-layer", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                finishCellEditing(table);
                data.deleteLayer(data.getSelectedLayer());
            }
        });
        actionManager.registerAction("layer-frame_move-layer-up", new StateActionPrototype() {
            @Override
            public void action(ActionEvent actionEvent) {
                finishCellEditing(table);
                data.moveLayerUp(data.getSelectedLayer());
            }

            @Override
            public boolean getStatus() {
                return data.canMoveLayerUp(data.getSelectedLayer());
            }
        });
        actionManager.registerAction("layer-frame_move-layer-down", new StateActionPrototype() {
            @Override
            public void action(ActionEvent actionEvent) {
                finishCellEditing(table);
                data.moveLayerDown(data.getSelectedLayer());
            }

            @Override
            public boolean getStatus() {
                return data.canMoveLayerDown(data.getSelectedLayer());
            }
        });
        actionManager.registerAction("layer-frame_layer-merge", new StateActionPrototype() {
            @Override
            public void action(ActionEvent actionEvent) {
                finishCellEditing(table);
                data.mergeVisibleLayers();
            }

            @Override
            public boolean getStatus() {
                return data.canMergeVisibleLayers();
            }
        });

        return result;
    }
}
