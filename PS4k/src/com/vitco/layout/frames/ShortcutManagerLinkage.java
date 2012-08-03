package com.vitco.layout.frames;

import com.jidesoft.docking.DockableFrame;
import com.vitco.actions.StateActionInterface;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * Created with IntelliJ IDEA.
 * User: VM Win 7
 * Date: 7/30/12
 * Time: 1:08 PM
 * To change this template use File | Settings | File Templates.
 */
public class ShortcutManagerLinkage extends FrameLinkagePrototype {
    @Override
    public DockableFrame buildFrame(String key) {
        frame = new DockableFrame(key, null);

        frame.addPropertyChangeListener("title", new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                String title = langSelector.getString("shortcut_mg_btn");
                if (evt.getNewValue() != title) {
                    frame.setTitle(title);
                    frame.setTabTitle(title);
                    frame.setSideTitle(title);
                }
            }
        });

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
            @Override
            public boolean isCellEditable(int rowIndex, int colIndex) {
                return false;
            }
        };

        frame.add(new JScrollPane(shortcut_table));

        actionManager.registerAction("shortcut-mg_state-action_show", new StateActionInterface() {
            @Override
            public boolean getStatus() {
                return isVisible();
            }

            @Override
            public void performAction() {
                toggleVisible();
            }
        });

        return frame;
    }
}
