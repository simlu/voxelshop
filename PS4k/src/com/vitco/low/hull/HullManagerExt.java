package com.vitco.low.hull;

import com.threed.jpct.SimpleVector;
import com.vitco.low.CubeIndexer;
import gnu.trove.iterator.TIntIterator;
import gnu.trove.set.hash.TIntHashSet;

import java.util.ArrayList;

/**
 * Extended functionality for the hull manager
 */
public class HullManagerExt<T> extends HullManager<T> implements HullManagerExtInterface {

    // =====================
    // -- ray hit test
    // =====================

    // do a hit test against the voxels in this hull manager
    @Override
    public short[] hitTest(SimpleVector origin, SimpleVector dir) {

        // If the origin is outside the max box of the CubeIndexer it needs to be
        // shifted into the cube before we can proceed
        // Note: Not necessary atm (since the camera is usually inside the max box)
//        origin = CubeIndexer.validateRay(origin, dir);
//        if (origin == null) {
//            return null;
//        }

        // ---------------

        // step direction
        short stepX = (short) Math.signum(dir.x);
        short stepY = (short) Math.signum(dir.y);
        short stepZ = (short) Math.signum(dir.z);

        boolean stepXB = stepX > 0;
        boolean stepYB = stepY > 0;
        boolean stepZB = stepZ > 0;

        short sideX = (short) (stepXB ? 1 : 0);
        short sideY = (short) (stepYB ? 3 : 2);
        short sideZ = (short) (stepZB ? 5 : 4);

        // starting grid coordinates
        short lastHitSide;
        int pos = CubeIndexer.getId(
                (short) Math.floor(origin.x),
                (short) Math.floor(origin.y),
                (short) Math.floor(origin.z)
        );

        // compute the offsets
        double offX = stepX == Math.signum(origin.x) ? (1 - Math.abs(origin.x%1d)) : Math.abs(origin.x%1d);
        double offY = stepY == Math.signum(origin.y) ? (1 - Math.abs(origin.y%1d)) : Math.abs(origin.y%1d);
        double offZ = stepZ == Math.signum(origin.z) ? (1 - Math.abs(origin.z%1d)) : Math.abs(origin.z%1d);
        offX = (double)Math.round(offX * 1000000000) / 1000000000;
        offY = (double)Math.round(offY * 1000000000) / 1000000000;
        offZ = (double)Math.round(offZ * 1000000000) / 1000000000;
        if (offX == 0) {
            offX = 1;
        }
        if (offY == 0) {
            offY = 1;
        }
        if (offZ == 0) {
            offZ = 1;
        }

        // the "progress" value
        double valYX = Math.abs(dir.y / dir.x);
        double valZX = Math.abs(dir.z / dir.x);
        double valZY = Math.abs(dir.z / dir.y);

        int tMaxX = 0;
        int tMaxY = 0;
        int tMaxZ = 0;

        // only check for nearby voxels (ray length)
        int i = 0;
        while (i++ < 400) {

            double diffYX = valYX * (tMaxX + offX) - (tMaxY + offY);

            if (diffYX < 0) {
                double diffZX = valZX * (tMaxX + offX) - (tMaxZ + offZ);
                if (diffZX < 0) {
                    tMaxX++;
                    pos = CubeIndexer.changeX(pos, stepXB);
                    lastHitSide = sideX;
                } else {
                    tMaxZ++;
                    pos = CubeIndexer.changeZ(pos, stepZB);
                    lastHitSide = sideZ;
                }
            } else {
                double diffZY = valZY * (tMaxY + offY) - (tMaxZ + offZ);
                if (diffZY < 0) {
                    tMaxY++;
                    pos = CubeIndexer.changeY(pos, stepYB);
                    lastHitSide = sideY;
                } else {
                    tMaxZ++;
                    pos = CubeIndexer.changeZ(pos, stepZB);
                    lastHitSide = sideZ;
                }
            }

            // check for containment
            if (containsBorder(pos, lastHitSide)) { // hit side has to be visible
                //if (id2obj.containsKey(pos)) { // any voxel can be hit
                short[] result = CubeIndexer.getPos(pos);
                return new short[] {result[0], result[1], result[2], lastHitSide};
            }
        }
        return null;
    }

    // constructor
    // ==============================
    // -- exterior computation
    // ==============================

    // holds the computed exterior
    private final TIntHashSet[] exterior = new TIntHashSet[] {
            new TIntHashSet(), new TIntHashSet(), new TIntHashSet(),
            new TIntHashSet(), new TIntHashSet(), new TIntHashSet()
    };

    // helper, return true if given border is present in hull
    // if true -> add border to stack if not already present in processed
    private boolean detectStepAdd(ArrayList<int[]> stack, int pos, int orientation, TIntHashSet[] processed) {
        boolean result = false;
        // (1) check if extension exists as border,
        if (containsBorder(pos, orientation)) {
            // (2) check if extension already processed,
            if (!processed[orientation].contains(pos)) {
                processed[orientation].add(pos);
                // (3) add to stack
                stack.add(new int[] {pos, orientation});
            }
            result = true;
        }
        return result;
    }

    // check which borders are correct neighbouring borders for a given border
    // then add to stack if not already processed
    // Note: This uses a "fold down model" for checking the neighbouring borders
    private void detectStep(ArrayList<int[]> stack, int pos, short orientation, TIntHashSet[] processed) {
        int[][] axisToCheck;
        switch (orientation) {
            case 0:case 1:
                axisToCheck = new int[][] {
                        new int[] {2,3},
                        new int[] {4,5}
                };
                break;
            case 2:case 3:
                axisToCheck = new int[][] {
                        new int[] {0,1},
                        new int[] {4,5}
                };
                break;
            default:
                axisToCheck = new int[][] {
                        new int[] {0,1},
                        new int[] {2,3}
                };
                break;
        }
        // --
        for (int[] axis : axisToCheck) {
            // check negative
            int posN = CubeIndexer.change(pos, axis[0]);
            int posNOff = CubeIndexer.change(posN, orientation);
            boolean detectedN = detectStepAdd(stack, posNOff, axis[1], processed) ||
                    detectStepAdd(stack, posN, orientation, processed) ||
                    detectStepAdd(stack, pos, axis[0], processed);
            // check positive
            int posP = CubeIndexer.change(pos, axis[1]);
            int posPOff = CubeIndexer.change(posP, orientation);
            boolean detectedP = detectStepAdd(stack, posPOff, axis[0], processed) ||
                    detectStepAdd(stack, posP, orientation, processed) ||
                    detectStepAdd(stack, pos, axis[1], processed);
            // at least one neighbouring side has to be detected into each direction
            // otherwise this voxel face would be bugged (i.e. have a "see through" face
            // as a neighbour)
            assert detectedN && detectedP;
        }
    }

    // follow an outline for a given starting border
    // store found borders in processed
    // returns the detected outline
    private TIntHashSet[] detectContour(int pos, short orientation, TIntHashSet[] processed) {
        TIntHashSet[] result = new TIntHashSet[] {
                new TIntHashSet(), new TIntHashSet(), new TIntHashSet(),
                new TIntHashSet(), new TIntHashSet(), new TIntHashSet()
        };
        // stack of currently processing voxel sides
        ArrayList<int[]> stack = new ArrayList<int[]>();
        detectStepAdd(stack, pos, orientation, processed);
        // follow all path
        while (!stack.isEmpty()) {
            int[] cur = stack.remove(0);
            // add to result
            result[cur[1]].add(cur[0]);
            // check all extensions and add to stack
            detectStep(stack, cur[0], (short) cur[1], processed);
        }
        // return the result
        return result;
    }

    // compute the "outside" of the described object
    @Override
    public boolean computeExterior() {
        // holds known exterior sides
        TIntHashSet[] exterior = new TIntHashSet[] {
                new TIntHashSet(), new TIntHashSet(), new TIntHashSet(),
                new TIntHashSet(), new TIntHashSet(), new TIntHashSet()
        };
        // holds the processed sides
        TIntHashSet[] processed = new TIntHashSet[] {
                new TIntHashSet(), new TIntHashSet(), new TIntHashSet(),
                new TIntHashSet(), new TIntHashSet(), new TIntHashSet()
        };
        // true if a hole was found
        boolean interiorFound = false;
        // loop over all orientations
        for (short i = 0; i < 6; i++) {
            short[][] hull = getHull(i);
//            // order by depth
//            final int k = i/2;
//            final boolean val = i%2 != 0;
//            Arrays.sort(hull, new Comparator<short[]>() {
//                @Override
//                public int compare(short[] o1, short[] o2) {
//                    return val ? o1[k] - o2[k] : o2[k] - o1[k];
//                }
//            });

            // loop over all sides
            for (short[] aHull : hull) {
                // check if this side was already processed
                int pos = CubeIndexer.getId(aHull[0], aHull[1], aHull[2]);
                if (!processed[i].contains(pos)) {
                    // -- fetch the contour that this border belongs to
                    TIntHashSet[] detected = detectContour(pos, i, processed);

                    // -- analyse whether it's exterior or interior
                    int minA = Integer.MAX_VALUE;
                    for (TIntIterator it = detected[0].iterator(); it.hasNext(); ) {
                        minA = Math.min(minA, CubeIndexer.getPos(it.next())[0]);
                    }
                    int minB = Integer.MAX_VALUE;
                    for (TIntIterator it = detected[1].iterator(); it.hasNext(); ) {
                        minB = Math.min(minB, CubeIndexer.getPos(it.next())[0]);
                    }
                    boolean interior = minA < minB;

                    // -- do action accordingly
                    if (interior) {
                        interiorFound = true;
                    } else {
                        // add to exterior list
                        for (int t = 0; t < 6; t++) {
                            exterior[t].addAll(detected[t]);
                        }
                    }
                }
            }
        }
        // update global exterior
        for (int i = 0; i < 6; i++) {
            this.exterior[i].clear();
            this.exterior[i].addAll(exterior[i]);
        }
        // return result
        return interiorFound;
    }

    // fetch the "outside" of the described object
    // into a specific direction.
    // Required computeExterior() to be called before working
    @Override
    public short[][] getExteriorHull(int direction) {
        short[][] result = new short[exterior[direction].size()][3]; // allocate with correct size
        int count = 0;
        for (TIntIterator it = exterior[direction].iterator(); it.hasNext();) {
            short[] val = CubeIndexer.getPos(it.next());
            result[count][0] = val[0];
            result[count][1] = val[1];
            result[count][2] = val[2];
            count++;
        }
        return result;
    }

}
