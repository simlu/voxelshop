package com.vitco.importer;

import com.vitco.util.file.FileIn;
import com.vitco.util.file.RandomAccessFileIn;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;

/**
 * Importer contract for (all?) voxel formats
 */
public abstract class AbstractImporter {

    // true if loading was successful
    private final boolean hasLoaded;
    public final boolean hasLoaded() {
        return hasLoaded;
    }

    // -------------

    // contains a layer object
    public final static class Layer {
        public final String name;
        // contains the voxel that were read
        private ArrayList<int[]> voxel = new ArrayList<int[]>();
        // constructor
        private Layer(String name) {
            this.name = name;
        }

        // for sanity testing
        private final HashSet<String> known = new HashSet<String>();

        private void addVoxel(int x, int y, int z, int color) {
            voxel.add(new int[]{x,y,z,color});
            // for sanity testing
            assert known.add(x+"_"+y+"_"+z);
        }

        // ----

        public final boolean isEmpty() {
            return voxel.isEmpty();
        }

        int i = 0;
        public final int[] next() {
            return voxel.get(i++);
        }

        public final boolean hasNext() {
            return i < voxel.size();
        }

        public final void reset() {
            i = 0;
        }

        private boolean visible = true;
        public void setVisible(boolean flag) {
            this.visible = flag;
        }
        public boolean isVisible() {
            return visible;
        }
    }

    // the currently active layer
    private Layer activeLayer;

    // list of layers
    public final ArrayList<Layer> getVoxel() {
        return layerList;
    }

    // add voxel to known-voxel list
    protected final void addVoxel(int x, int y, int z, int color) {
        voxelCount++;
        lx = Math.min(lx, x);
        ly = Math.min(ly, y);
        lz = Math.min(lz, z);
        hx = Math.max(hx, x);
        hy = Math.max(hy, y);
        hz = Math.max(hz, z);
        cx += x;
        cy += y;
        cz += z;
        activeLayer.addVoxel(x, y, z, color);
    }

    // contains the layer list
    private final ArrayList<Layer> layerList = new ArrayList<Layer>();
    // add a new layer that the voxels are now added to
    protected final void addLayer(String name) {
        if (activeLayer.isEmpty()) {
            layerList.remove(activeLayer);
        }
        activeLayer = new Layer(name);
        layerList.add(activeLayer);
    }
    // prepend a new layer that the voxels are now added to
    protected final void prependLayer(String name) {
        if (activeLayer.isEmpty()) {
            layerList.remove(activeLayer);
        }
        activeLayer = new Layer(name);
        layerList.add(0, activeLayer);
    }

    // set visibility of current layer
    protected final void setLayerVisibility(boolean flag) {
        activeLayer.setVisible(flag);
    }


    // --------------

    // lowest voxel value in that dimension (all layers)
    private int lx = Integer.MAX_VALUE;
    private int ly = Integer.MAX_VALUE;
    private int lz = Integer.MAX_VALUE;
    public final int[] getLowest() {
        return new int[] {lx, ly, lz};
    }

    // highest voxel value in that dimension (all layers)
    private int hx = Integer.MIN_VALUE;
    private int hy = Integer.MIN_VALUE;
    private int hz = Integer.MIN_VALUE;
    public final int[] getHighest() {
        return new int[] {hx, hy, hz};
    }

    // the amount of voxel that were added (all layers)
    private int voxelCount = 0;
    public final int getVoxelCount() {
        return voxelCount;
    }

    // weighted center (all layers)
    private long cx = 0;
    private long cy = 0;
    private long cz = 0;
    public final int[] getWeightedCenter() {
        return new int[] {(int) (cx/voxelCount), (int) (cy/voxelCount), (int) (cz/voxelCount)};
    }

    // the the center (determined by the boundaries)
    public final int[] getCenter() {
        return new int[] {Math.round(lx + (hx-lx)/2.0f), Math.round(ly + (hy-ly)/2.0f), Math.round(lz + (hz-lz)/2.0f)};
    }

    // --------------

    // constructor
    public AbstractImporter(File file, String layerName) throws IOException {
        activeLayer = new Layer(layerName);
        layerList.add(activeLayer);
        FileIn fileIn = new FileIn(file);
        RandomAccessFileIn raf = new RandomAccessFileIn(file, "r");
        try {
            hasLoaded = read(fileIn, raf);
        } finally {
            raf.close();
            fileIn.finish();
        }
    }

    // read file - returns true if file has loaded correctly
    protected abstract boolean read(FileIn fileIn, RandomAccessFileIn raf) throws IOException;
}
