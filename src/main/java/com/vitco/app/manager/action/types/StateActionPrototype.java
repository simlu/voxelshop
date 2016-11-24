package com.vitco.app.manager.action.types;

import com.vitco.app.manager.action.ChangeListener;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;

/**
 * This implements AbstractAction and can be queried for status (true or false)
 */
public abstract class StateActionPrototype extends AbstractAction {
    // listener
    protected final ArrayList<ChangeListener> listener = new ArrayList<ChangeListener>();
    public final void addChangeListener(ChangeListener changeListener) {
        listener.add(changeListener); // add
        changeListener.actionFired(getStatus()); // update already directly
    }

    public final void refresh() {
        // now we notify all listeners
        boolean status = getStatus();
        for (ChangeListener actionListener : listener) {
            actionListener.actionFired(status);
        }
    }

    @Override
    public final void actionPerformed(ActionEvent e) {
        action(e); // first the custom action is executed
        refresh();
    }

    public abstract void action(ActionEvent e);
    public abstract boolean getStatus();

    public boolean isEnabled() {
        return getStatus();
    }
    public boolean isChecked() {
        return getStatus();
    }
    public boolean isVisible() {
        return getStatus();
    }
}
