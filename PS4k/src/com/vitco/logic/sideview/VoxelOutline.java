package com.vitco.logic.sideview;

import com.newbrightidea.util.RTree;
import com.threed.jpct.SimpleVector;
import com.vitco.res.VitcoSettings;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * This manages the outline for voxel positions and returns the lines that need to be
 * drawn for the outline.
 */
public class VoxelOutline {
    // the neglected side
    private int[][] list = new int[0][];

    // wrapper
    private static final class Wrapper {
        public final float[] pos;
        public final boolean[] edges;

        private Wrapper(float[] pos, boolean[] edges) {
            this.pos = pos;
            this.edges = edges;
        }
    }

    // zero value
    private static final float[] ZEROS = new float[] {0f, 0f, 0f};

    // holds all the positions
    private final RTree<boolean[]> voxels = new RTree<boolean[]>(50, 2, 3);

    // remember the edges and positions
    private final HashMap<String, Wrapper> outline = new HashMap<String, Wrapper>();

    // true iff data has changed and buffer invalid
    private boolean bufferValid = false;
    private SimpleVector[][] buffer = new SimpleVector[0][];

    // constructor
    public VoxelOutline(int side) {
        // find the sides to check
        switch (side) {
            case 0:
                list = new int[][] {
                        new int[] {1, 0, 0},
                        new int[] {-1, 0, 0},
                        new int[] {0, 1, 0},
                        new int[] {0, -1, 0}
                };
                break;
            case 1:
                list = new int[][] {
                        new int[] {1, 0, 0},
                        new int[] {-1, 0, 0},
                        new int[] {0, 0, 1},
                        new int[] {0, 0, -1}
                };
                break;
            case 2:
                list = new int[][] {
                        new int[] {0, 1, 0},
                        new int[] {0, -1, 0},
                        new int[] {0, 0, 1},
                        new int[] {0, 0, -1}
                };
                break;
        }
    }

    // internal - update a position and set the correct edges
    private void updatePos(float[] pos, boolean[] center) {
        // update the sides
        for (int i = 0; i < list.length; i++) {
            int[] entry = list[i];
            int a1 = i < 2 ? 0 : 2;
            int add = a1 + i%2;
            int sub = a1 + (i+1)%2;
            List<boolean[]> search =
                    voxels.search(new float[]{pos[0] + entry[0], pos[1] + entry[1], pos[2] + entry[2]}, ZEROS);
            if (search.size() > 0) {
                if (center != null) {
                    center[add] = false;
                    search.get(0)[sub] = false;
                } else {
                    search.get(0)[sub] = true;
                }
            } else {
                if (center != null) {
                    center[add] = true;
                }
            }
        }
    }

    // retrieve the lines needed to draw the outline
    public final SimpleVector[][] getLines() {
        if (!bufferValid) {
            ArrayList<SimpleVector[]> arraybuffer = new ArrayList<SimpleVector[]>();
            for (Wrapper wrapper : outline.values()) {
                boolean[] edges = wrapper.edges;
                for (int i = 0; i < edges.length; i++) {
                    if (edges[i]) {
                        int a1 = i > 1 ? 0 : 2;
                        SimpleVector p1 = new SimpleVector(
                                wrapper.pos[0] + list[i][0]/2f + list[a1][0]/2f,
                                wrapper.pos[1] + list[i][1]/2f + list[a1][1]/2f,
                                wrapper.pos[2] + list[i][2]/2f + list[a1][2]/2f
                        );
                        p1.scalarMul(VitcoSettings.VOXEL_SIZE);
                        SimpleVector p2 = new SimpleVector(
                                wrapper.pos[0] + list[i][0]/2f + list[a1 + 1][0]/2f,
                                wrapper.pos[1] + list[i][1]/2f + list[a1 + 1][1]/2f,
                                wrapper.pos[2] + list[i][2]/2f + list[a1 + 1][2]/2f);
                        p2.scalarMul(VitcoSettings.VOXEL_SIZE);

                        arraybuffer.add(new SimpleVector[]{p1, p2});
                    }
                }
            }
            buffer = new SimpleVector[arraybuffer.size()][];
            arraybuffer.toArray(buffer);
            bufferValid = true;
        }
        return buffer.clone();
    }

    // add a position
    public final void addPosition(float[] pos) {
        if (voxels.search(pos, ZEROS).size() == 0) {
            boolean[] edges = new boolean[] {true, true, true, true};
            voxels.insert(pos, ZEROS, edges);
            outline.put(posToString(pos), new Wrapper(pos, edges));
            updatePos(pos, edges);
            bufferValid = false;
        }
    }

    // helper to convert position to string
    private String posToString(float[] pos) {
        return pos[0] + "_" + pos[1] + "_" + pos[2];
    }

    // remove a position
    public final void removePosition(float[] pos) {
        java.util.List<boolean[]> search = voxels.search(pos, ZEROS);
        if (search.size() > 0) {
            voxels.delete(pos, ZEROS, search.get(0));
            outline.remove(posToString(pos));
            updatePos(pos, null);
            bufferValid = false;
        }
    }

    // reset everything
    public final void clear() {
        outline.clear();
        voxels.clear();
        bufferValid = false;
    }

}
