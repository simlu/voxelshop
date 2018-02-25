package com.vitco.app.layout.content.layer;

import com.jidesoft.action.CommandMenuBar;
import com.vitco.app.core.data.Data;
import com.vitco.app.core.data.notification.DataChangeAdapter;
import com.vitco.app.layout.content.JCustomScrollPane;
import com.vitco.app.layout.content.JCustomTable;
import com.vitco.app.layout.content.ViewPrototype;
import com.vitco.app.manager.action.types.StateActionPrototype;
import com.vitco.app.manager.async.AsyncAction;
import com.vitco.app.manager.async.AsyncActionManager;
import com.vitco.app.manager.pref.PrefChangeListener;
import com.vitco.app.settings.VitcoSettings;
import com.vitco.app.util.misc.SwingAsyncHelper;
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
    private String[] layerNames = new String[]{};
    private int layerCount = 0;
    private Integer[] layerVoxelCounts = new Integer[]{};
    private Boolean[] layerVisibilities = new Boolean[]{};
    // true if editing was canceled
    private boolean cancelEdit = false;

    // this instance
    private final LayerView thisInstance = this;

    protected AsyncActionManager asyncActionManager;
    // set the action handler
    @Autowired
    public final void setAsyncActionManager(AsyncActionManager asyncActionManager) {
        this.asyncActionManager = asyncActionManager;
    }

    // layout of cells
    private class TableRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(
                JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column
        ) {
            boolean isSelectedLayer;
            boolean isVisible;
            synchronized (thisInstance) {
                isSelectedLayer = layers[row] == selectedLayer;
                isVisible = layerVisibilities[row];
            }
            // set the correct background (selected/unselected/visible/invisible)
            if (isSelectedLayer) {
                setFont(VitcoSettings.TABLE_FONT_BOLD); // set font
                if (isVisible) {
                    setBackground(VitcoSettings.VISIBLE_SELECTED_LAYER_BG);
                } else {
                    setBackground(VitcoSettings.HIDDEN_SELECTED_LAYER_BG);
                }
            } else {
                setFont(VitcoSettings.TABLE_FONT); // set font
                if (isVisible) {
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
            synchronized (thisInstance) {
                return layerCount;
            }
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
            synchronized (thisInstance) {
                switch (col) {
                    case 0:
                        return layerNames[row] + " (" + layerVoxelCounts[row] + ")";
                    default:
                        return layerVisibilities[row];
                }
            }
        }

        public Class getColumnClass(int c) {
            return getValueAt(0, c).getClass();
        }

        // only the name is editable
        public boolean isCellEditable(int row, int col) {
            return col == 0;
        }

        public final void setValueAt(final Object value, final int row, final int col) {
            if (!cancelEdit) {
                switch (col) {
                    case 0:
                        // this needs to be done asynchronously,
                        // b/c it could trigger a UI refresh
                        final int layer;
                        synchronized (thisInstance) {
                            layer = layers[row];
                        }
                        asyncActionManager.addAsyncAction(new AsyncAction() {
                            @Override
                            public void performAction() {
                                data.renameLayer(layer, (String) value);
                            }
                        });
                        break;
                    default: break;
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
        SaveTextArea component;

        @Override
        public boolean isCellEditable( EventObject e ) {
            // only edit with double-click
            return !(e instanceof MouseEvent) || ((MouseEvent) e).getClickCount() >= 2;
        }

        // start editing
        public Component getTableCellEditorComponent(final JTable table, Object value,
                                                     boolean isSelected, final int rowIndex, final int vColIndex) {
            synchronized (thisInstance) {
                component = new SaveTextArea(layerNames[rowIndex]);
            }
            component.setBorder(VitcoSettings.DEFAULT_CELL_BORDER_EDIT); // border
            component.setFont(VitcoSettings.TABLE_FONT_BOLD); // font
            component.addKeyListener(new KeyAdapter() {
                @Override
                public void keyPressed(final KeyEvent e) {
                    if (e.getKeyCode() == 10) { // apply changes (return key)
                        // needs to be done asynchronously
                        asyncActionManager.addAsyncAction(new AsyncAction() {
                            @Override
                            public void performAction() {
                                finishCellEditing(table);
                            }
                        });
                    }
                }

                @Override
                public void keyReleased(final KeyEvent e) {
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
    public final void setData(Data data) {
        this.data = data;
    }

    protected void finishCellEditing(final JTable table) {
        if (table.isEditing()) {
            // save the value
            final Component editField = table.getEditorComponent();
            if (editField != null) {
                // needs to be done async, b/c it could trigger a ui refresh
                final int layer;
                synchronized (thisInstance) {
                    layer = layers[table.getEditingRow()];
                }
                asyncActionManager.addAsyncAction(new AsyncAction() {
                    @Override
                    public void performAction() {
                        data.renameLayer(layer, ((JTextArea)editField).getText());
                    }
                });
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

    // true if we are in animation mode, false if in voxel mode
    private boolean isAnimationMode = false;

    @Override
    public final JPanel build() {
        JPanel result = new JPanel();
        result.setLayout(new BorderLayout());

        // create the table
        final JCustomTable table = new JCustomTable(new LayerTableModel());

        // custom layout for cells
        TableRenderer tableRenderer = new TableRenderer();
        table.getColumnModel().getColumn(0).setCellRenderer(tableRenderer);
        table.getColumnModel().getColumn(1).setCellRenderer(tableRenderer);

        // update now
        synchronized (thisInstance) {
            synchronized (VitcoSettings.SYNC) {
                selectedLayer = data.getSelectedLayer();
                layers = data.getLayers();
                layerCount = layers.length;
                layerNames = data.getLayerNames();
                layerVoxelCounts = new Integer[layerNames.length];
                layerVisibilities = new Boolean[layerNames.length];
                for (int i = 0; i < layers.length; i++) {
                    layerVoxelCounts[i] =  data.getVoxelCount(layers[i]);
                    layerVisibilities[i] =  data.getLayerVisible(layers[i]);
                }
            }
        }
        // update table when data changes
        data.addDataChangeListener(new DataChangeAdapter() {

            private void refresh(final int msDelay) {
                synchronized (thisInstance) {
                    synchronized (VitcoSettings.SYNC) {
                        selectedLayer = data.getSelectedLayer();
                        layers = data.getLayers();
                        layerCount = layers.length;
                        layerNames = data.getLayerNames();
                        layerVoxelCounts = new Integer[layerNames.length];
                        layerVisibilities = new Boolean[layerNames.length];
                        for (int i = 0; i < layers.length; i++) {
                            layerVoxelCounts[i] =  data.getVoxelCount(layers[i]);
                            layerVisibilities[i] =  data.getLayerVisible(layers[i]);
                        }
                    }
                }
                // refresh this group
                actionGroupManager.refreshGroup("voxel_layer_interaction");
                // refresh table

                asyncActionManager.addAsyncAction(new AsyncAction("RenderLayerViewTable") {
                    private final long time = System.currentTimeMillis();
                    @Override
                    public void performAction() {
                        SwingAsyncHelper.handle(new Runnable() {
                            @Override
                            public void run() {
                                table.revalidate();
                                table.repaint();
                            }
                        }, errorHandler);
                    }

                    @Override
                    public boolean ready() {
                        return System.currentTimeMillis() - msDelay > time;
                    }
                });
            }

            @Override
            public void onVoxelDataChanged() {
                refresh(200);
            }

            @Override
            public void onLayerStateChanged() {
                refresh(10);
            }
        });

        // change visibility/selection of layer
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(final MouseEvent e) {
                JTable aTable = (JTable)e.getSource();
                final int row = aTable.rowAtPoint(e.getPoint());
                if (row > -1) { // verify that a row was clicked
                    final int col = aTable.columnAtPoint(e.getPoint());
                    final int layer;
                    synchronized (thisInstance) {
                        layer = layers[row];
                    }
                    asyncActionManager.addAsyncAction(new AsyncAction() {
                        @Override
                        public void performAction() {
    //                        if (e.getClickCount() == 1) { // select layer
    //                            data.selectLayerSoft(layer);
    //                        } else if (col == 1 && e.getClickCount() > 1 && e.getClickCount()%2 == 0) { // toggle visibility
    //                            data.setVisible(layer, !data.getLayerVisible(layer));
    //                        }
                            if (col == 0) {
                                data.selectLayerSoft(layer); // select layer
                            } else {
                                data.setVisible(layer, !data.getLayerVisible(layer)); // toggle visibility
                            }
                            // cancel editing if we are editing
                            if (e.getClickCount() == 1 && table.isEditing()) {
                                finishCellEditing(table);
                            }
                        }
                    });
                }
            }
        });

        // register editing
        table.getColumnModel().getColumn(0).setCellEditor(cellEditor);

        // container for table
        JCustomScrollPane pane = new JCustomScrollPane("layers", table);
        pane.setBorder(BorderFactory.createMatteBorder(1,1,0,1,VitcoSettings.DEFAULT_BORDER_COLOR));
        result.add(pane, BorderLayout.CENTER);

        // create the menu bar
        CommandMenuBar menuPanel = new CommandMenuBar();
        menuGenerator.buildMenuFromXML(menuPanel, "com/vitco/app/layout/content/layer/toolbar.xml");
        menuPanel.setBorder(BorderFactory.createMatteBorder(0,1,1,1,VitcoSettings.DEFAULT_BORDER_COLOR));
        result.add(menuPanel, BorderLayout.SOUTH);

        // register change of mode (disable buttons to make history consistent)
        preferences.addPrefChangeListener("is_animation_mode_active", new PrefChangeListener() {
            @Override
            public void onPrefChange(Object o) {
                isAnimationMode = (Boolean)o;
                actionGroupManager.refreshGroup("voxel_layer_interaction");
            }
        });

        // register the menu actions
        actionGroupManager.addAction("voxel_layer_interaction", "layer-frame_add-layer", new StateActionPrototype() {
            @Override
            public void action(ActionEvent actionEvent) {
                if (getStatus()) {
                    finishCellEditing(table);
                    data.selectLayer(data.createLayer("Layer"));
                }
            }

            @Override
            public boolean getStatus() {
                return data.getLayers().length < VitcoSettings.MAX_LAYER_COUNT && !isAnimationMode;
            }
        });
        actionGroupManager.addAction("voxel_layer_interaction", "layer-frame_remove-layer", new StateActionPrototype() {

            @Override
            public void action(ActionEvent actionEvent) {
                finishCellEditing(table);
                data.deleteLayer(data.getSelectedLayer());
            }

            @Override
            public boolean getStatus() {
                return data.getSelectedLayer() != -1 && !isAnimationMode;
            }
        });
        actionGroupManager.addAction("voxel_layer_interaction", "layer-frame_move-layer-up", new StateActionPrototype() {
            @Override
            public void action(ActionEvent actionEvent) {
                finishCellEditing(table);
                data.moveLayerUp(data.getSelectedLayer());
            }

            @Override
            public boolean getStatus() {
                return data.canMoveLayerUp(data.getSelectedLayer()) && !isAnimationMode;
            }
        });
        actionGroupManager.addAction("voxel_layer_interaction", "layer-frame_move-layer-down", new StateActionPrototype() {
            @Override
            public void action(ActionEvent actionEvent) {
                finishCellEditing(table);
                data.moveLayerDown(data.getSelectedLayer());
            }

            @Override
            public boolean getStatus() {
                return data.canMoveLayerDown(data.getSelectedLayer()) && !isAnimationMode;
            }
        });
        actionGroupManager.addAction("voxel_layer_interaction", "layer-frame_layer-merge", new StateActionPrototype() {
            @Override
            public void action(ActionEvent actionEvent) {
                finishCellEditing(table);
                data.mergeVisibleLayers();
            }

            @Override
            public boolean getStatus() {
                return data.canMergeVisibleLayers() && !isAnimationMode;
            }
        });
        actionGroupManager.registerGroup("voxel_layer_interaction");

        return result;
    }
}
