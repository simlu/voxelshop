package com.vitco.util.dialog;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Needs to be extended by all objects that are added to a UserInputDialog.
 */
public class BlankDialogModule extends JComponent {
    // indicates ready status of component
    private boolean ready = true;

    // the child modules of this module
    private final HashMap<String, BlankDialogModule> childModules = new HashMap<String, BlankDialogModule>();

    // the identifier of this object
    private final String identifier;
    protected final String getIdentifier() {
        return identifier;
    }

    // the listener of this module
    private final ArrayList<DialogModuleChangeListener> listeners = new ArrayList<DialogModuleChangeListener>();
    // add a state change listener
    protected final void addListener(DialogModuleChangeListener listener) {
        listeners.add(listener);
    }

    // listener that is used to propagate events from all children to
    // this module (so we always receive it at the top level)
    private final DialogModuleChangeListener propagationListener = new DialogModuleChangeAdapter() {
        @Override
        public void onReadyStateChanged() {
            notifyReadyStateChanged();
        }

        @Override
        public void onContentChanged() {
            notifyContentChanged();
        }
    };

    // called when the ready state of this component has changed
    protected final void notifyReadyStateChanged() {
        for (DialogModuleChangeListener listener : listeners) {
            listener.onReadyStateChanged();
        }
    }

    // called when the content of this component has changed
    protected final void notifyContentChanged() {
        for (DialogModuleChangeListener listener : listeners) {
            listener.onContentChanged();
        }
    }

    // --------------

    // constructor
    public BlankDialogModule(String identifier) {
        // spacing below (is removed if child modules are added)
        setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        // set the layout
        setLayout(new GridBagLayout());
        // store identifer
        this.identifier = identifier;
    }

    // -------------------

    // add a child module to this module
    protected final void addModule(BlankDialogModule module, boolean display) {
        // remove spacing (the child deals with that now)
        setBorder(BorderFactory.createEmptyBorder());
        // register this module
        childModules.put(module.getIdentifier(), module);
        // inject listener
        module.addListener(propagationListener);
        // add child module to this container to be displayed
        if (display) {
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.gridwidth = GridBagConstraints.REMAINDER;
            gbc.weightx = 1;
            this.add(module, gbc);
        }
    }

    // set ready status of component
    protected final void setReady(boolean ready) {
        if (this.ready != ready) {
            this.ready = ready;
            notifyReadyStateChanged();
        }
    }

    // returns true if this component and all sub components are ready
    protected final boolean isReady() {
        // check if visible (invisible components are ok to be not ready)
        if (!this.isDisplayable()) {
            return true;
        }
        // check if this is not ready
        if (!ready) {
            return false;
        }
        // check if any child is not ready
        for (BlankDialogModule module : childModules.values()) {
            if (!module.isReady()) {
                return false;
            }
        }
        // all ready!
        return true;
    }

    // get the value of this object
    // needs to be overwritten for objects that do not
    // get their value from their children
    protected Object getValue(String identifier) {
        if (identifier == null) {
            return null;
        }
        String[] path = identifier.split("\\.", 2);
        BlankDialogModule object = childModules.get(path[0]);
        if (object != null) {
            return object.getValue(path.length > 1 ? path[1] : null);
        }
        return null;
    }

}
