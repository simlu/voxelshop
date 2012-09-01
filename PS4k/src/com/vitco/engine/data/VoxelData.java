package com.vitco.engine.data;

import com.vitco.engine.data.container.Voxel;
import com.vitco.engine.data.container.VoxelLayer;
import com.vitco.engine.data.history.BasicActionIntent;
import com.vitco.engine.data.history.HistoryChangeListener;
import com.vitco.engine.data.history.HistoryManager;

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
                layerBufferValid = false;
                layerNameBufferValid = false;
                visibleLayerVoxelBufferValid = false;
                layerVoxelBufferValid = false;
                layerVoxelXYBufferValid = false;
                layerVoxelXZBufferValid = false;
                layerVoxelYZBufferValid = false;
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

    // layer intents
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
                // remove all points in this layer
                for (Voxel voxel : layers.get(layerId).getVoxels()) {
                    historyManagerV.applyIntent(new RemoveVoxelIntent(voxel.id, true));
                }
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
    }

    // voxel intents
    private final class AddVoxelIntent extends BasicActionIntent {
        private final Voxel voxel;

        protected AddVoxelIntent(int voxelId, int[] pos, Color color, int layerId, boolean attach) {
            super(attach);
            voxel = new Voxel(voxelId, pos, color, layerId);
        }

        @Override
        protected void applyAction() {
            voxels.put(voxel.id, voxel);
            layers.get(voxel.getLayerId()).addVoxel(voxel);
        }

        @Override
        protected void unapplyAction() {
            voxels.remove(voxel.id);
            layers.get(voxel.getLayerId()).removeVoxel(voxel);
        }
    }

    private final class RemoveVoxelIntent extends BasicActionIntent {
        private final int voxelId;
        private Voxel voxel;

        protected RemoveVoxelIntent(int voxelId, boolean attach) {
            super(attach);
            this.voxelId = voxelId;
        }

        @Override
        protected void applyAction() {
            if (isFirstCall()) {
                voxel = voxels.get(voxelId);
            }
            voxels.remove(voxel.id);
            layers.get(voxel.getLayerId()).removeVoxel(voxel);
        }

        @Override
        protected void unapplyAction() {
            voxels.put(voxel.id, voxel);
            layers.get(voxel.getLayerId()).addVoxel(voxel);
        }
    }

    private final class MoveVoxelIntent extends BasicActionIntent {
        private final int voxelId;
        private final int[] newPos;

        protected MoveVoxelIntent(int voxelId, int[] newPos, boolean attach) {
            super(attach);
            this.voxelId = voxelId;
            this.newPos = newPos;
        }

        @Override
        protected void applyAction() {
            if (isFirstCall()) {
                Voxel voxel = voxels.get(voxelId);
                historyManagerV.applyIntent(new RemoveVoxelIntent(voxelId, true));
                historyManagerV.applyIntent(new AddVoxelIntent(voxelId, newPos, voxel.getColor(), voxel.getLayerId(), true));
            }
        }

        @Override
        protected void unapplyAction() {
            // nothing to do here
        }
    }

    private final class ColorVoxelIntent extends BasicActionIntent {
        private final int voxelId;
        private final Color newColor;
        private Color oldColor;
        private Voxel voxel;

        protected ColorVoxelIntent(int voxelId, Color newColor, boolean attach) {
            super(attach);
            this.voxelId = voxelId;
            this.newColor = newColor;
        }

        @Override
        protected void applyAction() {
            if (isFirstCall()) {
                oldColor = voxels.get(voxelId).getColor();
                voxel = voxels.get(voxelId);
            }
            layers.get(voxel.getLayerId()).setVoxelColor(voxel, newColor);
        }

        @Override
        protected void unapplyAction() {
            layers.get(voxel.getLayerId()).setVoxelColor(voxel, oldColor);
        }
    }

    private final class AlphaVoxelIntent extends BasicActionIntent {
        private final int voxelId;
        private final int newAlpha;
        private int oldAlpha;
        private Voxel voxel;

        protected AlphaVoxelIntent(int voxelId, int newAlpha, boolean attach) {
            super(attach);
            this.voxelId = voxelId;
            this.newAlpha = newAlpha;
        }

        @Override
        protected void applyAction() {
            if (isFirstCall()) {
                voxel = voxels.get(voxelId);
                oldAlpha = voxel.getAlpha();
            }
            layers.get(voxel.getLayerId()).setVoxelAlpha(voxel, newAlpha);
        }

        @Override
        protected void unapplyAction() {
            layers.get(voxel.getLayerId()).setVoxelAlpha(voxel, oldAlpha);
        }
    }

    private final class ClearVoxelRangeIntent extends BasicActionIntent {
        private final int[] pos;
        private final int radius;
        private final int layerId;

        protected ClearVoxelRangeIntent(int[] pos, int radius, int layerId, boolean attach) {
            super(attach);
            this.pos = pos;
            this.radius = radius;
            this.layerId = layerId;
        }

        @Override
        protected void applyAction() {
            if (isFirstCall()) {
                // get all voxels in this area and remove them
                for (Voxel voxel : layers.get(layerId).search(pos, radius)) {
                    historyManagerV.applyIntent(new RemoveVoxelIntent(voxel.id, true));
                }
            }
        }

        @Override
        protected void unapplyAction() {
            // nothing to do
        }
    }

    private final class FillVoxelRangeIntent extends BasicActionIntent {
        private final int[] pos;
        private final int radius;
        private final int layerId;
        private final Color color;

        protected FillVoxelRangeIntent(int[] pos, int radius, int layerId, Color color, boolean attach) {
            super(attach);
            this.pos = pos;
            this.radius = radius;
            this.layerId = layerId;
            this.color = color;
        }

        @Override
        protected void applyAction() {
            if (isFirstCall()) {
                // add all the voxels in this area that do not exist yet
                VoxelLayer layer = layers.get(layerId);
                for (int x = pos[0] - radius; x <= pos[0] + radius; x++) {
                    for (int y = pos[1] - radius; y <= pos[1] + radius; y++) {
                        for (int z = pos[2] - radius;z <= pos[2] + radius; z++) {
                            int[] pos = new int[]{x,y,z};
                            if (layer.search(pos, 0).length == 0) {
                                historyManagerV.applyIntent(new AddVoxelIntent(getFreeVoxelId(), pos, color, layerId, true));
                            }
                        }
                    }
                }
            }
        }

        @Override
        protected void unapplyAction() {
            // nothing to do
        }
    }

    private final class ClearVoxelIntent extends BasicActionIntent {
        private final int layerId;

        protected ClearVoxelIntent(int layerId, boolean attach) {
            super(attach);
            this.layerId = layerId;
        }

        @Override
        protected void applyAction() {
            if (isFirstCall()) {
                // get all voxels and remove them
                for (Voxel voxel : layers.get(layerId).getVoxels()) {
                    historyManagerV.applyIntent(new RemoveVoxelIntent(voxel.id, true));
                }
            }
        }

        @Override
        protected void unapplyAction() {
            // nothing to do
        }
    }

    private final class MergeLayersIntent extends BasicActionIntent {

        protected MergeLayersIntent(boolean attach) {
            super(attach);
        }

        @Override
        protected void applyAction() {
            if (isFirstCall()) {
                // duplicate the current layer
                int mergedLayerId = getFreeLayerId();
                historyManagerV.applyIntent(new CreateLayerIntent(mergedLayerId, "Merged", true));

                // add the voxels to the new layer (top to bottom)
                for (int layerId : layerOrder) {
                    if (layers.get(layerId).isVisible()) { // only visible
                        Voxel[] voxels = getLayerVoxels(layerId); // get voxels
                        for (Voxel voxel : voxels) {
                            if (!layers.get(mergedLayerId).containsVoxel(voxel.getPosAsInt())) { // add if this voxel does not exist
                                historyManagerV.applyIntent( // we <need> a new id for this voxel
                                        new AddVoxelIntent(getFreeVoxelId(), voxel.getPosAsInt(),
                                                voxel.getColor(), mergedLayerId, true)
                                );
                            }
                        }
                    }
                }

                // delete the visible layers (not the new one)
                Integer[] layer = new Integer[layerOrder.size()];
                layerOrder.toArray(layer);
                for (int layerId : layer) {
                    if (layerId != mergedLayerId && layers.get(layerId).isVisible()) {
                        historyManagerV.applyIntent(new DeleteLayerIntent(layerId, true));
                    }
                }

                // select the new layer
                historyManagerV.applyIntent(new SelectLayerIntent(mergedLayerId, true));
            }
        }

        @Override
        protected void unapplyAction() {
            // nothing to do
        }
    }

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
        int result = -1;
        VoxelLayer layer = layers.get(selectedLayer);
        if (layer != null && !layer.containsVoxel(pos)) {
            result = getFreeVoxelId();
            historyManagerV.applyIntent(new AddVoxelIntent(result, pos, color, selectedLayer, false));
        }
        return result;
    }

    @Override
    public final boolean removeVoxel(int voxelId) {
        boolean result = false;
        if (voxels.containsKey(voxelId)) {
            historyManagerV.applyIntent(new RemoveVoxelIntent(voxelId, false));
            result = true;
        }
        return result;
    }

    @Override
    public final boolean moveVoxel(int voxelId, int[] newPos) {
        boolean result = false;
        Voxel voxel = voxels.get(voxelId);
        if (voxel != null) {
            if (!layers.get(voxel.getLayerId()).containsVoxel(newPos)) {
                historyManagerV.applyIntent(new MoveVoxelIntent(voxel.id, newPos, false));
                result = true;
            }
        }
        return result;
    }

    @Override
    public final Voxel getVoxel(int voxelId) {
        Voxel result = null;
        if (voxels.containsKey(voxelId)) {
            result = voxels.get(voxelId);
        }
        return result;
    }

    @Override
    public final boolean setColor(int voxelId, Color color) {
        boolean result = false;
        if (voxels.containsKey(voxelId)) {
            historyManagerV.applyIntent(new ColorVoxelIntent(voxelId, color, false));
            result = true;
        }
        return result;
    }

    @Override
    public final Color getColor(int voxelId) {
        Color result = null;
        if (voxels.containsKey(voxelId)) {
            result = voxels.get(voxelId).getColor();
        }
        return result;
    }

    @Override
    public final boolean setAlpha(int voxelId, int alpha) {
        boolean result = false;
        if (voxels.containsKey(voxelId)) {
            historyManagerV.applyIntent(new AlphaVoxelIntent(voxelId, alpha, false));
            result = true;
        }
        return result;
    }

    @Override
    public final int getAlpha(int voxelId) {
        int result = -1;
        if (voxels.containsKey(voxelId)) {
            result = voxels.get(voxelId).getAlpha();
        }
        return result;
    }

    @Override
    public final int getLayer(int voxelId) {
        int result = -1;
        if (voxels.containsKey(voxelId)) {
            result = voxels.get(voxelId).getLayerId();
        }
        return result;
    }

    @Override
    public final boolean clearRange(int[] center, int rad) {
        boolean result = false;
        if (layers.containsKey(selectedLayer)) {
            if (layers.get(selectedLayer).search(center, rad).length > 0) {
                historyManagerV.applyIntent(new ClearVoxelRangeIntent(center, rad, selectedLayer, false));
                result = true;
            }
        }
        return result;
    }

    @Override
    public final boolean fillRange(int[] center, int rad, Color color) {
        boolean result = false;
        if (layers.containsKey(selectedLayer)) {
            if (layers.get(selectedLayer).search(center, rad).length < Math.pow(rad*2 + 1, 3)) { // if there are still free voxels
                historyManagerV.applyIntent(new FillVoxelRangeIntent(center, rad, selectedLayer, color, false));
                result = true;
            }
        }
        return result;
    }

    @Override
    public final boolean clearV(int layerId) {
        boolean result = false;
        if (layers.containsKey(layerId)) {
            if (layers.get(layerId).getVoxels().length > 0) {
                historyManagerV.applyIntent(new ClearVoxelIntent(layerId, false));
                result = true;
            }
        }
        return result;
    }

    Voxel[] layerVoxelBuffer = new Voxel[0];
    boolean layerVoxelBufferValid = false;
    int layerVoxelBufferLastLayer;
    @Override
    public final Voxel[] getLayerVoxels(int layerId) {
        if (!layerVoxelBufferValid || layerVoxelBufferLastLayer != layerId) {
            VoxelLayer layer = layers.get(layerId);
            if (layer != null) {
                layerVoxelBuffer = layer.getVoxels();
            }
            layerVoxelBufferValid = true;
            layerVoxelBufferLastLayer = layerId;
        }
        return layerVoxelBuffer.clone();
    }


    Voxel[] visibleLayerVoxelBuffer = new Voxel[0];
    boolean visibleLayerVoxelBufferValid = false;
    @Override
    public final Voxel[] getVisibleLayerVoxel() {
        if (!visibleLayerVoxelBufferValid) {
            VoxelLayer result = new VoxelLayer(-1, "tmp");
            for (Integer layerId : layerOrder) {
                if (layers.get(layerId).isVisible()) {
                    Voxel[] voxels = layers.get(layerId).getVoxels();
                    for (Voxel voxel : voxels) {
                        if (!result.containsVoxel(voxel.getPosAsInt())) {
                            result.addVoxel(voxel);
                        }
                    }
                }
            }
            visibleLayerVoxelBuffer = result.getVoxels();
            visibleLayerVoxelBufferValid = true;
        }
        return visibleLayerVoxelBuffer.clone();
    }

    int lastVoxelXYBufferZValue;
    boolean layerVoxelXYBufferValid = false;
    Voxel[] layerVoxelXYBuffer = new Voxel[0];
    @Override
    public final Voxel[] getVoxelsXY(int z) {
        if (!layerVoxelXYBufferValid || z != lastVoxelXYBufferZValue) {
            if (layers.containsKey(selectedLayer)) {
                layerVoxelXYBuffer = layers.get(selectedLayer).search(
                        new float[] {Integer.MIN_VALUE/2, Integer.MIN_VALUE/2, z},
                        new float[] {Integer.MAX_VALUE, Integer.MAX_VALUE, 0}
                );
            }
            layerVoxelXYBufferValid = true;
            lastVoxelXYBufferZValue = z;
        }
        return layerVoxelXYBuffer.clone();
    }

    int lastVoxelXZBufferYValue;
    boolean layerVoxelXZBufferValid = false;
    Voxel[] layerVoxelXZBuffer = new Voxel[0];
    @Override
    public final Voxel[] getVoxelsXZ(int y) {
        if (!layerVoxelXZBufferValid || y != lastVoxelXZBufferYValue) {
            if (layers.containsKey(selectedLayer)) {
                layerVoxelXZBuffer = layers.get(selectedLayer).search(
                        new float[] {Integer.MIN_VALUE/2, y, Integer.MIN_VALUE/2},
                        new float[] {Integer.MAX_VALUE, 0, Integer.MAX_VALUE}
                );
            }
            layerVoxelXZBufferValid = true;
            lastVoxelXZBufferYValue = y;
        }
        return layerVoxelXZBuffer.clone();
    }

    int lastVoxelYZBufferXValue;
    boolean layerVoxelYZBufferValid = false;
    Voxel[] layerVoxelYZBuffer = new Voxel[0];
    @Override
    public final Voxel[] getVoxelsYZ(int x) {
        if (!layerVoxelYZBufferValid || x != lastVoxelYZBufferXValue) {
            if (layers.containsKey(selectedLayer)) {
                layerVoxelYZBuffer = layers.get(selectedLayer).search(
                        new float[] {x, Integer.MIN_VALUE/2, Integer.MIN_VALUE/2},
                        new float[] {0, Integer.MAX_VALUE, Integer.MAX_VALUE}
                );
            }
            layerVoxelYZBufferValid = true;
            lastVoxelYZBufferXValue = x;
        }
        return layerVoxelYZBuffer.clone();
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
        return result;
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
        // if there are more than one visible layer
        int visibleLayers = 0;
        for (int layerId : layerOrder) {
            if (layers.get(layerId).isVisible()) {
                if (visibleLayers > 0) {
                    historyManagerV.applyIntent(new MergeLayersIntent(false));
                    return true;
                }
                visibleLayers++;
            }
        }
        return false;
    }
}
