package com.vitco.core.data;

import com.vitco.core.data.container.Voxel;
import com.vitco.core.data.container.VoxelLayer;
import com.vitco.core.data.history.HistoryChangeListener;
import com.vitco.core.data.history.HistoryManager;
import com.vitco.core.data.history.VoxelActionIntent;
import com.vitco.low.CubeIndexer;
import com.vitco.settings.VitcoSettings;
import com.vitco.util.graphic.GraphicTools;
import com.vitco.util.misc.ArrayUtil;
import com.vitco.util.misc.ColorTools;
import gnu.trove.iterator.TIntIterator;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.set.hash.TIntHashSet;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Defines the voxel data interaction (layer, undo, etc)
 */
public abstract class VoxelData extends AnimationHighlight implements VoxelDataInterface {

    // constructor
    // contains the (history) listener event declaration for voxel and texture
    protected VoxelData() {
        super();
        // notify when the data changes
        historyManagerV.addChangeListener(new HistoryChangeListener<VoxelActionIntent>() {
            @Override
            public final void onChange(VoxelActionIntent action) {
                int[][] effectedVoxels = null;
                boolean effectsTexture = false;
                if (action != null) {
                    effectedVoxels = action.effected();
                    effectsTexture = action.effectsTexture();
                }
                // calculate the effected voxels
                invalidateV(effectedVoxels);
                // notify if texture have been changed
                if (effectsTexture) {
                    notifier.onTextureDataChanged();
                }
            }

            @Override
            public void onFrozenIntent(VoxelActionIntent actionIntent) {
                notifier.onFrozenAction();
            }

            @Override
            public void onFrozenApply() {
                notifier.onFrozenRedo();
            }

            @Override
            public void onFrozenUnapply() {
                notifier.onFrozenUndo();
            }
        });
    }

    // holds the voxel positions that are currently selected (so we only notify
    // position where the selection state has actually changed!)
    private final TIntHashSet currentSelectedVoxel = new TIntHashSet();

    // invalidate cache
    protected final void invalidateV(int[][] effected) {
        if (effected != null) {
            // notification of changed visible voxels
            for (TIntObjectHashMap<int[]> map : changedVisibleVoxel.values()) {
                if (map != null) {
                    for (int[] invalid : effected) {
                        Integer key = CubeIndexer.getId(invalid[0], invalid[1], invalid[2]);
                        if (!map.containsKey(key)) {
                            map.put(key, invalid);
                        }
                    }
                }
            }

            // notification of changed selected voxels
            TIntObjectHashMap<int[]> invalidSelected = new TIntObjectHashMap<int[]>();
            for (int[] invalid : effected) {
                Voxel voxel = searchVoxel(invalid, false);
                if (voxel == null) {
                    int key = CubeIndexer.getId(invalid[0], invalid[1], invalid[2]);
                    if (currentSelectedVoxel.remove(key)) {
                        invalidSelected.put(key, invalid);
                    }
                } else {
                    int key = voxel.posId;
                    if (voxel.isSelected()) {
                        if (currentSelectedVoxel.add(key)) {
                            invalidSelected.put(key, invalid);
                        }
                    } else {
                        if (currentSelectedVoxel.remove(key)) {
                            invalidSelected.put(key, invalid);
                        }
                    }
                }
            }
            for (TIntObjectHashMap<int[]> map : changedSelectedVoxel.values()) {
                if (map != null) {
                    map.putAll(invalidSelected);
                }
            }

            // notification of changed visible voxel for this plane
            for (TIntIterator it = changedVisibleVoxelPlane.keySet().iterator(); it.hasNext();) {
                int side = it.next();
                int missingSide = 1;
                switch (side) {
                    case 0: missingSide = 2; break;
                    case 2: missingSide = 0; break;
                    default: break;
                }
                for (String requestId : changedVisibleVoxelPlane.get(side).keySet()) {
                    for (int[] invalid : effected) {
                        // make sure this is set
                        if (!changedVisibleVoxelPlane.get(side).get(requestId).containsKey(invalid[missingSide])) {
                            changedVisibleVoxelPlane.get(side).get(requestId).put(
                                    invalid[missingSide], new TIntObjectHashMap<int[]>());
                        }
                        // fill the details
                        int key = CubeIndexer.getId(invalid[0], invalid[1], invalid[2]);
                        if (!changedVisibleVoxelPlane.get(side).get(requestId).get(invalid[missingSide]).containsKey(key)) {
                            changedVisibleVoxelPlane.get(side).get(requestId).get(invalid[missingSide]).put(key, invalid);
                        }
                    }
                }
            }
        } else {
            currentSelectedVoxel.clear();
            changedSelectedVoxel.clear();
            changedVisibleVoxel.clear();
            changedVisibleVoxelPlane.clear();
        }
        layerBufferValid = false;
        layerNameBufferValid = false;
        layerVoxelBufferValid = false;
        layerVoxelXYBufferValid = false;
        layerVoxelXZBufferValid = false;
        layerVoxelYZBufferValid = false;
        selectedVoxelBufferValid = false;
        visibleLayerVoxelInternalBufferValid = false;
        notifier.onVoxelDataChanged();
    }

    // holds the historyV data
    protected final HistoryManager<VoxelActionIntent> historyManagerV = new HistoryManager<VoxelActionIntent>();

    // buffer for the selected voxels
    private Voxel[] selectedVoxelBuffer = new Voxel[0];
    private boolean selectedVoxelBufferValid = false;

    // ===========================================

    // ###################### PRIVATE HELPER CLASSES

    // layer intents
    private final class CreateLayerIntent extends VoxelActionIntent {
        private final Integer layerId;
        private final String layerName;

        protected CreateLayerIntent(int layerId, String layerName, boolean attach) {
            super(attach);
            this.layerId = layerId;
            this.layerName = layerName;
        }

        @Override
        protected void applyAction() {
            dataContainer.layers.put(layerId, new VoxelLayer(layerId, layerName));
            dataContainer.layerOrder.add(0, layerId);
        }

        @Override
        protected void unapplyAction() {
            dataContainer.layers.remove(layerId);
            dataContainer.layerOrder.remove(dataContainer.layerOrder.lastIndexOf(layerId));
        }

        @Override
        public int[][] effected() {
            // nothing effected
            return new int[0][];
        }
    }

    private final class DeleteLayerIntent extends VoxelActionIntent {
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
                // remember effected positions
                effected = new int[dataContainer.layers.get(layerId).getVoxels().length][];
                // remove all points in this layer
                Voxel[] voxels = dataContainer.layers.get(layerId).getVoxels();
                for (int i = 0; i < voxels.length; i++) {
                    Voxel voxel = voxels[i];
                    historyManagerV.applyIntent(new RemoveVoxelIntent(voxel.id, true));
                    effected[i] = voxel.getPosAsInt(); // store
                }
                // remember the position of this layer
                layerPosition = dataContainer.layerOrder.indexOf(layerId);
                // and the name
                layerName = dataContainer.layers.get(layerId).getName();
            }
            dataContainer.layers.remove(layerId);
            dataContainer.layerOrder.remove(layerId);
        }

        @Override
        protected void unapplyAction() {
            dataContainer.layers.put(layerId, new VoxelLayer(layerId, layerName));
            dataContainer.layerOrder.add(layerPosition, layerId);
        }

        private int[][] effected = null; // everything effected
        @Override
        public int[][] effected() {
            return effected;
        }
    }

    private final class RenameLayerIntent extends VoxelActionIntent {
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
                oldName = dataContainer.layers.get(layerId).getName();
            }
            dataContainer.layers.get(layerId).setName(newName);
        }

        @Override
        protected void unapplyAction() {
            dataContainer.layers.get(layerId).setName(oldName);
        }

        @Override
        public int[][] effected() {
            // nothing effected
            return new int[0][];
        }
    }

    private final class SelectLayerIntent extends VoxelActionIntent {
        private final Integer newLayerId;
        private Integer oldLayerId;

        protected SelectLayerIntent(int newLayerId, boolean attach) {
            super(attach);
            this.newLayerId = newLayerId;
        }

        @Override
        protected void applyAction() {
            if (isFirstCall()) {
                oldLayerId = dataContainer.selectedLayer;
            }
            dataContainer.selectedLayer = newLayerId;
        }

        @Override
        protected void unapplyAction() {
            dataContainer.selectedLayer = oldLayerId;
        }

        @Override
        public int[][] effected() {
            // nothing effected
            return new int[0][];
        }
    }

    private final class LayerVisibilityIntent extends VoxelActionIntent {
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
                oldVisible = dataContainer.layers.get(layerId).isVisible();
            }
            dataContainer.layers.get(layerId).setVisible(visible);
        }

        @Override
        protected void unapplyAction() {
            dataContainer.layers.get(layerId).setVisible(oldVisible);
        }

        private int[][] effected = null; // everything effected
        @Override
        public int[][] effected() {
            if (effected == null) { // get effected positions
                Voxel[] voxels = dataContainer.layers.get(layerId).getVoxels();
                effected = new int[voxels.length][];
                for (int i = 0, voxelsLength = voxels.length; i < voxelsLength; i++) {
                    effected[i] = voxels[i].getPosAsInt();
                }
            }
            return effected;
        }
    }

    private final class MoveLayerIntent extends VoxelActionIntent {
        private final Integer layerId;
        private final boolean moveUp;

        protected MoveLayerIntent(int layerId, boolean moveUp, boolean attach) {
            super(attach);
            this.layerId = layerId;
            this.moveUp = moveUp;
        }

        @Override
        protected void applyAction() {
            int index = dataContainer.layerOrder.lastIndexOf(layerId);
            if (moveUp) {
                Collections.swap(dataContainer.layerOrder, index, index - 1);
            } else {
                Collections.swap(dataContainer.layerOrder, index, index + 1);
            }
        }

        @Override
        protected void unapplyAction() {
            int index = dataContainer.layerOrder.lastIndexOf(layerId);
            if (moveUp) {
                Collections.swap(dataContainer.layerOrder, index, index + 1);
            } else {
                Collections.swap(dataContainer.layerOrder, index, index - 1);
            }
        }

        private int[][] effected = null; // everything effected
        @Override
        public int[][] effected() {
            if (effected == null) { // get effected positions
                Voxel[] voxels = dataContainer.layers.get(layerId).getVoxels();
                effected = new int[voxels.length][];
                for (int i = 0, voxelsLength = voxels.length; i < voxelsLength; i++) {
                    effected[i] = voxels[i].getPosAsInt();
                }
            }
            return effected;
        }
    }

    // voxel intents
    private final class AddVoxelIntent extends VoxelActionIntent {
        private final Voxel voxel;

        protected AddVoxelIntent(int voxelId, int[] pos, Color color, boolean selected,
                                 int[] textureId, int layerId, boolean attach) {
            super(attach);
            voxel = new Voxel(voxelId, pos, color, selected, textureId, layerId);
        }

        @Override
        protected void applyAction() {
            dataContainer.voxels.put(voxel.id, voxel);
            dataContainer.layers.get(voxel.getLayerId()).addVoxel(voxel);
        }

        @Override
        protected void unapplyAction() {
            dataContainer.voxels.remove(voxel.id);
            dataContainer.layers.get(voxel.getLayerId()).removeVoxel(voxel);
        }

        @Override
        public int[][] effected() {
            return new int[][]{voxel.getPosAsInt()};
        }
    }

    private final class RemoveVoxelIntent extends VoxelActionIntent {
        private final int voxelId;
        private Voxel voxel;

        protected RemoveVoxelIntent(int voxelId, boolean attach) {
            super(attach);
            this.voxelId = voxelId;
        }

        @Override
        protected void applyAction() {
            if (isFirstCall()) {
                voxel = dataContainer.voxels.get(voxelId);
            }
            dataContainer.voxels.remove(voxel.id);
            dataContainer.layers.get(voxel.getLayerId()).removeVoxel(voxel);
        }

        @Override
        protected void unapplyAction() {
            dataContainer.voxels.put(voxel.id, voxel);
            dataContainer.layers.get(voxel.getLayerId()).addVoxel(voxel);
        }

        @Override
        public int[][] effected() {
            return new int[][]{voxel.getPosAsInt()};
        }
    }

    private final class SelectVoxelIntent extends VoxelActionIntent {
        private final int voxelId;
        private final boolean selected;
        private boolean prevSelected;
        private Voxel voxel;

        protected SelectVoxelIntent(int voxelId, boolean selected, boolean attach) {
            super(attach);
            this.voxelId = voxelId;
            this.selected = selected;
        }

        @Override
        protected void applyAction() {
            if (isFirstCall()) {
                voxel = dataContainer.voxels.get(voxelId);
                prevSelected = voxel.isSelected();
            }
            voxel.setSelected(selected);
        }

        @Override
        protected void unapplyAction() {
            voxel.setSelected(prevSelected);
        }

        @Override
        public int[][] effected() {
            return new int[][]{voxel.getPosAsInt()};
        }
    }

    private final class MoveVoxelIntent extends VoxelActionIntent {
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
                Voxel voxel = dataContainer.voxels.get(voxelId);
                historyManagerV.applyIntent(new RemoveVoxelIntent(voxelId, true));

                // remove if something is at new position in this layer
                Voxel toRemove = dataContainer.layers.get(voxel.getLayerId()).search(newPos);
                if (toRemove != null) {
                    historyManagerV.applyIntent(new RemoveVoxelIntent(toRemove.id, true));
                }

                // add the voxel at new position
                historyManagerV.applyIntent(new AddVoxelIntent(voxelId, newPos, voxel.getColor(), voxel.isSelected(), voxel.getTexture(), voxel.getLayerId(), true));

                // what is effected
                effected = new int[][]{voxel.getPosAsInt(), newPos};
            }
        }

        @Override
        protected void unapplyAction() {
            // nothing to do here
        }

        private int[][] effected = null;
        @Override
        public int[][] effected() {
            return effected;
        }
    }

    private final class ColorVoxelIntent extends VoxelActionIntent {
        private final Voxel voxel;
        private final Color newColor;
        private final Color oldColor;

        protected ColorVoxelIntent(int voxelId, Color newColor, boolean attach) {
            super(attach);
            this.voxel = dataContainer.voxels.get(voxelId);
            this.oldColor = voxel.getColor();
            this.newColor = newColor;
            this.effected = new int[][]{voxel.getPosAsInt()};
        }

        @Override
        protected void applyAction() {
            voxel.setColor(newColor);
        }

        @Override
        protected void unapplyAction() {
            voxel.setColor(oldColor);
        }

        private int[][] effected = null;
        @Override
        public int[][] effected() {
            return effected;
        }
    }

    private final class ColorShiftVoxelIntent extends VoxelActionIntent {
        private final Voxel voxel;
        private final Color newColor;
        private final Color oldColor;

        protected ColorShiftVoxelIntent(int voxelId, float[] hsbOffset, boolean attach) {
            super(attach);
            this.voxel = dataContainer.voxels.get(voxelId);
            this.oldColor = voxel.getColor();
            float[] currentHSB = ColorTools.colorToHSB(this.oldColor);
            this.newColor = ColorTools.hsbToColor(new float[] {
                    (currentHSB[0] + hsbOffset[0] + 2) % 1,
                    Math.max(0, Math.min(1, currentHSB[1] + hsbOffset[1])),
                    Math.max(0, Math.min(1, currentHSB[2] + hsbOffset[2]))
            });
            this.effected = new int[][]{voxel.getPosAsInt()};
        }

        @Override
        protected void applyAction() {
            voxel.setColor(newColor);
        }

        @Override
        protected void unapplyAction() {
            voxel.setColor(oldColor);
        }

        private int[][] effected = null;
        @Override
        public int[][] effected() {
            return effected;
        }
    }

    private final class AlphaVoxelIntent extends VoxelActionIntent {
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
                voxel = dataContainer.voxels.get(voxelId);
                oldAlpha = voxel.getAlpha();
                // what is effected
                effected = new int[][]{voxel.getPosAsInt()};
            }
            dataContainer.layers.get(voxel.getLayerId()).setVoxelAlpha(voxel, newAlpha);
        }

        @Override
        protected void unapplyAction() {
            dataContainer.layers.get(voxel.getLayerId()).setVoxelAlpha(voxel, oldAlpha);
        }

        private int[][] effected = null;
        @Override
        public int[][] effected() {
            return effected;
        }
    }

    private final class ClearVoxelIntent extends VoxelActionIntent {
        private final int layerId;

        protected ClearVoxelIntent(int layerId, boolean attach) {
            super(attach);
            this.layerId = layerId;
        }

        @Override
        protected void applyAction() {
            if (isFirstCall()) {
                ArrayList<int[]> effected = new ArrayList<int[]>();

                // get all voxels and remove them
                for (Voxel voxel : dataContainer.layers.get(layerId).getVoxels()) {
                    effected.add(voxel.getPosAsInt());
                    historyManagerV.applyIntent(new RemoveVoxelIntent(voxel.id, true));
                }

                // what is effected
                this.effected = new int[effected.size()][];
                effected.toArray(this.effected);
            }
        }

        @Override
        protected void unapplyAction() {
            // nothing to do
        }

        private int[][] effected = null;
        @Override
        public int[][] effected() {
            return effected;
        }
    }

    private final class MergeLayersIntent extends VoxelActionIntent {

        protected MergeLayersIntent(boolean attach) {
            super(attach);
        }

        @Override
        protected void applyAction() {
            if (isFirstCall()) {
                ArrayList<int[]> effected = new ArrayList<int[]>();

                // create new layer
                int mergedLayerId = getFreeLayerId();
                historyManagerV.applyIntent(new CreateLayerIntent(mergedLayerId, "Merged", true));

                // add the voxels to the new layer (top to bottom)
                for (int layerId : dataContainer.layerOrder) {
                    if (dataContainer.layers.get(layerId).isVisible()) { // only visible
                        Voxel[] voxels = getLayerVoxels(layerId); // get voxels
                        for (Voxel voxel : voxels) {
                            if (dataContainer.layers.get(mergedLayerId).voxelPositionFree(voxel)) { // add if this voxel does not exist
                                effected.add(voxel.getPosAsInt());
                                historyManagerV.applyIntent( // we <need> a new id for this voxel
                                        new AddVoxelIntent(getFreeVoxelId(), voxel.getPosAsInt(),
                                                voxel.getColor(), voxel.isSelected(), voxel.getTexture(), mergedLayerId, true)
                                );
                            }
                        }
                    }
                }

                // delete the visible layers (not the new one)
                Integer[] layer = new Integer[dataContainer.layerOrder.size()];
                dataContainer.layerOrder.toArray(layer);
                for (int layerId : layer) {
                    if (layerId != mergedLayerId && dataContainer.layers.get(layerId).isVisible()) {
                        historyManagerV.applyIntent(new DeleteLayerIntent(layerId, true));
                    }
                }

                // select the new layer (only when created)
                dataContainer.selectedLayer = mergedLayerId;

                // what is effected
                this.effected = new int[effected.size()][];
                effected.toArray(this.effected);
            }
        }

        @Override
        protected void unapplyAction() {
            // nothing to do
        }

        private int[][] effected = null;
        @Override
        public int[][] effected() {
            return effected;
        }
    }

    // texture intents
    private final class AddTextureIntent extends VoxelActionIntent {
        private final ImageIcon texture;
        private final int textureId;

        protected AddTextureIntent(ImageIcon texture, boolean attach) {
            super(attach);
            this.texture = texture;
            textureId = getFreeTextureId();
        }

        @Override
        protected void applyAction() {
            dataContainer.textures.put(textureId, texture);
            if (isFirstCall()) {
                historyManagerV.applyIntent(new SelectTextureIntent(textureId, true));
            }
        }

        @Override
        protected void unapplyAction() {
            dataContainer.textures.remove(textureId);
        }

        @Override
        public int[][] effected() {
            // nothing effected
            return new int[0][];
        }

        // return true if this action effects textures
        public boolean effectsTexture() {
            return true;
        }
    }

    // texture intents
    private final class AddTextureGridIntent extends VoxelActionIntent {
        private final BufferedImage texture;

        protected AddTextureGridIntent(BufferedImage texture, boolean attach) {
            super(attach);
            this.texture = texture;
        }

        @Override
        protected void applyAction() {
            if (isFirstCall()) {
                // split the image and add
                for (int y = 0, lenY = texture.getHeight(); y < lenY; y+= 32) {
                    for (int x = 0, lenX = texture.getWidth(); x < lenX; x+= 32) {
                        historyManagerV.applyIntent(
                                new AddTextureIntent(
                                        new ImageIcon(texture.getSubimage(x, y, 32, 32)), true)
                        );
                    }
                }
            }
        }

        @Override
        protected void unapplyAction() {
            // nothing to do
        }

        @Override
        public int[][] effected() {
            // nothing effected
            return new int[0][];
        }

        // return true if this action effects textures
        public boolean effectsTexture() {
            return true;
        }
    }

    private final class RemoveTextureIntent extends VoxelActionIntent {
        private ImageIcon texture;
        private final int textureId;

        protected RemoveTextureIntent(int textureId, boolean attach) {
            super(attach);
            this.textureId = textureId;
        }

        @Override
        protected void applyAction() {
            if (isFirstCall()) {
                if (textureId == dataContainer.selectedTexture) {
                    historyManagerV.applyIntent(new SelectTextureIntent(-1, true));
                }
            }
            texture = dataContainer.textures.get(textureId);
            dataContainer.textures.remove(textureId);
        }

        @Override
        protected void unapplyAction() {
            dataContainer.textures.put(textureId, texture);
        }

        @Override
        public int[][] effected() {
            // nothing effected
            return new int[0][];
        }

        // return true if this action effects textures
        public boolean effectsTexture() {
            return true;
        }
    }

    // clear the texture list, remove unused texture
    private final class RemoveAllTextureIntent extends VoxelActionIntent {

        private final ArrayList<Integer> unusedTextures;

        protected RemoveAllTextureIntent(ArrayList<Integer> unusedTextures, boolean attach) {
            super(attach);
            this.unusedTextures = unusedTextures;
        }

        @Override
        protected void applyAction() {
            if (isFirstCall()) {
                // deselect texture
                if (unusedTextures.contains(dataContainer.selectedTexture)) {
                    historyManagerV.applyIntent(new SelectTextureIntent(-1, true));
                }
                // remove the textures
                for (int i : unusedTextures) {
                    historyManagerV.applyIntent(new RemoveTextureIntent(i, true));
                }
                // we don't need this data anymore
                unusedTextures.clear();
            }
        }

        @Override
        protected void unapplyAction() {
            // nothing to do here
        }

        @Override
        public int[][] effected() {
            // nothing effected
            return new int[0][];
        }

        // return true if this action effects textures
        public boolean effectsTexture() {
            return true;
        }
    }

    // replace texture in the texture list
    private final class ReplaceTextureIntent extends VoxelActionIntent {
        private final ImageIcon textureNew;
        private final ImageIcon textureOld;
        private final int textureId;

        protected ReplaceTextureIntent(int textureId, ImageIcon texture, boolean attach) {
            super(attach);
            this.textureId = textureId;
            this.textureNew = texture;
            this.textureOld = dataContainer.textures.get(textureId);
        }

        @Override
        protected void applyAction() {
            dataContainer.textures.put(textureId, textureNew);
        }

        @Override
        protected void unapplyAction() {
            dataContainer.textures.put(textureId, textureOld);
        }

        @Override
        public int[][] effected() {
            // nothing effected
            return new int[0][];
        }

        // return true if this action effects textures
        public boolean effectsTexture() {
            return true;
        }
    }

    // select texture in the texture list
    private final class SelectTextureIntent extends VoxelActionIntent {
        private final int oldTextureId;
        private final int newTextureId;

        protected SelectTextureIntent(Integer textureId, boolean attach) {
            super(attach);
            this.newTextureId = textureId;
            oldTextureId = dataContainer.selectedTexture;
        }

        @Override
        protected void applyAction() {
            dataContainer.selectedTexture = newTextureId;
        }

        @Override
        protected void unapplyAction() {
            dataContainer.selectedTexture = oldTextureId;
        }

        @Override
        public int[][] effected() {
            // nothing effected
            return new int[0][];
        }

        // return true if this action effects textures
        public boolean effectsTexture() {
            return true;
        }
    }

    // texture a voxel with a given texture (id)
    private final class TextureVoxelIntent extends VoxelActionIntent {
        private final Voxel voxel;
        private final int[] oldVoxelTexture;
        private final int[] newVoxelTexture;

        protected TextureVoxelIntent(int voxelId, Integer voxelSide, int newTextureId, boolean attach) {
            super(attach);
            this.voxel = dataContainer.voxels.get(voxelId);
            this.oldVoxelTexture = voxel.getTexture();
            if (newTextureId != -1) { // otherwise unset texture
                if (oldVoxelTexture == null || voxelSide == null) {
                    newVoxelTexture = new int[] {
                            newTextureId, newTextureId, newTextureId,
                            newTextureId, newTextureId, newTextureId
                    };
                } else {
                    newVoxelTexture = oldVoxelTexture.clone();
                    newVoxelTexture[voxelSide] = newTextureId;
                }
            } else {
                newVoxelTexture = null;
            }
            // what is effected
            effected = new int[][]{voxel.getPosAsInt()};
        }

        @Override
        protected void applyAction() {
            voxel.setTexture(newVoxelTexture);
        }

        @Override
        protected void unapplyAction() {
            voxel.setTexture(oldVoxelTexture);
        }

        private int[][] effected = null;
        @Override
        public int[][] effected() {
            return effected;
        }
    }

    // texture many voxel at the same time
    private final class MassTextureVoxelIntent extends VoxelActionIntent  {
        private final Integer[] voxelIds;
        private final int textureId;

        protected MassTextureVoxelIntent(Integer[] voxelIds, int textureId, boolean attach) {
            super(attach);

            // what is effected (there could be duplicate positions here)
            effected = new int[voxelIds.length][];
            for (int i = 0; i < effected.length; i++) {
                effected[i] = dataContainer.voxels.get(voxelIds[i]).getPosAsInt();
            }

            this.voxelIds = voxelIds;
            this.textureId = textureId;
        }

        @Override
        protected void applyAction() {
            if (isFirstCall()) {
                for (Integer voxelId : voxelIds) {
                    historyManagerV.applyIntent(new TextureVoxelIntent(voxelId, null, textureId, true));
                }
            }
        }

        @Override
        protected void unapplyAction() {
            // nothing to do
        }

        private int[][] effected = null;
        @Override
        public int[][] effected() {
            return effected;
        }
    }

    // rotate voxel texture
    private final class RotateVoxelTextureIntent extends VoxelActionIntent  {
        private final Voxel voxel;
        private final int voxelSide;

        protected RotateVoxelTextureIntent(int voxelId, int voxelSide, boolean attach) {
            super(attach);
            this.voxel = dataContainer.voxels.get(voxelId);
            this.voxelSide = voxelSide;
            effected = new int[][]{voxel.getPosAsInt()};
        }

        @Override
        protected void applyAction() {
            voxel.rotate(voxelSide);
        }

        @Override
        protected void unapplyAction() {
            voxel.rotateReverse(voxelSide);
        }

        private int[][] effected = null;
        @Override
        public int[][] effected() {
            return effected;
        }
    }

    // flip voxel texture
    private final class FlipVoxelTextureIntent extends VoxelActionIntent  {
        private final Voxel voxel;
        private final int voxelSide;

        protected FlipVoxelTextureIntent(int voxelId, int voxelSide, boolean attach) {
            super(attach);
            this.voxel = dataContainer.voxels.get(voxelId);
            this.voxelSide = voxelSide;
            effected = new int[][]{voxel.getPosAsInt()};
        }

        @Override
        protected void applyAction() {
            voxel.flip(voxelSide);
        }

        @Override
        protected void unapplyAction() {
            voxel.flip(voxelSide);
        }

        private int[][] effected = null;
        @Override
        public int[][] effected() {
            return effected;
        }
    }

    // layer events

    // move to new layer
    private final class MigrateIntent extends VoxelActionIntent {
        private final Voxel[] voxels;

        protected MigrateIntent(Voxel[] voxels, boolean attach) {
            super(attach);
            this.voxels = voxels;
        }

        private Integer[] convertVoxelsToIdArray(Voxel[] voxels) {
            // what is effected (there *should* not be duplicate positions
            // as they are all moved to one new layer)
            effected = new int[voxels.length][];
            for (int i = 0; i < effected.length; i++) {
                effected[i] = voxels[i].getPosAsInt();
            }

            Integer[] voxelIds = new Integer[voxels.length];
            int i = 0;
            for (Voxel voxel : voxels) {
                voxelIds[i++] = voxel.id;
            }
            return voxelIds;
        }

        @Override
        protected void applyAction() {
            if (isFirstCall()) {
                // create a new layer
                int layerId = getFreeLayerId();
                historyManagerV.applyIntent(new CreateLayerIntent(layerId, "Migrated", true));
                // remove all voxels
                historyManagerV.applyIntent(
                        new MassRemoveVoxelIntent(convertVoxelsToIdArray(voxels), true));
                // add all voxels to new layer
                historyManagerV.applyIntent(new MassAddVoxelIntent(voxels, layerId, true));
                // select the new layer
                historyManagerV.applyIntent(new SelectLayerIntent(layerId, true));
            }
        }

        @Override
        protected void unapplyAction() {
            // nothing to do
        }

        private int[][] effected = null;
        @Override
        public int[][] effected() {
            return effected;
        }
    }

    // mass events

    private final class MassSelectVoxelIntent extends VoxelActionIntent {
        private final Integer[] voxelIds;
        private final boolean selected;

        protected MassSelectVoxelIntent(Integer[] voxelIds, boolean selected, boolean attach) {
            super(attach);

            // what is effected (there could be duplicate positions here)
            effected = new int[voxelIds.length][];
            for (int i = 0; i < effected.length; i++) {
                effected[i] = dataContainer.voxels.get(voxelIds[i]).getPosAsInt();
            }

            this.voxelIds = voxelIds;
            this.selected = selected;
        }

        @Override
        protected void applyAction() {
            if (isFirstCall()) {
                for (Integer id : voxelIds) {
                    historyManagerV.applyIntent(new SelectVoxelIntent(id, selected, true));
                }
            }
        }

        @Override
        protected void unapplyAction() {
            // nothing to do
        }

        private int[][] effected = null;
        @Override
        public int[][] effected() {
            return effected;
        }
    }

    private final class MassRemoveVoxelIntent extends VoxelActionIntent {
        private final Integer[] voxelIds;

        protected MassRemoveVoxelIntent(Integer[] voxelIds, boolean attach) {
            super(attach);

            // what is effected (there could be duplicate positions here)
            effected = new int[voxelIds.length][];
            for (int i = 0; i < effected.length; i++) {
                effected[i] = dataContainer.voxels.get(voxelIds[i]).getPosAsInt();
            }

            this.voxelIds = voxelIds;
        }

        @Override
        protected void applyAction() {
            if (isFirstCall()) {
                for (Integer id : voxelIds) {
                    historyManagerV.applyIntent(new RemoveVoxelIntent(id, true));
                }
            }
        }

        @Override
        protected void unapplyAction() {
            // nothing to do
        }

        private int[][] effected = null;
        @Override
        public int[][] effected() {
            return effected;
        }
    }

    // if the layerid is null the voxel layerId will be used,
    // otherwise the provided layerid
    // the voxel id is never used (!)
    private final class MassAddVoxelIntent extends VoxelActionIntent {
        private final Voxel[] voxels;
        private final Integer layerId;

        protected MassAddVoxelIntent(Voxel[] voxels, Integer layerId, boolean attach) {
            super(attach);

            // what is effected (there could be duplicate positions here)
            effected = new int[voxels.length][];
            for (int i = 0; i < effected.length; i++) {
                effected[i] = voxels[i].getPosAsInt();
            }

            this.voxels = voxels;
            this.layerId = layerId;
        }

        @Override
        protected void applyAction() {
            if (isFirstCall()) {
                boolean layerIdSet = layerId != null;
                for (Voxel voxel : voxels) {
                    historyManagerV.applyIntent(
                            new AddVoxelIntent(getFreeVoxelId(), voxel.getPosAsInt(),
                                    voxel.getColor(), voxel.isSelected(), voxel.getTexture(), layerIdSet ? layerId : voxel.getLayerId(), true));
                }
            }
        }

        @Override
        protected void unapplyAction() {
            // nothing to do
        }

        private int[][] effected = null;
        @Override
        public int[][] effected() {
            return effected;
        }
    }

    private final class MassColorVoxelIntent extends VoxelActionIntent  {
        private final Integer[] voxelIds;
        private final Color color;

        protected MassColorVoxelIntent(Integer[] voxelIds, Color color, boolean attach) {
            super(attach);

            // what is effected (there could be duplicate positions here)
            effected = new int[voxelIds.length][];
            for (int i = 0; i < effected.length; i++) {
                effected[i] = dataContainer.voxels.get(voxelIds[i]).getPosAsInt();
            }

            this.voxelIds = voxelIds;
            this.color = color;
        }

        @Override
        protected void applyAction() {
            if (isFirstCall()) {
                for (Integer voxelId : voxelIds) {
                    historyManagerV.applyIntent(new ColorVoxelIntent(voxelId, color, true));
                }
            }
        }

        @Override
        protected void unapplyAction() {
            // nothing to do
        }

        private int[][] effected = null;
        @Override
        public int[][] effected() {
            return effected;
        }
    }

    private final class MassColorShiftVoxelIntent extends VoxelActionIntent  {
        private final Integer[] voxelIds;
        private final float[] hsbOffset;

        protected MassColorShiftVoxelIntent(Integer[] voxelIds, float[] hsbOffset, boolean attach) {
            super(attach);

            // what is effected (there could be duplicate positions here)
            effected = new int[voxelIds.length][];
            for (int i = 0; i < effected.length; i++) {
                effected[i] = dataContainer.voxels.get(voxelIds[i]).getPosAsInt();
            }

            this.voxelIds = voxelIds;
            this.hsbOffset = hsbOffset;
        }

        @Override
        protected void applyAction() {
            if (isFirstCall()) {
                for (Integer voxelId : voxelIds) {
                    historyManagerV.applyIntent(new ColorShiftVoxelIntent(voxelId, hsbOffset, true));
                }
            }
        }

        @Override
        protected void unapplyAction() {
            // nothing to do
        }

        private int[][] effected = null;
        @Override
        public int[][] effected() {
            return effected;
        }
    }

    private final class MassMoveVoxelIntent extends VoxelActionIntent  {
        private final Voxel[] voxels;
        private final int[] shift;

        protected MassMoveVoxelIntent(Voxel[] voxels, int[] shift, boolean attach) {
            super(attach);
            this.voxels = voxels;
            this.shift = shift;
        }

        @Override
        protected void applyAction() {
            if (isFirstCall()) {

                // what is effected (there could be duplicate positions here)
                effected = new int[voxels.length*2][];

                // generate the ids
                Integer[] voxelIds = new Integer[voxels.length];
                for (int i = 0; i < voxels.length; i++) {
                    voxelIds[i] = voxels[i].id;
                }
                // remove all voxels
                historyManagerV.applyIntent(new MassRemoveVoxelIntent(voxelIds, true));

                // create new voxels (with new position) and delete
                // existing voxels at those positions
                Voxel[] shiftedVoxels = new Voxel[voxels.length];
                for (int i = 0; i < voxels.length; i++) {
                    Voxel voxel = voxels[i];
                    int[] pos = voxel.getPosAsInt();
                    effected[i] = voxel.getPosAsInt(); // what is effected
                    pos[0] -= shift[0];
                    pos[1] -= shift[1];
                    pos[2] -= shift[2];
                    effected[i + voxels.length] = pos.clone(); // what is effected
                    shiftedVoxels[i] = new Voxel(voxel.id, pos, voxel.getColor(), voxel.isSelected(), voxel.getTexture(), voxel.getLayerId());
                    // remove existing voxels in this layer
                    Voxel result = dataContainer.layers.get(voxel.getLayerId()).search(pos);
                    if (result != null) {
                        historyManagerV.applyIntent(new RemoveVoxelIntent(result.id, true));
                    }
                }
                // (re)add all the shifted voxels (null ~ the voxel layer id is used)
                historyManagerV.applyIntent(new MassAddVoxelIntent(shiftedVoxels, null, true));
            }
        }

        @Override
        protected void unapplyAction() {
            // nothing to do
        }

        private int[][] effected = null;
        @Override
        public int[][] effected() {
            return effected;
        }
    }

    // rotate voxel around their center (but not the voxel "texture" itself)
    private final class RotateVoxelCenterIntent extends VoxelActionIntent  {
        private final Voxel[] voxels;
        private final int axe;
        private final float angle;

        protected RotateVoxelCenterIntent(Voxel[] voxels, int axe, float angle, boolean attach) {
            super(attach);
            this.voxels = voxels;
            this.axe = axe;
            this.angle = angle;
        }

        @Override
        protected void applyAction() {
            if (isFirstCall()) {

                // what is effected (there could be duplicate positions here)
                effected = new int[voxels.length*2][];

                // generate the ids and find center
                int[] centerMin = null;
                int[] centerMax = null;
                Integer[] voxelIds = new Integer[voxels.length];
                for (int i = 0; i < voxels.length; i++) {
                    voxelIds[i] = voxels[i].id;
                    Voxel voxel = voxels[i];
                    if (centerMin == null) {
                        centerMin = voxel.getPosAsInt();
                        centerMax = voxel.getPosAsInt();
                    }
                    centerMin[0] = Math.min(centerMin[0],voxel.x);
                    centerMin[1] = Math.min(centerMin[1],voxel.y);
                    centerMin[2] = Math.min(centerMin[2],voxel.z);
                    centerMax[0] = Math.max(centerMax[0],voxel.x);
                    centerMax[1] = Math.max(centerMax[1],voxel.y);
                    centerMax[2] = Math.max(centerMax[2],voxel.z);
                }
                // calculate center - note: voxels.length must not be zero
                assert centerMin != null;
                float[] center = new float[] {
                        (centerMin[0]/(float)2 + centerMax[0]/(float)2),
                        (centerMin[1]/(float)2 + centerMax[1]/(float)2),
                        (centerMin[2]/(float)2 + centerMax[2]/(float)2)
                };

                int rot1 = 0;
                int rot2 = 2;
                switch (axe) {
                    case 2:
                        rot1 = 0;
                        rot2 = 1;
                        break;
                    case 1:
                        rot1 = 0;
                        rot2 = 2;
                        break;
                    case 0:
                        rot1 = 1;
                        rot2 = 2;
                        break;
                    default: break;
                }

                // remove all voxels
                historyManagerV.applyIntent(new MassRemoveVoxelIntent(voxelIds, true));

                // create new voxels (with new position) and delete
                // existing voxels at those positions
                Voxel[] shiftedVoxels = new Voxel[voxels.length];
                for (int i = 0; i < voxels.length; i++) {
                    Voxel voxel = voxels[i];
                    int[] pos = voxel.getPosAsInt();
                    effected[i] = voxel.getPosAsInt(); // what is effected

                    // rotate the point around the center
                    // todo check for duplicates (overlaps when rotating values other than 90, 180, 270)
                    double[] pt = {pos[rot1], pos[rot2]};
                    AffineTransform.getRotateInstance(Math.toRadians(angle), center[rot1], center[rot2])
                            .transform(pt, 0, pt, 0, 1); // specifying to use this double[] to hold coords
                    pos[rot1] = (int)Math.round(pt[0]);
                    pos[rot2] = (int)Math.round(pt[1]);

                    effected[i + voxels.length] = pos.clone(); // what is effected
                    shiftedVoxels[i] = new Voxel(voxel.id, pos, voxel.getColor(), voxel.isSelected(), voxel.getTexture(), voxel.getLayerId());
                    // remove existing voxels in this layer
                    Voxel result = dataContainer.layers.get(voxel.getLayerId()).search(pos);
                    if (result != null) {
                        historyManagerV.applyIntent(new RemoveVoxelIntent(result.id, true));
                    }
                }
                // (re)add all the rotated voxels (null ~ the voxel layer id is used)
                historyManagerV.applyIntent(new MassAddVoxelIntent(shiftedVoxels, null, true));
            }
        }

        @Override
        protected void unapplyAction() {
            // nothing to do
        }

        private int[][] effected = null;
        @Override
        public int[][] effected() {
            return effected;
        }
    }

    private final class MirrorVoxelIntent extends VoxelActionIntent  {
        private final Voxel[] voxels;
        private final int axe;

        protected MirrorVoxelIntent(Voxel[] voxels, int axe, boolean attach) {
            super(attach);
            this.voxels = voxels;
            this.axe = axe;
        }

        @Override
        protected void applyAction() {
            if (isFirstCall()) {

                // what is effected (there could be duplicate positions here)
                effected = new int[voxels.length*2][];

                // generate the ids and find center
                int[] centerMin = null;
                int[] centerMax = null;
                Integer[] voxelIds = new Integer[voxels.length];
                for (int i = 0; i < voxels.length; i++) {
                    voxelIds[i] = voxels[i].id;
                    Voxel voxel = voxels[i];
                    if (centerMin == null) {
                        centerMin = voxel.getPosAsInt();
                        centerMax = voxel.getPosAsInt();
                    }
                    centerMin[0] = Math.min(centerMin[0],voxel.x);
                    centerMin[1] = Math.min(centerMin[1],voxel.y);
                    centerMin[2] = Math.min(centerMin[2],voxel.z);
                    centerMax[0] = Math.max(centerMax[0],voxel.x);
                    centerMax[1] = Math.max(centerMax[1],voxel.y);
                    centerMax[2] = Math.max(centerMax[2],voxel.z);
                }
                // calculate center - note: voxels.length must not be zero
                assert centerMin != null;
                float[] center = new float[] {
                        (centerMin[0]/(float)2 + centerMax[0]/(float)2),
                        (centerMin[1]/(float)2 + centerMax[1]/(float)2),
                        (centerMin[2]/(float)2 + centerMax[2]/(float)2)
                };

                // remove all voxels
                historyManagerV.applyIntent(new MassRemoveVoxelIntent(voxelIds, true));

                // create new voxels (with new position) and delete
                // existing voxels at those positions
                Voxel[] shiftedVoxels = new Voxel[voxels.length];
                for (int i = 0; i < voxels.length; i++) {
                    Voxel voxel = voxels[i];
                    int[] pos = voxel.getPosAsInt();
                    effected[i] = voxel.getPosAsInt(); // what is effected

                    // switch the point with the center
                    pos[axe] = Math.round(- pos[axe] + 2*center[axe]);

                    effected[i + voxels.length] = pos.clone(); // what is effected
                    shiftedVoxels[i] = new Voxel(voxel.id, pos, voxel.getColor(), voxel.isSelected(), voxel.getTexture(), voxel.getLayerId());
                    // remove existing voxels in this layer
                    Voxel result = dataContainer.layers.get(voxel.getLayerId()).search(pos);
                    if (result != null) {
                        historyManagerV.applyIntent(new RemoveVoxelIntent(result.id, true));
                    }
                }
                // (re)add all the rotated voxels (null ~ the voxel layer id is used)
                historyManagerV.applyIntent(new MassAddVoxelIntent(shiftedVoxels, null, true));
            }
        }

        @Override
        protected void unapplyAction() {
            // nothing to do
        }

        private int[][] effected = null;
        @Override
        public int[][] effected() {
            return effected;
        }
    }

    // ##################### PRIVATE HELPER FUNCTIONS
    // returns a free voxel id
    private int lastVoxel = -1;
    private int getFreeVoxelId() {
        do {
            lastVoxel++;
        } while (dataContainer.voxels.containsKey(lastVoxel));
        return lastVoxel;
    }

    // returns a free layer id
    private int lastLayer = -1;
    private int getFreeLayerId() {
        do {
            lastLayer++;
        } while (dataContainer.layers.containsKey(lastLayer));
        return lastLayer;
    }

    // returns a free texture id
    private int lastTexture = -1;
    private int getFreeTextureId() {
        do {
            lastTexture++;
        } while (dataContainer.textures.containsKey(lastTexture));
        return lastTexture;
    }

    // =========================
    // === interface methods ===
    // =========================

    @Override
    public final int addVoxelDirect(Color color, int[] pos) {
        synchronized (VitcoSettings.SYNC) {
            int result = -1;
            VoxelLayer layer = dataContainer.layers.get(dataContainer.selectedLayer);
            if (layer != null && layer.voxelPositionFree(pos)) {
                result = getFreeVoxelId();
                Voxel voxel = new Voxel(result, pos, color, false, null, dataContainer.selectedLayer);
                dataContainer.voxels.put(voxel.id, voxel);
                dataContainer.layers.get(voxel.getLayerId()).addVoxel(voxel);
            }
            return result;
        }
    }

    @Override
    public final int addVoxel(Color color, int[] textureId, int[] pos) {
        synchronized (VitcoSettings.SYNC) {
            int result = -1;
            VoxelLayer layer = dataContainer.layers.get(dataContainer.selectedLayer);
            if (layer != null && layer.getSize() < VitcoSettings.MAX_VOXEL_COUNT_PER_LAYER && layer.voxelPositionFree(pos)) {
                result = getFreeVoxelId();
                historyManagerV.applyIntent(new AddVoxelIntent(result, pos, color, false, textureId, dataContainer.selectedLayer, false));
            }
            return result;
        }
    }

    @Override
    public final boolean massAddVoxel(Voxel[] voxels) {
        synchronized (VitcoSettings.SYNC) {
            boolean result = false;
            VoxelLayer layer = dataContainer.layers.get(dataContainer.selectedLayer);
            if (layer != null) {
                ArrayList<Voxel> validVoxel = new ArrayList<Voxel>();
                HashSet<String> voxelPos = new HashSet<String>();
                for (Voxel voxel : voxels) {
                    String posHash = voxel.getPosAsString();
                    if (layer.voxelPositionFree(voxel)
                            && !voxelPos.contains(posHash)) {
                        validVoxel.add(voxel);
                        voxelPos.add(posHash);
                    }
                }
                if (validVoxel.size() > 0 && layer.getSize() + validVoxel.size() <= VitcoSettings.MAX_VOXEL_COUNT_PER_LAYER) {
                    Voxel[] valid = new Voxel[validVoxel.size()];
                    validVoxel.toArray(valid);
                    historyManagerV.applyIntent(new MassAddVoxelIntent(valid, layer.id, false));
                    result = true;
                }
            }
            return result;
        }
    }

    @Override
    public final boolean removeVoxel(int voxelId) {
        synchronized (VitcoSettings.SYNC) {
            boolean result = false;
            if (dataContainer.voxels.containsKey(voxelId)) {
                historyManagerV.applyIntent(new RemoveVoxelIntent(voxelId, false));
                result = true;
            }
            return result;
        }
    }

    @Override
    public final boolean massRemoveVoxel(Integer[] voxelIds) {
        synchronized (VitcoSettings.SYNC) {
            ArrayList<Integer> validVoxel = new ArrayList<Integer>();
            for (int voxelId : voxelIds) {
                if (dataContainer.voxels.containsKey(voxelId)) {
                    validVoxel.add(voxelId);
                }
            }
            if (validVoxel.size() > 0) {
                Integer[] valid = new Integer[validVoxel.size()];
                validVoxel.toArray(valid);
                historyManagerV.applyIntent(new MassRemoveVoxelIntent(valid, false));
                return true;
            } else {
                return false;
            }
        }
    }

    @Override
    public final boolean moveVoxel(int voxelId, int[] newPos) {
        synchronized (VitcoSettings.SYNC) {
            boolean result = false;
            Voxel voxel = dataContainer.voxels.get(voxelId);
            if (voxel != null) {
                historyManagerV.applyIntent(new MoveVoxelIntent(voxel.id, newPos, false));
                result = true;
            }
            return result;
        }
    }

    @Override
    public final boolean massMoveVoxel(Voxel[] voxel, int[] shift) {
        synchronized (VitcoSettings.SYNC) {
            boolean result = false;
            if (voxel.length > 0 && (shift[0] != 0 || shift[1] != 0 || shift[2] != 0)) {
                historyManagerV.applyIntent(new MassMoveVoxelIntent(voxel, shift.clone(), false));
                result = true;
            }
            return result;
        }
    }

    // rotate voxel around their center (but not the voxel "texture" itself)
    @Override
    public final boolean rotateVoxelCenter(Voxel[] voxel, int axe, float degree) {
        synchronized (VitcoSettings.SYNC) {
            boolean result = false;
            if (voxel.length > 0 && degree/360 != 0 && axe <= 2 && axe >= 0) {
                historyManagerV.applyIntent(new VoxelData.RotateVoxelCenterIntent(voxel, axe, degree, false));
                result = true;
            }
            return result;
        }
    }

    @Override
    public final boolean mirrorVoxel(Voxel[] voxel, int axe) {
        synchronized (VitcoSettings.SYNC) {
            boolean result = false;
            if (voxel.length > 0 && axe <= 2 && axe >= 0) {
                historyManagerV.applyIntent(new MirrorVoxelIntent(voxel, axe, false));
                result = true;
            }
            return result;
        }
    }

    @Override
    public final Voxel getVoxel(int voxelId) {
        synchronized (VitcoSettings.SYNC) {
            Voxel result = null;
            if (dataContainer.voxels.containsKey(voxelId)) {
                result = dataContainer.voxels.get(voxelId);
            }
            return result;
        }
    }

    @Override
    public final boolean setColor(int voxelId, Color color) {
        synchronized (VitcoSettings.SYNC) {
            boolean result = false;
            if (dataContainer.voxels.containsKey(voxelId) &&
                    (!dataContainer.voxels.get(voxelId).getColor().equals(color) ||
                            dataContainer.voxels.get(voxelId).getTexture() != null)) {
                historyManagerV.applyIntent(new ColorVoxelIntent(voxelId, color, false));
                result = true;
            }
            return result;
        }
    }

    @Override
    public final boolean massSetColor(Integer[] voxelIds, Color color) {
        synchronized (VitcoSettings.SYNC) {
            ArrayList<Integer> validVoxel = new ArrayList<Integer>();
            for (int voxelId : voxelIds) {
                Voxel voxel = dataContainer.voxels.get(voxelId);
                if (voxel != null && !voxel.getColor().equals(color)) {
                    validVoxel.add(voxelId);
                }
            }
            if (validVoxel.size() > 0) {
                Integer[] valid = new Integer[validVoxel.size()];
                validVoxel.toArray(valid);
                historyManagerV.applyIntent(new MassColorVoxelIntent(valid, color, false));
                return true;
            } else {
                return false;
            }
        }
    }

    @Override
    public final boolean massShiftColor(Integer[] voxelIds, float[] hsbOffset) {
        synchronized (VitcoSettings.SYNC) {
            ArrayList<Integer> validVoxel = new ArrayList<Integer>();
            for (int voxelId : voxelIds) {
                Voxel voxel = dataContainer.voxels.get(voxelId);
                if (voxel != null) {
                    validVoxel.add(voxelId);
                }
            }
            if (validVoxel.size() > 0) {
                Integer[] valid = new Integer[validVoxel.size()];
                validVoxel.toArray(valid);
                historyManagerV.applyIntent(new MassColorShiftVoxelIntent(valid, hsbOffset, false));
                return true;
            } else {
                return false;
            }
        }
    }

    @Override
    public final Color getColor(int voxelId) {
        synchronized (VitcoSettings.SYNC) {
            Color result = null;
            if (dataContainer.voxels.containsKey(voxelId)) {
                result = dataContainer.voxels.get(voxelId).getColor();
            }
            return result;
        }
    }

    @Override
    public final boolean setAlpha(int voxelId, int alpha) {
        synchronized (VitcoSettings.SYNC) {
            boolean result = false;
            if (dataContainer.voxels.containsKey(voxelId) && dataContainer.voxels.get(voxelId).getAlpha() != alpha) {
                historyManagerV.applyIntent(new AlphaVoxelIntent(voxelId, alpha, false));
                result = true;
            }
            return result;
        }
    }

    @Override
    public final int getAlpha(int voxelId) {
        synchronized (VitcoSettings.SYNC) {
            int result = -1;
            if (dataContainer.voxels.containsKey(voxelId)) {
                result = dataContainer.voxels.get(voxelId).getAlpha();
            }
            return result;
        }
    }

    @Override
    public final int getLayer(int voxelId) {
        synchronized (VitcoSettings.SYNC) {
            int result = -1;
            if (dataContainer.voxels.containsKey(voxelId)) {
                result = dataContainer.voxels.get(voxelId).getLayerId();
            }
            return result;
        }
    }

    @Override
    public final boolean clearV(int layerId) {
        synchronized (VitcoSettings.SYNC) {
            boolean result = false;
            if (dataContainer.layers.containsKey(layerId)) {
                if (dataContainer.layers.get(layerId).getSize() > 0) {
                    historyManagerV.applyIntent(new ClearVoxelIntent(layerId, false));
                    result = true;
                }
            }
            return result;
        }
    }

    @Override
    public final Voxel searchVoxel(int[] pos, int layerId) {
        synchronized (VitcoSettings.SYNC) {
            return dataContainer.layers.get(layerId).search(pos);
        }
    }

    @Override
    public final Voxel searchVoxel(int[] pos, boolean onlyCurrentLayer) {
        synchronized (VitcoSettings.SYNC) {
            if (onlyCurrentLayer) { // search only the current layers
                VoxelLayer layer = dataContainer.layers.get(dataContainer.selectedLayer);
                if (layer != null && layer.isVisible()) {
                    Voxel result = layer.search(pos);
                    if (result != null) {
                        return result;
                    }
                }
            } else { // search all layers in correct order
                for (Integer layerId : dataContainer.layerOrder) {
                    if (dataContainer.layers.get(layerId).isVisible()) {
                        Voxel result = dataContainer.layers.get(layerId).search(pos);
                        if (result != null) {
                            return result;
                        }
                    }
                }
            }
            return null;
        }
    }

    // ================================ selection of voxels

    // select a voxel
    @Override
    public final boolean setVoxelSelected(int voxelId, boolean selected) {
        synchronized (VitcoSettings.SYNC) {
            boolean result = false;
            if (dataContainer.voxels.containsKey(voxelId) && dataContainer.voxels.get(voxelId).isSelected() != selected) {
                historyManagerV.applyIntent(new SelectVoxelIntent(voxelId, selected, false));
                result = true;
            }
            return result;
        }
    }

    @Override
    public final boolean isSelected(int voxelId) {
        synchronized (VitcoSettings.SYNC) {
            return dataContainer.voxels.containsKey(voxelId) && dataContainer.voxels.get(voxelId).isSelected();
        }
    }

    private final HashMap<String, TIntObjectHashMap<int[]>> changedSelectedVoxel = new HashMap<String, TIntObjectHashMap<int[]>>();
    @Override
    public final Voxel[][] getNewSelectedVoxel(String requestId) {
        synchronized (VitcoSettings.SYNC) {
            if (!changedSelectedVoxel.containsKey(requestId)) {
                changedSelectedVoxel.put(requestId, null);
            }
            if (changedSelectedVoxel.get(requestId) == null) {
                changedSelectedVoxel.put(requestId, new TIntObjectHashMap<int[]>());
                return new Voxel[][] {null, getSelectedVoxels()};
            } else {
                ArrayList<Voxel> removed = new ArrayList<Voxel>();
                ArrayList<Voxel> added = new ArrayList<Voxel>();
                for (int[] pos : changedSelectedVoxel.get(requestId).valueCollection()) {
                    Voxel voxel = searchVoxel(pos, false);
                    if (voxel != null && voxel.isSelected()) {
                        added.add(voxel);
                    } else {
                        removed.add(new Voxel(-1, pos, null, false, null, -1));
                    }
                }
                Voxel[][] result = new Voxel[2][];
                result[0] = new Voxel[removed.size()];
                removed.toArray(result[0]);
                result[1] = new Voxel[added.size()];
                added.toArray(result[1]);
                changedSelectedVoxel.get(requestId).clear();
                return result;
            }
        }
    }

    // get selected visible voxels
    @Override
    public final Voxel[] getSelectedVoxels() {
        synchronized (VitcoSettings.SYNC) {
            if (!selectedVoxelBufferValid) {
                // get all presented voxels
                Voxel voxels[] = _getVisibleLayerVoxel();
                // filter the selected
                ArrayList<Voxel> selected = new ArrayList<Voxel>();
                for (Voxel voxel : voxels) {
                    if (voxel.isSelected()) {
                        selected.add(voxel);
                    }
                }
                selectedVoxelBuffer = new Voxel[selected.size()];
                selected.toArray(selectedVoxelBuffer);
                selectedVoxelBufferValid = true;
            }
            return selectedVoxelBuffer.clone();
        }
    }

    @Override
    public final boolean massSetVoxelSelected(Integer[] voxelIds, boolean selected) {
        synchronized (VitcoSettings.SYNC) {
            ArrayList<Integer> validVoxel = new ArrayList<Integer>();
            for (Integer voxelId : voxelIds) {
                if (dataContainer.voxels.containsKey(voxelId) && dataContainer.voxels.get(voxelId).isSelected() != selected) {
                    validVoxel.add(voxelId);
                }
            }
            if (validVoxel.size() > 0) {
                Integer[] valid = new Integer[validVoxel.size()];
                validVoxel.toArray(valid);
                historyManagerV.applyIntent(new MassSelectVoxelIntent(valid, selected, false));
                return true;
            } else {
                return false;
            }
        }
    }

    @Override
    public final boolean migrateVoxels(Voxel[] voxels) {
        synchronized (VitcoSettings.SYNC) {
            boolean result = false;
            if (voxels.length > 0 && voxels.length <= VitcoSettings.MAX_VOXEL_COUNT_PER_LAYER) {
                historyManagerV.applyIntent(new MigrateIntent(voxels, false));
                result = true;
            }
            return result;
        }
    }


    // =============================

    Voxel[] layerVoxelBuffer = new Voxel[0];
    boolean layerVoxelBufferValid = false;
    int layerVoxelBufferLastLayer;
    @Override
    public final Voxel[] getLayerVoxels(int layerId) {
        synchronized (VitcoSettings.SYNC) {
            if (!layerVoxelBufferValid || layerVoxelBufferLastLayer != layerId) {
                VoxelLayer layer = dataContainer.layers.get(layerId);
                if (layer != null) {
                    layerVoxelBuffer = layer.getVoxels();
                } else {
                    layerVoxelBuffer = new Voxel[0];
                }
                layerVoxelBufferValid = true;
                layerVoxelBufferLastLayer = layerId;
            }
            return layerVoxelBuffer.clone();
        }
    }

    // get the new visible voxels, NOTE: if first element of array is null
    // this means that everything is erased
    private final HashMap<String, TIntObjectHashMap<int[]>> changedVisibleVoxel = new HashMap<String, TIntObjectHashMap<int[]>>();
    @Override
    public final Voxel[][] getNewVisibleLayerVoxel(String requestId) {
        synchronized (VitcoSettings.SYNC) {
            if (!changedVisibleVoxel.containsKey(requestId)) {
                changedVisibleVoxel.put(requestId, null);
            }
            if (changedVisibleVoxel.get(requestId) == null) {
                changedVisibleVoxel.put(requestId, new TIntObjectHashMap<int[]>());
                return new Voxel[][] {null, _getVisibleLayerVoxel()};
            } else {
                ArrayList<Voxel> removed = new ArrayList<Voxel>();
                ArrayList<Voxel> added = new ArrayList<Voxel>();
                for (int[] pos : changedVisibleVoxel.get(requestId).valueCollection()) {
                    Voxel voxel = searchVoxel(pos, false);
                    if (voxel != null) {
                        added.add(voxel);
                    } else {
                        removed.add(new Voxel(-1, pos, null, false, null, -1));
                    }
                }
                Voxel[][] result = new Voxel[2][];
                result[0] = new Voxel[removed.size()];
                removed.toArray(result[0]);
                result[1] = new Voxel[added.size()];
                added.toArray(result[1]);
                changedVisibleVoxel.get(requestId).clear();
                return result;
            }
        }
    }

    // internal function, heavy!
    Voxel[] visibleLayerVoxelInternalBuffer = new Voxel[0];
    boolean visibleLayerVoxelInternalBufferValid = false;
    private Voxel[] _getVisibleLayerVoxel() {
        if (!visibleLayerVoxelInternalBufferValid) {
            VoxelLayer result = new VoxelLayer(-1, "tmp");
            for (Integer layerId : dataContainer.layerOrder) {
                if (dataContainer.layers.get(layerId).isVisible()) {
                    Voxel[] voxels = dataContainer.layers.get(layerId).getVoxels();
                    for (Voxel voxel : voxels) {
                        if (result.voxelPositionFree(voxel)) {
                            result.addVoxel(voxel);
                        }
                    }
                }
            }
            visibleLayerVoxelInternalBuffer = result.getVoxels();
            visibleLayerVoxelInternalBufferValid = true;
        }
        return visibleLayerVoxelInternalBuffer.clone();
    }

    // returns visible voxels
    @Override
    public final Voxel[] getVisibleLayerVoxel() {
        synchronized (VitcoSettings.SYNC) {
            updateVisVoxTreeInternal();
            return visibleLayerVoxelBuffer;
        }
    }

    // helper to update buffers for visible voxels
    private Voxel[] visibleLayerVoxelBuffer = new Voxel[0];
    private boolean anyVoxelsVisibleBuffer = false;
    private final HashMap<String, Voxel> visVoxelList = new HashMap<String, Voxel>();
    private void updateVisVoxTreeInternal() {
        Voxel[][] newV = getNewVisibleLayerVoxel("___internal___visible_list");
        if (newV[0] == null) {
            visVoxelList.clear();
        } else {
            for (Voxel removed : newV[0]) {
                visVoxelList.remove(removed.getPosAsString());
            }
        }
        for (Voxel added : newV[1]) {
            visVoxelList.put(added.getPosAsString(), added);
        }
        // update the buffer
        if (newV[0]== null || newV[0].length > 0 || newV[1].length > 0) {
            visibleLayerVoxelBuffer = new Voxel[visVoxelList.size()];
            visVoxelList.values().toArray(visibleLayerVoxelBuffer);
            anyVoxelsVisibleBuffer = visibleLayerVoxelBuffer.length > 0;
        }
    }

    // true iff any voxel are visible
    @Override
    public final boolean anyLayerVoxelVisible() {
        synchronized (VitcoSettings.SYNC) {
            updateVisVoxTreeInternal();
            return anyVoxelsVisibleBuffer;
        }
    }

    // true iff any voxel are selected
    @Override
    public final boolean anyVoxelSelected() {
        synchronized (VitcoSettings.SYNC) {
            if (!selectedVoxelBufferValid) {
                return getSelectedVoxels().length > 0;
            } else {
                return selectedVoxelBuffer.length > 0;
            }
        }
    }

    // to invalidate the side view buffer
    @Override
    public final void invalidateSideViewBuffer(String requestId, Integer side, Integer plane) {
        synchronized (VitcoSettings.SYNC) {
            // make sure this plane is set
            if (!changedVisibleVoxelPlane.containsKey(side)) {
                changedVisibleVoxelPlane.put(side, new HashMap<String, TIntObjectHashMap<TIntObjectHashMap<int[]>>>());
            }
            // make sure the requestId is set
            if (!changedVisibleVoxelPlane.get(side).containsKey(requestId)) {
                changedVisibleVoxelPlane.get(side).put(requestId, new TIntObjectHashMap<TIntObjectHashMap<int[]>>());
            }
            // make sure this plane has no information stored (force complete refresh)
            changedVisibleVoxelPlane.get(side).get(requestId).remove(plane);
        }
    }

    // side -> requestId -> plane -> positions
    private final TIntObjectHashMap<HashMap<String, TIntObjectHashMap<TIntObjectHashMap<int[]>>>> changedVisibleVoxelPlane
            = new TIntObjectHashMap<HashMap<String, TIntObjectHashMap<TIntObjectHashMap<int[]>>>>();
    @Override
    public final Voxel[][] getNewSideVoxel(String requestId, Integer side, Integer plane) {
        synchronized (VitcoSettings.SYNC) {
            // default result (delete all + empty)
            Voxel[][] result = new Voxel[][]{null, new Voxel[0]};
            // make sure this plane is set
            if (!changedVisibleVoxelPlane.containsKey(side)) {
                changedVisibleVoxelPlane.put(side, new HashMap<String, TIntObjectHashMap<TIntObjectHashMap<int[]>>>());
            }
            // make sure the requestId is set
            if (!changedVisibleVoxelPlane.get(side).containsKey(requestId)) {
                changedVisibleVoxelPlane.get(side).put(requestId, new TIntObjectHashMap<TIntObjectHashMap<int[]>>());
            }

            if (changedVisibleVoxelPlane.get(side).get(requestId).get(plane) == null) {
                // if the plane is null, fetch all data and set it no empty
                switch (side) {
                    case 0:
                        result = new Voxel[][] {null, getVoxelsXY(plane)};
                        break;
                    case 1:
                        result = new Voxel[][] {null, getVoxelsXZ(plane)};
                        break;
                    case 2:
                        result = new Voxel[][] {null, getVoxelsYZ(plane)};
                        break;
                    default: break;
                }
                // reset
                changedVisibleVoxelPlane.get(side).get(requestId).put(plane, new TIntObjectHashMap<int[]>());
            } else {
                // if there are changed positions, notify only those positions
                ArrayList<Voxel> removed = new ArrayList<Voxel>();
                ArrayList<Voxel> added = new ArrayList<Voxel>();
                for (int[] pos : changedVisibleVoxelPlane.get(side).get(requestId).get(plane).valueCollection()) {
                    Voxel voxel = searchVoxel(pos, false);
                    if (voxel != null) {
                        added.add(voxel);
                    } else {
                        removed.add(new Voxel(-1, pos, null, false, null, -1));
                    }
                }
                result = new Voxel[2][];
                result[0] = new Voxel[removed.size()];
                removed.toArray(result[0]);
                result[1] = new Voxel[added.size()];
                added.toArray(result[1]);
                // these changes have now been forwarded
                changedVisibleVoxelPlane.get(side).get(requestId).get(plane).clear();
            }
            // return the result
            return result;
        }
    }

    @Override
    public final Voxel[] getVoxelsXY(int z, int layerId) {
        synchronized (VitcoSettings.SYNC) {
            if (dataContainer.layers.containsKey(layerId)) {
                return dataContainer.layers.get(layerId).getZPlane(z);
            }
            return null;
        }
    }

    @Override
    public final Voxel[] getVoxelsXZ(int y, int layerId) {
        synchronized (VitcoSettings.SYNC) {
            if (dataContainer.layers.containsKey(layerId)) {
                return dataContainer.layers.get(layerId).getYPlane(y);
            }
            return null;
        }
    }

    @Override
    public final Voxel[] getVoxelsYZ(int x, int layerId) {
        synchronized (VitcoSettings.SYNC) {
            if (dataContainer.layers.containsKey(layerId)) {
                return dataContainer.layers.get(layerId).getXPlane(x);
            }
            return null;
        }
    }

    int lastVoxelXYBufferZValue;
    boolean layerVoxelXYBufferValid = false;
    Voxel[] layerVoxelXYBuffer = new Voxel[0];
    @Override
    public final Voxel[] getVoxelsXY(int z) {
        synchronized (VitcoSettings.SYNC) {
            if (!layerVoxelXYBufferValid || z != lastVoxelXYBufferZValue) {

                VoxelLayer result = new VoxelLayer(-1, "tmp");
                for (Integer layerId : dataContainer.layerOrder) {
                    if (dataContainer.layers.get(layerId).isVisible()) {
                        Voxel[] voxels = dataContainer.layers.get(layerId).getZPlane(z);
                        for (Voxel voxel : voxels) {
                            if (result.voxelPositionFree(voxel)) {
                                result.addVoxel(voxel);
                            }
                        }
                    }
                }
                layerVoxelXYBuffer = result.getVoxels();
                layerVoxelXYBufferValid = true;
                lastVoxelXYBufferZValue = z;
            }
            return layerVoxelXYBuffer.clone();
        }
    }

    int lastVoxelXZBufferYValue;
    boolean layerVoxelXZBufferValid = false;
    Voxel[] layerVoxelXZBuffer = new Voxel[0];
    @Override
    public final Voxel[] getVoxelsXZ(int y) {
        synchronized (VitcoSettings.SYNC) {
            if (!layerVoxelXZBufferValid || y != lastVoxelXZBufferYValue) {

                VoxelLayer result = new VoxelLayer(-1, "tmp");
                for (Integer layerId : dataContainer.layerOrder) {
                    if (dataContainer.layers.get(layerId).isVisible()) {
                        Voxel[] voxels = dataContainer.layers.get(layerId).getYPlane(y);
                        for (Voxel voxel : voxels) {
                            if (result.voxelPositionFree(voxel)) {
                                result.addVoxel(voxel);
                            }
                        }
                    }
                }
                layerVoxelXZBuffer = result.getVoxels();
                layerVoxelXZBufferValid = true;
                lastVoxelXZBufferYValue = y;
            }
            return layerVoxelXZBuffer.clone();
        }
    }

    int lastVoxelYZBufferXValue;
    boolean layerVoxelYZBufferValid = false;
    Voxel[] layerVoxelYZBuffer = new Voxel[0];
    @Override
    public final Voxel[] getVoxelsYZ(int x) {
        synchronized (VitcoSettings.SYNC) {
            if (!layerVoxelYZBufferValid || x != lastVoxelYZBufferXValue) {

                VoxelLayer result = new VoxelLayer(-1, "tmp");
                for (Integer layerId : dataContainer.layerOrder) {
                    if (dataContainer.layers.get(layerId).isVisible()) {
                        Voxel[] voxels = dataContainer.layers.get(layerId).getXPlane(x);
                        for (Voxel voxel : voxels) {
                            if (result.voxelPositionFree(voxel)) {
                                result.addVoxel(voxel);
                            }
                        }
                    }
                }
                layerVoxelYZBuffer = result.getVoxels();
                layerVoxelYZBufferValid = true;
                lastVoxelYZBufferXValue = x;
            }
            return layerVoxelYZBuffer.clone();
        }
    }

    @Override
    public final int getVoxelCount(int layerId) {
        synchronized (VitcoSettings.SYNC) {
            int result = 0;
            if (dataContainer.layers.containsKey(layerId)) {
                result = dataContainer.layers.get(layerId).getSize();
            }
            return result;
        }
    }

    // ==================================

    @Override
    public final void undoV() {
        synchronized (VitcoSettings.SYNC) {
            historyManagerV.unapply();
        }
    }

    @Override
    public final void redoV() {
        synchronized (VitcoSettings.SYNC) {
            historyManagerV.apply();
        }
    }

    @Override
    public final boolean canUndoV() {
        synchronized (VitcoSettings.SYNC) {
            return historyManagerV.canUndo();
        }
    }

    @Override
    public final boolean canRedoV() {
        synchronized (VitcoSettings.SYNC) {
            return historyManagerV.canRedo();
        }
    }

    @Override
    public final int createLayer(String layerName) {
        synchronized (VitcoSettings.SYNC) {
            int layerId = getFreeLayerId();
            historyManagerV.applyIntent(new CreateLayerIntent(layerId, layerName, false));
            notifier.onLayerStateChanged();
            return layerId;
        }
    }

    @Override
    public  final boolean deleteLayer(int layerId) {
        synchronized (VitcoSettings.SYNC) {
            boolean result = false;
            if (dataContainer.layers.containsKey(layerId)) {
                historyManagerV.applyIntent(new DeleteLayerIntent(layerId, false));
                notifier.onLayerStateChanged();
                result = true;
            }
            return result;
        }
    }

    @Override
    public  final boolean renameLayer(int layerId, String newName) {
        synchronized (VitcoSettings.SYNC) {
            boolean result = false;
            if (dataContainer.layers.containsKey(layerId) && !newName.equals(dataContainer.layers.get(layerId).getName())) {
                historyManagerV.applyIntent(new RenameLayerIntent(layerId, newName, false));
                notifier.onLayerStateChanged();
                result = true;
            }
            return result;
        }
    }

    @Override
    public final String getLayerName(int layerId) {
        synchronized (VitcoSettings.SYNC) {
            return dataContainer.layers.containsKey(layerId) ? dataContainer.layers.get(layerId).getName() : null;
        }
    }

    private boolean layerNameBufferValid = false;
    private String[] layerNameBuffer = new String[]{};
    @Override
    public final String[] getLayerNames() {
        synchronized (VitcoSettings.SYNC) {
            if (!layerNameBufferValid) {
                if (layerNameBuffer.length != dataContainer.layers.size()) {
                    layerNameBuffer = new String[dataContainer.layers.size()];
                }
                int i = 0;
                for (Integer layerId : dataContainer.layerOrder) {
                    layerNameBuffer[i++] = getLayerName(layerId);
                }
                layerNameBufferValid = true;
            }
            return layerNameBuffer.clone();
        }
    }

    @Override
    public final boolean selectLayer(int layerId) {
        synchronized (VitcoSettings.SYNC) {
            boolean result = false;
            if ((dataContainer.layers.containsKey(layerId) || layerId == -1) && dataContainer.selectedLayer != layerId) {
                historyManagerV.applyIntent(new SelectLayerIntent(layerId, false));
                notifier.onLayerStateChanged();
                result = true;
            }
            return result;
        }
    }

    @Override
    public final boolean selectLayerSoft(int layerId) {
        synchronized (VitcoSettings.SYNC) {
            boolean result = false;
            if ((dataContainer.layers.containsKey(layerId) || layerId == -1) && dataContainer.selectedLayer != layerId) {
                dataContainer.selectedLayer = layerId;
                invalidateV(new int[0][]);
                notifier.onLayerStateChanged();
                result = true;
            }
            return result;
        }
    }

    @Override
    public final int getSelectedLayer() {
        synchronized (VitcoSettings.SYNC) {
            // make sure the selected layer is always valid
            return dataContainer.layers.containsKey(dataContainer.selectedLayer) ? dataContainer.selectedLayer : -1;
        }
    }

    private boolean layerBufferValid = false;
    private Integer[] layerBuffer = new Integer[]{};
    @Override
    public final Integer[] getLayers() {
        synchronized (VitcoSettings.SYNC) {
            if (!layerBufferValid) {
                if (layerBuffer.length != dataContainer.layers.size()) {
                    layerBuffer = new Integer[dataContainer.layers.size()];
                }
                dataContainer.layerOrder.toArray(layerBuffer);
                layerBufferValid = true;
            }
            return layerBuffer.clone();
        }
    }

    @Override
    public final boolean setVisible(int layerId, boolean b) {
        synchronized (VitcoSettings.SYNC) {
            boolean result = false;
            if (dataContainer.layers.containsKey(layerId) && dataContainer.layers.get(layerId).isVisible() != b) {
                historyManagerV.applyIntent(new LayerVisibilityIntent(layerId, b, false));
                notifier.onLayerStateChanged();
                result = true;
            }
            return result;
        }
    }

    @Override
    public final boolean getLayerVisible(int layerId) {
        synchronized (VitcoSettings.SYNC) {
            boolean result = false;
            if (dataContainer.layers.containsKey(layerId)) {
                result = dataContainer.layers.get(layerId).isVisible();
            }
            return result;
        }
    }

    @Override
    public final boolean moveLayerUp(int layerId) {
        synchronized (VitcoSettings.SYNC) {
            boolean result = false;
            if (canMoveLayerUp(layerId)) {
                historyManagerV.applyIntent(new MoveLayerIntent(layerId, true, false));
                notifier.onLayerStateChanged();
                result = true;
            }
            return result;
        }
    }

    @Override
    public final boolean moveLayerDown(int layerId) {
        synchronized (VitcoSettings.SYNC) {
            boolean result = false;
            if (canMoveLayerDown(layerId)) {
                historyManagerV.applyIntent(new MoveLayerIntent(layerId, false, false));
                notifier.onLayerStateChanged();
                result = true;
            }
            return result;
        }
    }

    @Override
    public final boolean canMoveLayerUp(int layerId) {
        synchronized (VitcoSettings.SYNC) {
            return dataContainer.layers.containsKey(layerId) && dataContainer.layerOrder.lastIndexOf(layerId) > 0;
        }
    }

    @Override
    public final boolean canMoveLayerDown(int layerId) {
        synchronized (VitcoSettings.SYNC) {
            return dataContainer.layers.containsKey(layerId) && dataContainer.layerOrder.lastIndexOf(layerId) < dataContainer.layerOrder.size() - 1;
        }
    }

    @Override
    public final boolean mergeVisibleLayers() {
        synchronized (VitcoSettings.SYNC) {
            if (canMergeVisibleLayers()) {
                historyManagerV.applyIntent(new MergeLayersIntent(false));
                notifier.onLayerStateChanged();
                return true;
            }
            return false;
        }
    }

    @Override
    public final boolean canMergeVisibleLayers() {
        synchronized (VitcoSettings.SYNC) {
            // if there are more than one visible layer
            int visibleLayers = 0;
            for (int layerId : dataContainer.layerOrder) {
                if (dataContainer.layers.get(layerId).isVisible()) {
                    if (visibleLayers > 0) {
                        return true;
                    }
                    visibleLayers++;
                }
            }
            return false;
        }
    }

    // texture actions

    @Override
    public final void addTexture(BufferedImage image) {
        synchronized (VitcoSettings.SYNC) {
            // make sure that the graphic is a mutiple of 32
            int width = ((int)Math.ceil(image.getWidth() / 32f)) * 32;
            int height = ((int)Math.ceil(image.getHeight() / 32f)) * 32;

            BufferedImage texture;
            if (width != image.getWidth() || height != image.getHeight()) {
                texture = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
                texture.getGraphics().drawImage(image, 0, 0, null);
            } else {
                texture = GraphicTools.deepCopy(image);
            }
            historyManagerV.applyIntent(new AddTextureGridIntent(texture, false));
        }
    }

    @Override
    public final boolean removeTexture(int textureId) {
        synchronized (VitcoSettings.SYNC) {
            boolean result = false;
            // check that this texture is not used (return false if used)
            for (Voxel voxel : dataContainer.voxels.values()) {
                if (ArrayUtil.contains(voxel.getTexture(), textureId)) {
                    return false;
                }
            }
            if (dataContainer.textures.containsKey(textureId)) {
                historyManagerV.applyIntent(new RemoveTextureIntent(textureId, false));
                result = true;
            }
            return result;
        }
    }

    @Override
    public final boolean removeAllTexture() {
        synchronized (VitcoSettings.SYNC) {
            boolean result = false;
            if (dataContainer.textures.size() > 0) {

                // check which textures are not in use
                ArrayList<Integer> unusedTextures = new ArrayList<Integer>(dataContainer.textures.keySet());
                for (Voxel voxel : dataContainer.voxels.values()) {
                    int[] textures = voxel.getTexture();
                    if (textures != null) {
                        for (Integer textureId : textures) {
                            unusedTextures.remove(textureId);
                        }
                    }
                }
                if (unusedTextures.size() > 0) {
                    historyManagerV.applyIntent(new RemoveAllTextureIntent(unusedTextures, false));
                    result = true;
                }
            }
            return result;
        }
    }

    @Override
    public final boolean replaceTexture(int textureId, ImageIcon texture) {
        synchronized (VitcoSettings.SYNC) {
            boolean result = false;
            if (dataContainer.textures.containsKey(textureId) &&
                    texture.getIconWidth() == 32 && texture.getIconHeight() == 32) {
                historyManagerV.applyIntent(new ReplaceTextureIntent(textureId, texture, false));
                result = true;
            }
            return result;
        }
    }

    @Override
    public final Integer[] getTextureList() {
        synchronized (VitcoSettings.SYNC) {
            Integer[] result = new Integer[dataContainer.textures.size()];
            dataContainer.textures.keySet().toArray(result);
            return result;
        }
    }

    @Override
    public final TIntHashSet getVoxelColorList() {
        synchronized (VitcoSettings.SYNC) {
            TIntHashSet result = new TIntHashSet();
            for (Integer layerId : getLayers()) {
                result.addAll(dataContainer.layers.get(layerId).getVoxelColors());
            }
            return result;
        }
    }

    @Override
    public final ImageIcon getTexture(Integer textureId) {
        synchronized (VitcoSettings.SYNC) {
            Image internalImg = dataContainer.textures.get(textureId).getImage();
            BufferedImage result = new BufferedImage(
                    internalImg.getWidth(null), internalImg.getHeight(null),
                    BufferedImage.TYPE_INT_ARGB);
            result.getGraphics().drawImage(internalImg, 0, 0, null);
            return new ImageIcon(result);
        }
    }

    @Override
    public final String getTextureHash(Integer textureId) {
        synchronized (VitcoSettings.SYNC) {
            if (dataContainer.textures.containsKey(textureId)) {
                if (dataContainer.textures.get(textureId).getDescription() == null) {
                    ImageIcon img = dataContainer.textures.get(textureId);
                    BufferedImage bi = new BufferedImage(
                            img.getIconWidth(),img.getIconHeight(),BufferedImage.TYPE_INT_RGB);
                    bi.createGraphics().drawImage(img.getImage(),0,0,null);
                    dataContainer.textures.get(textureId).setDescription(GraphicTools.getHash(bi));
                }
                return dataContainer.textures.get(textureId).getDescription();
            } else {
                return "";
            }
        }
    }

    @Override
    public final void selectTexture(int textureId) {
        synchronized (VitcoSettings.SYNC) {
            if (textureId != -1 && dataContainer.textures.containsKey(textureId)) {
                if (textureId != dataContainer.selectedTexture) {
                    historyManagerV.applyIntent(new SelectTextureIntent(textureId, false));
                }
            } else {
                if (dataContainer.selectedTexture != -1) {
                    historyManagerV.applyIntent(new SelectTextureIntent(-1, false));
                }
            }
        }
    }

    @Override
    public final void selectTextureSoft(int textureId) {
        synchronized (VitcoSettings.SYNC) {
            if (dataContainer.selectedTexture != textureId &&
                    (textureId == -1 || dataContainer.textures.containsKey(textureId))) {
                dataContainer.selectedTexture = textureId;
                notifier.onTextureDataChanged();
            }
        }
    }

    @Override
    public final int getSelectedTexture() {
        synchronized (VitcoSettings.SYNC) {
            if (!dataContainer.textures.containsKey(dataContainer.selectedTexture)) {
                selectTextureSoft(-1);
            }
            return dataContainer.selectedTexture;
        }
    }

    @Override
    public final boolean setTexture(int voxelId, int voxelSide, int textureId) {
        synchronized (VitcoSettings.SYNC) {
            boolean result = false;
            if (dataContainer.voxels.containsKey(voxelId) &&
                    (dataContainer.voxels.get(voxelId).getTexture() == null ||
                    dataContainer.voxels.get(voxelId).getTexture()[voxelSide] != textureId)) {
                historyManagerV.applyIntent(new TextureVoxelIntent(voxelId, voxelSide, textureId, false));
                result = true;
            }
            return result;
        }
    }

    @Override
    public final boolean massSetTexture(Integer[] voxelIds, int textureId) {
        synchronized (VitcoSettings.SYNC) {
            ArrayList<Integer> validVoxel = new ArrayList<Integer>();
            for (int voxelId : voxelIds) {
                if (dataContainer.voxels.containsKey(voxelId)) {
                    validVoxel.add(voxelId);
                }
            }
            if (validVoxel.size() > 0) {
                Integer[] valid = new Integer[validVoxel.size()];
                validVoxel.toArray(valid);
                historyManagerV.applyIntent(new MassTextureVoxelIntent(valid, textureId, false));
                return true;
            } else {
                return false;
            }
        }
    }

    // get texture id of a voxel
    @Override
    public final int[] getVoxelTextureIds(int voxelId) {
        synchronized (VitcoSettings.SYNC) {
            if (dataContainer.voxels.containsKey(voxelId)) {
                return dataContainer.voxels.get(voxelId).getTexture();
            }
            return null; // error
        }
    }

    // flip the texture of a voxel
    @Override
    public final boolean flipVoxelTexture(int voxelId, int voxelSide) {
        synchronized (VitcoSettings.SYNC) {
            boolean result = false;
            if (dataContainer.voxels.containsKey(voxelId) &&
                    dataContainer.voxels.get(voxelId).getTexture() != null) {
                historyManagerV.applyIntent(new FlipVoxelTextureIntent(voxelId, voxelSide, false));
                result = true;
            }
            return result;
        }
    }

    // rotate the texture of a voxel
    @Override
    public final boolean rotateVoxelTexture(int voxelId, int voxelSide) {
        synchronized (VitcoSettings.SYNC) {
            boolean result = false;
            if (dataContainer.voxels.containsKey(voxelId) &&
                    dataContainer.voxels.get(voxelId).getTexture() != null) {
                historyManagerV.applyIntent(new RotateVoxelTextureIntent(voxelId, voxelSide, false));
                result = true;
            }
            return result;
        }
    }
}
