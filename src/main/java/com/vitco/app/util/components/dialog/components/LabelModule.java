package com.vitco.app.util.components.dialog.components;

import com.jidesoft.swing.JideLabel;
import com.vitco.app.util.components.dialog.BlankDialogModule;

import javax.swing.*;
import javax.swing.text.View;
import java.awt.*;

/**
 * A simple label module
 */
public class LabelModule extends BlankDialogModule {

    private static final JLabel resizer = new JLabel();

    // Returns the preferred size to set a component at in order to render an html string.
    // Allows to specify the size of one dimension.
    public static Dimension getPreferredSize(String html, boolean width, int prefSize) {
        resizer.setText(html);
        View view = (View) resizer.getClientProperty(javax.swing.plaf.basic.BasicHTML.propertyKey);
        view.setSize(width ? prefSize : 0, width ? 0 : prefSize);
        float w = view.getPreferredSpan(View.X_AXIS);
        float h = view.getPreferredSpan(View.Y_AXIS);
        return new java.awt.Dimension((int) Math.ceil(w), (int) Math.ceil(h));
    }

    // constructor
    public LabelModule(String text) {
        super("");
        setLayout(new BorderLayout());
        JideLabel label = new JideLabel("<html>" + text);
        this.add(label, BorderLayout.WEST);
        // set the size (so it is not dynamic!)
        // this is very important to have the height constant when the object is not rendered yet
        Dimension prefSize = getPreferredSize(label.getText(), true, 450);
        label.setPreferredSize(prefSize);

    }

    @Override
    public String getValue(String identifier) {
        // nothing to return
        return null;
    }
}
