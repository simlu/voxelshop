package com.vitco.app.importer;

import com.vitco.app.importer.dataStatic.CCVxlStatic;
import com.vitco.app.util.file.FileIn;
import com.vitco.app.util.file.RandomAccessFileIn;

import java.io.File;
import java.io.IOException;

/**
 * Voxel Importer for "Command & Conquer: Red Alert 2" voxel objects
 *
 * Taken from VxlReader
 * https://github.com/OpenRA/OpenRA/blob/bleed/OpenRA.Game/FileFormats/VxlReader.cs
 */
public class CCVxlImporter extends AbstractImporter {

    // constructor
    public CCVxlImporter(File file, String name) throws IOException {
        super(file, name);
    }

    // type of game (they use different color palettes)
    public enum NormalType { TiberianSun(2), RedAlert2(4);
        public final int id;
        NormalType(int id) {
            this.id = id;
        }
    }

    // voxel layer
    private final static class VxlLimb {
        public String name;
        public float scale;
        public float[] bounds;
        public int[] size;
        public NormalType type;

        public int voxelCount;
    }

    // read the voxel information for a specific layer
    private void readVoxelData(RandomAccessFileIn s, VxlLimb l) throws IOException {
        int baseSize = l.size[0] * l.size[1];
        int[] colStart = new int[baseSize];
        for (int i = 0; i < baseSize; i++) {
            colStart[i] = s.readInt32();
        }
        s.seek(4 * baseSize, RandomAccessFileIn.CURRENT);
        int dataStart = (int) s.getFilePointer();

        // Count the voxels in this limb
        l.voxelCount = 0;
        for (int i = 0; i < baseSize; i++) {
            // Empty column
            if (colStart[i] == -1) {
                continue;
            }

            s.seek(dataStart + colStart[i], RandomAccessFileIn.BEGINNING);
            int z = 0;
            do {
                z += s.readUInt8();
                int count = s.readUInt8();
                z += count;
                l.voxelCount += count;
                s.seek(2 * count + 1, RandomAccessFileIn.CURRENT);
            } while (z < l.size[2]);
        }

        // Read the data
        for (int i = 0; i < baseSize; i++) {
            // Empty column
            if (colStart[i] == -1)
                continue;

            s.seek(dataStart + colStart[i], RandomAccessFileIn.BEGINNING);

            int x = i % l.size[0];
            int y = i / l.size[0];
            int z = 0;
            do {
                z += s.readUInt8();
                int count = s.readUInt8();
                for (int j = 0; j < count; j++) {
                    int color = s.readUInt8();
                    s.readUInt8(); //int normal = s.readUInt8();

                    // add a voxel with correct color
                    addVoxel(
                            x, -z, y,
                            l.type == NormalType.TiberianSun
                                    ? CCVxlStatic.COLORS_TIBERIAN_DAWN[color]
                                    : CCVxlStatic.COLORS_RED_ALERT[color]
                    );
                    z++;
                }
                // Skip duplicate count
                s.readUInt8();
            } while (z < l.size[2]);
        }
    }

    // read file - returns true if file has loaded correctly
    @Override
    protected boolean read(FileIn fileIn, RandomAccessFileIn s) throws IOException {

        // identifier
        if (!s.readASCII(16).startsWith("Voxel Animation")) {
            return false;
        }

        // read basic information
        s.readUInt32();
        int limbCount = s.readUInt32();
        s.readUInt32();
        int bodySize = s.readUInt32();
        s.seek(770, RandomAccessFileIn.CURRENT);

        // Read Limb (layer) headers
        VxlLimb[] limbs = new VxlLimb[limbCount];
        for (int i = 0; i < limbCount; i++) {
            limbs[i] = new VxlLimb();
            limbs[i].name = s.readASCII(16).trim();
            s.seek(12, RandomAccessFileIn.CURRENT);
        }

        // skip to the limb (layer) footers
        s.seek(802 + 28 * limbCount + bodySize, RandomAccessFileIn.BEGINNING);

        int[] limbDataOffset = new int[limbCount];
        for (int i = 0; i < limbCount; i++) {
            limbDataOffset[i] = s.readUInt32();
            s.seek(8, RandomAccessFileIn.CURRENT);
            limbs[i].scale = s.readFloat();
            s.seek(48, RandomAccessFileIn.CURRENT);

            limbs[i].bounds = new float[6];
            for (int j = 0; j < 6; j++) {
                limbs[i].bounds[j] = s.readFloat();
            }
            limbs[i].size = new int[] {s.readByte() & 0xFF, s.readByte() & 0xFF, s.readByte() & 0xFF };
            limbs[i].type = s.readByte() == 2 ? NormalType.TiberianSun : NormalType.RedAlert2;
        }

        // read the voxel data for all layers
        for (int i = 0; i < limbCount; i++) {
            // add a new layer
            addLayer(limbs[i].name);
            s.seek(802 + 28*limbCount + limbDataOffset[i], RandomAccessFileIn.BEGINNING);
            readVoxelData(s, limbs[i]);
        }

        // success (i.e. no error)
        return true;
    }
}
