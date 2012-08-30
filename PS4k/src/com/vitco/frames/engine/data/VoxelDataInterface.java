package com.vitco.frames.engine.data;

import java.awt.*;

/**
 * Defines the voxel data interaction (layer, undo, etc)
 */
public interface VoxelDataInterface {
    // adds a voxel to current layer and returns voxel id
    int addVoxel(Color color, int[] pos);
    // deleted the voxel
    boolean removeVoxel(int voxelId);
    // move a voxel
    boolean moveVoxel(int voxelId, int[] newPos);
    // set the voxel identifier
    boolean setVoxelLinkId(int voxelId, int linkId);
    // retrieve the voxel identifier
    int getVoxelLinkId(int voxelId);
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
    // delete all voxels in current layer
    void clearV();
    // retrieves all voxels of visible layers
    int getAllVoxels();
    // get all voxels in current layer
    int getLayerVoxels();
    // get voxel range (plane) of current layer
    int getVoxelsXY(int z);
    int getVoxelsXZ(int y);
    int getVoxelsYZ(int x);
    // get voxel range for all visible layers (plane)
    int getAllVoxelsXY(int z);
    int getAllVoxelsXZ(int y);
    int getAllVoxelsYZ(int x);


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
    boolean mergeLayers();
}