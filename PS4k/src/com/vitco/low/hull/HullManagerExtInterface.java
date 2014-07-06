package com.vitco.low.hull;

import com.threed.jpct.SimpleVector;

/**
 * Interface - Extended functionality for the hull manager
 */
public interface HullManagerExtInterface {
    // do a hit test against the voxels in this hull manager
    short[] hitTest(SimpleVector position, SimpleVector dir);

    // ==================

    // compute the "outside" of the described object
    boolean computeExterior();

    // fetch the "outside" of the described object
    // into a specific direction.
    // Required computeExterior() to be called before working
    short[][] getExteriorHull(int direction);
}
