package com.vitco.core.data;

import java.io.File;

/**
 * General Data interaction.
 */
public interface DataInterface {
    // loads all data from file
    boolean loadFromFile(File file);
    // stores all data to file
    boolean saveToFile(File file);
    // delete all history
    void clearHistoryA();
    void clearHistoryV();
    // erase everything and start fresh
    void freshStart();
}
