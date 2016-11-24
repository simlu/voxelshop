package com.vitco.app.low.hull;

import com.threed.jpct.SimpleVector;
import com.vitco.app.low.CubeIndexer;
import gnu.trove.iterator.TIntIterator;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.hash.TIntShortHashMap;
import gnu.trove.set.hash.TIntHashSet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

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
    // holds the computed interior
    private final TIntHashSet[] interior = new TIntHashSet[] {
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

    // helper class that wraps a (continuous set of sides)
    private static final class HullWrapper {
        // holds the data
        private TIntHashSet[] data = new TIntHashSet[] {
                new TIntHashSet(), new TIntHashSet(), new TIntHashSet(),
                new TIntHashSet(), new TIntHashSet(), new TIntHashSet()
        };

        // constructor
        public HullWrapper(TIntHashSet[] data) {
            for (int i = 0; i < 6; i++) {
                this.data[i].addAll(data[i]);
            }
        }

        // returns true if containment is detected
        public boolean contains(HullWrapper other) {
            // -- check if the other HullWrapper is contained in this hull wrapper
            // fetch a first side into X direction
            short[] side = CubeIndexer.getPos(other.data[0].iterator().next());
            // search for all sides at the found YZ position in the potential "outer" HullWrapper
            ArrayList<Short> list = new ArrayList<Short>();
            for (TIntIterator it = this.data[0].iterator(); it.hasNext();) {
                short[] val = CubeIndexer.getPos(it.next());
                if (val[1] == side[1] && val[2] == side[2]) {
                    list.add(val[0]);
                }
            }
            for (TIntIterator it = this.data[1].iterator(); it.hasNext();) {
                short[] val = CubeIndexer.getPos(it.next());
                if (val[1] == side[1] && val[2] == side[2]) {
                    list.add(val[0]);
                }
            }
            // sort the found sides
            Collections.sort(list);
            // check if the initially selected side lives inside the extracted sides
            boolean inside = false;
            short lastDepth = 0;
            boolean foundInside = false;
            for (short depth : list) {
                inside = !inside;
                if (!inside) {
                    if (side[0] < depth && side[0] > lastDepth) {
                        foundInside = true;
                        break;
                    }
                }
                lastDepth = depth;
            }
            return foundInside;
        }
    }

    // compute the "outside" of the described object
    @Override
    public boolean computeExterior() {
        // holds known exterior sides
        ArrayList<HullWrapper> exterior = new ArrayList<HullWrapper>();
        // holds known interior sides
        ArrayList<HullWrapper> interior = new ArrayList<HullWrapper>();
        // holds the processed sides
        TIntHashSet[] processed = new TIntHashSet[]{
                new TIntHashSet(), new TIntHashSet(), new TIntHashSet(),
                new TIntHashSet(), new TIntHashSet(), new TIntHashSet()
        };
        // true if a hole was found
        boolean interiorFound = false;
        // loop over all potential starting positions
        // (one direction is enough for this!)
        for (int pos : getHullAsIds(0)) {
            // check if this side was already processed with another starting position
            if (!processed[0].contains(pos)) {
                // -- fetch the contour that this border belongs to
                TIntHashSet[] detected = detectContour(pos, (short) 0, processed);

                // -- analyse whether it's outside or inside facing hull
                int minA = Integer.MAX_VALUE;
                for (TIntIterator it = detected[0].iterator(); it.hasNext(); ) {
                    minA = Math.min(minA, CubeIndexer.getPos(it.next())[0]);
                }
                int minB = Integer.MAX_VALUE;
                for (TIntIterator it = detected[1].iterator(); it.hasNext(); ) {
                    minB = Math.min(minB, CubeIndexer.getPos(it.next())[0]);
                }
                boolean isInsideHull = minA < minB;

                // -- add to according lists
                if (isInsideHull) {
                    interiorFound = true;
                    // add to interior list
                    interior.add(new HullWrapper(detected));
                } else {
                    // add to exterior list
                    exterior.add(new HullWrapper(detected));
                }
            }
        }
        // check if we need to migrate any exterior to interior
        // (this is the case if the outside facing hull lives
        // inside an inside facing hull)
        int i = 0;
        while (i < exterior.size()) {
            HullWrapper exteriorWrapper = exterior.get(i);
            boolean found = false;
            for (HullWrapper interiorWrapper : interior) {
                if (interiorWrapper.contains(exteriorWrapper)) {
                    found = true;
                    break;
                }
            }
            if (found) {
                interior.add(exterior.remove(i));
            } else {
                i++;
            }
        }
        // update global exterior and interior
        for (int k = 0; k < 6; k++) {
            this.exterior[k].clear();
            for (HullWrapper wrapper : exterior) {
                this.exterior[k].addAll(wrapper.data[k]);
            }
            this.interior[k].clear();
            for (HullWrapper wrapper : interior) {
                this.interior[k].addAll(wrapper.data[k]);
            }
        }
        // return result
        return interiorFound;
    }

    // fetch the "outside" faces of the described object
    // into a specific direction.
    // Required computeExterior() to be called before working
    @Override
    public short[][] getExteriorHull(int direction) {
        short[][] result = new short[exterior[direction].size()][3]; // allocate with correct size
        int count = 0;
        for (TIntIterator it = exterior[direction].iterator(); it.hasNext(); ) {
            short[] val = CubeIndexer.getPos(it.next());
            result[count][0] = val[0];
            result[count][1] = val[1];
            result[count][2] = val[2];
            count++;
        }
        return result;
    }

    // fetch the "inside" faces of the described object
    // into a specific direction.
    // Required computeExterior() to be called before working
    @Override
    public short[][] getInteriorHull(int direction) {
        short[][] result = new short[interior[direction].size()][3]; // allocate with correct size
        int count = 0;
        for (TIntIterator it = interior[direction].iterator(); it.hasNext(); ) {
            short[] val = CubeIndexer.getPos(it.next());
            result[count][0] = val[0];
            result[count][1] = val[1];
            result[count][2] = val[2];
            count++;
        }
        return result;
    }

    // comparator - order by depth
    private static final Comparator<short[]> comparator = new Comparator<short[]>() {
        @Override
        public int compare(short[] o1, short[] o2) {
            return o1[0] - o2[0];
        }
    };

    // get the empty positions of voxels inside
    // Required computeExterior() to be called before working
    @Override
    public int[] getEmptyInterior() {
        // result
        TIntArrayList list = new TIntArrayList();
        // -- fetch the interior faces into two opposite directions
        short[][] hullA = getInteriorHull(0);
        short[][] hullB = getInteriorHull(1);
        if (hullA.length > 0 && hullB.length > 0) {
            // -- order by depth
            Arrays.sort(hullA, comparator);
            Arrays.sort(hullB, comparator);
            // find places to fill
            int iA = 0;
            int iB = 0;
            TIntShortHashMap buffer = new TIntShortHashMap();
            while (iB < hullB.length || iA < hullA.length) {
                if (iA < hullA.length && (!(iB < hullB.length) || hullA[iA][0] < hullB[iB][0])) {
                    // front face - add the starting position
                    buffer.put(CubeIndexer.getId(0, hullA[iA][1], hullA[iA][2]), hullA[iA][0]);
                    iA++;
                } else {
                    // back face - add missing until finish positions
                    short val = buffer.remove(CubeIndexer.getId(0, hullB[iB][1], hullB[iB][2]));
                    for (short pos = ++val; pos < hullB[iB][0]; pos++) {
                        list.add(CubeIndexer.getId(pos, hullB[iB][1], hullB[iB][2]));
                    }
                    iB++;
                }
            }
        }
        // empty result
        return list.toArray();
    }

    // get the voxel positions of voxels inside
    // Required computeExterior() to be called before working
    @Override
    public int[] getFilledInterior() {
        TIntArrayList result = new TIntArrayList();
        // loop over all objects
        for (int posId : getPosIds()) {
            // exclude positions that have an exterior face attached
            if (!exterior[0].contains(posId) && !exterior[1].contains(posId) &&
                    !exterior[2].contains(posId) && !exterior[3].contains(posId) &&
                    !exterior[4].contains(posId) && !exterior[5].contains(posId)) {
                result.add(posId);
            }
        }
        // return result
        return result.toArray();
    }
}
