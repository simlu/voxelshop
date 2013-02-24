package vitco.tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

/**
 * Some basic file functionality
 */
public class FileTools {

    // get all files in a folder
    public static String[] getFilesInFolder(File folder, String ending) {
        ArrayList<String> result = new ArrayList<String>();
        for (final File fileEntry : folder.listFiles()) {
            if (!fileEntry.isDirectory()) {
                String path = fileEntry.getAbsolutePath();
                if (path.endsWith(ending)) {
                    result.add(fileEntry.getAbsolutePath());
                }
            }
        }
        String[] resultArray = new String[result.size()];
        result.toArray(resultArray);
        return  resultArray;
    }

    // rename a file
    public static boolean renameFile(String oldname, String newname) {
        File f = new File(oldname);
        return f.renameTo(new File(newname));
    }

    // Returns the contents of the file in a byte array.
    public static byte[] getBytesFromFile(File file) throws IOException {
        InputStream is = new FileInputStream(file);

        // Get the size of the file
        long length = file.length();

        // You cannot create an array using a long type.
        // It needs to be an int type.
        // Before converting to an int type, check
        // to ensure that file is not larger than Integer.MAX_VALUE.
        if (length > Integer.MAX_VALUE) {
            // File is too large
        }

        // Create the byte array to hold the data
        byte[] bytes = new byte[(int)length];

        // Read in the bytes
        int offset = 0;
        int numRead;
        while (offset < bytes.length
                && (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0) {
            offset += numRead;
        }

        // Ensure all the bytes have been read in
        if (offset < bytes.length) {
            throw new IOException("Could not completely read file "+file.getName());
        }

        // Close the input stream and return bytes
        is.close();
        return bytes;
    }

    public static boolean deleteDir(String dirName) {
        return deleteDir(new File(dirName));
    }

    public static boolean deleteDir(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (String aChildren : children) {
                boolean success = deleteDir(new File(dir, aChildren));
                if (!success) {
                    return false;
                }
            }
        }
        return dir.delete();
    }

    public static boolean emptyDir(String dirName) {
        return emptyDir(new File(dirName));
    }

    public static boolean emptyDir(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (String aChildren : children) {
                boolean success = deleteDir(new File(dir, aChildren));
                if (!success) {
                    return false;
                }
            }
        }
        return true;
    }

    public static boolean createDir(String directory) {
        return (new File(directory)).mkdirs();
    }
}
