package com.vitco.logic.frames.console;

/**
 * deals with the content of the console
 */
public interface ConsoleInterface {
    void addLine(String text);
    void addConsoleListener(ConsoleListener consoleListener);
    void removeConsoleListener(ConsoleListener consoleListener);
}
