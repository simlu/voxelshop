package com.vitco.util;

import com.jidesoft.action.CommandBar;
import com.vitco.action.ActionManagerInterface;

/**
 * Created with IntelliJ IDEA.
 * User: VM Win 7
 * Date: 8/2/12
 * Time: 7:03 PM
 * To change this template use File | Settings | File Templates.
 */
public interface MenuGeneratorInterface {
    public void setLangSelector(LangSelectorInterface langSel);
    public void setActionManager(ActionManagerInterface actionManager);
    public void buildMenuFromXML(CommandBar bar, String xmlFile);

}
