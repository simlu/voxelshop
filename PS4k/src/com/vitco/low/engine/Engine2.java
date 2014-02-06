package com.vitco.low.engine;

import edu.princeton.cs.algs4.RedBlackBST;

/**
 * Created by flux on 12/16/13.
 */
public class Engine2 {

    private static final class Value {
        private final int id;
        private final int length;

        private Value(int id, int length) {

            this.id = id;
            this.length = length;
        }
    }

    private RedBlackBST<Integer, Value> redBlackBST = new RedBlackBST<Integer, Value>();

    // -----------------

    public final void add(int[][] xyzs, VoxelType type) {
//        Arrays.sort(xyzs, new Comparator<int[]>() {
//            @Override
//            public int compare(int[] o1, int[] o2) {
//                int sign = Integer.compare(o1[1], o2[1]);
//                if (sign == 0) {
//                    sign = Integer.compare(o1[2], o2[2]);
//                    if (sign == 0) {
//                        return Integer.compare(o1[0], o2[0]); // x difference
//                    }
//                    return sign; // z difference
//                }
//                return sign; // y difference
//            }
//        });

        // summarize runs and add them to the tree
        // ...
    }

}
