package com.vitco.util.triangulate;

import com.vitco.util.StringIndexer;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Polygon;
import org.jaitools.media.jai.vectorize.VectorizeDescriptor;
import org.poly2tri.Poly2Tri;
import org.poly2tri.geometry.polygon.PolygonPoint;
import org.poly2tri.triangulation.TriangulationAlgorithm;
import org.poly2tri.triangulation.TriangulationContext;
import org.poly2tri.triangulation.delaunay.DelaunayTriangle;

import javax.media.jai.JAI;
import javax.media.jai.ParameterBlockJAI;
import javax.media.jai.RenderedOp;
import java.awt.image.RenderedImage;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;

/**
 * Helper class that converts a grid into triangles.
 */
public final class Grid2Tri {

    // initialize the parameter block
    private final static ParameterBlockJAI pb = new ParameterBlockJAI("Vectorize");
    static {
        pb.setParameter("outsideValues", Collections.singleton(0));
    }

    // helper - converts "black and white" image into vector representation
    @SuppressWarnings("unchecked")
    public static Collection<Polygon> doVectorize(RenderedImage src) {
        pb.setSource("source0", src);

        // Get the desintation image: this is the unmodified source image data
        // plus a property for the generated vectors
        RenderedOp dest = JAI.create("Vectorize", pb);
        pb.removeSources(); // free data (references)

        // Get the vectors
        Object property = dest.getProperty(VectorizeDescriptor.VECTOR_PROPERTY_NAME);

        // Note: this is unchecked (but should be fine) - order doesn't matter
        return (Collection<Polygon>)property;
    }

    // we need only one context for all conversion (faster)
    private static TriangulationContext tcx = Poly2Tri.createContext(TriangulationAlgorithm.DTSweep);

    // interpolation value (to avoid duplicate values as poly2tri can't handle those)
    private final static float interp = 0.000001f;

    // method that merges two arraylists at a given point and also interpolates the point
    // Note: Assumes that the point only exists once in each list
    // Example: (1,2,3,4,5), (6,7,4,9,10), 4 should result in (1,2,3,4,6,7,~4,9,10)
    // where the second 4 is interpolated with the interp value from above
    // Note: The interpolation is done into the "correct" direction to prevent
    // overlap of the areas that are described by the two arraylists
    public static ArrayList<PolygonPoint> mergeInterp(
            ArrayList<PolygonPoint> listA, ArrayList<PolygonPoint> listB, PolygonPoint p) {
        ArrayList<PolygonPoint> result = new ArrayList<PolygonPoint>();

        // true once the point was found in the inner list
        boolean found = false;
        // counter that indicates how many values were inserted from
        // the second list after the point was found in it
        int count = 0;

        // loop over first list
        for (int i = 0, size = listA.size(); i < size; i++) {
            // add point to new list
            PolygonPoint pA = listA.get(i);
            result.add(pA);
            // check if this is the merge point
            if (pA.getX() == p.getX() && pA.getY() == p.getY()) {
                // loop over second list
                for (PolygonPoint pB : listB) {
                    if (pB.getX() == p.getX() && pB.getY() == p.getY()) {
                        // this is the merge point in the inner list
                        found = true;
                    } else {
                        // check if we already found the point in the second list
                        if (found) {
                            count++;
                            result.add(i + count, pB);
                        } else {
                            result.add(pB);
                        }
                    }
                }

                // interpolate the second occurrence of the merge point (into the correct direction!)
                PolygonPoint refPoint = i + 1 < size ? listA.get(i + 1) : listA.get(0);
                double x = pA.getX();
                double y = pA.getY();
                // interpolate x value if appropriate
                if (refPoint.getX() > x) {
                    x += interp;
                } else if (refPoint.getX() < x) {
                    x -= interp;
                } else {
                    // interpolate y value
                    if (refPoint.getY() > y) {
                        y += interp;
                    } else if (refPoint.getY() < y) {
                        y -= interp;
                    }
                }
                // add the interpolated point
                result.add(new PolygonPoint(x, y));
            }
        }

        return result;
    }

    // triangulate a polygon
    // Note: Since poly2tri has problems with duplicate points in the polygon data
    // we need to "fix" that by merging border holes into the polygon outline
    // and also merging bordering inside holes. Duplicate points are moved apart
    // so that no area intersection is created.
    public static ArrayList<DelaunayTriangle> triangulate(Collection<Polygon> polys) {
        ArrayList<DelaunayTriangle> result = new ArrayList<DelaunayTriangle>();

        // loop over all polygon (a polygon consists or exterior and interior ring)
        for (Polygon poly : polys) {
            // stores and manages all seen points
            StringIndexer indexer = new StringIndexer();

            // stores the "fixed" polygon (zero entry is outside, others are holes)
            HashMap<Integer, ArrayList<PolygonPoint>> polygon = new HashMap<Integer, ArrayList<PolygonPoint>>();

            // initialize
            ArrayList<PolygonPoint> active = new ArrayList<PolygonPoint>();
            int activeId = 0;
            polygon.put(activeId, active);

            // loop over polygon outline (has no clashing points)
            Coordinate[] coordinates = poly.getExteriorRing().getCoordinates();
            for (int i = 0; i < coordinates.length - 1; i++) {
                // add point to list
                PolygonPoint point = new PolygonPoint(coordinates[i].x, coordinates[i].y);
                active.add(point);
                // index the point
                indexer.index(point.toString(), activeId);
            }

            // loop over all holes
            for (int n = 0, size = poly.getNumInteriorRing(); n < size; n++) {

                // create new active point list
                active = new ArrayList<PolygonPoint>();
                activeId++;

                // not empty iff this point was seen
                ArrayList<Integer> seenInList = new ArrayList<Integer>();
                ArrayList<PolygonPoint> seenPointsList = new ArrayList<PolygonPoint>();
                boolean needToMerge = false;

                // loop over all points in this hole
                coordinates = poly.getInteriorRingN(n).getCoordinates();
                for (int i = 0; i < coordinates.length - 1; i++) {
                    // add point to list (holes)
                    PolygonPoint point = new PolygonPoint(coordinates[i].x, coordinates[i].y);
                    active.add(point);
                    // check if this needs merging
                    Integer seenInTmp = indexer.getIndex(point.toString());
                    if (seenInTmp != null) {
                        // store all information we need for merging
                        seenInList.add(seenInTmp);
                        seenPointsList.add(point);
                        needToMerge = true;
                    } else {
                        // point is unknown, add to index
                        indexer.index(point.toString(), activeId);
                    }
                }

                // merge
                if (needToMerge) {
                    // initial merge (the active list is not stored in "polygon" yet)
                    // Note: there might be no points indexed yet with activeId (if all points in hole
                    // were already indexed before!)
                    int prevSeenIn = seenInList.get(0);
                    polygon.put(prevSeenIn, mergeInterp(polygon.get(prevSeenIn), active, seenPointsList.get(0)));
                    indexer.changeIndex(prevSeenIn, activeId);

                    // merge further seen points
                    for (int i = 1; i < seenInList.size(); i++) {
                        // retrieve merge information
                        Integer seenIn = seenInList.get(i);
                        PolygonPoint point = seenPointsList.get(i);

                        // We always merge to lower id. This is required since the lowest id is
                        // the exterior ring of the polygon.
                        int mergeTo = Math.min(seenIn, prevSeenIn);
                        int mergeFrom = Math.max(seenIn, prevSeenIn);

                        // further merge
                        polygon.put(mergeTo, mergeInterp(polygon.get(mergeTo), polygon.get(mergeFrom), point));
                        indexer.changeIndex(mergeTo, mergeFrom);
                        // update all remaining merges (the index might no longer exist!)
                        for (int j = i + 1; j < seenInList.size(); j++) {
                            if (seenInList.get(j) == mergeFrom) {
                                seenInList.set(j, mergeTo);
                            }
                        }
                        // remove old list
                        polygon.remove(mergeFrom);

                        // store the id that we previously merged to (for next merge)
                        prevSeenIn = mergeTo;
                    }
                } else {
                    polygon.put(activeId, active);
                }

            }

            // convert to polygon from raw data (zero is always the id that contains the exterior of the polygon)
            org.poly2tri.geometry.polygon.Polygon polyR = new org.poly2tri.geometry.polygon.Polygon(polygon.remove(0));
            for (ArrayList<PolygonPoint> hole : polygon.values()) {
                polyR.addHole(new org.poly2tri.geometry.polygon.Polygon(hole));
            }

            // do the triangulation and add the triangles
            tcx.prepareTriangulation(polyR);
            Poly2Tri.triangulate(tcx);
            tcx.clear();
            result.addAll(polyR.getTriangles());

        }

        return result;
    }

}
