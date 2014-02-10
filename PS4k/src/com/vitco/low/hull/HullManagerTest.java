package com.vitco.low.hull;

import org.junit.Test;

/**
 * Testing that the hull detection is working properly.
 */
public class HullManagerTest {

    // todo create "big random" test case

    // helper - get point for one direction
    private static short[] get(int val, int direction) {
        switch (direction) {
            case 0: return new short[] {(short) val,50,570};
            case 1: return new short[] {-10, (short) val,570};
            default: return new short[] {-10,50, (short) val};
        }
    }

    // helper
    private static short[] get(int x, int y, int z) {
        return new short[] {(short) x, (short) y, (short) z};
    }

    // helper
    private static String toString(short[] val) {
        return val[0] + " " + val[1] + " " + val[2];
    }

    // helper
    private static void update(HullManager<String> hullManager, short[] val) {
        hullManager.update(val, toString(val));
    }

    @Test
    public void testInsideVoxel() throws Exception {
        HullManager<String> hullManager = new HullManager<String>();

        // one block
        for (short x = 0; x < 10; x++) {
            for (short y = 0; y < 10; y++) {
                for (short z = 0; z < 10; z++) {
                    update(hullManager, get(x, y, z));
                }
            }
        }
        update(hullManager, get(10, 5, 5));

        for (int j = 0; j < 6; j++) {
            assert hullManager.getHullAdditions(j).size() == (j/2 == 0 ? 100 : 101);
            assert hullManager.getHullRemovals(j).size() == 0;
        }

        for (short x = 10; x < 20; x++) {
            for (short y = 0; y < 10; y++) {
                for (short z = 0; z < 10; z++) {
                    update(hullManager, get(x, y, z));
                }
            }
        }

        for (int j = 0; j < 6; j++) {
            int sa = hullManager.getHullAdditions(j).size();
            int sr = hullManager.getHullRemovals(j).size();
            //System.out.println(sa);
            //System.out.println(sr);
            assert sa == (j/2 == 0 ? (j == 0 ? 100 : 0) : 100);
            assert sr == (j/2 == 0 ? (j == 0 ? 100 : 0) : 1);
        }
    }

    @Test
    public void testHoleDetection() throws Exception {
        HullManager<String> hullManager = new HullManager<String>();

        // one block
        for (short x = 0; x < 10; x++) {
            for (short y = 0; y < 10; y++) {
                for (short z = 0; z < 10; z++) {
                    update(hullManager, get(x, y, z));
                }
            }
        }
        for (int j = 0; j < 6; j++) {
            assert hullManager.getHullAdditions(j).size() == 100;
            assert hullManager.getHullRemovals(j).size() == 0;
        }

        // remove inner voxel from one side
        hullManager.clearPosition(get(9,5,5));

        for (int j = 0; j < 6; j++) {
            assert hullManager.getHullAdditions(j).size() == (j == 1 ? 0 : 1);
            assert hullManager.getHullRemovals(j).size() == (j == 0 ? 1 : 0);
        }

    }

    @Test
    public void testAccuracy() throws Exception {
        HullManager<String> hullManager = new HullManager<String>();

        for (int i = 0; i < 3; i++) {
            // test that double adding does not return
            // the edge "in between" as changed
            update(hullManager,get(0, i));
            update(hullManager,get(1, i));
            for (int j = 0; j < 6; j++) {
                assert hullManager.getHullAdditions(j).size() == (j/2 == i ? 1 : 2);
                assert hullManager.getHullRemovals(j).size() == 0;
            }
            for (int j = 0; j < 6; j++) {
                assert hullManager.getHullAdditions(j).size() == 0;
                assert hullManager.getHullRemovals(j).size() == 0;
            }

            // test that double removing does not return
            // the edge "in between" as changed

            assert hullManager.clearPosition(get(0, i));
            assert !hullManager.clearPosition(get(0, i));
            assert hullManager.clearPosition(get(1, i));

            for (int j = 0; j < 6; j++) {
                assert hullManager.getHullAdditions(j).size() == 0;
                assert hullManager.getHullRemovals(j).size() == (j/2 == i ? 1 : 2);
            }

            hullManager.clear();
        }

        // test that double adding only reports edges from the voxel (and not the neighbours)
        for (int i = 0; i < 3; i++) {
            update(hullManager,get(0, i));
            update(hullManager,get(1, i));
            update(hullManager,get(2, i));

            for (int j = 0; j < 6; j++) {
                assert hullManager.getHullAdditions(j).size() == (j/2 == i ? 1 : 3);
                assert hullManager.getHullRemovals(j).size() == 0;
            }

            update(hullManager,get(1, i));

            for (int j = 0; j < 6; j++) {
                assert hullManager.getHullAdditions(j).size() == (j/2 == i ? 0 : 1);
                assert hullManager.getHullRemovals(j).size() == 0;
            }

            hullManager.clear();
        }

        for (int i = 0; i < 3; i++) {
            update(hullManager,get(0, i));

            // test that empty removal does not trigger any changes
            for (int j = 0; j < 6; j++) {
                assert hullManager.getHullAdditions(j).size() == 1;
                assert hullManager.getHullRemovals(j).size() == 0;
            }

            assert !hullManager.clearPosition(get(1, i));

            for (int j = 0; j < 6; j++) {
                assert hullManager.getHullAdditions(j).size() == 0;
                assert hullManager.getHullRemovals(j).size() == 0;
            }

            hullManager.clear();
        }

    }

    @Test
    public void massTest() throws Exception {

        HullManager<String> hullManager = new HullManager<String>();

        long time = System.currentTimeMillis();

        // one million (as a block) 100 ^ 3
        for (short x = 0; x < 100; x++) {
            for (short y = 0; y < 100; y++) {
                for (short z = 0; z < 100; z++) {
                    hullManager.update(new short[]{x,y,z}, x + "," + y + "," + z);
                }
            }
        }

        for (byte b : new byte[] {0,1,2,3,4,5}) {
            System.out.println(b + " ===");
            hullManager.getHullAdditions(b);
            hullManager.getHullRemovals(b);
        }

        System.out.println("Time for 100^3 block: " + (System.currentTimeMillis() - time) + " ms");


    }
}
