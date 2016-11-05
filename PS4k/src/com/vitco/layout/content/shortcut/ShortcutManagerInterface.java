package com.vitco.layout.content.shortcut;

import com.jidesoft.docking.DockingManager;
import com.vitco.manager.action.ActionManager;
import com.vitco.manager.error.ErrorHandlerInterface;
import com.vitco.manager.lang.LangSelectorInterface;
import com.vitco.manager.pref.PreferencesInterface;

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
    // convert KeyStroke to string representation
    String asString(KeyStroke keyStroke);
    void setErrorHandler(ErrorHandlerInterface errorHandler);
    void setPreferences(PreferencesInterface preferences);
    void activateShortcuts();
    void deactivateShortcuts();

    // get global KeyStroke by action
    KeyStroke getGlobalShortcutByAction(String actionName);

    void addGlobalShortcutChangeListener(GlobalShortcutChangeListener globalShortcutChangeListener);

    void removeGlobalShortcutChangeListener(GlobalShortcutChangeListener globalShortcutChangeListener);

    // register all actions of global shortcuts, to perform validity check
    void registerGlobalShortcutActions();

    // register global shortcuts and make sure all shortcuts are correctly enabled
    void registerShortcuts(Frame frame, final DockingManager dockingManager);
}
