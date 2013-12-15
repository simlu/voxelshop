package com.vitco.layout.content.shortcut;

import javax.swing.*;
import java.io.Serializable;

/* to manage the data structure */

public class ShortcutObject implements Serializable {
    private static final long serialVersionUID = 1L;

    public String actionName;
    public String caption;
    public transient JComponent linkedFrame; // no need to serialize the frame
    public KeyStroke keyStroke;
}
