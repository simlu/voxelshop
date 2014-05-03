package com.vitco.low.triangulate;

import org.poly2tri.geometry.polygon.PolygonPoint;
import org.poly2tri.triangulation.delaunay.DelaunayTriangle;

import java.util.ArrayList;

/**
 * A naive triangulation that simply puts two triangles for every voxel side.
 */
public class Grid2TriNaive {

    // compute the triangulation
    public static ArrayList<DelaunayTriangle> triangulate(boolean[][] bits) {
        ArrayList<DelaunayTriangle> result = new ArrayList<DelaunayTriangle>();
        int lenY = bits[0].length;
        for (int x = 0; x < bits.length; x++) {
            for (int y = 0; y < lenY; y++) {
                if (bits[x][y]) {
                    result.add(new DelaunayTriangle(new PolygonPoint(x, y), new PolygonPoint(x + 1, y), new PolygonPoint(x, y + 1)));
                    result.add(new DelaunayTriangle(new PolygonPoint(x, y + 1), new PolygonPoint(x + 1, y), new PolygonPoint(x + 1, y + 1)));
                }
            }
        }
        return result;
    }
}
