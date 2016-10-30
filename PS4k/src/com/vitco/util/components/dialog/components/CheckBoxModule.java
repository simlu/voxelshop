package com.vitco.util.components.dialog.components;

import com.vitco.util.components.dialog.BlankDialogModule;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.font.TextAttribute;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Module that has a checkbox
 */
public class CheckBoxModule extends BlankDialogModule {

    private static class JCustomCheckBox extends JCheckBox {
        public JCustomCheckBox(String s) {
            super(s);
        }

        @Override
        public void paint(Graphics g) {
            if (this.isEnabled() || !this.isSelected()) {
                super.paint(g);
            } else {
                this.setSelected(false);
                super.paint(g);
                this.setSelected(true);
            }
        }
    }

    // the checkbox reference
    private final JCustomCheckBox checkbox;

    // constructor
    public CheckBoxModule(final String identifier, String caption, final boolean checked) {
        super(identifier);
        setLayout(new BorderLayout());
        // add checkbox
        checkbox = new JCustomCheckBox(" " + caption); // add a bit more spacing
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
        // NOTE: we serialize the check state of the checkbox, so disabled checkboxes are serialized with the check state (!)
        keyValuePair.add(new String[] {path, String.valueOf(checkbox.isSelected())});
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
