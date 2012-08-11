package com.vitco.shortcut;

import com.vitco.action.ActionManagerInterface;
import com.vitco.util.LangSelectorInterface;

import javax.swing.*;

/**
 * Created with IntelliJ IDEA.
 * User: VM Win 7
 * Date: 8/9/12
 * Time: 7:22 PM
 * To change this template use File | Settings | File Templates.
 */
public interface ShortcutManagerInterface {
    public void setConfigFile(String filename);
    public void loadConfig();
    public void registerFrame(final JComponent frame);
    public void setActionManager(ActionManagerInterface actionManager);
    public String[][] getFrames();
    public void setLangSelector(LangSelectorInterface langSel);
    public String[][] getShortcuts(String frameKey);
    public boolean isValidShortcut(String frame, KeyStroke keyStroke);
    public boolean updateShortcutObject(KeyStroke keyStroke, String frame, int id);
    public boolean isFreeShortcut(String frame, KeyStroke keyStroke);
    // convert KeyStroke to string representation
    public String asString(KeyStroke keyStroke);
}
