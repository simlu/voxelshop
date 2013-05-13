package com.vitco.logic.menu;

/**
 * Handles the select bar logic.
 */

import com.vitco.engine.data.container.Voxel;
import com.vitco.engine.data.notification.DataChangeAdapter;
import com.vitco.res.VitcoSettings;
import com.vitco.util.ColorTools;
import com.vitco.util.action.ComplexActionManager;
import com.vitco.util.action.types.StateActionPrototype;
import com.vitco.util.colors.PresetColorChooser;
import com.vitco.util.colors.basics.ColorChangeListener;
import com.vitco.util.pref.PrefChangeListener;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class OptionBarLogic extends MenuLogicPrototype implements MenuLogicInterface {

    // var & setter
    private ComplexActionManager complexActionManager;
    @Autowired
    public final void setComplexActionManager(ComplexActionManager complexActionManager) {
        this.complexActionManager = complexActionManager;
    }

    private boolean isAnimate = false;

    private boolean toggleMirrorFlag = false;

    public void registerLogic(Frame frame) {

        // handles mirroring of voxels
        final DataChangeAdapter mirrorToolAdapter = new DataChangeAdapter() {
            // to make sure this can't call itself
            boolean active = false;

            @Override
            public void onVoxelDataChanged() {
                if (!active && !data.canRedoV() && data.canUndoV()) {
                    active = true;
                    HashSet<Integer> toRemove = new HashSet<Integer>();
                    HashSet<Voxel> toAdd = new HashSet<Voxel>();
                    HashMap<Color, HashSet<Integer>> toRecolor = new HashMap<Color, HashSet<Integer>>();
                    // fetch the changed voxel
                    Voxel[][] changed = data.getNewVisibleLayerVoxel("mirror_flag_listener");
                    if (changed[0] != null) {
                        // remove individual voxel
                        for (Voxel remove : changed[0]) {
                            int[] pos = remove.getPosAsInt();
                            if (pos[0] != 0) {
                                pos[0] = -pos[0];
                                Voxel voxel = data.searchVoxel(pos, true);
                                if (voxel != null) {
                                    toRemove.add(voxel.id);
                                }
                            }
                        }
                    } /* else would be "clear all voxel" */
                    for (Voxel added : changed[1]) {
                        int[] pos = added.getPosAsInt();
                        if (pos[0] != 0) {
                            pos[0] = -pos[0];
                            Voxel voxel = data.searchVoxel(pos, false);
                            if (voxel == null) {
                                toAdd.add(new Voxel(-1, pos,
                                        added.getColor(), false,
                                        added.getTexture(),  added.getLayerId()));
                            } else if (voxel.getLayerId() == added.getLayerId()
                                    && voxel.getColor() != added.getColor()) {
                                Color color = added.getColor();
                                HashSet<Integer> voxelIds = toRecolor.get(color);
                                if (voxelIds == null) {
                                    voxelIds = new HashSet<Integer>();
                                    toRecolor.put(color, voxelIds);
                                }
                                voxelIds.add(voxel.id);
                            }
                        }
                    }

                    // remove
                    Integer[] massRemove = new Integer[toRemove.size()];
                    toRemove.toArray(massRemove);
                    data.massRemoveVoxel(massRemove);

                    // add
                    Voxel[] massAdd = new Voxel[toAdd.size()];
                    toAdd.toArray(massAdd);
                    data.massAddVoxel(massAdd);

                    // recolor
                    for (Map.Entry<Color, HashSet<Integer>> entry : toRecolor.entrySet()) {
                        HashSet<Integer> oneColor = entry.getValue();
                        Integer[] massRecolor = new Integer[oneColor.size()];
                        oneColor.toArray(massRecolor);
                        data.massSetColor(massRecolor, entry.getKey());
                    }

                    active = false;
                } else {
                    data.getNewVisibleLayerVoxel("mirror_flag_listener");
                }
            }
        };

        if (!preferences.contains("mirror_flag_active")) {
            preferences.storeBoolean("mirror_flag_active", toggleMirrorFlag);
        }

        // listens to mirror flag events
        preferences.addPrefChangeListener("mirror_flag_active", new PrefChangeListener() {
            @Override
            public void onPrefChange(Object o) {
                toggleMirrorFlag = (Boolean)o;
                // Note: a DataChangeListener can never be in more
                // than once in the notification list
                if (toggleMirrorFlag) {
                    data.getNewVisibleLayerVoxel("mirror_flag_listener");
                    data.addDataChangeListener(mirrorToolAdapter);
                } else {
                    data.removeDataChangeListener(mirrorToolAdapter);
                }
            }
        });

        // register mirror option
        actionManager.registerAction("toggle_mirror_flag", new StateActionPrototype() {
            @Override
            public void action(ActionEvent actionEvent) {
                preferences.storeBoolean("mirror_flag_active", !toggleMirrorFlag);
            }

            @Override
            public boolean getStatus() {
                return !isAnimate; // "enabled"
            }

            @Override
            public boolean isChecked() {
                return toggleMirrorFlag;
            }
        });

        // register change of animation mode
        preferences.addPrefChangeListener("is_animation_mode_active", new PrefChangeListener() {
            @Override
            public void onPrefChange(Object newValue) {
                isAnimate = (Boolean) newValue;
                // make sure the mirror flag is enabled correctly
                ((StateActionPrototype)actionManager.getAction("toggle_mirror_flag")).refresh();
            }
        });
    }

    @PostConstruct
    public final void init() {
        // create the complex action to select the color of the background plane
        PresetColorChooser bgPlaneColorChooser = new PresetColorChooser();
        bgPlaneColorChooser.addColorChangeListener(new ColorChangeListener() {
            @Override
            public void colorChanged(float[] hsb) {
                preferences.storeObject("main_view_ground_plane_color", ColorTools.hsbToColor(hsb));

            }
        });
        complexActionManager.registerAction("pick_color_voxel_ground_plane", bgPlaneColorChooser);

        // create the complex action to select the color of the background
        PresetColorChooser bgColorChooser = new PresetColorChooser();
        if (!preferences.contains("engine_view_bg_color")) {
            preferences.storeObject("engine_view_bg_color", VitcoSettings.ANIMATION_BG_COLOR);
        }
        bgColorChooser.addColorChangeListener(new ColorChangeListener() {
            @Override
            public void colorChanged(float[] hsb) {
                preferences.storeObject("engine_view_bg_color", ColorTools.hsbToColor(hsb));
            }
        });
        complexActionManager.registerAction("pick_color_voxel_bg", bgColorChooser);
    }

}

