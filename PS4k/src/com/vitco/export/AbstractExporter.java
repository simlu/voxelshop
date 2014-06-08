package com.vitco.export;

import com.vitco.core.data.Data;
import com.vitco.core.data.container.Voxel;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * Abstract class for voxel format exporter.
 */
public abstract class AbstractExporter {

    // the file that we export to
    protected final File exportTo;

    // the data that we use
    protected final Data data;

    // true if write was successful
    private final boolean wasWritten;

    // check if write was successful
    public final boolean wasWritten() {
        return wasWritten;
    }

    // random access file for writing
    protected final RandomAccessFile raf;

    // store voxel information
    private final int[] min = new int[]{Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE};
    private final int[] max = new int[]{Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE};
    private final int[] size = new int[] {0,0,0};

    // constructor
    public AbstractExporter(File exportTo, Data data) throws IOException {
        this.exportTo = exportTo;
        this.data = data;

        // retrieve information
        for (Voxel voxel : data.getVisibleLayerVoxel()) {
            min[0] = Math.min(voxel.x, min[0]);
            min[1] = Math.min(voxel.y, min[1]);
            min[2] = Math.min(voxel.z, min[2]);
            max[0] = Math.max(voxel.x, max[0]);
            max[1] = Math.max(voxel.y, max[1]);
            max[2] = Math.max(voxel.z, max[2]);
        }
        size[0] = max[0] - min[0];
        size[1] = max[1] - min[1];
        size[2] = max[2] - min[2];

        // write the file content
        raf = new RandomAccessFile(exportTo, "rw");
        raf.setLength(0);
        try {
            wasWritten = writeFile();
        } finally {
            raf.close();
        }
    }

    // write the file - to be implemented
    protected abstract boolean writeFile() throws IOException;

    // ===============

    // helper - get size of voxel batch
    protected final int[] getSize() {
        return size.clone();
    }

    // helper - get minimum voxel position
    protected final int[] getMin() {
        return min.clone();
    }

    // helper - get maximum voxel position
    protected final int[] getMax() {
        return max.clone();
    }



}
