package com.vitco.layout.content.shortcut;

import com.jidesoft.swing.JideTabbedPane;
import com.vitco.layout.content.ViewPrototype;
import com.vitco.manager.async.AsyncAction;
import com.vitco.manager.async.AsyncActionManager;
import com.vitco.settings.VitcoSettings;
import org.springframework.beans.factory.annotation.Autowired;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import java.awt.*;
import java.awt.event.*;
import java.util.EventObject;

/**
 * Handle the displaying and the logic for the editing of shortcuts. (view & link to logic)
 */
public class ShortcutManagerView extends ViewPrototype implements ShortcutManagerViewInterface {

    protected AsyncActionManager asyncActionManager;
    @Autowired
    public final void setAsyncActionManager(AsyncActionManager asyncActionManager) {
        this.asyncActionManager = asyncActionManager;
    }


    // last hover cell
    private int curRow = -1;
    private int curCol = -1;

    // layout of cells
    private class TableRenderer extends DefaultTableCellRenderer {
        // mainview table cell
        @Override
        public Component getTableCellRendererComponent(
                JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column
        ) {
            setForeground(VitcoSettings.DEFAULT_TEXT_COLOR); // set text color
            if (row == curRow && column == curCol && column == 1) {
                setBackground(VitcoSettings.DEFAULT_HOVER_COLOR); // hover effect
                setToolTipText(langSelector.getString("dbl-clc-to-edit_tooltip"));
            } else {
                setBackground(VitcoSettings.DEFAULT_BG_COLOR);
            }
            setFont(VitcoSettings.TABLE_FONT);
            setBorder(VitcoSettings.DEFAULT_CELL_BORDER); // padding
            setValue(value.toString()); // set the value
            return this;
        }
    }

    // cell edit action
    private class CellEditor extends AbstractCellEditor implements TableCellEditor {

        // handles the editing of the cell value
        JTextArea component;
        // var & setter (constructor)
        // so we have a reference to the current frame
        // this can be null if we handle global shortcuts
        private final String frame;
        public CellEditor(String frame) {
            super();
            this.frame = frame;
        }

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
            component.setBorder(VitcoSettings.DEFAULT_CELL_BORDER_EDIT);
            component.setFont(VitcoSettings.TABLE_FONT_BOLD);
            // add the listener
            component.addKeyListener(new KeyListener() {
                @Override
                public void keyTyped(KeyEvent e) {
                    e.consume(); // prevent further use of this keystroke
                }

                @Override
                public void keyPressed(final KeyEvent e) {
                    asyncActionManager.addAsyncAction(new AsyncAction() {
                        @Override
                        public void performAction() {
                            KeyStroke keyStroke = e.getKeyCode() == 27
                                    ? null // escape
                                    : KeyStroke.getKeyStrokeForEvent(e); // else
                            if (shortcutManager.isValidShortcut(keyStroke)) {
                                if (shortcutManager.isFreeShortcut(frame, keyStroke)) {
                                    // update the shortcut
                                    if (shortcutManager.updateShortcutObject(keyStroke, frame, rowIndex)) {
                                        String shortcutText = shortcutManager.asString(keyStroke);
                                        component.setText(shortcutText);
                                        // make sure the table is up to date
                                        // note: workaround for resize bug
                                        table.setValueAt(shortcutText, rowIndex, vColIndex);
                                    }
                                    component.setBackground(VitcoSettings.EDIT_BG_COLOR);
                                } else { // this shortcut is already used (!)
                                    // show error color for one second
                                    component.setBackground(VitcoSettings.EDIT_ERROR_BG_COLOR);
                                    Toolkit.getDefaultToolkit().beep(); // play beep
                                    new Thread() {
                                        @Override
                                        public void run() {
                                            try {
                                                Thread.sleep(1000);
                                            } catch (InterruptedException e1) {
                                                // no need to track this
                                                // e1.printStackTrace();
                                            }
                                            component.setBackground(VitcoSettings.EDIT_BG_COLOR);
                                        }
                                    }.start();
                                }
                            }
                            e.consume(); // prevent further use of this keystroke
                        }
                    });
                }

                @Override
                public void keyReleased(KeyEvent e) {
                    e.consume(); // prevent further use of this keystroke
                }
            });
            component.setHighlighter(null); // do not show selection
            component.setEditable(false); // hide the caret
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

    // internal - takes shortcuts, col names and frame key
    // frame key can be null if we handle global shortcuts
    // global flag
    private JTable createTab(String[][] data, String[] columnNames, String frameKey) {
            // create the default sideview
            DefaultTableModel model = new DefaultTableModel(data, columnNames);
            // create table, only allow editing for second column (shortcuts)
            JTable shortcut_table = new JTable(model) {
                @Override
                public boolean isCellEditable(int rowIndex, int colIndex) {
                    // only allow editing shortcuts
                    return colIndex == 1;
                }
            };
            // custom row height
            shortcut_table.setRowHeight(shortcut_table.getRowHeight()+VitcoSettings.DEFAULT_TABLE_INCREASE);
            shortcut_table.setCellSelectionEnabled(false); // disable selection in table
            shortcut_table.setFocusable(false); // disable focus on cells
            shortcut_table.setBackground(VitcoSettings.DEFAULT_BG_COLOR); // set the default bg color (unnecessary)
            // add hover effect
            shortcut_table.addMouseMotionListener(new MouseMotionAdapter() {
                @Override
                public void mouseMoved(MouseEvent e) {
                    JTable aTable = (JTable)e.getSource();
                    int tRow = aTable.rowAtPoint(e.getPoint());
                    int tCol = aTable.columnAtPoint(e.getPoint());
                    if (curRow != tRow || curCol != tCol) {
                        curRow = tRow;
                        curCol = tCol;
                        aTable.repaint();
                    }
                }
            });
            shortcut_table.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseExited(MouseEvent e) {
                    curRow = -1;
                    curCol = -1;
                    ((JTable)e.getSource()).repaint();
                }
            });
            // custom layout for the cells
            shortcut_table.getColumnModel().getColumn(0).setCellRenderer(new TableRenderer());
            shortcut_table.getColumnModel().getColumn(1).setCellRenderer(new TableRenderer());
            // stop editing when table looses focus
            shortcut_table.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
            // create a new custom cell editor for the second column (shortcuts)
            shortcut_table.getColumnModel().getColumn(1).setCellEditor(new CellEditor(frameKey));
            return shortcut_table;
    }

    // return a JideTabbedPane that is autonomous and manages shortcuts
    @Override
    public JTabbedPane getEditTables() {
        // create the content of this frame (Shortcut Manager)
        // default header
        String[] columnNames = {
                langSelector.getString("shortcut_mg_header_action"),
                langSelector.getString("shortcut_mg_header_shortcut")
        };
        // the different frame shortcuts are in different tabs
        final JideTabbedPane tabbedPane = new JideTabbedPane(JTabbedPane.TOP, JideTabbedPane.SCROLL_TAB_LAYOUT);
        tabbedPane.setFocusable(false); // looks nicer
        //tabbedPane.setTabShape(JideTabbedPane.SHAPE_WINDOWS); // make square
        //tabbedPane.setTabResizeMode(JideTabbedPane.RESIZE_MODE_FIT);
        // add the global shortcuts
        String[][] globalShortcuts = shortcutManager.getShortcuts(null);
        tabbedPane.addTab(
            langSelector.getString("global_shortcuts_caption"),
            new JScrollPane(createTab(globalShortcuts, columnNames, null))
        );
        // add the frame shortcuts
        String[][] frames = shortcutManager.getFrames();
        for (String[] frame : frames) {
            // create the list of shortcuts for this frame
            String[][] data = shortcutManager.getShortcuts(frame[0]); // the shortcuts (list)
            if (data.length > 0) { // only create tab if it has shortcuts
                // add this tab to the tabbedPane
                tabbedPane.addTab(
                        frame[1],
                        new JScrollPane(createTab(data, columnNames, frame[0]))
                );
            }
        }
        // set tooltips
        for (int i = 0; i < tabbedPane.getTabCount(); i ++) {
            tabbedPane.setToolTipTextAt(i, tabbedPane.getTitleAt(i));
        }

        // todo add setter and getter for this and store and load in linkage class
        // load from preferences
        if (preferences.contains("shortcut-manager_active-tab")) {
            int selectedIndex = preferences.loadInteger("shortcut-manager_active-tab");
            if (tabbedPane.getTabCount() > selectedIndex) {
                tabbedPane.setSelectedIndex(selectedIndex); // set the stored index as active
            }
        }
        // store when active tab changes
        tabbedPane.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                preferences.storeInteger("shortcut-manager_active-tab", tabbedPane.getSelectedIndex());
            }
        });
        return tabbedPane;
    }
}
