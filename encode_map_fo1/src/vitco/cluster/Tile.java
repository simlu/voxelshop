package vitco.cluster;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * Implements a clusterable object.
 */
public class Tile {
    // the id of this tile
    public final int id;

    // holds the color count
    public final int count;

    // maps the colors present in this tile to their percentage
    // of the whole tile
    private final HashMap<Integer, Double> colors = new HashMap<Integer, Double>();

    // most prominent color in the tile
    public final int color;
    public final double colorPerc;

    // constructor
    public Tile(int id, BufferedImage tile) {
        // assign the id
        this.id = id;
        // generate the color information for this tile
        for (Integer color : tile.getRGB(0, 0, 32, 32, null, 0, 32)) {
            if (colors.containsKey(color)) {
                colors.put(color, colors.get(color) + 1);
            } else {
                colors.put(color, 1d);
            }
        }
        // convert to percentage
        for (Map.Entry<Integer, Double> entry : colors.entrySet()) {
            colors.put(entry.getKey(), entry.getValue() / 1024);
        }
        // set the color with highest percentage as prominent color
        Integer max = null;
        double val = 0;
        for (Map.Entry<Integer, Double> entry : colors.entrySet()) {
            if (max == null || val < entry.getValue()) {
                val = entry.getValue();
                max = entry.getKey();
            }
        }
        color = max;
        colorPerc = val;
        // set the color count of this tile
        count = colors.size();
    }

    // retrieve the colors of this tile
    public final HashMap<Integer, Double> getColors() {
        return colors;
    }

    // ############################
    // blocking
    // ############################

    // holds the blocked clusters for this tile
    // (the tile can not be assigned to them anymore)
    private final HashSet<TileCluster> blocked = new HashSet<TileCluster>();
    // block a cluster for this tile
    public void blockCluster(TileCluster cluster) {
        blocked.add(cluster);
    }
    // check if a given cluster is blocked for this tile
    public final boolean isBlocked(TileCluster cluster) {
        return blocked.contains(cluster);
    }

}
