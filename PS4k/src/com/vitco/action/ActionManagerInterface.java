package com.vitco.action;

import javax.swing.*;

/**
 * Created with IntelliJ IDEA.
 * User: VM Win 7
 * Date: 8/2/12
 * Time: 6:57 PM
 * To change this template use File | Settings | File Templates.
 */
public interface ActionManagerInterface {
    // these methods are to detect errors!
    public boolean performValidityCheck();
    public void registerActionName(String key);
    // the core methods
    public void registerAction(String key, AbstractAction action);
    public AbstractAction getAction(String key);
}
