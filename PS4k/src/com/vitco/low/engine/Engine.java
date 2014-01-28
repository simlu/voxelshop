package com.vitco.low.engine;

import com.vitco.low.CubeIndexer;
import com.vitco.util.misc.IntegerTools;
import gnu.trove.map.hash.TIntObjectHashMap;

import java.util.HashMap;

/**
 * Proves fast read/write access to voxel in the world.
 */
public class Engine {

    // ------------------------------
    // static information for all engines (!!!)

    // holds the different known voxel types
    private static final HashMap<VoxelType, VoxelType> voxelTypes = new HashMap<VoxelType, VoxelType>();

    // generate unique ids for the voxel
    private static int uidCount = 1;
    private static int generateUID() {
        return uidCount++;
    }

    // -----------------------------

    // holds the different known chunks
    private final TIntObjectHashMap<Chunk> chunks = new TIntObjectHashMap<Chunk>();

    // obtain the appropriate chunk (create a new one if it doesn't exist)
    private Chunk getChunk(int[] xyz) {
        int chunkId = CubeIndexer.getId(
                IntegerTools.ifloordiv2(xyz[0], Chunk.CHUNK_SIZE),
                IntegerTools.ifloordiv2(xyz[1], Chunk.CHUNK_SIZE),
                IntegerTools.ifloordiv2(xyz[2], Chunk.CHUNK_SIZE));
        Chunk result = chunks.get(chunkId);
        if (result == null) {
            result = new Chunk();
            chunks.put(chunkId, result);
        }
        return result;
    }

    // delete a voxel
    public boolean delete(int[][] xyzs) {
        return false;
    }

    // set a voxel
    public void set(int[][] xyzs, VoxelType newType) {
        // get the voxel type if it already exists, or create a new one
        VoxelType type = voxelTypes.get(newType);
        if (type == null) {
            voxelTypes.put(newType, newType);
            newType.uId = generateUID();
            type = newType;
        }
        for (int[] xyz : xyzs) {
            // increase the used count
            type.usedCount++;
            // get the chunk this voxel lives in
            Chunk chunk = getChunk(xyz);
            // check if the voxel already exists (and decrease used count of the type if so)
            // ...
            // update the voxel in the chunk
            // ...
        }
    }

    // get a voxel
    public VoxelType get(int[][] xyz) {
        return null;
    }

}
