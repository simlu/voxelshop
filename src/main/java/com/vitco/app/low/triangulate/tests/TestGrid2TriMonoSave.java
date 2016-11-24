package com.vitco.app.low.triangulate.tests;

import com.vitco.app.low.triangulate.Grid2TriMono;
import org.junit.Test;
import org.poly2tri.triangulation.delaunay.DelaunayTriangle;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Test class for Grid2TriMono triangulation
 */
public class TestGrid2TriMonoSave extends AbstractTriangulationTest {

    @Override
    ArrayList<DelaunayTriangle> triangulate(boolean[][] data) {
        return Grid2TriMono.triangulate(data, true);
    }

    // do many tests and check against different conditions
    @Test
    public void testTriangulation() throws IOException {
        super.testTriangulation(1, 10000, false, true);
    }

    // do a test case
    @Test
    public void testTriangulationCase() throws IOException {
        super.testTriangulationCase("test.png", "out.png", 20, false);
    }

}
