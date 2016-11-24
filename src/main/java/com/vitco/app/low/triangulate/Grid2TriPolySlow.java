package com.vitco.app.low.triangulate;

import com.vitco.app.util.misc.StringIndexer;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Polygon;
import org.jaitools.media.jai.vectorize.VectorizeDescriptor;
import org.poly2tri.Poly2Tri;
import org.poly2tri.geometry.polygon.PolygonPoint;
import org.poly2tri.triangulation.TriangulationAlgorithm;
import org.poly2tri.triangulation.TriangulationContext;
import org.poly2tri.triangulation.TriangulationPoint;
import org.poly2tri.triangulation.delaunay.DelaunayTriangle;

import javax.media.jai.JAI;
import javax.media.jai.ParameterBlockJAI;
import javax.media.jai.RenderedOp;
import java.awt.image.RenderedImage;
import java.util.*;

/**
 * Helper class that converts a grid into triangles.
 *
 * Uses the surface outline as polygon and then triangulates that.
 *
 * Allows for optional merging of triangles (this is slow though).
 *
 * Reference:
 * http://code.google.com/p/poly2tri/
 *
 * Note: The conversion voxel -> polygone with holes uses a very slow implementation
 * with heavy resource usage
 */
public final class Grid2TriPolySlow {

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
    private final static TriangulationContext tcx = Poly2Tri.createContext(TriangulationAlgorithm.DTSweep);

    // interpolation value (to avoid duplicate values as poly2tri can't handle those)
    public final static float INTERP = 0.000001f;

    // helper - method that merges two arraylists at a given point and also interpolates the point
    // Note: Assumes that the point only exists once in each list
    // Example: (1,2,3,4,5), (6,7,4,9,10), 4 should result in (1,2,3,4,6,7,~4,9,10)
    // where the second 4 is interpolated with the interp value from above
    // Note: The interpolation is done into the "correct" direction to prevent
    // overlap of the areas that are described by the two arraylists
    public static ArrayList<PolygonPoint> mergeInterp(ArrayList<PolygonPoint> listA,
                                                      ArrayList<PolygonPoint> listB, PolygonPoint p) {
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
                    x += INTERP;
                } else if (refPoint.getX() < x) {
                    x -= INTERP;
                } else {
                    // interpolate y value
                    if (refPoint.getY() > y) {
                        y += INTERP;
                    } else if (refPoint.getY() < y) {
                        y -= INTERP;
                    }
                }
                // add the interpolated point
                result.add(new PolygonPoint(x, y));
            }
        }

        return result;
    }

    // helper - Merge triangles if they form one big triangle
    private static DelaunayTriangle reduce(DelaunayTriangle triA, DelaunayTriangle triB) {
        ArrayList<TriangulationPoint> newTri = new ArrayList<TriangulationPoint>();
        // compute which values are in both triangles
        boolean[] foundA = new boolean[3];
        boolean[] foundB = new boolean[3];
        int found = 0;
        for (int i = 0; i < 3; i++) {
            TriangulationPoint p1 = triA.points[i];
            newTri.add(p1);
            for (int j = 0; j < 3; j++) {
                TriangulationPoint p2 = triB.points[j];
                if (p1.equals(p2)) {
                    foundA[i] = true;
                    foundB[j] = true;
                    found++;
                }
            }
        }
        if (found != 2) {
            return null;
        }
        // create a triangle with four points and check if we can
        // merge this into a "real" triangle
        // the four point triangle always looks like this: n - f - n - f
        for (int i = 0; i < 3; i++) {
            if (!foundB[i]) {
                if (!foundA[0]) {
                    newTri.add(2, triB.points[i]);
                } else if (!foundA[1]) {
                    newTri.add(0, triB.points[i]);
                } else {
                    newTri.add(0, newTri.remove(2));
                    newTri.add(2, triB.points[i]);
                }
            }
        }
        // check if we can remove a point
        TriangulationPoint p1 = newTri.get(0);
        TriangulationPoint p2 = newTri.get(1);
        TriangulationPoint p3 = newTri.get(2);
        float derivative1 = (p2.getYf() - p1.getYf())/(p2.getXf() - p1.getXf());
        float derivative2 = (p3.getYf() - p1.getYf())/(p3.getXf() - p1.getXf());
        if (Math.abs(derivative1 - derivative2) < 0.001) {
            return new DelaunayTriangle(p1, p3, newTri.get(3));
        }
        p2 = newTri.get(3);
        derivative1 = (p1.getYf() - p2.getYf())/(p1.getXf() - p2.getXf());
        derivative2 = (p3.getYf() - p2.getYf())/(p3.getXf() - p2.getXf());
        if (Math.abs(derivative1 - derivative2) < 0.001) {
            return new DelaunayTriangle(p1, newTri.get(1), p3);
        }
        return null;
    }

    // helper - compress the triangles in list (merge what is possible)
    private static List<DelaunayTriangle> reduce(List<DelaunayTriangle> toMerge) {
        // loop over all entries
        for (int i = 0; i < toMerge.size() - 1; i++) {
            DelaunayTriangle tri = toMerge.get(i);
            // check all neighbours
            for (int j = i + 1; j < toMerge.size(); j++) {
                DelaunayTriangle triN = toMerge.get(j);
                // check if we can merge with the neighbour
                DelaunayTriangle merged = reduce(tri, triN);
                if (merged != null) {
                    // set merged triangle and remove neighbour
                    toMerge.set(i, merged);
                    toMerge.remove(j);
                    i--;
                    break;
                }
            }
        }
        return toMerge;
    }

    // triangulate a polygon
    // Note: Since poly2tri has problems with duplicate points in the polygon data
    // we need to "fix" that by merging border holes into the polygon outline
    // and also merging bordering inside holes. Duplicate points are moved apart
    // so that no area intersection is created.
    public static ArrayList<DelaunayTriangle> triangulate(Collection<Polygon> polys, boolean triangleReduction) {
        ArrayList<DelaunayTriangle> result = new ArrayList<DelaunayTriangle>();

        // loop over all polygon (a polygon consists of exterior and interior ring)
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
            if (triangleReduction) {
                result.addAll(reduce(polyR.getTriangles()));
            } else {
                result.addAll(polyR.getTriangles());
            }

        }

        return result;
    }

}
