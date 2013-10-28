package vitco.main;

import vitco.helper.ColorPal;
import vitco.helper.WorldGenerator;
import vitco.helper.XmlLinkageReader;
import vitco.tools.FileTools;

/**
 * Main class that handles the encoding of the map.
 */
public class Main {


    public static void main(String[] args) {

        // build index
        Config.logFile.log("Generating world representation...");

        // generate the world object
        WorldGenerator worldGenerator = new WorldGenerator(
                new XmlLinkageReader(Config.linkageFile),
                Config.workingDir
        );

        // build index
        Config.logFile.log("Building tile index...");
        worldGenerator.indexTiles();

        // delete duplicate tiles
        Config.logFile.log("Deleting tile duplicates... ");
        int removed = worldGenerator.deleteDuplicateTiles();
        Config.logFile.log("Removed " + removed + " duplicates.");

        // info
        Config.logFile.log("There are " + worldGenerator.getTileCount() + " unique tiles.");

        // make sure the tile sheet dir exists and is clean
        if (!FileTools.createDir(Config.tileSheetDir)) {
            FileTools.emptyDir(Config.tileSheetDir);
        }

        Config.logFile.log("Clustering and writing tile sheets... ");
        //worldGenerator.clusterWrite(Config.tileSheetDir);
        if (Config.writeAsOneFile) {
            worldGenerator.oneFileWrite(Config.tileSheetDir);
        } else if (Config.writeAs1204Texture) {
            worldGenerator.as1024TextureWrite(Config.tileSheetDir);
        } else {
            worldGenerator.clusterDensityWrite(Config.tileSheetDir);
        }
        //*

        // writing everything to zip
        Config.logFile.log("Writing final zip file... ");
        worldGenerator.writeZipMap(Config.outputDir + "world.zip");

        // output to evaluate (and find mistakes)
        if (Config.recompileOverview) {
            Config.logFile.log("Compiling overview images...");
            if (!FileTools.createDir(Config.overviewDir)) {
                FileTools.emptyDir(Config.overviewDir);
            }
            worldGenerator.printWalkable(Config.overviewDir + "walkable.png");
            worldGenerator.printTileIds(Config.overviewDir + "tileIds.png");
            worldGenerator.drawMaps(Config.mapDir, Config.minimapDir);
            worldGenerator.drawWorld(Config.overviewDir + "world.png");
            Config.logFile.log("Writing color swatches... ");
            ColorPal.write(Config.tileSheetDir);
        }

        Config.logFile.log("All done.");

        //*/

    }
}
