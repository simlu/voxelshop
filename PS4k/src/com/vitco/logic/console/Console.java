package com.vitco.logic.console;

import com.vitco.util.DateTools;
import com.vitco.util.pref.PreferencesInterface;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.ArrayList;

/**
 * deals with the content of the console
 */
public class Console implements ConsoleInterface {
    public static final int LINE_BUFFER_COUNT = 500;
    private final ArrayList<ConsoleListener> consoleListeners = new ArrayList<ConsoleListener>();
    private final ArrayList<String> consoleData = new ArrayList<String>();

    // var & setter
    protected PreferencesInterface preferences;
    @Autowired(required=true)
    public final void setPreferences(PreferencesInterface preferences) {
        this.preferences = preferences;
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
        // clear the console on start
        clear();
    }

    @PreDestroy
    @Override
    public void finish() {
        // store console data
        preferences.storeObject("console_stored_data", consoleData);
    }

    // clear the console data
    @Override
    public void clear() {
        consoleData.clear();
    }

    // return all buffered console data
    @Override
    public ArrayList<String> getConsoleData() {
        ArrayList<String> lines = new ArrayList<String>();
        for (String aConsoleData : consoleData) {
            lines.add(aConsoleData);
        }
        return lines;
    }

    // adds a line to the console and notifies listeners
    @Override
    public void addLine(String text) {
        String line = DateTools.now("hh:mm:ss a ") + text + "\n"; // format with time
        consoleData.add(line); // add to console
        // make sure we do not buffer too many lines
        while (consoleData.size() > Console.LINE_BUFFER_COUNT) {
            consoleData.remove(0);
        }
        // notify listeners
        for (ConsoleListener consoleListener : consoleListeners) {
             consoleListener.lineAdded(line);
        }
    }

    // add a console listener
    @Override
    public void addConsoleListener(ConsoleListener consoleListener) {
        consoleListeners.add(consoleListener);
    }

    // remove a console listener
    @Override
    public void removeConsoleListener(ConsoleListener consoleListener) {
        consoleListeners.remove(consoleListener);
    }

}
