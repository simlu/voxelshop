package com.vitco.importer;

import com.vitco.util.file.FileIn;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;

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

    // for sanity testing
//    HashMap<String, Boolean> known = new HashMap<String, Boolean>();
//        System.out.println(x+"_"+y+"_"+z);
//        if (known.put(x+"_"+y+"_"+z, true) != null) {
//            System.out.println("Error");
//        }

    // contains the voxel that were read
    private final ArrayList<int[]> voxel = new ArrayList<int[]>();
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
        voxel.add(new int[]{x,y,z,color});
    }
    // return the voxel that were read
    public final ArrayList<int[]> getVoxel() {
        return voxel;
    }

    // --------------

    // lowest voxel value in that dimension
    private int lx = Integer.MAX_VALUE;
    private int ly = Integer.MAX_VALUE;
    private int lz = Integer.MAX_VALUE;
    public final int[] getLowest() {
        return new int[] {lx, ly, lz};
    }

    // highest voxel value in that dimension
    private int hx = Integer.MIN_VALUE;
    private int hy = Integer.MIN_VALUE;
    private int hz = Integer.MIN_VALUE;
    public int[] getHighest() {
        return new int[] {hx, hy, hz};
    }

    // the amount of voxel that were added
    private int voxelCount = 0;
    public int getVoxelCount() {
        return voxelCount;
    }

    // weighted center
    private long cx = 0;
    private long cy = 0;
    private long cz = 0;
    public int[] getCenter() {
        return new int[] {(int) (cx/voxelCount), (int) (cy/voxelCount), (int) (cz/voxelCount)};
    }

    // --------------

    // constructor
    public AbstractImporter(File file) throws IOException {
        FileIn fileIn = new FileIn(file);
        RandomAccessFile raf = new RandomAccessFile(file, "r");
        hasLoaded = read(fileIn, raf);
        raf.close();
        fileIn.finish();
    }

    // read file - returns true if file has loaded correctly
    protected abstract boolean read(FileIn fileIn, RandomAccessFile raf) throws IOException;
}
