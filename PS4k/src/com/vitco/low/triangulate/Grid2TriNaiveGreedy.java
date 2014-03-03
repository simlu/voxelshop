package com.vitco.low.triangulate;

import org.poly2tri.geometry.polygon.PolygonPoint;
import org.poly2tri.triangulation.delaunay.DelaunayTriangle;

import java.util.ArrayList;

/**
 * Helper class that converts a grid into triangles.
 *
 * Uses the Naive Greedy Meshing approach (highly optimized).
 *
 * Reference:
 * http://0fps.net/2012/06/30/meshing-in-a-minecraft-game/
 */
public class Grid2TriNaiveGreedy {

    // triangulate a bit array (use shorts, slower but works with higher values)
    private static ArrayList<DelaunayTriangle> triangulateSave(boolean[][] bits) {
        ArrayList<DelaunayTriangle> result = new ArrayList<DelaunayTriangle>();

        short lenX = (short) bits.length;
        short lenY = (short) bits[0].length;
        short lenXM = (short) (lenX - 1);
        short lenYM = (short) (lenY - 1);
        short pxP,pxN,pyP,pyN;

        for (short x = 0; x < lenX; x++) {
            for (short y = 0; y < lenY; y++) {
                // we found a set point -> start expanding
                if (bits[x][y]) {
                    // unset point
                    bits[x][y] = false;
                    pxP = lenXM;
                    //pxN = 0;
                    pyP = lenYM;
                    pyN = y;

                    // expand in x direction (positive)
                    for (short i = x; ++i < lenX;) {
                        if (!bits[i][y]) {
                            pxP = --i;
                            break;
                        } else {
                            bits[i][y] = false;
                        }
                    }
                    pxN = x;

                    // expand in y direction (positive)
                    loop: for (short i = y; ++i < lenY;) {
                        if (!bits[pxP][i] || !bits[pxN][i]) {
                            pyP = --i;
                            break;
                        } else {
                            bits[pxP][i] = false;
                            bits[pxN][i] = false;
                        }
                        for (short j = pxN; ++j < pxP;) {
                            if (!bits[j][i]) {
                                for (short k = j; --k > pxN;) {
                                    bits[k][i] = true;
                                }
                                bits[pxP][i] = true;
                                bits[pxN][i] = true;
                                pyP = --i;
                                break loop;
                            } else {
                                bits[j][i] = false;
                            }
                        }
                    }

                    // add the triangles for this rectangle
                    result.add(new DelaunayTriangle(new PolygonPoint(pxN, pyN), new PolygonPoint(pxP + 1, pyN), new PolygonPoint(pxN, pyP + 1)));
                    result.add(new DelaunayTriangle(new PolygonPoint(pxP + 1, pyN), new PolygonPoint(pxP + 1, pyP + 1), new PolygonPoint(pxN, pyP + 1)));
                }
            }
        }
        return result;
    }

    // triangulate a bit array (using bytes with fallback to shorts)
    public static ArrayList<DelaunayTriangle> triangulate(boolean[][] bits) {
        byte lenX = (byte) bits.length;
        byte lenY = (byte) bits[0].length;
        // use version with shorts for larger areas
        if (lenX > 127 || lenY > 127) {
            return triangulateSave(bits);
        }
        ArrayList<DelaunayTriangle> result = new ArrayList<DelaunayTriangle>();
        byte lenXM = (byte) (lenX - 1);
        byte lenYM = (byte) (lenY - 1);
        byte pxP,pxN,pyP,pyN;

        for (byte x = 0; x < lenX; x++) {
            for (byte y = 0; y < lenY; y++) {
                // we found a set point -> start expanding
                if (bits[x][y]) {
                    // unset point
                    bits[x][y] = false;
                    pxP = lenXM;
                    //pxN = 0;
                    pyP = lenYM;
                    pyN = y;

                    // expand in x direction (positive)
                    for (byte i = x; ++i < lenX;) {
                        if (!bits[i][y]) {
                            pxP = --i;
                            break;
                        } else {
                            bits[i][y] = false;
                        }
                    }
                    pxN = x;

                    // expand in y direction (positive)
                    loop: for (byte i = y; ++i < lenY;) {
                        if (!bits[pxP][i] || !bits[pxN][i]) {
                            pyP = --i;
                            break;
                        } else {
                            bits[pxP][i] = false;
                            bits[pxN][i] = false;
                        }
                        for (byte j = pxN; ++j < pxP;) {
                            if (!bits[j][i]) {
                                for (byte k = j; --k > pxN;) {
                                    bits[k][i] = true;
                                }
                                bits[pxP][i] = true;
                                bits[pxN][i] = true;
                                pyP = --i;
                                break loop;
                            } else {
                                bits[j][i] = false;
                            }
                        }
                    }

                    // add the triangles for this rectangle
                    result.add(new DelaunayTriangle(new PolygonPoint(pxN, pyN), new PolygonPoint(pxP + 1, pyN), new PolygonPoint(pxN, pyP + 1)));
                    result.add(new DelaunayTriangle(new PolygonPoint(pxP + 1, pyN), new PolygonPoint(pxP + 1, pyP + 1), new PolygonPoint(pxN, pyP + 1)));
                }
            }
        }
        return result;
    }
}
