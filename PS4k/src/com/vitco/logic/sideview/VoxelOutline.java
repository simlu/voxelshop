package com.vitco.logic.sideview;

import com.threed.jpct.SimpleVector;
import com.vitco.engine.data.container.Voxel;
import com.vitco.res.VitcoSettings;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * This manages the outline for voxel positions and returns the lines that need to be
 * drawn for the outline.
 */
public class VoxelOutline {
    // the neglected side
    private int[][] list = new int[0][];

    // wrapper
    private static final class Wrapper {
        public final Voxel voxel;
        public final boolean[] edges;

        private Wrapper(Voxel voxel, boolean[] edges) {
            this.voxel = voxel;
            this.edges = edges;
        }
    }

    // holds all the positions
    private final HashMap<String, boolean[]> voxelIndex = new HashMap<String, boolean[]>();

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
            default: break;
        }
    }

    // internal - update a position and set the correct edges
    private void updatePos(Voxel voxel, boolean[] center) {
        // update the sides
        for (int i = 0; i < list.length; i++) {
            int[] entry = list[i];
            int a1 = i < 2 ? 0 : 2;
            int add = a1 + i%2;
            int sub = a1 + (i+1)%2;
            String index = (voxel.x + entry[0]) + "_" + (voxel.y + entry[1]) + "_" + (voxel.z + entry[2]);
            boolean[] sides = voxelIndex.get(index);
            if (sides != null) {
                if (center != null) {
                    center[add] = false;
                    sides[sub] = false;
                } else {
                    sides[sub] = true;
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
                                wrapper.voxel.x + list[i][0]/2f + list[a1][0]/2f,
                                wrapper.voxel.y + list[i][1]/2f + list[a1][1]/2f,
                                wrapper.voxel.z + list[i][2]/2f + list[a1][2]/2f
                        );
                        p1.scalarMul(VitcoSettings.VOXEL_SIZE);
                        SimpleVector p2 = new SimpleVector(
                                wrapper.voxel.x + list[i][0]/2f + list[a1 + 1][0]/2f,
                                wrapper.voxel.y + list[i][1]/2f + list[a1 + 1][1]/2f,
                                wrapper.voxel.z + list[i][2]/2f + list[a1 + 1][2]/2f);
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
    public final void addPosition(Voxel voxel) {
        String index = voxel.getPosAsString();
        if (!voxelIndex.containsKey(index)) {
            boolean[] edges = new boolean[] {true, true, true, true};
            voxelIndex.put(index, edges);
            outline.put(index, new Wrapper(voxel, edges));
            updatePos(voxel, edges);
            bufferValid = false;
        }
    }

    // remove a position
    public final void removePosition(Voxel voxel) {
        String index = voxel.getPosAsString();
        if (voxelIndex.remove(index) != null) {
            outline.remove(index);
            updatePos(voxel, null);
            bufferValid = false;
        }
    }

    // reset everything
    public final void clear() {
        outline.clear();
        voxelIndex.clear();
        bufferValid = false;
    }

}
