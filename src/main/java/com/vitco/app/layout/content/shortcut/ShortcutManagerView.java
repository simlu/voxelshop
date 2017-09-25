package com.vitco.app.layout.content.shortcut;

import com.jidesoft.swing.JideButton;
import com.jidesoft.swing.JideComboBox;
import com.jidesoft.swing.JideTabbedPane;
import com.vitco.app.layout.content.JCustomScrollPane;
import com.vitco.app.layout.content.JCustomTable;
import com.vitco.app.layout.content.ViewPrototype;
import com.vitco.app.manager.async.AsyncAction;
import com.vitco.app.manager.async.AsyncActionManager;
import com.vitco.app.settings.VitcoSettings;
import com.vitco.app.util.misc.SaveResourceLoader;
import org.springframework.beans.factory.annotation.Autowired;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.TreeMap;

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
                setBackground(VitcoSettings.DEFAULT_CELL_COLOR);
            }
            setFont(VitcoSettings.TABLE_FONT);
            setBorder(VitcoSettings.DEFAULT_CELL_BORDER); // padding
            setValue(value.toString()); // set the value
            return this;
        }
    }

    // key cell edit action
    private class KeyCellEditor extends AbstractCellEditor implements TableCellEditor {

        // handles the editing of the cell value
        JPanel wrapper;
        JTextArea component;
        // var & setter (constructor)
        // so we have a reference to the current frame
        // this can be null if we handle global shortcuts
        private final String frame;
        public KeyCellEditor(String frame) {
            super();
            this.frame = frame;
        }

        @Override
        public boolean isCellEditable( EventObject e ) {
            // only edit with double-click
            return !(e instanceof MouseEvent) || ((MouseEvent) e).getClickCount() >= 2;
        }

        private void handleKeyStroke(final KeyStroke keyStroke, JTable table, final int rowIndex, int vColIndex) {
            if (shortcutManager.isValidShortcut(keyStroke)) {
                if (shortcutManager.isFreeShortcut(frame, keyStroke)) {
                    // update the shortcut
                    if (shortcutManager.updateShortcutObject(keyStroke, frame, rowIndex)) {
                        String shortcutText = shortcutManager.asString(keyStroke);
                        component.setText(shortcutText);
                    }
                    wrapper.setBackground(shortcutManager.getEditBgColor(frame, rowIndex));
                    stopCellEditing(); // immediately stop editing
                } else { // this shortcut is already used (!)
                    // show error color for one second
                    wrapper.setBackground(VitcoSettings.EDIT_ERROR_BG_COLOR);
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
                            wrapper.setBackground(shortcutManager.getEditBgColor(frame, rowIndex));
                        }
                    }.start();
                }
            }
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
                            handleKeyStroke(KeyStroke.getKeyStrokeForEvent(e), table, rowIndex, vColIndex);
                        }
                    });
                    // prevent further use of this keystroke (this mustn't be done async)
                    e.consume();
                }

                @Override
                public void keyReleased(KeyEvent e) {
                    e.consume(); // prevent further use of this keystroke
                }
            });
            component.setHighlighter(null); // do not show selection
            // NOTE: We can't use "setEditable" b/c then global shortcuts would not be deactivated
            component.setCaretColor(new Color(0, 0, 0, 0)); // hide the caret
            component.setCursor(Cursor.getDefaultCursor()); // just show the ordinary mouse cursor
            component.setOpaque(false);
            component.setForeground(VitcoSettings.EDIT_TEXT_COLOR); // set text color when edit

            // Allow for clearing the shortcut
            JideButton clearButton = new JideButton();
            clearButton.setIcon(new SaveResourceLoader("resource/img/icons/clear.png").asIconImage());
            clearButton.setButtonStyle(JideButton.FLAT_STYLE);
            clearButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    handleKeyStroke(null, table, rowIndex, vColIndex);
                }
            });

            // holds edit field and clear button
            wrapper = new JPanel();
            wrapper.setLayout(new BorderLayout());
            wrapper.add(component, BorderLayout.CENTER);
            wrapper.add(clearButton, BorderLayout.EAST);
            wrapper.setBackground(shortcutManager.getEditBgColor(frame, rowIndex)); // bg color when edit

            return wrapper;
        }

        public Object getCellEditorValue() {
            return component.getText(); // return new value
        }
    }

    // internal - takes shortcuts, col names and frame key
    // frame key can be null if we handle global shortcuts
    // global flag
    private JTable createTab(String[][] data, String[] columnNames) {
        // create the default model for data and column names
        DefaultTableModel model = new DefaultTableModel(data, columnNames);
        // create table, only allow editing for second column (shortcuts)
        JCustomTable shortcut_table = new JCustomTable(model) {
            @Override
            public boolean isCellEditable(int rowIndex, int colIndex) {
                // only allow editing shortcuts
                return colIndex == 1;
            }
        };

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
        TableRenderer tableRenderer = new TableRenderer();
        shortcut_table.getColumnModel().getColumn(0).setCellRenderer(tableRenderer);
        shortcut_table.getColumnModel().getColumn(1).setCellRenderer(tableRenderer);
        return shortcut_table;
    }

    // tab to customize key shortcuts
    private JTable createKeyTab(String[][] data, String[] columnNames, String frameKey) {
        JTable shortcut_table = createTab(data, columnNames);
        // create a new custom cell editor for the second column (shortcuts)
        shortcut_table.getColumnModel().getColumn(1).setCellEditor(new ShortcutManagerView.KeyCellEditor(frameKey));
        return shortcut_table;
    }

    // mouse cell edit action
    private class MouseCellEditor extends AbstractCellEditor implements TableCellEditor {

        final TreeMap<String, String> actionLookup = new TreeMap<>();
        {
            actionLookup.put("rotate", "Rotate Camera");
            actionLookup.put("truck", "Truck Camera");
        }

        // handles the editing of the cell value
        JPanel wrapper;
        JideComboBox component;

        public MouseCellEditor() {
            super();
        }

        @Override
        public boolean isCellEditable( EventObject e ) {
            // only edit with double-click
            return !(e instanceof MouseEvent) || ((MouseEvent) e).getClickCount() >= 2;
        }

        // start editing
        public Component getTableCellEditorComponent(final JTable table, Object value,
                                                     boolean isSelected, final int rowIndex, final int vColIndex) {
            component = new JideComboBox(actionLookup.values().toArray());
            component.setFont(VitcoSettings.TABLE_FONT_BOLD);
            component.addItemListener(e -> {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    preferences.storeString("mbutton" + (rowIndex + 1), (String) actionLookup.keySet().toArray()[component.getSelectedIndex()]);
                }
            });
            component.setSelectedIndex(new ArrayList<>(actionLookup.keySet()).indexOf(preferences.loadString("mbutton" + (rowIndex + 1))));

            component.setCursor(Cursor.getDefaultCursor()); // just show the ordinary mouse cursor
            component.setOpaque(false);
            component.setForeground(VitcoSettings.EDIT_TEXT_COLOR); // set text color when edit

            // holds drop down select
            wrapper = new JPanel();
            wrapper.setLayout(new BorderLayout());
            wrapper.add(component, BorderLayout.CENTER);
            wrapper.setBackground(shortcutManager.getEditBgColor(null, rowIndex)); // bg color when edit

            return wrapper;
        }

        public Object getCellEditorValue() {
            return actionLookup.values().toArray()[component.getSelectedIndex()];
        }
    }

    // tab to customize mouse shortcuts
    private JTable createMouseTab() {
        MouseCellEditor mouseCellEditor = new ShortcutManagerView.MouseCellEditor();
        String[] columnNames = new String[] {
                langSelector.getString("shortcut_mg_header_mbutton"),
                langSelector.getString("shortcut_mg_header_action")
        };
        // set default preferences
        for (String[] btn : new String[][] {
                new String[] {"mbutton1", "rotate"},
                new String[] {"mbutton2", "rotate"},
                new String[] {"mbutton3", "truck"}
        }) {
            if (!preferences.contains(btn[0])) {
                preferences.storeString(btn[0], btn[1]);
            }
        }
        String[][] data = new String[][] {
                new String[] {langSelector.getString("mouse_button_left_caption"), mouseCellEditor.actionLookup.get(preferences.loadString("mbutton1"))},
                new String[] {langSelector.getString("mouse_button_middle_caption"), mouseCellEditor.actionLookup.get(preferences.loadString("mbutton2"))},
                new String[] {langSelector.getString("mouse_button_right_caption"), mouseCellEditor.actionLookup.get(preferences.loadString("mbutton3"))}
        };
        JTable shortcut_table = createTab(data, columnNames);
        // create a new custom cell editor for the second column (shortcuts)
        shortcut_table.getColumnModel().getColumn(1).setCellEditor(new ShortcutManagerView.MouseCellEditor());
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
        tabbedPane.addTab(langSelector.getString("global_shortcuts_caption"),
                new JCustomScrollPane(createKeyTab(globalShortcuts, columnNames, null)));
        // add mouse shortcuts
        tabbedPane.addTab(
                langSelector.getString("mouse_shortcuts_caption"),
                new JCustomScrollPane(createMouseTab())
        );
        // add the frame shortcuts
        String[][] frames = shortcutManager.getFrames();
        for (String[] frame : frames) {
            // create the list of shortcuts for this frame
            String[][] data = shortcutManager.getShortcuts(frame[0]); // the shortcuts (list)
            if (data.length > 0) { // only create tab if it has shortcuts
                // add this tab to the tabbedPane
                tabbedPane.addTab(frame[1], new JCustomScrollPane(createKeyTab(data, columnNames, frame[0])));
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
