package vitco.cluster;

import com.google.common.collect.Sets;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;

/**
 * Represents a cluster
 */
public class TileCluster {

    private final static Random rand = new Random();

    // holds all the colors of this cluster (with count)
    private final HashMap<Integer, Integer> colors = new HashMap<Integer, Integer>();

    // holds all the tiles of this cluster
    private final HashSet<Tile> tiles = new HashSet<Tile>();

    public final void addTile(Tile tile) {
        for (int color : tile.getColorSet()) {
            if (colors.containsKey(color)) {
                colors.put(color, colors.get(color) + 1);
            } else {
                colors.put(color, 1);
            }
        }
        tiles.add(tile);
    }

    // returns null if adding this tile would result
    // in cluster above threshold
    public final Integer getMergeInfo(Tile tile) {
        int wouldBeColorCount = Sets.union(colors.keySet(), tile.getColorSet()).size();
        if (wouldBeColorCount > threshold) {
            return null;
        } else {
            return (wouldBeColorCount - colors.size());
               // + (int)Math.ceil(MathTools.binlog(wouldBeColorCount)) * 2;
        }
    }

    public final HashSet<Tile> getTiles() {
        return tiles;
    }

    public final int getTileCount() {
        return tiles.size();
    }

    public final int getColorCount() {
        return colors.size();
    }

    public final Tile deleteTileRand() {
        int randTile = rand.nextInt(tiles.size());
        Tile toRemove = null;
        for (Tile tile : tiles) {
            if (randTile == 0) {
                toRemove = tile;
                break;
            }
            randTile--;
        }
        tiles.remove(toRemove);
        // clear the colors
        assert toRemove != null;
        for (int color : toRemove.getColorSet()) {
            int newCount = colors.get(color)-1;
            if (newCount > 0) {
                colors.put(color, newCount);
            } else {
                colors.remove(color);
            }
        }
        return toRemove;
    }

    public final Tile deleteTile() {
        // find the color with the lowest count
        Integer toRemoveColor = null;
        int count = 0;
        for (Map.Entry<Integer, Integer> color : colors.entrySet()) {
            if (toRemoveColor == null || count > color.getValue()) {
                toRemoveColor = color.getKey();
                count = color.getValue();
            }
        }
        // find all the images that have this value
        // and identify the one with the smallest count
        // in other colors
        Tile toRemove = null;
        int size = 0;
        for (Tile tile : tiles) {
            int thisSize = 0;
            for (Integer color : tile.getColorSet()) {
                thisSize += colors.get(color);
            }
            //int thisSize = tile.getColorSet().size();
            if (tile.getColorSet().contains(toRemoveColor)) {
                if (toRemove == null || size > thisSize) {
                    toRemove = tile;
                    size = thisSize;
                }
            }
        }
        assert toRemove != null;
        // mark this cluster as blocked for the tile
        toRemove.blockCluster(this);
        // remove the tile
        tiles.remove(toRemove);
        // clear the colors
        for (int color : toRemove.getColorSet()) {
            int newCount = colors.get(color)-1;
            if (newCount > 0) {
                colors.put(color, newCount);
            } else {
                colors.remove(color);
            }
        }
        return toRemove;
    }

    private final int threshold;
    public TileCluster(int threshold) {
        this.threshold = threshold;
    }
}
