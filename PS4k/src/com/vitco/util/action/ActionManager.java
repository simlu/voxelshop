package com.vitco.util.action;

import javax.swing.*;
import java.awt.event.ActionEvent;

/**
 * maps strings to actions, allows for checks (unused actions, undeclared actions)
 */
public class ActionManager extends ActionManagerPrototype<AbstractAction> {
    @Override
    protected AbstractAction getDummyAction(final String actionName) {
        return new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("Dummy Action \"" + actionName + "\"");
            }
        };
    }

    @Override
    protected String getClassName() {
        return "AbstractAction";
    }
}
