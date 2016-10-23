package com.vitco.export.generic.container;

import gnu.trove.iterator.TIntIntIterator;
import gnu.trove.map.hash.TIntIntHashMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Manages the texture triangles and exposes (complex) useful information.
 */
public class TexTriangleManager {

    // holds all the stored triangles
    private final ArrayList<TexTriangle> triangles = new ArrayList<TexTriangle>();

    // add a triangle to this manager
    public final void addTriangle(TexTriangle tri) {
        triangles.add(tri);
    }

    // get all stored triangles
    public final TexTriangle[] getTriangles() {
        TexTriangle[] result = new TexTriangle[triangles.size()];
        triangles.toArray(result);
        return result;
    }

    // return random sample color from each texture, int[] {rgb, count}
    public final int[][] getSampleRgbs() {
        // sample colors, obtain at least one color from every texture
        TIntIntHashMap colors = new TIntIntHashMap();
        for (TexTriangle tri : this.getTriangles()) {
            int rgb = tri.getTexture().getSampleRGB();
            colors.adjustOrPutValue(rgb, 1, 1);
        }
        TIntIntIterator it = colors.iterator();
        final int[][] result = new int[colors.size()][];
        for (int i = 0; i < result.length; i++) {
            it.advance();
            result[i] = new int [] {it.key(), it.value()};
        }
        return result;
    }

    // retrieve the different texture ids and their "use count" as (id, count)
    public final int[][] getTextureIds() {
        // extract unique ids and count
        HashMap<Integer, Integer> idList = new HashMap<Integer, Integer>();
        for (TexTriangle tri : triangles) {
            int texId = tri.getTexture().getId();
            Integer count = idList.get(texId);
            if (count == null) {
                count = 0;
            }
            idList.put(texId, count+1);
        }
        // convert to result array
        int[][] result = new int[idList.size()][2];
        int i = 0;
        for (Map.Entry<Integer, Integer> entry : idList.entrySet()) {
            result[i][0] = entry.getKey();
            result[i++][1] = entry.getValue();
        }
        return result;
    }

    // ----------------

    // get the triangle coordinate list
    // (i.e. "[p1_ uv1 p2 uv2 p3 uv3]_tri1 [p1_ uv1 p2 uv2 p3 uv3]_tri2 ...")
    public final String getTrianglePolygonList(Integer groupId, Integer rgb, boolean exportOrthogonalVertexNormals, boolean useVertexColoring) {
        StringBuilder stringBuilder = new StringBuilder();
        boolean first = true;
        for (TexTriangle tri : triangles) {
            // only consider triangles with the specific texture id
            if (
                (groupId == null || tri.getTexture().getId() == groupId) &&
                (rgb == null || tri.getTexture().getSampleRGB() == rgb)
            ) {
                if (!first) {
                    stringBuilder.append(" ");
                } else {
                    first = false;
                }
                TexTriUV[] uvs = null;
                if (!useVertexColoring) {
                    uvs = tri.getUVs();
                }
                stringBuilder.append(tri.getPoint(0).getId()).append(" ");
                if (!useVertexColoring) {
                   stringBuilder.append(uvs[0].getId()).append(" ");
                }
                if (exportOrthogonalVertexNormals) {
                    stringBuilder.append(tri.getOrientation()).append(" ");
                }
                stringBuilder.append(tri.getPoint(1).getId()).append(" ");
                if (!useVertexColoring) {
                    stringBuilder.append(uvs[1].getId()).append(" ");
                }
                if (exportOrthogonalVertexNormals) {
                    stringBuilder.append(tri.getOrientation()).append(" ");
                }
                stringBuilder.append(tri.getPoint(2).getId());
                if (!useVertexColoring) {
                    stringBuilder.append(" ").append(uvs[2].getId());
                }
                if (exportOrthogonalVertexNormals) {
                    stringBuilder.append(" ").append(tri.getOrientation());
                }
            }
        }
        return stringBuilder.toString();
    }

    // ----------------

    // list of known uvs
    private final TexTriCornerManager<TexTriUV> uvManager = new TexTriCornerManager<TexTriUV>();

    // add a triangle uv
    protected final void addUV(TexTriUV uv) {
        uvManager.add(uv);
    }

    // invalidate triangle uv ids
    protected final void invalidateUVs() {
        uvManager.invalidate();
    }

    // get the id of a triangle uv
    protected final int getUVId(TexTriUV uv) {
        return uvManager.getId(uv);
    }

    // get string with unique uvs
    public final String getUniqueUVString(boolean asInt) {
        return uvManager.getString(asInt);
    }

    // get the amount of unique uvs
    public final int getUniqueUVCount() {
        return uvManager.getUniqueCount();
    }

    // ----------------

    // list of known points
    private final TexTriCornerManager<TexTriPoint> pointManager = new TexTriCornerManager<TexTriPoint>();

    // add a triangle point
    protected final void addPoint(TexTriPoint point) {
        pointManager.add(point);
    }

    // invalidate triangle point ids
    protected final void invalidatePoints() {
        pointManager.invalidate();
    }

    // get the id of a triangle point
    protected final int getPointId(TexTriPoint point) {
        return pointManager.getId(point);
    }

    // get string with unique points
    public final String getUniquePointString(boolean asInt) {
        return pointManager.getString(asInt);
    }

    // get the amount of unique points
    public final int getUniquePointCount() {
        return pointManager.getUniqueCount();
    }

}
