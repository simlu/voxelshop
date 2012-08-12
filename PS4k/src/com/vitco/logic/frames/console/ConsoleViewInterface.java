package com.vitco.logic.frames.console;

import javax.swing.*;

/**
 * Displays console content and buttons to user.
 */
public interface ConsoleViewInterface {
    void setConsole(ConsoleInterface console);
    JComponent buildConsole();
    void addLine(String text);
}
