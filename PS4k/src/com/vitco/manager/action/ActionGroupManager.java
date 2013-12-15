package com.vitco.manager.action;

import com.vitco.manager.action.types.StateActionPrototype;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.Map;

/**
 * Handles groups of actions that belong together and interfere with each other.
 *
 * E.g. pressing one tool button will unselect another.
 */
public class ActionGroupManager implements ActionGroupManagerInterface {
    // var & setter
    protected ActionManager actionManager;
    @Autowired(required=true)
    public final void setActionManager(ActionManager actionManager) {
        this.actionManager = actionManager;
    }

    // maps group names to the actions in that group
    private final HashMap<String, HashMap<String, StateActionPrototype>> map = new HashMap<String, HashMap<String, StateActionPrototype>>();

    // refresh all action states
    @Override
    public final void refreshGroup(String groupName) {
        if (map.containsKey(groupName)) {
            for (StateActionPrototype action : map.get(groupName).values()) {
                action.refresh();
            }
        }
    }

    // register group actions
    @Override
    public final void registerGroup(String groupName) {
        if (map.containsKey(groupName)) {
            for (Map.Entry<String,StateActionPrototype> entry : map.get(groupName).entrySet()) {
                actionManager.registerAction(entry.getKey(), entry.getValue());
            }
        }
    }

    // add to group
    @Override
    public final void addAction(String groupName, String actionName, StateActionPrototype action) {
        // make sure the group is initialized
        if (!map.containsKey(groupName)) {
            map.put(groupName, new HashMap<String, StateActionPrototype>());
        }
        // add the action
        map.get(groupName).put(actionName, action);
    }

}
