package com.vitco.app.layout.content.shortcut;

import com.jidesoft.docking.DockingManager;
import com.vitco.app.manager.action.ActionManager;
import com.vitco.app.manager.error.ErrorHandlerInterface;
import com.vitco.app.manager.lang.LangSelectorInterface;
import com.vitco.app.manager.pref.PreferencesInterface;

import javax.swing.*;
import java.awt.*;

/**
 * Handles shortcut linking (logic)
 */
public interface ShortcutManagerInterface {
    void setConfigFile(String filename);
    void loadConfig();
    void registerFrame(final JComponent frame);
    void setActionManager(ActionManager actionManager);
    String[][] getFrames();
    void setLangSelector(LangSelectorInterface langSel);
    String[][] getShortcuts(String frameKey);
    boolean isValidShortcut(KeyStroke keyStroke);
    boolean updateShortcutObject(KeyStroke keyStroke, String frame, int id);
    boolean isFreeShortcut(String frame, KeyStroke keyStroke);
    Color getEditBgColor(String frame, int id);
    // convert KeyStroke to string representation
    String asString(KeyStroke keyStroke);
    void setErrorHandler(ErrorHandlerInterface errorHandler);
    void setPreferences(PreferencesInterface preferences);
    void activateShortcuts();
    void deactivateShortcuts();

    // get KeyStroke by action
    KeyStroke getShortcutByAction(String frame, String actionName);

    void addShortcutChangeListener(ShortcutChangeListener shortcutChangeListener);
    void removeShortcutChangeListener(ShortcutChangeListener shortcutChangeListener);

    // register all actions of global shortcuts, to perform validity check
    void registerGlobalShortcutActions();

    // register global shortcuts and make sure all shortcuts are correctly enabled
    void registerShortcuts(Frame frame, final DockingManager dockingManager);
}
