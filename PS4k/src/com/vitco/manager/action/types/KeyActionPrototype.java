package com.vitco.manager.action.types;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;

public abstract class KeyActionPrototype extends AbstractAction {

    // remember all the actions we still need to trigger the release event for
    private final static ArrayList<KeyActionPrototype> down = new ArrayList<KeyActionPrototype>();

    // handle release for a key event
    public static void release() {
        synchronized (down) {
            if (!down.isEmpty()) {
                down.remove(0).onKeyUp();
            }
            // ensure only one event is in here at most after release
            while (down.size() > 1) {
                down.remove(0).onKeyUp();
            }
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        onKeyDown();
        synchronized (down) {
            // remember, so we can release later
            down.add(this);
        }
    }

    public abstract void onKeyDown();
    public abstract void onKeyUp();
}
