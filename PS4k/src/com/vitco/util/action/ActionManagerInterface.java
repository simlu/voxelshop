package com.vitco.util.action;

import com.vitco.actions.ActionInterface;

/**
 * Created with IntelliJ IDEA.
 * User: VM Win 7
 * Date: 8/2/12
 * Time: 6:57 PM
 * To change this template use File | Settings | File Templates.
 */
public interface ActionManagerInterface {

    public void registerAction(String key, ActionInterface action);
    public ActionInterface getAction(String key);
}
