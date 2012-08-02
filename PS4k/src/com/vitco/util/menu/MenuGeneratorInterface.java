package com.vitco.util.menu;

import com.jidesoft.action.CommandBar;
import com.vitco.util.action.ActionManagerInterface;
import com.vitco.util.lang.LangSelectorInterface;

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
