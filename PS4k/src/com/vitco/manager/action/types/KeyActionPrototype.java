package com.vitco.manager.action.types;

import javax.swing.*;
import java.awt.event.ActionEvent;

public abstract class KeyActionPrototype extends AbstractAction {

    @Override
    public void actionPerformed(ActionEvent ignored) {}

    public abstract void onKeyDown();
    public abstract void onKeyUp();
}
