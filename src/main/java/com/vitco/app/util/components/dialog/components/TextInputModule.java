package com.vitco.app.util.components.dialog.components;

import com.vitco.app.util.components.dialog.BlankDialogModule;
import com.vitco.app.util.components.textfield.JCustomTextField;
import com.vitco.app.util.components.textfield.TextChangeListener;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;

/**
 * A Text input element for the UserInputDialog
 *
 * The component has a text on the left and text input on the right.
 *
 * There is an option to require text for this module.
 */
public class TextInputModule extends BlankDialogModule {

    // the text field part
    private final JCustomTextField textField = new JCustomTextField();

    // constructor
    public TextInputModule(String identifier, String caption, String text, boolean requireText) {
        super(identifier);
        setLayout(new BorderLayout());
        // create label
        JLabel textLabel = new JLabel(caption);
        // add spacing to the right
        textLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10));
        add(textLabel, BorderLayout.WEST);
        // create input field
        textField.setText(text);
        add(textField, BorderLayout.CENTER);

        // only proceed if text is required for this component to be ready
        if (requireText) {
            // set the initial ready state
            setReady(!textField.getText().trim().equals(""));

            // listen to edit events
            textField.addTextChangeListener(new TextChangeListener() {
                @Override
                public void onChange() {
                    setReady(!textField.getText().trim().equals("") || !textField.isEnabled());
                    notifyContentChanged();
                }
            });

            // listen to enabled/disabled changes (they affect the "readyness")
            textField.addPropertyChangeListener(new PropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    if ("enabled".equals(evt.getPropertyName())) {
                        setReady(!textField.getText().trim().equals("") || !textField.isEnabled());
                        notifyContentChanged();
                    }
                }
            });
        } else {
            // listen to change events (simple)
            textField.addTextChangeListener(new TextChangeListener() {
                @Override
                public void onChange() {
                    notifyContentChanged();
                }
            });
        }
    }

    // get the value of this object
    @Override
    protected String getValue(String identifier) {
        return textField.getText();
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
            textField.setText(pair[1]);
            notifyContentChanged();
            return true;
        }
        return false;
    }
}
