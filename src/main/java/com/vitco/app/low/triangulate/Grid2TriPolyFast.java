package com.vitco.app.low.triangulate;

import com.vitco.app.util.misc.IntegerTools;
import gnu.trove.set.hash.TIntHashSet;
import org.poly2tri.Poly2Tri;
import org.poly2tri.geometry.polygon.PolygonPoint;
import org.poly2tri.triangulation.TriangulationAlgorithm;
import org.poly2tri.triangulation.TriangulationContext;
import org.poly2tri.triangulation.delaunay.DelaunayTriangle;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Takes a 2D bit array and converts it into polygons with holes.
 *
 * Also implements access to the Poly2Tri algorithm to convert the created polygons into triangles.
 *
 * Note: The main difference between this algorithm and the JAI algorithm of "voxel -> polygon" is
 * that this one already combines holes that touch inside a polygon and furthermore already integrates
 * holes that touch the outline of the polygon. This is helpful as it is required for processing
 * with Poly2Tri, but could probably be changes by changing
 *    (edgeR[4] == 1 ? 1 : -1)
 * to
 *    (edgeR[4] == 1 ? -1 : 1)
 * two times (!). Verification still required.
 */
public class Grid2TriPolyFast {

    // interpolation value
    private static final double INTERP = 0.000001;

    // ==============

    // helper - we need only one context for all conversion (faster)
    private final static TriangulationContext tcx = Poly2Tri.createContext(TriangulationAlgorithm.DTSweep);

    // triangulate a polygon, the input data is interpolated to allow Poly2Tri to process it.
    // Hence the output data is slightly "off". This can be fixed by rounding the output data, don't use (int)
    // casting though as this might round down instead of up.
    public static ArrayList<DelaunayTriangle> triangulate(short[][][] polys) {
        ArrayList<DelaunayTriangle> result = new ArrayList<DelaunayTriangle>();

        // stores and manages all seen points
        TIntHashSet indexer = new TIntHashSet();

        // loop over all polygon (a polygon consists of exterior and interior ring)
        for (short[][] poly : polys) {

            // stores an interpolated polygon ("0" entry is outline, others are holes)
            HashMap<Integer, ArrayList<PolygonPoint>> polygon = new HashMap<Integer, ArrayList<PolygonPoint>>();

            // stored the current point list (temp variable)
            ArrayList<PolygonPoint> resultOutline = new ArrayList<PolygonPoint>();
            polygon.put(0,resultOutline);

            // loop over polygon outline
            short[] outline = poly[0];
            for (int i = 0, len = outline.length - 2; i < len; i+=2) {
                // check if we need to interpolate
                if (!indexer.add(IntegerTools.makeInt(outline[i], outline[i + 1]))) {
                    resultOutline.add(new PolygonPoint(outline[i] - Math.signum(outline[i] - outline[i+2]) * INTERP, outline[i+1] - Math.signum(outline[i+1] - outline[i+3]) * INTERP));
                } else {
                    resultOutline.add(new PolygonPoint(outline[i], outline[i+1]));
                }
            }
            indexer.clear();
              
            // loop over polygon holes
            for (int j = 1; j < poly.length; j++) {
                // create new hole outline entry
                resultOutline = new ArrayList<PolygonPoint>();
                polygon.put(j,resultOutline);
                outline = poly[j];
                for (int i = 0, len = outline.length - 2; i < len; i+=2) {
                    // check if we need to interpolate
                    if (!indexer.add(IntegerTools.makeInt(outline[i], outline[i + 1]))) {
                        resultOutline.add(new PolygonPoint(outline[i] - Math.signum(outline[i] - outline[i+2]) * INTERP, outline[i+1] - Math.signum(outline[i+1] - outline[i+3]) * INTERP));
                    } else {
                        resultOutline.add(new PolygonPoint(outline[i], outline[i+1]));
                    }
                }
                indexer.clear();
            }

            // convert to polygon from raw data (zero is always the id that contains the exterior of the polygon)
            org.poly2tri.geometry.polygon.Polygon polyR = new org.poly2tri.geometry.polygon.Polygon(polygon.remove(0));
            for (ArrayList<PolygonPoint> hole : polygon.values()) {
                polyR.addHole(new org.poly2tri.geometry.polygon.Polygon(hole));
            }

            // do the triangulation and add the triangles for this polygon
            // Note: This needs to be synchronized to prevent multiple instances
            // from accessing the tcx context at once
            synchronized (tcx) {
                tcx.prepareTriangulation(polyR);
                Poly2Tri.triangulate(tcx);
                tcx.clear();
            }
            result.addAll(polyR.getTriangles());

        }

        // return all triangles
        return result;
    }

}
