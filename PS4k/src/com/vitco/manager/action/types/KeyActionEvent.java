package com.vitco.manager.action.types;

import java.awt.event.ActionEvent;

// wrapper to remember the key code
public class KeyActionEvent extends ActionEvent {

    public final int keyCode;

    public KeyActionEvent(int keyCode, Object source, int id, String command, long when, int modifiers) {
        super(source, id, command, when, modifiers);
        this.keyCode = keyCode;
    }
}
