package com.vitco.layout.content.menu;

import com.vitco.core.data.Data;
import com.vitco.layout.content.console.ConsoleInterface;
import com.vitco.manager.action.ActionGroupManagerInterface;
import com.vitco.manager.action.ActionManager;
import com.vitco.manager.error.ErrorHandlerInterface;
import com.vitco.manager.lang.LangSelectorInterface;
import com.vitco.manager.pref.PreferencesInterface;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Implements basic classes
 */
public abstract class MenuLogicPrototype {
    // var & setter
    protected ConsoleInterface console;
    @Autowired
    public final void setConsole(ConsoleInterface console) {
        this.console = console;
    }

    // var & setter
    protected LangSelectorInterface langSelector;
    @Autowired
    public final void setLangSelector(LangSelectorInterface langSelector) {
        this.langSelector = langSelector;
    }

    // var & setter
    protected ErrorHandlerInterface errorHandler;
    @Autowired(required=true)
    public final void setErrorHandler(ErrorHandlerInterface errorHandler) {
        this.errorHandler = errorHandler;
    }

    // var & setter
    protected ActionManager actionManager;
    @Autowired(required=true)
    public final void setActionManager(ActionManager actionManager) {
        this.actionManager = actionManager;
    }

    // var & setter
    protected ActionGroupManagerInterface actionGroupManager;
    @Autowired(required=true)
    public final void setActionGroupManager(ActionGroupManagerInterface actionGroupManager) {
        this.actionGroupManager = actionGroupManager;
    }

    // var & setter (can not be interface!!)
    protected Data data;
    @Autowired
    public final void setData(Data data) {
        this.data = data;
    }

    // var & setter
    protected PreferencesInterface preferences;
    @Autowired(required=true)
    public final void setPreferences(PreferencesInterface preferences) {
        this.preferences = preferences;
    }
}
