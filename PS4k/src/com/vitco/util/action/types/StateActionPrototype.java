package com.vitco.util.action.types;

import javax.swing.*;

/**
 * This implements AbstractAction and can be queried for status (true or false)
 */
public abstract class StateActionPrototype extends AbstractAction {
    public abstract boolean getStatus();
}
