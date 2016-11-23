package com.vitco.app.manager.action;

import javax.swing.*;
import java.awt.*;

/**
 * Manages complex actions, which are custom arrangements of components to execute complex actions.
 * These are usually shown in JideSplitButtons.
 */
public class ComplexActionManager extends ActionManagerPrototype<Component> {

    @Override
    protected Component getDummyAction(String actionName) {
        return new JLabel("Complex Action Dummy: " + actionName);
    }

    @Override
    protected String getClassName() {
        return "Component";
    }

}
