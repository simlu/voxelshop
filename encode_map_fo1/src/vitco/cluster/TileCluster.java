package vitco.cluster;

import com.google.common.collect.Sets;

import java.util.*;

/**
 * Represents a cluster
 */
public class TileCluster {

    // holds all the colors of this cluster
    // mapped to the percentage SUM of all tiles
    private final HashMap<Integer, Double> colors = new HashMap<Integer, Double>();

    // holds all the tiles of this cluster
    private final ArrayList<Tile> tiles = new ArrayList<Tile>();

    // add a tile to this cluster
    public final void addTile(Tile tile) {
        // update the color information
        for (Map.Entry<Integer, Double> color : tile.getColors().entrySet()) {
            Integer colorId = color.getKey();
            if (colors.containsKey(colorId)) {
                colors.put(colorId, colors.get(colorId) + color.getValue());
            } else {
                colors.put(colorId, color.getValue());
            }
        }
        // increase counter
        added++;
        // add the tile
        tiles.add(tile);
    }

    // internal - retrieve the overlap in colors of a specific tile
    // with this cluster
    private double overlap(Tile tile) {
        double result = 0;
        // loop over all the colors in the tile
        for (Map.Entry<Integer, Double> color : tile.getColors().entrySet()) {
            int colorId = color.getKey();
            if (colors.containsKey(colorId)) {
                // how much percent of the cluster is this color
                double percentCluster = colors.get(color.getKey()) / colors.size();
                result += percentCluster * color.getValue();
            }
        }
        // normalize by colors in tile
        return result / tile.count;
    }

    // returns null if adding this tile would result
    // in cluster above threshold
    public final Double getMergeInfo(Tile tile) {
        int wouldBeColorCount = Sets.union(colors.keySet(), tile.getColors().keySet()).size();
        if (wouldBeColorCount > threshold) {
            return null;
        } else {
            // calculate how well the tile would fit into this cluster
            // (we use the percentage of colors contained in the tile)
            return overlap(tile);
        }
    }

    // returns how many tiles were already added to this cluster
    private int added = 0;
    public final int getAddedCount() {
        return added;
    }

    public final ArrayList<Tile> getTiles() {
        return tiles;
    }

    public final int getTileCount() {
        return tiles.size();
    }

    public final int getColorCount() {
        return colors.size();
    }

    // sort this cluster
    public final void sort() {
        Collections.sort(tiles, new Comparator<Tile>() {
            @Override
            public int compare(Tile o1, Tile o2) {
                return o1.color == o2.color
                        ? (int)Math.signum(o2.colorPerc - o1.colorPerc)
                        : o2.color - o1.color;
            }
        });
    }

    public final Tile deleteTile(boolean blockCluster) {
        // find the tile with the smallest overlap
        Tile toRemove = null;
        double overlap = 0;
        for (Tile tile : tiles) {
            double cOverlap = overlap(tile);
            if (toRemove == null || cOverlap < overlap) {
                toRemove = tile;
                overlap = cOverlap;
            }
        }
        assert toRemove != null;
        // block the tile
        if (blockCluster) {
            toRemove.blockCluster(this);
        }
        // remove the tile
        tiles.remove(toRemove);
        // remove the colors
        for (Map.Entry<Integer, Double> color : toRemove.getColors().entrySet()) {
            Integer colorId = color.getKey();
            double newVal = colors.get(colorId) - color.getValue();
            if (newVal == 0) {
               colors.remove(colorId);
            } else {
                colors.put(colorId, newVal);
            }
        }
        // return the tile that was deleted
        return toRemove;
    }

    private final int threshold;
    public TileCluster(int threshold) {
        this.threshold = threshold;
    }
}
