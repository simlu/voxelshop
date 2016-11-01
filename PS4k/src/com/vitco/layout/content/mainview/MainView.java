package com.vitco.layout.content.mainview;

import com.jidesoft.action.CommandMenuBar;
import com.threed.jpct.Config;
import com.threed.jpct.Object3D;
import com.threed.jpct.SimpleVector;
import com.threed.jpct.util.Light;
import com.vitco.core.CameraChangeListener;
import com.vitco.core.EngineInteractionPrototype;
import com.vitco.core.data.container.Voxel;
import com.vitco.core.world.WorldManager;
import com.vitco.manager.action.types.KeyActionPrototype;
import com.vitco.manager.action.types.StateActionPrototype;
import com.vitco.manager.async.AsyncAction;
import com.vitco.manager.pref.PrefChangeListener;
import com.vitco.manager.thread.LifeTimeThread;
import com.vitco.manager.thread.ThreadManagerInterface;
import com.vitco.settings.VitcoSettings;
import org.springframework.beans.factory.annotation.Autowired;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.util.Random;
import com.vitco.layout.frames.MainViewLinkage;

/**
 * Creates the main view instance and attaches the specific user interaction.
 */
public class MainView extends EngineInteractionPrototype implements MainViewInterface {

    private ThreadManagerInterface threadManager;
    // set the action handler
    @Autowired
    public final void setThreadManager(ThreadManagerInterface threadManager) {
        this.threadManager = threadManager;
    }

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
        direction.scalarMul(2000000f);
        light.setPosition(direction);
    }

    // true if using "bounding box"
    private boolean useBoundingBox = true;

    // linkage to be able to update mainview's title from
    private MainViewLinkage linkage;
    public void updateTitleWithFileName(String fileName) {
        linkage.updateTitleWithFileName(fileName);
    }

    @Override
    public final JPanel build(MainViewLinkage linkage) {

        this.linkage = linkage;

        // make sure we can see into the distance
        world.setClippingPlanes(Config.nearPlane, VitcoSettings.MAIN_VIEW_ZOOM_OUT_LIMIT * 2);
        selectedVoxelsWorld.setClippingPlanes(Config.nearPlane,VitcoSettings.MAIN_VIEW_ZOOM_OUT_LIMIT*2);

        // toggle shader
        // enable/disable shader
        actionManager.registerAction("toggle_shader_enabled", new AbstractAction() {
            private boolean enabled = false;
            @Override
            public void actionPerformed(ActionEvent e) {
                enabled = !enabled;
                container.enableShader(enabled);
                if (enabled) {
                    console.addLine("Shader is enabled.");
                } else {
                    console.addLine("Shader is disabled.");
                }
                forceRepaint();
            }
        });

        // start/stop test mode (rapid camera rotation)
        actionManager.registerAction("toggle_rapid_camera_testing",new AbstractAction() {

            private boolean active = false;
            private final Random rand = new Random();
            private LifeTimeThread thread;

            private float dirx = 0f;
            private float diry = 0f;

            private final float maxSpeed = 1f;
            private final float speedChange = 0.1f;

            @Override
            public void actionPerformed(ActionEvent e) {
                active = !active;
                if (active) {
                    thread = new LifeTimeThread() {
                        @Override
                        public void loop() throws InterruptedException {
                            asyncActionManager.addAsyncAction(new AsyncAction() {
                                @Override
                                public void performAction() {
                                    dirx = Math.min(maxSpeed, Math.max(-maxSpeed, dirx + (rand.nextFloat() - 0.5f)*speedChange));
                                    diry = Math.min(maxSpeed, Math.max(-maxSpeed, diry + (rand.nextFloat() - 0.5f)*speedChange));
                                    camera.rotate(dirx, diry);
                                    forceRepaint();
                                }
                            });
                            synchronized (this) {
                                thread.wait(50);
                            }
                        }
                    };
                    threadManager.manage(thread);
                    console.addLine("Test activated.");
                } else {
                    threadManager.remove(thread);
                    console.addLine("Test deactivated.");
                }

            }
        });

        // lighting
        final Light dark_light = WorldManager.addLight(world, SimpleVector.ORIGIN, -10);
        final Light light1 = WorldManager.addLight(world, new SimpleVector(-1500000, -2000000, -1000000), 3);
        final Light light2 = WorldManager.addLight(world, new SimpleVector(1500000, 2000000, 1000000), 3);
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
                world.setBorder(gridModeOn);
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


        // =============== BOUNDING BOX
        // add the bounding box (texture)
        final Object3D[] boundingBox = {WorldManager.getGridPlane()};
        final int[] boundingBoxId = {world.addObject(boundingBox[0])};

        // listen to bounding box size changes and change texture object (only in main view)
        // Note: This doesn't need a repaint since that is done in a more general listener
        preferences.addPrefChangeListener("bounding_box_size", new PrefChangeListener() {
            @Override
            public void onPrefChange(Object newValue) {
                // generate new bounding box
                boundingBox[0] = WorldManager.getGridPlane();
                boundingBox[0].setVisibility(useBoundingBox);
                // remove old bounding box
                world.removeObject(boundingBoxId[0]);
                // add new bounding box
                boundingBoxId[0] = world.addObject(boundingBox[0]);
            }
        });

        // listen to bounding box visibility changes
        preferences.addPrefChangeListener("use_bounding_box", new PrefChangeListener() {
            @Override
            public void onPrefChange(Object o) {
                useBoundingBox = (Boolean)o;
                container.setDrawBoundingBox(useBoundingBox); // overlay part
                boundingBox[0].setVisibility(useBoundingBox); // texture part
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
                        switch (e.getModifiers() & (MouseEvent.BUTTON1_MASK | MouseEvent.BUTTON2_MASK | MouseEvent.BUTTON3_MASK)) {
                            case MouseEvent.BUTTON1_MASK: case MouseEvent.BUTTON2_MASK: leftMouseDown = e.getPoint(); break;
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
                        switch (e.getModifiers() & (MouseEvent.BUTTON1_MASK | MouseEvent.BUTTON2_MASK | MouseEvent.BUTTON3_MASK)) {
                            case MouseEvent.BUTTON1_MASK: case MouseEvent.BUTTON2_MASK: leftMouseDown = null; break;
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

        abstract class CameraAction extends KeyActionPrototype {
            protected abstract void step();
            private LifeTimeThread thread = null;

            @Override
            public void onKeyDown() {
                if (thread == null) {
                    thread = new LifeTimeThread() {
                        @Override
                        public void loop() throws InterruptedException {
                            asyncActionManager.addAsyncAction(new AsyncAction() {
                                @Override
                                public void performAction() {
                                    step();
                                    voxelAdapter.replayHover();
                                    forceRepaint();
                                }
                            });
                            synchronized (this) {
                                thread.wait(25);
                            }
                        }
                    };
                    threadManager.manage(thread);
                }
            }

            @Override
            public void onKeyUp() {
                if (thread != null) {
                    threadManager.remove(thread);
                    thread = null;
                }
            }
        }

        // register zoom buttons
        actionManager.registerAction("mainview_zoom_in", new CameraAction() {
            @Override
            protected void step() {
                camera.zoomIn(VitcoSettings.MAIN_VIEW_BUTTON_ZOOM_SPEED);
            }
        });
        actionManager.registerAction("mainview_zoom_out", new CameraAction() {
            @Override
            protected void step() {
                camera.zoomOut(VitcoSettings.MAIN_VIEW_BUTTON_ZOOM_SPEED);
            }
        });
        // register rotate buttons
        actionManager.registerAction("mainview_rotate_left", new CameraAction() {
            @Override
            protected void step() {
                camera.rotate(VitcoSettings.MAIN_VIEW_BUTTON_ROTATE_SIDEWAYS_SPEED, 0);
            }
        });
        actionManager.registerAction("mainview_rotate_right", new CameraAction() {
            @Override
            protected void step() {
                camera.rotate(-VitcoSettings.MAIN_VIEW_BUTTON_ROTATE_SIDEWAYS_SPEED, 0);
            }
        });
        actionManager.registerAction("mainview_rotate_up", new CameraAction() {
            @Override
            protected void step() {
                camera.rotate(0, VitcoSettings.MAIN_VIEW_BUTTON_ROTATE_OVER_SPEED);
            }
        });
        actionManager.registerAction("mainview_rotate_down", new CameraAction() {
            @Override
            protected void step() {
                camera.rotate(0, -VitcoSettings.MAIN_VIEW_BUTTON_ROTATE_OVER_SPEED);
            }
        });
        // register move buttons
        actionManager.registerAction("mainview_move_left", new CameraAction() {
            @Override
            protected void step() {
                camera.shift(1, 0, VitcoSettings.MAIN_VIEW_BUTTON_MOVE_SIDEWAYS_SPEED);
            }
        });
        actionManager.registerAction("mainview_move_right", new CameraAction() {
            @Override
            protected void step() {
                camera.shift(-1, 0, VitcoSettings.MAIN_VIEW_BUTTON_MOVE_SIDEWAYS_SPEED);
            }
        });
        actionManager.registerAction("mainview_move_up", new CameraAction() {
            @Override
            protected void step() {
                camera.shift(0, 1, VitcoSettings.MAIN_VIEW_BUTTON_MOVE_OVER_SPEED);
            }
        });
        actionManager.registerAction("mainview_move_down", new CameraAction() {
            @Override
            protected void step() {
                camera.shift(0, -1, VitcoSettings.MAIN_VIEW_BUTTON_MOVE_OVER_SPEED);
            }
        });

        // register reset action
        actionManager.registerAction("reset_main_view_camera", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                camera.setView(VitcoSettings.MAIN_VIEW_CAMERA_POSITION);
                container.doNotSkipNextWorldRender();
                forceRepaint();
            }
        });

        actionManager.registerAction("center_main_view_camera", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                camera.setCenterShift(new SimpleVector(
                        -preferences.loadInteger("currentplane_sideview3") * VitcoSettings.VOXEL_SIZE,
                        -preferences.loadInteger("currentplane_sideview2") * VitcoSettings.VOXEL_SIZE,
                        -preferences.loadInteger("currentplane_sideview1") * VitcoSettings.VOXEL_SIZE
                ));
                container.doNotSkipNextWorldRender();
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
                container.useWireFrame(useWireFrame);
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
        menuGenerator.buildMenuFromXML(menuPanel, "com/vitco/layout/content/mainview/toolbar.xml");
        // so the background doesn't show
        menuPanel.setOpaque(true);

        // add to wrapper
        wrapper.add(menuPanel, BorderLayout.SOUTH);
        wrapper.add(container, BorderLayout.CENTER);

        return wrapper;
    }

}
