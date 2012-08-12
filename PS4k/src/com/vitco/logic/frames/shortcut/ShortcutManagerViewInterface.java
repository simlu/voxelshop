package com.vitco.logic.frames.shortcut;

import com.vitco.util.lang.LangSelectorInterface;
import com.vitco.util.pref.PreferencesInterface;

import javax.swing.*;

/**
 * Handle the displaying and the logic for the editing of shortcuts. (view & link to logic)
 */
public interface ShortcutManagerViewInterface {
    // return a JTabbedPane that is autonomous and manages shortcuts
    JTabbedPane getEditTables();
    // handle loading of state (selected index)
    void loadStateInformation();
    // handle saving of state (selected index)
    void storeStateInformation();
    void setPreferences(PreferencesInterface preferences);
    void setShortcutManager(ShortcutManagerInterface shortcutManager);
    void setLangSelector(LangSelectorInterface langSelector);
}
