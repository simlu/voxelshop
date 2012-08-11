package com.vitco.util;

/**
 * Created with IntelliJ IDEA.
 * User: VM Win 7
 * Date: 8/11/12
 * Time: 11:02 AM
 * To change this template use File | Settings | File Templates.
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
}
