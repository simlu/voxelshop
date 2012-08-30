package com.vitco.util.pref;

import com.vitco.util.error.ErrorHandlerInterface;

/**
 * Stores & Loads preferences (Integer, String, Object)
 * Storing is executed after the PreDestroy of all the other objects.
 */
public interface PreferencesInterface {
    public void storeInteger(String key, int value);
    public void storeString(String key, String value);
    public int loadInteger(String key);
    public String loadString(String key);
    public void setStorageFile(String filename);
    public void save();
    public void load();
    public Object loadObject(String key);
    public void storeObject(String key, Object value);
    public void setErrorHandler(ErrorHandlerInterface errorHandler);
    boolean contains(String key);

    void storeBoolean(String key, boolean value);

    boolean loadBoolean(String key);
}
