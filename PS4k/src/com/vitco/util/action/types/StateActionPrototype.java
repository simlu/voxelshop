package com.vitco.util.action.types;

import com.vitco.util.action.ChangeListener;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;

/**
 * This implements AbstractAction and can be queried for status (true or false)
 */
public abstract class StateActionPrototype extends AbstractAction {
    // listener
    protected final ArrayList<ChangeListener> listener = new ArrayList<ChangeListener>();
    public void addChangeListener(ChangeListener changeListener) {
        listener.add(changeListener); // add
        changeListener.actionFired(getStatus()); // update already directly
    }

    @Override
    public final void actionPerformed(ActionEvent e) {
        action(e); // first the custom action is executed
        // now we notify all listeners
        boolean status = getStatus();
        for (ChangeListener actionListener : listener) {
            actionListener.actionFired(status);
        }
    }

    public abstract void action(ActionEvent e);
    public abstract boolean getStatus();
}
