package com.vitco.engine.adapter.tools;

import com.vitco.engine.data.container.Voxel;
import com.vitco.engine.view.DrawContainer;
import com.vitco.util.ColorTools;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Defines the basic color changer tool.
 */
public class FloodFillTool extends AbstractVoxelTool {

    // constructor
    public FloodFillTool(DrawContainer container, int side) {
        super(container, side);
    }

    // --------------------

    // flood fill (recolor)
    private void floodColor(Voxel start, HashMap<String, Integer> result, boolean currentLayer) {
        Color color = start.getColor();
        ArrayList<int[]> queue = new ArrayList<int[]>();
        queue.add(0, start.getPosAsInt());
        while (!queue.isEmpty()) {
            int[] node = queue.remove(0);
            String strPos = node[0] + "_" + node[1] + "_" + node[2];
            if (!result.containsKey(strPos)) {
                Voxel voxel = data.searchVoxel(node, currentLayer);
                if (voxel != null && voxel.getColor().equals(color) && voxel.getTexture() == null) {
                    // add to result list
                    result.put(strPos, voxel.id);
                    if (side != 2) {
                        queue.add(0, new int[] {node[0] + 1, node[1], node[2]});
                        queue.add(0, new int[] {node[0] - 1, node[1], node[2]});
                    }
                    if (side != 1) {
                        queue.add(0, new int[] {node[0], node[1] + 1, node[2]});
                        queue.add(0, new int[] {node[0], node[1] - 1, node[2]});
                    }
                    if (side != 0) {
                        queue.add(0, new int[] {node[0], node[1], node[2] + 1});
                        queue.add(0, new int[] {node[0], node[1], node[2] - 1});
                    }
                }
            }
        }
    }

    // flood fill starting from a voxel
    private boolean flood(Voxel start, boolean currentLayer) {
        // recolor/retexture the voxels
        int selectedTexture = data.getSelectedTexture();
        Color newColor = ColorTools.hsbToColor(getCurrentColor());
        if (selectedTexture != -1 || !newColor.equals(start.getColor())) {
            // find the voxels
            HashMap<String, Integer> result = new HashMap<String, Integer>();
            floodColor(start, result, currentLayer);
            Integer[] resultArray = new Integer[result.size()];
            result.values().toArray(resultArray);
            // recolor/retexture the voxels
            if (selectedTexture != -1) {
                data.massSetTexture(resultArray, selectedTexture);
            } else {
                data.massSetColor(resultArray, newColor);
            }
            return true;
        }
        return false;
    }

    // recolor voxel (all voxels in layer or plane)
    private void recolor(int[] start, Color color, boolean currentLayer) {
        // find the voxels
        Voxel[] voxels;
        boolean prune = false; // true iff we need to consider currentLayer
        switch (side) {
            case -1:
                // pruning is automatically done
                if (currentLayer) {
                    voxels = data.getLayerVoxels(getCurrentLayer());
                } else {
                    voxels = data.getVisibleLayerVoxel();
                }
                break;
            case 2:
                voxels = data.getVoxelsYZ(start[0]);
                prune = currentLayer;
                break;
            case 1:
                voxels = data.getVoxelsXZ(start[1]);
                prune = currentLayer;
                break;
            case 0:
                voxels = data.getVoxelsXY(start[2]);
                prune = currentLayer;
                break;
            default:
                voxels = new Voxel[0];
                break;
        }
        ArrayList<Integer> result = new ArrayList<Integer>();
        for (Voxel voxel : voxels) {
            if (voxel.getColor().equals(color) && (!prune || voxel.getLayerId() == getCurrentLayer())) {
                result.add(voxel.id);
            }
        }
        Integer[] resultArray = new Integer[result.size()];
        result.toArray(resultArray);
        // recolor / retexture the voxels
        int selectedTexture = data.getSelectedTexture();
        if (selectedTexture != -1) {
            data.massSetTexture(resultArray, selectedTexture);
        } else {
            data.massSetColor(resultArray, ColorTools.hsbToColor(getCurrentColor()));
        }
    }

    // --------------------

    @Override
    protected void key() {}

    // --------------------------
    // simple drawing
    // --------------------------

    @Override
    public void move(MouseEvent e) {
        data.highlightVoxel(getVoxelSimple(e.getPoint()));
    }

    @Override
    public void press(MouseEvent e) {

        int[] highlighted = data.getHighlightedVoxel();
        Voxel highlightedVoxel = data.searchVoxel(highlighted, false);

        // perform action
        if (highlightedVoxel != null) {
            // start flood filling
            flood(highlightedVoxel, isMouse3Down());
        }

        // cancel highlighting
        data.highlightVoxel(null);
    }

    @Override
    protected void release(MouseEvent e) {
        data.highlightVoxel(getVoxelSimple(e.getPoint()));
    }

    @Override
    public void drag(MouseEvent e) {
        int[] highlighted = getVoxelSimple(e.getPoint());
        if (highlighted != null) {
            Voxel highlightedVoxel = data.searchVoxel(highlighted, true);
            if (highlightedVoxel != null) {
                // start flood filling
                flood(highlightedVoxel, isMouse3Down());
            }
        }
    }

    @Override
    protected void click(MouseEvent e) {}

    @Override
    protected void singleClick(MouseEvent e) {}

    // --------------------------
    // shift drawing
    // --------------------------


    @Override
    protected void shiftMove(MouseEvent e) {
        data.highlightVoxel(getVoxelSimple(e.getPoint()));
    }

    @Override
    protected void shiftPress(MouseEvent e) {
        int[] highlighted = data.getHighlightedVoxel();
        Voxel highlightedVoxel = data.searchVoxel(highlighted, false);

        // perform action
        if (highlightedVoxel != null) {
            // start recoloring
            if (!isMouse3Down() || highlightedVoxel.getLayerId() == getCurrentLayer()) {
                recolor(highlighted, highlightedVoxel.getColor(), isMouse3Down());
            }
        }

        // cancel highlighting
        data.highlightVoxel(null);
    }

    @Override
    protected void shiftRelease(MouseEvent e) {
        data.highlightVoxel(getVoxelSimple(e.getPoint()));
    }

    @Override
    protected void shiftDrag(MouseEvent e) {
        int[] highlighted = getVoxelSimple(e.getPoint());
        if (highlighted != null) {
            Voxel highlightedVoxel = data.searchVoxel(highlighted, true);
            if (highlightedVoxel != null) {
                // start recoloring
                if (!isMouse3Down() || highlightedVoxel.getLayerId() == getCurrentLayer()) {
                    recolor(highlighted, highlightedVoxel.getColor(), isMouse3Down());
                }
            }
        }
    }

    @Override
    protected void shiftClick(MouseEvent e) {}

    @Override
    protected void singleShiftClick(MouseEvent e) {}

}

