package com.vitco.util.action;

import javax.swing.*;
import java.awt.event.ActionEvent;

/**
 * maps strings to actions, allows for checks (unused actions, undeclared actions)
 */
public interface ActionManagerInterface {
    // these methods are to detect errors!
    boolean performValidityCheck();
    void registerActionIsUsed(String key);
    // the core methods
    void registerAction(String key, AbstractAction action);
    AbstractAction getAction(String key);

    void performWhenActionIsReady(String action, Runnable thread);

    boolean tryExecuteAction(String actionName, ActionEvent e);
}
