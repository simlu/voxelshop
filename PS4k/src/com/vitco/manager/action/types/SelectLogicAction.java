package com.vitco.manager.action.types;

import com.vitco.core.data.container.Voxel;
import java.awt.event.ActionEvent;
import java.util.Arrays;
import java.util.HashSet;

import com.vitco.core.data.Data;

public abstract class SelectLogicAction extends StateActionPrototype {

    public abstract Integer[] getVoxelsToSelect();

    private Data data;

    protected SelectLogicAction(Data data) {
        this.data = data;
    }

    @Override
    public void action(ActionEvent actionEvent) {

        // deselect voxels (this is necessary if there are voxel selected that are not in the current layer)
        Integer[] selected = Voxel.convertVoxelsToIdArray(data.getSelectedVoxels());

        Integer[] toSelect = getVoxelsToSelect();

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

        // select voxels
        if (toSelect.length != 0) {
            data.massSetVoxelSelected(toSelect, true);
        }
    }
}