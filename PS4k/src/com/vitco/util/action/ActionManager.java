package com.vitco.util.action;

import com.vitco.actions.ActionInterface;

import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: VM Win 7
 * Date: 8/2/12
 * Time: 2:05 PM
 * To change this template use File | Settings | File Templates.
 */
public class ActionManager implements ActionManagerInterface {

    // maps strings to actions
    private final Map<String, ActionInterface> map = new HashMap<String, ActionInterface>();

    // allows to register an action
    @Override
    public void registerAction(String key, ActionInterface action) {
        if (map.containsKey(key)) {
            System.err.println("Error: This action is already registered!");
            System.err.println("Action: \"" + key + "\"");
        } else {
            map.put(key, action);
        }

    }

    // allows to retrieve an action for a key
    public ActionInterface getAction(String key) {
        if (map.containsKey(key)) {
            return map.get(key);
        } else {
            System.err.println("Error: This action is note registered!");
            System.err.println("Action: \"" + key + "\"");
            return null;
        }

    }
}
