package com.vitco.logic.menu;

/**
 * Handles the select bar logic.
 */

import com.vitco.engine.data.container.Voxel;
import com.vitco.res.VitcoSettings;
import com.vitco.util.ColorTools;
import com.vitco.util.action.types.StateActionPrototype;
import com.vitco.util.pref.PrefChangeListener;

import javax.annotation.PostConstruct;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collections;

public class SelectBarLogic extends MenuLogicPrototype implements MenuLogicInterface {

    boolean isAnimate = VitcoSettings.INITIAL_MODE_IS_ANIMATION;

    private Integer[] convertVoxelsToIdArray(Voxel[] voxels) {
        Integer[] voxelIds = new Integer[voxels.length];
        for (int i = 0; i < voxels.length; i++) {
            voxelIds[i] = voxels[i].id;
        }
        return voxelIds;
    }

    public void registerLogic(Frame frame) {
        // cut, copy, paste
        final ArrayList<Voxel> storedVoxels = new ArrayList<Voxel>();
        actionGroupManager.addAction("selection_interaction", "selection_tool_cut", new StateActionPrototype() {
            @Override
            public void action(ActionEvent actionEvent) {
                if (getStatus()) {
                    // remember what we cut
                    storedVoxels.clear();
                    Voxel[] voxels = data.getSelectedVoxels();
                    Collections.addAll(storedVoxels, voxels);

                    // fetch voxel ids for cut
                    Integer[] voxelIds = convertVoxelsToIdArray(voxels);
                    // mass delete
                    data.massRemoveVoxel(voxelIds);
                }
            }

            @Override
            public boolean getStatus() {
                return !isAnimate;
            }
        });
        actionGroupManager.addAction("selection_interaction", "selection_tool_copy", new StateActionPrototype() {
            @Override
            public void action(ActionEvent actionEvent) {
                if (getStatus()) {
                    storedVoxels.clear();
                    Voxel[] voxels = data.getSelectedVoxels();
                    Collections.addAll(storedVoxels, voxels);
                }
            }

            @Override
            public boolean getStatus() {
                return !isAnimate;
            }
        });
        actionGroupManager.addAction("selection_interaction", "selection_tool_paste", new StateActionPrototype() {
            @Override
            public void action(ActionEvent actionEvent) {
                if (getStatus()) {
                    if (storedVoxels.size() > 0) {
                        Voxel[] voxels = new Voxel[storedVoxels.size()];
                        storedVoxels.toArray(voxels);
                        if (!data.massAddVoxel(voxels)) {
                            console.addLine(langSelector.getString("min_max_voxel_error"));
                        }
                    }
                }
            }

            @Override
            public boolean getStatus() {
                return !isAnimate;
            }
        });

        // deselect, delete
        actionGroupManager.addAction("selection_interaction", "selection_tool_deselect", new StateActionPrototype() {
            @Override
            public void action(ActionEvent actionEvent) {
                if (getStatus()) {
                    // mass deselect
                    Integer[] voxelIds = convertVoxelsToIdArray(data.getSelectedVoxels());
                    data.massSetVoxelSelected(voxelIds, false);
                }
            }

            @Override
            public boolean getStatus() {
                return !isAnimate;
            }
        });
        actionGroupManager.addAction("selection_interaction", "selection_tool_delete", new StateActionPrototype() {
            @Override
            public void action(ActionEvent actionEvent) {
                if (getStatus()) {
                    // mass delete
                    Integer[] voxelIds = convertVoxelsToIdArray(data.getSelectedVoxels());
                    data.massRemoveVoxel(voxelIds);
                }
            }

            @Override
            public boolean getStatus() {
                return !isAnimate;
            }
        });

        // select all, move to new layer, recolor
        actionGroupManager.addAction("selection_interaction", "selection_tool_select_all", new StateActionPrototype() {
            @Override
            public void action(ActionEvent actionEvent) {
                if (getStatus()) {
                    // todo make this an intent (select layer)
                    // deselect all voxels
                    Integer[] voxelIds = convertVoxelsToIdArray(data.getSelectedVoxels());
                    data.massSetVoxelSelected(voxelIds, false);

                    // select layer
                    voxelIds = convertVoxelsToIdArray(data.getLayerVoxels(data.getSelectedLayer()));
                    data.massSetVoxelSelected(voxelIds, true);
                }
            }

            @Override
            public boolean getStatus() {
                return !isAnimate;
            }
        });
        actionGroupManager.addAction("selection_interaction", "selection_tool_as_new_layer", new StateActionPrototype() {
            @Override
            public void action(ActionEvent actionEvent) {
                if (getStatus()) {
                    if (!data.migrateVoxels(data.getSelectedVoxels())) {
                        console.addLine(langSelector.getString("min_max_voxel_error"));
                    }
                }
            }

            @Override
            public boolean getStatus() {
                return !isAnimate;
            }
        });
        actionGroupManager.addAction("selection_interaction", "selection_tool_recolor", new StateActionPrototype() {
            @Override
            public void action(ActionEvent actionEvent) {
                if (getStatus()) {
                    if (preferences.contains("currently_used_color")) {
                        Integer[] voxelIds = convertVoxelsToIdArray(data.getSelectedVoxels());
                        Color color = ColorTools.hsbToColor((float[])preferences.loadObject("currently_used_color"));
                        data.massSetColor(voxelIds, color);
                    }
                }
            }

            @Override
            public boolean getStatus() {
                return !isAnimate;
            }
        });

        actionGroupManager.registerGroup("selection_interaction");
    }

    @PostConstruct
    public final void init() {
        // register change of animation mode
        preferences.addPrefChangeListener("is_animation_mode_active", new PrefChangeListener() {
            @Override
            public void onPrefChange(Object newValue) {
                isAnimate = (Boolean) newValue;
                actionGroupManager.refreshGroup("selection_interaction");
            }
        });
    }

}

