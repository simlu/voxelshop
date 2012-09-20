package com.vitco.logic.menu;

import com.vitco.engine.data.Data;
import com.vitco.logic.console.ConsoleInterface;
import com.vitco.util.action.ActionGroupManagerInterface;
import com.vitco.util.action.ActionManager;
import com.vitco.util.error.ErrorHandlerInterface;
import com.vitco.util.lang.LangSelectorInterface;
import com.vitco.util.pref.PreferencesInterface;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Implements basic classes
 */
public abstract class MenuLogicPrototype {
    // var & setter
    protected ConsoleInterface console;
    @Autowired
    public void setConsole(ConsoleInterface console) {
        this.console = console;
    }

    // var & setter
    protected LangSelectorInterface langSelector;
    @Autowired
    public void setLangSelector(LangSelectorInterface langSelector) {
        this.langSelector = langSelector;
    }

    // var & setter
    protected ErrorHandlerInterface errorHandler;
    @Autowired(required=true)
    public void setErrorHandler(ErrorHandlerInterface errorHandler) {
        this.errorHandler = errorHandler;
    }

    // var & setter
    protected ActionManager actionManager;
    @Autowired(required=true)
    public void setActionManager(ActionManager actionManager) {
        this.actionManager = actionManager;
    }

    // var & setter
    protected ActionGroupManagerInterface actionGroupManager;
    @Autowired(required=true)
    public void setActionGroupManager(ActionGroupManagerInterface actionGroupManager) {
        this.actionGroupManager = actionGroupManager;
    }

    // var & setter (can not be interface!!)
    protected Data data;
    @Autowired
    public void setData(Data data) {
        this.data = data;
    }

    // var & setter
    protected PreferencesInterface preferences;
    @Autowired(required=true)
    public void setPreferences(PreferencesInterface preferences) {
        this.preferences = preferences;
    }
}
