package com.vitco.app.layout.content.shortcut;

import javax.swing.*;

/**
 * Handle the displaying and the logic for the editing of shortcuts. (view & link to logic)
 */
public interface ShortcutManagerViewInterface {
    // return a JTabbedPane that is autonomous and manages shortcuts
    JTabbedPane getEditTables();
}
