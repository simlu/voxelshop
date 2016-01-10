package com.vitco.core.data;

import com.vitco.core.data.container.Voxel;
import gnu.trove.set.hash.TIntHashSet;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Defines the voxel data interaction (layer, undo, etc)
 */
public interface VoxelDataInterface {
    // adds a voxel directly to the container (no history entry is created!)
    // only to be used for back imports etc
    int addVoxelDirect(Color color, int[] pos);
    // adds a voxel to current layer and returns voxel id
    int addVoxel(Color color, int[] textureId, int[] pos);
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
    // delete all voxels in layer
    boolean clearV(int layerId);
    // search for voxel in specific layer (no matter if visible or not)
    Voxel searchVoxel(int[] pos, int layerId);
    // returns a voxel if the current layer has a voxel at the position
    // layer needs to be visible
    Voxel searchVoxel(int[] pos, boolean onlyCurrentLayer);
    // get all voxels in current layer
    Voxel[] getLayerVoxels(int layerId);
    // get changed voxels since last call
    Voxel[][] getNewVisibleLayerVoxel(String requestId);
    // get all visible layer voxels
    Voxel[] getVisibleLayerVoxel();
    // true iff any voxels are visible
    boolean anyLayerVoxelVisible();
    // true iff any voxels are visible
    boolean anyVoxelSelected();
    // to invalidate the side view buffer
    void invalidateSideViewBuffer(String requestId, Integer side, Integer plane);
    // get changed side view voxels since last call
    Voxel[][] getNewSideVoxel(String requestId, Integer side, Integer plane);
    // get voxel range (plane) of specific layer (not buffered!)
    Voxel[] getVoxelsXY(int z, int layerId);
    Voxel[] getVoxelsXZ(int y, int layerId);
    Voxel[] getVoxelsYZ(int x, int layerId);
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
    // get the new selected voxels since last call
    Voxel[][] getNewSelectedVoxel(String requestId);
    // select a voxel
    boolean setVoxelSelected(int voxelId, boolean selected);
    // select several voxels at once
    boolean massSetVoxelSelected(Integer[] voxelIds, boolean selected);
    // remove several voxels at once
    boolean massRemoveVoxel(Integer[] voxelIds);
    // add several voxels at once
    boolean massAddVoxel(Voxel[] voxels);
    // migrate voxels to a new layer
    boolean migrateVoxels(Voxel[] voxels);
    // set color of several voxels at once
    boolean massSetColor(Integer[] voxelIds, Color color);
    // shift color of several voxels at once
    boolean massShiftColor(Integer[] voxelIds, float[] hsbOffset);
    // move several voxels at once
    boolean massMoveVoxel(Voxel[] voxel, int[] shift);

    // todo describe functions below

    boolean rotateVoxelCenter(Voxel[] voxel, int axe, float degree);

    boolean mirrorVoxel(Voxel[] voxel, int axe);

    void addTexture(BufferedImage image);

    boolean removeTexture(int textureId);

    boolean removeAllTexture();

    boolean replaceTexture(int textureId, ImageIcon texture);

    Integer[] getTextureList();

    TIntHashSet getVoxelColorList();

    ImageIcon getTexture(Integer textureId);

    String getTextureHash(Integer textureId);

    void selectTexture(int textureId);

    void selectTextureSoft(int textureId);

    int getSelectedTexture();

    boolean setTexture(int voxelId, int voxelSide, int textureId);

    boolean massSetTexture(Integer[] voxelIds, int textureId);

    int[] getVoxelTextureIds(int voxelId);

    boolean flipVoxelTexture(int voxelId, int voxelSide);

    boolean rotateVoxelTexture(int voxelId, int voxelSide);

}