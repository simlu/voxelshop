package com.vitco.frames.engine.data;

import com.vitco.frames.engine.data.container.Voxel;
import com.vitco.frames.engine.data.container.VoxelLayer;
import com.vitco.frames.engine.data.history.BasicActionIntent;
import com.vitco.frames.engine.data.history.HistoryChangeListener;
import com.vitco.frames.engine.data.history.HistoryManager;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

/**
 * Defines the voxel data interaction (layer, undo, etc)
 */
public abstract class VoxelData extends AnimationHighlight implements VoxelDataInterface {

    // constructor
    protected VoxelData() {
        super();
        // notify when the data changes
        historyManagerV.addChangeListener(new HistoryChangeListener() {
            @Override
            public final void onChange() {
                // todo put all buffer valid to false here!!
                layerBufferValid = false;
                layerNameBufferValid = false;
                notifier.onVoxelDataChanged();
            }
        });
    }

    // ###################### DATA
    // holds the historyV data
    protected final HistoryManager historyManagerV = new HistoryManager();

    // holds all layers (layers map to voxel ids)
    protected int selectedLayer = -1;
    protected final HashMap<Integer, VoxelLayer> layers = new HashMap<Integer, VoxelLayer>();
    // order of the layers
    protected final ArrayList<Integer> layerOrder = new ArrayList<Integer>();

    // holds all voxels (maps id to voxel)
    private final HashMap<Integer, Voxel> voxels = new HashMap<Integer, Voxel>();

    // ###################### PRIVATE HELPER CLASSES
    private final class CreateLayerIntent extends BasicActionIntent {
        private final Integer layerId;
        private final String layerName;

        protected CreateLayerIntent(int layerId, String layerName, boolean attach) {
            super(attach);
            this.layerId = layerId;
            this.layerName = layerName;
        }

        @Override
        protected void applyAction() {
            layers.put(layerId, new VoxelLayer(layerId, layerName));
            layerOrder.add(0, layerId);
        }

        @Override
        protected void unapplyAction() {
            layers.remove(layerId);
            layerOrder.remove(layerOrder.lastIndexOf(layerId));
        }
    }

    private final class DeleteLayerIntent extends BasicActionIntent {
        private final Integer layerId;
        private Integer layerPosition;
        private String layerName;

        protected DeleteLayerIntent(int layerId, boolean attach) {
            super(attach);
            this.layerId = layerId;
        }

        @Override
        protected void applyAction() {
            if (isFirstCall()) {
                // todo remove all points in this layer

                // make sure the selected Layer is still "valid"
                if (selectedLayer == layerId) {
                    historyManagerV.applyIntent(new SelectLayerIntent(-1, true));
                }
                // remember the position of this layer
                layerPosition = layerOrder.indexOf(layerId);
                // and the name
                layerName = layers.get(layerId).getName();
            }
            layers.remove(layerId);
            layerOrder.remove(layerId);
        }

        @Override
        protected void unapplyAction() {
            layers.put(layerId, new VoxelLayer(layerId, layerName));
            layerOrder.add(layerPosition, layerId);
        }
    }

    private final class RenameLayerIntent extends BasicActionIntent {
        private final Integer layerId;
        private final String newName;
        private String oldName;

        protected RenameLayerIntent(int layerId, String newName, boolean attach) {
            super(attach);
            this.layerId = layerId;
            this.newName = newName;
        }

        @Override
        protected void applyAction() {
            if (isFirstCall()) {
                oldName = layers.get(layerId).getName();
            }
            layers.get(layerId).setName(newName);
        }

        @Override
        protected void unapplyAction() {
            layers.get(layerId).setName(oldName);
        }
    }

    private final class SelectLayerIntent extends BasicActionIntent {
        private final Integer newLayerId;
        private Integer oldLayerId;

        protected SelectLayerIntent(int newLayerId, boolean attach) {
            super(attach);
            this.newLayerId = newLayerId;
        }

        @Override
        protected void applyAction() {
            if (isFirstCall()) {
                oldLayerId = selectedLayer;
            }
            selectedLayer = newLayerId;
        }

        @Override
        protected void unapplyAction() {
            selectedLayer = oldLayerId;
        }
    }

    private final class LayerVisibilityIntent extends BasicActionIntent {
        private final Integer layerId;
        private final boolean visible;
        private boolean oldVisible;

        protected LayerVisibilityIntent(int layerId, boolean visible, boolean attach) {
            super(attach);
            this.layerId = layerId;
            this.visible = visible;
        }

        @Override
        protected void applyAction() {
            if (isFirstCall()) {
                oldVisible = layers.get(layerId).isVisible();
            }
            layers.get(layerId).setVisible(visible);
        }

        @Override
        protected void unapplyAction() {
            layers.get(layerId).setVisible(oldVisible);
        }
    }

    private final class MoveLayerIntent extends BasicActionIntent {
        private final Integer layerId;
        private final boolean moveUp;

        protected MoveLayerIntent(int layerId, boolean moveUp, boolean attach) {
            super(attach);
            this.layerId = layerId;
            this.moveUp = moveUp;
        }

        @Override
        protected void applyAction() {
            int index = layerOrder.lastIndexOf(layerId);
            if (moveUp) {
                Collections.swap(layerOrder, index, index - 1);
            } else {
                Collections.swap(layerOrder, index, index + 1);
            }
        }

        @Override
        protected void unapplyAction() {
            int index = layerOrder.lastIndexOf(layerId);
            if (moveUp) {
                Collections.swap(layerOrder, index, index + 1);
            } else {
                Collections.swap(layerOrder, index, index - 1);
            }
        }
    };

    // ##################### PRIVATE HELPER FUNCTIONS
    // returns a free voxel id
    private int lastVoxel = -1;
    private int getFreeVoxelId() {
        do {
            lastVoxel++;
        } while (voxels.containsKey(lastVoxel));
        return lastVoxel;
    }

    // returns a free layer id
    private int lastLayer = -1;
    private int getFreeLayerId() {
        do {
            lastLayer++;
        } while (layers.containsKey(lastLayer));
        return lastLayer;
    }

    // =========================
    // === interface methods ===
    // =========================

    @Override
    public final int addVoxel(Color color, int[] pos) {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public final boolean removeVoxel(int voxelId) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public final boolean moveVoxel(int voxelId, int[] newPos) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public final boolean setVoxelLinkId(int voxelId, int linkId) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public final int getVoxelLinkId(int voxelId) {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public final boolean setColor(int voxelId, Color color) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public final Color getColor(int voxelId) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public final boolean setAlpha(int voxelId, int alpha) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public final int getAlpha(int voxelId) {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public final int getLayer(int voxelId) {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public final boolean clearRange(int[] center, int rad) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public final boolean fillRange(int[] center, int rad, Color color) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public final void clearV() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public final int getAllVoxels() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public final int getLayerVoxels() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public final int getVoxelsXY(int z) {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public final int getVoxelsXZ(int y) {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public final int getVoxelsYZ(int x) {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public final int getAllVoxelsXY(int z) {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public final int getAllVoxelsXZ(int y) {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public final int getAllVoxelsYZ(int x) {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public final void undoV() {
        historyManagerV.unapply();
    }

    @Override
    public final void redoV() {
        historyManagerV.apply();
    }

    @Override
    public final boolean canUndoV() {
        return historyManagerV.canUndo();
    }

    @Override
    public final boolean canRedoV() {
        return historyManagerV.canRedo();
    }

    @Override
    public final int createLayer(String layerName) {
        int layerId = getFreeLayerId();
        historyManagerV.applyIntent(new CreateLayerIntent(layerId, layerName, false));
        return layerId;
    }

    @Override
    public final boolean deleteLayer(int layerId) {
        boolean result = false;
        if (layers.containsKey(layerId)) {
            historyManagerV.applyIntent(new DeleteLayerIntent(layerId, false));
            result = true;
        }
        return result;
    }

    @Override
    public final boolean renameLayer(int layerId, String newName) {
        boolean result = false;
        if (layers.containsKey(layerId)) {
            historyManagerV.applyIntent(new RenameLayerIntent(layerId, newName, false));
            result = true;
        }
        return result;
    }

    @Override
    public final String getLayerName(int layerId) {
        return layers.containsKey(layerId) ? layers.get(layerId).getName() : null;
    }

    private boolean layerNameBufferValid = false;
    private String[] layerNameBuffer = new String[]{};
    @Override
    public final String[] getLayerNames() {
        if (!layerNameBufferValid) {
            if (layerNameBuffer.length != layers.size()) {
                layerNameBuffer = new String[layers.size()];
            }
            int i = 0;
            for (Integer layerId : layerOrder) {
                layerNameBuffer[i++] = getLayerName(layerId);
            }
            layerBufferValid = true;
        }
        return layerNameBuffer.clone();
    }

    @Override
    public final boolean selectLayer(int layerId) {
        boolean result = false;
        if (layers.containsKey(layerId) || layerId == -1) {
            historyManagerV.applyIntent(new SelectLayerIntent(layerId, false));
            result = true;
        }
        return result;
    }

    @Override
    public final int getSelectedLayer() {
        return selectedLayer;
    }

    private boolean layerBufferValid = false;
    private Integer[] layerBuffer = new Integer[]{};
    @Override
    public final Integer[] getLayers() {
        if (!layerBufferValid) {
            if (layerBuffer.length != layers.size()) {
                layerBuffer = new Integer[layers.size()];
            }
            layerOrder.toArray(layerBuffer);
            layerBufferValid = true;
        }
        return layerBuffer.clone();
    }

    @Override
    public final boolean setVisible(int layerId, boolean b) {
        boolean result = false;
        if (layers.containsKey(layerId) && layers.get(layerId).isVisible() != b) {
            historyManagerV.applyIntent(new LayerVisibilityIntent(layerId, b, false));
            result = true;
        }
        return result;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public final boolean getLayerVisible(int layerId) {
        boolean result = false;
        if (layers.containsKey(layerId)) {
            result = layers.get(layerId).isVisible();
        }
        return result;
    }

    @Override
    public final boolean moveLayerUp(int layerId) {
        boolean result = false;
        if (canMoveLayerUp(layerId)) {
            historyManagerV.applyIntent(new MoveLayerIntent(layerId, true, false));
            result = true;
        }
        return result;
    }

    @Override
    public final boolean moveLayerDown(int layerId) {
        boolean result = false;
        if (canMoveLayerDown(layerId)) {
            historyManagerV.applyIntent(new MoveLayerIntent(layerId, false, false));
            result = true;
        }
        return result;
    }

    @Override
    public final boolean canMoveLayerUp(int layerId) {
        return layerOrder.lastIndexOf(layerId) > 0;
    }

    @Override
    public final boolean canMoveLayerDown(int layerId) {
        return layerOrder.lastIndexOf(layerId) < layerOrder.size() - 1;
    }

    @Override
    public final boolean mergeLayers() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
