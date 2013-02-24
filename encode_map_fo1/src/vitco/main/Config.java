package vitco.main;

import vitco.helper.LogFile;

/**
 * Configuration file. Defines global variables and file-path.
 */
public class Config {
    public final static String workingDir = "C:\\Users\\VM Win 7\\Dropbox\\Artwork\\FO Dev\\maps\\";
    public final static String outputDir = workingDir + "_out\\";
    public final static String linkageFile = workingDir + "manifest.xml";
    //public final static String linkageFile = workingDir + "manifest-test.xml";

    public final static String minimapDir = outputDir + "minimaps\\";

    public final static String overviewDir = outputDir + "overview\\";
    public final static String mapDir = overviewDir + "maps\\";
    public final static String swatchFolder = overviewDir + "swatches\\";

    public final static String tileSheetDir = outputDir + "sheets\\";

    public final static String tmpFolder = "E:\\TEMP\\~tiles\\";

    public final static String logFileName = outputDir + "log.txt";

    public final static boolean recompileOverview = true;
    public final static boolean compileMinimaps = false;

    // log file
    public final static LogFile logFile = new LogFile(logFileName);

}
