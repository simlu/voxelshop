package com.vitco.app.core.modes.tools;

import com.threed.jpct.SimpleVector;
import com.vitco.app.core.container.DrawContainer;
import com.vitco.app.core.data.container.Voxel;
import com.vitco.app.settings.VitcoSettings;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.LinkedList;

/**
 * Defines the basic select tool.
 */
public class SelectTool extends AbstractVoxelTool {

    // constructor
    public SelectTool(DrawContainer container, int side) {
        super(container, side);
        // disable the background camera
        // NOTE: this can cause the highlighted parameter to be null in
        // the press and shiftPress functions
        useBackgroundCamera(false);
    }

    // --------------------

    @Override
    protected void key() {
        container.setCursor(Cursor.getDefaultCursor());
        useBackgroundCamera(isShiftDown());
    }

    // --------------------

    private void doSelect(Point c1, Point c2, boolean select) {
        Point start = new Point(
                Math.min(c1.x, c2.x),
                Math.min(c1.y, c2.y)
        );
        Point stop = new Point(
                Math.max(c1.x, c2.x),
                Math.max(c1.y, c2.y)
        );

        java.util.List<Integer> searchResult = new LinkedList<Integer>();

        if (!start.equals(stop)) {
            Voxel[] voxels;
            switch (side) {
                case -1:
                    voxels = data.getVisibleLayerVoxel();
                    break;
                case 2:
                    voxels = data.getVoxelsYZ(container.getPlane());
                    break;
                case 1:
                    voxels = data.getVoxelsXZ(container.getPlane());
                    break;
                case 0:
                    voxels = data.getVoxelsXY(container.getPlane());
                    break;
                default:
                    voxels = new Voxel[0];
                    break;
            }

            // use all voxles
            for (Voxel voxel : voxels) {
                SimpleVector vec = container.convert3D2D(new SimpleVector(
                        voxel.x * VitcoSettings.VOXEL_SIZE,
                        voxel.y * VitcoSettings.VOXEL_SIZE,
                        voxel.z * VitcoSettings.VOXEL_SIZE));
                if (vec != null) {
                    if (vec.x >= start.x && vec.x <= stop.x && vec.y >= start.y && vec.y <= stop.y) {
                        searchResult.add(voxel.id);
                    }
                }
            }
        }

        // execute the select
        Integer[] toSet = new Integer[searchResult.size()];
        searchResult.toArray(toSet);
        if (toSet.length > 0) {
            // reset selection shift
            data.setVoxelSelectionShift(0,0,0);
            // select voxels
            data.massSetVoxelSelected(toSet, select);
        }

    }

    // --------------------------
    // simple drawing
    // --------------------------

    // the points of a drag
    private Point start = null;
    private Point stop = null;

    // shifting selection,
    // if neq null user is currently dragging the selection
    private int[] currentSelectionShift = null;
    // the reference click voxel (in 3D)
    private SimpleVector dragStartReferencePos = null;
    // the "real" drag position (applying shifting)
    private SimpleVector dragStart = null;
    private SimpleVector dragStop = null;

    // true if mouse3 was pressed when drag started
    private boolean mouse3down = false;

    @Override
    public void move(MouseEvent e) {
        data.highlightVoxel(null);

        short[] hitVoxel = container.getShiftedCollisionVoxel(e.getPoint());
        if (hitVoxel != null) {
            container.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
        } else {
            container.setCursor(Cursor.getDefaultCursor());
        }
    }

    @Override
    public void press(MouseEvent e) {
        start = e.getPoint();
        mouse3down = isMouse3Down();
        // check if we hit something selected
        short[] hitVoxel = container.getShiftedCollisionVoxel(e.getPoint());
        if (hitVoxel != null) {
            // start dragging selection
            dragStartReferencePos = new SimpleVector(hitVoxel[0], hitVoxel[1], hitVoxel[2]);
            dragStart = container.convert2D3D(e.getX(), e.getY(), dragStartReferencePos);
            dragStop = null;
            currentSelectionShift = data.getVoxelSelectionShift();
        } else {
            currentSelectionShift = null;
        }
    }

    @Override
    protected void release(MouseEvent e) {
        stop = e.getPoint();
        // use the selected rectangle if this was not a drag that moved
        // the selection (1) or if this was a single position
        // click, i.e. no drag (2)
        if (currentSelectionShift == null || dragStop == null) {
            // do the selection
            doSelect(start, stop, !mouse3down);
        }
        // hide the rectangle
        data.setSelectionRect(null);
    }

    @Override
    public void drag(MouseEvent e) {
        stop = e.getPoint();
        // check if we're dragging selection (1) or are outlining a selection (2)
        if (currentSelectionShift != null) {
            // get reference position
            dragStop = container.convert2D3D(e.getX(), e.getY(), dragStartReferencePos);
            // update position of what we dragged to
            data.setVoxelSelectionShift(
                    Math.round(currentSelectionShift[0] - (dragStop.x - dragStart.x)/VitcoSettings.VOXEL_SIZE),
                    Math.round(currentSelectionShift[1] - (dragStop.y - dragStart.y)/VitcoSettings.VOXEL_SIZE),
                    Math.round(currentSelectionShift[2] - (dragStop.z - dragStart.z)/VitcoSettings.VOXEL_SIZE));
        } else {
            // update selection outline rectangle
            int x1 = Math.min(start.x, stop.x);
            int y1 = Math.min(start.y, stop.y);
            int x2 = Math.max(start.x, stop.x);
            int y2 = Math.max(start.y, stop.y);
            data.setSelectionRect(new Rectangle(x1, y1, x2 - x1, y2 - y1));
        }
    }

    @Override
    protected void click(MouseEvent e) {}

    // executed if no drag occurred
    @Override
    protected void singleClick(MouseEvent e) {
        // execute single selection click
        int[] pos = getVoxelSimple(e.getPoint());
        if (pos != null) {
            Voxel voxel = data.searchVoxel(pos, false);
            if (voxel != null) {
                //todo: fix that this can cause the wrong cursor to appear
                data.setVoxelSelected(voxel.id, !mouse3down);
            }
        }
    }

    // --------------------------
    // shift drawing
    // --------------------------

    // last active voxel position
    private int[] initialVoxelPos = null;

    private int[] lastVoxelPos = null;

    // previewRect color
    private final int[] selectDashColor = new int[]{Color.BLACK.getRGB(), Color.WHITE.getRGB()};

    // --------------------------

    @Override
    protected void shiftMove(MouseEvent e) {
        data.highlightVoxel(getVoxel(e.getPoint(), false));
    }

    @Override
    protected void shiftPress(MouseEvent e) {
        int[] highlighted = data.getHighlightedVoxel();
        // store state
        mouse3down = isMouse3Down();
        // memo selection outline
        initialVoxelPos = highlighted;
        lastVoxelPos = highlighted;
        if (highlighted != null) {
            // set initial preview rect
            data.setOutlineBox("preview", new int[][]{initialVoxelPos, lastVoxelPos, selectDashColor}.clone());
        }
        // no voxel selection
        data.highlightVoxel(null);
    }

    @Override
    protected void shiftRelease(MouseEvent e) {
        // use the voxels to select the new color
        if (lastVoxelPos != null && initialVoxelPos != null) {
            ArrayList<Integer> list = new ArrayList<Integer>();
            for (int x = Math.min(initialVoxelPos[0], lastVoxelPos[0]),
                         maxx = Math.max(initialVoxelPos[0], lastVoxelPos[0]); x <= maxx; x++ ) {
                for (int y = Math.min(initialVoxelPos[1], lastVoxelPos[1]),
                             maxy = Math.max(initialVoxelPos[1], lastVoxelPos[1]); y <= maxy; y++ ) {
                    for (int z = Math.min(initialVoxelPos[2], lastVoxelPos[2]),
                                 maxz = Math.max(initialVoxelPos[2], lastVoxelPos[2]); z <= maxz; z++ ) {
                        // depending on mouse3 state we only search current layer
                        Voxel voxel = data.searchVoxel(new int[]{x,y,z}, false);
                        if (voxel != null && voxel.getTexture() == null) {
                            // collect the voxels
                            list.add(voxel.id);
                        }
                    }
                }
            }
            // set the selection
            // execute the select
            Integer[] toSet = new Integer[list.size()];
            list.toArray(toSet);
            if (toSet.length > 0) {
                // reset selection shift
                data.setVoxelSelectionShift(0,0,0);
                // select voxels
                data.massSetVoxelSelected(toSet, !mouse3down);
            }
        }
        // hide preview
        data.setOutlineBox("preview", null);
    }

    @Override
    protected void shiftDrag(MouseEvent e) {
        lastVoxelPos = getVoxel(e.getPoint(), false);
        // display preview
        if (lastVoxelPos != null && initialVoxelPos != null) {
            data.setOutlineBox("preview", new int[][]{initialVoxelPos, lastVoxelPos, selectDashColor});
        } else {
            data.setOutlineBox("preview", null);
        }
    }

    @Override
    protected void shiftClick(MouseEvent e) {}

    @Override
    protected void singleShiftClick(MouseEvent e) {}

}

