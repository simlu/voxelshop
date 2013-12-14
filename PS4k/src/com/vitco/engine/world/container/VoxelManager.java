package com.vitco.engine.world.container;

import com.vitco.engine.data.container.Voxel;
import com.vitco.res.VitcoSettings;
import com.vitco.util.IntegerTools;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Manages face data structure.
 *
 * - Adding, removing, fetching of faces.
 * - invalidation, querying of invalid areas
 *
 * Note: Areas are automatically invalidated when the corresponding faces are changed
 */
public class VoxelManager {

    // constructor
    public VoxelManager() {
        // initialize data structure (all six orientations)
        for (int i = 0; i < 6; i++) {
            orientationList.add(new HashMap<Integer, HashMap<Point, HashMap<String, Voxel>>>());
            changedAreas.add(new HashMap<Integer, HashSet<Point>>());
        }
    }

    // helper to convert 2D position to area id
    public static Point getAreaId(int[] pos) {
        // this uses efficient integer division that rounds down
        return new Point(
                IntegerTools.ifloordiv2(pos[0] + VitcoSettings.TRI_GRID_OFFSET, VitcoSettings.TRI_GRID_SIZE),
                IntegerTools.ifloordiv2(pos[1] + VitcoSettings.TRI_GRID_OFFSET, VitcoSettings.TRI_GRID_SIZE));
    }

    // static convert 3D to 2D
    public static int[] convert(Voxel voxel, int axis) {
        int[] result;
        switch (axis) {
            case 0: result = new int[] {voxel.y, voxel.z}; break;
            case 1: result = new int[] {voxel.x, voxel.z}; break;
            default: result = new int[] {voxel.x, voxel.y}; break;
        }
        return result;
    }

    // get the plane this voxel is in for the axis
    private static int getPlane(Voxel voxel, int axis) {
        switch (axis) {
            case 0: return voxel.x;
            case 1: return voxel.y;
            default: return voxel.z;
        }
    }

    // clear this data structure
    public void clear() {
        for (int i = 0; i < 6; i++) {
            orientationList.get(i).clear();
            changedAreas.get(i).clear();
        }
    }

    // -----------------------
    // area invalidation

    // keeps track of the areas that need updating
    private final ArrayList<HashMap<Integer, HashSet<Point>>> changedAreas = new ArrayList<HashMap<Integer, HashSet<Point>>>();

    // mark an area as changed
    public final void invalidate(Integer orientation, Integer plane, Point areaId) {
        // get the correct plane list for the orientation
        HashMap<Integer, HashSet<Point>> planeList = changedAreas.get(orientation);
        // get the correct area list for this plane
        HashSet<Point> areaList = planeList.get(plane);
        if (areaList == null) {
            areaList = new HashSet<Point>();
            planeList.put(plane, areaList);
        }
        areaList.add(areaId);
    }

    // check if an area is marked as changed
    public final boolean isInvalid(Integer orientation, Integer plane, Point areaId) {
        // get the correct plane list for the orientation
        HashMap<Integer, HashSet<Point>> planeList = changedAreas.get(orientation);
        // get the correct area list for this plane
        HashSet<Point> areaList = planeList.get(plane);
        // check if area is contained
        return areaList != null && areaList.contains(areaId);
    }

    // get all the invalid areas for a plane
    public final void clearInvalidAreas(Integer orientation) {
        changedAreas.get(orientation).clear();
    }

    // get all the invalid planes for an orientation
    // Note: You can remove areas manually, just make sure empty entries
    // are deleted in both hashmaps
    // Note: An alternative is to call clearInvalidAreas(orientation)
    public final HashMap<Integer, HashSet<Point>> getInvalidPlanes(Integer orientation) {
        return changedAreas.get(orientation);
    }

    // -----------------------
    // face handling

    // holds all the face information
    // Structure is as following
    // orientation -> plane -> area -> face
    private final ArrayList<HashMap<Integer, HashMap<Point, HashMap<String, Voxel>>>> orientationList =
            new ArrayList<HashMap<Integer, HashMap<Point, HashMap<String, Voxel>>>>();

    // add a face to this data structure
    // (returns true if no overwrite of existing face occurred)
    public final boolean addFace(Integer orientation, Voxel voxel) {
        // determine the area id
        int axis = orientation/2;
        int[] pos2D = convert(voxel, axis);
        int plane = getPlane(voxel, axis);
        Point areaId = getAreaId(pos2D);
        // invalidate
        invalidate(orientation, plane, areaId);
        // get the correct plane list for the orientation
        HashMap<Integer, HashMap<Point, HashMap<String, Voxel>>> planeList = orientationList.get(orientation);
        // get the correct area list for this plane
        HashMap<Point, HashMap<String, Voxel>> areaList = planeList.get(plane);
        // will contain the correct face list that we need to add the face to
        HashMap<String, Voxel> faceList;
        if (areaList == null) {
            // create area and face list if the area list does not exist yet
            areaList = new HashMap<Point, HashMap<String, Voxel>>();
            planeList.put(plane, areaList);
            faceList = new HashMap<String, Voxel>();
            areaList.put(areaId, faceList);
        } else {
            faceList = areaList.get(areaId);
            if (faceList == null) {
                // create face list
                faceList = new HashMap<String, Voxel>();
                areaList.put(areaId, faceList);
            }
        }
        // add the face to this data structure
        return faceList.put(pos2D[0] + "_" + pos2D[1], voxel) == null;
    }

    // remove a face from this data structure (return true if successfully removed)
    public final boolean removeFace(Integer orientation, Voxel voxel) {
        // determine the area id
        int axis = orientation/2;
        int[] pos2D = convert(voxel, axis);
        int plane = getPlane(voxel, axis);
        Point areaId = getAreaId(pos2D);
        // invalidate
        invalidate(orientation, plane, areaId);
        // get the correct plane list for the orientation
        HashMap<Integer, HashMap<Point, HashMap<String, Voxel>>> planeList = orientationList.get(orientation);
        // get the correct area list for this plane
        HashMap<Point, HashMap<String, Voxel>> areaList = planeList.get(plane);
        if (areaList != null) {
            // get the face list that we expect our face to be in
            HashMap<String, Voxel> faceList = areaList.get(areaId);
            if (faceList != null) {
                if (faceList.remove(pos2D[0] + "_" + pos2D[1]) != null) {
                    // cleanup the empty lists
                    if (faceList.isEmpty()) {
                        areaList.remove(areaId);
                        if (areaList.isEmpty()) {
                            planeList.remove(plane);
                        }
                    }
                    // remove was successful
                    return true;
                }
            }
        }
        return false;
    }

    // get a face from this data structure (returns null if not found)
    public final Voxel getFace(Integer orientation, Integer plane, int[] facePos) {
        // determine area id
        Point areaId = getAreaId(facePos);
        // get the correct plane list for the orientation
        HashMap<Integer, HashMap<Point, HashMap<String, Voxel>>> planeList = orientationList.get(orientation);
        // get the correct area list for this plane
        HashMap<Point, HashMap<String, Voxel>> areaList = planeList.get(plane);
        if (areaList != null) {
            // get the face list that we expect our face to be in
            HashMap<String, Voxel> faceList = areaList.get(areaId);
            if (faceList != null) {
                return faceList.get(facePos[0] + "_" + facePos[1]);
            }
        }
        return null;
    }

    // expose a list of faces in a particular area (is expected not to be changed!)
    public final Collection<Voxel> getFaces(int orientation, Integer plane, Point areaId) {
        // get the correct plane list for the orientation
        HashMap<Integer, HashMap<Point, HashMap<String, Voxel>>> planeList = orientationList.get(orientation);
        // get the correct area list for this plane
        HashMap<Point, HashMap<String, Voxel>> areaList = planeList.get(plane);
        if (areaList != null) {
            HashMap<String, Voxel> faces = areaList.get(areaId);
            if (faces != null) {
                return faces.values();
            }
        }
        return null;
    }
}


