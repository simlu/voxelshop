package com.vitco.app.core.world;

import com.threed.jpct.SimpleVector;
import com.vitco.app.core.data.container.Voxel;
import com.vitco.app.core.world.container.BorderObject3D;
import com.vitco.app.core.world.container.VoxelManager;
import com.vitco.app.low.hull.HullManagerExt;
import com.vitco.app.low.triangulate.Grid2TriPolyFast;
import com.vitco.app.low.triangulate.util.Grid2PolyHelper;
import com.vitco.app.settings.VitcoSettings;
import com.vitco.app.util.graphic.SharedImageFactory;
import org.poly2tri.Poly2Tri;
import org.poly2tri.triangulation.delaunay.DelaunayTriangle;

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

    // ----------------

    // static constructor
    static {
        // jvm will convert this to native code
        // => so there is no lag later on
        Poly2Tri.warmup();
        // initialize the buffered images (for textures)
        for (int e1 = 2; e1 <= 4; e1++) { // from 4 (=2^2) to 16 (=2^4)
            int d1 = (int)Math.pow(2, e1);
            for (int e2 = 2; e2 <= 4; e2++) { // from 4 (=2^2) to 16 (=2^4)
                int d2 = (int)Math.pow(2, e2);
                SharedImageFactory.getBufferedImage(d1, d2);
            }
        }
        for (int e1 = 7; e1 <= 9; e1++) { // from 128 (=2^7) to 512 (=2^9)
            int d1 = (int)Math.pow(2, e1);
            for (int e2 = 7; e2 <= 9; e2++) { // from 128 (=2^7) to 512 (=2^9)
                int d2 = (int)Math.pow(2, e2);
                SharedImageFactory.getBufferedImage(d1, d2);
            }
        }
    }

    // manages the voxel "hull" (allows for easy querying of hull changes)
    private final HullManagerExt<Voxel> hullManager = new HullManagerExt<Voxel>();

    // manages the voxels that are in this world, allows for easy detection
    // of changed areas (combined faces of neighbouring voxels)
    private final VoxelManager voxelManager = new VoxelManager(hullManager, side);
    // true if the world needs a clear (this flag is necessary to do the
    // clearing in sync with the rendering)
    private boolean worldNeedsClear = false;

    // add or update a voxel
    @Override
    public void updateVoxel(Voxel voxel) {
        hullManager.update(voxel.posId, voxel);
    }

    // erase the entire content of this world
    @Override
    public void clear() {
        // the hull manager needs to be cleared here (since it is not in sync
        // with the rendering thread)
        hullManager.clear();
        // this flag is necessary to do the clearing in sync with the rendering
        worldNeedsClear = true;
    }


    // clear field by voxel
    @Override
    public boolean clearPosition(Voxel voxel) {
        return hullManager.clearPosition(voxel.posId);
    }

    // ====================================

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
        HashMap<Integer, HashMap<Point, Boolean>> outdatedPlanes = voxelManager.getInvalidPlanes(orientation);

        int progressCounter = 0;

        for (Iterator<Map.Entry<Integer, HashMap<Point, Boolean>>> planeIterator = outdatedPlanes.entrySet().iterator(); planeIterator.hasNext() && progressCounter < maxAreaDraw;) {
            Map.Entry<Integer, HashMap<Point, Boolean>> entry = planeIterator.next();
            Integer outdatedPlane = entry.getKey();
            HashMap<Point, Boolean> outdatedAreas = entry.getValue();

            // loop over all outdated areas
            for (Iterator<Map.Entry<Point,Boolean>> areaIterator = outdatedAreas.entrySet().iterator(); areaIterator.hasNext() && progressCounter < maxAreaDraw; progressCounter++) {
                Map.Entry<Point,Boolean> outdatedAreaEntry = areaIterator.next();
                Point outdatedArea = outdatedAreaEntry.getKey();
                Boolean fullRefresh = outdatedAreaEntry.getValue();
                // id for this particular area
                String areaKey = orientation + "_" + outdatedPlane + "_" + outdatedArea.x + "_" + outdatedArea.y;

                if (fullRefresh) { // full refresh (recreate triangulation)
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
                            int[] pos2D = VoxelManager.convert3D2D(face, axis);
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
                        ArrayList<DelaunayTriangle> tris = new ArrayList<DelaunayTriangle>();
                        boolean[][] data = new boolean[w][h];
                        for (Voxel face : faceList) {
                            int[] pos2D = VoxelManager.convert3D2D(face, axis);
                            data[pos2D[0] - min1][pos2D[1] - min2] = true;
//                            // consider textured faces separately (needed?)
//                            if (face.getTexture() == null) {
//                                data[pos2D[0] - min1][pos2D[1] - min2] = true;
//                            } else {
//                                int pX = pos2D[0] - min1;
//                                int pY = pos2D[1] - min2;
//                                // add the textured faces separately
//                                tris.add(new DelaunayTriangle(new PolygonPoint(pX, pY), new PolygonPoint(pX + 1, pY), new PolygonPoint(pX, pY + 1)));
//                                tris.add(new DelaunayTriangle(new PolygonPoint(pX + 1, pY), new PolygonPoint(pX + 1, pY + 1), new PolygonPoint(pX, pY + 1)));
//                            }
                        }
                        tris.addAll(Grid2TriPolyFast.triangulate(Grid2PolyHelper.convert(data)));
//                        tris.addAll(Grid2TriGreedyOptimal.triangulate(data));
                        // --------------
                        // todo: remove
//                        // build image to compute triangle overlay
//                        TiledImage src = SharedImageFactory.getTiledImage(w, h);
//                        for (Voxel face : faceList) {
//                            int[] pos2D = VoxelManager.convert3D2D(face, axis);
//                            src.setSample(pos2D[0] - min1, pos2D[1] - min2, 0, 1);
//                        }
//                        // triangulate the image
//                        ArrayList<DelaunayTriangle> tris = Grid2Tri.triangulate(Grid2Tri.doVectorize(src));
//                        // reset image
//                        for (Voxel face : faceList) {
//                            int[] pos2D = VoxelManager.convert3D2D(face, axis);
//                            src.setSample(pos2D[0] - min1, pos2D[1] - min2, 0, 0);
//                        }
//                        // --------------

                        // build the plane
                        BorderObject3D box = new BorderObject3D(
                                tris, faceList,
                                min1, min2, w, h, orientation, axis,
                                outdatedPlane, simpleMode, side, culling,
                                hasBorder, hullManager
                        );
                        // remove old version of this side (if exists)
                        Integer oldId = plane2WorldId.get(areaKey);
                        if (oldId != null) {
                            // only remove texture in non-wireframe world
                            if (!simpleMode) {
                                BorderObject3D obj = (BorderObject3D) getObject(oldId);
                                // remove other information
                                removeObject(oldId);
                                obj.freeTexture();
                            } else {
                                // remove other information
                                removeObject(oldId);
                            }
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
                            // only remove texture in non-wireframe world
                            if (!simpleMode) {
                                BorderObject3D obj = (BorderObject3D) getObject(oldId);
                                // remove other information
                                removeObject(oldId);
                                obj.freeTexture();
                            } else {
                                // remove other information
                                removeObject(oldId);
                            }
                            worldId2Side.remove(oldId);
                        }
                    }
                } else if (!simpleMode) {
                    // only do texture refresh (soft)
                    Integer objId = plane2WorldId.get(areaKey);
                    if (objId != null) {
                        ((BorderObject3D) getObject(objId)).refreshTextureInterpolation();
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

        // clear the voxel manager if necessary (needs to be done in sync!)
        if (worldNeedsClear) {
            worldNeedsClear = false;
            // clear the voxel manager
            voxelManager.clear();
            // remove world objects
            for (Integer objId : worldId2Side.keySet()) {
                // only remove texture in non-wireframe world
                if (!simpleMode) {
                    BorderObject3D obj = (BorderObject3D) getObject(objId);
                    // remove other information
                    removeObject(objId);
                    obj.freeTexture();
                } else {
                    // remove other information
                    removeObject(objId);
                }
            }
            worldId2Side.clear();
            plane2WorldId.clear();
        }

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

    // do a hit test against the voxels in this world
    @Override
    public short[] hitTest(SimpleVector position, SimpleVector dir) {
        return hullManager.hitTest(position, dir);
    }

}
