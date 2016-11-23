package com.vitco.app.low;

import org.junit.Test;

/**
 * Test that the mapping is a bijection for the cube.
 */
public class CubeIndexerTest {

    private static boolean testConversion(int x, int y, int z) {
        short[] pos = CubeIndexer.getPos(CubeIndexer.getId(new short[]{(short) x, (short) y, (short) z}));
        if (!(pos[0] == x && pos[1] == y && pos[2] == z)) {
            System.out.println(x + "," + y + "," + z);
            System.out.println(
                    pos[0] + " == " + x + " && " + pos[1] + " == " + y + " && " + pos[2] + " == " + z
            );
            return false;
        }
        return true;
    }

    @Test
    public void testMapping() throws Exception {

        for (int i = 0; i< 10; i++) {
            testConversion(i,2,3);
        }

        testConversion(1,2,3);
        testConversion(-1,2,3);
        testConversion(1,-2,3);
        testConversion(1,2,-3);
        testConversion(1,-2,-3);
        testConversion(-1,2,-3);
        testConversion(-1,-2,3);
        testConversion(-1,-2,-3);

        short start = -CubeIndexer.radius;
        short stop = CubeIndexer.radius;

        for (short x = start; x < stop; x++) {
            for (short y = start; y < stop; y++) {
                for (short z = start; z < stop; z++) {
                    assert testConversion(x, y, z);
                }
            }
        }
    }

}
