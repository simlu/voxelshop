package com.vitco.layout.frames;

import com.jidesoft.docking.DockableFrame;
import com.vitco.util.lang.LangSelectorInterface;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

/**
 * Created with IntelliJ IDEA.
 * User: VM Win 7
 * Date: 7/30/12
 * Time: 1:08 PM
 * To change this template use File | Settings | File Templates.
 */
public class ShortcutManagerLinkage implements FrameLinkageInterface {
    @Override
    public DockableFrame buildFrame(String key, LangSelectorInterface langSel) {
        DockableFrame frame = new DockableFrame(key, null);

        String[][] data = {
                {"Kathy", "Smith"},
                {"John", "Doe"},
                {"Sue", "Black"},
                {"Jane", "White"},
                {"Joe", "Brown"}
        };

        String[] columnNames = {"Action", "Shortcut"};

        DefaultTableModel model = new DefaultTableModel(data,columnNames);

        JTable shortcut_table = new JTable(model) {
            //when checking if a cell is editable always return false
            @Override
            public boolean isCellEditable(int rowIndex, int colIndex) {
                return false;
            }
        };

        frame.add(new JScrollPane(shortcut_table));

        return frame;
    }
}
