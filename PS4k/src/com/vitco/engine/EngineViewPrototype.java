package com.vitco.engine;

import com.newbrightidea.util.RTree;
import com.threed.jpct.*;
import com.vitco.engine.data.Data;
import com.vitco.engine.data.container.ExtendedVector;
import com.vitco.engine.data.container.Voxel;
import com.vitco.logic.ViewPrototype;
import com.vitco.res.VitcoSettings;
import com.vitco.util.BiMap;
import com.vitco.util.G2DUtil;
import com.vitco.util.WorldUtil;
import com.vitco.util.pref.PrefChangeListener;
import com.vitco.util.thread.LifeTimeThread;
import com.vitco.util.thread.ThreadManagerInterface;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.*;
import java.util.List;

/**
 * Rendering functionality of this World (data + overlay)
 *
 * Can switch each of them on/off. Defines the basic objects: data, world, buffer, camera.
 */
public abstract class EngineViewPrototype extends ViewPrototype {
    // var & setter
    protected Data data;
    @Autowired
    public final void setData(Data data) {
        this.data = data;
    }

    protected ThreadManagerInterface threadManager;
    // set the action handler
    @Autowired
    public final void setThreadManager(ThreadManagerInterface threadManager) {
        this.threadManager = threadManager;
    }

    // the world-required objects
    protected final World world;
    protected final World selectedVoxelsWorld;
    protected FrameBuffer buffer;
    protected final CCamera camera;

    // conversion
    protected final SimpleVector convert2D3D(int x, int y, SimpleVector referencePoint) {
        SimpleVector result = Interact2D.reproject2D3DWS(camera, buffer, x*2, y*2).normalize();
        result.scalarMul(camera.getPosition().distance(referencePoint));
        result.add(camera.getPosition());
        return result;
    }

    // conversion
    protected final SimpleVector convert3D2D(SimpleVector point) {
        SimpleVector result = Interact2D.project3D2D(camera, buffer, point);
        if (result != null) {
            result.scalarMul(0.5f);
        }
        return result;
    }

    // conversion
    protected final ExtendedVector convertExt3D2D(ExtendedVector point) {
        ExtendedVector result = null;
        SimpleVector point2d = Interact2D.project3D2D(camera, buffer, point);
        if (point2d != null) {
            point2d.scalarMul(0.5f);
            result = new ExtendedVector(point2d, point.id);
        }
        return result;
    }

    // ==============================
    // updating of world with voxels
    // ==============================

    // if true we use updateWorldWithVoxelsSimple with simpleVoxelModeSide,
    // otherwise we use updateWorldWithVoxels to update world
    protected boolean useSimpleVoxelMode = false;
    protected int simpleVoxelModeSide = -1;

    // toggle between simple mode (draw only one side) and normal mode (draw needed sides)
    // clear indexes
    protected final void setSimpleVoxelMode(boolean b, int side) {
        useSimpleVoxelMode = b;
        simpleVoxelModeSide = side;

        selectedVoxels.clear();
        voxelToObject.clear();
        idToVoxel.clear();
        world.removeAllObjects();
        selectedVoxelsWorld.removeAllObjects();

        voxelPositions.clear();
    }

    // voxel data getter to be defined
    protected abstract Voxel[] getVoxels();

    // retrieve the voxel for an object id if exists, otherwise return null
    protected final Voxel getVoxelForObjectId(int id) {
        return data.getVoxel(voxelToObject.getKey(id));
    }

    // keeps track of selected voxels on world
    private final HashMap<Integer, Integer> selectedVoxels = new HashMap<Integer, Integer>();

    // maps voxel ids to world ids
    private final BiMap<Integer, Integer> voxelToObject = new BiMap<Integer, Integer>();
    private final HashMap<Integer, Voxel> idToVoxel = new HashMap<Integer, Voxel>();
    // index to find voxels in range
    private final RTree<Voxel> voxelPositions = new RTree<Voxel>(50, 2, 3);
    private static final float[] ZEROS = new float[] {0,0,0};

    // helper to remove a voxel id from the world
    private void removeVoxel(int worldObjId) {
        world.removeObject(worldObjId); // remove
        int voxelId = voxelToObject.getKey(worldObjId);
        // remove selected voxel if exists
        if (selectedVoxels.containsKey(voxelId)) {
            selectedVoxelsWorld.removeObject(selectedVoxels.get(voxelId));
            selectedVoxels.remove(voxelId);
        }
    }

    // helper: set the correct deselect/select state of this voxel
    private void handleVoxelSelection(Voxel voxel) {
        if (voxel.isSelected()) {
            if (!selectedVoxels.containsKey(voxel.id)) {
                int selId = WorldUtil.addBox(selectedVoxelsWorld, new SimpleVector(
                        voxel.getPosAsInt()[0] * VitcoSettings.VOXEL_SIZE,
                        voxel.getPosAsInt()[1] * VitcoSettings.VOXEL_SIZE,
                        voxel.getPosAsInt()[2] * VitcoSettings.VOXEL_SIZE),
                        VitcoSettings.VOXEL_SIZE/2, voxel.getColor());
                selectedVoxels.put(voxel.id, selId);
            }
        } else {
            if (selectedVoxels.containsKey(voxel.id)) {
                selectedVoxelsWorld.removeObject(selectedVoxels.get(voxel.id));
                selectedVoxels.remove(voxel.id);
            }
        }
    }

    // helper to add voxel to world (with one side)
    private void addVoxelToWorld(Voxel voxel, int side) {
        int id = WorldUtil.addBoxSides(world,
                new SimpleVector(
                        voxel.getPosAsInt()[0] * VitcoSettings.VOXEL_SIZE,
                        voxel.getPosAsInt()[1] * VitcoSettings.VOXEL_SIZE,
                        voxel.getPosAsInt()[2] * VitcoSettings.VOXEL_SIZE),
                voxel.getColor(),
                "1" + (side == 2 ? "0" : "1") +
                "1" + (side == 1 ? "0" : "1") +
                "1" + (side == 0 ? "0" : "1"));
        voxelToObject.put(voxel.id, id);
    }

    // helper - make sure the voxel objects are up to date, but only draw one side of the voxel
    private void updateWorldWithVoxelsSimple(int side) {
        // retrive the voxels
        Voxel[] voxels = getVoxels();

        // temporary to find unneeded objects
        ArrayList<Integer> voxelIds = new ArrayList<Integer>();
        voxelIds.addAll(voxelToObject.keySet());

        // loop over all voxels
        for (Voxel voxel : voxels) {
            if (!voxelToObject.containsKey(voxel.id)) { // add all new voxels
                voxelPositions.insert(voxel.getPosAsFloat(), ZEROS, voxel);
                addVoxelToWorld(voxel, side);
                idToVoxel.put(voxel.id, voxel);
            } else { // remove and add all altered voxels
                Voxel oldVoxel = idToVoxel.get(voxel.id);
                if (!oldVoxel.equals(voxel)) {
                    voxelPositions.delete(oldVoxel.getPosAsFloat(), ZEROS, oldVoxel); // delete old entry
                    voxelPositions.insert(voxel.getPosAsFloat(), ZEROS, voxel); // add new
                    idToVoxel.put(voxel.id, voxel);
                    removeVoxel(voxelToObject.get(voxel.id)); // remove
                    addVoxelToWorld(voxel, side);
                }
            }
            // check that the selection is up to date
            handleVoxelSelection(voxel);
            // do not remove this voxel
            voxelIds.remove((Integer)voxel.id);
        }

        // remove the objects that are no longer needed
        for (int id : voxelIds) {
            removeVoxel(voxelToObject.get(id));
            voxelToObject.removeByKey(id);
            Voxel voxel = idToVoxel.get(id);
            voxelPositions.delete(voxel.getPosAsFloat(), ZEROS, voxel);
            idToVoxel.remove(id);
        }
    }

    // add neighbors, uses voxelPositions index
    private void addNeighbors(ArrayList<Voxel> toRefresh, Voxel voxel) {
        float[] pos = voxel.getPosAsFloat();
        // search all the voxels neighboring
        for (int i = 0; i < 6; i++) {
            int add = i%2 == 0 ? 1 : -1;
            List<Voxel> list = voxelPositions.search(new float[] {
                    i/2 == 0 ? pos[0] + add : pos[0],
                    i/2 == 1 ? pos[1] + add : pos[1],
                    i/2 == 2 ? pos[2] + add : pos[2]
            }, ZEROS);
            if (list.size() > 0) {
                Voxel neigh = list.get(0);
                if (!toRefresh.contains(neigh)) { // add them if not already known to need updating
                    toRefresh.add(neigh);
                }
            }
        }
    }

    // helper - make sure the voxel objects in the world are up to date
    private void updateWorldWithVoxels() {
        final Voxel[] voxels = getVoxels();

        // we need to rebuild these voxels
        ArrayList<Voxel> toRefresh = new ArrayList<Voxel>();

        // temporary to find unneeded objects
        ArrayList<Integer> voxelToRemove = new ArrayList<Integer>();
        voxelToRemove.addAll(voxelToObject.keySet());

        // erased voxels
        ArrayList<Voxel> refreshOnlyNeighbors = new ArrayList<Voxel>();

        // loop over all voxels
        for (Voxel voxel : voxels) {
            if (!voxelToObject.containsKey(voxel.id)) { // add all new voxels
                idToVoxel.put(voxel.id, voxel);
                voxelPositions.insert(voxel.getPosAsFloat(), ZEROS, voxel);
                toRefresh.add(voxel);
            } else { // remove and re-add all altered voxels
                Voxel oldVoxel = idToVoxel.get(voxel.id);
                if (!oldVoxel.equals(voxel)) {
                    voxelPositions.delete(oldVoxel.getPosAsFloat(), ZEROS, oldVoxel); // delete old entry
                    voxelPositions.insert(voxel.getPosAsFloat(), ZEROS, voxel); // add new
                    idToVoxel.put(voxel.id, voxel); // overwrite voxel object
                    // register to change environment
                    refreshOnlyNeighbors.add(oldVoxel);
                    toRefresh.add(voxel);
                }
            }
            // check that the selection is up to date
            handleVoxelSelection(voxel);
            // do not remove this voxel
            voxelToRemove.remove((Integer) voxel.id);
        }

        // remove the objects that are no longer needed
        for (int id : voxelToRemove) {
            removeVoxel(voxelToObject.get(id));
            voxelToObject.removeByKey(id);
            Voxel voxel = idToVoxel.get(id);
            refreshOnlyNeighbors.add(voxel);
            voxelPositions.delete(voxel.getPosAsFloat(), ZEROS, voxel);
            idToVoxel.remove(id);
        }

        // add all neighbors
        for (int i1 = 0, tobuildSize = toRefresh.size(); i1 < tobuildSize; i1++) {
            Voxel voxel = toRefresh.get(i1);
            addNeighbors(toRefresh, voxel);
        }

        // add all neighbors of the erased voxels
        for (Voxel voxel : refreshOnlyNeighbors) {
            addNeighbors(toRefresh, voxel);
        }

        // refresh the objects
        for (Voxel voxel : toRefresh) {
            float[] pos = voxel.getPosAsFloat();
            // calculate the required sides
            StringBuilder sides = new StringBuilder();
            for (int i = 0; i < 6; i++) {
                int add = i%2 == 0 ? 1 : -1;
                sides.append(voxelPositions.search(new float[] {
                        i/2 == 0 ? pos[0] + add : pos[0],
                        i/2 == 1 ? pos[1] + add : pos[1],
                        i/2 == 2 ? pos[2] + add : pos[2]
                }, ZEROS).size() > 0 ? "1" : "0");
            }
            int id = WorldUtil.addBoxSides(world,
                    new SimpleVector(
                            voxel.getPosAsInt()[0] * VitcoSettings.VOXEL_SIZE,
                            voxel.getPosAsInt()[1] * VitcoSettings.VOXEL_SIZE,
                            voxel.getPosAsInt()[2] * VitcoSettings.VOXEL_SIZE),
                    voxel.getColor(), sides.toString());
            // remember the object reference
            Integer key = voxelToObject.get(voxel.id);
            voxelToObject.put(voxel.id, id);
            // remove if there was already an object for this voxel
            // (just the display changed!)
            if (key != null) {
                world.removeObject(key);
                //removeVoxel(key);
            }

        }

    }

    // true iff the world does not need to be updated with voxels
    private boolean worldVoxelCurrent = false;
    // force update of world before next draw
    protected final void invalidateVoxels() {
        if (localMouseDown) { // instant update needed for interaction
            refreshVoxels();
        } else {
            worldVoxelCurrent = false;
        }
    }

    // make sure the voxels are valid "right now"
    private void refreshVoxels() {
        if (useSimpleVoxelMode) {
            updateWorldWithVoxelsSimple(simpleVoxelModeSide);
        } else {
            updateWorldWithVoxels();
        }
        worldVoxelCurrent = true;
    }

    // ==============================
    // END: updating of world with voxels
    // ==============================

    // set wireframe mode
    private boolean useWireFrame = false;
    protected final void useWireFrame(boolean b) {
        useWireFrame = b;
    }

    // the container that we draw on
    protected final MPanel container = new MPanel();

    protected final class MPanel extends JPanel {

        // bg color of this panel
        private Color bgColor = VitcoSettings.ANIMATION_BG_COLOR;
        // the current preview plane
        private int previewPlane = 0;

        // initialize
        public void init() {
            // register bg color change
            preferences.addPrefChangeListener("engine_view_bg_color", new PrefChangeListener() {
                @Override
                public void onPrefChange(Object newValue) {
                    bgColor = (Color)newValue;
                    forceRepaint();
                }
            });
            // register preview plane change
            preferences.addPrefChangeListener("engine_view_voxel_preview_plane", new PrefChangeListener() {
                @Override
                public void onPrefChange(Object newValue) {
                    previewPlane = (Integer)newValue;
                }
            });
        }

        // this draws opengl content if enabled
        private boolean drawWorld = true;
        public final void setDrawWorld(boolean b) {
            drawWorld = b;
        }

        // this enabled/disables all overlay
        private boolean drawOverlay = true;
        public final void setDrawOverlay(boolean b) {
            drawOverlay = b;
        }

        // enabled/ disables animation overlay
        private boolean drawAnimationOverlay = true;
        public final void setDrawAnimationOverlay(boolean b) {
            drawAnimationOverlay = b;
        }

        // enabled/ disables animation overlay
        private boolean drawVoxelOverlay = true;
        public final void setDrawVoxelOverlay(boolean b) {
            drawVoxelOverlay = b;
        }

        // for the next refresh do not update the world (OpenGL render)
        private boolean skipNextWorldRender = false;
        public final void skipNextWorldRender() {
            skipNextWorldRender = true;
        }
        // prevent skipping of next world view
        private boolean doNotSkipNextWorldRender = false;
        public final void doNotSkipNextWorldRender() {
            doNotSkipNextWorldRender = true;
        }

        // to set the preview rect
        private Rectangle previewRect = null;
        public final void setPreviewRect(Rectangle rect) {
            previewRect = rect;
        }

        // wrapper
        private void drawCubeLine(Graphics2D ig, SimpleVector[] vectors, int i, int j, Color color, float distance, float[] zRange) {
            float range1 = (vectors[i].z-zRange[0])/distance;
            float range2 = (vectors[j].z-zRange[0])/distance;

            ig.setPaint(new GradientPaint(
                    Math.round(vectors[i].x), Math.round(vectors[i].y),
                    new Color(color.getRed(), color.getGreen(), color.getBlue(), Math.round(55 + range1*150)),
                    Math.round(vectors[j].x), Math.round(vectors[j].y),
                    new Color(color.getRed(), color.getGreen(), color.getBlue(), Math.round(55 + range2*150)),
                    false));

            ig.drawLine(Math.round(vectors[i].x), Math.round(vectors[i].y),
                    Math.round(vectors[j].x), Math.round(vectors[j].y));
        }

        // draw overlay for voxels
        private void drawVoxelOverlay(Graphics2D ig) {
            // Anti-alias
            ig.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);

            // draw the select rect
            if (previewRect != null) {
                ig.setColor(Color.WHITE);
                ig.drawRect(previewRect.x, previewRect.y, previewRect.width, previewRect.height);
            }

            // draw the prievew voxel
            final int[] voxel = data.getHighlightedVoxel();
            // draw selected voxel (ghost / preview voxel)
            if (voxel != null) {
                Color previewColor = VitcoSettings.VOXEL_PREVIEW_LINE_COLOR;
//                // try to find the voxel color
//                List<Voxel> list = voxelPositions.search(new float[] {voxel[0], voxel[1], voxel[2]}, ZEROS);
//                if (list.size() > 0) {
//                    Voxel voxelObject3D = list.get(0);
//                    if (ColorTools.perceivedBrightness(voxelObject3D.getColor()) < 130) {
//                        previewColor = VitcoSettings.VOXEL_PREVIEW_LINE_COLOR_BRIGHT;
//                    }
//                }
                // define the points of the voxel
                SimpleVector[] vectors = new SimpleVector[] {
                        new SimpleVector(voxel[0] + 0.5, voxel[1] + 0.5, voxel[2] + 0.5),
                        new SimpleVector(voxel[0] + 0.5, voxel[1] + 0.5, voxel[2] - 0.5),
                        new SimpleVector(voxel[0] + 0.5, voxel[1] - 0.5, voxel[2] - 0.5),
                        new SimpleVector(voxel[0] + 0.5, voxel[1] - 0.5, voxel[2] + 0.5),
                        new SimpleVector(voxel[0] - 0.5, voxel[1] + 0.5, voxel[2] + 0.5),
                        new SimpleVector(voxel[0] - 0.5, voxel[1] + 0.5, voxel[2] - 0.5),
                        new SimpleVector(voxel[0] - 0.5, voxel[1] - 0.5, voxel[2] - 0.5),
                        new SimpleVector(voxel[0] - 0.5, voxel[1] - 0.5, voxel[2] + 0.5)
                };
                boolean valid = true;
                for (int i = 0; i < vectors.length; i++) {
                    // scale and convert the points
                    vectors[i].scalarMul(VitcoSettings.VOXEL_SIZE);
                    vectors[i] = convert3D2D(vectors[i]);
                    // check that valid
                    if (vectors[i] == null) {
                        valid = false;
                    }
                }

                if (valid) {
                    // calculate the z range
                    float[] zRange = new float[] {vectors[0].z, vectors[0].z}; // min and max z value
                    for (int i = 1; i < 8; i ++) {
                        zRange[0] = Math.min(vectors[i].z, zRange[0]);
                        zRange[1] = Math.max(vectors[i].z, zRange[1]);
                    }
                    float distance = zRange[1] - zRange[0];

                    // draw the cube
                    ig.setStroke(new BasicStroke(1f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL)); // line size
                    for (int i = 0; i < 4; i++) {
                        drawCubeLine(ig, vectors, i, (i + 1) % 4, previewColor, distance, zRange);
                        drawCubeLine(ig, vectors, i + 4, (i + 1) % 4 + 4, previewColor, distance, zRange);
                        drawCubeLine(ig, vectors, i, i + 4, previewColor, distance, zRange);
                    }

                    // draw the highlighted side
                    if (previewPlane != -1) {
                        // calculate center and some variables
                        int RANGE = 4;
                        float shift = (previewPlane%2 == 0 ? 0.5f : -0.5f);
                        int plane = previewPlane / 2;
                        SimpleVector center = new SimpleVector(
                                voxel[0] + (plane == 2 ? shift : 0),
                                voxel[1] + (plane == 1 ? shift : 0),
                                voxel[2] + (plane == 0 ? shift : 0)
                        );

                        // calculate the points
                        SimpleVector[] points = new SimpleVector[(RANGE*2 - 1) * 4];
                        float[] range = new float[]{-RANGE, RANGE + 1};
                        int c = 0;
                        for (int i = -RANGE + 1; i < RANGE; i++) {
                            for (float j : range) {
                                points[c] = center.calcAdd(new SimpleVector(
                                        (plane != 2 ? i - 0.5 : 0),
                                        (plane != 1 ? j - 0.5 : 0),
                                        (plane == 1 ? j - 0.5 : (plane == 2 ? i - 0.5 : 0))
                                ));
                                points[c].scalarMul(VitcoSettings.VOXEL_SIZE);
                                points[c] = convert3D2D(points[c]);
                                points[c+1] = center.calcAdd(new SimpleVector(
                                        (plane != 2 ? j - 0.5 : 0),
                                        (plane != 1 ? i - 0.5 : 0),
                                        (plane == 1 ? i - 0.5 : (plane == 2 ? j - 0.5 : 0))
                                ));
                                points[c+1].scalarMul(VitcoSettings.VOXEL_SIZE);
                                points[c+1] = convert3D2D(points[c+1]);
                                if (points[c] == null || points[c+1] == null) {
                                    valid = false;
                                }
                                c+=2;
                            }
                        }

                        if (valid) {
                            // draw the lines
                            float halfLen = points.length/((float)8);
                            Color transColor = new Color(previewColor.getRed(),
                                    previewColor.getGreen(),
                                    previewColor.getBlue(),
                                    0);
                            for (int i = 0, len = points.length/4; i < len; i++) {

                                float alpha = (halfLen-Math.abs(i-halfLen))/halfLen;
                                Color visColor = new Color(
                                        previewColor.getRed(),
                                        previewColor.getGreen(),
                                        previewColor.getBlue(),
                                        Math.min(255,Math.max(0,Math.round(100*alpha))));

                                ig.setPaint(new GradientPaint(
                                        Math.round(points[i*4].x), Math.round(points[i*4].y),
                                        transColor,
                                        Math.round((points[i*4].x + points[i*4 + 2].x)/2), Math.round((points[i*4].y + points[i*4 + 2].y)/2),
                                        visColor,
                                        true));
                                ig.drawLine(Math.round(points[i*4].x), Math.round(points[i*4].y),
                                        Math.round(points[i*4 + 2].x), Math.round(points[i*4 + 2].y));

                                ig.setPaint(new GradientPaint(
                                        Math.round(points[i*4 + 1].x), Math.round(points[i*4 + 1].y),
                                        transColor,
                                        Math.round((points[i*4 + 1].x + points[i*4 + 3].x)/2), Math.round((points[i*4 + 1].y + points[i*4 + 3].y)/2),
                                        visColor,
                                        true));
                                ig.drawLine(Math.round(points[i*4 + 1].x), Math.round(points[i*4 + 1].y),
                                        Math.round(points[i*4 + 3].x), Math.round(points[i*4 + 3].y));
                            }
                        }
                    }
                }
            }
        }

        // draw dynamic overlay on top of the openGL
        private void drawAnimationOverlay(Graphics2D ig) {
            // Anti-alias
            ig.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);

            // store lines and points
            ArrayList<ExtendedVector[]> objects = new ArrayList<ExtendedVector[]>();
            // add lines
            for (ExtendedVector[] line : data.getLines()) {
                ExtendedVector point2da = convertExt3D2D(line[0]);
                ExtendedVector point2db = convertExt3D2D(line[1]);
                if (point2da != null && point2db != null) {
                    ExtendedVector mid = new ExtendedVector(point2da.calcAdd(point2db), 0);
                    mid.scalarMul(0.5f);
                    objects.add(new ExtendedVector[] {point2da, point2db, mid});
                }
            }
            // add preview line
            ExtendedVector[] preview_line = data.getPreviewLine();
            boolean connected = preview_line != null && data.areConnected(preview_line[0].id, preview_line[1].id);
            if (preview_line != null && !connected) {
                ExtendedVector point2da = convertExt3D2D(preview_line[0]);
                ExtendedVector point2db = convertExt3D2D(preview_line[1]);
                if (point2da != null && point2db != null) {
                    ExtendedVector mid = new ExtendedVector(point2da.calcAdd(point2db), 0);
                    mid.scalarMul(0.5f);
                    objects.add(new ExtendedVector[] {point2da, point2db, mid});
                }
            }
            // add points
            for (ExtendedVector point : data.getPoints()) {
                ExtendedVector point2d = convertExt3D2D(point);
                if (point2d != null) {
                    objects.add(new ExtendedVector[] {point2d});
                }
            }
            // sort the data
            Collections.sort(objects, new Comparator<ExtendedVector[]>() {
                @Override
                public int compare(ExtendedVector[] o1, ExtendedVector[] o2) {
                    if (o1.length == 3 && o2.length == 3) { // two lines
                        return (int)Math.signum(o1[2].z - o2[2].z);
                    } else if (o1.length == 1 && o2.length == 1) { // two points
                        return (int)Math.signum(o1[0].z - o2[0].z);
                    } else if (o1.length == 1 && o2.length == 3) { // point and line
                        return (int)Math.signum(o1[0].z - o2[2].z);
                    } else if (o1.length == 3 && o2.length == 1) { // line and point
                        return (int)Math.signum(o1[2].z - o2[0].z);
                    }
                    return 0;
                }
            });

            // draw all points and lines
            int selected_point = data.getSelectedPoint();
            int highlighted_point = data.getHighlightedPoint();

            for (ExtendedVector[] object : objects) {
                if (object.length == 1) {
                    if (object[0].id == selected_point) { // selected
                        G2DUtil.drawPoint(object[0], ig,
                                VitcoSettings.ANIMATION_DOT_SEL_INNER_COLOR,
                                VitcoSettings.ANIMATION_DOT_SEL_OUTER_COLOR,
                                VitcoSettings.ANIMATION_CIRCLE_RADIUS,
                                VitcoSettings.ANIMATION_CIRCLE_BORDER_SIZE);
                    } else if (object[0].id == highlighted_point) { // highlighted
                        G2DUtil.drawPoint(object[0], ig,
                                VitcoSettings.ANIMATION_DOT_HL_INNER_COLOR,
                                VitcoSettings.ANIMATION_DOT_HL_OUTER_COLOR,
                                VitcoSettings.ANIMATION_CIRCLE_RADIUS,
                                VitcoSettings.ANIMATION_CIRCLE_BORDER_SIZE);
                    } else { // default
                        G2DUtil.drawPoint(object[0], ig,
                                VitcoSettings.ANIMATION_DOT_INNER_COLOR,
                                VitcoSettings.ANIMATION_DOT_OUTER_COLOR,
                                VitcoSettings.ANIMATION_CIRCLE_RADIUS,
                                VitcoSettings.ANIMATION_CIRCLE_BORDER_SIZE);
                    }
                } else {
                    if (preview_line != null &&
                            ((object[0].id == preview_line[0].id && object[1].id == preview_line[1].id)
                                    || (object[0].id == preview_line[1].id && object[1].id == preview_line[0].id))) {
                        G2DUtil.drawLine(object[0], object[1], ig,
                                connected ? VitcoSettings.ANIMATION_LINE_PREVIEW_REMOVE_COLOR : VitcoSettings.ANIMATION_LINE_PREVIEW_ADD_COLOR,
                                VitcoSettings.ANIMATION_LINE_OUTER_COLOR,
                                VitcoSettings.ANIMATION_LINE_SIZE);
                    } else {
                        G2DUtil.drawLine(object[0], object[1], ig,
                                VitcoSettings.ANIMATION_LINE_INNER_COLOR, VitcoSettings.ANIMATION_LINE_OUTER_COLOR,
                                VitcoSettings.ANIMATION_LINE_SIZE);
                    }
                }
            }

        }

        // wrapper
        private void drawAxeHalf(SimpleVector unitVector, boolean invert, Graphics2D ig, Color innerColor, Color outerColor, float size) {
            G2DUtil.drawLine(
                    new SimpleVector(Math.round((invert?-1:1)*unitVector.x*15 + 25), Math.round((invert?-1:1)*unitVector.y*15 + 25), 0),
                    new SimpleVector(Math.round((invert?-1:1)*unitVector.x*3 + 25), Math.round((invert?-1:1)*unitVector.y*3 + 25), 0),
                    ig,
                    innerColor,
                    outerColor,
                    size
            );
        }

        // called for content that only changes when the opengl content changes
        private void drawLinkedOverlay(final Graphics2D ig) {
            // Anti-alias
            ig.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);

            // draw axis into corner (top left)
            Matrix matrix = camera.getBack();
            ExtendedVector[] vec = new ExtendedVector[]{
                    new ExtendedVector(1,0,0,0),
                    new ExtendedVector(0,1,0,1),
                    new ExtendedVector(0,0,1,2)
            };
            for (ExtendedVector v : vec) {
                v.matMul(matrix);
            }
            Arrays.sort(vec, new Comparator<ExtendedVector>() {
                @Override
                public int compare(ExtendedVector o1, ExtendedVector o2) {
                    return (int)Math.signum(o1.z - o2.z);
                }
            });
            for (ExtendedVector v : vec) {
                drawAxeHalf(v, true, ig,
                        (v.id == 0
                                ? VitcoSettings.ANIMATION_AXIS_COLOR_X
                                : (v.id == 1
                                ? VitcoSettings.ANIMATION_AXIS_COLOR_Y
                                : VitcoSettings.ANIMATION_AXIS_COLOR_Z)),
                        VitcoSettings.ANIMATION_AXIS_OUTER_COLOR, VitcoSettings.ANIMATION_AXIS_LINE_SIZE);
            }


            // draw center cross
            ig.setColor(VitcoSettings.ANIMATION_CENTER_CROSS_COLOR);
            ig.setStroke(new BasicStroke(1.0f));
            SimpleVector center = convert3D2D(new SimpleVector(0,0,0));
            if (center != null) {
                ig.drawLine(Math.round(center.x - 5), Math.round(center.y), Math.round(center.x + 5), Math.round(center.y));
                ig.drawLine(Math.round(center.x), Math.round(center.y - 5), Math.round(center.x), Math.round(center.y + 5));
            }
        }

        // handle the redrawing of this component
        @Override
        protected final void paintComponent(Graphics g1) {
            if (skipNextWorldRender && !doNotSkipNextWorldRender) {
                skipNextWorldRender = false;
            } else {
                if (doNotSkipNextWorldRender) {
                    doNotSkipNextWorldRender = false;
                    skipNextWorldRender = false;
                }
                buffer.clear(bgColor);
                if (drawWorld) {
                    if (!worldVoxelCurrent) {
                        refreshVoxels();
                    }
                    world.renderScene(buffer);
                    if (useWireFrame) {
                        world.drawWireframe(buffer, Color.WHITE);
                    } else {
                        world.draw(buffer);
                        selectedVoxelsWorld.renderScene(buffer);
                        selectedVoxelsWorld.drawWireframe(buffer, Color.WHITE);
                    }
                }
                buffer.update();
                if (drawOverlay) { // overlay part 1
                    drawLinkedOverlay((Graphics2D) buffer.getGraphics()); // refreshes with OpenGL
                }
            }
            buffer.display(g1);
            if (drawOverlay && drawAnimationOverlay) { // overlay part 2
                drawAnimationOverlay((Graphics2D) g1); // refreshes with animation data
            }
            if (drawOverlay && drawVoxelOverlay) {
                drawVoxelOverlay((Graphics2D) g1);
            }
        }
    }

    private boolean needRepaint = true;
    protected final void forceRepaint() {
        needRepaint = true;
    }

    @PostConstruct
    public final void startup() {
        // manages the repainting of this container instance
        threadManager.manage(new LifeTimeThread() {
            @Override
            public void loop() throws InterruptedException {
                if (needRepaint) {
                    needRepaint = false;
                    container.repaint();
                }
                sleep(33); // about 25 fps
                // stop repaint when mouse is down on another view
                while (globalMouseDown && !localMouseDown) {
                    sleep(50);
                }
            }
        });
        // initialize the conainer
        container.init();
    }

    @PreDestroy
    public final void cleanup() {
        buffer.disableRenderer(IRenderer.RENDERER_OPENGL);
        buffer.dispose();
    }

    private boolean localMouseDown = false;
    private static boolean globalMouseDown = false;
    private static boolean initialized = false;
    protected EngineViewPrototype() {

        // only perform these actions once (even if the class is instantiated several times)
        if (!initialized) {
            Config.tuneForOutdoor();
            Config.fadeoutLight=false;
            Config.maxPolysVisible = 15000;
            Logger.setLogLevel(Logger.ERROR);
            initialized = true;
        }

        container.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                globalMouseDown = true;
                localMouseDown = true;
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                globalMouseDown = false;
                localMouseDown = false;
            }
        });

        // set up world objects
        world = new World();
        selectedVoxelsWorld = new World();
        camera = new CCamera();
        world.setCameraTo(camera);
        selectedVoxelsWorld.setCameraTo(camera);
        buffer = new FrameBuffer(100, 100, FrameBuffer.SAMPLINGMODE_OGSS);

        // lighting (1,1,1) = true color
        world.setAmbientLight(1, 1, 1);

        // add a border to our view
        container.setBorder(BorderFactory.createLineBorder(VitcoSettings.DEFAULT_BORDER_COLOR));

        // register size change of container and change buffer size accordingly
        container.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                if (container.getWidth() > 0 && container.getHeight() > 0) {
                    buffer.dispose();
                    buffer = null; // so the gc can collect before creation if necessary
                    buffer = new FrameBuffer(container.getWidth(), container.getHeight(), FrameBuffer.SAMPLINGMODE_OGSS);
                    container.doNotSkipNextWorldRender();
                    forceRepaint();
                }
            }
        });

    }
}
