package com.vitco.engine;

import com.newbrightidea.util.RTree;
import com.threed.jpct.Interact2D;
import com.threed.jpct.Object3D;
import com.threed.jpct.SimpleVector;
import com.vitco.engine.data.container.ExtendedVector;
import com.vitco.engine.data.container.VOXELMODE;
import com.vitco.engine.data.container.Voxel;
import com.vitco.engine.data.notification.DataChangeAdapter;
import com.vitco.res.VitcoSettings;
import com.vitco.util.BiMap;
import com.vitco.util.WorldUtil;
import com.vitco.util.action.ChangeListener;
import com.vitco.util.action.types.StateActionPrototype;

import javax.annotation.PostConstruct;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

/**
 * Defines general (common) interactions available for this engine view and sets them up.
 */
public abstract class EngineInteractionPrototype extends EngineViewPrototype {

    // constructor
    protected EngineInteractionPrototype() {
        super();
        // refresh the 2D index when the camera changes
        camera.addCameraChangeListener(new CameraChangeListener() {
            @Override
            public void onCameraChange() {
                animationAdapter.refresh2DIndex();
            }
        });
    }

    // current mode
    protected VOXELMODE voxelMode;

    // ===============================
    // Animation
    // ===============================

    // animation adapter
    protected final AnimationMouseAdapter animationAdapter = new AnimationMouseAdapter();
    protected class AnimationMouseAdapter extends MouseAdapter {
        private int dragPoint = -1; // the point that is dragged
        private long wasDragged = -1; // -1 if not dragged or the time in ms of first drag event

        // rebuild 2d index to do hit test when mouse is moving
        private final RTree<ExtendedVector> points2D = new RTree<ExtendedVector>(50, 2, 2); //2D Rtree
        private boolean needToRebuild = true;
        private void rebuild2DIndex() {
            if (needToRebuild) {
                points2D.clear();
                for (ExtendedVector point : data.getPoints()) {
                    ExtendedVector tmp = convertExt3D2D(point);
                    if (tmp != null) {
                        points2D.insert(new float[]{tmp.x, tmp.y}, new float[] {0,0}, tmp);
                    }
                }
                needToRebuild = false;
            }
        }

        // get the 3D point for the mouse event in the same distance as the refPoint (from camera)
        private SimpleVector getPoint(MouseEvent e, SimpleVector refPoint) {
            SimpleVector result;
            if (voxelSnap) {
                SimpleVector dir = Interact2D.reproject2D3DWS(camera, buffer, e.getX()*2, e.getY()*2).normalize();
                Object[] res = world.calcMinDistanceAndObject3D(camera.getPosition(), dir, 1000000);
                if (res[1] != null) {
                    Object3D obj3D = ((Object3D)res[1]);
                    result = obj3D.getOrigin();
                } else {
                    result = convert2D3D(e.getX(), e.getY(), refPoint);
                }
            } else {
                result = convert2D3D(e.getX(), e.getY(), refPoint);
            }
            return result;
        }

        // set the snap functionality for animations dots (they "snap" to voxels)
        private boolean voxelSnap = false;
        public void setVoxelSnap(boolean b) {
            voxelSnap = b;
        }
        public boolean getVoxelSnap() {
            return voxelSnap;
        }

        // call this when the animation data points have changed (e.g the perspective changed or
        // the data changed)
        public void refresh2DIndex() {
            needToRebuild = true;
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            if (dragPoint != -1) { // there is a point dragged
                if (container.getBounds().contains(e.getPoint())) {
                    if (wasDragged == -1) { // remember that this point was dragged
                        wasDragged = System.currentTimeMillis();
                    } else {
                        data.undoA();
                    }
                    data.setPreviewLine(-1, -1); // reset the preview line
                    // move the point to the correct position
                    ExtendedVector tmp = data.getPoint(dragPoint);

                    SimpleVector point = getPoint(e, tmp);
                    data.movePoint(dragPoint, point);
                }
            }
        }

        @Override
        public void mouseMoved(MouseEvent e) {
            rebuild2DIndex(); // only recomputes if necessary
            int selected_point = data.getSelectedPoint();

            // find if there is a point nearby
            List<ExtendedVector> search = points2D.search(new float[]{e.getX() - VitcoSettings.ANIMATION_CIRCLE_RADIUS,e.getY() - VitcoSettings.ANIMATION_CIRCLE_RADIUS},
                    new float[]{VitcoSettings.ANIMATION_CIRCLE_RADIUS * 2, VitcoSettings.ANIMATION_CIRCLE_RADIUS * 2});

            // make sure this is in circle (and not just in square!)
            for (int i = 0, len = search.size(); i < len; i++) {
                if ( Math.pow(search.get(i).x - e.getX(),2.0) + Math.pow(search.get(i).y - e.getY(),2.0)
                        > Math.pow((double)VitcoSettings.ANIMATION_CIRCLE_RADIUS,2.0)) {
                    search.remove(i);
                    i--;
                    len--;
                }
            }

            int tmp = -1;
            if (search.size() > 0) {
                // get the circle on top
                Collections.sort(search, new Comparator<ExtendedVector>() {
                    @Override
                    public int compare(ExtendedVector o1, ExtendedVector o2) {
                        return (int)Math.signum(o2.z - o1.z);
                    }
                });
                // remember id
                tmp = search.get(0).id;
            }

            data.highlightPoint(tmp); // highlight that point
            int highlighted_point = data.getHighlightedPoint();

            // set the preview line iff highlighted and selected point exist and are different
            if (selected_point != -1 && highlighted_point != selected_point && highlighted_point != -1) {
                data.setPreviewLine(selected_point, highlighted_point);
            } else {
                data.setPreviewLine(-1, -1);
            }
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            camera.setEnabled(true);
            needToRebuild = true;
            if (e.getButton() == 1) { // left mb
                if (dragPoint != -1 && wasDragged != -1) {
                    // do not save this move action if the point position did not change
                    ExtendedVector point = data.getPoint(dragPoint);
                    data.undoA();
                    ExtendedVector point2 = data.getPoint(dragPoint);
                    if (!point.equals(point2)) {
                        data.redoA();
                    }
                }
                dragPoint = -1; // stop dragging
                final int highlighted_point = data.getHighlightedPoint();
                final int selected_point = data.getSelectedPoint();
                if (highlighted_point != -1) { // there is a highlighted point
                    // if it was not at all or only for a short time dragged
                    if (wasDragged == -1 || (System.currentTimeMillis() - wasDragged < 75) ) {
                        if (selected_point == highlighted_point) { // click on selected point
                            data.selectPoint(-1); // deselect
                        } else {
                            if (selected_point == -1) { // click on new point
                                data.selectPoint(highlighted_point); // select
                            } else {
                                // click on different point -> connect/disconnect line
                                if (data.areConnected(selected_point, highlighted_point)) {
                                    data.disconnect(selected_point, highlighted_point);
                                    //animationData.selectPoint(-1); // unselect after disconnect
                                } else {
                                    data.connect(selected_point, highlighted_point);
                                    //animationData.selectPoint(highlighted_point); // select after connect
                                }
                                data.selectPoint(-1); // unselect
                                // reset "highlighting"
                                data.setPreviewLine(-1, -1);
                            }
                        }
                    } else {
                        data.selectPoint(-1);
                    }
                }
            }
            mouseMoved(e);
        }

        @Override
        public void mousePressed(final MouseEvent e) {
            final int highlighted_point = data.getHighlightedPoint();
            final int selected_point = data.getSelectedPoint();
            if (highlighted_point != -1) {
                camera.setEnabled(false);
            }
            switch (e.getButton()) {
                case 3: if (highlighted_point != -1) {
                    // highlighted point -> ask to remove
                    JPopupMenu popup = new JPopupMenu();
                    JMenuItem remove = new JMenuItem(langSelector.getString("remove_point"));
                    remove.addActionListener(new ActionListener() {
                        private final int tmp_point = highlighted_point; // store point for later access
                        @Override
                        public void actionPerformed(ActionEvent evt) {
                            // add a point
                            data.removePoint(tmp_point);
                            data.highlightPoint(-1);
                        }
                    });
                    popup.add(remove);
                    popup.show(e.getComponent(), e.getX(), e.getY());
                } else {
                    // right click on background -> deselect
                    data.selectPoint(-1);
                }
                    break;
                case 1: // if left mouse
                    if (highlighted_point != -1) { // highlighted -> select point
                        wasDragged = -1;
                        dragPoint = highlighted_point;
                    } else if (e.getClickCount() == 2) {
                        // not highlighted and double-click -> add a point
                        // check if we hit something
                        SimpleVector point = getPoint(e, getRefPoint());
                        int added = data.addPoint(point);
                        if (selected_point != -1) { // connect if possible
                            data.connect(added, selected_point);
                        }
                        data.selectPoint(added); // and select
                    }
                    break;
            }
        }
    }

    // return reference point (for new points to add - same distance)
    protected SimpleVector getRefPoint() {
        return SimpleVector.ORIGIN;
    }

    // ===============================
    // Voxel
    // ===============================

    // voxel data getter to be defined
    protected abstract Voxel[] getVoxels();

    // maps voxel ids to world ids
    protected final BiMap<Integer, Integer> voxelToObject = new BiMap<Integer, Integer>();
    protected final HashMap<Integer, Voxel> idToVoxel = new HashMap<Integer, Voxel>();

    // helper - add a voxel object to world
    protected final void addVoxelToWorld(Voxel voxel) {
        int id = WorldUtil.addBox(world,
                new SimpleVector(
                        voxel.getPosAsInt()[0] * VitcoSettings.VOXEL_SIZE,
                        voxel.getPosAsInt()[1] * VitcoSettings.VOXEL_SIZE,
                        voxel.getPosAsInt()[2] * VitcoSettings.VOXEL_SIZE),
                VitcoSettings.VOXEL_SIZE / 2,
                voxel.getColor());
        voxelToObject.put(voxel.id, id);
    }

    // helper - make sure the voxel objects in the world are up to date
    @Override
    protected final void updateWorldWithVoxels() {
        Voxel[] voxels = getVoxels();

        // temporary to find unneeded objects
        ArrayList<Integer> voxelIds = new ArrayList<Integer>();
        voxelIds.addAll(voxelToObject.keySet());

        // loop over all voxels
        for (Voxel voxel : voxels) {
            voxelIds.remove((Integer)voxel.id);
            if (voxelToObject.doesNotContainKey(voxel.id)) { // add all new voxels
                addVoxelToWorld(voxel);
                idToVoxel.put(voxel.id, voxel);
            } else { // remove and add all altered voxels
                if (!idToVoxel.get(voxel.id).equals(voxel)) {
                    idToVoxel.put(voxel.id, voxel);
                    world.removeObject(voxelToObject.get(voxel.id)); // remove
                    addVoxelToWorld(voxel); // add
                }
            }
        }

        // remove the objects that are no longer needed
        for (int id : voxelIds) {
            world.removeObject(voxelToObject.get(id));
            voxelToObject.removeByKey(id);
            idToVoxel.remove(id);
        }
    }

    // voxel draw adapter for main view
    @SuppressWarnings("CanBeFinal")
    protected VoxelAdapter voxelAdapter = new VoxelAdapter();
    protected class VoxelAdapter extends MouseAdapter {
        // true means we're currently adding/removing voxels on drag
        private boolean massVoxel = false;

        private Point lastMovePos = new Point(0,0);
        private boolean mouseInside = false;
        // replay the last hover
        public final void replayHover() {
            if (mouseInside) {
                hover(lastMovePos);
            }
        }

        // execute on mouse event
        protected void execute(MouseEvent e) {
            if (container.getBounds().contains(e.getPoint())) {
                if (data.getHighlightedVoxel() != null) { // something highlighted
                    camera.setEnabled(voxelMode == VOXELMODE.VIEW);
                    if (voxelMode == VOXELMODE.DRAW) { // add voxel
                        if (data.getLayerVisible(data.getSelectedLayer())) { // is visible
                            switch (e.getModifiersEx()) {
                                case InputEvent.BUTTON1_DOWN_MASK: // left click
                                    data.addVoxel(data.getCurrentColor(), data.getHighlightedVoxel());
                                    break;
                                case InputEvent.BUTTON3_DOWN_MASK: // right click
                                    Voxel voxel = data.searchVoxel(data.getHighlightedVoxel(), true);
                                    if (null != voxel) {
                                        data.removeVoxel(voxel.id);
                                    }
                                    break;
                            }
                        }
                    } else if (voxelMode == VOXELMODE.ERASE) { // remove voxel
                        Voxel highlightedVoxel = data.searchVoxel(data.getHighlightedVoxel(), true);
                        if (highlightedVoxel != null) {
                            data.removeVoxel(highlightedVoxel.id);
                        }
                    } else if (voxelMode == VOXELMODE.PICKER) {
                        Voxel highlightedVoxel = data.searchVoxel(data.getHighlightedVoxel(), false);
                        if (highlightedVoxel != null) {
                            data.setCurrentColor(highlightedVoxel.getColor());
                        }
                    } else if (voxelMode == VOXELMODE.COLORCHANGER) {
                        Voxel highlightedVoxel = data.searchVoxel(data.getHighlightedVoxel(), true);
                        if (highlightedVoxel != null) {
                            data.setColor(highlightedVoxel.id, data.getCurrentColor());
                        }
                    }
                    massVoxel = true;
                }
            }
        }

        // hover on mouse event
        protected void hover(Point point) {
            // check if we hit something
            SimpleVector dir = Interact2D.reproject2D3DWS(camera, buffer, (int)Math.round(point.getX() * 2), (int)Math.round(point.getY() * 2)).normalize();
            Object[] res = world.calcMinDistanceAndObject3D(camera.getPosition(), dir, 10000);
            if (res[1] != null && voxelMode != VOXELMODE.VIEW) { // something hit
                Object3D obj3D = ((Object3D)res[1]);
                Voxel hitVoxel = data.getVoxel(voxelToObject.getKey(obj3D.getID()));
                if (hitVoxel != null) {
                    int[] voxelPos = hitVoxel.getPosAsInt();
                    if (voxelMode == VOXELMODE.DRAW) { // select next to voxel
                        // find collision point
                        SimpleVector colPoint = camera.getPosition();
                        dir.scalarMul((Float)res[0]);
                        colPoint.add(dir);
                        // find side that it hits
                        ArrayList<float[]> planes = new ArrayList<float[]>();
                        planes.add(new float[] {1, colPoint.distance(obj3D.getOrigin().calcAdd(new SimpleVector(0,-1,0)))});
                        planes.add(new float[] {2, colPoint.distance(obj3D.getOrigin().calcAdd(new SimpleVector(0,1,0)))});
                        planes.add(new float[] {3, colPoint.distance(obj3D.getOrigin().calcAdd(new SimpleVector(-1,0,0)))});
                        planes.add(new float[] {4, colPoint.distance(obj3D.getOrigin().calcAdd(new SimpleVector(1,0,0)))});
                        planes.add(new float[] {5, colPoint.distance(obj3D.getOrigin().calcAdd(new SimpleVector(0,0,-1)))});
                        planes.add(new float[] {6, colPoint.distance(obj3D.getOrigin().calcAdd(new SimpleVector(0,0,1)))});
                        Collections.sort(planes, new Comparator<float[]>() {
                            @Override
                            public int compare(float[] o1, float[] o2) {
                                return (int) Math.signum(o1[1] - o2[1]);
                            }
                        });
                        switch ((int)planes.get(0)[0]) {
                            case 1: voxelPos[1] -= 1; break;
                            case 2: voxelPos[1] += 1; break;
                            case 3: voxelPos[0] -= 1; break;
                            case 4: voxelPos[0] += 1; break;
                            case 5: voxelPos[2] -= 1; break;
                            case 6: voxelPos[2] += 1; break;
                        }
                    }
                    // highlight the voxel (position)
                    data.highlightVoxel(voxelPos);
                }
            } else { // hit nothing
                if (voxelMode == VOXELMODE.DRAW) { // trying to draw
                    // hit nothing, draw preview on zero level
                    if (dir.y > 0.05) { // angle big enough
                        // calculate position
                        float t = (VitcoSettings.VOXEL_GROUND_DISTANCE - camera.getPosition().y) / dir.y;
                        dir.scalarMul(t);
                        SimpleVector pos = camera.getPosition();
                        pos.add(dir);
                        pos.scalarMul(1/VitcoSettings.VOXEL_SIZE);
                        if (Math.abs(pos.x) < VitcoSettings.VOXEL_GROUND_MAX_RANGE && Math.abs(pos.z) < VitcoSettings.VOXEL_GROUND_MAX_RANGE) {
                            // if we hit the ground plane
                            data.highlightVoxel(new int[]{Math.round(pos.x),Math.round(pos.y - 0.5f),Math.round(pos.z)});
                        } else {
                            data.highlightVoxel(null);
                        }
                    } else { // angle too small
                        data.highlightVoxel(null);
                    }
                } else { // not trying to draw and hit nothing
                    data.highlightVoxel(null);
                }
            }
        }

        @Override
        public final void mousePressed(MouseEvent e) {
            // make sure there is a layer selected or display a warning
            if (data.getSelectedLayer() == -1) {
                Integer[] layers = data.getLayers();
                if (layers.length > 0) {
                    data.selectLayer(layers[0]);
                } else {
                    console.addLine(langSelector.getString("no_layer_warning"));
                }
            }
            execute(e);
        }

        @Override
        public final void mouseReleased(MouseEvent e) {
            massVoxel = false;
            hover(e.getPoint());
            camera.setEnabled(true);
        }

        @Override
        public final void mouseEntered(MouseEvent e) {
            mouseInside = true;
        }

        @Override
        public final void mouseExited(MouseEvent e) {
            mouseInside = false;
            data.removeVoxelHighlights();
        }

        @Override
        public final void mouseDragged(MouseEvent e) {
            lastMovePos = new Point(e.getPoint());
            if (massVoxel) {
                hover(e.getPoint());
                execute(e);
            } else {
                data.removeVoxelHighlights();
            }
        }

        @Override
        public final void mouseMoved(MouseEvent e) {
            lastMovePos = new Point(e.getPoint());
            hover(e.getPoint());
        }
    }

    @PostConstruct
    protected final void init() {

        // enable/disable snap
        actionManager.performWhenActionIsReady("toggle_voxel_snap", new Runnable() {
            @Override
            public void run() {
                ((StateActionPrototype) actionManager.getAction("toggle_voxel_snap")).addChangeListener(new ChangeListener() {
                    @Override
                    public void actionFired(boolean b) {
                        animationAdapter.setVoxelSnap(b);
                    }
                });
            }
        });

        // what to do when data changes
        DataChangeAdapter dca = new DataChangeAdapter() {

            @Override
            public void onAnimationDataChanged() {
                container.skipNextWorldRender(); // no need to re-render scene
                animationAdapter.refresh2DIndex(); // refresh 2D index when data changes
                forceRepaint();
            }

            @Override
            public void onAnimationSelectionChanged() {
                container.skipNextWorldRender(); // no need to re-render scene
                forceRepaint();
            }

            @Override
            public void onVoxelDataChanged() {
                invalidateVoxels();
                container.doNotSkipNextWorldRender();
                forceRepaint();
            }

            @Override
            public void onVoxelSelectionChanged() {
                container.skipNextWorldRender();
                forceRepaint();
            }

            @Override
            public void onAnimateChanged() {
                if (data.isAnimate()) {
                    data.removeVoxelHighlights();
                    container.removeMouseMotionListener(voxelAdapter);
                    container.removeMouseListener(voxelAdapter);
                    container.addMouseMotionListener(animationAdapter);
                    container.addMouseListener(animationAdapter);
                } else {
                    data.removeAnimationHighlights();
                    container.removeMouseMotionListener(animationAdapter);
                    container.removeMouseListener(animationAdapter);
                    container.addMouseMotionListener(voxelAdapter);
                    container.addMouseListener(voxelAdapter);
                    voxelAdapter.replayHover();
                }
                container.setDrawAnimationOverlay(data.isAnimate());
                forceRepaint();
            }

            @Override
            public void onVoxelModeChanged() {
                voxelMode = data.getVoxelMode();
                voxelAdapter.replayHover();
                forceRepaint();
            }
        };
        data.addDataChangeListener(dca);
        // initialize modes
        dca.onAnimateChanged();
        dca.onVoxelModeChanged();
    }
}
