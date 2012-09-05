package com.vitco.logic.sideview;

import com.jidesoft.action.CommandMenuBar;
import com.threed.jpct.Config;
import com.threed.jpct.SimpleVector;
import com.vitco.engine.EngineInteractionPrototype;
import com.vitco.engine.data.container.Voxel;
import com.vitco.engine.data.notification.DataChangeAdapter;
import com.vitco.res.VitcoSettings;
import com.vitco.util.BiMap;
import com.vitco.util.WorldUtil;
import com.vitco.util.action.ChangeListener;
import com.vitco.util.action.types.StateActionPrototype;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Creates one side view instance (one perspective) and the specific user interaction.
 */
public class SideView extends EngineInteractionPrototype implements SideViewInterface {

    private final int side;

    // resets the view for this side
    private void resetView() {
        switch (side) {
            case 0:
                camera.setView(new SimpleVector(0,-1,-VitcoSettings.SIDE_VIEW_ZOOM_START));
                break;
            case 1:
                camera.setView(new SimpleVector(0,-VitcoSettings.SIDE_VIEW_ZOOM_START,-1));
                break;
            case 2:
                camera.setView(new SimpleVector(-VitcoSettings.SIDE_VIEW_ZOOM_START,0,-1));
                break;
        }
        camera.setFOVLimits(VitcoSettings.SIDE_VIEW_ZOOM_FOV,VitcoSettings.SIDE_VIEW_ZOOM_FOV);
        camera.setFOV(VitcoSettings.SIDE_VIEW_ZOOM_FOV);
    }

    // constructor
    public SideView(int side) {
        this.side = side;
        resetView();
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
                VitcoSettings.VOXEL_SIZE / 2,
                voxel.getColor());
        voxelToObject.put(voxel.id, id);
    }

    private int currentplane = 0;

    // helper
    private void updateWorldWithVoxels() {
        // get the current voxels
        Voxel[] voxels = data.getVisibleLayerVoxel();
        switch (side) {
            case 0:
                voxels = data.getVoxelsXY(currentplane);
                break;
            case 1:
                voxels = data.getVoxelsXZ(currentplane);
                break;
            case 2:
                voxels = data.getVoxelsYZ(currentplane);
                break;
        }

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

    // for voxel interaction
    private final VoxelAdapter drawAdapter = new VoxelAdapter();
    private final class VoxelAdapter extends MouseAdapter implements KeyEventDispatcher {

        @Override
        public void mousePressed(MouseEvent e) {
            if (data.getHighlightedVoxel() != null) {
                data.addVoxel(data.getCurrentColor(),data.getHighlightedVoxel());
            }
        }

        @Override
        public void mouseReleased(MouseEvent e) {

        }

        @Override
        public void mouseEntered(MouseEvent e) {

        }

        @Override
        public void mouseExited(MouseEvent e) {

        }

        @Override
        public boolean dispatchKeyEvent(KeyEvent e) {
            return false;
        }

        @Override
        public void mouseDragged(MouseEvent e) {

        }

        // highlight correct voxel
        @Override
        public void mouseMoved(MouseEvent e) {
            SimpleVector nPos = convert2D3D(e.getX(), e.getY(),
                    new SimpleVector(
                            side == 2 ? currentplane : 0,
                            side == 1 ? currentplane : 0,
                            side == 0 ? currentplane : 0
                    )
            );
            data.highlightVoxel(new int[]{
                    side == 2 ? currentplane : Math.round(nPos.x/VitcoSettings.VOXEL_SIZE),
                    side == 1 ? currentplane : Math.round(nPos.y/VitcoSettings.VOXEL_SIZE),
                    side == 0 ? currentplane : Math.round(nPos.z/VitcoSettings.VOXEL_SIZE)
            });
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

        // make sure we can see into the distance
        world.setClippingPlanes(Config.nearPlane,VitcoSettings.SIDE_VIEW_MAX_ZOOM*2);

        // register clip buttons
        actionManager.registerAction("sideview_move_plane_in" + (side + 1), new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                currentplane++;
                updateWorldWithVoxels();
                container.repaint();
            }
        });
        actionManager.registerAction("sideview_move_plane_out" + (side + 1), new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                currentplane--;
                updateWorldWithVoxels();
                container.repaint();
            }
        });

        // register zoom buttons
        actionManager.registerAction("sideview_zoom_in_tb" + (side + 1), new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                camera.zoomIn(VitcoSettings.SIDE_VIEW_COARSE_ZOOM_SPEED);
                container.repaint();
            }
        });
        actionManager.registerAction("sideview_zoom_out_tb" + (side + 1), new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                camera.zoomOut(VitcoSettings.SIDE_VIEW_COARSE_ZOOM_SPEED);
                container.repaint();
            }
        });

        // register the reset view action
        actionManager.registerAction("sideview_reset_view" + (side + 1), new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                resetView();
                container.repaint();
            }
        });

        // register zoom (mouse wheel)
        camera.setZoomLimits(VitcoSettings.SIDE_VIEW_MIN_ZOOM, VitcoSettings.SIDE_VIEW_MAX_ZOOM);
        container.addMouseWheelListener(new MouseAdapter() {
            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                if (e.getWheelRotation() == -1) {
                    camera.zoomIn(VitcoSettings.SIDE_VIEW_FINE_ZOOM_SPEED);
                } else {
                    camera.zoomOut(VitcoSettings.SIDE_VIEW_FINE_ZOOM_SPEED);
                }
                animationAdapter.mouseMoved(e); // keep selection refreshed (zoom ~ mouse move)
                container.repaint();
            }
        });

        // register shifting
        MouseAdapter shiftingMouseAdapter = new MouseAdapter() {
            private Point mouse_down_point = null;

            @Override
            public void mousePressed(MouseEvent e) {
                if (e.getButton() == 1) {
                    mouse_down_point = e.getPoint();
                } else {
                    mouse_down_point = null;
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                mouse_down_point = null;
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (mouse_down_point != null) {
                    camera.shift((float)(e.getX() - mouse_down_point.getX()),
                            (float)(e.getY() - mouse_down_point.getY()),
                            VitcoSettings.SIDE_VIEW_SIDE_MOVE_FACTOR);
                    mouse_down_point = e.getPoint();
                    container.repaint();
                }
            }
        };
        container.addMouseMotionListener(shiftingMouseAdapter);
        container.addMouseListener(shiftingMouseAdapter);

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
                container.skipNextWorldRender();
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

        // holds the menu and the draw panel (container)
        final JPanel wrapper = new JPanel();
        wrapper.setLayout(new BorderLayout());

        // create menu
        CommandMenuBar menuPanel = new CommandMenuBar();
        menuGenerator.buildMenuFromXML(menuPanel, "com/vitco/logic/sideview/toolbar" + (side + 1) + ".xml");
        menuPanel.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        menuPanel.setBorder(
                // the last menu gets a bottom border
                BorderFactory.createMatteBorder(0, 1, side == 2 ? 1 : 0, 1, VitcoSettings.DEFAULT_BORDER_COLOR)
        );

        // add menu and container
        wrapper.add(menuPanel, BorderLayout.SOUTH);
        wrapper.add(container, BorderLayout.CENTER);

        return wrapper;
    }
}
