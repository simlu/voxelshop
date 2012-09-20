package com.vitco.layout;

import com.jidesoft.action.DockableBar;
import com.jidesoft.docking.DockableFrame;
import com.vitco.engine.data.Data;
import com.vitco.layout.bars.BarLinkagePrototype;
import com.vitco.layout.frames.FrameLinkagePrototype;
import com.vitco.logic.shortcut.ShortcutManagerInterface;
import com.vitco.util.action.ActionManager;
import com.vitco.util.error.ErrorHandlerInterface;
import com.vitco.util.lang.LangSelectorInterface;
import com.vitco.util.pref.PreferencesInterface;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.Map;

/**
 * Interface
 *
 * Initializes all the different sub-windows.
 */
public interface WindowManagerInterface {
    // prepare a frame (key is the key of the frame)
    DockableFrame prepareFrame(String key);
    // prepare a bar (key is the key of the frame)
    DockableBar prepareBar(String key);
    // set the key <-> bar class linkage map (defined in config.xml)
    void setBarLinkageMap(Map<String, BarLinkagePrototype> map);
    // set the key <-> frame class linkage map (defined in config.xml)
    void setFrameLinkageMap(Map<String, FrameLinkagePrototype> map);
    // set the ShortcutManager
    void setShortcutManager(ShortcutManagerInterface shortcutManager);
    // set error handler
    void setErrorHandler(ErrorHandlerInterface errorHandler);
    // set the action manager
    void setActionManager(ActionManager actionManager);
    // set pref
    void setPreferences(PreferencesInterface preferences);
    // set data container
    void setData(Data data);
    // set language selector
    void setLangSelector(LangSelectorInterface langSelector);

    @PostConstruct
    void init();

    @PreDestroy
    void finish();
}
