package com.vitco.core.modes.tools;

import com.threed.jpct.SimpleVector;
import com.vitco.core.container.DrawContainer;

import java.awt.*;

/**
 * Abstract of a voxel tool that can be used to alter voxel data.
 */
public abstract class AbstractVoxelTool extends AbstractBasicTool {

    // constructor
    public AbstractVoxelTool(DrawContainer container, int side) {
        super(container, side);
    }

    // -----------------------------

    // the plane that was last interfered with
    private int activePlane = 0;
    public final int getActivePlane() {
        return activePlane;
    }

    // the side that was last hit by an action
    private int activeSide = 0;
    public final int getActiveSide() {
        return activeSide;
    }

    // simple hit test for voxel
    protected final int[] getVoxelSimple(Point p) {
        int[] result;
        if (side == -1) {
            result = container.voxelForHover3D(p, false, false);
            activeSide = container.getLastActiveSide();
            activePlane = activeSide/2;
        } else {
            result = container.voxelForHover2D(p);
            if (result != null && data.searchVoxel(result, false) == null) {
                result = null;
            }
            switch (side) {
                case 0: activeSide = 5; break;
                case 1: activeSide = 2; break;
                //case 2: activeSide = 0; break;
                default: activeSide = 0; break;
            }
            activePlane = activeSide/2;

        }
        return result;
    }

    // get a voxel for a point
    protected final int[] getVoxel(Point p, boolean neighbour) {
        int[] result;
        if (side == -1) {
            result = container.voxelForHover3D(p, neighbour, isUseBoundingBox());
            activeSide = container.getLastActiveSide();
            activePlane = activeSide/2;
        } else {
            result = container.voxelForHover2D(p);
            switch (side) {
                case 0: activeSide = 5; break;
                case 1: activeSide = 2; break;
                //case 2: activeSide = 0; break;
                default: activeSide = 0; break;
            }
            activePlane = activeSide/2;
        }
        return result;
    }

    // get a voxel for a point and a plane (3D)
    protected final int[] getVoxelUsePlaneNext(Point p) {
        SimpleVector dir = container.getDirection(p.x, p.y);
        return container.voxelForHover3DNext(dir, activeCenter[activePlane], activePlane);
    }

    // get a voxel for a point and a plane (3D)
    protected final int[] getVoxelUsePlanePrev(Point p) {
        SimpleVector dir = container.getDirection(p.x, p.y);
        return container.voxelForHover3DPrev(dir, activeCenter[activePlane], activePlane);
    }

    // the current center information that is used to determine
    // which plane should be used for a hit test
    private final int[] activeCenter = new int[] {0,0,0};
    // setter
    public final void setActiveCenter(int[] activeCenter) {
        this.activeCenter[0] = activeCenter[0];
        this.activeCenter[1] = activeCenter[1];
        this.activeCenter[2] = activeCenter[2];
    }

    // -----------------------------

    // true if the background camera is disabled (clicking on the background to enable camera)
    private boolean useBackgroundCamera = true;
    public final void useBackgroundCamera(boolean b) {
        this.useBackgroundCamera = b;
    }

    // -----------------------------

    @Override
    protected final void softCleanUp() {
        container.setCursor(Cursor.getDefaultCursor());
        data.highlightVoxel(null); // cancel highlight
    }

    @Override
    protected final boolean allowDrag() {
        return data.getHighlightedVoxel() != null || !useBackgroundCamera;
    }



}
