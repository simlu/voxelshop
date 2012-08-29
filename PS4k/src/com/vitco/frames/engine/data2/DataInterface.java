package com.vitco.frames.engine.data2;

import java.io.File;

/**
 * General Data interaction.
 */
public interface DataInterface {
    // loads all data from file
    boolean loadFromFile(File file);
    // stores all data to file
    boolean storeToFile(File file);
}
