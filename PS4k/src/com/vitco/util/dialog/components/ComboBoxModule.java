package com.vitco.util.dialog.components;

import com.jidesoft.swing.JideComboBox;
import com.vitco.util.dialog.BlankDialogModule;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;

/**
 * Combo Box Module.
 */
public class ComboBoxModule extends BlankDialogModule {

    // the combo box
    private final JideComboBox comboBox;

    // list that maps ids to identifier (for passed fieldSet Array)
    private final HashMap<Integer, String> id2Identifier = new HashMap<Integer, String>();

    // constructor
    public ComboBoxModule(String identifier, String[][] values, int selected) {
        super(identifier);
        setLayout(new BorderLayout());

        // -----------

        // -- create combo box
        String[] displayedStrings = new String[values.length];
        String longestString = "";
        for (int i = 0; i < values.length; i++) {
            // set drop down text entries
            displayedStrings[i] = values[i][1];
            // select longest string
            if (longestString.length() < displayedStrings[i].length()) {
                longestString = displayedStrings[i];
            }
            // add identifier
            id2Identifier.put(i, values[i][0]);
        }

        // create the combo box
        comboBox = new JideComboBox(displayedStrings) {
            @Override
            public Dimension getPreferredSize() {
                Dimension dimension = super.getPreferredSize();
                // add width to prevent dots in menu items (they show at the end otherwise!)
                return new Dimension(dimension.width + 30, dimension.height);
            }
        };
        // disable focus for combo box
        comboBox.setFocusable(false);
        // make sure the combo box is "long enough"
        comboBox.setPrototypeDisplayValue(longestString);
        // validate and set selected index
        selected = Math.min(displayedStrings.length - 1, Math.max(0, selected));
        comboBox.setSelectedIndex(selected);
        // listen to select events of combo box
        comboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // notify that the content has changed
                notifyContentChanged();
            }
        });

        // ---------

        add(comboBox, BorderLayout.WEST);
    }

    // get the value identifier of the selected entry
    @Override
    protected Object getValue(String identifier) {
        return id2Identifier.get(comboBox.getSelectedIndex());
    }
}
