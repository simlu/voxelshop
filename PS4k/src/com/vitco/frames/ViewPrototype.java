package com.vitco.frames;

import com.vitco.frames.console.ConsoleInterface;
import com.vitco.frames.shortcut.ShortcutManagerInterface;
import com.vitco.util.action.ActionManagerInterface;
import com.vitco.util.error.ErrorHandlerInterface;
import com.vitco.util.lang.LangSelectorInterface;
import com.vitco.util.menu.MenuGeneratorInterface;
import com.vitco.util.pref.PreferencesInterface;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Prototype of a view object
 */
public abstract class ViewPrototype {

    // var & setter
    protected ActionManagerInterface actionManager;
    @Autowired(required=true)
    public void setActionManager(ActionManagerInterface actionManager) {
        this.actionManager = actionManager;
    }

    // var & setter
    protected MenuGeneratorInterface menuGenerator;
    @Autowired(required=true)
    public void setMenuGenerator(MenuGeneratorInterface menuGenerator) {
        this.menuGenerator = menuGenerator;
    }

    // var & setter
    protected ConsoleInterface console;
    @Autowired(required=true)
    public void setConsole(ConsoleInterface console) {
        this.console = console;
    }

    // var & setter
    protected PreferencesInterface preferences;
    @Autowired(required=true)
    public void setPreferences(PreferencesInterface preferences) {
        this.preferences = preferences;
    }

    // var & setter
    protected ShortcutManagerInterface shortcutManager;
    @Autowired(required=true)
    public void setShortcutManager(ShortcutManagerInterface shortcutManager) {
        this.shortcutManager = shortcutManager;
    }

    // var & setter
    protected LangSelectorInterface langSelector;
    @Autowired(required=true)
    public void setLangSelector(LangSelectorInterface langSelector) {
        this.langSelector = langSelector;
    }

    // var & setter
    protected ErrorHandlerInterface errorHandler;
    @Autowired(required=true)
    public void setErrorHandler(ErrorHandlerInterface errorHandler) {
        this.errorHandler = errorHandler;
    }

}
