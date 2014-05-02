package com.vitco.util.dialog.components;

import com.jidesoft.swing.JideLabel;
import com.vitco.util.dialog.BlankDialogModule;

import java.awt.*;

/**
 * A simple label module
 */
public class LabelModule extends BlankDialogModule {

    // constructor
    public LabelModule(String text) {
        super("");
        setLayout(new BorderLayout());
        // create text label
        JideLabel label = new JideLabel(text);
        this.add(label, BorderLayout.WEST);
    }

    @Override
    public Object getValue(String identifier) {
        // nothing to return
        return null;
    }
}
