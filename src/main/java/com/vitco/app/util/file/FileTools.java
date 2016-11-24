package com.vitco.app.util.file;

import com.vitco.app.core.container.HackedObjectInputStream;
import com.vitco.app.manager.error.ErrorHandlerInterface;
import com.vitco.app.util.misc.AutoFileCloser;

import java.io.*;
import java.util.HashMap;

/**
 * Some basic tools to deal with files and streams.
 */
public class FileTools {

    // find all files with a particular ending in folder
    public static File[] findFiles(String dirName, final String ext){
        File dir = new File(dirName);
        return dir.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String filename) {
                return filename.endsWith("." + ext);
            }
        });
    }

    // add a separator to file path if not already present
    public static String ensureTrailingSeparator(String path) {
        if (path.charAt(path.length()-1) != File.separatorChar) {
            path += File.separator;
        }
        return path;
    }

    // todo: improve so it only ever erases from "slash to slash"
    // shorten a long file path for display purposes
    public static String shortenPath(String path, int length) {
        int originalLength = path.length();
        if (originalLength <= length) {
            return path;
        }
        int toCrop = originalLength - length + 3;
        if (toCrop > originalLength) {
            toCrop = originalLength;
        }
        int starting = originalLength/2 - toCrop/2;
        int stopping = starting + toCrop;
        return path.substring(0, starting) + "..." + path.substring(stopping, originalLength);
    }

    // remove extension from file and return name without extension
    public static String extractNameWithoutExtension(File f) {
        // if it's a directory, don't remove the extension
        if (f.isDirectory()) return f.getName();
        String name = f.getName();
        // if it is a hidden file
        if (name.startsWith(".")) {
            // if there is no extension, do not remove one...
            if (name.lastIndexOf('.') == name.indexOf('.')) return name;
        }
        // if there is no extension, don't do anything
        if (!name.contains(".")) return name;
        // Otherwise, remove the last 'extension type thing'
        return name.substring(0, name.lastIndexOf('.'));
    }

    // remove extension from file and return file path without extension
    public static String removeExtension(File f) {
        // if it's a directory, don't remove the extension
        if (f.isDirectory()) return f.getAbsolutePath();
        String name = f.getName();
        // if it is a hidden file
        if (name.startsWith(".")) {
            // if there is no extension, do not remove one...
            if (name.lastIndexOf('.') == name.indexOf('.')) return f.getAbsolutePath();
        }
        // if there is no extension, don't do anything
        if (!name.contains(".")) return f.getAbsolutePath();
        // Otherwise, remove the last 'extension type thing'
        return f.getAbsolutePath().substring(0, f.getAbsolutePath().lastIndexOf('.'));
    }

    // reads the content of a file as string
    public static String readFileAsString(File file, ErrorHandlerInterface errorHandler){
        String result = "";
        BufferedReader br = null;
        try
        {
            br = new BufferedReader(new InputStreamReader(new FileInputStream(file), "utf-8"));
            StringBuilder str = new StringBuilder();
            String line = br.readLine();
            while (line != null)
            {
                str.append(line).append("\n");
                line = br.readLine();
            }
            result = str.toString();
        } catch (FileNotFoundException e) {
            errorHandler.handle(e);
        } catch (IOException e) {
            errorHandler.handle(e);
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    errorHandler.handle(e);
                }
            }
        }
        return result;
    }

    // de-serialize object from file
    public static Object loadFromFile(final File file, ErrorHandlerInterface errorHandler) {
        final Object[] result = {null};
        if (file != null && file.exists()) {
            try {
                new AutoFileCloser() {
                    @Override protected void doWork() throws Throwable {
                        try {
                            InputStream inputStream = autoClose(new FileInputStream(file));
                            InputStream buffer = autoClose(new BufferedInputStream(inputStream));
                            ObjectInput input = autoClose(new HackedObjectInputStream(buffer));
                            result[0] = input.readObject();
                        } catch (StreamCorruptedException ignored) {} // caused if the file format is invalid
                    }
                };
            } catch (RuntimeException e) {
                errorHandler.handle(e);
            }
        }
        return result[0];
    }

    // serialize object to file
    public static boolean saveToFile(final File file, final Object object, ErrorHandlerInterface errorHandler) {
        final boolean[] result = {false};
        try {
            new AutoFileCloser() {
                @Override protected void doWork() throws Throwable {
                    OutputStream outputStream = autoClose(new FileOutputStream( file ));
                    OutputStream buffer = autoClose(new BufferedOutputStream( outputStream ));
                    ObjectOutput output = autoClose(new ObjectOutputStream( buffer ));

                    output.writeObject(object);
                    result[0] = true;
                }
            };
        } catch (RuntimeException e) {
            errorHandler.handle(e);
        }
        return result[0];
    }

    // convert inputstream to string
    public static String inputStreamToString(InputStream in) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in, "utf-8"));
        StringBuilder stringBuilder = new StringBuilder();
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            stringBuilder.append(line).append("\n");
        }
        // remove the last line break (was not in file!)
        stringBuilder.deleteCharAt(stringBuilder.length()-1);

        bufferedReader.close();
        return stringBuilder.toString();
    }
    // Generic Helper to keep casts safe when de-serializing hash-maps.
    // "prevent compiler warning by explicitly casting"
    public static <K, V> HashMap<K, V> castHash(HashMap input,
                                                Class<K> keyClass,
                                                Class<V> valueClass) {
        HashMap<K, V> output = new HashMap<K, V>();
        if (input == null)
            return output;
        for (Object key: input.keySet().toArray()) {
            if ((key == null) || (keyClass.isAssignableFrom(key.getClass()))) {
                Object value = input.get(key);
                if ((value == null) || (valueClass.isAssignableFrom(value.getClass()))) {
                    K k = keyClass.cast(key);
                    V v = valueClass.cast(value);
                    output.put(k, v);
                } else {
                    throw new AssertionError(
                            "Cannot cast to HashMap<"+ keyClass.getSimpleName()
                                    +", "+ valueClass.getSimpleName() +">"
                                    +", value "+ value +" is not a "+ valueClass.getSimpleName()
                    );
                }
            } else {
                throw new AssertionError(
                        "Cannot cast to HashMap<"+ keyClass.getSimpleName()
                                +", "+ valueClass.getSimpleName() +">"
                                +", key "+ key +" is not a " + keyClass.getSimpleName()
                );
            }
        }
        return output;
    }

    // change the extension of a file e.g. "test.txt", ".dat" -> "test.dat"
    public static String changeExtension(String originalName, String newExtension) {
        int lastDot = originalName.lastIndexOf(".");
        if (lastDot != -1) {
            return originalName.substring(0, lastDot) + newExtension;
        } else {
            return originalName + newExtension;
        }
    }
}
