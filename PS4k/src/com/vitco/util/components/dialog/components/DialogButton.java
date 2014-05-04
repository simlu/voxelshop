package com.vitco.util.components.dialog.components;

import com.jidesoft.swing.ButtonStyle;
import com.jidesoft.swing.JideButton;

import java.awt.*;

/**
 * Custom button that is used in the UserInputDialog.
 */
public class DialogButton extends JideButton {

    // constructor
    public DialogButton(String caption) {
        super(caption);
        // disable focus
        setFocusable(false);
        // make sure the border is visible
        setButtonStyle(ButtonStyle.TOOLBOX_STYLE);
        // set the text-border distance
        setMargin(new Insets(3, 20, 3, 20));
    }
}
