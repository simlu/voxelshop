package com.vitco.engine.data;

import com.vitco.engine.data.container.Voxel;

import java.awt.*;

/**
 * Defines the voxel data interaction (layer, undo, etc)
 */
public interface VoxelDataInterface {
    // adds a voxel directly to the container (no history entry is created!)
    // only to be used for back imports etc
    int addVoxelDirect(Color color, int[] pos);
    // adds a voxel to current layer and returns voxel id
    int addVoxel(Color color, int[] pos);
    // deleted the voxel
    boolean removeVoxel(int voxelId);
    // move a voxel
    boolean moveVoxel(int voxelId, int[] newPos);
    // retrieve a voxel by id
    Voxel getVoxel(int voxelId);
    // set the color of a voxel
    boolean setColor(int voxelId, Color color);
    // get the color of a voxel
    Color getColor(int voxelId);
    // set the alpha of a voxel
    boolean setAlpha(int voxelId, int alpha);
    // get the alpha of a voxel
    int getAlpha(int voxelId);
    // returns the layer of a voxel
    int getLayer(int voxelId);
    // delete all voxels in area in current layer
    boolean clearRange(int[] center, int rad);
    // set all voxels in an area for current layer (overwrite)
    boolean fillRange(int[] center, int rad, Color color);
    // delete all voxels in layer
    boolean clearV(int layerId);
    // returns a voxel if the current layer has a voxel at the position
    Voxel searchVoxel(int[] pos, boolean onlyCurrentLayer);
    // get all voxels in current layer
    Voxel[] getLayerVoxels(int layerId);
    // get all visible layer voxels
    Voxel[] getVisibleLayerVoxel();
    // get voxel range (plane) of current layer
    Voxel[] getVoxelsXY(int z);
    Voxel[] getVoxelsXZ(int y);
    Voxel[] getVoxelsYZ(int x);
    // get voxel number in layer
    int getVoxelCount(int layerId);

    // undo last action (animation)
    void undoV();
    // redo last action (animation)
    void redoV();
    // return true if the last action can be undone (animation)
    boolean canUndoV();
    // return true if the last action can be redone (animation)
    boolean canRedoV();


    // add a layer
    int createLayer(String layerName);
    // deletes a layer
    boolean deleteLayer(int layerId);
    // rename a layer
    boolean renameLayer(int layerId, String newName);
    // get the name of a layer
    String getLayerName(int layerId);
    // get the names of all layers
    String[] getLayerNames();
    // select a layer
    boolean selectLayer(int layerId);
    // select a layer without producing a history entry
    boolean selectLayerSoft(int layerId);
    // retrieve selected layer
    int getSelectedLayer();
    // retrieve all layers
    Integer[] getLayers();
    // set a layer visible/ invisible
    boolean setVisible(int layerId, boolean b);
    // return true iff layer is visible
    boolean getLayerVisible(int layerId);
    // move layer up
    boolean moveLayerUp(int layerId);
    // move layer down
    boolean moveLayerDown(int layerId);
    // true iff layer can be moved up
    boolean canMoveLayerUp(int layerId);
    // true iff layer can be moved down
    boolean canMoveLayerDown(int layerId);
    // merge visible layers
    boolean mergeVisibleLayers();
    // true iff vis layers can be merged
    boolean canMergeVisibleLayers();

    boolean isSelected(int voxelId);
    Voxel[] getSelectedVoxels();
    // select a voxel
    boolean setVoxelSelected(int voxelId, boolean selected);

    boolean massSetVoxelSelected(Integer[] voxelIds, boolean selected);
}