package com.vitco.engine;

import com.newbrightidea.util.RTree;
import com.threed.jpct.*;
import com.vitco.engine.data.container.Voxel;
import com.vitco.res.VitcoSettings;
import com.vitco.util.BiMap;
import com.vitco.util.WorldUtil;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * This is a world wrapper that provides easy voxel interaction.
 */
public class CWorld extends World {
    private static final long serialVersionUID = 1L;
    // static variables
    private final static float[] ZEROS = new float[] {0,0,0};

    // constructor
    private final boolean culling;
    private final Integer side; // default -1 (all sites)
    public CWorld(boolean culling, Integer side) {
        this.culling = culling;
        this.side = side;
    }

    // ==============================

    // holds the voxel internal
    private final HashMap<Integer, Voxel> voxels = new HashMap<Integer, Voxel>();
    // RTree that is used to find voxels by location
    private final RTree<Voxel> positions = new RTree<Voxel>(50,2,3);
    // these voxels need to be refreshed
    private final ArrayList<Integer> toRefresh = new ArrayList<Integer>();
    // maps voxel ids to world objects (entries are existent iff world object exists)
    private transient final BiMap<Integer, Integer> voxelIdToWorldId = new BiMap<Integer, Integer>();

    // ==============================
    // drawing of selected (wireframe)

    // move offset
    private SimpleVector offset = new SimpleVector(0, 0, 0);
    private float length = offset.length();

    // set the shift of this (just used for "drawAsShiftedWireframe"
    public final void setShift(int[] shift) {
        offset = new SimpleVector(
                shift[0],
                shift[1],
                shift[2]);
        length = offset.length() * VitcoSettings.VOXEL_SIZE;
        offset = offset.normalize();
    }

    // draw just the wireframe (possible shifted)
    public final void drawAsShiftedWireframe(FrameBuffer buffer, Color selected, Color shifted) {
        if (length != 0) {
            getCamera().moveCamera(offset, length);
            renderScene(buffer);
            drawWireframe(buffer, shifted);
            getCamera().moveCamera(offset, -length);
        } else {
            renderScene(buffer);
            drawWireframe(buffer, selected);
        }
    }

    public final SimpleVector shiftedCollisionPoint(Point point, FrameBuffer buffer) {
        // check if we hit a <selected> voxel
        SimpleVector result = null;
        Camera camera = getCamera();
        camera.moveCamera(offset, length);
        SimpleVector dir = Interact2D.reproject2D3DWS(camera, buffer, (int) Math.round(point.getX() * 2), (int) Math.round(point.getY() * 2)).normalize();
        Object[] res = calcMinDistanceAndObject3D(camera.getPosition(), dir, 100000);
        if (res[1] != null) { // something hit
            // find collision point
            result = camera.getPosition();
            dir.scalarMul((Float)res[0]);
            result.add(dir);
        }
        camera.moveCamera(offset, -length);
        return result;
    }

    // ==============================
    // internal voxel handling

    // updates internal lists (no refresh registration)
    private void updateVoxelInternal(Voxel voxel) {
        // make sure old detailes are removed (if exist)
        removeVoxelInternal(voxel.id);

        // make sure there exists no voxel already
        int[] pos = voxel.getPosAsInt();
        List<Voxel> list = positions.search(new float[] {pos[0], pos[1], pos[2]}, ZEROS);
        if (list.size() > 0) {
            int id = list.get(0).id;
            removeVoxelInternal(id);
            if (!toRefresh.contains(id)) { // add them if not already known to need updating
                toRefresh.add(id);
            }
        }

        // add this voxel
        voxels.put(voxel.id, voxel);
        positions.insert(voxel.getPosAsFloat(), voxel);
    }

    // updates internal lists (no refresh registration)
    private void removeVoxelInternal(Integer voxelId) {
        if (voxels.containsKey(voxelId)) {
            Voxel voxel = voxels.get(voxelId);
            positions.delete(voxel.getPosAsFloat(), voxel);
            voxels.remove(voxelId);
        }
    }

    // mark the neighbors of this position to need refresh
    private void markNeighborsForRefresh(float[] pos) {
        // search all the neighboring positions
        for (int i = 0; i < 6; i++) {
            int add = i%2 == 0 ? 1 : -1;
            List<Voxel> list = positions.search(new float[] {
                    i/2 == 0 ? pos[0] + add : pos[0],
                    i/2 == 1 ? pos[1] + add : pos[1],
                    i/2 == 2 ? pos[2] + add : pos[2]
            }, ZEROS);
            if (list.size() > 0) {
                Voxel neigh = list.get(0);
                if (!toRefresh.contains(neigh.id)) { // add them if not already known to need updating
                    toRefresh.add(neigh.id);
                }
            }
        }
    }

    // calculate the required sides
    private String calculateRequiredSides(float[] pos) {
        // calculate the required sides
        StringBuilder sides = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            int add = i%2 == 0 ? 1 : -1;
            sides.append(positions.search(new float[] {
                    i/2 == 0 ? pos[0] + add : pos[0],
                    i/2 == 1 ? pos[1] + add : pos[1],
                    i/2 == 2 ? pos[2] + add : pos[2]
            }, ZEROS).size() > 0 ? "1" : "0");
        }
        return sides.toString();
    }

    // add voxel
    public final boolean updateVoxel(Voxel voxel) {
        boolean result = false;
        if (!voxels.containsKey(voxel.id)) { // unknown voxel
            result = true;
        } else {
            if (!voxels.get(voxel.id).equals(voxel)) { // new version of voxel
                result = true;
            }
        }
        if (result) {
            // mark all the neighbors for refresh
            markNeighborsForRefresh(voxel.getPosAsFloat());
            // store/overwrite this voxel
            updateVoxelInternal(voxel);
            // mark to refresh (if not already marked)
            if (!toRefresh.contains(voxel.id)) {
                toRefresh.add(voxel.id);
            }
        }
        return result;
    }

    // remove voxel
    public final boolean removeVoxel(Integer voxelId) {
        boolean result = false;
        if (voxels.containsKey(voxelId)) {
            // mark all the neighbors for refresh
            markNeighborsForRefresh(voxels.get(voxelId).getPosAsFloat());
            // remove voxel
            removeVoxelInternal(voxelId);
            // mark to refresh (if not already marked)
            if (!toRefresh.contains(voxelId)) {
                toRefresh.add(voxelId);
            }
            result = true;
        }
        return result;
    }

    public final void clear() {
        Integer[] ids = new Integer[voxels.size()];
        int i = 0;
        for (Voxel voxel : voxels.values()) {
            ids[i++] = voxel.id;
        }
        for (int id : ids) {
            removeVoxelInternal(id);
            // mark to refresh (if not already marked)
            if (!toRefresh.contains(id)) {
                toRefresh.add(id);
            }
        }
    }

    // clear field by position
    public final boolean clearPosition(int[] pos) {
        boolean result = false;
        List<Voxel> list = positions.search(new float[] {pos[0], pos[1], pos[2]}, ZEROS);
        if (list.size() > 0) {
            removeVoxel(list.get(0).id);
            result = true;
        }
        return result;
    }

    // refresh world
    public final void refreshWorld() {
        for (Integer voxelId : toRefresh) {
            if (voxels.containsKey(voxelId)) {
                // remove old world object (if exists)
                if (voxelIdToWorldId.containsKey(voxelId)) {
                    removeObject(voxelIdToWorldId.get(voxelId));
                }
                // add new world object
                Voxel voxel = voxels.get(voxelId);
                int id = WorldUtil.addBoxSides(this,
                        new SimpleVector(
                                voxel.getPosAsInt()[0] * VitcoSettings.VOXEL_SIZE,
                                voxel.getPosAsInt()[1] * VitcoSettings.VOXEL_SIZE,
                                voxel.getPosAsInt()[2] * VitcoSettings.VOXEL_SIZE),
                        voxel.getColor(),
                        // draw the appropriate site only
                        side == -1
                                ? calculateRequiredSides(voxel.getPosAsFloat())
                                : ("1" + (side == 2 ? "0" : "1") +
                                "1" + (side == 1 ? "0" : "1") +
                                "1" + (side == 0 ? "0" : "1"))
                        , culling);
                voxelIdToWorldId.put(voxel.id, id);
            } else {
                // need to remove world object
                if (voxelIdToWorldId.containsKey(voxelId)) {
                    removeObject(voxelIdToWorldId.get(voxelId));
                    voxelIdToWorldId.removeByKey(voxelId);
                }
            }
        }
        toRefresh.clear();
    }

    // retrieve voxel for object id
    public final Integer getVoxelId(Integer objectId) {
        return voxelIdToWorldId.getKey(objectId);
    }

    // retrieve all voxel ids that are loaded
    public final Integer[] getLoaded() {
        Integer[] result = new Integer[voxelIdToWorldId.keySet().size()];
        voxelIdToWorldId.keySet().toArray(result);
        return result.clone();
    }

}