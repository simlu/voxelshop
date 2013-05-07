package vitco.cluster;

import java.awt.image.BufferedImage;
import java.util.*;

/**
 * Manages the tile cluster creation.
 */
public class TileClusterManager {
    // holds the result
    private static final ArrayList<TileCluster> result = new ArrayList<TileCluster>();

    public final ArrayList<TileCluster> getClusters() {
        return result;
    }

    private static final int clusterSize = 144;

    private static final int startThres = 256;
    private static final int maxThres = 256;

    // constructor
    public TileClusterManager(HashMap<Integer, BufferedImage> input) {

        // generate all tiles
        HashSet<Tile> tiles = new HashSet<Tile>();
        for (Map.Entry<Integer, BufferedImage> tile : input.entrySet()) {
            tiles.add(new Tile(tile.getKey(), tile.getValue()));
        }

        // do the clustering
        int thres = startThres;
        while (thres <= maxThres) {
            // find all the tiles that are below the threshold
            ArrayList<Tile> toCluster = new ArrayList<Tile>();
            for (Tile tile : tiles) {
                if (tile.count <= thres) {
                    toCluster.add(tile);
                }
            }

            // will hold the clusters for this threshold
            ArrayList<TileCluster> clusters = new ArrayList<TileCluster>();

            // sort smallest value first (tiles that have few colors)
            Collections.sort(toCluster, new Comparator<Tile>() {
                @Override
                public int compare(Tile o1, Tile o2) {
                    return (int)Math.signum(o1.count - o2.count);
                }
            });

            // initialize clusters with different primary colors (use the
            // tiles that have highest percentage of that color
            // and have it as a primary color)
            HashMap<Integer, Tile> seedTiles = new HashMap<Integer, Tile>();
            for (Tile tile : tiles) {
                if (seedTiles.containsKey(tile.color)) {
                    if (seedTiles.get(tile.color).colorPerc < tile.colorPerc) {
                        seedTiles.put(tile.color, tile);
                    }
                } else {
                    seedTiles.put(tile.color, tile);
                }
            }
            for (Tile tile : seedTiles.values()) {
                TileCluster cluster = new TileCluster(thres);
                cluster.addTile(tile);
                toCluster.remove(tile);
                clusters.add(cluster);
            }

            // try to cluster all the elements
            int iterations = 0;
            while (toCluster.size() > 0) {
                // retrieve the tile
                Tile tile = toCluster.remove(0);

                // find the best matching cluster for this tile
                // (biggest overlap)
                TileCluster mergeCluster = null;
                double overlap = 0;
                for (TileCluster cluster : clusters) {
                    // if the tile is not blocked for this cluster
                    if (!tile.isBlocked (cluster)) {
                        // compute the overlap
                        Double cOverlap = cluster.getMergeInfo(tile);
                        if (cOverlap != null &&
                                 (mergeCluster == null || cOverlap > overlap)) {
                            mergeCluster = cluster;
                            overlap = cOverlap;
                        }
                    }
                }

                // add this tile to the best cluster
                // or form a new one
                if (mergeCluster == null) {
                    mergeCluster = new TileCluster(thres);
                    clusters.add(mergeCluster);
                }
                mergeCluster.addTile(tile);

                // fetch information
                int tileCount = mergeCluster.getTileCount();
                int tileAddedCount = mergeCluster.getAddedCount();

                // check if this cluster is too full (block)
                if (tileCount > clusterSize) {
                    toCluster.add(mergeCluster.deleteTile(true));
                } else {
                    // delete a tile for every second tile that was added (no block)
                    if (tileCount >= 6 && tileAddedCount%2 == 0) {
                        toCluster.add(mergeCluster.deleteTile(false));
                    }
                }

                // start pruning clusters
                if (toCluster.size() == 0) {
                    if (iterations < 50) {
                        for (int i = 0; i < clusters.size(); i++) {
                            TileCluster cluster = clusters.get(i);
                            // delete five entries from every cluster
                            for (int j = 0; j < 5; j++) {
                                if (cluster.getTileCount() > 0) {
                                    toCluster.add(cluster.deleteTile(false));
                                }
                            }
                            // remove cluster if is is now empty
                            if (cluster.getTileCount() == 0) {
                                clusters.remove(i);
                                i--;
                            }
                        }
                    } else if (iterations < 100) {
                        for (int i = 0; i < clusters.size(); i++) {
                            if (clusters.size() < clusterSize) {
                                TileCluster cluster = clusters.get(i);
                                // delete five entries from every cluster
                                for (int j = 0; j < 5; j++) {
                                    if (cluster.getTileCount() > 0) {
                                        toCluster.add(cluster.deleteTile(false));
                                    }
                                }
                                // remove cluster if is is now empty
                                if (cluster.getTileCount() == 0) {
                                    clusters.remove(i);
                                    i--;
                                }
                            }
                        }
                    }
//                    Collections.sort(toCluster, new Comparator<Tile>() {
//                        @Override
//                        public int compare(Tile o1, Tile o2) {
//                            return (int)Math.signum(o1.count - o2.count);
//                        }
//                    });
                    Collections.shuffle(toCluster);
                    iterations++;
                }
            }

            // see what we got
            System.out.println("### " + thres + " " + clusters.size());
            int count = 0;
            for (TileCluster cluster : clusters) {
                if (thres >= maxThres || cluster.getTileCount() >= clusterSize) {
                    // order the cluster by prominent colors
                    cluster.sort();
                    result.add(cluster);
                    System.out.println(cluster.getTileCount() + " @ " + cluster.getColorCount());
                    for (Tile tile : cluster.getTiles()) {
                        tiles.remove(tile);
                    }
                    count++;
                }
            }
            System.out.println("=== " + thres + " " + count + "/" + clusters.size());

            thres *= 2;
        }

        // add remaining tiles
        if (tiles.size() > 0) {
            TileCluster cluster = new TileCluster(0);
            for (Tile tile : tiles) {
                cluster.addTile(tile);
            }
            cluster.sort();
            result.add(cluster);
            System.out.println(cluster.getTileCount() + " @ " + cluster.getColorCount());
        }
    }
}