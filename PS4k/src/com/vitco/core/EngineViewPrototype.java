package com.vitco.core;

import com.threed.jpct.Config;
import com.threed.jpct.Logger;
import com.threed.jpct.SimpleVector;
import com.vitco.Main;
import com.vitco.core.container.DrawContainer;
import com.vitco.core.data.Data;
import com.vitco.core.data.container.Voxel;
import com.vitco.core.world.AbstractCWorld;
import com.vitco.core.world.CWorld;
import com.vitco.layout.content.ViewPrototype;
import com.vitco.layout.content.mainview.components.BoundingBoxDimChooser;
import com.vitco.manager.action.types.StateActionPrototype;
import com.vitco.manager.async.AsyncAction;
import com.vitco.manager.async.AsyncActionManager;
import com.vitco.manager.pref.PrefChangeListener;
import com.vitco.settings.DynamicSettings;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

/**
 * Rendering functionality of this World (data + overlay). Uses a DrawContainer.
 *
 * Defines the basic objects: data, world, camera.
 */
public abstract class EngineViewPrototype extends ViewPrototype {

    // var & setter
    protected AsyncActionManager asyncActionManager;
    @Autowired
    public final void setAsyncActionManager(AsyncActionManager asyncActionManager) {
        this.asyncActionManager = asyncActionManager;
    }

    // var & setter
    protected Data data;
    @Autowired
    public final void setData(Data data) {
        this.data = data;
    }

    // the world-required objects
    protected final AbstractCWorld world;
    protected final AbstractCWorld selectedVoxelsWorld;
    protected final CCamera camera;

    // information about this container
    protected final int side;
    private boolean localMouseDown = false;
    private static boolean globalMouseDown = false;

    // reference
    private final EngineViewPrototype thisInstance = this;

    // the container that we draw on (instance)
    protected final DrawContainer container;

    // true if high resolution rendering active
    private static boolean highQualityActive = true;

    // ===============================
    // Access underlying container
    // ===============================

    // access to the underlying z buffer
    public final int[] getZBuffer() {
        return container.getZBuffer();
    }
    // access to the underlying pixels
    public final int[] getPixels() {
        return container.getPixels();
    }
    // access to the container dimensions
    public final int getHeight() {
        return container.getHeight();
    }
    public final int getWidth() {
        return container.getWidth();
    }
    // get the image rendered in container in high quality
    public BufferedImage getImage() throws Exception {
        return container.getImage();
    }

    // get camera position
    public final float[] getCamPosition() { return this.camera.getCamPosition(); }

    public BufferedImage getImage(final Color bgColor, final boolean addWatermark) throws Exception { return container.getImage(bgColor, addWatermark); }
    public BufferedImage getImage(SimpleVector camPos, final Color bgColor, final boolean addWatermark) throws Exception { return this.getImage(camPos, bgColor, addWatermark, false); }
    public BufferedImage getImage(SimpleVector camPos, final Color bgColor, final boolean addWatermark, final boolean camLookAtOrigin) throws Exception {
        // get camera's initial position
        float[] initialCamPos = this.getCamPosition();
        // set camera position for the "shot"
        this.camera.setPosition(camPos.x, camPos.y, camPos.z);
        // camera look at origin
        if (camLookAtOrigin) this.camera.lookAt(new SimpleVector());

        BufferedImage image = getImage(bgColor, addWatermark);
        // reset camera to it initial position
        this.camera.setView(new SimpleVector(initialCamPos));
        return image;
    }
    // get the depth image
    public BufferedImage getDepthImage() {
        return container.getDepthImage();
    }

    // ==============================
    // updating of world with voxels
    // ==============================

    // overlay ghost voxels
    protected abstract SimpleVector[][] getGhostOverlay();
    // true if overlay has changed since last call
    protected abstract boolean updateGhostOverlay();

    // voxel data getter to be defined
    protected abstract Voxel[] getVoxels();

    // voxel data getter to be defined (for changed voxels since last call)
    protected abstract Voxel[][] getChangedVoxels();

    // changed selected voxels since last call
    protected abstract Voxel[][] getChangedSelectedVoxels();

    // helper - make sure the voxel objects in the world are up to date
    // and also trigger refresh for redraws
    private void updateWorldWithVoxels() {
        // only retrieve the changed voxels
        Voxel[][] changed = getChangedVoxels();
        if (changed[0] == null) { // rebuild
            world.clear();
        } else {
            // remove individual voxel
            for (Voxel remove : changed[0]) {
                world.clearPosition(remove);
            }
        }
        for (Voxel added : changed[1]) {
            world.updateVoxel(added);
        }
        asyncActionManager.addAsyncAction(new AsyncAction("asyncWorld" + side) {
            @Override
            public void performAction() {
                container.doNotSkipNextWorldRender();
                forceRepaint();
                if (!world.refreshWorld()) {
                    asyncActionManager.addAsyncAction(this);
                }
            }
        });

        // only retrieve the changed voxels
        changed = getChangedSelectedVoxels();
        if (changed[0] == null) { // rebuild
            selectedVoxelsWorld.clear();
        } else {
            // remove individual voxel
            for (Voxel remove : changed[0]) {
                selectedVoxelsWorld.clearPosition(remove);
            }
        }
        for (Voxel added : changed[1]) {
            selectedVoxelsWorld.updateVoxel(added);
        }
        asyncActionManager.addAsyncAction(new AsyncAction("asyncSelWorld" + side) {
            @Override
            public void performAction() {
                container.doNotSkipNextWorldRender();
                forceRepaint();
                if (!selectedVoxelsWorld.refreshWorld()) {
                    asyncActionManager.addAsyncAction(this);
                }
            }
        });
    }

    // true iff the world does not need to be updated with voxels
    private boolean worldVoxelCurrent = false;
    // force update of world before next draw
    protected final void invalidateVoxels() {
        if (localMouseDown) { // instant update needed for interaction
            refreshVoxels(true);
        } else {
            worldVoxelCurrent = false;
        }
    }

    // force true: make sure the voxels are valid "right now", no matter what
    private void refreshVoxels(boolean force) {
        if (!worldVoxelCurrent || force) {
            worldVoxelCurrent = true;
            updateWorldWithVoxels();
        }
    }

    // ==============================
    // END: updating of world with voxels
    // ==============================

    // makes sure the repaint doesn't get called too often
    // (only one in queue at a time)
    // Note: we still need this even though repaint() is thread safe,
    // as it still pushes another event on the Swing queue
    protected final void forceRepaint() {
        if (!container.isRepainting()) {
            container.setRepainting(true);
            final boolean skipNextWorldRender = container.isSkipNextWorldRender();
            final boolean doNotSkipNextWorldRender = container.isDoNotSkipNextWorldRender();
            container.resetSkipRenderFlags();
            asyncActionManager.addAsyncAction(new AsyncAction("repaint" + side) {
                @Override
                public void performAction() {
                    if (skipNextWorldRender) {
                        container.skipNextWorldRender();
                    }
                    if (doNotSkipNextWorldRender) {
                        container.doNotSkipNextWorldRender();
                    }
                    container.render();
                    // this is thread save (but will execute with a delay!)
                    // AWT event queue is ok as long as this method is fast to execute
                    container.repaint();
                }

                @Override
                public boolean ready() {
                    return !globalMouseDown || localMouseDown;
                }
            });
        } else {
            container.bufferWorldRenderFlags();
            container.setNeedRepainting(true);
        }
    }

    // true if the action was executed
    private static boolean initialized = false;

    @PostConstruct
    public final void startup() {

        // handle quality changes
        // ------------------------
        // only execute once
        if (!initialized) {
            initialized = true;

            // load/initialize the render quality setting
            if (preferences.contains("high_quality_active")) {
                highQualityActive = preferences.loadBoolean("high_quality_active");
            } else {
                preferences.storeBoolean("high_quality_active", highQualityActive);
            }

            // change variables accordingly (this is added first and hence always "notified" first)
            preferences.addPrefChangeListener("high_quality_active", new PrefChangeListener() {
                @Override
                public void onPrefChange(Object o) {
                    DynamicSettings.setSamplingMode(highQualityActive);
                }
            });

            // register button change
            actionManager.registerAction("toggle_render_quality", new StateActionPrototype() {
                @Override
                public void action(ActionEvent actionEvent) {
                    // toggle and store
                    highQualityActive = !highQualityActive;
                    preferences.storeBoolean("high_quality_active", highQualityActive);
                }

                @Override
                public boolean getStatus() {
                    return highQualityActive;
                }
            });

            // load the bounding box size (if stored)
            if (preferences.contains("bounding_box_size")) {
                int[] bounding_box_size = (int[]) preferences.loadObject("bounding_box_size");
                DynamicSettings.setPlaneSizeX(bounding_box_size[0]);
                DynamicSettings.setPlaneSizeY(bounding_box_size[1]);
                DynamicSettings.setPlaneSizeZ(bounding_box_size[2]);
            }

            // register component to change bounding box size
            complexActionManager.registerAction("resize_bounding_box_component",
                    new BoundingBoxDimChooser(DynamicSettings.VOXEL_PLANE_SIZE_X, DynamicSettings.VOXEL_PLANE_SIZE_Y, DynamicSettings.VOXEL_PLANE_SIZE_Z, langSelector) {
                private void updateBoundingBox() {
                    // store size in preferences
                    preferences.storeObject("bounding_box_size", new int[]{
                            DynamicSettings.VOXEL_PLANE_SIZE_X,
                            DynamicSettings.VOXEL_PLANE_SIZE_Y,
                            DynamicSettings.VOXEL_PLANE_SIZE_Z
                    });
                }

                // -- below are change listeners

                @Override
                public void onXChange(int newVal) {
                    DynamicSettings.setPlaneSizeX(newVal);
                    updateBoundingBox();
                }

                @Override
                public void onYChange(int newVal) {
                    DynamicSettings.setPlaneSizeY(newVal);
                    updateBoundingBox();
                }

                @Override
                public void onZChange(int newVal) {
                    DynamicSettings.setPlaneSizeZ(newVal);
                    updateBoundingBox();
                }
            });

        }

        // initialize the container
        // ---------------------------
        // register bg color change
        preferences.addPrefChangeListener("engine_view_bg_color", new PrefChangeListener() {
            @Override
            public void onPrefChange(Object newValue) {
                container.setBgColor((Color)newValue);
                forceRepaint();
            }
        });
        // register preview plane change
        preferences.addPrefChangeListener("engine_view_voxel_preview_plane", new PrefChangeListener() {
            @Override
            public void onPrefChange(Object newValue) {
                container.setPreviewPlane((Integer)newValue);
            }
        });
        // register camera change listener
        camera.addCameraChangeListener(new CameraChangeListener() {
            @Override
            public void onCameraChange() {
                container.setCameraChanged(true);
            }
        });

        // initialize basic parameters
        container.setCamera(camera);
        container.setSelectedVoxelsWorld(selectedVoxelsWorld);
        container.setWorld(world);
        container.setAsyncActionManager(asyncActionManager);
        container.setData(data);

        // final initialization
        container.init();

        // force rebuilding and repainting of this container when the quality changes
        // Note: This requires the async action manager to be set (!)
        preferences.addPrefChangeListener("high_quality_active", new PrefChangeListener() {
            @Override
            public void onPrefChange(Object o) {
                container.refreshBuffer();
                forceRepaint();
            }
        });

        // force repainting of this container when the bounding box size changes
        preferences.addPrefChangeListener("bounding_box_size", new PrefChangeListener() {
            @Override
            public void onPrefChange(Object newValue) {
                forceRepaint();
            }
        });
    }

    @PreDestroy
    public final void cleanup() {
        container.cleanup();
    }

    // only perform these actions once (even if the class is instantiated several times)
    static {
        Config.fadeoutLight = false;
        //Config.maxPolysVisible = 10000;

        Config.useMultipleThreads = true;
        Config.maxNumberOfCores = Runtime.getRuntime().availableProcessors();
        Config.loadBalancingStrategy = 1; // default 0
        // usually not worth it (http://www.jpct.net/doc/com/threed/jpct/Config.html#useMultiThreadedBlitting)
        Config.useMultiThreadedBlitting = true;   //default false

        // not really needed (and is very slow when using images, i.e. large textures 512x512)
        // Config.mipmap = true;

        // disable anti-aliasing for textures
        Config.texelFilter = false;

        Config.mtDebug = false;
        if (Main.isDebugMode()) {
            // for debug mode show also warnings
            Logger.setLogLevel(Logger.LL_ERRORS_AND_WARNINGS);
        } else {
            Logger.setLogLevel(Logger.LL_ONLY_ERRORS);
        }
    }

    // constructor
    protected EngineViewPrototype(Integer side) {
        // make sure side defaults to -1
        if (side == null || side < 0 || side > 2) {
            side = -1;
        }
        this.side = side;

        // initialize the container
        container = new DrawContainer(side) {
            @Override
            protected void forceRepaint() {
                thisInstance.forceRepaint();
            }

            @Override
            protected boolean updateGhostOverlay() {
                return thisInstance.updateGhostOverlay();
            }

            @Override
            protected SimpleVector[][] getGhostOverlay() {
                return thisInstance.getGhostOverlay();
            }

            @Override
            protected void refreshVoxels(boolean b) {
                thisInstance.refreshVoxels(b);
            }
        };

        // handle mouse events for this container
        container.addMouseListener(new MouseAdapter() {
            private void handleMouseState(final MouseEvent e, final boolean flag) {
                asyncActionManager.addAsyncAction(new AsyncAction() {
                    @Override
                    public void performAction() {
                        // only consider mouse events that don't involve middle mouse (camera)
                        if ((e.getModifiers() & MouseEvent.BUTTON2_MASK) == 0) {
                            localMouseDown = globalMouseDown = flag;
                        }
                    }
                });
            }

            @Override
            public void mousePressed(MouseEvent e) {
                handleMouseState(e, true);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                handleMouseState(e, false);
            }
        });

        // NOTE: The sum should be not more than 40k (!)
        // define the max poly count for this world
        Config.maxPolysVisible = side == -1 ? 10000 : 2500;
        // set up world objects
        world = new CWorld(true, side, false);

        // define the max poly count for this selected world
        Config.maxPolysVisible = side == -1 ? 10000 : 2500;
        // no culling, since we want to see all selected voxels (in the main view only)
        selectedVoxelsWorld = new CWorld(side != -1, side, true);

        // define camera
        camera = new CCamera();
        world.setCameraTo(camera);
        selectedVoxelsWorld.setCameraTo(camera);

        // lighting (1,1,1) = true color
        world.setAmbientLight(1, 1, 1);

    }
}
