package vitco.main;

import vitco.helper.LogFile;

import java.util.HashSet;

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

    // log file
    public final static LogFile logFile = new LogFile(logFileName);

    //=======================================================
    // output settings below
    //=======================================================

    public final static boolean recompileOverview = true;
    public final static boolean compileMinimaps = true;
    public final static HashSet<String> minimapsToCompile = new HashSet<String>();
    static {
        minimapsToCompile.add("Forgotten Under Underground");
        minimapsToCompile.add("Forgotten Underground");
    }

    // "manual" can result in non 32 bit compression
    public final static boolean manualCompress = false;

    // write as 32 bit
    public final static boolean writeAs32Bit = false;

    // ===============
    // note: the following are exclusive (one of them equal true or none)
    // write only one huge image chunk (png)
    public final static boolean writeAsOneFile = false;
    // write as 1024 x 1024 tiles
    public final static boolean writeAs1204Texture = false;
    // ===============

    // include the image in the map file
    public final static boolean includeImageInMapFile = true;

}
