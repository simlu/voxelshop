package com.vitco.app.low.triangulate.util;

import com.vitco.app.util.misc.IntegerTools;
import gnu.trove.list.array.TShortArrayList;
import gnu.trove.map.hash.TIntObjectHashMap;

import java.util.ArrayList;

/**
 * Helper class to convert a binary grid into a polygon with holes
 *
 * The method first extracts the edges and then puts them together while determining
 * which hole belongs to which hole.
 *
 * Reference: http://www.lsi.upc.edu/~jmartinez/publications/VPAM12.pdf
 *
 * This particular implementation should be very fast!
 */
public class Grid2PolyHelper {
    // extract edges from an bit array (data)
    // When the method terminates, the vertEdges ArrayList holds the vertical edges sorted in x direction (but not sorted
    // in any y direction) and the edges HashMap, meshes the first coordinate point hash "makeInt" to a short[] array.
    // The short array contains in that order: x1, y1, x2, y2, direction (1 or 0), polygonid (always initialized with -1)
    private static void extractEdges(boolean[][] data, ArrayList<short[]> vertEdges, TIntObjectHashMap<short[]> edges) {
        // temporary array to hold current edge
        short[] edge;

        // prepare dimension variables
        short lenX = (short) data.length;
        short lenXM = (short) (lenX-1);
        short lenY = (short) data[0].length;
        short lenYM = (short) (lenY-1);

        // used for edge computation to memorize starting/stop position
        // when traversing the edges
        short start = -1;
        short stop = -1;

        // ==================
        // compute vertical edges

        // find inner vertical edges
        for (short x = 1, xM = 0; x < lenX; xM = x, x++) {
            for (short y = 0; y < lenY; y++) {
                // ---------------
                if ((data[xM][y] || data[xM][y] == data[x][y]) && start != -1) {
                    // add vertex
                    edge = new short[] {x,start,x,y,1,-1};
                    edges.put(-IntegerTools.makeInt(edge[0], edge[1]),edge);
                    vertEdges.add(edge);
                    start = -1;
                }
                if ((data[x][y] || data[xM][y] == data[x][y]) && stop != -1) {
                    // add vertex
                    edge = new short[] {x,y,x,stop,0,-1};
                    edges.put(IntegerTools.makeInt(edge[0], edge[1]),edge);
                    vertEdges.add(edge);
                    stop = -1;
                }
                if (data[xM][y] ^ data[x][y]) {
                    if (data[xM][y] && stop == -1) {
                        stop = y;
                    } else if (!data[xM][y] && start == -1) {
                        start = y;
                    }
                }
                // --------------
            }
            // ---------------
            // finish vertical inner edges that start/end at the bottom of the column
            if (start != -1) {
                edge = new short[] {x,start,x,lenY,1,-1};
                edges.put(-IntegerTools.makeInt(edge[0], edge[1]),edge);
                vertEdges.add(edge);
                start = -1;
            }
            if (stop != -1) {
                edge = new short[] {x,lenY,x,stop,0,-1};
                edges.put(IntegerTools.makeInt(edge[0], edge[1]),edge);
                vertEdges.add(edge);
                stop = -1;
            }
            // -------------
        }

        // =============
        // find outside vertical edges
        for (short y = 0; y < lenY; y++) {
            if (!data[0][y] && start != -1) {
                // add vertex
                edge = new short[] {0,start,0,y,1,-1};
                edges.put(-IntegerTools.makeInt(edge[0], edge[1]), edge);
                vertEdges.add(0,edge);
                start = -1;
            }
            if (!data[lenXM][y] && stop != -1) {
                // add vertex
                edge = new short[] {lenX,y,lenX,stop,0,-1};
                edges.put(IntegerTools.makeInt(edge[0], edge[1]),edge);
                vertEdges.add(edge);
                stop = -1;
            }
            if (data[0][y] && start == -1) {
                start = y;
            }
            if (data[lenXM][y] && stop == -1) {
                stop = y;
            }
        }
        // finish vertical outside edges that start/end at the bottom of the two columns
        if (start != -1) {
            edge = new short[] {0,start,0,lenY,1,-1};
            edges.put(-IntegerTools.makeInt(edge[0], edge[1]),edge);
            vertEdges.add(0,edge);
            start = -1;
        }
        if (stop != -1) {
            edge = new short[] {lenX, lenY, lenX, stop,0,-1};
            edges.put(IntegerTools.makeInt(edge[0], edge[1]),edge);
            vertEdges.add(edge);
            stop = -1;
        }

        // ======================
        // compute horizontal edges

        // find inner horizontal edges
        for (short y = 1, yM = 0; y < lenY; yM = y, y++) {
            for (short x = 0; x < lenX; x++) {
                // ---------------
                if ((data[x][yM] || data[x][yM] == data[x][y]) && start != -1) {
                    // add vertex
                    edge = new short[] {x,y,start,y,0,-1};
                    edges.put(-IntegerTools.makeInt(edge[0], edge[1]), edge);
                    start = -1;
                }
                if ((data[x][y] || data[x][yM] == data[x][y]) && stop != -1) {
                    // add vertex
                    edge = new short[] {stop,y,x,y,1,-1};
                    edges.put(IntegerTools.makeInt(edge[0], edge[1]), edge);
                    stop = -1;
                }
                if (data[x][yM] ^ data[x][y]) {
                    if (data[x][yM] && stop == -1) {
                        stop = x;
                    } else if (!data[x][yM] && start == -1) {
                        start = x;
                    }
                }
                // --------------
            }
            // ---------------
            // finish inner horizontal edges that start/end at the end of the row
            if (start != -1) {
                edge = new short[] {lenX,y,start,y,0,-1};
                edges.put(-IntegerTools.makeInt(edge[0], edge[1]), edge);
                start = -1;
            }
            if (stop != -1) {
                edge = new short[] {stop,y,lenX,y,1,-1};
                edges.put(IntegerTools.makeInt(edge[0], edge[1]), edge);
                stop = -1;
            }
            // -------------
        }

        // =============
        // compute outside horizontal edges

        for (short x = 0; x < lenX; x++) {
            if (!data[x][0] && start != -1) {
                // add vertex
                edge = new short[] {x,0,start,0,0,-1};
                edges.put(-IntegerTools.makeInt(edge[0], edge[1]), edge);
                start = -1;
            }
            if (!data[x][lenYM] && stop != -1) {
                // add vertex
                edge = new short[] {stop,lenY,x,lenY,1,-1};
                edges.put(IntegerTools.makeInt(edge[0], edge[1]), edge);
                stop = -1;
            }
            if (data[x][0] && start == -1) {
                start = x;
            }
            if (data[x][lenYM] && stop == -1) {
                stop = x;
            }
        }
        // finish horizontal edges that start/end at the end of the two rows
        if (start != -1) {
            edge = new short[] {lenX,0,start,0,0,-1};
            edges.put(-IntegerTools.makeInt(edge[0], edge[1]), edge);
            //start = -1;
        }
        if (stop != -1) {
            edge = new short[] {stop,lenY,lenX,lenY,1,-1};
            edges.put(IntegerTools.makeInt(edge[0], edge[1]), edge);
            //stop = -1;
        }
    }

    // convert bit data into polygon, the result is structured as following:
    // the first array holds the different polygons, the first inner array holds as
    // a first entry the polygon outline and the following entries describe the outline
    // of the holes. The inner most arrays finally describe outlines and are structured
    // as x1,y1,x2,y2,x3,y3,....,xn,yn,x1,y1
    public static short[][][] convert(boolean[][] data) {
        // result list that still needs conversion into array, each array list holds a polygon.
        // The outline is stored in the first entry and the holes as further entries (optionally).
        TIntObjectHashMap<ArrayList<short[]>> result = new TIntObjectHashMap<ArrayList<short[]>>();

        // ----- extract unprocessed edges
        // holds the vertically edges and all edges
        ArrayList<short[]> vertEdges = new ArrayList<short[]>();
        TIntObjectHashMap<short[]> edges = new TIntObjectHashMap<short[]>();

        // extract the edges from out input data
        extractEdges(data, vertEdges, edges);

        // ----- initialize variables
        // temporary variables
        TShortArrayList outlineValues = new TShortArrayList();
        int id;
        short[] edgeR;

        // holds poly id information for current column
        short[] polyIds = new short[data[0].length];
        // holds current poly id
        short polyId = 0;

        // ----- combine the edges in a "smart" way that allows us to extract poly/hole relationship while
        // ----- building the outlines, similar concept: http://www.lsi.upc.edu/~jmartinez/publications/VPAM12.pdf

        // loop over vertical edges
        for (short[] edge : vertEdges) {
            // if this edge is not analysed yet
            if (edge[5] == -1) {
                // check edge orientation
                if (edge[4] == 1) { // down orientation
                    // find the outline and add as polygon
                    edgeR = edge;
                    outlineValues.add(edgeR[0]);
                    outlineValues.add(edgeR[1]);
                    // traverse out HashMap into the "correct" direction
                    // until we find the starting edge again
                    do {
                        outlineValues.add(edgeR[2]);
                        outlineValues.add(edgeR[3]);
                        edgeR[5] = polyId; // mark edge with polygon id
                        id = IntegerTools.makeInt(edgeR[2], edgeR[3]) * (edgeR[4] == 1 ? 1 : -1);
                        edgeR = edges.get(id);
                        if (edgeR == null) edgeR = edges.get(-id);
                    } while (edgeR != edge);
                    // store the polygon
                    short[] polyArray = new short[outlineValues.size()];
                    outlineValues.toArray(polyArray);
                    outlineValues.clear();
                    ArrayList<short[]> poly = new ArrayList<short[]>();
                    poly.add(polyArray);
                    result.put(polyId, poly);
                    polyId++;
                } else { // up orientation
                    // find the outline and add as hole
                    edge[5] = polyIds[edge[1]]; // check which polygon this hole belongs to
                    edgeR = edge;
                    outlineValues.add(edgeR[0]);
                    outlineValues.add(edgeR[1]);
                    // traverse out HashMap into the "correct" direction
                    // until we find the starting edge again
                    do {
                        outlineValues.add(edgeR[2]);
                        outlineValues.add(edgeR[3]);
                        edgeR[5] = edge[5]; // mark edge with polygon id
                        id = IntegerTools.makeInt(edgeR[2], edgeR[3]) * (edgeR[4] == 1 ? 1 : -1);
                        edgeR = edges.get(id);
                        if (edgeR == null) edgeR = edges.get(-id);
                    } while (edgeR != edge);
                    // store the polygon
                    short[] polyArray = new short[outlineValues.size()];
                    outlineValues.toArray(polyArray);
                    outlineValues.clear();
                    ArrayList<short[]> poly = result.get(edge[5]);
                    poly.add(polyArray);
                }
            }
            // update our sweep line with the information which polygon is currently
            // processed in the corresponding row
            if (edge[4] == 1) {
                for (int y = edge[1]; y < edge[3]; y++) {
                    polyIds[y] = edge[5];
                }
            } else {
                for (int y = edge[3]; y < edge[1]; y++) {
                    polyIds[y] = 0;
                }
            }
        }

        // convert and return result
        short[][][] resultArray = new short[result.size()][][];
        for (int i = 0; i < result.size(); i++) {
            ArrayList<short[]> list = result.get(i);
            resultArray[i] = new short[list.size()][];
            list.toArray(resultArray[i]);
        }
        return resultArray;
    }
}
