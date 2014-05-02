package com.vitco.util.dialog.components;

import com.vitco.util.dialog.BlankDialogModule;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * Module that has a checkbox
 */
public class CheckBoxModule extends BlankDialogModule {

    // the checkbox reference
    private final JCheckBox checkbox;

    // constructor
    public CheckBoxModule(String identifier, String caption) {
        super(identifier);
        setLayout(new BorderLayout());
        // add checkbox
        checkbox = new JCheckBox(" " + caption); // add a bit more spacing
        checkbox.setFocusable(false);
        // remove border (align to right side)
        checkbox.setBorder(BorderFactory.createEmptyBorder());
        // add to west so it doesn't stretch over the whole width
        add(checkbox, BorderLayout.WEST);
        // listen to checkbox event
        checkbox.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                notifyContentChanged();
            }
        });
    }

    // get the value of this object
    @Override
    protected Object getValue(String identifier) {
        return checkbox.isSelected();
    }
}
