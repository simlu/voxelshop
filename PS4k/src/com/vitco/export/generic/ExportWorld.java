package com.vitco.export.generic;

import com.vitco.core.data.container.Voxel;
import com.vitco.low.triangulate.Grid2TriGreedyOptimal;
import com.vitco.low.triangulate.Grid2TriMono;
import com.vitco.low.triangulate.Grid2TriNaiveGreedy;
import com.vitco.low.triangulate.Grid2TriPolyFast;
import com.vitco.low.triangulate.util.Grid2PolyHelper;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Helps with managing the data structure, so we can easily extract the information we
 * need to generate the export file.
 *
 * Todo: Rewrite exporter to always use textures and to use fewer triangles (than for each voxel side two)
 */
public class ExportWorld {

    // list of all voxels
    private final ArrayList<VoxelSide> voxels = new ArrayList<VoxelSide>();

    // voxel index to determine which sides are visible
    private final HashMap<String, VoxelSide> index = new HashMap<String, VoxelSide>();

    // helper class that represents a voxel
    private final class VoxelSide {
        private final int[] pos;
        private final boolean[] visSides = new boolean[]{true, true, true, true, true, true};

        private void hideSide(int side) {
            visSides[side] = false;
        }

        // check if side is visible
        public final boolean sideVisible(int side) {
            return visSides[side];
        }

        // get position for axis
        public final int posForAxis(int axis) {
            return pos[axis];
        }

        // constructor
        public VoxelSide(Voxel voxel) {
            this.pos = voxel.getPosAsInt();
            // update all the sides
            for (int i = 0; i < 6; i++) {
                int add = i%2 == 0 ? 1 : -1;
                VoxelSide found = index.get(
                        (i/2 == 0 ? voxel.x + add : voxel.x) + "_" +
                                (i/2 == 1 ? voxel.y + add : voxel.y) + "_" +
                                (i/2 == 2 ? voxel.z + add : voxel.z)
                );
                if (found != null) {
                    hideSide(i);
                    found.hideSide(i%2 == 0 ? i + 1 : i - 1);
                }
            }

            index.put(voxel.getPosAsString(), this);
        }
    }

    // constructor
    public ExportWorld(Voxel[] input) {
        for (Voxel voxel : input) {
            voxels.add(new VoxelSide(voxel));
        }
    }

    // to reference the different algorithms
    public static final int ALGORITHM_POLY2TRI = 0;
    public static final int ALGORITHM_GREEDY = 1;
    public static final int ALGORITHM_MONO = 2;
    public static final int ALGORITHM_MONO_SAVE = 3;
    public static final int ALGORITHM_GREEDY_OPTIMAL = 4;

    // build the sides and returns the total and
    // the (minimal possible) reduced number of triangles
    public int[] analyzeTriCount(int algorithmid) {
        int triCount = 0;
        int triCountRaw = 0;
        long time = 0;
        // for all sides
        for (int i = 0; i < 6; i++) {
            // holds the sides per "slice"
            HashMap<Integer, ArrayList<Point>> polygons = new HashMap<Integer, ArrayList<Point>>();
            // min,max for other axix
            Integer min2 = null;
            Integer min3 = null;
            Integer max2 = null;
            Integer max3 = null;
            // for all voxels
            for (VoxelSide voxel : voxels) {
                if (voxel.sideVisible(i)) {
                    triCountRaw += 2;
                    int depthAxis = i/2;
                    ArrayList<Point> pixels = polygons.get(voxel.posForAxis(depthAxis));
                    if (pixels == null) {
                        pixels = new ArrayList<Point>();
                        polygons.put(voxel.posForAxis(depthAxis), pixels);
                    }
                    // add the other two axis as point
                    Point pixel = new Point(voxel.posForAxis((depthAxis + 1)% 3), voxel.posForAxis((depthAxis + 2)% 3));
                    min2 = Math.min(min2 == null ? pixel.x : min2, pixel.x);
                    min3 = Math.min(min3 == null ? pixel.y : min3, pixel.y);
                    max2 = Math.max(max2 == null ? pixel.x : max2, pixel.x);
                    max3 = Math.max(max3 == null ? pixel.y : max3, pixel.y);
                    pixels.add(pixel);
                }
            }
            if (min2 != null) {
                // analyze the polygons
                for (Map.Entry<Integer, ArrayList<Point>> entry : polygons.entrySet()) {
                    int w = max2 - min2 + 1;
                    int h = max3 - min3 + 1;

                    switch (algorithmid) {
                        case 0: // poly2tri (will produce no t-junction problems)
                            boolean[][] dataPoly2Tri = new boolean[w][h];
                            for (Point point : entry.getValue()) {
                                dataPoly2Tri[point.x - min2][point.y - min3] = true;
                            }
                            time -= System.currentTimeMillis();
                            triCount += Grid2TriPolyFast.triangulate(Grid2PolyHelper.convert(dataPoly2Tri)).size();
                            time += System.currentTimeMillis();
                            break;
                        case 1: // greedy (will produce many t-junction problems)
                            boolean[][] dataGreedy = new boolean[w][h];
                            for (Point point : entry.getValue()) {
                                dataGreedy[point.x - min2][point.y - min3] = true;
                            }
                            time -= System.currentTimeMillis();
                            triCount += Grid2TriNaiveGreedy.triangulate(dataGreedy).size();
                            time += System.currentTimeMillis();
                            break;
                        case 2: // mono  (will produce some t-junction problems)
                            boolean[][] dataMono = new boolean[w][h];
                            for (Point point : entry.getValue()) {
                                dataMono[point.x - min2][point.y - min3] = true;
                            }
                            time -= System.currentTimeMillis();
                            triCount += Grid2TriMono.triangulate(dataMono, false).size();
                            time += System.currentTimeMillis();
                            break;
                        case 3: // altered mono (will only produce t-junction problems in 3D)
                            boolean[][] dataMonoSave = new boolean[w][h];
                            for (Point point : entry.getValue()) {
                                dataMonoSave[point.x - min2][point.y - min3] = true;
                            }
                            time -= System.currentTimeMillis();
                            triCount += Grid2TriMono.triangulate(dataMonoSave, true).size();
                            time += System.currentTimeMillis();
                            break;
                        default: // optimal greedy (will produce many t-junction problems)
                            boolean[][] dataGreedyOpt = new boolean[w][h];
                            for (Point point : entry.getValue()) {
                                dataGreedyOpt[point.x - min2][point.y - min3] = true;
                            }
                            time -= System.currentTimeMillis();
                            triCount += Grid2TriGreedyOptimal.triangulate(dataGreedyOpt).size();
                            time += System.currentTimeMillis();
                            break;
                    }

                }
            }
        }
        return new int[] {triCount, triCountRaw, (int) (time)};
    }
}
