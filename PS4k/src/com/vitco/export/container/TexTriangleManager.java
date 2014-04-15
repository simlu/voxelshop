package com.vitco.export.container;

import java.util.ArrayList;

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

    // return how many triangles are stored in the manager
    public final int triangleCount() {
        return triangles.size();
    }

    // ----------------

    // get the grouping logic for the triangles
    // (i.e. "3 3 3 ...")
    public final String getTriangleGrouping() {
        int size = triangles.size();
        if (size > 0) {
            return new String(new char[triangles.size()]).replace("\0", " 3").substring(1);
        } else {
            return "";
        }
    }

    // get the triangle coordinate list
    // (i.e. "[p1_ uv1 p2 uv2 p3 uv3]_tri1 [p1_ uv1 p2 uv2 p3 uv3]_tri2 ...")
    public final String getTrianglePolygonList() {
        StringBuilder stringBuilder = new StringBuilder();
        boolean first = true;
        for (TexTriangle tri : triangles) {
            if (!first) {
                stringBuilder.append(" ");
            } else {
                first = false;
            }
            stringBuilder.append(tri.getPoint(0).getId()).append(" ").append(tri.getUV(0).getId()).append(" ");
            stringBuilder.append(tri.getPoint(1).getId()).append(" ").append(tri.getUV(1).getId()).append(" ");
            stringBuilder.append(tri.getPoint(2).getId()).append(" ").append(tri.getUV(2).getId());
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
