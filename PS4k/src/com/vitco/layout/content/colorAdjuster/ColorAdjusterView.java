package com.vitco.layout.content.coloradjuster;

import com.jidesoft.action.CommandMenuBar;
import com.vitco.core.data.Data;
import com.vitco.core.data.container.Voxel;
import com.vitco.core.data.notification.DataChangeAdapter;
import com.vitco.layout.content.ViewPrototype;
import com.vitco.layout.content.colorchooser.basic.ColorChangeListener;
import com.vitco.layout.content.colorchooser.components.colorslider.HSBTab;
import com.vitco.manager.action.types.StateActionPrototype;
import com.vitco.util.misc.ColorTools;
import org.springframework.beans.factory.annotation.Autowired;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * Content for the color adjuster frame.
 */
public class ColorAdjusterView extends ViewPrototype implements ColorAdjusterViewInterface {

    // var & setter (can not be interface!!)
    protected Data data;
    @Autowired
    public final void setData(Data data) {
        this.data = data;
    }

    private final HSBTab hsb = new HSBTab();

    private final CommandMenuBar menuPanel = new CommandMenuBar();

    private Integer[] selectedVoxelIds = null;

    private boolean active = false;
    private void setActive(boolean flag) {
        boolean changed = flag != active;
        if (changed) {
            active = flag;
            apply.refresh();
            cancel.refresh();
        }
        data.setFrozen(active);
        if (changed) {  // disable undo/redo buttons correctly
            actionGroupManager.refreshGroup("history_actions");
        }
        if (!active) {
            hsb.setColor(ColorTools.hsbToColor(new float[]{0.5f, 0.5f, 0.5f}));
        }
    }
    private boolean isActive() {
        return active;
    }

    // define the actions
    StateActionPrototype apply = new StateActionPrototype() {
        @Override
        public boolean getStatus() {
            return isActive();
        }

        @Override
        public void action(ActionEvent e) {
            setActive(false);
        }
    };
    StateActionPrototype cancel = new StateActionPrototype() {
        @Override
        public boolean getStatus() {
            return isActive();
        }

        @Override
        public void action(ActionEvent e) {
            setActive(false);
            data.undoV();
            data.undoV();
        }
    };

    @Override
    public JComponent build(Frame frame) {
        setActive(false);

        JPanel wrapper = new JPanel();
        wrapper.setLayout(new BorderLayout());

        // define the menu
        menuGenerator.buildMenuFromXML(menuPanel, "com/vitco/layout/content/coloradjuster/toolbar.xml");
        wrapper.add(menuPanel, BorderLayout.SOUTH);

        actionManager.registerAction("color_adjuster_apply", apply);
        actionManager.registerAction("color_adjuster_cancel", cancel);

        data.addDataChangeListener(new DataChangeAdapter() {
            private void abort() {
                if (isActive()) {
                    cancel.actionPerformed(null);
                }
            }

            @Override
            public void onFrozenAction() {
                super.onFrozenAction();
                abort();
            }

            @Override
            public void onFrozenRedo() {
                super.onFrozenRedo();
                abort();
            }

            @Override
            public void onFrozenUndo() {
                super.onFrozenUndo();
                abort();
            }
        });

        // define the sliders and action
        hsb.addColorChangeListener(new ColorChangeListener() {
            @Override
            public void colorChanged(float[] hsb) {
                data.setFrozen(false);
                boolean active = isActive();
                if (active) { // undo previous color selection
                    data.undoV();
                } else { // check if voxels are selected, memorize and deselect them
                    selectedVoxelIds = Voxel.convertVoxelsToIdArray(data.getSelectedVoxels());
                    active = selectedVoxelIds.length > 0;
                    if (active) {
                        data.massSetVoxelSelected(selectedVoxelIds, false);
                    }
                }
                if (active) { // apply the color shift
                    data.massShiftColor(selectedVoxelIds, new float[]{
                            hsb[0] * 2 - 1f, hsb[1] * 2 - 1f, hsb[2] * 2 - 1f
                    });
                }
                setActive(active);
            }
        });
        wrapper.add(hsb, BorderLayout.CENTER);

        return wrapper;
    }
}
