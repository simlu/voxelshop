package com.vitco.logic.frames.console;

import com.vitco.logic.frames.ModelPrototype;
import com.vitco.util.DateTools;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.ArrayList;

/**
 * deals with the content of the console
 */
public class Console extends ModelPrototype implements ConsoleInterface {
    public static final int LINE_BUFFER_COUNT = 500;
    private final ArrayList<ConsoleListener> consoleListeners = new ArrayList<ConsoleListener>();
    private final ArrayList<String> consoleData = new ArrayList<String>();

    // clear the console data
    @Override
    public void clear() {
        consoleData.clear();
    }

    @PostConstruct
    @Override
    public void init() {
        if (preferences.contains("console_stored_data")) {
            // load console data
            for (Object line : (ArrayList)preferences.loadObject("console_stored_data")) {
                consoleData.add((String)line);
            }
        }
        //this.addLine(String.valueOf(consoleData.size()));
    }

    @PreDestroy
    @Override
    public void finish() {
        // store console data
        preferences.storeObject("console_stored_data", consoleData);
    }

    // return all buffered console data
    @Override
    public ArrayList<String> getConsoleData(int lastLines) {
        ArrayList<String> lines = new ArrayList<String>();
        for (int i = Math.max(0, consoleData.size()-lastLines), len = consoleData.size(); i < len; i ++) {
            lines.add(consoleData.get(i));
        }
        return lines;
    }

    @Override
    public void addLine(String text) {
        String line = DateTools.now("hh:mm:ss a ") + text + "\n";
        consoleData.add(line);
        while (consoleData.size() > Console.LINE_BUFFER_COUNT) {
            consoleData.remove(0); // make sure we do not buffer too many lines
        }
        for (ConsoleListener consoleListener : consoleListeners) {
             consoleListener.lineAdded(line);
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
