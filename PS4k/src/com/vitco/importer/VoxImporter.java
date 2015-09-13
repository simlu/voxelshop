package com.vitco.importer;

import com.vitco.util.file.FileIn;
import com.vitco.util.file.FileTools;
import com.vitco.util.file.RandomAccessFileIn;
import com.vitco.util.misc.NumberTools;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;

public class VoxImporter extends AbstractImporter {

    // constructor
    public VoxImporter(File file, String name) throws IOException {
        super(file, name);
    }

    private static final int[] voxColors = new int[] {
            -1, -52, -103, -154, -205, -256, -13057, -13108, -13159, -13210, -13261, -13312, -26113, -26164, -26215, -26266,
            -26317, -26368, -39169, -39220, -39271, -39322, -39373, -39424, -52225, -52276, -52327, -52378, -52429, -52480, -65281,
            -65332, -65383, -65434, -65485, -65536, -3342337, -3342388, -3342439, -3342490, -3342541, -3342592, -3355393, -3355444,
            -3355495, -3355546, -3355597, -3355648, -3368449, -3368500, -3368551, -3368602, -3368653, -3368704, -3381505, -3381556,
            -3381607, -3381658, -3381709, -3381760, -3394561, -3394612, -3394663, -3394714, -3394765, -3394816, -3407617, -3407668,
            -3407719, -3407770, -3407821, -3407872, -6684673, -6684724, -6684775, -6684826, -6684877, -6684928, -6697729, -6697780,
            -6697831, -6697882, -6697933, -6697984, -6710785, -6710836, -6710887, -6710938, -6710989, -6711040, -6723841, -6723892,
            -6723943, -6723994, -6724045, -6724096, -6736897, -6736948, -6736999, -6737050, -6737101, -6737152, -6749953, -6750004,
            -6750055, -6750106, -6750157, -6750208, -10027009, -10027060, -10027111, -10027162, -10027213, -10027264, -10040065,
            -10040116, -10040167, -10040218, -10040269, -10040320, -10053121, -10053172, -10053223, -10053274, -10053325, -10053376,
            -10066177, -10066228, -10066279, -10066330, -10066381, -10066432, -10079233, -10079284, -10079335, -10079386, -10079437,
            -10079488, -10092289, -10092340, -10092391, -10092442, -10092493, -10092544, -13369345, -13369396, -13369447, -13369498,
            -13369549, -13369600, -13382401, -13382452, -13382503, -13382554, -13382605, -13382656, -13395457, -13395508, -13395559,
            -13395610, -13395661, -13395712, -13408513, -13408564, -13408615, -13408666, -13408717, -13408768, -13421569, -13421620,
            -13421671, -13421722, -13421773, -13421824, -13434625, -13434676, -13434727, -13434778, -13434829, -13434880, -16711681,
            -16711732, -16711783, -16711834, -16711885, -16711936, -16724737, -16724788, -16724839, -16724890, -16724941, -16724992,
            -16737793, -16737844, -16737895, -16737946, -16737997, -16738048, -16750849, -16750900, -16750951, -16751002, -16751053,
            -16751104, -16763905, -16763956, -16764007, -16764058, -16764109, -16764160, -16776961, -16777012, -16777063, -16777114,
            -16777165, -1179648, -2293760, -4521984, -5636096, -7864320, -8978432, -11206656, -12320768, -14548992, -15663104, -16716288,
            -16720640, -16729344, -16733696, -16742400, -16746752, -16755456, -16759808, -16768512, -16772864, -16776978, -16776995,
            -16777029, -16777046, -16777080, -16777097, -16777131, -16777148, -16777182, -16777199, -1118482, -2236963, -4473925,
            -5592406, -7829368, -8947849, -11184811, -12303292, -14540254, -15658735, -16777216
    };

    // read file - returns true if file has loaded correctly
    @Override
    protected boolean read(FileIn fileIn, RandomAccessFileIn raf) throws IOException {

        // check magic number
        byte[] check = new byte[4];
        raf.read(check);
        String checkSum = new String(check, "ASCII");
        if (!checkSum.equals("VOX ")) {

            // this might be the voxel format from the voxlap engine (slab6)
            raf.seek(0);
            int sx = Integer.reverseBytes(raf.readInt());
            int sy = Integer.reverseBytes(raf.readInt());
            int sz = Integer.reverseBytes(raf.readInt());

            if (sx > 512 || sy > 512 || sz > 512 || sx <= 0 || sy <= 0 || sz <= 0) {

                // =====================
                // vox-game.com format (voxel and config file)
                // =====================

                String[] line = fileIn.readLine().split(" ");
                if (line.length == 3) {
                    sx = NumberTools.parseInt(line[0], 0);
                    sy = NumberTools.parseInt(line[1], 0);
                    sz = NumberTools.parseInt(line[2], 0);
                } else if (line.length == 2) {
                    // check if this is a config file
                    if (line[0].equals("skeletonScale:")) {
                        float scale = NumberTools.parseFloat(line[1], 1);
                        // sanity check
                        if (!fileIn.readLine().equals("")) {
                            return false;
                        }
                        // read
                        String scaleLine;
                        float offx = 0, offy = 0, offz = 0;
                        while ((scaleLine = fileIn.readLine()) != null) {
                            if (scaleLine.length() > 8 && scaleLine.contains("Scale: ")) {
                                // extract name
                                String name = scaleLine.substring(0, scaleLine.indexOf(" ") - 6);
                                // extract offsets
                                String[] strOffx = fileIn.readLine().split(" ");
                                if (strOffx.length == 2) {
                                    offx = NumberTools.parseFloat(strOffx[1], 0)/(10 * scale);
                                }
                                String[] strOffy = fileIn.readLine().split(" ");
                                if (strOffy.length == 2) {
                                    offy = NumberTools.parseFloat(strOffy[1], 0)/(0.13f * scale);
                                }
                                String[] strOffz = fileIn.readLine().split(" ");
                                if (strOffz.length == 2) {
                                    offz = NumberTools.parseFloat(strOffz[1], 0)/(0.05f * scale);
                                }
                                //System.out.println(offx + " " + offy + " " + offz);
                                // read the file and import voxel
                                if (name.equals("feet")) {
                                    name = "foot";
                                } else if (name.equals("hands")) {
                                    name = "hand";
                                }
                                File partFile = new File(FileTools.ensureTrailingSeparator(fileIn.getInternalFile().getParent()) + name + ".vox");
                                if (partFile.exists()) {
                                    VoxImporter voxImporter = new VoxImporter(partFile, name);
                                    if (voxImporter.hasLoaded()) {
                                        for (Layer layer : voxImporter.getVoxel()) {
                                            addLayer(layer.name);
                                            for (int[] vox; layer.hasNext();) {
                                                vox = layer.next();
                                                // no need to rotate this (already rotated)
                                                addVoxel(vox[0] - Math.round(offx), vox[1] - Math.round(offy), vox[2] + Math.round(offz), vox[3]);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        return true;
                    }
                }
                // sanity check
                if (sx > 512 || sy > 512 || sz > 512 || sx <= 0 || sy <= 0 || sz <= 0) {
                    return false;
                }
                // check that this line is empty
                if (!fileIn.readLine().equals("")) {
                    return false;
                }
                // read to next space
                for (int y = 0; y < sy; y++) {
                    for (int x = 0; x < sx; x++) {
                        for (int z = 0; z < sz; z++) {
                            int visible = NumberTools.parseInt(fileIn.readSpaceString(), 0);
                            int r = Math.round(NumberTools.parseFloat(fileIn.readSpaceString(), 0f) * 255);
                            int g = Math.round(NumberTools.parseFloat(fileIn.readSpaceString(), 0f) * 255);
                            int b = Math.round(NumberTools.parseFloat(fileIn.readSpaceString(), 0f) * 255);
                            if (visible == 1) {
                                addVoxel(-x,-y,z, new Color(r,g,b).getRGB());
                            }
                        }
                    }
                }
                return true;
            } else {
                // read over integer values
                fileIn.skipBytes(12);
            }

            // =====================
            // VOXLAP ENGINE *.vox FORMAT
            // =====================

            // Read the color palette (always at the end of the file)
            raf.seek(raf.length() - 768);
            int[] colPalette = new int[256];
            for (int i = 0; i < 256; i++) {
                int r = Math.min(255,Math.max(0,Math.round((raf.read() * 255)/63f)));
                int g = Math.min(255,Math.max(0,Math.round((raf.read() * 255)/63f)));
                int b = Math.min(255,Math.max(0,Math.round((raf.read() * 255)/63f)));
                colPalette[i] = new Color(r, g, b).getRGB();
            }

            // read the voxel
            for (int x = 0; x < sx; x++) {
                for (int y = 0; y < sy; y++) {
                    for (int z = 0; z < sz; z++) {
                        int paletteEntry = fileIn.readByteUnsigned();
                        if (paletteEntry != 255) {
                            //noinspection SuspiciousNameCombination
                            addVoxel(x, z, -y, colPalette[paletteEntry]);
                        }
                    }
                }
            }

            return true;
        } else {
            // skip over chechsum
            fileIn.skipBytes(4);
        }

        // =====================
        // MagicaVoxel *.vox FORMAT
        // =====================

        // check version number
        int version = fileIn.readIntRev();
        if (version < 150) {
            return false;
        }

        // check main chunk identifier
        if (!fileIn.readASCIIString(4).equals("MAIN")) {
            return false;
        }

        int mainContentSize = fileIn.readIntRev();
        int totalChildrenSize = fileIn.readIntRev();

        // skip over the main content
        if (!fileIn.skipBytes(mainContentSize)) {
            return false;
        }

        ArrayList<Integer> voxels = new ArrayList<Integer>();
        int[] palette = voxColors;

        for (int i = 0; i < totalChildrenSize;) {
            // each chunk has an ID, size and child chunks
            String chunkName = fileIn.readASCIIString(4);

            int chunkSize = fileIn.readIntRev();
            fileIn.readIntRev();//int childChunks = fileIn.readIntRev();

            if (chunkName.equals("SIZE")) {
                // read x,y,z
                fileIn.readIntRev();
                fileIn.readIntRev();
                fileIn.readIntRev();
            } else if (chunkName.equals("XYZI")) {
                int numVoxels = fileIn.readIntRev();
                if (numVoxels < 0) { // sanity check
                    return false;
                }
                for (int j = 0; j < numVoxels; j += 1) {
                    voxels.add(fileIn.readInt());
                }

            } else if (chunkName.equals("RGBA")) {
                // use custom color palette
                palette = new int[256];
                for (int j = 0; j < 256; j++) {
                    int r = fileIn.readByteUnsigned();
                    int g = fileIn.readByteUnsigned();
                    int b = fileIn.readByteUnsigned();
                    fileIn.readByteUnsigned();//int a = fileIn.readByteUnsigned();
                    palette[j] = new Color(r,g,b).getRGB();
                }

            } else {
                fileIn.skipBytes(chunkSize);
            }

            i += 12 + chunkSize;
        }

        ByteBuffer buf = ByteBuffer.allocate(4);
        for (Integer voxel : voxels) {
            buf.position(0);
            buf.putInt(voxel);
            addVoxel(-(buf.get(0) & 0xFF), -(buf.get(2) & 0xFF), buf.get(1) & 0xFF, palette[(buf.get(3) & 0xFF) - 1]);
        }

        return true;
    }
}