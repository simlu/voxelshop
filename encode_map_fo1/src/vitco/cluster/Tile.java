package vitco.cluster;

import java.util.HashSet;

/**
 * Implements a clusterable object.
 */
public class Tile {
    private final int id;
    private final HashSet<Integer> colorSet;
    public Tile(int id, HashSet<Integer> colorSet) {
        this.id = id;
        this.colorSet = colorSet;
    }

    public int getId() {
        return id;
    }

    private final HashSet<TileCluster> blocked = new HashSet<TileCluster>();
    public void blockCluster(TileCluster cluster) {
        blocked.add(cluster);
    }

    public final boolean isBlocked(TileCluster cluster) {
        return blocked.contains(cluster);
    }

    public HashSet<Integer> getColorSet() {
        return colorSet;
    }
}
