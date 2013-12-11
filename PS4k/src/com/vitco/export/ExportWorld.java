package com.vitco.export;

import com.vitco.engine.data.container.Voxel;
import com.vitco.util.SharedImageFactory;
import com.vitco.util.triangulate.Grid2Tri;
import org.jaitools.imageutils.ImageUtils;

import javax.media.jai.TiledImage;
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

    // build the sides and returns the total and
    // the (minimal possible) reduced number of triangles
    public int[] analyzeTriCount() {
        int triCount = 0;
        int triCountRaw = 0;
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
                    boolean isAllocated = SharedImageFactory.isTiledImageAllocated(w, h);
                    TiledImage src;
                    if (isAllocated) {
                        src = SharedImageFactory.getTiledImage(w, h);
                    } else {
                        src = ImageUtils.createConstantImage(w, h, 0);
                    }
                    for (Point point : entry.getValue()) {
                        src.setSample(point.x - min2, point.y - min3, 0, 1);
                    }
                    triCount += Grid2Tri.triangulate(Grid2Tri.doVectorize(src)).size();
                    // cleanup
                    if (isAllocated) {
                        for (Point point : entry.getValue()) {
                            src.setSample(point.x - min2, point.y - min3, 0, 0);
                        }
                    }

                }
            }
        }
        return new int[] {triCount, triCountRaw};
    }
}
