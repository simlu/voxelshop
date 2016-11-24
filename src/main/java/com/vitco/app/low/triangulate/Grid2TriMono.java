package com.vitco.app.low.triangulate;

import org.poly2tri.Poly2Tri;
import org.poly2tri.geometry.polygon.Polygon;
import org.poly2tri.geometry.polygon.PolygonPoint;
import org.poly2tri.triangulation.delaunay.DelaunayTriangle;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Helper class that converts a grid into triangles.
 *
 * Uses a variant of the monotone polygon approach (optimized).
 *
 * Reference:
 * http://0fps.net/2012/07/07/meshing-minecraft-part-2/
 */
public class Grid2TriMono {

    // slower (and more general) implementation that doesn't require a monotone polygon
    private static java.util.List<DelaunayTriangle> triangulateSlow(ArrayList<byte[]> polyPoints) {
        ArrayList<PolygonPoint> pp = new ArrayList<PolygonPoint>();
        for (byte[] point : polyPoints) {
            pp.add(new PolygonPoint(point[0], point[1]));
        }

        Polygon poly = new Polygon(pp);
        Poly2Tri.triangulate(poly);
        return poly.getTriangles();
    }

    // helper - check if c is in between a and b
    private static boolean inBetween(byte[] a, byte[] b, byte[] c) {
        return (a != c && b != c) && // not the same points
                ((b[0] - a[0]) * (c[1] - a[1]) == (c[0] - a[0]) * (b[1] - a[1])) && // on one line
                ((a[0] < c[0] == c[0] < b[0]) && (a[1] < c[1] == c[1] < b[1])); // in between on that line
    }

    // helper to add a triangle
    private static void addTriangle(java.util.List<byte[][]> result, byte[] p1, byte[] p2, byte[] p3, ArrayList<byte[]> skippedPoints) {
        // check if we need to split the triangle on a given point
        boolean skipped = false;
        for (byte[] s : skippedPoints) {
            if (inBetween(p1, p2, s)) {
                skippedPoints.remove(s);
                addTriangle(result,s,p2,p3,skippedPoints);
                addTriangle(result, p1, s, p3, skippedPoints);
                skipped = true;
                break;
            }
            if (inBetween(p1, p3, s)) {
                skippedPoints.remove(s);
                addTriangle(result,s,p2,p3,skippedPoints);
                addTriangle(result, p1, p2, s, skippedPoints);
                skipped = true;
                break;
            }
            if (inBetween(p2, p3, s)) {
                skippedPoints.remove(s);
                addTriangle(result,p1,s,p3,skippedPoints);
                addTriangle(result, p1, p2, s, skippedPoints);
                skipped = true;
                break;
            }
        }
        // the triangle was not split and can be added
        if (!skipped) {
            result.add(new byte[][] {p1, p2, p3});
        }

    }

    // loop over previous triangles and split if point
    // is on the edge of one triangle (it is assumed that exactly one triangle
    // in the previous triangles can be split)
    private static void splitPreviousTri(byte[] p, ArrayList<byte[][]> result) {
        // reverse loop (this is one of the recently added triangles)
        for (int i = result.size() - 1; i >= 0; i--) {
            byte[][] tri = result.get(i);
            if (inBetween(tri[0], tri[1], p)) {
                result.remove(tri);
                result.add(new byte[][]{p, tri[1], tri[2]});
                result.add(new byte[][]{tri[0], p, tri[2]});
                return;
            }
            if (inBetween(tri[0], tri[2], p)) {
                result.remove(tri);
                result.add(new byte[][]{p, tri[1], tri[2]});
                result.add(new byte[][]{tri[0], tri[1], p});
                return;
            }
            if (inBetween(tri[1], tri[2], p)) {
                result.remove(tri);
                result.add(new byte[][]{tri[0], p, tri[2]});
                result.add(new byte[][]{tri[0], tri[1], p});
                return;
            }
        }
        // no triangle found ?!
        assert false;
    }

    // expects the mono polygon points to be sorted by x coordinate and
    // by y coordinate in "poly direction"
    private static java.util.List<DelaunayTriangle> triangulate(ArrayList<byte[]> orderedPoints, boolean fixEdges) {
        ArrayList<byte[][]> result = new ArrayList<byte[][]>();

        LinkedList<byte[]> stack = new LinkedList<byte[]>();
        stack.push(orderedPoints.remove(0));
        stack.push(orderedPoints.remove(0));

        // contains list with "skipped" pixel (i.e. pixel that have missed a triangulation
        ArrayList<byte[]> skippedPoints = new ArrayList<byte[]>();

        for (byte[] vi : orderedPoints) {

            byte[] top = stack.pop();
            byte[] upper = top;
            byte[] lower = top;

            if (vi[2] == top[2]) {

                boolean convex = true;
                while (convex && !stack.isEmpty()) {
                    lower = stack.pop();
                    // check angles and create triangles
                    int cross = (upper[0]-lower[0])*(vi[1]-upper[1]) - (upper[1]-lower[1])*(vi[0]-upper[0]);
                    // check if angle is convex
                    convex = Math.signum(cross) == -upper[2] || cross == 0;
                    if (convex) {
                        if (cross != 0) {
                            if (upper[2] == -1) {
                                addTriangle(result, lower, upper, vi, skippedPoints);
                            } else {
                                addTriangle(result, upper, lower, vi, skippedPoints);
                            }
                        } else if (fixEdges) {
                            boolean xdiffer, ydiffer;
                            if (((xdiffer = lower[0] != upper[0]) && (lower[0] < upper[0] == upper[0] < vi[0])) ||
                                    ((ydiffer = lower[1] != upper[1]) && (lower[1] < upper[1] == upper[1] < vi[1]))) {
                                skippedPoints.add(upper);
                            } else if (((xdiffer) && (lower[0] < vi[0] == vi[0] < upper[0])) ||
                                    ((ydiffer) && (lower[1] < vi[1] == vi[1] < upper[1]))) {
                                // we need to consider old triangles now (!)
                                splitPreviousTri(vi, result);
                            } else {
                                skippedPoints.add(lower);
                            }
                        }
                    } else {
                        stack.push(lower);
                        stack.push(upper);
                    }
                    upper = lower;
                }
                if (convex) {
                    stack.push(lower);
                }

            } else {
                // connect to all points on stack
                while (!stack.isEmpty()) {
                    lower = stack.pop();
                    // test if points form a line
                    // note: this is only ok b/c we work with voxel (!)
                    boolean areLineX = (upper[0] == lower[0] && lower[0] == vi[0]);
                    boolean areLineY = (upper[1] == lower[1] && lower[1] == vi[1]);
                    if (!areLineX && !areLineY) {
                        if (upper[2] == -1) {
                            addTriangle(result, lower, upper, vi, skippedPoints);
                        } else {
                            addTriangle(result, upper, lower, vi, skippedPoints);
                        }
                    } else if (fixEdges) {
                        // Note: case where vi lies in the middle doesn't exist
                        if ((areLineX && (lower[1] < upper[1] == upper[1] < vi[1])) ||
                                (areLineY && (lower[0] < upper[0] == upper[0] < vi[0]))) {
                            skippedPoints.add(upper);
                        } else {
                            skippedPoints.add(lower);
                        }
                    }
                    upper = lower;
                }
                stack.push(top);
            }
            stack.push(vi);
        }

        assert skippedPoints.isEmpty();

        // convert to triangles
        List<DelaunayTriangle> resultObjects = new ArrayList<DelaunayTriangle>();
        for (byte[][] tri : result) {
            resultObjects.add(new DelaunayTriangle(new PolygonPoint(tri[0][0], tri[0][1]), new PolygonPoint(tri[1][0], tri[1][1]), new PolygonPoint(tri[2][0], tri[2][1])));
        }
        return resultObjects;
    }


    // clone a 2D boolean array
    public static boolean[][] clone2DArray(boolean[][] array) {
        int rows = array.length;
        boolean[][] newArray = array.clone();
        for(int row=0; row < rows; row++){
            newArray[row]=array[row].clone();
        }
        return newArray;
    }

    // triangulate the bit array (treat as voxel)
    public static ArrayList<DelaunayTriangle> triangulate(boolean[][] bits, boolean fixEdges) {
        byte lenX = (byte) bits.length;
        byte lenY = (byte) bits[0].length;
        byte pyP,pyN,pyPprev,pyNprev,start;
        boolean cleanup;
        byte[] p;

        boolean[][] grayList = clone2DArray(bits);

        ArrayList<DelaunayTriangle> result = new ArrayList<DelaunayTriangle>();
        ArrayList<byte[]> orderedPoints = new ArrayList<byte[]>();

        // split out voxel area in mono polygons
        // loop over all points
        for (byte x = 0; x < lenX; x++) {
            for (byte y = 0; y < lenY; y++) {

                // check if this position is set
                if (bits[x][y]) {

                    // clear point list
                    orderedPoints.clear();

                    // set the previous points
                    pyPprev = -1;
                    pyNprev = -1;

                    // initial starting position
                    start = y;
                    // loop over all columns
                    for (byte i = x; i < lenX; ) {

                        // initialize borders
                        pyN = 0;
                        pyP = lenY;

                        // -- handle positive y direction
                        for (byte j = start; j < lenY; j++) {
                            if (bits[i][j]) {
                                bits[i][j] = false;
                            } else {
                                pyP = j;
                                break;
                            }
                        }

                        // -- handle negative y direction
                        for (byte j = start; --j > -1;) {
                            if (bits[i][j]) {
                                bits[i][j] = false;
                            } else {
                                pyN = ++j;
                                break;
                            }
                        }

                        // ----------

                        // add missing poly points
                        if (pyNprev != pyN) {
                            if (pyNprev != -1) {
                                p = new byte[] {i, pyNprev, -1};
                                orderedPoints.add(p);
                            } else {
                                pyNprev = y;
                            }
                            if (fixEdges) {
                                // >>>
                                // check forward y overlap (negative)
                                boolean prevFound = grayList[i][pyN];
                                for (byte j = pyNprev; ++j < pyN;) {
                                    if (grayList[i][j] && !prevFound) {
                                        p = new byte[] {i, j, -1};
                                        orderedPoints.add(p);
                                    } else if (!grayList[i][j] && prevFound) {
                                        p = new byte[] {i, j, -1};
                                        orderedPoints.add(p);
                                    }
                                    prevFound = grayList[i][j];
                                }
                                // <<<
                                // >>>
                                if (i > 0) {
                                    // check backward y overlap (negative)
                                    prevFound = pyNprev > 0 && grayList[i-1][pyNprev-1];
                                    for (byte j = (byte) (pyNprev-2); j >= pyN; j--) {
                                        if (grayList[i-1][j] && !prevFound) {
                                            p = new byte[] {i, (byte) (j+1), -1};
                                            orderedPoints.add(p);
                                        } else if (!grayList[i-1][j] && prevFound) {
                                            p = new byte[] {i, (byte) (j+1), -1};
                                            orderedPoints.add(p);
                                        }
                                        prevFound = grayList[i-1][j];
                                    }
                                }
                                // <<<
                            }
                            p = new byte[] {i, pyN, -1};
                            orderedPoints.add(p);
                            pyNprev = pyN;
                        }
                        if (pyPprev != pyP) {
                            if (pyPprev != -1) {
                                p = new byte[] {i, pyPprev, 1};
                                orderedPoints.add(p);
                            } else {
                                pyPprev = y;
                            }
                            if (fixEdges) {
                                // >>>
                                // check forward y overlap (positive)
                                boolean prevFound = pyPprev > 0 && grayList[i][pyPprev-1];
                                for (byte j = pyPprev; --j > pyP;) {
                                    if (grayList[i][j-1] && !prevFound) {
                                        p = new byte[] {i, j, 1};
                                        orderedPoints.add(p);
                                    } else if (!grayList[i][j-1] && prevFound) {
                                        p = new byte[] {i, j, 1};
                                        orderedPoints.add(p);
                                    }
                                    prevFound = grayList[i][j-1];
                                }
                                // <<<
                                // >>>
                                if (i > 0) {
                                    // check backward y overlap (positive)
                                    prevFound = pyPprev < lenY && grayList[i-1][pyPprev];
                                    for (byte j = pyPprev; ++j < pyP;) {
                                        if (grayList[i-1][j] && !prevFound) {
                                            p = new byte[] {i, j, 1};
                                            orderedPoints.add(p);
                                        } else if (!grayList[i-1][j] && prevFound) {
                                            p = new byte[] {i, j, 1};
                                            orderedPoints.add(p);
                                        }
                                        prevFound = grayList[i-1][j];
                                    }
                                }
                                // <<<
                            }
                            p = new byte[] {i, pyP, 1};
                            orderedPoints.add(p);
                            pyPprev = pyP;
                        }

                        // -----------
                        // find new start position
                        cleanup = true;
                        if (++i < lenX) {
                            for (byte j = pyN; j < pyP; j++) {
                                if (bits[i][j]) {
                                    start = j;
                                    cleanup = false;
                                    break;
                                }
                            }
                        }
                        // stop searching (no connection found)
                        if (cleanup) {
                            // add closure points
                            p = new byte[] {i, pyN, -1};
                            orderedPoints.add(p);
                            p = new byte[] {i, pyP, 1};
                            orderedPoints.add(p);

                            if (fixEdges) {
                                // >>>
                                // add final edge points
                                if (i < lenX) {
                                    boolean prevFound = grayList[i][pyP-1];
                                    for (byte j = (byte) (pyP - 2); j >= pyN; j--) {
                                        if (grayList[i][j] && !prevFound) {
                                            p = new byte[] {i, (byte) (j+1), 1};
                                            orderedPoints.add(p);
                                        } else if (!grayList[i][j] && prevFound) {
                                            p = new byte[] {i, (byte) (j+1), 1};
                                            orderedPoints.add(p);
                                        }
                                        prevFound = grayList[i][j];
                                    }
                                }
                                // <<<
                            }

                            break;
                        }

                    }

                    result.addAll(triangulate(orderedPoints, fixEdges));

                }
            }
        }


        return result;
    }

}
