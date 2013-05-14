package com.vitco.logic.mainview;

import com.jidesoft.action.CommandMenuBar;
import com.threed.jpct.Config;
import com.threed.jpct.FrameBuffer;
import com.threed.jpct.SimpleVector;
import com.threed.jpct.util.Light;
import com.vitco.async.AsyncAction;
import com.vitco.engine.CameraChangeListener;
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
import java.awt.image.BufferedImage;

/**
 * Creates the main view instance and attaches the specific user interaction.
 */
public class MainView extends EngineInteractionPrototype implements MainViewInterface {

    protected MainView() {
        super(-1);
    }

    // --------------
    // we don't have an ghost overlay to draw
    @Override
    protected SimpleVector[][] getGhostOverlay() {
        return new SimpleVector[0][];
    }
    @Override
    protected boolean updateGhostOverlay() {
        return false;
    }
    // -----------------------

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

    // true if the "grid mode is on"
    private boolean gridModeOn = true;

    // true if the "light is on"
    private boolean staticLightOn = false;
    private void moveLightBehindCamera(Light light) {
        SimpleVector direction = camera.getPosition().normalize();
        direction.scalarMul(2000f);
        light.setPosition(direction);
    }

    // true if using "bounding box"
    private boolean useBoundingBox = true;

    @Override
    public final JPanel build() {

        // make sure we can see into the distance
        world.setClippingPlanes(Config.nearPlane,VitcoSettings.MAIN_VIEW_ZOOM_OUT_LIMIT*2);
        selectedVoxelsWorld.setClippingPlanes(Config.nearPlane,VitcoSettings.MAIN_VIEW_ZOOM_OUT_LIMIT*2);

        // lighting
        final Light dark_light = WorldUtil.addLight(world, SimpleVector.ORIGIN, -10);
        final Light light1 = WorldUtil.addLight(world, new SimpleVector(-1500, -2000, -1000), 3);
        final Light light2 = WorldUtil.addLight(world, new SimpleVector(1500, 2000, 1000), 3);
        camera.addCameraChangeListener(new CameraChangeListener() {
            @Override
            public void onCameraChange() {
                if (!staticLightOn) {
                    moveLightBehindCamera(dark_light);
                }
            }
        });

        if (!preferences.contains("light_mode_active")) {
            preferences.storeBoolean("light_mode_active", staticLightOn);
        }

        // react to changes on the light status
        preferences.addPrefChangeListener("light_mode_active", new PrefChangeListener() {
            @Override
            public void onPrefChange(Object o) {
                staticLightOn = (Boolean) o;
                if (staticLightOn) {
                    dark_light.disable();
                    light1.enable();
                    light2.enable();
                    world.setAmbientLight(0, 0, 0);
                } else {
                    dark_light.enable();
                    light1.disable();
                    light2.disable();
                    world.setAmbientLight(60, 60, 60);
                }
                moveLightBehindCamera(dark_light);
                forceRepaint();
            }
        });

        // react to changes on the light status
        preferences.addPrefChangeListener("light_mode_active", new PrefChangeListener() {
            @Override
            public void onPrefChange(Object o) {
                staticLightOn = (Boolean) o;
                if (staticLightOn) {
                    dark_light.disable();
                    light1.enable();
                    light2.enable();
                    world.setAmbientLight(0, 0, 0);
                } else {
                    dark_light.enable();
                    light1.disable();
                    light2.disable();
                    world.setAmbientLight(60, 60, 60);
                }
                moveLightBehindCamera(dark_light);
                forceRepaint();
            }
        });

        // register the toggle light mode action (always possible)
        actionManager.registerAction("toggle_light_mode", new StateActionPrototype() {
            @Override
            public void action(ActionEvent actionEvent) {
                preferences.storeBoolean("light_mode_active", !staticLightOn);
            }

            @Override
            public boolean getStatus() {
                return !staticLightOn;
            }
        });

        if (!preferences.contains("grid_mode_active")) {
            preferences.storeBoolean("grid_mode_active", gridModeOn);
        }

        // react to changes on the grid status
        preferences.addPrefChangeListener("grid_mode_active", new PrefChangeListener() {
            @Override
            public void onPrefChange(Object o) {
                gridModeOn = (Boolean) o;
                WorldUtil.enableGrid(gridModeOn);
                forceRepaint();
            }
        });

        // register the toggle grid mode action (always possible)
        actionManager.registerAction("toggle_grid_mode", new StateActionPrototype() {
            @Override
            public void action(ActionEvent actionEvent) {
                preferences.storeBoolean("grid_mode_active", !gridModeOn);
            }

            @Override
            public boolean getStatus() {
                return gridModeOn;
            }
        });

        // camera settings
        camera.setFOVLimits(VitcoSettings.MAIN_VIEW_ZOOM_FOV,VitcoSettings.MAIN_VIEW_ZOOM_FOV);
        camera.setFOV(VitcoSettings.MAIN_VIEW_ZOOM_FOV);
        camera.setZoomLimits(VitcoSettings.MAIN_VIEW_ZOOM_IN_LIMIT, VitcoSettings.MAIN_VIEW_ZOOM_OUT_LIMIT);
        camera.setView(VitcoSettings.MAIN_VIEW_CAMERA_POSITION); // camera initial position

        // add ground plane
        final int worldPlane = WorldUtil.addPlane(
                world,
                new SimpleVector(0, VitcoSettings.VOXEL_GROUND_DISTANCE, 0),
                new SimpleVector(0, 0, 0),
                VitcoSettings.VOXEL_GROUND_PLANE_SIZE,
                VitcoSettings.VOXEL_GROUND_PLANE_COLOR,
                0
        );

        // =============== BOUNDING BOX
        // add the bounding box (texture)
        final int boundingBox = WorldUtil.addGridPlane(world);

        preferences.addPrefChangeListener("use_bounding_box", new PrefChangeListener() {
            @Override
            public void onPrefChange(Object o) {
                useBoundingBox = (Boolean)o;
                container.setDrawBoundingBox(useBoundingBox); // overlay part
                world.getObject(boundingBox).setVisibility(useBoundingBox); // texture part
                // default ground plane
                world.getObject(worldPlane).setVisibility(!useBoundingBox);
                // redraw container
                container.doNotSkipNextWorldRender();
                forceRepaint();
            }
        });

        // make sure the preference is set
        if (!preferences.contains("use_bounding_box")) {
            preferences.storeBoolean("use_bounding_box", useBoundingBox);
        }

        actionManager.registerAction("toggle_bounding_box", new StateActionPrototype() {
            @Override
            public void action(ActionEvent actionEvent) {
                preferences.storeBoolean("use_bounding_box", !useBoundingBox);
            }

            @Override
            public boolean getStatus() {
                return useBoundingBox;
            }
        });
        // ===============

        // user mouse input - change camera position
        MouseAdapter mouseAdapter = new MouseAdapter() {
            @Override
            public void mouseWheelMoved(final MouseWheelEvent e) { // scroll = zoom in and out
                asyncActionManager.addAsyncAction(new AsyncAction() {
                    @Override
                    public void performAction() {
                        int rotation = e.getWheelRotation();
                        if (rotation < 0) {
                            camera.zoomIn(Math.abs(rotation) * VitcoSettings.MAIN_VIEW_ZOOM_SPEED_SLOW);
                        } else {
                            camera.zoomOut(rotation * VitcoSettings.MAIN_VIEW_ZOOM_SPEED_SLOW);
                        }
                        voxelAdapter.replayHover();
                        container.doNotSkipNextWorldRender();
                        forceRepaint();
                    }
                });
            }

            private Point leftMouseDown = null;
            private Point rightMouseDown = null;

            @Override
            public void mousePressed(final MouseEvent e) {
                asyncActionManager.addAsyncAction(new AsyncAction() {
                    @Override
                    public void performAction() {
                        switch (e.getModifiers()) {
                            case MouseEvent.BUTTON1_MASK: leftMouseDown = e.getPoint(); break;
                            case MouseEvent.BUTTON3_MASK: rightMouseDown = e.getPoint(); break;
                            default: break;
                        }
                    }
                });
            }

            @Override
            public void mouseReleased(final MouseEvent e) {
                asyncActionManager.addAsyncAction(new AsyncAction() {
                    @Override
                    public void performAction() {
                        switch (e.getModifiers()) {
                            case MouseEvent.BUTTON1_MASK: leftMouseDown = null; break;
                            case MouseEvent.BUTTON3_MASK: rightMouseDown = null; break;
                            default: break;
                        }
                    }
                });
            }

            @Override
            public void mouseDragged(final MouseEvent e) {
                asyncActionManager.addAsyncAction(new AsyncAction() {
                    @Override
                    public void performAction() {
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
                });
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
