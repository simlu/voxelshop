package com.vitco.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;

/**
 * Some basic tools to deal with files and streams.
 */
public class FileTools {
    // convert inputstream to string
    public static String inputStreamToString(InputStream in) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in));
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
}
