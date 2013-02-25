package vitco.cluster;

import java.awt.image.BufferedImage;
import java.util.*;

/**
 * Manages the tile cluster creation.
 */
public class TileClusterManager {
    private static HashMap<String, Integer> distance = new HashMap<String, Integer>();

    // holds the result
    private static final ArrayList<TileCluster> result = new ArrayList<TileCluster>();

    public final ArrayList<TileCluster> getClusters() {
        return result;
    }

    // constructor
    public TileClusterManager(HashMap<Integer, BufferedImage> tiles) {

        // compute the colors for each tile
        HashMap<Integer, HashSet<Integer>> colorCount = new HashMap<Integer, HashSet<Integer>>();
        for (Map.Entry<Integer, BufferedImage> tile : tiles.entrySet()) {
            HashSet<Integer> colors = new HashSet<Integer>();
            for (Integer color : tile.getValue().getRGB(0, 0, 32, 32, null, 0, 32)) {
                colors.add(color);
            }
            colorCount.put(tile.getKey(), colors);
        }

        // do the clustering
        int thres = 16;
        while (thres <= 1024) {
            // find all the elements that we need to cluster
            ArrayList<Tile> toCluster = new ArrayList<Tile>();
            for (Map.Entry<Integer, HashSet<Integer>> tile : colorCount.entrySet()) {
                if (tile.getValue().size() <= thres) {
                    toCluster.add(new Tile(tile.getKey(), tile.getValue()));
                }
            }

            // will hold the clusters for this threshold
            ArrayList<TileCluster> clusters = new ArrayList<TileCluster>();

            // sort smallest value first
            Collections.sort(toCluster, new Comparator<Tile>() {
                @Override
                public int compare(Tile o1, Tile o2) {
                    return o1.getColorSet().size() - o2.getColorSet().size();
                }
            });

            ArrayList<Tile> availableTiles = new ArrayList<Tile>();
            availableTiles.addAll(toCluster);
            HashSet<Tile> blocked = new HashSet<Tile>();

            boolean pending = true;
            while (pending) {
                // initialize the cluster

                TileCluster newCluster = new TileCluster(thres);
                for (Tile availableTile : availableTiles) {
                    if (!blocked.contains(availableTile)) {
                        newCluster.addTile(availableTile);
                        blocked.add(availableTile);
                        break;
                    }
                }

                if (newCluster.getTileCount() == 1) {

                    // loop over all tiles and find the most promising once
                    boolean tileAdded = true;
                    while (tileAdded && newCluster.getTileCount() < 70) {
                        tileAdded = false;
                        // find next tile
                        int distVal = 0;
                        Tile toAdd = null;
                        for (Tile tile : availableTiles) {
                            if (!newCluster.getTiles().contains(tile)) {
                                Integer dist = newCluster.getMergeInfo(tile);
                                if (dist != null && (toAdd == null || dist < distVal)) {
                                    toAdd = tile;
                                    distVal = dist;
                                }
                            }
                        }
                        // add the tile
                        if (toAdd != null) {
                            newCluster.addTile(toAdd);
                            tileAdded = true;
                        }
                    }
                    // we found a cluster
                    if (newCluster.getTileCount() == 70) {
                        clusters.add(newCluster);
                        availableTiles.removeAll(newCluster.getTiles());
                        System.out.println("### " + newCluster.getTileCount() +
                                " " + newCluster.getColorCount());
                    }

                } else {
                    pending = false;
                }
            }

            /*

            // try to cluster all the elements
            int iterations = 0;
            while (toCluster.size() > 0) {
                Tile tile = toCluster.remove(0);

                // find the best matching cluster for this tile
                TileCluster mergeCluster = null;
                int mergeVal = 0;
                for (TileCluster cluster : clusters) {
                    Integer mergeValThis = cluster.getMergeInfo(tile);
                    if (mergeValThis != null &&
                            !tile.isBlocked (cluster) &&
                            (mergeCluster == null || mergeVal > mergeValThis)) {
                        mergeCluster = cluster;
                        mergeVal = mergeValThis;
                    }
                }

                // add this tile to the best cluster
                if (mergeCluster == null) {
                    mergeCluster = new TileCluster(thres);
                    clusters.add(mergeCluster);
                }
                mergeCluster.addTile(tile);

                // check if this cluster is too full
                if (mergeCluster.getTileCount() > 70) {
                    toCluster.add(mergeCluster.deleteTile());
                }

//                if (toCluster.size() == 0 && iterations < thres/2) {
//                    // remove all clusters that are not full
//                    for (int i = 0; i < clusters.size(); i++) {
//                        TileCluster cluster = clusters.get(i);
//                        if (cluster.getTileCount() < iterations*20) {
//                            while (cluster.getTileCount() > 0) {
//                                toCluster.add(cluster.deleteTileRand());
//                            }
//                            clusters.remove(cluster);
//                            i--;
//                        }
//                    }
//                    Collections.shuffle(toCluster);
//                    iterations++;
//                }

//                // delete the cluster with the highest threshold
//                if (toCluster.size() == 0 && iterations >= 0) {
//
//                    Collections.sort(clusters, new Comparator<TileCluster>() {
//                        @Override
//                        public int compare(TileCluster o1, TileCluster o2) {
//                            return o2.getColorCount() - o1.getColorCount();
//                        }
//                    });
//
//                    for (int i = 0; i < iterations; i++) {
//                        if (clusters.size() > 1) {
//                            TileCluster toRemove = clusters.get(0);
//                            while (toRemove.getTileCount() > 0) {
//                                toCluster.add(toRemove.deleteTileRand());
//                            }
//                            clusters.remove(toRemove);
//                        }
//                    }
//                    Collections.shuffle(toCluster);
//                    iterations--;
//                }
                // if there are no more tiles to cluster
                // delete some tiles
                if (toCluster.size() == 0 && iterations < 10) {
                    for (int i = 0; i < clusters.size(); i++) {
                        TileCluster cluster = clusters.get(i);
                        if (cluster.getTileCount() < 70) {
                            while (cluster.getTileCount() > 0) {
                                toCluster.add(cluster.deleteTileRand());
                            }
                            clusters.remove(cluster);
                            i--;
                        }
                    }
                    Collections.shuffle(toCluster);
                    iterations++;
                }

            }

            */

            // see what we got
            System.out.println("### " + thres + " " + clusters.size());
            int count = 0;
            for (TileCluster cluster : clusters) {
                if (cluster.getTileCount() >= 70) {
                    result.add(cluster);
                    System.out.println(cluster.getTileCount() + " @ " + cluster.getColorCount());
                    for (Tile tile : cluster.getTiles()) {
                        colorCount.remove(tile.getId());
                    }
                    count++;
                }
            }
            System.out.println("=== " + thres + " " + count + "/" + clusters.size());

            thres *= 2;
        }

        // add remaining tiles
        if (colorCount.size() > 0) {
            TileCluster cluster = new TileCluster(2048);
            for (Map.Entry<Integer, HashSet<Integer>> tile : colorCount.entrySet()) {
                cluster.addTile(new Tile(tile.getKey(), tile.getValue()));
            }
            result.add(cluster);
        }


        /*

            // ################################
            // initial clustering with k means
            // ################################

            // collect the colors
            BiMap<Integer, Integer> colors = new BiMap<Integer, Integer>();
            for (Map.Entry<Integer, HashSet<Integer>> tile : toCluster.entrySet()) {
                int id = 0;
                for (Integer color : tile.getValue()) {
                    if (!colors.containsValue(color)) {
                        colors.put(id, color);
                        id++;
                    }
                }
            }
            int colorSize = colors.size();

            // create the data points for clustering
            ArrayList<TileClusterable> mPoints = new ArrayList<TileClusterable>();

            for (Map.Entry<Integer, HashSet<Integer>> tile : toCluster.entrySet()) {
                // extract the colors
                float[] vector = new float[colorSize];
                for (Integer color : tile.getValue()) {
                    vector[colors.getKey(color)] = 1;
                }
                mPoints.add(new TileClusterable(vector, tile.getValue(), tile.getKey()));
            }

            KMeansClusterer clusterer = new KMeansClusterer();
            Cluster[] clusters = clusterer.cluster(mPoints, (int)Math.floor(mPoints.size()/70));

            System.out.println("### " + thres);

            for (Cluster cluster : clusters) {
                HashSet<Integer> clusterColor = new HashSet<Integer>();
                for (Clusterable item : cluster.getItems()) {
                    clusterColor.addAll(((TileClusterable)item).getColorSet());
                }
                System.out.println(cluster.getItems().size() + " " + clusterColor.size());
                if (cluster.getItems().size() >= 70 && clusterColor.size() <= thres) {
                    int c = 0;
                    for (Clusterable item : cluster.getItems()) {
                        if (c < 70) {
                            colorCount.remove(((TileClusterable)item).getId());
                            c++;
                        } else {
                            break;
                        }
                    }
                    System.out.println("ok");
                }
            }

            //*/

        /*
        File bufferFile = new File(Config.tile_difference_buffer);
        if (bufferFile.exists()) {
            //noinspection unchecked
            distance = (HashMap<String, Integer>) FileTools.loadFromFile(bufferFile);
        }

        Long time = System.currentTimeMillis();
        // generate the md5 hash of all tiles
        HashMap<String, Integer> tileMD5 = new HashMap<String, Integer>();
        for (Map.Entry<Integer, BufferedImage> tile : tiles.entrySet()) {
            tileMD5.put(ImgTools.getHash(tile.getValue()), tile.getKey());
        }
        int c = 0;
        // compute all the distances
        for (Map.Entry<String, Integer> tile1 : tileMD5.entrySet()) {
            for (Map.Entry<String, Integer> tile2 : tileMD5.entrySet()) {
                if (!tile1.getKey().equals(tile2.getKey())) {
                    String key1 = tile1.getKey() + "_" + tile2.getKey();
                    String key2 = tile2.getKey() + "_" + tile1.getKey();
                    if (!distance.containsKey(key1) || !distance.containsKey(key2)) {
                        // calculate the color distance
                        HashSet<Integer> col1 = new HashSet<Integer>();
                        HashSet<Integer> col2 = new HashSet<Integer>();
                        BufferedImage img1 = tiles.get(tile1.getValue());
                        BufferedImage img2 = tiles.get(tile1.getValue());
                        // fetch all colors
                        for (Integer color : img1.getRGB(0,0, 32, 32, null, 0, 32)) {
                            col1.add(color);
                        }
                        for (Integer color : img2.getRGB(0,0, 32, 32, null, 0, 32)) {
                            col2.add(color);
                        }
                        Integer diff = Sets.symmetricDifference(col1, col2).size();
                        distance.put(key1, diff);
                        distance.put(key2, diff);
                    }
                }
            }
            //System.out.println(c++);
        }

        // save data
        FileTools.saveToFile(bufferFile, distance);

        System.out.println(System.currentTimeMillis() - time);
        //*/
    }
}
