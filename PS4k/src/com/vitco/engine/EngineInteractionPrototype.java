package com.vitco.engine;

import com.newbrightidea.util.RTree;
import com.threed.jpct.FrameBuffer;
import com.threed.jpct.Interact2D;
import com.threed.jpct.Object3D;
import com.threed.jpct.SimpleVector;
import com.vitco.async.AsyncAction;
import com.vitco.async.AsyncActionManager;
import com.vitco.engine.data.container.ExtendedVector;
import com.vitco.engine.data.container.VOXELMODE;
import com.vitco.engine.data.container.Voxel;
import com.vitco.engine.data.notification.DataChangeAdapter;
import com.vitco.res.VitcoSettings;
import com.vitco.util.ColorTools;
import com.vitco.util.SwingAsyncHelper;
import com.vitco.util.action.ChangeListener;
import com.vitco.util.action.types.StateActionPrototype;
import com.vitco.util.pref.PrefChangeListener;
import com.vitco.util.thread.ThreadManagerInterface;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.List;

// todo restructure / rewrite this class (very complex...)

/**
 * Defines general (common) interactions available for this engine view and sets them up.
 */
public abstract class EngineInteractionPrototype extends EngineViewPrototype {

    // constructor
    protected EngineInteractionPrototype(Integer side) {
        super(side);
        // refresh the 2D index when the camera changes
        camera.addCameraChangeListener(new CameraChangeListener() {
            @Override
            public void onCameraChange() {
                animationAdapter.refresh2DIndex();
            }
        });
    }

    // current mode
    protected VOXELMODE voxelMode = VitcoSettings.INITIAL_VOXEL_MODE;

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
                SimpleVector dir = Interact2D.reproject2D3DWS(camera, buffer,
                        e.getX() * VitcoSettings.SAMPLING_MODE_MULTIPLICAND,
                        e.getY() * VitcoSettings.SAMPLING_MODE_MULTIPLICAND).normalize();
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
        public final void setVoxelSnap(boolean b) {
            voxelSnap = b;
        }
        public final boolean getVoxelSnap() {
            return voxelSnap;
        }

        // call this when the animation data points have changed (e.g the perspective changed or
        // the data changed)
        public final void refresh2DIndex() {
            needToRebuild = true;
        }

        @Override
        public void mouseDragged(final MouseEvent e) {
            asyncActionManager.addAsyncAction(new AsyncAction() {
                @Override
                public void performAction() {
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
            });
        }

        @Override
        public void mouseMoved(final MouseEvent e) {
            asyncActionManager.addAsyncAction(new AsyncAction() {
                @Override
                public void performAction() {
                    rebuild2DIndex(); // only recomputes if necessary
                    int selected_point = data.getSelectedPoint();

                    // find if there is a point nearby
                    List<ExtendedVector> search = points2D.search(new float[]{e.getX() - VitcoSettings.ANIMATION_CIRCLE_RADIUS,e.getY() - VitcoSettings.ANIMATION_CIRCLE_RADIUS},
                            new float[]{
                                    VitcoSettings.ANIMATION_CIRCLE_RADIUS * VitcoSettings.SAMPLING_MODE_MULTIPLICAND,
                                    VitcoSettings.ANIMATION_CIRCLE_RADIUS * VitcoSettings.SAMPLING_MODE_MULTIPLICAND});

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
            });
        }

        @Override
        public void mouseReleased(final MouseEvent e) {
            asyncActionManager.addAsyncAction(new AsyncAction() {
                @Override
                public void performAction() {
                    camera.setEnabled(true);
                    needToRebuild = true;
                    if ((e.getModifiers() & MouseEvent.BUTTON1_MASK) == MouseEvent.BUTTON1_MASK) { // left mb
                        if (dragPoint != -1 && wasDragged != -1) {
                            // do not save this move action if the point position did not change
                            ExtendedVector point = data.getPoint(dragPoint);
                            data.undoA();
                            ExtendedVector point2 = data.getPoint(dragPoint);
                            if (!point.equals(point2)) {
                                data.redoA();
                            }
                            // quick select for side view "current" planes
                            SimpleVector position = data.getPoint(dragPoint);
                            preferences.storeObject("currentplane_sideview1", Math.round(position.z/VitcoSettings.VOXEL_SIZE));
                            preferences.storeObject("currentplane_sideview2", Math.round(position.y/VitcoSettings.VOXEL_SIZE));
                            preferences.storeObject("currentplane_sideview3", Math.round(position.x/VitcoSettings.VOXEL_SIZE));
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
            });
        }

        @Override
        public void mousePressed(final MouseEvent e) {
            asyncActionManager.addAsyncAction(new AsyncAction() {
                @Override
                public void performAction() {
                    final int highlighted_point = data.getHighlightedPoint();
                    final int selected_point = data.getSelectedPoint();
                    if (highlighted_point != -1) {
                        camera.setEnabled(false);
                    }
                    switch (e.getModifiers() & (MouseEvent.BUTTON3_MASK | MouseEvent.BUTTON1_MASK)) {
                        case MouseEvent.BUTTON3_MASK:
                            if (highlighted_point != -1) {
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
                        case MouseEvent.BUTTON1_MASK: // if left mouse
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
                        default: break;
                    }
                }
            });
        }
    }

    // return reference point (for new points to add - same distance)
    protected SimpleVector getRefPoint() {
        return SimpleVector.ORIGIN;
    }

    // ===============================
    // Voxel
    // ===============================

    // holds all the direction vectors (right, left, lower, upper, back, front)
    private final static SimpleVector[] directionVectors = new SimpleVector[] {
        new SimpleVector(1,0,0),
        new SimpleVector(-1,0,0),
        new SimpleVector(0,1,0),
        new SimpleVector(0,-1,0),
        new SimpleVector(0,0,1),
        new SimpleVector(0,0,-1)
    };
    // holds the voxel side that was last hit by a hover event
    protected int lastVoxelHitSide = 0;
    // can be override (sideview)
    protected int[] voxelPosForHoverPos(Point point, boolean selectNeighbour) {
        int[] voxelPos = null;
        // check if we hit something
        SimpleVector dir = Interact2D.reproject2D3DWS(camera, buffer,
                point.x * VitcoSettings.SAMPLING_MODE_MULTIPLICAND,
                point.y * VitcoSettings.SAMPLING_MODE_MULTIPLICAND).normalize();
        Object[] res = world.calcMinDistanceAndObject3D(camera.getPosition(), dir, 10000);
        if (res[1] != null) { // something hit
            Object3D obj3D = ((Object3D)res[1]);
            Voxel hitVoxel = data.getVoxel(world.getVoxelId(obj3D.getID()));
            if (hitVoxel != null) {
                voxelPos = hitVoxel.getPosAsInt();
                // find collision point
                SimpleVector colPoint = camera.getPosition();
                dir.scalarMul((Float)res[0]);
                colPoint.add(dir);
                colPoint.sub(obj3D.getOrigin());
                // find side that it hits
                float dist = colPoint.distance(directionVectors[0]);
                lastVoxelHitSide = 0;
                for (int i = 1; i < directionVectors.length; i++) {
                    float tempDist = colPoint.distance(directionVectors[i]);
                    if (dist > tempDist) {
                        dist = tempDist;
                        lastVoxelHitSide = i;
                    }
                }
                if (selectNeighbour) {
                    switch (lastVoxelHitSide) {
                        case 0: voxelPos[0] += 1; break;
                        case 1: voxelPos[0] -= 1; break;
                        case 2: voxelPos[1] += 1; break;
                        case 3: voxelPos[1] -= 1; break;
                        case 4: voxelPos[2] += 1; break;
                        case 5: voxelPos[2] -= 1; break;
                        default: break;
                    }
                }
            }
        } else {
            if (voxelMode == VOXELMODE.DRAW || voxelMode == VOXELMODE.FLOODFILL) { // trying to draw
                // hit nothing, draw preview on zero level
                if (dir.y > 0.05) { // angle big enough
                    // calculate position
                    float t = (VitcoSettings.VOXEL_GROUND_DISTANCE - 0.5f - camera.getPosition().y) / dir.y;
                    dir.scalarMul(t);
                    SimpleVector pos = camera.getPosition();
                    pos.add(dir);
                    pos.scalarMul(1/VitcoSettings.VOXEL_SIZE);
                    if (Math.abs(pos.x) < VitcoSettings.VOXEL_GROUND_MAX_RANGE && Math.abs(pos.z) < VitcoSettings.VOXEL_GROUND_MAX_RANGE) {
                        // if we hit the ground plane
                        voxelPos = new int[]{Math.round(pos.x),Math.round(pos.y),Math.round(pos.z)};
                    }
                }
            }
        }
        return voxelPos;
    }

    // voxel draw adapter for main view
    @SuppressWarnings("CanBeFinal")
    protected VoxelAdapter voxelAdapter = new VoxelAdapter();
    protected class VoxelAdapter extends MouseAdapter {
        // true means we're currently adding/removing voxels on drag
        private boolean massVoxel = false;
        private VOXELMODE massVoxelMode = null;

        // the last position we drew
        private int[] dragDrawStartPos = null;

        // the current color (to draw)
        private float[] currentColor = ColorTools.colorToHSB(VitcoSettings.INITIAL_CURRENT_COLOR);

        // select functionality
        private Integer selectMode = 0; // 0 = do nothing, 1 = select voxels, 2 = drag voxels
        private SimpleVector dragStartReferencePos = null;
        private SimpleVector dragStartPos = null;
        private int[] currentSelectionShift = new int[3];
        private Point selectStartPoint = new Point(0,0);

        // true if this adapter is used
        private boolean active = false;
        public final void setActive(boolean active) {
            this.active = active;
        }

        // initialize
        public void init() {
            // register change of current color
            preferences.addPrefChangeListener("currently_used_color", new PrefChangeListener() {
                @Override
                public void onPrefChange(Object newValue) {
                    currentColor = (float[])newValue;
                }
            });
            modifierListener.add(new KeyAdapter() {
                @Override
                public void keyPressed(final KeyEvent e) {
                    if (active && mouseInside) {
                        asyncActionManager.addAsyncAction(new AsyncAction() {
                            @Override
                            public void performAction() {
                                if (lastMoveEvent != null) {
                                    hover(new MouseEvent(e.getComponent(), e.getID(), e.getWhen(),
                                            e.getModifiers(), lastMoveEvent.getX(),
                                            lastMoveEvent.getY(), 0, false));
                                }
                            }
                        });
                    }
                }
            });
        }

        private MouseEvent lastMoveEvent = null;
        private boolean mouseInside = false;
        // replay the last hover
        public final void replayHover() {
            if (mouseInside && lastMoveEvent != null) {
                hover(lastMoveEvent);
            }
        }

        // the voxel position that was last added
        // in this massVoxel add event
        private int[] lastAddedVoxel = null;

        // helper for flood fille
        private int dist(int pos[], int node[]) {
            return Math.max( Math.max(Math.abs(pos[0] - node[0]), Math.abs(pos[1] - node[1])), Math.abs(pos[2] - node[2]));
        }

        // flood fill with new voxel
        private void floodFill(int[] pos, HashMap<String, int[]> result, int size, int limit) {
            ArrayList<int[]> queue = new ArrayList<int[]>();
            queue.add(0, new int[]{pos[0], pos[1], pos[2], 0});
            int count = 0;
            while (!queue.isEmpty()) {
                Collections.sort(queue, new Comparator<int[]>() {
                    @Override
                    public int compare(int[] o1, int[] o2) {
                        return o1[3] - o2[3];
                    }
                });
                int[] node = queue.remove(0);
                String strPos = node[0] + "_" + node[1] + "_" + node[2];
                if (count < limit && node[3] < size) {
                    if (!result.containsKey(strPos)) {
                        if (data.searchVoxel(node, false) == null) {
                            // add to result list
                            result.put(strPos, new int[] {node[0], node[1], node[2]});
                            count++;
                            if (side != 2) {
                                queue.add(0, new int[] {node[0] + 1, node[1], node[2],
                                        dist(pos, new int[] {node[0] + 1, node[1], node[2]})});
                                queue.add(0, new int[] {node[0] - 1, node[1], node[2],
                                        dist(pos, new int[] {node[0] - 1, node[1], node[2]})});
                            }
                            if (side != 1) {
                                queue.add(0, new int[] {node[0], node[1] + 1, node[2],
                                        dist(pos, new int[] {node[0], node[1] + 1, node[2]})});
                                queue.add(0, new int[] {node[0], node[1] - 1, node[2],
                                        dist(pos, new int[] {node[0], node[1] - 1, node[2]})});
                            }
                            if (side != 0) {
                                queue.add(0, new int[] {node[0], node[1], node[2] + 1,
                                        dist(pos, new int[] {node[0], node[1], node[2] + 1})});
                                queue.add(0, new int[] {node[0], node[1], node[2] - 1,
                                        dist(pos, new int[] {node[0], node[1], node[2] - 1})});
                            }
                        }
                    }
                }
            }
        }

        // flood fill (recolor)
        private void floodColor(int[] pos, Color color, HashMap<String, Integer> result) {
            ArrayList<int[]> queue = new ArrayList<int[]>();
            queue.add(0, pos.clone());
            while (!queue.isEmpty()) {
                int[] node = queue.remove(0);
                String strPos = node[0] + "_" + node[1] + "_" + node[2];
                if (!result.containsKey(strPos)) {
                    Voxel voxel = data.searchVoxel(node, false);
                    if (voxel != null && voxel.getColor().equals(color)) {
                        // add to result list
                        result.put(strPos, voxel.id);
                        if (side != 2) {
                            queue.add(0, new int[] {node[0] + 1, node[1], node[2]});
                            queue.add(0, new int[] {node[0] - 1, node[1], node[2]});
                        }
                        if (side != 1) {
                            queue.add(0, new int[] {node[0], node[1] + 1, node[2]});
                            queue.add(0, new int[] {node[0], node[1] - 1, node[2]});
                        }
                        if (side != 0) {
                            queue.add(0, new int[] {node[0], node[1], node[2] + 1});
                            queue.add(0, new int[] {node[0], node[1], node[2] - 1});
                        }
                    }
                }
            }
        }

        // execute on mouse event
        protected void executeNormalMode(MouseEvent e) {
            if (container.getBounds().contains(e.getPoint())) {

                int[] highlighted = data.getHighlightedVoxel();
                if (highlighted != null) { // something highlighted
                    camera.setEnabled(voxelMode == VOXELMODE.VIEW);
                    if (voxelMode == VOXELMODE.DRAW) { // add voxel
                        if (data.getLayerVisible(data.getSelectedLayer())) { // is visible
                            switch (e.getModifiersEx() & (InputEvent.BUTTON1_DOWN_MASK | InputEvent.BUTTON3_DOWN_MASK)) {
                                case InputEvent.BUTTON1_DOWN_MASK: // left click
                                    if (!massVoxel && side == -1) {
                                        // memorise position
                                        dragDrawStartPos = highlighted;
                                    }
                                    if (side != -1 || (dragDrawStartPos != null && dragDrawStartPos[1] == highlighted[1])) {
                                        // get the texture
                                        int selectedTexture = data.getSelectedTexture();
                                        int[] texture = selectedTexture == -1 ? null : new int[] {
                                                selectedTexture, selectedTexture, selectedTexture,
                                                selectedTexture, selectedTexture, selectedTexture
                                        };

                                        if (lastAddedVoxel != null) {
                                            // try to add voxels in between
                                            int[] mid = new int[] {
                                                    (lastAddedVoxel[0] & highlighted[0]) + ((lastAddedVoxel[0] ^ highlighted[0]) >> 1),
                                                    (lastAddedVoxel[1] & highlighted[1]) + ((lastAddedVoxel[1] ^ highlighted[1]) >> 1),
                                                    (lastAddedVoxel[2] & highlighted[2]) + ((lastAddedVoxel[2] ^ highlighted[2]) >> 1)
                                            };
                                            if (data.searchVoxel(mid, false) == null) {
                                                // only draw if there is no voxels already here
                                                data.addVoxel(ColorTools.hsbToColor(currentColor), texture, mid);
                                            }
                                        }

                                        if (data.searchVoxel(highlighted, false) == null) {
                                            // only draw if there is no voxels already here
                                            data.addVoxel(ColorTools.hsbToColor(currentColor), texture, highlighted);
                                            lastAddedVoxel = highlighted;
                                        }
                                    }
                                    break;
                                case InputEvent.BUTTON3_DOWN_MASK: // right click
                                    Voxel voxel = data.searchVoxel(highlighted, true);
                                    if (null != voxel) {
                                        data.removeVoxel(voxel.id);
                                    }
                                    break;
                                default: break;
                            }
                        }
                    } else if (voxelMode == VOXELMODE.FLOODFILL) { // remove voxel
                        if (data.getLayerVisible(data.getSelectedLayer())) { // is visible
                            switch (e.getModifiersEx() & (InputEvent.BUTTON1_DOWN_MASK | InputEvent.BUTTON3_DOWN_MASK)) {
                                case InputEvent.BUTTON1_DOWN_MASK: // left click
                                    if (!massVoxel && side == -1) {
                                        // memorise position
                                        dragDrawStartPos = highlighted;
                                    }
                                    if (side != -1 || (dragDrawStartPos != null && dragDrawStartPos[1] == highlighted[1])) {

                                        Voxel voxel = data.searchVoxel(highlighted, false);
                                        if (voxel != null) {
                                            Color color = ColorTools.hsbToColor(currentColor);
                                            if (!color.equals(voxel.getColor())) {
                                                // flood recolor
                                                HashMap<String, Integer> result = new HashMap<String, Integer>();
                                                floodColor(voxel.getPosAsInt(), voxel.getColor(), result);
                                                Integer[] resultArray = new Integer[result.size()];
                                                result.values().toArray(resultArray);
                                                data.massSetColor(resultArray, color);
                                            }
                                        } else {
                                            // get the texture
                                            int selectedTexture = data.getSelectedTexture();
                                            int[] texture = selectedTexture == -1 ? null : new int[] {
                                                    selectedTexture, selectedTexture, selectedTexture,
                                                    selectedTexture, selectedTexture, selectedTexture
                                            };
                                            // flood fill
                                            HashMap<String, int[]> result = new HashMap<String, int[]>();
                                            floodFill(highlighted, result, side == -1 ? 2 : 100, side == -1 ? 27 : 841);
                                            int selectedLayer = data.getSelectedLayer();
                                            Voxel[] resultArray = new Voxel[result.size()];
                                            int i = 0;
                                            for (int[] pos : result.values()) {
                                                resultArray[i++] =
                                                        new Voxel(-1, pos, ColorTools.hsbToColor(currentColor), false, texture, selectedLayer);
                                            }
                                            data.massAddVoxel(resultArray);
                                        }
                                        // last added voxel is center
                                        lastAddedVoxel = highlighted;
                                    }
                                    break;
                                case InputEvent.BUTTON3_DOWN_MASK: // right click
                                    if (!massVoxel && side == -1) {
                                        // memorise position
                                        dragDrawStartPos = highlighted;
                                    }
                                    if (side != -1 || (dragDrawStartPos != null && dragDrawStartPos[1] == highlighted[1])) {

                                        Voxel voxel = data.searchVoxel(highlighted, false);
                                        if (voxel != null) {
                                            Color color = ColorTools.hsbToColor(currentColor);
                                            Color beforeColor = voxel.getColor();
                                            if (!color.equals(beforeColor)) {
                                                // flood recolor <all voxels> of this color (not only connected)
                                                Voxel[] voxels;
                                                switch (side) {
                                                    case -1:
                                                        voxels = data.getVisibleLayerVoxel();
                                                        break;
                                                    case 2:
                                                        voxels = data.getVoxelsYZ(highlighted[0]);
                                                        break;
                                                    case 1:
                                                        voxels = data.getVoxelsXZ(highlighted[1]);
                                                        break;
                                                    case 0:
                                                        voxels = data.getVoxelsXY(highlighted[2]);
                                                        break;
                                                    default:
                                                        voxels = new Voxel[0];
                                                        break;
                                                }
                                                HashSet<Integer> result = new HashSet<Integer>();
                                                for (Voxel vox : voxels) {
                                                    if (vox.getColor().equals(beforeColor)) {
                                                        result.add(vox.id);
                                                    }
                                                }
                                                Integer[] resultArray = new Integer[result.size()];
                                                result.toArray(resultArray);
                                                data.massSetColor(resultArray, color);
                                            }
                                        }
                                        // last added voxel is center
                                        lastAddedVoxel = highlighted;
                                    }
                                    break;
                                default: break;
                            }
                        }
                    } else if (voxelMode == VOXELMODE.ERASE) { // remove voxel
                        Voxel highlightedVoxel = data.searchVoxel(highlighted, false);
                        if (highlightedVoxel != null) {
                            data.removeVoxel(highlightedVoxel.id);
                        }
                    } else if (voxelMode == VOXELMODE.PICKER) {
                        Voxel highlightedVoxel = data.searchVoxel(highlighted, false);
                        if (highlightedVoxel != null) {
                            int[] textureIds = highlightedVoxel.getTexture();
                            if (textureIds == null) {
                                preferences.storeObject("currently_used_color",
                                    ColorTools.colorToHSB(highlightedVoxel.getColor()));
                            }
                            data.selectTextureSoft(textureIds == null ? -1 : textureIds[lastVoxelHitSide]);
                        }
                    } else if (voxelMode == VOXELMODE.COLORCHANGER) {
                        Voxel highlightedVoxel = data.searchVoxel(highlighted, false);
                        if (highlightedVoxel != null) {
                            int selectedTexture = data.getSelectedTexture();
                            if (selectedTexture == -1) {
                                // change color
                                data.setColor(highlightedVoxel.id, ColorTools.hsbToColor(currentColor));
                            } else {
                                int[] textureIds = highlightedVoxel.getTexture();
                                if (textureIds != null && e.isControlDown()) {
                                    // rotate the texture
                                    data.rotateVoxelTexture(highlightedVoxel.id, lastVoxelHitSide);
                                } else if (textureIds != null && e.isShiftDown()) {
                                    // flip the texture
                                    data.flipVoxelTexture(highlightedVoxel.id, lastVoxelHitSide);
                                } else {
                                    // set the texture
                                    data.setTexture(highlightedVoxel.id, lastVoxelHitSide, selectedTexture);
                                }
                            }
                        }
                    } else if (voxelMode == VOXELMODE.VIEW) {
                        // quick select for side view "current" planes
                        preferences.storeObject("currentplane_sideview1", highlighted[2]);
                        preferences.storeObject("currentplane_sideview2", highlighted[1]);
                        preferences.storeObject("currentplane_sideview3", highlighted[0]);
                    }
                    massVoxel = true;
                }
            }
        }

        // execute on mouse event
        protected void executeSelectionMode(MouseEvent e) {
            switch (selectMode) {
                case 1:
                    int x1 = Math.min(selectStartPoint.x, e.getPoint().x);
                    int y1 = Math.min(selectStartPoint.y, e.getPoint().y);
                    int x2 = Math.max(selectStartPoint.x, e.getPoint().x);
                    int y2 = Math.max(selectStartPoint.y, e.getPoint().y);
                    container.setPreviewRect(new Rectangle(x1, y1, x2-x1, y2-y1));
                    break;
                case 2:
                    SimpleVector stopPos = convert2D3D(e.getX(), e.getY(), dragStartReferencePos);
                    // update position of what we dragged
                    data.setVoxelSelectionShift(
                            Math.round(currentSelectionShift[0] - (stopPos.x - dragStartPos.x)/VitcoSettings.VOXEL_SIZE),
                            Math.round(currentSelectionShift[1] - (stopPos.y - dragStartPos.y)/VitcoSettings.VOXEL_SIZE),
                            Math.round(currentSelectionShift[2] - (stopPos.z - dragStartPos.z)/VitcoSettings.VOXEL_SIZE));
                    break;
                default: break;
            }
        }

        // called to select / deselect voxels in rect
        // returns the voxel id that was hit (direct hit)
        protected final Integer finishSelect(MouseEvent e) {
            Integer result = null;
            boolean changedSelection = false;
            Point start = new Point(
                    Math.min(e.getPoint().x, selectStartPoint.x),
                    Math.min(e.getPoint().y, selectStartPoint.y)
            );
            Point stop = new Point(
                    Math.max(e.getPoint().x, selectStartPoint.x),
                    Math.max(e.getPoint().y, selectStartPoint.y)
            );

            List<Integer> searchResult = new LinkedList<Integer>();

            // only search range if start != stop
            if (!start.equals(stop)) {
                Voxel[] voxels = getVoxels();
                // generate the quad tree that we'll use to
                // find the selected voxels
//                //======================
//                // use only visible voxels:
//                Enumeration objects = world.getObjects();
//                while (objects.hasMoreElements()) {
//                    Object3D obj = (Object3D)objects.nextElement();
//                    if (obj.wasVisible()) {
//                        Integer voxelId = world.getVoxelId(obj.getID());
//                        if (voxelId != null) {
//                            Voxel voxel = data.getVoxel(voxelId);
//                            float[] pos = voxel.getPosAsFloat();
//                            SimpleVector vec = convert3D2D(new SimpleVector(
//                                    pos[0] * VitcoSettings.VOXEL_SIZE,
//                                    pos[1] * VitcoSettings.VOXEL_SIZE,
//                                    pos[2] * VitcoSettings.VOXEL_SIZE));
//                            if (vec != null) {
//                                if (vec.x >= start.x && vec.x <= stop.x && vec.y >= start.y && vec.y <= stop.y) {
//                                    searchResult.add(voxel.id);
//                                }
//                            }
//                        }
//                    }
//                }
                //======================
                // use all voxles
                for (Voxel voxel : voxels) {
                    float[] pos = voxel.getPosAsFloat();
                    SimpleVector vec = convert3D2D(new SimpleVector(
                            pos[0] * VitcoSettings.VOXEL_SIZE,
                            pos[1] * VitcoSettings.VOXEL_SIZE,
                            pos[2] * VitcoSettings.VOXEL_SIZE));
                    if (vec != null) {
                        if (vec.x >= start.x && vec.x <= stop.x && vec.y >= start.y && vec.y <= stop.y) {
                            searchResult.add(voxel.id);
                        }
                    }
                }
            }
            // do a single click search as well
            SimpleVector dir = Interact2D.reproject2D3DWS(camera, buffer,
                    e.getX() * VitcoSettings.SAMPLING_MODE_MULTIPLICAND,
                    e.getY() * VitcoSettings.SAMPLING_MODE_MULTIPLICAND).normalize();
            Object[] res = world.calcMinDistanceAndObject3D(camera.getPosition(), dir, 100000);
            if (res[1] != null) { // something hit
                Object3D obj3D = ((Object3D)res[1]);
                Voxel hitVoxel = data.getVoxel(world.getVoxelId(obj3D.getID()));
                if (!searchResult.contains(hitVoxel.id)) {
                    searchResult.add(hitVoxel.id);
                }
                result = hitVoxel.id;
            }

            // execute the select
            Integer[] toSet = new Integer[searchResult.size()];
            searchResult.toArray(toSet);
            if (toSet.length > 0) {
                changedSelection = true;
                data.massSetVoxelSelected(toSet, (e.getModifiers() & MouseEvent.BUTTON1_MASK) == MouseEvent.BUTTON1_MASK);
            }

            container.setPreviewRect(null);
            camera.setEnabled(true);

            if (changedSelection) {
                // reset shift
                data.setVoxelSelectionShift(0,0,0);
            }
            return result;
        }

        // hover on mouse event
        protected final void hover(MouseEvent e) {
            if (voxelMode != VOXELMODE.SELECT) {
                int[] voxelPos = null;
                if (dragDrawStartPos == null) { // normal hover
                    voxelPos = voxelPosForHoverPos(e.getPoint(),
                            (voxelMode == VOXELMODE.DRAW) && !rightMouseDown);
                } else { // find voxel in same plane
                    SimpleVector dir = Interact2D.reproject2D3DWS(camera, buffer,
                            e.getX() * VitcoSettings.SAMPLING_MODE_MULTIPLICAND,
                            e.getY() * VitcoSettings.SAMPLING_MODE_MULTIPLICAND).normalize();
                    // hit nothing, draw preview on zero level
                    if (dir.y > 0.05) { // angle big enough
                        // calculate position
                        float t = (dragDrawStartPos[1]*VitcoSettings.VOXEL_SIZE - camera.getPosition().y) / dir.y;
                        dir.scalarMul(t);
                        SimpleVector pos = camera.getPosition();
                        pos.add(dir);
                        pos.scalarMul(1/VitcoSettings.VOXEL_SIZE);
                        voxelPos = new int[]{Math.round(pos.x),Math.round(pos.y),Math.round(pos.z)};
                    }
                }
                data.highlightVoxel(voxelPos);
            } else {
                switch (selectMode) {
                    case 0:
                    case 2:
                        SimpleVector hitPos = selectedVoxelsWorld.shiftedCollisionPoint(e.getPoint(), buffer);
                        if (hitPos != null && !e.isControlDown()) {
                            dragStartReferencePos = hitPos;
                            container.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
                            selectMode = 2;
                        } else {
                            container.setCursor(Cursor.getDefaultCursor());
                            selectMode = 0;
                        }
                        // remove voxel highlights
                        data.highlightVoxel(null);
                        break;
                    default: break;
                }
            }
        }

        // cancel all active actions
        private void cancelAllActions() {
            dragStartReferencePos = null;
            selectMode = 0;
            massVoxel = false;
            lastAddedVoxel = null;
            container.setPreviewRect(null);
            container.setCursor(Cursor.getDefaultCursor());
        }

        // called when mode changes
        public final void notifyModeChange() {
            if (massVoxelMode != voxelMode) { // cancel if mode changed
                cancelAllActions();
                massVoxelMode = voxelMode;
            }
        }

        //true iff right mouse is down
        private boolean rightMouseDown = false;

        @Override
        public final void mousePressed(final MouseEvent e) {
            asyncActionManager.addAsyncAction(new AsyncAction() {
                @Override
                public void performAction() {
                    rightMouseDown = (e.getModifiers() & MouseEvent.BUTTON3_MASK) == MouseEvent.BUTTON3_MASK;
                    if (rightMouseDown) {
                        hover(e);
                    }
                    // remember voxel mode
                    massVoxelMode = voxelMode;
                    // make sure there is a layer selected or display a warning
                    if (data.getSelectedLayer() == -1) {
                        Integer[] layers = data.getLayers();
                        if (layers.length > 0) {
                            data.selectLayer(layers[0]);
                        } else {
                            console.addLine(langSelector.getString("no_layer_warning"));
                        }
                    }
                    // execute action
                    if (voxelMode == VOXELMODE.SELECT) {
                        camera.setEnabled(false);
                        switch (selectMode) {
                            case 0:
                                selectStartPoint = e.getPoint();
                                selectMode = 1;
                                break;
                            case 2:
                                currentSelectionShift = data.getVoxelSelectionShift().clone();
                                dragStartPos = convert2D3D(e.getX(), e.getY(), dragStartReferencePos);
                                break;
                            default: break;
                        }
                    } else {
                        executeNormalMode(e);
                    }
                }
            });
        }

        @Override
        public final void mouseReleased(final MouseEvent e) {
            asyncActionManager.addAsyncAction(new AsyncAction() {
                @Override
                public void performAction() {
                    camera.setEnabled(true);
                    rightMouseDown = rightMouseDown && (e.getModifiers() & MouseEvent.BUTTON3_DOWN_MASK) == MouseEvent.BUTTON3_DOWN_MASK;
                    massVoxel = false;
                    lastAddedVoxel = null;
                    dragDrawStartPos = null;
                    switch (selectMode) {
                        case 1:
                            Integer hitVoxelId = finishSelect(e);
                            selectMode = 0;
                            if (hitVoxelId != null && data.getVoxel(hitVoxelId).isSelected() && !e.isControlDown()) {
                                // make sure the voxel is correctly selected
                                container.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
                            }
                            break;
                        case 2:
                            selectMode = 0;
                            break;
                        default: break;
                    }
                    hover(e);
                    invalidateVoxels();
                    forceRepaint();
                }
            });
        }

        @Override
        public final void mouseEntered(MouseEvent e) {
            asyncActionManager.addAsyncAction(new AsyncAction() {
                @Override
                public void performAction() {
                    mouseInside = true;
                }
            });
        }

        @Override
        public final void mouseExited(MouseEvent e) {
            asyncActionManager.addAsyncAction(new AsyncAction() {
                @Override
                public void performAction() {
                    mouseInside = false;
                    data.removeVoxelHighlights();
                }
            });
        }

        @Override
        public final void mouseDragged(final MouseEvent e) {
            asyncActionManager.addAsyncAction(new AsyncAction() {
                @Override
                public void performAction() {
                    lastMoveEvent = e;
                    if (massVoxel) {
                        hover(e);
                        executeNormalMode(e);
                    } else {
                        switch (selectMode) {
                            case 1:
                            case 2:
                                executeSelectionMode(e);
                                forceRepaint();
                                break;
                            default: break;
                        }
                    }
                }
            });
        }

        @Override
        public final void mouseMoved(final MouseEvent e) {
            asyncActionManager.addAsyncAction(new AsyncAction() {
                @Override
                public void performAction() {
                    lastMoveEvent = e;
                    hover(e);
                }
            });
        }
    }

    private final ArrayList<KeyListener> modifierListener = new ArrayList<KeyListener>();

    private static boolean initialized = false;

    @PostConstruct
    protected final void init() {

        if (!initialized) {
            initialized = true;
            // register shortcuts for shifting the selection
            actionManager.registerAction("shift_selected_voxels_up", new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (data.anyVoxelSelected()) {
                        int[] shift = data.getVoxelSelectionShift();
                        data.setVoxelSelectionShift(shift[0], shift[1]+1, shift[2]);
                    }
                }
            });
            // register shortcuts for shifting the selection
            actionManager.registerAction("shift_selected_voxels_down", new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (data.anyVoxelSelected()) {
                        int[] shift = data.getVoxelSelectionShift();
                        data.setVoxelSelectionShift(shift[0], shift[1] - 1, shift[2]);
                    }
                }
            });

            // register shortcuts for shifting the selection
            actionManager.registerAction("shift_selected_voxels_left", new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (data.anyVoxelSelected()) {
                        int[] shift = data.getVoxelSelectionShift();
                        data.setVoxelSelectionShift(shift[0], shift[1], shift[2] - 1);
                    }
                }
            });
            // register shortcuts for shifting the selection
            actionManager.registerAction("shift_selected_voxels_right", new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (data.anyVoxelSelected()) {
                        int[] shift = data.getVoxelSelectionShift();
                        data.setVoxelSelectionShift(shift[0], shift[1], shift[2] + 1);
                    }
                }
            });

            // register shortcuts for shifting the selection
            actionManager.registerAction("shift_selected_voxels_out", new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (data.anyVoxelSelected()) {
                        int[] shift = data.getVoxelSelectionShift();
                        data.setVoxelSelectionShift(shift[0] + 1, shift[1], shift[2]);
                    }
                }
            });
            // register shortcuts for shifting the selection
            actionManager.registerAction("shift_selected_voxels_in", new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (data.anyVoxelSelected()) {
                        int[] shift = data.getVoxelSelectionShift();
                        data.setVoxelSelectionShift(shift[0] - 1, shift[1], shift[2]);
                    }
                }
            });
        }

        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(new KeyEventDispatcher() {
            private boolean ctrlDown = false;
            private boolean altDown = false;
            private boolean shiftDown = false;

            @Override
            public boolean dispatchKeyEvent(final KeyEvent e) {
                if (ctrlDown != e.isControlDown() || altDown != e.isAltDown() || shiftDown != e.isShiftDown()) {
                    asyncActionManager.addAsyncAction(new AsyncAction() {
                        @Override
                        public void performAction() {
                            ctrlDown = e.isControlDown();
                            altDown = e.isAltDown();
                            shiftDown = e.isShiftDown();
                            for (KeyListener kl : modifierListener) {
                                kl.keyPressed(e);
                            }
                        }
                    });
                }
                return false;
            }
        });

        // init the voxel adapter
        voxelAdapter.init();

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
                // reset shift of voxels selection
                data.setVoxelSelectionShift(0,0,0);
                invalidateVoxels();
                container.doNotSkipNextWorldRender();
                forceRepaint();
            }

            @Override
            public void onVoxelHighlightingChanged() {
                container.skipNextWorldRender();
                forceRepaint();
            }

            @Override
            public void onVoxelSelectionShiftChanged() {
                int[] shift = data.getVoxelSelectionShift();
                selectedVoxelsWorld.setShift(shift);
                container.doNotSkipNextWorldRender();
                forceRepaint();
            }

            @Override
            public void onTextureDataChanged() {
                invalidateVoxels();
                container.doNotSkipNextWorldRender();
                forceRepaint();
            }

        };
        data.addDataChangeListener(dca);

        preferences.addPrefChangeListener("is_animation_mode_active", new PrefChangeListener() {
            @Override
            public void onPrefChange(Object newValue) {
                boolean isAnimate = (Boolean)newValue;
                if (isAnimate) {
                    data.removeVoxelHighlights();
                    removeAll();
                    container.addMouseMotionListener(animationAdapter);
                    container.addMouseListener(animationAdapter);
                } else {
                    data.removeAnimationHighlights();
                    removeAll();
                    container.addMouseMotionListener(voxelAdapter);
                    container.addMouseListener(voxelAdapter);
                    voxelAdapter.setActive(true);
                    voxelAdapter.replayHover();
                }
                container.setDrawAnimationOverlay(isAnimate);
                container.setDrawSelectedVoxels(!isAnimate);
                forceRepaint();
            }

            private void removeAll() {
                // just to be sure there are no listeners left
                voxelAdapter.setActive(false);
                container.removeMouseMotionListener(voxelAdapter);
                container.removeMouseListener(voxelAdapter);
                container.removeMouseMotionListener(animationAdapter);
                container.removeMouseListener(animationAdapter);
            }
        });
        // initialize listener adapter
        if (!preferences.contains("is_animation_mode_active")) {
            preferences.storeObject("is_animation_mode_active", VitcoSettings.INITIAL_MODE_IS_ANIMATION);
        }

        // register change of voxel mode
        preferences.addPrefChangeListener("active_voxel_submode", new PrefChangeListener() {
            @Override
            public void onPrefChange(Object newValue) {
                voxelMode = (VOXELMODE) newValue;
                voxelAdapter.notifyModeChange();
                voxelAdapter.replayHover();
                forceRepaint();
            }
        });

    }
}
