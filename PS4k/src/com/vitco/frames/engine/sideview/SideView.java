package com.vitco.frames.engine.sideview;

import com.threed.jpct.Camera;
import com.threed.jpct.SimpleVector;
import com.vitco.frames.engine.EngineInteractionPrototype;
import com.vitco.frames.engine.data.listener.DataChangeListener;
import com.vitco.res.VitcoSettings;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

/**
 * Created with IntelliJ IDEA.
 * User: VM Win 7
 * Date: 8/21/12
 * Time: 2:29 PM
 * To change this template use File | Settings | File Templates.
 */
public class SideView extends EngineInteractionPrototype implements SideViewInterface {

    private final int side;

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

    public SideView(int side) {
        this.side = side;
        resetView();
    }

    @Override
    public JPanel build() {
        // no need to draw the openGL content
        container.setDrawWorld(false);

        container.addMouseMotionListener(animationAdapter);
        container.addMouseListener(animationAdapter);

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
        animationData.addDataChangeListener(new DataChangeListener() {
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
            public void onFrameDataChanged() {
                //...
            }

            @Override
            public void onVoxelDataChanged() {
                //...
            }
        });


        return container;
    }
}
