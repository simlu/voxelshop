package com.vitco.util.menu;

import com.jidesoft.action.CommandBar;
import com.vitco.util.action.ActionManagerInterface;
import com.vitco.util.error.ErrorHandlerInterface;
import com.vitco.util.lang.LangSelectorInterface;

/**
 * Generates menus from xml files and links actions to them (e.g. main menu, tool menu)
 */
public interface MenuGeneratorInterface {
    public void setLangSelector(LangSelectorInterface langSel);
    public void setActionManager(ActionManagerInterface actionManager);
    public void buildMenuFromXML(CommandBar bar, String xmlFile);
    public void setErrorHandler(ErrorHandlerInterface errorHandler);
}
