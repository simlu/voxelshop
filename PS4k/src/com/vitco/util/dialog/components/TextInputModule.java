package com.vitco.util.dialog.components;

import com.vitco.util.components.JCustomTextField;
import com.vitco.util.components.TextChangeListener;
import com.vitco.util.dialog.BlankDialogModule;

import javax.swing.*;
import java.awt.*;
import java.io.File;

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
    public TextInputModule(String identifier, String caption, File initFile, boolean requireText) {
        super(identifier);
        setLayout(new BorderLayout());
        // create label
        JLabel textLabel = new JLabel(caption);
        // add spacing to the right
        textLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10));
        add(textLabel, BorderLayout.WEST);
        // create input field
        if (initFile.isFile()) {
            // only add if this is a file (and not a folder)
            textField.setText(initFile.getName());
        }
        add(textField, BorderLayout.CENTER);

        // only proceed if text is required for this component to be ready
        if (requireText) {
            // set the initial ready state
            setReady(!textField.getText().trim().equals(""));

            // listen to edit events
            textField.addTextChangeListener(new TextChangeListener() {
                @Override
                public void onChange() {
                    setReady(!textField.getText().trim().equals(""));
                }
            });
        }
        // listen to change events
        textField.addTextChangeListener(new TextChangeListener() {
            @Override
            public void onChange() {
                notifyContentChanged();
            }
        });
    }

    // get the value of this object
    @Override
    protected Object getValue(String identifier) {
        return textField.getText();
    }
}
