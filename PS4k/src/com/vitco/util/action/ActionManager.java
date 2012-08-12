package com.vitco.util.action;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * maps strings to actions, allows for checks (unused actions, undeclared actions)
 */
public class ActionManager implements ActionManagerInterface {

    // maps strings to action
    private final Map<String, AbstractAction> map = new HashMap<String, AbstractAction>();

    // allows to register an action
    @Override
    public void registerAction(String key, AbstractAction action) {
        if (map.containsKey(key)) {
            System.err.println("Error: The action \"" + key + "\" is already registered!");
        } else {
            map.put(key, action);
        }

    }

    // allows to retrieve an action for a key
    public AbstractAction getAction(String key) {
        if (map.containsKey(key)) {
            return map.get(key);
        } else {
            System.err.println("Error: The action \"" + key + "\" is not registered!");
            return null;
        }
    }

    // validate things
    @Override
    public boolean performValidityCheck() {
        boolean result = true;
        for (final String actionName : actionNames) {
            if (!map.containsKey(actionName)) {
                System.err.println("Error: The action \"" + actionName + "\" is not registered!");
                System.err.println("Creating dummy action.");
                // register dummy action
                map.put(actionName, new AbstractAction() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        System.out.println("Dummy Action \"" + actionName + "\"");
                    }
                });
                result = false;
            }
        }
        for (String key : map.keySet()) {
            if (!actionNames.contains(key)) {
                System.err.println("Error: The action \"" + key + "\" is never used!");
            }
        }
        return result;
    }

    // holds action names, to detect possible errors
    private final ArrayList<String> actionNames = new ArrayList<String>();
    @Override
    public void registerActionName(String key) {
        // only need to register this action name one (several pieces of code
        // might be executing this action!)
        if (!actionNames.contains(key)) {
            actionNames.add(key);
        }
    }
}
