package com.vitco.logic.frames.console;

import com.vitco.util.DateTools;

import java.util.ArrayList;

/**
 * deals with the content of the console
 */
public class Console implements ConsoleInterface {
    private final ArrayList<ConsoleListener> consoleListeners = new ArrayList<ConsoleListener>();

    @Override
    public void addLine(String text) {
        for (ConsoleListener consoleListener : consoleListeners) {
             consoleListener.lineAdded(DateTools.now("HH:mm:ss a ") + text);
        }
    }

    @Override
    public void addConsoleListener(ConsoleListener consoleListener) {
        consoleListeners.add(consoleListener);
    }

    @Override
    public void removeConsoleListener(ConsoleListener consoleListener) {
        consoleListeners.remove(consoleListener);
    }

}
