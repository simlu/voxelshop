package com.vitco.app.manager.action.types;

import javax.swing.*;
import java.awt.event.ActionEvent;

public abstract class SwitchActionPrototype extends AbstractAction {

    @Override
    public void actionPerformed(ActionEvent e) {
        switchOn();
    }

    public abstract void switchOn();
    public abstract void switchOff();
}
