package com.vitco.util.menu;

import com.vitco.frames.shortcut.ShortcutManagerInterface;
import com.vitco.util.action.ActionManagerInterface;
import com.vitco.util.error.ErrorHandlerInterface;
import com.vitco.util.lang.LangSelectorInterface;

import javax.swing.*;

/**
 * Generates menus from xml files and links actions to them (e.g. main menu, tool menu)
 */
public interface MenuGeneratorInterface {
    public void setLangSelector(LangSelectorInterface langSel);
    public void setActionManager(ActionManagerInterface actionManager);
    public void buildMenuFromXML(JComponent jComponent, String xmlFile);
    public void setErrorHandler(ErrorHandlerInterface errorHandler);

    void setShortcutManager(ShortcutManagerInterface shortcutManager);
}
