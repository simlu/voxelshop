package com.vitco.util.action;

import javax.swing.*;

/**
 * maps strings to actions, allows for checks (unused actions, undeclared actions)
 */
public interface ActionManagerInterface {
    // these methods are to detect errors!
    boolean performValidityCheck();
    void registerActionName(String key);
    // the core methods
    void registerAction(String key, AbstractAction action);
    AbstractAction getAction(String key);

    void performWhenActionIsReady(String action, Runnable thread);
}
