package com.vitco.engine.world.container;

import com.vitco.engine.data.container.Voxel;

/**
 * Listener interface that is used in case of changing face information.
 */
public interface FaceListener {
    void onAdd(Voxel voxel, int orientation);
    void onRemove(Voxel voxel, int orientation);
    void onRefresh(Voxel voxel, int orientation);
}
