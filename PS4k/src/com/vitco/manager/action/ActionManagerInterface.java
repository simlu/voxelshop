package com.vitco.manager.action;

/**
 * maps strings to actions, allows for checks (unused actions, undeclared actions)
 */
public interface ActionManagerInterface<T> {
    // these methods are to detect errors!
    boolean performValidityCheck();
    void registerActionIsUsed(String key);
    // the core methods
    void registerAction(String key, T action);
    T getAction(String key);

    void performWhenActionIsReady(String action, Runnable thread);
}
