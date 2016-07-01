package com.vitco.layout.content.menu;

/**
 * Handles the select bar logic.
 */

import com.vitco.core.data.container.Voxel;
import com.vitco.core.data.notification.DataChangeAdapter;
import com.vitco.manager.action.types.StateActionPrototype;
import com.vitco.manager.pref.PrefChangeListener;
import com.vitco.settings.VitcoSettings;
import com.vitco.util.misc.ColorTools;
import gnu.trove.set.hash.THashSet;

import javax.annotation.PostConstruct;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

public class SelectBarLogic extends MenuLogicPrototype implements MenuLogicInterface {

    private boolean isAnimate = VitcoSettings.INITIAL_MODE_IS_ANIMATION;
    // status of selection moved
    private boolean voxelsAreMoved = false;
    // true iff there are selected voxels
    private boolean voxelsAreSelected = false;
    // true iff there are voxels in layer
    private boolean voxelsAreInLayer = false;

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
                    Integer[] voxelIds = Voxel.convertVoxelsToIdArray(voxels);
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
                            pos = voxel.getPosAsInt();
                            pos[0] -= shift[2];
                            pos[1] -= shift[1];
                            pos[2] -= shift[0];
                            voxels[i++] = new Voxel(voxel.id, pos, voxel.getColor(),
                                    voxel.isSelected(), voxel.getTexture(), voxel.getLayerId());
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
                    int[] shift = data.getVoxelSelectionShift();
                    if (shift[0] != 0 || shift[1] != 0 || shift[2] != 0) {
                        data.setVoxelSelectionShift(0,0,0);
                    } else {
                        // mass deselect
                        Integer[] voxelIds = Voxel.convertVoxelsToIdArray(data.getSelectedVoxels());
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
                    Integer[] voxelIds = Voxel.convertVoxelsToIdArray(data.getSelectedVoxels());
                    data.massRemoveVoxel(voxelIds);
                }
            }

            @Override
            public boolean getStatus() {
                return !isAnimate && voxelsAreSelected;
            }
        });

        // select all, expand selection, move to new layer, recolor
        actionGroupManager.addAction("selection_interaction", "selection_tool_select_all", new StateActionPrototype() {
            @Override
            public void action(ActionEvent actionEvent) {
                if (getStatus()) {
                    // todo make this an intent (select layer) -> to prevent two history entries
                    // deselect voxels (this is necessary if there are voxel selected that are not in the current layer)
                    Integer[] selected = Voxel.convertVoxelsToIdArray(data.getSelectedVoxels());
                    Integer[] toSelect = Voxel.convertVoxelsToIdArray(data.getLayerVoxels(data.getSelectedLayer()));
                    if (selected.length > 0) {
                        HashSet<Integer> toDeselectList = new HashSet<Integer>(Arrays.asList(selected));
                        HashSet<Integer> toSelectList = new HashSet<Integer>(Arrays.asList(toSelect));
                        toSelectList.removeAll(toDeselectList);
                        toDeselectList.removeAll(Arrays.asList(toSelect));

                        if (!toDeselectList.isEmpty()) {
                            Integer[] toDeselectArray = new Integer[toDeselectList.size()];
                            toDeselectList.toArray(toDeselectArray);
                            data.massSetVoxelSelected(toDeselectArray, false);
                        }

                        toSelect = new Integer[toSelectList.size()];
                        toSelectList.toArray(toSelect);
                    }

                    // select layer
                    if (toSelect.length != 0) {
                        data.massSetVoxelSelected(toSelect, true);
                    }
                }
            }

            @Override
            public boolean getStatus() {
                return !isAnimate && voxelsAreInLayer;
            }
        });
        actionGroupManager.addAction("selection_interaction", "selection_tool_expand_selection", new StateActionPrototype() {
            @Override
            public void action(ActionEvent actionEvent) {
                if (getStatus()) {
                    // extract colors from selected voxels
                    THashSet<Color> colors = new THashSet<Color>();
                    for (Voxel voxel : data.getSelectedVoxels()) {
                        colors.add(voxel.getColor());
                    }
                    // identify which voxels to select
                    ArrayList<Integer> toSelect = new ArrayList<Integer>();
                    for (Voxel voxel : data.getVisibleLayerVoxel()) {
                        if (colors.contains(voxel.getColor())) {
                            toSelect.add(voxel.id);
                        }
                    }
                    // select voxels
                    if (toSelect.size() != 0) {
                        Integer[] toSelectArray = new Integer[toSelect.size()];
                        toSelect.toArray(toSelectArray);
                        data.massSetVoxelSelected(toSelectArray, true);
                    }
                }
            }
            @Override
            public boolean getStatus() {
                return !isAnimate && voxelsAreSelected;
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
                        Integer[] voxelIds = Voxel.convertVoxelsToIdArray(data.getSelectedVoxels());
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
        actionGroupManager.addAction("selection_interaction", "selection_tool_retexture", new StateActionPrototype() {
            @Override
            public void action(ActionEvent actionEvent) {
                if (getStatus()) {
                    Integer[] voxelIds = Voxel.convertVoxelsToIdArray(data.getSelectedVoxels());
                    data.massSetTexture(voxelIds, data.getSelectedTexture());
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
                    int[] shift = data.getVoxelSelectionShift();
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

        // finalize shifting as copy
        actionGroupManager.addAction("selection_interaction", "selection_tool_finalize_shifting_as_copy", new StateActionPrototype() {
            @Override
            public void action(ActionEvent actionEvent) {
                if (getStatus()) {
                    // note: shifting will deselect voxels (so no need to do it here)
                    Voxel[] selectedVoxels = data.getSelectedVoxels();
                    int[] shift = data.getVoxelSelectionShift();

                    if (selectedVoxels.length > 0 && (shift[0] != 0 || shift[1] != 0 || shift[2] != 0)) {

                        // make a copy of the voxels, but shifted
                        Integer[] voxelIds = new Integer[selectedVoxels.length];
                        Voxel[] shiftedVoxels = new Voxel[selectedVoxels.length];
                        for (int i = 0; i < selectedVoxels.length; i++) {
                            Voxel voxel = selectedVoxels[i];
                            voxelIds[i] = voxel.id;
                            shiftedVoxels[i] = new Voxel(-1, new int[] {
                                    voxel.x - shift[0],
                                    voxel.y - shift[1],
                                    voxel.z - shift[2]
                            }, voxel.getColor(), voxel.isSelected(), voxel.getTexture(),  voxel.getLayerId());
                        }

                        // note: the following order makes sense if we want to copy the selection again to another place
                        // add the shifted voxels
                        // execute the (shifted) add
                        if (!data.massAddVoxel(shiftedVoxels)) {
                            console.addLine(langSelector.getString("min_max_voxel_error"));
                        } else {
                            // deselect voxels
                            data.massSetVoxelSelected(voxelIds, false);
                        }
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
                    data.rotateVoxelCenter(data.getSelectedVoxels(), 0, 90);
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
                    data.rotateVoxelCenter(data.getSelectedVoxels(), 0, 180);
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
                    data.rotateVoxelCenter(data.getSelectedVoxels(), 0, 270);
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
                    data.rotateVoxelCenter(data.getSelectedVoxels(), 1, 90);
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
                    data.rotateVoxelCenter(data.getSelectedVoxels(), 1, 180);
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
                    data.rotateVoxelCenter(data.getSelectedVoxels(), 1, 270);
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
                    data.rotateVoxelCenter(data.getSelectedVoxels(), 2, 90);
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
                    data.rotateVoxelCenter(data.getSelectedVoxels(), 2, 180);
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
                    data.rotateVoxelCenter(data.getSelectedVoxels(), 2, 270);
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
                int[] shift = data.getVoxelSelectionShift();
                boolean voxelsAreMovedTemp = shift[0] != 0 || shift[1] != 0 || shift[2] != 0;
                if (voxelsAreMovedTemp != voxelsAreMoved) {
                    voxelsAreMoved = voxelsAreMovedTemp;
                    actionGroupManager.refreshGroup("selection_interaction");
                }
            }

            @Override
            public void onVoxelDataChanged() {
                // todo: rewrite voxel data to make fetching of these feasible (!)
                boolean voxelsAreSelectedTemp = true;
                boolean voxelsAreInLayerTemp = true;
                if (voxelsAreSelected != voxelsAreSelectedTemp || voxelsAreInLayer != voxelsAreInLayerTemp) {
                    voxelsAreSelected = voxelsAreSelectedTemp;
                    voxelsAreInLayer = voxelsAreInLayerTemp;
                    actionGroupManager.refreshGroup("selection_interaction");
                }
            }
        });


    }

}

