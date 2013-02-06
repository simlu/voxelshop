package com.vitco.util;

import com.vitco.util.error.ErrorHandlerInterface;

import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;

/**
 * Some basic tools to deal with files and streams.
 */
public class FileTools {

    public static String readFileAsString(File file, ErrorHandlerInterface errorHandler){
        String result = "";
        BufferedReader br = null;
        try
        {
            br = new BufferedReader(new FileReader(file));
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

    public static String md5Hash(String str, ErrorHandlerInterface errorHandler) {
        String result = "";
        try {
            byte[] bytesOfMessage = str.getBytes("UTF-8");
            MessageDigest md = MessageDigest.getInstance("MD5");
            result = new String(md.digest(bytesOfMessage));
        } catch (UnsupportedEncodingException e) {
            errorHandler.handle(e);
        } catch (NoSuchAlgorithmException e) {
            errorHandler.handle(e);
        }
        return result;
    }

    // de-serialize object from file
    public static Object loadFromFile(File file, ErrorHandlerInterface errorHandler) {
        Object result = null;
        try{
            InputStream inputStream = new FileInputStream( file );
            InputStream buffer = new BufferedInputStream( inputStream );
            ObjectInput input = new ObjectInputStream ( buffer );
            try {
                result = input.readObject();
            }
            finally{
                input.close();
            }
        }
        catch(ClassNotFoundException ex){
            errorHandler.handle(ex);
        }
        catch(IOException ex){
            errorHandler.handle(ex);
        }
        return result;
    }

    // serialize object to file
    public static boolean saveToFile(File file, Object object, ErrorHandlerInterface errorHandler) {
        boolean result = false;

        try{
            OutputStream outputStream = new FileOutputStream( file );
            OutputStream buffer = new BufferedOutputStream( outputStream );
            ObjectOutput output = new ObjectOutputStream( buffer );
            try{
                output.writeObject(object);
                result = true;
            }
            finally{
                output.close();
            }
        }
        catch(IOException ex){
            errorHandler.handle(ex);
        }

        return result;
    }

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
