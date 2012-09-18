package com.vitco.util.action;

import com.vitco.util.action.types.StateActionPrototype;

/**
 * Handles groups of actions that belong together and interfere with each other.
 *
 * E.g. pressing one tool button will unselect another.
 */
public interface ActionGroupManagerInterface {
    // add to group
    void addAction(String groupName, String actionName, StateActionPrototype action);

    // refresh all action states
    void refreshGroup(String groupName);

    // register group actions
    void registerGroup(String groupName);
}
