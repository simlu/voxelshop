package com.vitco.logic.menu;

/**
 * Handles the select bar logic.
 */

import com.vitco.engine.data.container.Voxel;
import com.vitco.engine.data.notification.DataChangeAdapter;
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

    private boolean isAnimate = VitcoSettings.INITIAL_MODE_IS_ANIMATION;
    // status of selection moved
    private boolean voxelsAreMoved = true; // todo should be false
    // true iff there are selected voxels
    private boolean voxelsAreSelected = true; // todo should be false
    // true iff there are voxels in layer
    private boolean voxelsAreInLayer = true;

    private Integer[] convertVoxelsToIdArray(Voxel[] voxels) {
        Integer[] voxelIds = new Integer[voxels.length];
        for (int i = 0; i < voxels.length; i++) {
            voxelIds[i] = voxels[i].id;
        }
        return voxelIds;
    }

    public void registerLogic(Frame frame) {
        // stores the current position (keeps current)
        // (used to shift copy + paste correctly in side view)
        final int[] currentPos = new int[3];
        for (int i = 0; i < 3; i++) {
            final int finalI = i;
            preferences.addPrefChangeListener("currentplane_sideview" + (i + 1), new PrefChangeListener() {
                @Override
                public void onPrefChange(Object o) {
                    currentPos[finalI] = (Integer)o;
                }
            });
        }

        // cut, copy, paste
        final ArrayList<Voxel> storedVoxels = new ArrayList<Voxel>();
        final int[] storedPos = new int[3];
        actionGroupManager.addAction("selection_interaction", "selection_tool_cut", new StateActionPrototype() {
            @Override
            public void action(ActionEvent actionEvent) {
                if (getStatus()) {
                    // remember what we cut
                    storedVoxels.clear();
                    Voxel[] voxels = data.getSelectedVoxels();
                    Collections.addAll(storedVoxels, voxels);
                    // remember the current position
                    storedPos[0] = currentPos[0];
                    storedPos[1] = currentPos[1];
                    storedPos[2] = currentPos[2];

                    // fetch voxel ids for cut
                    Integer[] voxelIds = convertVoxelsToIdArray(voxels);
                    // mass delete
                    data.massRemoveVoxel(voxelIds);
                    // refresh status
                    actionGroupManager.refreshGroup("selection_interaction");
                }
            }

            @Override
            public boolean getStatus() {
                return !isAnimate && voxelsAreSelected;
            }
        });
        actionGroupManager.addAction("selection_interaction", "selection_tool_copy", new StateActionPrototype() {
            @Override
            public void action(ActionEvent actionEvent) {
                if (getStatus()) {
                    storedVoxels.clear();
                    Voxel[] voxels = data.getSelectedVoxels();
                    Collections.addAll(storedVoxels, voxels);
                    // remember the current position
                    storedPos[0] = currentPos[0];
                    storedPos[1] = currentPos[1];
                    storedPos[2] = currentPos[2];
                    // refresh status
                    actionGroupManager.refreshGroup("selection_interaction");
                }
            }

            @Override
            public boolean getStatus() {
                return !isAnimate && voxelsAreSelected;
            }
        });
        actionGroupManager.addAction("selection_interaction", "selection_tool_paste", new StateActionPrototype() {
            @Override
            public void action(ActionEvent actionEvent) {
                if (getStatus()) {
                    if (storedVoxels.size() > 0) {
                        Voxel[] voxels = new Voxel[storedVoxels.size()];
                        int[] pos;
                        // compute the shift for all voxels
                        int[] shift = new int[] {
                                storedPos[0] - currentPos[0],
                                storedPos[1] - currentPos[1],
                                storedPos[2] - currentPos[2]
                        };
                        // apply the shift
                        int i = 0;
                        for (Voxel voxel : storedVoxels) {
                            pos = voxel.getPosAsInt().clone();
                            pos[0] -= shift[2];
                            pos[1] -= shift[1];
                            pos[2] -= shift[0];
                            voxels[i++] = new Voxel(voxel.id, pos, voxel.getColor(), voxel.getLayerId());
                        }
                        // execute the (shifted) add
                        if (!data.massAddVoxel(voxels)) {
                            console.addLine(langSelector.getString("min_max_voxel_error"));
                        }
                    }
                }
            }

            @Override
            public boolean getStatus() {
                return !isAnimate && storedVoxels.size() > 0;
            }
        });

        // deselect, delete
        actionGroupManager.addAction("selection_interaction", "selection_tool_deselect", new StateActionPrototype() {
            @Override
            public void action(ActionEvent actionEvent) {
                if (getStatus()) {
                    Integer[] shift = data.getVoxelSelectionShift();
                    if (shift[0] != 0 || shift[1] != 0 || shift[2] != 0) {
                        data.setVoxelSelectionShift(0,0,0);
                    } else {
                        // mass deselect
                        Integer[] voxelIds = convertVoxelsToIdArray(data.getSelectedVoxels());
                        data.massSetVoxelSelected(voxelIds, false);
                    }
                }
            }

            @Override
            public boolean getStatus() {
                return !isAnimate && voxelsAreSelected;
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
                return !isAnimate && voxelsAreSelected;
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
                return !isAnimate && voxelsAreInLayer;
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
                return !isAnimate && voxelsAreSelected;
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
                return !isAnimate && voxelsAreSelected;
            }
        });

        // finalize shifting
        actionGroupManager.addAction("selection_interaction", "selection_tool_finalize_shifting", new StateActionPrototype() {
            @Override
            public void action(ActionEvent actionEvent) {
                if (getStatus()) {
                    // note: shifting will deselect voxels (so no need to do it here)
                    Voxel[] selectedVoxels = data.getSelectedVoxels();
                    Integer[] shift = data.getVoxelSelectionShift();
                    if (selectedVoxels.length > 0 && (shift[0] != 0 || shift[1] != 0 || shift[2] != 0)) {
                        data.massMoveVoxel(data.getSelectedVoxels(), shift);
                    }
                }
            }

            @Override
            public boolean getStatus() {
                return !isAnimate && voxelsAreMoved && voxelsAreSelected;
            }
        });

        // rotate (popup buttons) - only to disable
        StateActionPrototype disableAction = new StateActionPrototype() {
            @Override
            public void action(ActionEvent actionEvent) {
                // nothing to do
            }

            @Override
            public boolean getStatus() {
                return !isAnimate && voxelsAreSelected;
            }
        };
        actionGroupManager.addAction("selection_interaction", "selection_tool_rotatex", disableAction);
        actionGroupManager.addAction("selection_interaction", "selection_tool_rotatey", disableAction);
        actionGroupManager.addAction("selection_interaction", "selection_tool_rotatez", disableAction);

        // rotate buttons, define the actions
        actionGroupManager.addAction("selection_interaction", "selection_tool_mirrorx90", new StateActionPrototype() {
            @Override
            public void action(ActionEvent actionEvent) {
                if (getStatus()) {
                    data.rotateVoxel(data.getSelectedVoxels(), 0, 90);
                }
            }

            @Override
            public boolean getStatus() {
                return !isAnimate && voxelsAreSelected;
            }
        });
        actionGroupManager.addAction("selection_interaction", "selection_tool_mirrorx180", new StateActionPrototype() {
            @Override
            public void action(ActionEvent actionEvent) {
                if (getStatus()) {
                    data.rotateVoxel(data.getSelectedVoxels(), 0, 180);
                }
            }

            @Override
            public boolean getStatus() {
                return !isAnimate && voxelsAreSelected;
            }
        });
        actionGroupManager.addAction("selection_interaction", "selection_tool_mirrorx270", new StateActionPrototype() {
            @Override
            public void action(ActionEvent actionEvent) {
                if (getStatus()) {
                    data.rotateVoxel(data.getSelectedVoxels(), 0, 270);
                }
            }

            @Override
            public boolean getStatus() {
                return !isAnimate && voxelsAreSelected;
            }
        });

        actionGroupManager.addAction("selection_interaction", "selection_tool_mirrory90", new StateActionPrototype() {
            @Override
            public void action(ActionEvent actionEvent) {
                if (getStatus()) {
                    data.rotateVoxel(data.getSelectedVoxels(), 1, 90);
                }
            }

            @Override
            public boolean getStatus() {
                return !isAnimate && voxelsAreSelected;
            }
        });
        actionGroupManager.addAction("selection_interaction", "selection_tool_mirrory180", new StateActionPrototype() {
            @Override
            public void action(ActionEvent actionEvent) {
                if (getStatus()) {
                    data.rotateVoxel(data.getSelectedVoxels(), 1, 180);
                }
            }

            @Override
            public boolean getStatus() {
                return !isAnimate && voxelsAreSelected;
            }
        });
        actionGroupManager.addAction("selection_interaction", "selection_tool_mirrory270", new StateActionPrototype() {
            @Override
            public void action(ActionEvent actionEvent) {
                if (getStatus()) {
                    data.rotateVoxel(data.getSelectedVoxels(), 1, 270);
                }
            }

            @Override
            public boolean getStatus() {
                return !isAnimate && voxelsAreSelected;
            }
        });

        actionGroupManager.addAction("selection_interaction", "selection_tool_mirrorz90", new StateActionPrototype() {
            @Override
            public void action(ActionEvent actionEvent) {
                if (getStatus()) {
                    data.rotateVoxel(data.getSelectedVoxels(), 2, 90);
                }
            }

            @Override
            public boolean getStatus() {
                return !isAnimate && voxelsAreSelected;
            }
        });
        actionGroupManager.addAction("selection_interaction", "selection_tool_mirrorz180", new StateActionPrototype() {
            @Override
            public void action(ActionEvent actionEvent) {
                if (getStatus()) {
                    data.rotateVoxel(data.getSelectedVoxels(), 2, 180);
                }
            }

            @Override
            public boolean getStatus() {
                return !isAnimate && voxelsAreSelected;
            }
        });
        actionGroupManager.addAction("selection_interaction", "selection_tool_mirrorz270", new StateActionPrototype() {
            @Override
            public void action(ActionEvent actionEvent) {
                if (getStatus()) {
                    data.rotateVoxel(data.getSelectedVoxels(), 2, 270);
                }
            }

            @Override
            public boolean getStatus() {
                return !isAnimate && voxelsAreSelected;
            }
        });

        // mirror actions
        actionGroupManager.addAction("selection_interaction", "selection_tool_mirrorx", new StateActionPrototype() {
            @Override
            public void action(ActionEvent actionEvent) {
                if (getStatus()) {
                    data.mirrorVoxel(data.getSelectedVoxels(), 0);
                }
            }

            @Override
            public boolean getStatus() {
                return !isAnimate && voxelsAreSelected;
            }
        });
        actionGroupManager.addAction("selection_interaction", "selection_tool_mirrory", new StateActionPrototype() {
            @Override
            public void action(ActionEvent actionEvent) {
                if (getStatus()) {
                    data.mirrorVoxel(data.getSelectedVoxels(), 1);
                }
            }

            @Override
            public boolean getStatus() {
                return !isAnimate && voxelsAreSelected;
            }
        });
        actionGroupManager.addAction("selection_interaction", "selection_tool_mirrorz", new StateActionPrototype() {
            @Override
            public void action(ActionEvent actionEvent) {
                if (getStatus()) {
                    data.mirrorVoxel(data.getSelectedVoxels(), 2);
                }
            }

            @Override
            public boolean getStatus() {
                return !isAnimate && voxelsAreSelected;
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

        // register data change listener
        data.addDataChangeListener(new DataChangeAdapter() {
            @Override
            public void onVoxelSelectionShiftChanged() {
                Integer[] shift = data.getVoxelSelectionShift();
                boolean voxelsAreMovedTemp = shift[0] != 0 || shift[1] != 0 || shift[2] != 0;
                if (voxelsAreMovedTemp != voxelsAreMoved) {
                    voxelsAreMoved = voxelsAreMovedTemp;
                    actionGroupManager.refreshGroup("selection_interaction");
                }
            }

            @Override
            public void onVoxelDataChanged() {
                boolean voxelsAreSelectedTemp = true;
                boolean voxelsAreInLayerTemp = true;
                if (voxelsAreSelected != voxelsAreSelectedTemp || voxelsAreInLayer != voxelsAreInLayerTemp) {
                    // todo: change definition above to false when fetching real values
                    voxelsAreSelected = voxelsAreSelectedTemp;
                    voxelsAreInLayer = voxelsAreInLayerTemp;
                    actionGroupManager.refreshGroup("selection_interaction");
                }
            }
        });


    }

}

