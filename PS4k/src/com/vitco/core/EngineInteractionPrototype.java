package com.vitco.core;

import com.vitco.core.data.container.VOXELMODE;
import com.vitco.core.data.notification.DataChangeAdapter;
import com.vitco.core.modes.AbstractAdapter;
import com.vitco.core.modes.AnimationAdapter;
import com.vitco.core.modes.VoxelAdapter;
import com.vitco.core.modes.tools.*;
import com.vitco.manager.pref.PrefChangeListener;
import com.vitco.settings.VitcoSettings;

import javax.annotation.PostConstruct;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.HashMap;

/**
 * Defines general (common) interactions available for this engine view and sets them up.
 */
public abstract class EngineInteractionPrototype extends EngineViewPrototype {

    // constructor
    protected EngineInteractionPrototype(Integer side) {
        super(side);
    }

    // list of tool mappings
    private final HashMap<VOXELMODE, AbstractTool> toolMapping = new HashMap<VOXELMODE, AbstractTool>();

    // current mode
    protected VOXELMODE voxelMode = VitcoSettings.INITIAL_VOXEL_MODE;

    // ===============================
    // Animation
    // ===============================

    // animation adapter
    protected final AnimationAdapter animationAdapter = new AnimationAdapter(container);

    // ===============================
    // Voxel
    // ===============================

    // voxel draw adapter for main view
    protected final VoxelAdapter voxelAdapter = new VoxelAdapter(container);

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

            // set the voxel snap preference (if not set)
            if (!preferences.contains("voxel_snap_enabled")) {
                preferences.storeBoolean("voxel_snap_enabled", VitcoSettings.INITIAL_ANIMATION_VOXEL_SNAP);
            }

            // initialize the data mapping for all adapters
            AbstractAdapter.setAsyncActionManager(asyncActionManager);

            // initialize the data mapping for all tools
            AbstractTool.setData(data);
            AbstractTool.setPreferences(preferences);
            AbstractTool.setAsyncActionManager(asyncActionManager);
            AbstractTool.setActionManager(actionManager);
            AbstractTool.setLangSelector(langSelector);
        }

        // initialize the tool mapping
        ViewTool viewTool = new ViewTool(container, side);
        toolMapping.put(VOXELMODE.VIEW, viewTool);
        toolMapping.put(VOXELMODE.DRAW, new DrawTool(container, side));
        toolMapping.put(VOXELMODE.ERASE, new EraseTool(container, side));
        toolMapping.put(VOXELMODE.PICKER, new PickerTool(container, side));
        toolMapping.put(VOXELMODE.COLORCHANGER, new ColorChangerTool(container, side));
        toolMapping.put(VOXELMODE.FLOODFILL, new FloodFillTool(container, side));
        toolMapping.put(VOXELMODE.SELECT, new SelectTool(container, side));
        // make sure the missing modes have the view tool assigned
        for (VOXELMODE mode : VOXELMODE.values()) {
            if (!toolMapping.containsKey(mode)) {
                System.err.println("Tool \"" + mode + "\" uses the default functionality.");
                toolMapping.put(mode, viewTool);
            }
        }
        // set the default mode
        voxelAdapter.setTool(toolMapping.get(voxelMode));

        // set the default animation tool
        animationAdapter.setTool(new PointTool(container, side));

        // what to do when data changes
        DataChangeAdapter dca = new DataChangeAdapter() {

            @Override
            public void onAnimationDataChanged() {
                container.skipNextWorldRender(); // no need to re-render scene
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

            @Override
            public void onOutlineBoxesChanged() {
                container.skipNextWorldRender();
                forceRepaint();
            }

            @Override
            public void onSelectionRectChanged() {
                container.skipNextWorldRender();
                forceRepaint();
            }

        };
        data.addDataChangeListener(dca);

        // logic to switch between voxel and animation modification tools
        preferences.addPrefChangeListener("is_animation_mode_active", new PrefChangeListener() {
            @Override
            public void onPrefChange(Object newValue) {
                boolean isAnimate = (Boolean)newValue;
                if (isAnimate) {
                    data.removeVoxelHighlights();
                    removeAll();
                    container.addMouseMotionListener(animationAdapter);
                    container.addMouseListener(animationAdapter);
                    animationAdapter.activate();
                } else {
                    data.removeAnimationHighlights();
                    removeAll();
                    container.addMouseMotionListener(voxelAdapter);
                    container.addMouseListener(voxelAdapter);
                    voxelAdapter.activate();
                }
                container.setDrawAnimationOverlay(isAnimate);
                container.setDrawSelectedVoxels(!isAnimate);
                // was added since there were some problems with selecting/deselecting in the side view
                container.doNotSkipNextWorldRender();
                forceRepaint();
            }

            private void removeAll() {
                // just to be sure there are no listeners left
                voxelAdapter.deactivate();
                container.removeMouseMotionListener(voxelAdapter);
                container.removeMouseListener(voxelAdapter);
                animationAdapter.deactivate();
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
                voxelAdapter.setTool(toolMapping.get(voxelMode));
                forceRepaint();
            }
        });

    }
}
