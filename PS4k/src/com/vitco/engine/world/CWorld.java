package com.vitco.engine.world;

import com.vitco.engine.data.container.Voxel;
import com.vitco.engine.world.container.BorderObject3D;
import com.vitco.engine.world.container.VoxelManager;
import com.vitco.res.VitcoSettings;
import com.vitco.util.SharedImageFactory;
import com.vitco.util.hull.HullManager;
import com.vitco.util.triangulate.Grid2Tri;
import org.poly2tri.Poly2Tri;
import org.poly2tri.triangulation.delaunay.DelaunayTriangle;

import javax.media.jai.TiledImage;
import java.awt.*;
import java.util.*;

/**
 * This is a world wrapper that provides easy voxel interaction
 * (allows for adding and removing of voxels)
 */
public class CWorld extends AbstractCWorld {
    private static final long serialVersionUID = 1L;
    // constructor
    public CWorld(boolean culling, Integer side, boolean simpleMode) {
        super(culling, side, simpleMode);
    }

    // convert a voxel to its short[] position
    private static short[] voxel2Short(Voxel voxel) {
        return new short[] {(short) voxel.x, (short) voxel.y, (short) voxel.z};
    }

    // ----------------

    // static constructor
    static {
        // jvm will convert this to native code
        // => so there is no lag later on
        Poly2Tri.warmup();
        // initialize the shared images (for conversion into polygon)
        for (int w = 1; w < VitcoSettings.TRI_GRID_SIZE + 1; w++) {
            for (int h = 1; h < VitcoSettings.TRI_GRID_SIZE + 1; h++) {
                SharedImageFactory.getTiledImage(w, h);
            }
        }
        // initialize the buffered images (for textures)
        for (int e = 2; e < 9; e++) { // from 4 (=2^2) to 512 (=2^9)
            int d = (int)Math.pow(2, e);
            SharedImageFactory.getBufferedImage(d, d);
        }
    }

    // manages the voxel "hull" (allows for easy querying of hull changes)
    private final HullManager<Voxel> hullManager = new HullManager<Voxel>();

    // manages the voxels that are in this world, allows for easy detection
    // of changed areas (combined faces of neighbouring voxels)
    private final VoxelManager voxelManager = new VoxelManager();

    // add or update a voxel
    @Override
    public void updateVoxel(Voxel voxel) {
        hullManager.update(voxel2Short(voxel), voxel);
    }

    // erase the entire content of this world
    @Override
    public void clear() {
        hullManager.clear();
        voxelManager.clear();
        // remove world objects
        for (Integer objId : worldId2Side.keySet()) {
            this.removeObject(objId);
        }
        worldId2Side.clear();
        plane2WorldId.clear();
    }

    // clear field by position
    @Override
    public boolean clearPosition(int[] pos) {
        return hullManager.clearPosition(new short[]{(short) pos[0], (short) pos[1], (short) pos[2]});
    }

    // clear field by voxel
    @Override
    public boolean clearPosition(Voxel voxel) {
        return hullManager.clearPosition(voxel2Short(voxel));
    }

    // ====================================

    // remove a texture
    private boolean removeTexture(int orientation, int plane, Point areaId) {
        return WorldManager.removeEfficientTexture(
                BorderObject3D.getTextureId(orientation, plane, areaId, side)
        );
    }

    // used to retrieve which world objects belong to which side (0-5, i.e. direction)
    private final HashMap<Integer, Integer> worldId2Side = new HashMap<Integer, Integer>();
    // stores side/plane/area combination to object world id
    private final HashMap<String, Integer> plane2WorldId = new HashMap<String, Integer>();

    // enable/disable the border on all objects in the world (main view)
    private boolean hasBorder = true;
    @Override
    public final void setBorder(boolean border) {
        hasBorder = border;
        for (Integer worldId : worldId2Side.keySet()) {
            ((BorderObject3D)this.getObject(worldId)).setBorder(border);
        }
    }

    // the maximum amount of areas that are drawn in one call
    private final static int maxAreaDraw = 10;

    // handle all planes (in one direction, determined by side)
    // returns true iff all areas are handled
    private boolean handleOrientedPlane(int orientation) {
        int axis = orientation/2;
        // processed entries are cleaned here in this function (!)
        HashMap<Integer, HashSet<Point>> outdatedPlanes = voxelManager.getInvalidPlanes(orientation);

        int progressCounter = 0;

        for (Iterator<Map.Entry<Integer, HashSet<Point>>> planeIterator = outdatedPlanes.entrySet().iterator(); planeIterator.hasNext() && progressCounter < maxAreaDraw;) {
            Map.Entry<Integer, HashSet<Point>> entry = planeIterator.next();
            Integer outdatedPlane = entry.getKey();
            HashSet<Point> outdatedAreas = entry.getValue();

            // loop over all outdated areas
            for (Iterator<Point> areaIterator = outdatedAreas.iterator(); areaIterator.hasNext() && progressCounter < maxAreaDraw; progressCounter++) {
                Point outdatedArea = areaIterator.next();
                // id for this particular area
                String areaKey = orientation + "_" + outdatedPlane + "_" + outdatedArea.x + "_" + outdatedArea.y;

                // handle the triangle building
                Collection<Voxel> faceList = voxelManager.getFaces(orientation, outdatedPlane, outdatedArea);
                if (faceList != null) {
                    // this should never happen as the faceManager deletes unused faceLists
                    assert !faceList.isEmpty();
                    // determine size of rect that contains all voxel faces
                    boolean first = true;
                    int min1 = 0;
                    int max1 = 0;
                    int min2 = 0;
                    int max2 = 0;
                    for (Voxel face : faceList) {
                        int[] pos2D = VoxelManager.convert(face, axis);
                        if (first) {
                            min1 = pos2D[0];
                            max1 = pos2D[0];
                            min2 = pos2D[1];
                            max2 = pos2D[1];
                            first = false;
                        } else {
                            min1 = Math.min(min1,pos2D[0]);
                            max1 = Math.max(max1, pos2D[0]);
                            min2 = Math.min(min2,pos2D[1]);
                            max2 = Math.max(max2, pos2D[1]);
                        }
                    }
                    int w = max1 - min1 + 1;
                    int h = max2 - min2 + 1;

                    // --------------
                    // build image to compute triangle overlay
                    TiledImage src = SharedImageFactory.getTiledImage(w, h);
                    for (Voxel face : faceList) {
                        int[] pos2D = VoxelManager.convert(face, axis);
                        src.setSample(pos2D[0] - min1, pos2D[1] - min2, 0, 1);
                    }
                    // triangulate the image
                    ArrayList<DelaunayTriangle> tris = Grid2Tri.triangulate(Grid2Tri.doVectorize(src));
                    // reset image
                    for (Voxel face : faceList) {
                        // todo optimize
                        int[] pos2D = VoxelManager.convert(face, axis);
                        src.setSample(pos2D[0] - min1, pos2D[1] - min2, 0, 0);
                    }
                    // --------------

                    // build the plane
                    BorderObject3D box = new BorderObject3D(
                            tris, faceList,
                            min1, min2, w, h, orientation, axis,
                            outdatedPlane, outdatedArea, simpleMode, side, culling,
                            hasBorder
                    );
                    // remove old version of this side (if exists)
                    Integer oldId = plane2WorldId.get(areaKey);
                    if (oldId != null) {
                        removeObject(oldId);
                        worldId2Side.remove(oldId);
                    }
                    // add new plane
                    int newWorldId = addObject(box);
                    plane2WorldId.put(areaKey, newWorldId);
                    worldId2Side.put(newWorldId, orientation);
                } else {
                    // remove old version of this side (if exists)
                    Integer oldId = plane2WorldId.remove(areaKey);
                    if (oldId != null) {
                        removeObject(oldId);
                        worldId2Side.remove(oldId);
                        // only remove texture in non-wireframe world
                        if (!simpleMode) {
                            removeTexture(orientation, outdatedPlane, outdatedArea);
                        }
                    }
                }
                // this area was processed
                areaIterator.remove();
            }
            // clear this plane entry if all areas are processed
            if (outdatedAreas.isEmpty()) {
                planeIterator.remove();
            }
        }
        //faceManager.clearInvalidAreas(orientation);
        return progressCounter < maxAreaDraw;
    }

    // refresh world (partially) - returns true if fully refreshed
    @Override
    public boolean refreshWorld() {
        // if this counter is six, the world is ready
        int ready = 0;

        // handle the updating
        if (side == -1) {
            for (int i = 0; i < 6; i++) {
                for (Voxel voxel : hullManager.getHullAdditions(i)) {
                    voxelManager.addFace(i, voxel);
                }
                for (Voxel voxel : hullManager.getHullRemovals(i)) {
                    voxelManager.removeFace(i, voxel);
                }
                if (handleOrientedPlane(i)) {
                    ready++;
                }
            }
        } else {
            int orientation = side == 0 ? 5 : (side == 1 ? 3 : 1);
            for (Voxel voxel : hullManager.getHullAdditions(orientation)) {
                voxelManager.addFace(orientation, voxel);
            }
            for (Voxel voxel : hullManager.getHullRemovals(orientation)) {
                voxelManager.removeFace(orientation, voxel);
            }
            if (handleOrientedPlane(orientation)) {
                ready = 6;
            }
        }

        return ready == 6;

    }

    // get voxel by hit position
    @Override
    public final int[] getVoxelPos(Integer objectId, float posx, float posy, float posz) {
        Integer side = worldId2Side.get(objectId);
        int[] result = null;
        if (side != null) {
            Integer axis = side/2;
            result = new int[] {
                    Math.round((posx/VitcoSettings.VOXEL_SIZE) + (axis == 0 ? (side == 0 ? -0.5f : 0.5f) : 0)),
                    Math.round((posy/VitcoSettings.VOXEL_SIZE) + (axis == 1 ? (side == 2 ? -0.5f : 0.5f) : 0)),
                    Math.round((posz/VitcoSettings.VOXEL_SIZE) + (axis == 2 ? (side == 4 ? -0.5f : 0.5f) : 0))
            };
        }
        return result;
    }

    // get side for world object
    @Override
    public Integer getSide(Integer objectId) {
        return worldId2Side.get(objectId);
    }

}
