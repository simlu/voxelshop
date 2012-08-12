package com.vitco.logic.frames.shortcut;

import com.vitco.res.color.VitcoColor;
import com.vitco.util.lang.LangSelectorInterface;
import com.vitco.util.pref.PreferencesInterface;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.swing.*;
import javax.swing.border.Border;
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
public class ShortcutManagerView implements ShortcutManagerViewInterface {

    // the default border
    private final Border defaultBorder = BorderFactory.createEmptyBorder(0, 10, 0, 0);

    // last hover cell
    private int curRow = -1;
    private int curCol = -1;

    // active tab
    private int selectedIndex = 0;

    // var & setter
    private PreferencesInterface preferences;
    @Override
    public void setPreferences(PreferencesInterface preferences) {
        this.preferences = preferences;
    }

    // var & setter
    private ShortcutManagerInterface shortcutManager;
    @Override
    public void setShortcutManager(ShortcutManagerInterface shortcutManager) {
        this.shortcutManager = shortcutManager;
    }

    // var & setter
    private LangSelectorInterface langSelector;
    @Override
    public void setLangSelector(LangSelectorInterface langSelector) {
        this.langSelector = langSelector;
    }

    // layout of cells
    private class TableRenderer extends DefaultTableCellRenderer {

        // var & setter (constructor)
        final String frame; // store frame for shortcut collision check
        public TableRenderer(String frame) {
            super();
            this.frame = frame;
        }

        // render table cell
        @Override
        public Component getTableCellRendererComponent(
                JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column
        ) {
            setForeground(VitcoColor.DEFAULT_TEXT_COLOR); // set text color
            if (row == curRow && column == curCol && column == 1) {
                setBackground(VitcoColor.DEFAULT_HOVER_COLOR); // hover effect
                setToolTipText(langSelector.getString("dbl-clc-to-edit_tooltip"));
            } else {
                setBackground(VitcoColor.DEFAULT_BG_COLOR);
            }
            setBorder(defaultBorder); // padding
            setValue(value.toString()); // set the value
            return this;
        }
    }

    // cell edit action
    private class CellEditor extends AbstractCellEditor implements TableCellEditor {

        // handles the editing of the cell value
        JTextArea component;
        // var & setter (constructor)
        private final String frame; // so we have a reference to the current frame
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
        public Component getTableCellEditorComponent(JTable table, Object value,
                                                     boolean isSelected, final int rowIndex, int vColIndex) {
            component = new JTextArea();
            component.setText((String) value); // set initial text
            component.setBorder(defaultBorder);
            component.setFont(
                    new Font(
                            component.getFont().getName(),
                            Font.BOLD,
                            component.getFont().getSize()
                    )
            );
            // add the listener
            component.addKeyListener(new KeyListener() {
                @Override
                public void keyTyped(KeyEvent e) {
                    e.consume(); // prevent further use of this keystroke
                }

                @Override
                public void keyPressed(KeyEvent e) {
                    KeyStroke keyStroke = KeyStroke.getKeyStrokeForEvent(e);
                    if (shortcutManager.isValidShortcut(keyStroke)) {
                        if (shortcutManager.isFreeShortcut(frame, keyStroke)) {
                            // update the shortcut
                            if (shortcutManager.updateShortcutObject(keyStroke, frame, rowIndex)) {
                                component.setText(
                                        shortcutManager.asString(keyStroke)
                                );
                            }
                            component.setBackground(VitcoColor.EDIT_BG_COLOR);
                        } else { // this shortcut is already used (!)
                            // show error color for one second
                            component.setBackground(VitcoColor.EDIT_ERROR_BG_COLOR);
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
                                    component.setBackground(VitcoColor.EDIT_BG_COLOR);
                                }
                            }.start();
                        }
                    }
                    e.consume(); // prevent further use of this keystroke
                }

                @Override
                public void keyReleased(KeyEvent e) {
                    e.consume(); // prevent further use of this keystroke
                }
            });
            component.setHighlighter(null); // do not show selection
            component.setEditable(false); // hide the caret
            component.setBackground(VitcoColor.EDIT_BG_COLOR); // bg color when edit
            component.setForeground(VitcoColor.EDIT_TEXT_COLOR); // set text color when edit
            return component;
        }

        // edit complete
        public Object getCellEditorValue() {
            return component.getText(); // return new value
        }
    }

    // return a JTabbedPane that is autonomous and manages shortcuts
    @Override
    public JTabbedPane getEditTables() {
        // create the content of this frame
        // all frames names
        String[][] frames = shortcutManager.getFrames();
        // default header
        String[] columnNames = {
                langSelector.getString("shortcut_mg_header_action"),
                langSelector.getString("shortcut_mg_header_shortcut")
        };
        // the different frame shortcuts are in different tabs
        final JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFocusable(false); // looks nicer
        for (String[] frame : frames) {
            // create the list of shortcuts for this frame
            String[][] data = shortcutManager.getShortcuts(frame[0]); // the shortcuts (list)
            if (data.length > 0) { // only create tab if it has shortcuts
                // create the default model
                DefaultTableModel model = new DefaultTableModel(data, columnNames);
                // create table, only allow editing for second column (shortcuts)
                JTable shortcut_table = new JTable(model) {
                    @Override
                    public boolean isCellEditable(int rowIndex, int colIndex) {
                        // only allow editing hotkeys
                        return colIndex == 1;
                    }
                };
                shortcut_table.setCellSelectionEnabled(false); // disable selection in table
                shortcut_table.setFocusable(false); // disable focus on cells
                shortcut_table.setBackground(VitcoColor.DEFAULT_BG_COLOR); // set the default bg color (unnecessary)
                // add hover effect
                shortcut_table.addMouseMotionListener(new MouseMotionListener() {

                    @Override
                    public void mouseDragged(MouseEvent e) {}

                    @Override
                    public void mouseMoved(MouseEvent e) {
                        JTable aTable = (JTable)e.getSource();
                        curRow = aTable.rowAtPoint(e.getPoint());
                        curCol = aTable.columnAtPoint(e.getPoint());
                        aTable.repaint();
                    }
                });
                shortcut_table.addMouseListener(new MouseListener() {
                    @Override
                    public void mouseClicked(MouseEvent e) {}

                    @Override
                    public void mousePressed(MouseEvent e) {}

                    @Override
                    public void mouseReleased(MouseEvent e) {}

                    @Override
                    public void mouseEntered(MouseEvent e) {}

                    @Override
                    public void mouseExited(MouseEvent e) {
                        curRow = -1;
                        curCol = -1;
                        ((JTable)e.getSource()).repaint();
                    }
                });
                // custom layout for the cells
                shortcut_table.getColumnModel().getColumn(0).setCellRenderer(new TableRenderer(frame[0]));
                shortcut_table.getColumnModel().getColumn(1).setCellRenderer(new TableRenderer(frame[0]));
                // stop editing when table looses focus
                shortcut_table.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
                // create a new custom cell editor for the second column (shortcuts)
                shortcut_table.getColumnModel().getColumn(1).setCellEditor(new CellEditor(frame[0]));
                // add this tab to the tabbedPane
                tabbedPane.addTab(frame[1], new JScrollPane(shortcut_table));
            }
        }
        if (tabbedPane.getTabCount() > selectedIndex) {
            tabbedPane.setSelectedIndex(selectedIndex); // set the stored index as active
        }
        // store when active tab changes
        tabbedPane.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                selectedIndex = tabbedPane.getSelectedIndex();
            }
        });
        return tabbedPane;
    }

    // handle loading of state (selected index)
    @PostConstruct
    @Override
    public void loadStateInformation() {
        selectedIndex = preferences.loadInteger("shortcut-manager_active-tab");
    }

    // handle saving of state (selected index)
    @PreDestroy
    @Override
    public void storeStateInformation() {
        preferences.storeInteger("shortcut-manager_active-tab", selectedIndex);
    }
}
