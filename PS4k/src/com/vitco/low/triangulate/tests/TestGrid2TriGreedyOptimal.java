package com.vitco.low.triangulate.tests;

import com.vitco.low.triangulate.Grid2TriGreedyOptimal;
import org.junit.Test;
import org.poly2tri.triangulation.delaunay.DelaunayTriangle;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Tests for Grid2TriGreedyOptimal
 */
public class TestGrid2TriGreedyOptimal extends AbstractTriangulationTest {

    @Override
    ArrayList<DelaunayTriangle> triangulate(boolean[][] data) {
        return Grid2TriGreedyOptimal.triangulate(data);
    }

    // do many tests and check against different conditions
    @Test
    public void testTriangulation() throws IOException {
        super.testTriangulation(1, 10000, false, false);
    }

    // do a test case
    @Test
    public void testTriangulationCase() throws IOException {
        super.testTriangulationCase("test.png", "out.png", 20, false);
    }

}
