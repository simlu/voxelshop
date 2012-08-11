package com.vitco.util;

import java.io.*;
import java.util.HashMap;

/**
 * Created with IntelliJ IDEA.
 * User: VM Win 7
 * Date: 8/11/12
 * Time: 11:03 AM
 * To change this template use File | Settings | File Templates.
 */
public class Preferences implements PreferencesInterface {

    private HashMap<String, Object> map = new HashMap<String, Object>();

    @Override
    public void storeObject(String key, Object value) {
        map.put(key, value);
    }

    @Override
    public Object loadObject(String key) {
        return map.containsKey(key) ? map.get(key) : null;
    }

    @Override
    public void storeInteger(String key, int value) {
        map.put(key, value);
    }

    @Override
    public void storeString(String key, String value) {
        map.put(key, value);
    }

    @Override
    public int loadInteger(String key) {
        return map.containsKey(key) ? (Integer)map.get(key) : 0;
    }

    @Override
    public String loadString(String key) {
        return map.containsKey(key) ? (String)map.get(key) : "";
    }

    // var % setter
    private String storageFileName;
    @Override
    public void setStorageFile(String filename) {
        storageFileName = filename;
    }

    // "manually" executed after all PreDestroys are called
    @Override
    public void save() {
        // store the map in file
        try {
            FileOutputStream fileOut = new FileOutputStream(storageFileName);
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(map);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // executed when initiated
    @Override
    public void load() {
        if (new File(storageFileName).exists()) {
            try {
                FileInputStream fileIn = new FileInputStream(storageFileName);
                ObjectInputStream in = new ObjectInputStream(fileIn);
                map = SerializableHelper.castHash((HashMap) in.readObject(), String.class, Object.class);
                in.close();
                fileIn.close();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

}
