package com.vitco.logic.sideview;

import com.jidesoft.action.CommandMenuBar;
import com.threed.jpct.Config;
import com.threed.jpct.SimpleVector;
import com.vitco.engine.EngineInteractionPrototype;
import com.vitco.engine.data.container.VOXELMODE;
import com.vitco.engine.data.container.Voxel;
import com.vitco.res.VitcoSettings;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

/**
 * Creates one side view instance (one perspective) and the specific user interaction.
 */
public class SideView extends EngineInteractionPrototype implements SideViewInterface {

    private final int side;

    // resets the view for this side
    private void resetView() {
        switch (side) {
            case 0:
                camera.setView(VitcoSettings.SIDE_VIEW1_CAMERA_POSITION);
                break;
            case 1:
                camera.setView(VitcoSettings.SIDE_VIEW2_CAMERA_POSITION);
                break;
            case 2:
                camera.setView(VitcoSettings.SIDE_VIEW3_CAMERA_POSITION);
                break;
        }
        camera.setFOVLimits(VitcoSettings.SIDE_VIEW_ZOOM_FOV,VitcoSettings.SIDE_VIEW_ZOOM_FOV);
        camera.setFOV(VitcoSettings.SIDE_VIEW_ZOOM_FOV);
    }

    // constructor
    public SideView(int side) {
        this.side = side;
        resetView();
        // set the adapter with the altered behalvior
        voxelAdapter = new VoxelAdapterSideView();
    }

    // the current depth of the plane that is shown
    private int currentplane = 0;

    // get the voxels to render
    @Override
    protected Voxel[] getVoxels() {
        // get the current voxels
        Voxel[] voxels = null;
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
        return voxels;
    }

    // get the reference point depending on the selected layer
    @Override
    protected SimpleVector getRefPoint() {
        return new SimpleVector(
                side == 2 ? currentplane*VitcoSettings.VOXEL_SIZE : 0,
                side == 1 ? currentplane*VitcoSettings.VOXEL_SIZE : 0,
                side == 0 ? currentplane*VitcoSettings.VOXEL_SIZE : 0
        );
    }

    // alter the behavior (always use position - not next to voxel)
    private final class VoxelAdapterSideView extends EngineInteractionPrototype.VoxelAdapter {
        // hover on mouse event
        @Override
        protected void hover(Point point) {
            if (voxelMode != VOXELMODE.VIEW) {
                // calculate position
                SimpleVector nPos = convert2D3D((int)Math.round(point.getX()), (int)Math.round(point.getY()),
                        new SimpleVector(
                                side == 2 ? currentplane : 0,
                                side == 1 ? currentplane : 0,
                                side == 0 ? currentplane : 0
                        )
                );
                int[] pos = new int[]{
                        side == 2 ? currentplane : Math.round(nPos.x/VitcoSettings.VOXEL_SIZE),
                        side == 1 ? currentplane : Math.round(nPos.y/VitcoSettings.VOXEL_SIZE),
                        side == 0 ? currentplane : Math.round(nPos.z/VitcoSettings.VOXEL_SIZE)
                };
                Voxel voxel = data.searchVoxel(pos, true);
                if (voxel != null || VOXELMODE.DRAW == voxelMode) {
                    data.highlightVoxel(pos);
                } else {
                    data.highlightVoxel(null);
                }
            }
        }
    }

    @Override
    public final JPanel build() {

        // make sure we can see into the distance
        world.setClippingPlanes(Config.nearPlane,VitcoSettings.SIDE_VIEW_MAX_ZOOM*2);

        // register clip buttons
        actionManager.registerAction("sideview_move_plane_in" + (side + 1), new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                currentplane--;
                invalidateVoxels();
                forceRepaint();
            }
        });
        actionManager.registerAction("sideview_move_plane_out" + (side + 1), new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                currentplane++;
                invalidateVoxels();
                forceRepaint();
            }
        });

        // register zoom buttons
        actionManager.registerAction("sideview_zoom_in_tb" + (side + 1), new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                camera.zoomIn(VitcoSettings.SIDE_VIEW_COARSE_ZOOM_SPEED);
                forceRepaint();
            }
        });
        actionManager.registerAction("sideview_zoom_out_tb" + (side + 1), new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                camera.zoomOut(VitcoSettings.SIDE_VIEW_COARSE_ZOOM_SPEED);
                forceRepaint();
            }
        });

        // register the reset view action
        actionManager.registerAction("sideview_reset_view" + (side + 1), new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                resetView();
                forceRepaint();
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
                forceRepaint();
            }
        });

        // register shifting and preview plane
        MouseAdapter shiftingMouseAdapter = new MouseAdapter() {
            // preview plane
            // =======================
            @Override
            public void mouseEntered(MouseEvent e) {
                data.setPreviewPlane(side*2);
            }
            @Override
            public void mouseExited(MouseEvent e) {
                data.setPreviewPlane(-1);
            }

            // shifting
            // =======================
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
                    forceRepaint();
                }
            }
        };
        container.addMouseMotionListener(shiftingMouseAdapter);
        container.addMouseListener(shiftingMouseAdapter);

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
