package com.vitco.util.graphic;

import com.vitco.util.misc.IntegerTools;
import gnu.trove.map.hash.TIntObjectHashMap;
import org.junit.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * Test class for TextureTools
 */
public class TextureToolsTest {
    // helper - test compression for a specific image
    private void compress(String image) throws IOException {
        BufferedImage img = ImageIO.read(new File("C:\\Users\\flux\\Dropbox\\java\\VoxelShop\\Test Files\\Texture Compression\\" + image + ".png"));
        // create hashmap
        TIntObjectHashMap<int[]> pixels = new TIntObjectHashMap<int[]>();
        for (int x = 0, width = img.getWidth(); x < width; x++) {
            for (int y = 0, height = img.getHeight(); y < height; y++) {
                int rgb = img.getRGB(x,y);
                // check that this is not a fully transparent pixel
                if (((rgb >> 24) & 0xff) != 0) {
                    pixels.put(IntegerTools.makeInt(x, y), new int[]{x, y, rgb});
                }
            }
        }
        int[] size = new int[] {img.getWidth(), img.getHeight(), 1};
        // do basic compression
        size = TextureTools.compress(size[0], size[1], 0, size[0], false, pixels);
        size = TextureTools.compress(size[0], size[1], 0, size[1], true, pixels);
        // obtain offset and compress with offsets (X)
        int[] offsetsX = TextureTools.getOffsets(size[0], size[1], false, pixels);
        if (offsetsX[0] > 0) {
            size = TextureTools.compress(size[0], size[1], offsetsX[0], size[0], false, pixels);
            if (size[2] > 1) {
                offsetsX = TextureTools.getOffsets(size[0], size[1], false, pixels);
            }
        }
        if (offsetsX[1] > 0) {
            size = TextureTools.compress(size[0], size[1], 0, offsetsX[1], false, pixels);
            if (size[2] > 1) {
                offsetsX = TextureTools.getOffsets(size[0], size[1], false, pixels);
            }
        }

        if (offsetsX[0] > 0 || offsetsX[1] > 0) {
            size = TextureTools.compress(size[0], size[1], offsetsX[0], offsetsX[1], false, pixels);
        }
        // obtain offset and compress with offsets (Y)
        int[] offsetsY = TextureTools.getOffsets(size[0], size[1], true, pixels);
        if (offsetsY[0] > 0) {
            size = TextureTools.compress(size[0], size[1], offsetsY[0], size[1], true, pixels);
            if (size[2] > 1) {
                offsetsY = TextureTools.getOffsets(size[0], size[1], true, pixels);
            }
        }

        if (offsetsY[1] > 0) {
            size = TextureTools.compress(size[0], size[1], 0, offsetsY[1], true, pixels);
            if (size[2] > 1) {
                offsetsY = TextureTools.getOffsets(size[0], size[1], true, pixels);
            }
        }

        if (offsetsY[0] > 0 || offsetsY[1] > 0) {
            size = TextureTools.compress(size[0], size[1], offsetsY[0], offsetsY[1], true, pixels);
        }
        // write final image
        BufferedImage imgResult = new BufferedImage(size[0], size[1], BufferedImage.TYPE_INT_ARGB);
        for (int[] pixel : pixels.valueCollection()) {
            imgResult.setRGB(pixel[0], pixel[1], pixel[2]);
        }
        ImageIO.write(imgResult, "png", new File("C:\\Users\\flux\\Dropbox\\java\\VoxelShop\\Test Files\\Texture Compression\\" + image + "_result.png"));
    }

    @Test
    public void testCompress() throws Exception {
        for (int i = 1; i <= 25; i++) {
            compress("sample" + i);
        }
    }

    // helper - obtain offset result for an image
    private int[] getOffsets(String image, boolean useHeight) throws IOException {
        BufferedImage img = ImageIO.read(new File("C:\\Users\\flux\\Dropbox\\java\\VoxelShop\\Test Files\\Texture Compression\\" + image + ".png"));
        // create hashmap
        TIntObjectHashMap<int[]> pixels = new TIntObjectHashMap<int[]>();
        for (int x = 0, width = img.getWidth(); x < width; x++) {
            for (int y = 0, height = img.getHeight(); y < height; y++) {
                int rgb = img.getRGB(x,y);
                // check that this is not a fully transparent pixel
                if (((rgb >> 24) & 0xff) != 0) {
                    pixels.put(IntegerTools.makeInt(x, y), new int[]{x, y, rgb});
                }
            }
        }
        return TextureTools.getOffsets(img.getWidth(),img.getHeight(), useHeight, pixels);
    }

    // batch test offsets against expected results
    @Test
    public void testGetOffsets() throws Exception {
        assert getOffsets("sample1", false)[0] == 1;
        assert getOffsets("sample1", false)[1] == 7;

        assert getOffsets("sample2", false)[0] == 1;
        assert getOffsets("sample2", false)[1] == 7;

        assert getOffsets("sample3", false)[0] == 1;
        assert getOffsets("sample3", false)[1] == 5;

        assert getOffsets("sample4", false)[0] == 2;
        assert getOffsets("sample4", false)[1] == 6;

        assert getOffsets("sample5", false)[0] == 0;
        assert getOffsets("sample5", false)[1] == 3;

        assert getOffsets("sample6", false)[0] == 0;
        assert getOffsets("sample6", false)[1] == 3;

        assert getOffsets("sample7", false)[0] == 0;
        assert getOffsets("sample7", false)[1] == 3;

        assert getOffsets("sample8", false)[0] == 0;
        assert getOffsets("sample8", false)[1] == 3;

        assert getOffsets("sample9", false)[0] == 1;
        assert getOffsets("sample9", false)[1] == 8;

        assert getOffsets("sample10", false)[0] == 0;
        assert getOffsets("sample10", false)[1] == 7;

        assert getOffsets("sample11", false)[0] == 1;
        assert getOffsets("sample11", false)[1] == 8;

        assert getOffsets("sample12", false)[0] == 0;
        assert getOffsets("sample12", false)[1] == 7;

        assert getOffsets("sample13", false)[0] == 3;
        assert getOffsets("sample13", false)[1] == 4;

        assert getOffsets("sample14", false)[0] == 1;
        assert getOffsets("sample14", false)[1] == 8;

        assert getOffsets("sample15", false)[0] == 1;
        assert getOffsets("sample15", false)[1] == 7;

        assert getOffsets("sample16", false)[0] == 1;
        assert getOffsets("sample16", false)[1] == 7;

        assert getOffsets("sample17", false)[0] == 0;
        assert getOffsets("sample17", false)[1] == 8;

        assert getOffsets("sample18", false)[0] == 4;
        assert getOffsets("sample18", false)[1] == 8;

        assert getOffsets("sample19", false)[0] == 3;
        assert getOffsets("sample19", false)[1] == 8;

        assert getOffsets("sample20", false)[0] == 0;
        assert getOffsets("sample20", false)[1] == 5;

        assert getOffsets("sample21", false)[0] == 0;
        assert getOffsets("sample21", false)[1] == 3;

        assert getOffsets("sample22", false)[0] == 0;
        assert getOffsets("sample22", false)[1] == 3;

        assert getOffsets("sample23", false)[0] == 0;
        assert getOffsets("sample23", false)[1] == 3;

        // ==================

        assert getOffsets("sample1", true)[0] == 0;
        assert getOffsets("sample1", true)[1] == 3;

        assert getOffsets("sample2", true)[0] == 0;
        assert getOffsets("sample2", true)[1] == 3;

        assert getOffsets("sample3", true)[0] == 0;
        assert getOffsets("sample3", true)[1] == 3;

        assert getOffsets("sample4", true)[0] == 0;
        assert getOffsets("sample4", true)[1] == 3;

        assert getOffsets("sample5", true)[0] == 1;
        assert getOffsets("sample5", true)[1] == 5;

        assert getOffsets("sample6", true)[0] == 2;
        assert getOffsets("sample6", true)[1] == 6;

        assert getOffsets("sample7", true)[0] == 1;
        assert getOffsets("sample7", true)[1] == 7;

        assert getOffsets("sample8", true)[0] == 1;
        assert getOffsets("sample8", true)[1] == 7;

        assert getOffsets("sample9", true)[0] == 0;
        assert getOffsets("sample9", true)[1] == 2;

        assert getOffsets("sample10", true)[0] == 1;
        assert getOffsets("sample10", true)[1] == 3;

        assert getOffsets("sample11", true)[0] == 1;
        assert getOffsets("sample11", true)[1] == 3;

        assert getOffsets("sample12", true)[0] == 0;
        assert getOffsets("sample12", true)[1] == 2;

        assert getOffsets("sample13", true)[0] == 1;
        assert getOffsets("sample13", true)[1] == 3;

        assert getOffsets("sample14", true)[0] == 1;
        assert getOffsets("sample14", true)[1] == 2;

        assert getOffsets("sample15", true)[0] == 1;
        assert getOffsets("sample15", true)[1] == 2;

        assert getOffsets("sample16", true)[0] == 1;
        assert getOffsets("sample16", true)[1] == 2;

        assert getOffsets("sample17", true)[0] == 0;
        assert getOffsets("sample17", true)[1] == 3;

        assert getOffsets("sample18", true)[0] == 0;
        assert getOffsets("sample18", true)[1] == 3;

        assert getOffsets("sample19", true)[0] == 0;
        assert getOffsets("sample19", true)[1] == 3;

        assert getOffsets("sample20", true)[0] == 0;
        assert getOffsets("sample20", true)[1] == 3;

        assert getOffsets("sample21", true)[0] == 4;
        assert getOffsets("sample21", true)[1] == 8;

        assert getOffsets("sample22", true)[0] == 3;
        assert getOffsets("sample22", true)[1] == 8;

        assert getOffsets("sample23", true)[0] == 0;
        assert getOffsets("sample23", true)[1] == 5;

        //System.out.println(getOffsets("sample25", true)[0] + " " + getOffsets("sample25", true)[1]);
    }
}
