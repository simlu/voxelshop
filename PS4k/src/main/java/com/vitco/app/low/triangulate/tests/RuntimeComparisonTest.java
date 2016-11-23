package com.vitco.app.low.triangulate.tests;

import com.vitco.app.low.triangulate.Grid2TriGreedyOptimal;
import com.vitco.app.low.triangulate.Grid2TriMono;
import com.vitco.app.low.triangulate.Grid2TriNaiveGreedy;
import com.vitco.app.low.triangulate.Grid2TriPolyFast;
import com.vitco.app.low.triangulate.util.Grid2PolyHelper;
import org.junit.Test;
import org.poly2tri.Poly2Tri;

import java.util.ArrayList;
import java.util.Random;

/**
 * Compare how the run times of the different algorithms behalf.
 */
public class RuntimeComparisonTest {

    private static final Random rand = new Random();

    // return a random bit grid for a specific size
    private static boolean[][] getRandomBitGrid(int size) {
        boolean[][] data = new boolean[size][size];
        // fill half of the entries with ones
        for (int i = 0, len = (size*size)/2; i < len; i++) {
            // get a random position
            int x = rand.nextInt(size);
            int y = rand.nextInt(size);
            if (!data[x][y]) {
                data[x][y] = true;
            } else {
                // find another position
                i--;
            }
        }
        return data;
    }

    // print a list for maple input
    private static void printList(ArrayList<Long> list, String name) {
        // print final results
        System.out.print(name + " := [");
        boolean first = true;
        for (int i = 0; i < list.size(); i++) {
            Long val = list.get(i);
            System.out.print((first ? "" : ",") + "[" + (i*10+10) + ","  + val + "]");
            first = false;
        }
        System.out.println("];");
    }

    @Test
    public final void testRuntimes() {

        Poly2Tri.warmup();

        // warmup
        for (int i = 0; i < 10000; i++) {
            boolean[][] grid = getRandomBitGrid(10);
            Grid2TriGreedyOptimal.triangulate(grid);
            Grid2TriNaiveGreedy.triangulate(grid);
            Grid2TriMono.triangulate(grid, false);
            Grid2TriPolyFast.triangulate(Grid2PolyHelper.convert(grid));
        }

        ArrayList<Long> greedyOptList = new ArrayList<Long>();
        ArrayList<Long> naiveGreedyList = new ArrayList<Long>();
        ArrayList<Long> monoList = new ArrayList<Long>();
        ArrayList<Long> polyList = new ArrayList<Long>();

        int repeat = 20;

        for (int i = 10; i <= 700; i += 10) {

            System.out.println(i);

            long greedyOpt = 0;
            long naiveGreedy = 0;
            long mono = 0;
            long poly = 0;

            // repeat each test
            for (int j = 0; j < repeat; j++) {
                boolean[][] grid = getRandomBitGrid(i);
                //test algorithm
                greedyOpt -= System.currentTimeMillis();
                Grid2TriGreedyOptimal.triangulate(grid);
                greedyOpt += System.currentTimeMillis();
                // test algorithm
                naiveGreedy -= System.currentTimeMillis();
                Grid2TriNaiveGreedy.triangulate(grid);
                naiveGreedy += System.currentTimeMillis();
                // test algorithm
                mono -= System.currentTimeMillis();
                Grid2TriMono.triangulate(grid, false);
                mono += System.currentTimeMillis();
                // test algorithm
                poly -= System.currentTimeMillis();
                Grid2TriPolyFast.triangulate(Grid2PolyHelper.convert(grid));
                poly += System.currentTimeMillis();
            }

            // normalize
            greedyOpt /= repeat;
            naiveGreedy /= repeat;
            mono /= repeat;
            poly /= repeat;

            // fill into arrays
            greedyOptList.add(greedyOpt);
            naiveGreedyList.add(naiveGreedy);
            monoList.add(mono);
            polyList.add(poly);
        }

        printList(greedyOptList, "greedyOpt");
        printList(naiveGreedyList, "naiveGreedy");
        printList(monoList, "mono");
        printList(polyList, "poly");

    }

}
