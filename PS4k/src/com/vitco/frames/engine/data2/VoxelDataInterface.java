package com.vitco.frames.engine.data2;

import java.awt.*;

/**
 * Interface for voxel and layer interaction
 */
public interface VoxelDataInterface {
    // adds a voxel to current layer
    boolean addVoxel(Color color, int[] pos);
    // deleted the voxel from current layer
    boolean removeVoxel(int[] pos);
    // move a voxel in current layer
    boolean moveVoxel(int[] oldPos, int[] newPos);
    // set the color of a voxel in current layer
    boolean setColor(Color color, int[] pos);
    // delete all voxels in area in current layer
    boolean clearRange(int[] center, int rad);
    // set all voxels in an area for current layer (overwrite)
    boolean fillRange(int[] center, int rad, Color color);
    // delete all voxels in current layer
    void clearV();
    // retrieves all voxels
    int getAllVoxels();
    // get all voxels in current layer
    int getLayerVoxels();
    // get voxel range (plane)
    int getVoxelsXY(int z);
    int getVoxelsXZ(int y);
    int getVoxelsYZ(int x);
    // get voxel range for layer (plane)
    int getVoxelsXY(int z, int layerId);
    int getVoxelsXZ(int y, int layerId);
    int getVoxelsYZ(int x, int layerId);


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
    // select a layer
    boolean setLayer(int layerId);
    // retrieve all layers
    int[] getLayers();
}