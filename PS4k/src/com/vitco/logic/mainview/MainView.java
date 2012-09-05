package com.vitco.logic.mainview;

import com.jidesoft.action.CommandMenuBar;
import com.threed.jpct.Interact2D;
import com.threed.jpct.Object3D;
import com.threed.jpct.Primitives;
import com.threed.jpct.SimpleVector;
import com.vitco.engine.EngineInteractionPrototype;
import com.vitco.engine.data.container.Voxel;
import com.vitco.engine.data.notification.DataChangeAdapter;
import com.vitco.res.VitcoSettings;
import com.vitco.util.BiMap;
import com.vitco.util.WorldUtil;
import com.vitco.util.action.ChangeListener;
import com.vitco.util.action.types.StateActionPrototype;

import javax.annotation.PreDestroy;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

/**
 * Creates the mian view instance and attaches the specific user interaction.
 */
public class MainView extends EngineInteractionPrototype implements MainViewInterface {

    // voxel draw adapter for main view
    protected final DrawMouseAdapter drawAdapter = new DrawMouseAdapter();
    protected class DrawMouseAdapter extends MouseAdapter implements KeyEventDispatcher {

        // true means we're currently adding/removing voxels on drag
        private boolean massVoxel = false;

        // current alt/ctrl status (mouse modifiers)
        private boolean shiftDown = false;
        private boolean ctrlDown = false;

        // last hover position
        private Point lastMouseMoved = new Point(-1, -1);

        // execute on mouse event
        private void execute() {
            if (data.getHighlightedVoxel() != null) { // something highlighted
                if (ctrlDown) { // control = add voxel
                    int[] highlightedVoxel = data.getHighlightedVoxel();
                    data.highlightVoxel(null);
                    data.addVoxel(data.getCurrentColor(), highlightedVoxel);
                    massVoxel = true;
                } else if (shiftDown) { // alt = remove voxel
                    Voxel highlightedVoxel = data.searchVoxel(data.getHighlightedVoxel());
                    if (highlightedVoxel != null) {
                        data.removeVoxel(highlightedVoxel.id);
                    }
                    massVoxel = true;
                }
            }
        }

        // hover on mouse event
        private void hover(MouseEvent e) {
            lastMouseMoved = e.getPoint();
            // check if we hit something
            SimpleVector dir = Interact2D.reproject2D3DWS(camera, buffer, e.getX() * 2, e.getY() * 2).normalize();
            Object[] res = world.calcMinDistanceAndObject3D(camera.getPosition(), dir, 10000);
            if (res[1] != null) { // something hit
                Object3D obj3D = ((Object3D)res[1]);
                int[] voxelPos = data.getVoxel(voxelToObject.getKey(obj3D.getID())).getPosAsInt();
                if (!shiftDown) { // alt = select voxel else select next to voxel
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
                            return (int)Math.signum(o1[1] - o2[1]);
                        }
                    });
                    switch ((int)planes.get(0)[0]) {
                        case 1:  voxelPos[1] -= 1; break;
                        case 2:  voxelPos[1] += 1; break;
                        case 3:  voxelPos[0] -= 1; break;
                        case 4:  voxelPos[0] += 1; break;
                        case 5:  voxelPos[2] -= 1; break;
                        case 6:  voxelPos[2] += 1; break;
                    }
                }
                // highlight the voxel (position)
                data.highlightVoxel(voxelPos);
            } else { // hit nothing
                if (!shiftDown) { // not trying to delete
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
                } else { // trying to delete and hit nothing
                    data.highlightVoxel(null);
                }
            }
        }

        @Override
        public void mousePressed(MouseEvent e) {
            execute();
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            massVoxel = false;
            hover(e);
        }

        @Override
        public void mouseExited(MouseEvent e) {
            data.removeVoxelHighlights();
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            if (massVoxel) {
                hover(e);
                execute();
            } else {
                data.removeVoxelHighlights();
            }
        }

        @Override
        public void mouseMoved(MouseEvent e) {
            hover(e);
        }

        // handle change of ctrl and alt
        @Override
        public boolean dispatchKeyEvent(KeyEvent e) {
            if (((e.isShiftDown() && e.getKeyCode() == 16) != shiftDown || (e.isControlDown() && e.getKeyCode() == 17) != ctrlDown)) {
                shiftDown = e.isShiftDown() && e.getKeyCode() == 16;
                ctrlDown = e.isControlDown() && e.getKeyCode() == 17;
                camera.setEnabled(!shiftDown && !ctrlDown);
                hover(new MouseEvent(e.getComponent(), e.getID(), e.getWhen(), e.getModifiers(), lastMouseMoved.x, lastMouseMoved.y, 1, false));
            }
            return false;
        }
    }

    // maps voxel ids to world ids
    protected final BiMap<Integer, Integer> voxelToObject = new BiMap<Integer, Integer>();
    protected final HashMap<Integer, Voxel> idToVoxel = new HashMap<Integer, Voxel>();

    // helper
    private void addVoxelToWorld(Voxel voxel) {
        int id = WorldUtil.addBox(world,
                new SimpleVector(
                        voxel.getPosAsInt()[0] * VitcoSettings.VOXEL_SIZE,
                        voxel.getPosAsInt()[1] * VitcoSettings.VOXEL_SIZE,
                        voxel.getPosAsInt()[2] * VitcoSettings.VOXEL_SIZE),
                VitcoSettings.VOXEL_SIZE/2,
                voxel.getColor());
        voxelToObject.put(voxel.id, id);
    }

    // helper
    private void updateWorldWithVoxels() {
        // get the current voxels
        Voxel[] voxels = data.getVisibleLayerVoxel();

        // temporary to find unneeded objects
        ArrayList<Integer> voxelIds = new ArrayList<Integer>();
        voxelIds.addAll(voxelToObject.keySet());

        // loop over all voxels
        for (Voxel voxel : voxels) {
            voxelIds.remove((Integer)voxel.id);
            if (!voxelToObject.containsKey(voxel.id)) { // add all new voxels
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

    @Override
    public final JPanel build() {

        // enable / disable voxel interaction
        actionManager.performWhenActionIsReady("toggle_animation_mode", new Runnable() {
            @Override
            public void run() {
                ((StateActionPrototype)actionManager.getAction("toggle_animation_mode")).addChangeListener(new ChangeListener() {
                    @Override
                    public void actionFired(boolean b) { // this is fired once on setup
                        if (b) {
                            data.removeVoxelHighlights();
                            container.removeMouseMotionListener(drawAdapter);
                            container.removeMouseListener(drawAdapter);
                            KeyboardFocusManager.getCurrentKeyboardFocusManager().removeKeyEventDispatcher(drawAdapter);
                        } else {
                            container.addMouseMotionListener(drawAdapter);
                            container.addMouseListener(drawAdapter);
                            KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(drawAdapter);
                        }
                        container.repaint();
                    }
                });
            }
        });

        // enable snap
        animationAdapter.setVoxelSnap(true);

        // camera settings
        camera.setZoomLimits(VitcoSettings.MAIN_VIEW_ZOOM_IN_LIMIT, VitcoSettings.MAIN_VIEW_ZOOM_OUT_LIMIT);
        camera.setView(VitcoSettings.MAIN_VIEW_CAMERA_POSITION); // camera initial position

        // lighting
        world.setAmbientLight(50, 50, 50);
        WorldUtil.addLight(world, new SimpleVector(-200, -1300, -200), 3);
        WorldUtil.addLight(world, new SimpleVector(200, 1300, 200), 3);
        WorldUtil.addLight(world, new SimpleVector(1300, 200, 200), 1);
        WorldUtil.addLight(world, new SimpleVector(-1300, -200, -200), 1);

        // add the world plane (ground)
        Object3D plane = Primitives.getPlane(1, VitcoSettings.VOXEL_GROUND_PLANE_SIZE);
        plane.setCulling(false); //show from both sides
        plane.setTransparency(0);
        plane.setAdditionalColor(VitcoSettings.MAIN_VIEW_GROUND_PLANE_COLOR);
        plane.setOrigin(new SimpleVector(0, VitcoSettings.VOXEL_GROUND_DISTANCE, 0));
        plane.rotateX((float)Math.PI/2);
        world.addObject(plane);

        // user mouse input - change camera position
        MouseAdapter mouseAdapter = new MouseAdapter() {
            @Override
            public void mouseWheelMoved(MouseWheelEvent e) { // scroll = zoom in and out
                if (e.getWheelRotation() == -1) {
                    camera.zoomIn(VitcoSettings.MAIN_VIEW_ZOOM_SPEED_SLOW);
                } else {
                    camera.zoomOut(VitcoSettings.MAIN_VIEW_ZOOM_SPEED_SLOW);
                }
                container.repaint();
            }

            private Point leftMouseDown = null;
            private Point rightMouseDown = null;

            @Override
            public void mousePressed(MouseEvent e) {
                switch (e.getButton()) {
                    case 1: leftMouseDown = e.getPoint(); break;
                    case 3: rightMouseDown = e.getPoint(); break;
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                switch (e.getButton()) {
                    case 1: leftMouseDown = null; break;
                    case 3: rightMouseDown = null; break;
                }
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (leftMouseDown != null) {
                    camera.rotate(e.getX() - leftMouseDown.x, e.getY() - leftMouseDown.y);
                    leftMouseDown.x = e.getX();
                    leftMouseDown.y = e.getY();
                    container.repaint();
                } else if (rightMouseDown != null) {
                    camera.shift(e.getX() - rightMouseDown.x, e.getY() - rightMouseDown.y, VitcoSettings.MAIN_VIEW_SIDE_MOVE_FACTOR);
                    rightMouseDown.x = e.getX();
                    rightMouseDown.y = e.getY();
                    container.repaint();
                }
            }
        };
        container.addMouseWheelListener(mouseAdapter);
        container.addMouseMotionListener(mouseAdapter);
        container.addMouseListener(mouseAdapter);

        // register zoom buttons
        actionManager.registerAction("mainview_zoom_in", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                camera.zoomIn(VitcoSettings.MAIN_VIEW_ZOOM_SPEED_FAST);
                container.repaint();
            }
        });
        actionManager.registerAction("mainview_zoom_out", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                camera.zoomOut(VitcoSettings.MAIN_VIEW_ZOOM_SPEED_FAST);
                container.repaint();
            }
        });

        // register voxel "snap" for bone joints
        if (preferences.contains("mainview_voxel_snap_enabled")) { // load previous settings
            animationAdapter.setVoxelSnap(preferences.loadBoolean("mainview_voxel_snap_enabled"));
        }
        actionManager.registerAction("mainview_toggle_voxel_snap", new StateActionPrototype() {
            @Override
            public void action(ActionEvent actionEvent) {
                animationAdapter.setVoxelSnap(!animationAdapter.getVoxelSnap());
            }

            @Override
            public boolean getStatus() {
                return animationAdapter.getVoxelSnap();
            }
        });

        // register reset action
        actionManager.registerAction("reset_main_view_camera", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                camera.setView(VitcoSettings.MAIN_VIEW_CAMERA_POSITION);
                container.repaint();
            }
        });

        // register redraw on animation data change
        data.addDataChangeListener(new DataChangeAdapter() {
            @Override
            public void onAnimationDataChanged() {
                container.skipNextWorldRender(); // no need to re-render scene
                animationAdapter.refresh2DIndex(); // refresh 2D index when data changes
                container.repaint();
            }

            @Override
            public void onAnimationSelectionChanged() {
                container.skipNextWorldRender(); // no need to re-render scene
                container.repaint();
            }

            @Override
            public void onVoxelDataChanged() {
                updateWorldWithVoxels();
                container.doNotSkipNextWorldRender();
                container.repaint();
            }

            @Override
            public void onVoxelSelectionChanged() {
                container.skipNextWorldRender();
                container.repaint();
            }
        });

        // holds menu and render area (container)
        final JPanel wrapper = new JPanel();
        wrapper.setLayout(new BorderLayout());

        // create menu
        CommandMenuBar menuPanel = new CommandMenuBar();
        menuPanel.setOrientation(1); // top down orientation
        menuGenerator.buildMenuFromXML(menuPanel, "com/vitco/logic/mainview/toolbar.xml");
        menuPanel.setBorder(BorderFactory.createMatteBorder(1,0,1,1,VitcoSettings.DEFAULT_BORDER_COLOR));

        // add to wrapper
        wrapper.add(menuPanel, BorderLayout.EAST);
        wrapper.add(container, BorderLayout.CENTER);

        return wrapper;
    }

    @PreDestroy
    public final void savePref() {
        // store "point snap on voxels" setting
        preferences.storeBoolean("mainview_voxel_snap_enabled", animationAdapter.getVoxelSnap());
    }

}
