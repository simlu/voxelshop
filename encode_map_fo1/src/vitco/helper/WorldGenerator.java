package vitco.helper;

import com.google.code.ekmeans.EKmeans;
import vitco.cluster.TileCluster;
import vitco.cluster.TileClusterManager;
import vitco.datastruct.Tile;
import vitco.group.GroupRep;
import vitco.group.TileRep;
import vitco.group.TileTools;
import vitco.main.Config;
import vitco.tools.*;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.MessageFormat;
import java.util.*;

/**
 * Constructs the world from the linkage map.
 */
public class WorldGenerator {

    // default map size
    int defaultMapWidth = 128;
    int defaultMapHeight = 96;

    // default tile size
    int tileSize = 32;

    // world size
    final int worldWidth;
    final int worldHeight;

    // the walkable area and internal setter
    private final byte[] walkable;
    private void writeWalkable(byte[] walkable, int mx, int my) {
        for (int x = 0; x < defaultMapWidth; x++) {
            for (int y = 0; y < defaultMapHeight; y++) {
                int wx = mx * defaultMapWidth + x;
                int wy = my * defaultMapHeight + y;
                this.walkable[wy * worldWidth + wx] = walkable[y * defaultMapWidth + x];
            }
        }
    }

    // retrieve the current tile count
    public int getTileCount() {
        return tiles.size();
    }

    // the map tile ids and internal setter
    // (this uses integer, because the world might grow too large!)
    private final int[] tileIds;
    private void writeTileIds(short[] tileIds, int mx, int my, int offset) {
        for (int x = 0; x < defaultMapWidth; x++) {
            for (int y = 0; y < defaultMapHeight; y++) {
                int wx = mx * defaultMapWidth + x;
                int wy = my * defaultMapHeight + y;
                this.tileIds[wy * worldWidth + wx] = (int) tileIds[y * defaultMapWidth + x] + offset;
            }
        }
    }

    // store the tile ids (for the world)
    private final HashMap<Integer, BufferedImage> tiles = new HashMap<Integer, BufferedImage>();

    // holds all the map objects
    private final HashMap<String, TmxMapReader> tmxObjects = new HashMap<String, TmxMapReader>();

    // helper to draw a map
    private BufferedImage drawMap(int wx, int wy) {
        BufferedImage img = new BufferedImage(32 * 128, 32 * 96, BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < defaultMapWidth; x++) {
            for (int y = 0; y < defaultMapHeight; y++) {

                img.getGraphics().drawImage(
                        tiles.get(tileIds[(wy * defaultMapHeight + y) * worldWidth + wx * defaultMapWidth + x]),
                        //tmxMapReader.getTile(tmxMapReader.getTileIds()[y * defaultMapWidth + x]),
                        x*32, y*32, null);
            }
        }
        return img;
    }

    // draw all maps into the folder
    public final void drawMaps(String mapDir, String minimapDir) {
        // fetch the names
        Connection sql = MySQLConnection.getInstance();
        HashMap<String, String> names = new HashMap<String, String>();
        try {
            Statement statement = sql.createStatement();
            ResultSet rs = statement.executeQuery("SELECT CONVERT(CONCAT_WS(\"_\", " +
                    "CAST(x/4096 as UNSIGNED), CAST(y/3072 as UNSIGNED)) USING latin1) as pos, " +
                    "name FROM fmofP.aafozone;");
            while (rs.next()) {
                names.put(rs.getString("pos"), rs.getString("name"));
            }
            rs.close();
            statement.close();
            sql.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // create the map directory
        if (!FileTools.createDir(mapDir)) {
            FileTools.emptyDir(mapDir);
        }

        // create the minimap directory
        if (!FileTools.createDir(minimapDir)) {
            FileTools.emptyDir(minimapDir);
        }

        // create a temporary folder
        if (FileTools.createDir(Config.tmpFolder)) {
            FileTools.emptyDir(Config.tmpFolder);
        }

        for (String pos : tmxObjects.keySet()) {
            final long[] before = {0};
            final long[] after = {0};
            String[] posStr = pos.split("_");
            Integer[] posInt = new Integer[] {Integer.valueOf(posStr[0]), Integer.valueOf(posStr[1])};
            BufferedImage img = drawMap(posInt[0], posInt[1]);
            try {
                String name = names.get(pos);

                // generate the minimaps
                if (name != null && Config.compileMinimaps) {
                    //String minimapDir = folder + "minimaps\\" + name + "\\";
                    BufferedImage resized = img;
                    for (int z = 0; z <= 3; z++) {
                        // final image
                        for (int x = 0, lenx = resized.getWidth()/128; x < lenx; x++) {
                            for (int y = 0, leny = resized.getHeight()/128; y < leny; y++) {
                                File outputfile = new File(Config.tmpFolder + "z" + z + "x" + x + "y" + y + ".png");
                                ImageIO.write(resized.getSubimage(x * 128, y * 128, 128, 128), "png", outputfile);
                                before[0] += new File(outputfile.getAbsolutePath()).length();
                                Process process = new ProcessBuilder("pngout\\pngout.exe","/y","/q",outputfile.getAbsolutePath()
                                ).start();
                                int exitCode = process.waitFor();
                                // 0 ~ compressed, 1 ~ File Error, 2 ~ can not compress further, 3 ~ bad options
                                if (exitCode != 0 && exitCode != 2) {
                                    System.err.println("Error: PNGOut exited with code " + exitCode + " on file \"" +
                                            "z" + z + "x" + x + "y" + y + ".png" + "\".");
                                }
                                after[0] += new File(outputfile.getAbsolutePath()).length();
                            }
                        }
                        // resize image
                        Image tookitImage = img.getScaledInstance(resized.getWidth()/2, resized.getHeight()/2, BufferedImage.SCALE_SMOOTH);
                        resized = new BufferedImage(resized.getWidth()/2, resized.getHeight()/2, BufferedImage.TYPE_INT_RGB);
                        resized.getGraphics().drawImage(tookitImage, 0, 0, null);
                    }
                    Zip.zip(new File(Config.tmpFolder), new File(minimapDir + name + ".zip"));
                    FileTools.emptyDir(Config.tmpFolder);
                    Config.logFile.log("Compression saved " + (Math.round(10000 * (1 - after[0] / (double) before[0])) / (double) 100) + "% for map \"" + name + "\".");
                }

                if (name == null) {
                    name = "unknown";
                }
                File outputfile = new File(mapDir + name + " (" + pos + ").png");
                ImageIO.write(img, "png", outputfile);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
        // delete tmp dir
        FileTools.deleteDir(Config.tmpFolder);

    }

    // constructor for the world
    public WorldGenerator(XmlLinkageReader xmlLinkage, String workingDir) {
        Point worldSize = xmlLinkage.getSize();

        worldWidth = worldSize.x * defaultMapWidth;
        worldHeight = worldSize.y * defaultMapHeight;

        // generate the walkable area (everything is not walkable for now)
        walkable = new byte[worldWidth * worldHeight];
        for (int i = 0; i < walkable.length; i++) {
            walkable[i] = (byte)Color.WHITE.getRGB();
        }

        // generate the tile ids (default id is zero)
        tileIds = new int[worldWidth * worldHeight];

        // offset for the tiles
        int tileIdOffset = 0;

        // loop over all the possible maps in the world (some might not be set)
        for (int x = 0; x < worldSize.x; x++) {
            for (int y = 0; y < worldSize.y; y++) {
                // get the map file uri for defined maps
                String[] mapFileName = xmlLinkage.getMapFile(x, y);
                if (mapFileName != null) {
                    if (new File(workingDir + mapFileName[0] + mapFileName[1]).exists()) {
                        // load data
                        TmxMapReader tmxMapReader = new TmxMapReader(workingDir + mapFileName[0] + mapFileName[1], workingDir + mapFileName[0] );
                        // write walkable data (to internal buffer)
                        writeWalkable(tmxMapReader.getWalkable(), x, y);
                        // write the tile ids (to internal buffer)
                        writeTileIds(tmxMapReader.getTileIds(), x, y, tileIdOffset);
                        // add the map tiles to the world tiles (with offset)
                        for (short tileId : tmxMapReader.getTileIds()) {
                            int offsetId = (int) tileId + tileIdOffset;
                            if (!tiles.containsKey(offsetId)) {
                                tiles.put(offsetId, tmxMapReader.getTile(tileId));
                            }
                        }
                        // store all objects
                        tmxObjects.put(x + "_" + y, tmxMapReader);
                        // update the tile id offset
                        tileIdOffset += tmxMapReader.getMaxTileId();
                    } else {
                        // badly defined manifest file
                        System.err.println("Error: Unknown xml file \"" + workingDir + mapFileName[0] + "\".");
                    }
                }
            }
        }
    }

    // draw the world as one image
    public final void drawWorld(String filename) {
        int factor = 3;
        BufferedImage image = new BufferedImage(worldWidth * factor, worldHeight * factor, BufferedImage.TYPE_INT_RGB);
        for (String pos : tmxObjects.keySet()) {
            String[] mapPos = pos.split("_");
            int[] mapPosInt = new int[] {Integer.valueOf(mapPos[0]), Integer.valueOf(mapPos[1])};
            BufferedImage map = drawMap(mapPosInt[0], mapPosInt[1]);
            Image mapSmall = map.getScaledInstance(defaultMapWidth * factor, defaultMapHeight * factor, BufferedImage.SCALE_SMOOTH);
            image.getGraphics().drawImage(mapSmall, mapPosInt[0] * defaultMapWidth * factor, mapPosInt[1] * defaultMapHeight * factor, null);
        }
        try {
            File outputfile = new File(filename);
            ImageIO.write(image, "png", outputfile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // prints the walkable information as png
    public final void printTileIds(String filename) {
        // create an image for testing
        BufferedImage img = new BufferedImage(worldWidth, worldHeight, BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < worldWidth; x++) {
            for (int y = 0; y < worldHeight; y++) {
                img.setRGB(x,y,tileIds[y * worldWidth + x]);
            }
        }
        try {
            File outputfile = new File(filename);
            ImageIO.write(img, "png", outputfile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // prints the tile id information as png
    public final void printWalkable(String filename) {
        // create an image for testing
        BufferedImage img = new BufferedImage(worldWidth, worldHeight, BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < worldWidth; x++) {
            for (int y = 0; y < worldHeight; y++) {
                img.setRGB(x,y,
                        walkable[y * worldWidth + x] == 0
                                ? Color.BLACK.getRGB()
                                : Color.WHITE.getRGB()
                );
            }
        }
        try {
            File outputfile = new File(filename);
            ImageIO.write(img, "png", outputfile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // index for the tiles (maps tiles to the colors used in them)
    HashMap<Integer, Integer[]> tileToColors = new HashMap<Integer, Integer[]>();
    // maps colors to index (to keep values small)
    HashMap<Integer, Integer> colorIndex = new HashMap<Integer, Integer>();
    // delete reset duplicate tiles (all tiles are actually used!)
    public final void indexTiles() {
        // build index
        for (Map.Entry<Integer, BufferedImage> entry : tiles.entrySet()) {
            // find all colors in the image
            Map<Integer,Integer> map = new HashMap<Integer,Integer>();
            BufferedImage img = entry.getValue();
            for (int x = 0, w = img.getWidth(); x < w; x++) {
                for (int y = 0, h = img.getHeight(); y < h; y++) {
                    int rgb = img.getRGB(x, y);
                    if (!colorIndex.containsKey(rgb)) {
                        colorIndex.put(rgb, colorIndex.size());
                    }
                    rgb = colorIndex.get(rgb);
                    if (!map.containsKey(rgb)) {
                        map.put(rgb, 1);
                    } else {
                        map.put(rgb, map.get(rgb) + 1);
                    }
                }
            }
            ArrayList<Integer> colors = new ArrayList<Integer>(map.keySet());
            Collections.sort(colors);
            Integer[] list = new Integer[colors.size()];
            colors.toArray(list);
            // add to index
            tileToColors.put(entry.getKey(), list);
        }
    }

    // maximum tile sheet size
    private static final int tileSheetSize = 70;

    // will hold the tile information (which id is mapped to which sheet and position)
    private ArrayList<Tile> tilesLogic = new ArrayList<Tile>();
    // holds the new tile ids information
    private short[] newMapTileIdData = null;
    // will map the tile sheet ids to the png images
    private ArrayList<String> tileSheetPng = new ArrayList<String>();

    public void clusterDensityWrite(String sheetDir) {
        TileClusterManager tileClusterManager = new TileClusterManager(tiles);
        ArrayList<TileCluster> clusters = tileClusterManager.getClusters();

        // holds the new map tile id data
        newMapTileIdData = new short[walkable.length];

        // current id number
        int idcount = 0;

        for (int i = 0; i < clusters.size(); i++) {
            int dist = -1;
            int sizex = 12;
            // calculate the best width
            for (int j = 12; j > 4; j--) {
                int tmp2 = j - (clusters.get(i).getTiles().size() - 1) % j;
                if (dist == -1 || tmp2 < dist) {
                    dist = tmp2;
                    sizex = j;
                }
            }
            // create the tile sheet
            BufferedImage newTileImage = new BufferedImage(
                    tileSize * sizex,
                    tileSize * (int) Math.ceil(clusters.get(i).getTiles().size() / (double) sizex),
                    BufferedImage.TYPE_INT_RGB
            );
            // write the icons to the tile sheets and update the position information
            int j = 0;
            for (vitco.cluster.Tile tile : clusters.get(i).getTiles()) {
                // hold the id
                int id = tile.getId();
                // get the image
                newTileImage.getGraphics().drawImage(
                        tiles.get(id),
                        (j % sizex) * tileSize,
                        (int) Math.floor(j / (double)sizex) * tileSize,
                        (j % sizex) * tileSize + tileSize,
                        (int) Math.floor(j / (double)sizex) * tileSize + tileSize,
                        0, 0, tileSize, tileSize, null);
                // update the map tile id data
                for (int k = 0; k < tileIds.length; k++) {
                    if (tileIds[k] == id) {
                        newMapTileIdData[k] = (short) (idcount);
                    }
                }
                // update the tile information
                tilesLogic.add(new Tile(i, (j % sizex) * tileSize, (int) Math.floor(j / (double)sizex) * tileSize, idcount));
                idcount++;
                j++;
            }

            try {
                String filename = sheetDir + "tile" + i + ".png";
                File outputfile = new File(filename);
                ImageIO.write(newTileImage, "png", outputfile);
                Process process = new ProcessBuilder("pngout\\pngout.exe","/y","/q",
                        outputfile.getAbsolutePath()
                ).start();
                process.waitFor();
                tileSheetPng.add(filename);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

    // cluster tiles and write images
    public void clusterWrite(String sheetDir) {
        // group tiles by similarity
        // holds the list of groups
        ArrayList<GroupRep> groupList = new ArrayList<GroupRep>();

        // create the Tile representation for all tiles (for easy color access)
        ArrayList<TileRep> tileRep = new ArrayList<TileRep>();
        for (Map.Entry<Integer, BufferedImage> entry : tiles.entrySet()) {
            tileRep.add(new TileRep(entry.getValue(), entry.getKey()));
        }

        // the ids of the tiles that were already assigned to groups
        Set<Integer> used = new HashSet<Integer>();

        // loop over thresholds (every threshold means that the png index would grow above the threshold)
        int thres = 1;
        while (thres <= 2048) {
            // find the group for these parameters
            TileTools.getGroup(tileRep, thres, tileSheetSize, tileSheetSize, used);
            // loop over found groups
            for (int i = 0, len = TileTools.lastGroupIdList.size(); i < len; i++) {
                // update the used tiles
                for (Integer integer : TileTools.lastGroupIdList.get(i)) {
                    used.add(integer);
                }
                // add this group to the group list
                groupList.add(TileTools.lastGroupList.get(i));
            }
            // increase threshold
            thres = thres * 2;
        }
        // add all the missing tiles (not grouped yet)
        GroupRep tmp = new GroupRep();
        for (int i = 0; i < tiles.size(); i++) {
            if (!used.contains(i)) {
                tmp.addTile(tileRep.get(i));
            }
        }
        // only add if there were missing tiles
        if (tmp.getTiles().size() > 0) {
            groupList.add(tmp);
        }

        // ++++++++++++++++++++++++

        // holds the new map tile id data
        newMapTileIdData = new short[walkable.length];

        // current id number
        int idcount = 0;

        for (int i = 0; i < groupList.size(); i++) {
            int dist = -1;
            int sizex = 12;
            // calculate the best width
            for (int j = 12; j > 4; j--) {
                int tmp2 = j - (groupList.get(i).getTiles().size() - 1) % j;
                if (dist == -1 || tmp2 < dist) {
                    dist = tmp2;
                    sizex = j;
                }
            }
            // create the tile sheet
            BufferedImage newTileImage = new BufferedImage(
                    tileSize * sizex,
                    tileSize * (int) Math.ceil(groupList.get(i).getTiles().size() / (double) sizex),
                    BufferedImage.TYPE_INT_RGB
            );
            // write the icons to the tile sheets and update the position information
            for (int j = 0; j < groupList.get(i).getTiles().size(); j++) {
                // hold the id
                int id = groupList.get(i).getTiles().get(j).id;
                // get the image
                newTileImage.getGraphics().drawImage(
                        tiles.get(id),
                        (j % sizex) * tileSize,
                        (int) Math.floor(j / (double)sizex) * tileSize,
                        (j % sizex) * tileSize + tileSize,
                        (int) Math.floor(j / (double)sizex) * tileSize + tileSize,
                        0, 0, tileSize, tileSize, null);
                // update the map tile id data
                for (int k = 0; k < tileIds.length; k++) {
                    if (tileIds[k] == id) {
                        newMapTileIdData[k] = (short) (idcount);
                    }
                }
                // update the tile information
                tilesLogic.add(new Tile(i, (j % sizex) * tileSize, (int) Math.floor(j / (double)sizex) * tileSize, idcount));
                idcount++;
            }

            try {
                String filename = sheetDir + "tile" + i + ".png";
                File outputfile = new File(filename);
                ImageIO.write(newTileImage, "png", outputfile);
                Process process = new ProcessBuilder("pngout\\pngout.exe","/y","/q",
                        outputfile.getAbsolutePath()
                ).start();
                process.waitFor();
                tileSheetPng.add(filename);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    // write map data
    public final void writeZipMap(String filename) {
        try {
            // initialize output stream
            Out out = new Out("world");
            // write basic dimensions
            out.writeIntRev(worldHeight);
            out.writeIntRev(worldWidth);
            out.writeIntRev(tileSize);
            out.writeIntRev(tileSize);

            // write tile id data size
            out.writeIntRev(walkable.length);

            // write the tile id data
            for (short aNewMapTileIdData : newMapTileIdData) {
                out.writeShortRev(aNewMapTileIdData);
            }

            // write the walk-able information
            for (byte aWalkable : walkable) {
                out.writeByte(aWalkable);
            }

            // stop, so we can compress the png files
            System.out.println("Please compress pngs and press return...");
            //noinspection ResultOfMethodCallIgnored
            System.in.read();

            // write the amount of tile sheets
            out.writeIntRev(tileSheetPng.size());
            // write all tile sheets
            for (String tileSheetFileName : tileSheetPng) {
                out.writeImage(new File(tileSheetFileName));
            }

            // write the mappings "tile id" -> "position in tile image"
            // how many tile IDs are there
            out.writeIntRev(tilesLogic.size());
            // write the mappings
            for (Tile tile : tilesLogic) {
                out.writeIntRev(tile.chunk);
                out.writeIntRev(tile.x);
                out.writeIntRev(tile.y);
            }

            // close the output stream
            out.finish();

            // output the finalized data
            Zip.pack(filename, "world");

            // delete the temporary data
            if (!new File("world").delete()) {
                System.err.println("Error: Can not clean temporary file!");
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    // cluster tiles
    public void cluster() {
        double[][] points = new double[tiles.size()][colorIndex.size()];
        int c = 0;
        for (final Integer i : tiles.keySet()) {
            Integer[] tmp = tileToColors.get(i);
            for (Integer aTmp : tmp) {
                points[c][aTmp] = 1;
            }
            c++;
        }
        double[][] centroids = new double[(int)Math.ceil(tiles.size()/(double) tileSheetSize)][colorIndex.size()];
        Random rand = new Random();
        for (int i = 0; i < centroids.length; i++) {
            for (int j = 0; j < centroids[i].length; j++) {
                centroids[i][j] = rand.nextBoolean() ? 1 : 0;
            }
        }
        EKmeans eKmeans = new EKmeans(centroids, points);
        eKmeans.setIteration(128);
        eKmeans.setEqual(true);
        eKmeans.setDistanceFunction(EKmeans.MANHATTAN_DISTANCE_FUNCTION);
        eKmeans.run();
        int[] assignments = eKmeans.getAssignments();
        for (int i = 0; i < assignments.length; i++) {
            System.out.println(MessageFormat.format("point {0} is assigned to cluster {1}", i, assignments[i]));
        }

        HashMap<Integer, HashMap<Integer, Boolean>> val = new HashMap<Integer, HashMap<Integer, Boolean>>();
        HashMap<Integer, Integer> count = new HashMap<Integer, Integer>();

        for (int i = 0; i < assignments.length; i++) {
            if (!val.containsKey(assignments[i])) {
                val.put(assignments[i], new HashMap<Integer, Boolean>());
                count.put(assignments[i], 1);
            } else {
                count.put(assignments[i], count.get(assignments[i]) + 1);
            }
            HashMap<Integer, Boolean> colors = val.get(assignments[i]);
            double[] loc = points[i];
            for (int j = 0; j < loc.length; j++) {
                if (loc[j] == 1) {
                    colors.put(j, true);
                }
            }
        }

        for (Map.Entry<Integer, HashMap<Integer, Boolean>> entry : val.entrySet()) {
            System.out.println(entry.getKey() + " @ " + entry.getValue().size() + " with " + count.get(entry.getKey()));
        }
    }

    // delete tiles that are duplicates
    // returns the amount of tiles that were removed
    public int deleteDuplicateTiles() {
        int sizeBefore = tiles.size();
        ArrayList<Integer[]> possibleDuplicates = new ArrayList<Integer[]>();
        for (Integer idx1 : tileToColors.keySet()) {
            for (Integer idx2 : tileToColors.keySet()) {
                if (!idx1.equals(idx2)) {
                    if (Arrays.equals(tileToColors.get(idx1), tileToColors.get(idx2))) {
                        possibleDuplicates.add(new Integer[] {idx1, idx2});
                    }
                }
            }
        }
        for (Integer[] dup : possibleDuplicates) {
            if (tiles.containsKey(dup[0]) && tiles.containsKey(dup[1])) {
                // prepare data
                int[] a = ((DataBufferInt) tiles.get(dup[0]).getRaster().getDataBuffer()).getData();
                int[] b = ((DataBufferInt) tiles.get(dup[1]).getRaster().getDataBuffer()).getData();
                boolean equal = Arrays.equals(a, b);
                if (equal) {
                    // replace all mappings
                    for (int k = 0; k < tileIds.length; k++) {
                        if (tileIds[k] == dup[1]) {
                            tileIds[k] = dup[0];
                        }
                    }
                    // delete tile entry
                    tiles.remove(dup[1]);
                }
            }
        }
        return sizeBefore - tiles.size();
    }

    // assign flips and rotations
    public void findRotationMirroring() {
        // maps tile md5 hash to Integer[]: (tileid, tilerotation, tilemirror)
        HashMap<String, Integer[]> tileMD5 = new HashMap<String, Integer[]>();

        int count = 0;

        // add all variations
        for (Map.Entry<Integer, BufferedImage> entry : tiles.entrySet()) {
            // generate variations
            HashMap<String, Integer[]> variations = new HashMap<String, Integer[]>();

            BufferedImage img = ImgTools.deepCopy(entry.getValue());
            // rotate image once
            BufferedImage rotImg = ImgTools.deepCopy(entry.getValue());
            Graphics2D g = rotImg.createGraphics();
            g.rotate(Math.toRadians(90), 16, 16);
            g.drawImage(img, 0, 0, null);
            g.dispose();

            // add without variations
            variations.put(ImgTools.getHash(img), new Integer[]{entry.getKey(), 0, 0});
            // add mirroring
            img.getGraphics().drawImage(entry.getValue(), 0, 0, 32, 32, 32, 0, 0, 32, null);
            variations.put(ImgTools.getHash(img), new Integer[]{entry.getKey(), 0, 1});
            // add mirroring + 2x rotation
            img.getGraphics().drawImage(entry.getValue(), 0, 0, 32, 32, 0, 32, 32, 0, null);
            variations.put(ImgTools.getHash(img), new Integer[]{entry.getKey(), 2, 1});
            // add 2x rotation
            img.getGraphics().drawImage(entry.getValue(), 0, 0, 32, 32, 32, 32, 0, 0, null);
            variations.put(ImgTools.getHash(img), new Integer[]{entry.getKey(), 2, 0});

            // add 1x rotation
            variations.put(ImgTools.getHash(rotImg), new Integer[]{entry.getKey(), 1, 0});
            // add mirroring + 3x rotation
            img.getGraphics().drawImage(rotImg, 0, 0, 32, 32, 32, 0, 0, 32, null);
            variations.put(ImgTools.getHash(img), new Integer[]{entry.getKey(), 3, 1});
            // add mirroring + 1x rotation
            img.getGraphics().drawImage(rotImg, 0, 0, 32, 32, 0, 32, 32, 0, null);
            variations.put(ImgTools.getHash(img), new Integer[]{entry.getKey(), 1, 1});
            // add 3x rotation
            img.getGraphics().drawImage(rotImg, 0, 0, 32, 32, 32, 32, 0, 0, null);
            variations.put(ImgTools.getHash(img), new Integer[]{entry.getKey(), 3, 0});

            boolean isVariation = false;

            // check that non of the variations is already containsed
            for (Map.Entry<String, Integer[]> variation : variations.entrySet()) {
                if (tileMD5.containsKey(variation.getKey())) {
                    isVariation = true;
                    // remember this
                    BufferedImage compare = new BufferedImage(64, 32, BufferedImage.TYPE_INT_ARGB);
                    compare.getGraphics().drawImage(
                            entry.getValue(), 0, 0, null);
                    compare.getGraphics().drawImage(
                            tiles.get(tileMD5.get(variation.getKey())[0]), 32, 0, null);
                    try {
                        ImageIO.write(compare, "png", new File(entry.getKey() + "_" +
                                tileMD5.get(variation.getKey())[0] + ".png"));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                }
            }

            // remember all the variations
            if (!isVariation) {
                tileMD5.putAll(variations);
                count++;
            }

        }
        System.out.println(count);
    }

}
