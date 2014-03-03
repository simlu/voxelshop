package com.vitco.low.triangulate.tests;

import com.vitco.low.triangulate.Grid2TriPolyFast;
import com.vitco.low.triangulate.util.Grid2PolyHelper;
import org.junit.Test;
import org.poly2tri.triangulation.delaunay.DelaunayTriangle;

import java.io.IOException;
import java.util.ArrayList;

/**
 * - Testing Poly2Tri and in particular new conversion "voxel -> polygon" by doing in depth validation of created geometry.
 *
 * - Manual testing internal implementation against the external implementation of "voxel -> polygon".
 *
 */
public class TestGrid2TriPolyFast extends AbstractTriangulationTest {

    @Override
    ArrayList<DelaunayTriangle> triangulate(boolean[][] data) {
        return Grid2TriPolyFast.triangulate(Grid2PolyHelper.convert(data));
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
