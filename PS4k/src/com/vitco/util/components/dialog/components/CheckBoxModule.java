package com.vitco.util.components.dialog.components;

import com.vitco.util.components.dialog.BlankDialogModule;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.font.TextAttribute;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Module that has a checkbox
 */
public class CheckBoxModule extends BlankDialogModule {

    // the checkbox reference
    private final JCheckBox checkbox;

    // constructor
    public CheckBoxModule(String identifier, String caption, boolean checked) {
        super(identifier);
        setLayout(new BorderLayout());
        // add checkbox
        checkbox = new JCheckBox(" " + caption); // add a bit more spacing
        checkbox.setFocusable(false);
        checkbox.setSelected(checked);
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

        // file "selected" property change event
        checkbox.addItemListener(new ItemListener() {
            private boolean oldValue = checkbox.isSelected();

            public void itemStateChanged(ItemEvent e) {
                checkbox.firePropertyChange("selected", oldValue, checkbox.isSelected());
                oldValue = checkbox.isSelected();
            }
        });

        // show as deselected when disabled
        checkbox.addPropertyChangeListener(new PropertyChangeListener() {

            private Boolean checkState = null;

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (evt.getPropertyName().equals("enabled")) { // if enabled state changed
                    if (evt.getOldValue() != evt.getNewValue()) { // if it actually changes
                        if ((Boolean)evt.getNewValue()) { // if checkbox is now enabled
                            if (checkState != null) { // there is a known check state
                                checkbox.setSelected(checkState); // restore check state
                            }
                        } else {
                            checkState = checkbox.isSelected(); // remember check state
                            checkbox.setSelected(false); // uncheck b/c disabled
                        }
                    }
                }
                if (!checkbox.isEnabled() && evt.getPropertyName().equals("selected")) { // disabled and the selected state changes
                    if (evt.getOldValue() != evt.getNewValue()) { // if it actually changes
                        // this might trigger more events (which is fine b/c we set the checkState second)
                        checkbox.setSelected(false);  // uncheck b/c disabled
                        checkState = (Boolean) evt.getNewValue();  // remember the state
                    }
                }
            }
        });
    }

    // strike the text of this checkbox (but don't disable)
    public final void setStrikeThrough(boolean state) {
        Map<TextAttribute, Object> attributes = new HashMap<TextAttribute, Object>();
        attributes.put(TextAttribute.STRIKETHROUGH, state);
        checkbox.setFont(checkbox.getFont().deriveFont(attributes));
    }

    // get the value of this object
    @Override
    protected String getValue(String identifier) {
        return String.valueOf(checkbox.isEnabled() && checkbox.isSelected());
    }

    @Override
    protected ArrayList<String[]> getSerialization(String path) {
        ArrayList<String[]> keyValuePair = new ArrayList<String[]>();
        keyValuePair.add(new String[] {path, getValue(null)});
        return keyValuePair;
    }

    @Override
    protected boolean loadValue(String[] pair) {
        if (pair[0].equals("")) {
            checkbox.setSelected(pair[1].equals("true"));
            notifyContentChanged();
            return true;
        }
        return false;
    }
}
