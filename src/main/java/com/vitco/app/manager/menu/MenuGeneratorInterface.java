package com.vitco.app.manager.menu;

import com.vitco.app.layout.content.shortcut.ShortcutManagerInterface;
import com.vitco.app.manager.action.ActionManager;
import com.vitco.app.manager.action.ComplexActionManager;
import com.vitco.app.manager.error.ErrorHandlerInterface;
import com.vitco.app.manager.lang.LangSelectorInterface;

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
