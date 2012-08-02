package com.vitco.layout.frames;

import com.jidesoft.docking.DockableFrame;
import com.vitco.actions.ToggleButtonActionInterface;
import com.vitco.util.action.ActionManagerInterface;
import com.vitco.util.lang.LangSelectorInterface;

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
public class ShortcutManagerLinkage implements FrameLinkageInterface {
    private DockableFrame frame;

    private LangSelectorInterface langSelector;
    @Override
    public void setLangSelector(LangSelectorInterface langSelector) {
        this.langSelector = langSelector;
    }

    private ActionManagerInterface actionManager;
    @Override
    public void setActionManager(ActionManagerInterface actionManager) {
        this.actionManager = actionManager;
    }

    private void registerActions() {
        ToggleButtonActionInterface toggleButtonAction = new ToggleButtonActionInterface() {
            @Override
            public boolean getStatus() {
                return isHidden();
            }

            @Override
            public void performAction() {
                toggleHidden();
            }
        };
        actionManager.registerAction("toggle_shortcut_mg_visible", toggleButtonAction);
        actionManager.registerAction("get_shortcut_mg_visible_status", toggleButtonAction);
    }

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

        registerActions();

        return frame;
    }

    @Override
    public void toggleHidden() {
        if (frame.getDockingManager().getFrame(frame.getName()).isVisible()) {
            frame.getDockingManager().hideFrame(frame.getName());
        } else {
            frame.getDockingManager().showFrame(frame.getName());
        }
    }

    @Override
    public boolean isHidden() {
        return frame.getDockingManager().getFrame(frame.getName()).isVisible();
    }
}
