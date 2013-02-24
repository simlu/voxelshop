package vitco.helper;

import vitco.tools.FileTools;
import vitco.tools.Out;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Represents a log file.
 */
public class LogFile {

    private Out output = null;

    // get the current time stamp
    private String getNow() {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss yyyy-MM-dd Z z");
        return sdf.format(cal.getTime());
    }

    public LogFile(String filename) {
        try {
            FileTools.createDir(filename.substring(0, filename.lastIndexOf("\\")));
            output = new Out(filename);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void log(String text) {
        try {
            String line = getNow() + ": " + text;
            System.out.println(line);
            output.writeLine(line);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
