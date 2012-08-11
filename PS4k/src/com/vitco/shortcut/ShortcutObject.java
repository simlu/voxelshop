package com.vitco.shortcut;

import javax.swing.*;
import java.io.Serializable;

/* to manage the data structure */

public class ShortcutObject implements Serializable {
    public String actionName;
    public String caption;
    public JComponent linkedFrame;
    public KeyStroke keyStroke;
}
