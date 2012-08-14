package com.vitco.logic.frames.console;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.ArrayList;

/**
 * deals with the content of the console
 */
public interface ConsoleInterface {
    void addLine(String text);
    void addConsoleListener(ConsoleListener consoleListener);
    void removeConsoleListener(ConsoleListener consoleListener);
    @PostConstruct
    void init();
    @PreDestroy
    void finish();
    void clear();
    // return all buffered console data
    ArrayList<String> getConsoleData(int lastLines);
}
