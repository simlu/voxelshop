package com.vitco.util.action;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Basic functionality for actions
 */
public abstract class ActionManagerPrototype<T> implements ActionManagerInterface<T> {
    // maps strings to action
    private final Map<String, T> map = new HashMap<String, T>();

    // maps action names to threads
    private final Map<String, ArrayList<Runnable>> actionQueStack = new HashMap<String, ArrayList<Runnable>>();

    @Override
    public final void performWhenActionIsReady(String action, Runnable thread) {
        if (map.containsKey(action)) {
            thread.run(); // the action is already ready
        } else {
            // we need to wait till the action is ready
            ArrayList<Runnable> value;
            if (actionQueStack.containsKey(action)) {
                value = actionQueStack.get(action);
            } else {
                value = new ArrayList<Runnable>();
            }
            value.add(thread);
            actionQueStack.put(action, value);
        }
    }

    // allows to register an action
    @Override
    public final void registerAction(String key, T action) {
        if (map.containsKey(key)) {
            System.err.println("Error: The action \"" + key + "\" (" + getClassName() + ") is already registered!");
        } else {
            map.put(key, action);
            if (actionQueStack.containsKey(key)) { // run the thread that was waiting for this action
                ArrayList<Runnable> value = actionQueStack.get(key);
                for (Runnable thread : value) {
                    // note: this can create an error if the action is not
                    // the extended type of AbstractAction that was
                    // expected (e.g. dummy action)
                    thread.run();
                }
                actionQueStack.remove(key); // free the key
            }
        }

    }

    // allows to retrieve an action for a key
    @Override
    public final T getAction(String key) {
        if (map.containsKey(key)) {
            return map.get(key);
        } else {
            System.err.println("Error: The action \"" + key + "\" (" + getClassName() + ") is not registered!");
            return null;
        }
    }

    // ===========================
    // BELOW ERROR CHECKING

    protected abstract T getDummyAction(String actionName);
    protected abstract String getClassName();

    // holds action names, to detect possible errors
    private final ArrayList<String> actionNames = new ArrayList<String>();
    // validate things
    @Override
    public final boolean performValidityCheck() {
        boolean result = true;
        for (final String actionName : actionNames) {
            if (!map.containsKey(actionName)) {
                System.err.println("Error: The action \"" + actionName + "\" (" + getClassName() + ") is not registered!");
                System.err.println("Creating dummy action.");
                // register dummy action
                registerAction(actionName, getDummyAction(actionName));
                result = false;
            }
        }
        for (String key : map.keySet()) {
            if (!actionNames.contains(key)) {
                System.err.println("Error: The action \"" + key + "\" (" + getClassName() + ") is never used!");
            }
        }
        // this should always be empty as a dummy action is registered above
        // (unless the action is never marked as used)
        for (String key : actionQueStack.keySet()) {
            System.err.println("Error: The action \"" + key + "\" (" + getClassName() + ") was never registered!");
            System.err.println("Did you forget to register that the action is used?!");
        }
        return result;
    }

    @Override
    public final void registerActionIsUsed(String key) {
        // only need to register this action name one (several pieces of code
        // might be executing this action!)
        if (!actionNames.contains(key)) {
            actionNames.add(key);
        }
    }
}
