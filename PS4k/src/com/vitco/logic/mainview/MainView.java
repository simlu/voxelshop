package com.vitco.logic.mainview;

import com.jidesoft.action.CommandMenuBar;
import com.threed.jpct.Config;
import com.threed.jpct.SimpleVector;
import com.vitco.engine.EngineInteractionPrototype;
import com.vitco.engine.data.container.Voxel;
import com.vitco.res.VitcoSettings;
import com.vitco.util.WorldUtil;
import com.vitco.util.action.types.StateActionPrototype;
import com.vitco.util.pref.PrefChangeListener;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

/**
 * Creates the main view instance and attaches the specific user interaction.
 */
public class MainView extends EngineInteractionPrototype implements MainViewInterface {

    protected MainView() {
        super(-1);
    }

    @Override
    protected Voxel[] getVoxels() {
        return data.getVisibleLayerVoxel();
    }

    @Override
    protected Voxel[][] getChangedVoxels() {
        return data.getNewVisibleLayerVoxel("main_view");
    }

    @Override
    protected Voxel[][] getChangedSelectedVoxels() {
        return data.getNewSelectedVoxel("main_view");
    }

    @Override
    public final JPanel build() {

        // make sure we can see into the distance
        world.setClippingPlanes(Config.nearPlane,VitcoSettings.MAIN_VIEW_ZOOM_OUT_LIMIT*2);
        selectedVoxelsWorld.setClippingPlanes(Config.nearPlane,VitcoSettings.MAIN_VIEW_ZOOM_OUT_LIMIT*2);

        // camera settings
        camera.setFOVLimits(VitcoSettings.MAIN_VIEW_ZOOM_FOV,VitcoSettings.MAIN_VIEW_ZOOM_FOV);
        camera.setFOV(VitcoSettings.MAIN_VIEW_ZOOM_FOV);
        camera.setZoomLimits(VitcoSettings.MAIN_VIEW_ZOOM_IN_LIMIT, VitcoSettings.MAIN_VIEW_ZOOM_OUT_LIMIT);
        camera.setView(VitcoSettings.MAIN_VIEW_CAMERA_POSITION); // camera initial position

        world.setAmbientLight(0, 0, 0); // compensate a bit for the lights
        WorldUtil.addLight(world, new SimpleVector(-1500, -2000, -1000), 3);
        WorldUtil.addLight(world, new SimpleVector(1500, 2000, 1000), 3);

        // add ground plane
        final int worldPlane = WorldUtil.addPlane(
                world,
                new SimpleVector(0, VitcoSettings.VOXEL_GROUND_DISTANCE, 0),
                new SimpleVector(0, 0, 0),
                VitcoSettings.VOXEL_GROUND_PLANE_SIZE,
                VitcoSettings.VOXEL_GROUND_PLANE_COLOR,
                0
        );

        // user mouse input - change camera position
        MouseAdapter mouseAdapter = new MouseAdapter() {
            @Override
            public void mouseWheelMoved(MouseWheelEvent e) { // scroll = zoom in and out
                if (e.getWheelRotation() == -1) {
                    camera.zoomIn(VitcoSettings.MAIN_VIEW_ZOOM_SPEED_SLOW);
                } else {
                    camera.zoomOut(VitcoSettings.MAIN_VIEW_ZOOM_SPEED_SLOW);
                }
                forceRepaint();
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
                    container.doNotSkipNextWorldRender();
                    forceRepaint();
                } else if (rightMouseDown != null) {
                    camera.shift(e.getX() - rightMouseDown.x, e.getY() - rightMouseDown.y, VitcoSettings.MAIN_VIEW_SIDE_MOVE_FACTOR);
                    rightMouseDown.x = e.getX();
                    rightMouseDown.y = e.getY();
                    container.doNotSkipNextWorldRender();
                    forceRepaint();
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
                forceRepaint();
            }
        });
        actionManager.registerAction("mainview_zoom_out", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                camera.zoomOut(VitcoSettings.MAIN_VIEW_ZOOM_SPEED_FAST);
                forceRepaint();
            }
        });

        // register reset action
        actionManager.registerAction("reset_main_view_camera", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                camera.setView(VitcoSettings.MAIN_VIEW_CAMERA_POSITION);
                forceRepaint();
            }
        });

        // register "align view to side plane" actions
        actionManager.registerAction("align_main_to_sideview1", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                SimpleVector pos = new SimpleVector(VitcoSettings.SIDE_VIEW1_CAMERA_POSITION);
                pos.makeEqualLength(camera.getPosition());
                camera.setView(pos);
                forceRepaint();
            }
        });
        actionManager.registerAction("align_main_to_sideview2", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                SimpleVector pos = new SimpleVector(VitcoSettings.SIDE_VIEW2_CAMERA_POSITION);
                pos.makeEqualLength(camera.getPosition());
                camera.setView(pos);
                forceRepaint();
            }
        });
        actionManager.registerAction("align_main_to_sideview3", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                SimpleVector pos = new SimpleVector(VitcoSettings.SIDE_VIEW3_CAMERA_POSITION);
                pos.makeEqualLength(camera.getPosition());
                camera.setView(pos);
                forceRepaint();
            }
        });

        // register button action for wireframe toggle

        actionManager.registerAction("main_window_toggle_wireframe", new StateActionPrototype() {
            private boolean useWireFrame = false; // always false on startup
            @Override
            public void action(ActionEvent actionEvent) {
                useWireFrame = !useWireFrame;
                useWireFrame(useWireFrame);
                //world.getObject(worldPlane).setVisibility(!useWireFrame);
                forceRepaint();
            }

            @Override
            public boolean getStatus() {
                return useWireFrame;
            }
        });

        // holds menu and render area (container)
        final JPanel wrapper = new JPanel();
        wrapper.setLayout(new BorderLayout());

        // prevent "flickering" when swapping windows
        preferences.addPrefChangeListener("engine_view_bg_color", new PrefChangeListener() {
            @Override
            public void onPrefChange(Object o) {
                wrapper.setBackground((Color) o);
            }
        });

        // create menu
        CommandMenuBar menuPanel = new CommandMenuBar();
        //menuPanel.setOrientation(1); // top down orientation
        menuPanel.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        menuGenerator.buildMenuFromXML(menuPanel, "com/vitco/logic/mainview/toolbar.xml");
        // so the background doesn't show
        menuPanel.setOpaque(true);

        // register color change event of ground plane
        preferences.addPrefChangeListener("main_view_ground_plane_color", new PrefChangeListener() {
            @Override
            public void onPrefChange(Object o) {
                world.getObject(worldPlane).setAdditionalColor((Color)o);
                forceRepaint();
            }
        });

        // add to wrapper
        wrapper.add(menuPanel, BorderLayout.SOUTH);
        wrapper.add(container, BorderLayout.CENTER);

        return wrapper;
    }

}
