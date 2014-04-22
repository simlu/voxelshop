package com.vitco.util.graphic;

import org.junit.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * Test Image comparator functionality
 */
public class ImageComparatorTest {

    // helper - get position search result for two images (with optional restriction)
    private int[] getResult(String child, String parent, int[] restriction) throws IOException {
        BufferedImage haystack = ImageIO.read(new File("C:\\Users\\flux\\Dropbox\\java\\VoxelShop\\Test Files\\SubImage Search\\" +
                parent));
        BufferedImage needle = ImageIO.read(new File("C:\\Users\\flux\\Dropbox\\java\\VoxelShop\\Test Files\\SubImage Search\\" +
                child));
        ImageComparator haystackComp = new ImageComparator(haystack);
        ImageComparator needleComp = new ImageComparator(needle);

        return haystackComp.getPosition(needleComp, restriction);
    }

    @Test
    public void testGetPosition() throws Exception {
        // test runtime

        BufferedImage haystack = ImageIO.read(new File("C:\\Users\\flux\\Dropbox\\java\\VoxelShop\\Test Files\\SubImage Search\\haystack_small.png"));
        BufferedImage needle = ImageIO.read(new File("C:\\Users\\flux\\Dropbox\\java\\VoxelShop\\Test Files\\SubImage Search\\needle.png"));

        ImageComparator parent = new ImageComparator(haystack);
        ImageComparator child = new ImageComparator(needle);

        long time = System.currentTimeMillis();

        int[] result = parent.getPosition(child, null);

        System.out.println("Time: " + (System.currentTimeMillis() - time));

        if (result != null) {
            System.out.println("R " + result[0] + " " + result[1] + " @ " + result[2]);
        }

        // ===========
        // -- test single image (should be always at "0,0")

        assert getResult("needle.png", "o1.png", null)[0] == 0;
        assert getResult("needle.png", "o1.png", null)[1] == 0;
        assert getResult("needle.png", "o1.png", null)[2] == 0;

        assert getResult("needle.png", "r1.png", null)[0] == 0;
        assert getResult("needle.png", "r1.png", null)[1] == 0;
        assert getResult("needle.png", "r1.png", null)[2] == 1;

        assert getResult("needle.png", "r2.png", null)[0] == 0;
        assert getResult("needle.png", "r2.png", null)[1] == 0;
        assert getResult("needle.png", "r2.png", null)[2] == 2;

        assert getResult("needle.png", "r3.png", null)[0] == 0;
        assert getResult("needle.png", "r3.png", null)[1] == 0;
        assert getResult("needle.png", "r3.png", null)[2] == 3;

        assert getResult("needle.png", "f1.png", null)[0] == 0;
        assert getResult("needle.png", "f1.png", null)[1] == 0;
        assert getResult("needle.png", "f1.png", null)[2] == 4;

        assert getResult("needle.png", "fr1.png", null)[0] == 0;
        assert getResult("needle.png", "fr1.png", null)[1] == 0;
        assert getResult("needle.png", "fr1.png", null)[2] == 5;

        assert getResult("needle.png", "fr2.png", null)[0] == 0;
        assert getResult("needle.png", "fr2.png", null)[1] == 0;
        assert getResult("needle.png", "fr2.png", null)[2] == 6;

        assert getResult("needle.png", "fr3.png", null)[0] == 0;
        assert getResult("needle.png", "fr3.png", null)[1] == 0;
        assert getResult("needle.png", "fr3.png", null)[2] == 7;

        // ==============
        // -- test restriction search

        assert getResult("needle.png", "haystack_small.png", new int[] {0})[0] == 16;
        assert getResult("needle.png", "haystack_small.png", new int[] {0})[1] == 17;
        assert getResult("needle.png", "haystack_small.png", new int[] {0})[2] == 0;

        assert getResult("needle.png", "haystack_small.png", new int[] {1})[0] == 64;
        assert getResult("needle.png", "haystack_small.png", new int[] {1})[1] == 15;
        assert getResult("needle.png", "haystack_small.png", new int[] {1})[2] == 1;

        assert getResult("needle.png", "haystack_small.png", new int[] {2})[0] == 99;
        assert getResult("needle.png", "haystack_small.png", new int[] {2})[1] == 17;
        assert getResult("needle.png", "haystack_small.png", new int[] {2})[2] == 2;

        assert getResult("needle.png", "haystack_small.png", new int[] {3})[0] == 136;
        assert getResult("needle.png", "haystack_small.png", new int[] {3})[1] == 15;
        assert getResult("needle.png", "haystack_small.png", new int[] {3})[2] == 3;

        assert getResult("needle.png", "haystack_small.png", new int[] {4})[0] == 14;
        assert getResult("needle.png", "haystack_small.png", new int[] {4})[1] == 78;
        assert getResult("needle.png", "haystack_small.png", new int[] {4})[2] == 4;

        assert getResult("needle.png", "haystack_small.png", new int[] {5})[0] == 62;
        assert getResult("needle.png", "haystack_small.png", new int[] {5})[1] == 74;
        assert getResult("needle.png", "haystack_small.png", new int[] {5})[2] == 5;

        assert getResult("needle.png", "haystack_small.png", new int[] {6})[0] == 97;
        assert getResult("needle.png", "haystack_small.png", new int[] {6})[1] == 76;
        assert getResult("needle.png", "haystack_small.png", new int[] {6})[2] == 6;

        assert getResult("needle.png", "haystack_small.png", new int[] {7})[0] == 136;
        assert getResult("needle.png", "haystack_small.png", new int[] {7})[1] == 73;
        assert getResult("needle.png", "haystack_small.png", new int[] {7})[2] == 7;

    }
}
