package com.vitco.manager.action.types;

import gnu.trove.map.hash.TIntObjectHashMap;

import javax.swing.*;
import java.awt.event.ActionEvent;

public abstract class KeyActionPrototype extends AbstractAction {

    // remember all the actions we still need to trigger the release event for
    private final static TIntObjectHashMap<KeyActionPrototype> down = new TIntObjectHashMap<KeyActionPrototype>();

    // handle release for a key event
    public static void release(int keyCode) {
        synchronized (down) {
            KeyActionPrototype keyActionPrototype = down.remove(keyCode);
            if (keyActionPrototype != null) {
                keyActionPrototype.onKeyUp();
            }
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        onKeyDown();
        synchronized (down) {
            // remember, so we can release later
            down.put(((KeyActionEvent)e).keyCode, this);
        }
    }

    public abstract void onKeyDown();
    public abstract void onKeyUp();
}
