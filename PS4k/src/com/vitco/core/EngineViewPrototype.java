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
import com.vitco.manager.async.AsyncAction;
import com.vitco.manager.async.AsyncActionManager;
import com.vitco.manager.pref.PrefChangeListener;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

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
    final EngineViewPrototype thisInstance = this;

    // the container that we draw on (instance)
    protected final DrawContainer container;

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

    @PostConstruct
    public final void startup() {
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
            @Override
            public void mousePressed(MouseEvent e) {
                asyncActionManager.addAsyncAction(new AsyncAction() {
                    @Override
                    public void performAction() {
                        globalMouseDown = true;
                        localMouseDown = true;
                    }
                });
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                asyncActionManager.addAsyncAction(new AsyncAction() {
                    @Override
                    public void performAction() {
                        globalMouseDown = false;
                        localMouseDown = false;
                    }
                });
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
