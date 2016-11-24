package com.vitco.app.manager.action;

/**
 * maps strings to actions, allows for checks (unused actions, undeclared actions)
 */
public interface ActionManagerInterface<T> {
    // these methods are to detect errors!
    boolean performValidityCheck();
    void registerActionIsUsed(String key);
    // the core methods
    void registerAction(String key, T action);
    // get action for key
    T getAction(String key);
    // retrieve keys for an action
    String[] getActionKeys(T action);

    void performWhenActionIsReady(String action, Runnable thread);
}
