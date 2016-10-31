package com.vitco.util.components.dialog;

import com.vitco.layout.content.console.ConsoleInterface;
import com.vitco.util.components.dialog.components.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;

/**
 * UserInputDialog
 *
 * This is a custom dialog popup that allows the user to enter specific information.
 *
 * There are object and buttons that can be added to this dialog. Buttons are displayed
 * to "finish" the dialog. The listener of this dialog will be notified with the
 * result state of the button.
 * The components are used by the user to enter information. Their value is accessible
 * through this class by using their id tree.
 *
 * Note: If the window was closed by using the "X" button the passed cancelFlag is returned.
 */
public class UserInputDialog extends JDialog {

    // cancel flag for this dialog
    private final int cancelFlag;

    // reference to this instance
    private UserInputDialog thisInstance = this;

    // right side (where the buttons are)
    private final JPanel buttons = new JPanel();

    // right side (bottom)
    private final JPanel links = new JPanel();

    // main content (where user enters information)
    private final BlankDialogModule content = new BlankDialogModule("root");

    // list of all "non cancel" buttons (used to en/disable them)
    private final ArrayList<DialogButton> nonCancelButtons = new ArrayList<DialogButton>();

    // listener of this dialog
    private UserInputDialogListener userInputDialogListener = null;

    // set a listener for this dialog (will overwrite existing listener if present)
    public final void setListener(UserInputDialogListener userInputDialogListener) {
        this.userInputDialogListener = userInputDialogListener;
    }

    // constructor
    public UserInputDialog(Frame owner, String title, final int cancelFlag) {
        // make this dialog block all user content (that is behind it)
        super(owner, title, Dialog.ModalityType.DOCUMENT_MODAL);
        // set layout for the dialog
        setLayout(new BorderLayout());

        // store cancel flag
        this.cancelFlag = cancelFlag;

        // prevent resize
        setResizable(false);

        // create formatting for and add right side (buttons)
        JPanel buttonWrapperPanel = new JPanel();
        buttonWrapperPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        buttonWrapperPanel.setLayout(new BorderLayout());
        JPanel innerWrapper = new JPanel();
        innerWrapper.setLayout(new BorderLayout());
        innerWrapper.add(buttons, BorderLayout.NORTH);
        innerWrapper.add(links, BorderLayout.SOUTH);
        buttons.setLayout(new GridBagLayout());
        links.setBorder(BorderFactory.createEmptyBorder(44, 0, 6, 10));
        links.setLayout(new GridBagLayout());
        buttonWrapperPanel.add(innerWrapper, BorderLayout.NORTH);
        this.add(buttonWrapperPanel, BorderLayout.EAST);

        // create formatting for and add left side (user content)
        JPanel contentWrapperPanel = new JPanel();
        contentWrapperPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        contentWrapperPanel.setLayout(new BorderLayout());
        contentWrapperPanel.add(content, BorderLayout.NORTH);
        this.add(contentWrapperPanel, BorderLayout.CENTER);
        content.setLayout(new GridBagLayout());

        // listen to state change events
        content.addListener(new DialogModuleChangeAdapter() {
            @Override
            public void onReadyStateChanged() {
                refreshButtonEnabled();
            }

            @Override
            public void onContentChanged() {
                // refresh the state of all components
                content.refreshState(content);
            }
        });

        // add "close" listener (when "X" button is pressed)
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);
                if (userInputDialogListener != null) {
                    userInputDialogListener.onClose(cancelFlag);
                }
            }
        });
    }

    // recheck if the "submit" buttons of this form should be enabled or not
    private void refreshButtonEnabled() {
        // check if there is a component that is currently disabled
        boolean globalReady = content.isReady();
        // set buttons
        for (DialogButton button : nonCancelButtons) {
            button.setEnabled(globalReady);
        }
    }

    // --------------------------

    // display/hide this dialog
    @Override
    public void setVisible(boolean flag) {
        if (flag) {
            // minimize the size (especially the height!)
            this.pack();
            // set default size
            this.setSize(630, getHeight());
            // set position to center
            this.setLocationRelativeTo(getParent());
            // check if buttons are enabled or not
            refreshButtonEnabled();
            // check states
            content.refreshState(content);
        }
        // show/hide
        super.setVisible(flag);
    }

    // get a value of a contained object (or return null if the
    // object was not found). This uses the fieldSet identifier
    // together with the object identifier, separated by a dot.
    // E.g. "fieldD=SetId.objectId.objectId...."
    // Can not return null
    public final String getValue(String identifier) {
        String result = content.getValue(identifier);
        return result == null ? "" : result;
    }

    // check that an expression is true (simple equal)
    public final boolean is(String expression) {
        String[] pair = expression.split("=", 2);
        if (pair.length == 2) {
            String actualValue = getValue(pair[0]);
            if (pair[1].equals(actualValue)) {
                return true;
            }
        }
        return false;
    }

    // ---------------------

    // add a button to this dialog (buttons are used to close
    // the dialog (and notify the listeners)
    public void addButton(String caption, final int resultFlag) {
        final DialogButton button = new DialogButton(caption);
        if (resultFlag != cancelFlag) {
            nonCancelButtons.add(button);
        }
        // create "close" action that notifies the listener
        button.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (button.isEnabled()) {
                    // allow the onClose action to "abort" closing
                    if (userInputDialogListener == null
                            || userInputDialogListener.onClose(resultFlag)
                            || resultFlag == cancelFlag) {
                        thisInstance.setVisible(false);
                    }
                }
            }
        });
        // add to this dialog (button pane)
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.insets = new Insets(5,0,5,0);
        buttons.add(button, gbc);
    }

    public void addLink(ConsoleInterface console, String caption, String url) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        if (links.getComponents().length == 0) {
            links.add(new SeparatorModule("Links"), gbc);
        }
        links.add(new LinkButton(console, "", caption, url), gbc);
    }

    // add a fieldSet to this input dialog (a fieldSet holds a set of components
    // that can be used by the user to enter information into this dialog)
    public void addFieldSet(FieldSet fieldSet) {
        // add to internal list (for later access) but don't display
        // (the wrapper will be used to display instead)
        content.addModule(fieldSet, false);
        // set border with title
        FieldSetWrapper fieldSetWrapper = new FieldSetWrapper(fieldSet);
        // add to this dialog (content pane)
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.weightx = 1;
        content.add(fieldSetWrapper, gbc);
    }

    // add several fieldSets that are displayed as a dropdown menu
    public void addComboBox(String identifier, FieldSet[] comboBoxFieldSets, int selected) {
        // add to internal list (for later access) but don't display
        // (the wrapper will be used to display instead)
        for (FieldSet fieldSet : comboBoxFieldSets) {
            content.addModule(fieldSet, false);
        }
        // create a drop down field set
        FieldSetWrapper fieldSetWrapper = new FieldSetWrapper(identifier, comboBoxFieldSets, selected);
        // listen to the wrapper select events (when a combo title box changes)
        // this can update the enabled buttons
        fieldSetWrapper.addListener(new DialogModuleChangeAdapter() {
            @Override
            public void onContentChanged() {
                refreshButtonEnabled();
            }
        });
        // add to this dialog (content pane)
        content.addModule(fieldSetWrapper, true);
    }

    // retrieve serialization of this object
    public final ArrayList<String[]> getSerialization() {
        return content.getSerialization("");
    }

    // load serialization
    public final void loadSerialization(ArrayList<String[]> ser) {
        for (String[] pair : ser) {
            if (pair.length == 2) {
                content.loadValue(pair);
            }
        }
    }
}
