package com.vitco.manager.menu;

import com.vitco.layout.content.shortcut.ShortcutManagerInterface;
import com.vitco.manager.action.ActionManager;
import com.vitco.manager.action.ComplexActionManager;
import com.vitco.manager.error.ErrorHandlerInterface;
import com.vitco.manager.lang.LangSelectorInterface;

import javax.swing.*;

/**
 * Generates menus from xml files and links actions to them (e.g. main menu, tool menu)
 */
public interface MenuGeneratorInterface {
    public void setLangSelector(LangSelectorInterface langSel);
    public void setActionManager(ActionManager actionManager);
    public void buildMenuFromXML(JComponent jComponent, String xmlFile);
    public void setErrorHandler(ErrorHandlerInterface errorHandler);

    void setShortcutManager(ShortcutManagerInterface shortcutManager);

    void setComplexActionManager(ComplexActionManager complexActionManager);
}
