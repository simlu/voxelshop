package com.vitco.manager.pref;

import com.vitco.Main;
import com.vitco.manager.error.ErrorHandlerInterface;
import com.vitco.settings.VitcoSettings;
import com.vitco.util.file.FileTools;
import com.vitco.util.misc.AutoFileCloser;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Stores & Loads preferences (Integer, String, Object)
 * Storing is executed after the PreDestroy of all the other objects.
 */
public class Preferences implements PreferencesInterface {

    // preferences
    private HashMap<String, Object> map = new HashMap<String, Object>();

    // listeners
    private final HashMap<String, ArrayList<PrefChangeListener>> listeners = new HashMap<String, ArrayList<PrefChangeListener>>();

    // Note: notification should take place in the order that the listener were added
    @Override
    public void notifyListeners(String key, Object value) {
        synchronized (VitcoSettings.SYNC) {
            if (listeners.containsKey(key)) {
                for (PrefChangeListener pcl : listeners.get(key)) {
                    pcl.onPrefChange(value);
                }
            }
        }
    }

    @Override
    public void addPrefChangeListener(String key, PrefChangeListener pcl) {
        synchronized (VitcoSettings.SYNC) {
            if (!listeners.containsKey(key)) { // make sure this is init
                listeners.put(key, new ArrayList<PrefChangeListener>());
            }
            // add the listener
            listeners.get(key).add(pcl);
            // call it if value is known
            if (map.containsKey(key)) {
                pcl.onPrefChange(map.get(key));
            }
        }
    }

    @Override
    public boolean contains(String key) {
        synchronized (VitcoSettings.SYNC) {
            return map.containsKey(key);
        }
    }

    // var & setter
    private ErrorHandlerInterface errorHandler;
    @Override
    public final void setErrorHandler(ErrorHandlerInterface errorHandler) {
        synchronized (VitcoSettings.SYNC) {
            this.errorHandler = errorHandler;
        }
    }

    @Override
    public final void storeObject(String key, Object value) {
        synchronized (VitcoSettings.SYNC) {
            if (!map.containsKey(key) || !map.get(key).equals(value)) {
                map.put(key, value);
                notifyListeners(key, value);
            }
        }
    }

    @Override
    public Object loadObject(String key) {
        synchronized (VitcoSettings.SYNC) {
            return map.containsKey(key) ? map.get(key) : null;
        }
    }

    @Override
    public void storeBoolean(String key, boolean value) {
        synchronized (VitcoSettings.SYNC) {
            storeObject(key, value);
        }
    }

    @Override
    public void storeInteger(String key, int value) {
        synchronized (VitcoSettings.SYNC) {
            storeObject(key, value);
        }
    }

    @Override
    public void storeString(String key, String value) {
        synchronized (VitcoSettings.SYNC) {
            storeObject(key, value);
        }
    }

    @Override
    public boolean loadBoolean(String key) {
        synchronized (VitcoSettings.SYNC) {
            return map.containsKey(key) ? (Boolean)map.get(key) : false;
        }
    }

    @Override
    public int loadInteger(String key) {
        synchronized (VitcoSettings.SYNC) {
            return map.containsKey(key) ? (Integer)map.get(key) : 0;
        }
    }

    @Override
    public final String loadString(String key) {
        synchronized (VitcoSettings.SYNC) {
            return map.containsKey(key) ? (String)map.get(key) : "";
        }
    }

    private static String getUserDataDirectory() {
        return System.getProperty("user.home") + File.separator + ".voxelshop" + File.separator;
    }

    // var % setter
    private String storageFileName;
    @Override
    public final void setStorageFile(String filename) {
        synchronized (VitcoSettings.SYNC) {
            if (Main.isDebugMode()) {
                storageFileName = filename;
            } else {
                // use user directory to store configuration
                storageFileName = getUserDataDirectory() + File.separator + filename;
            }
        }
    }

    // "manually" executed after all PreDestroys are called
    @Override
    public void save() {
        synchronized (VitcoSettings.SYNC) {
            // store the map in file
            final File dataFile = new File(storageFileName);
            if (dataFile.getParentFile().exists() || dataFile.getParentFile().mkdirs()) {
                try {
                    new AutoFileCloser() {
                        @Override protected void doWork() throws Throwable {
                            FileOutputStream fileOut = autoClose(new FileOutputStream(dataFile));
                            ObjectOutputStream out = autoClose(new ObjectOutputStream(fileOut));

                            out.writeObject(map);
                        }
                    };
                } catch (RuntimeException e) {
                    errorHandler.handle(e);
                }
            }
        }
    }

    // executed when initiated (spring "init-method")
    @Override
    public void load() {
        synchronized (VitcoSettings.SYNC) {
            final File dataFile = new File(storageFileName);
            if (dataFile.exists()) {
                try {
                    new AutoFileCloser() {
                        @Override protected void doWork() throws Throwable {
                            try {
                                FileInputStream fileIn = autoClose(new FileInputStream(dataFile));
                                ObjectInputStream in = autoClose(new ObjectInputStream(fileIn));
                                map = FileTools.castHash((HashMap) in.readObject(), String.class, Object.class);
                            } catch (InvalidClassException ignored) { // ignore any error, it's "ok" for the pref file to be corrupt
                            } catch (EOFException ignored) {
                            } catch (StreamCorruptedException ignored) {
                            } catch (RuntimeException ignored) {}
                        }
                    };
                } catch (RuntimeException e) {
                    errorHandler.handle(e);
                }
            }
        }
    }

}
