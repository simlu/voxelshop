package com.vitco.core.world.container;

import com.threed.jpct.Object3D;
import com.threed.jpct.PolygonManager;
import com.threed.jpct.SimpleVector;
import com.threed.jpct.TextureInfo;
import com.vitco.core.data.container.Voxel;
import com.vitco.low.hull.HullManager;
import com.vitco.settings.VitcoSettings;
import com.vitco.util.misc.ConversionTools;
import org.poly2tri.triangulation.delaunay.DelaunayTriangle;
import org.poly2tri.triangulation.point.TPoint;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

/**
 * A special Object3D plane that has a border that can be enabled/disabled.
 */
public class BorderObject3D extends Object3D {
    private static final long serialVersionUID = 1L;

    // get the rounded points of a triangle as an int array
    private static int[] getRoundedPoints(DelaunayTriangle triangle) {
        // needed since the triangulation can result in non integers
        return new int[] {
                Math.round(triangle.points[0].getXf()),
                Math.round(triangle.points[0].getYf()),
                Math.round(triangle.points[1].getXf()),
                Math.round(triangle.points[1].getYf()),
                Math.round(triangle.points[2].getXf()),
                Math.round(triangle.points[2].getYf())
        };
    }

    // get the center direction for a triangle described by points and a center
    // true indicates that the point axis is greater than the center
    private static boolean[] getCenterDirection(int[] trianglePoints, float centerX, float centerY) {
        boolean[] centerDir = new boolean[6];
        // check two at a time (x and y)
        for (int i = 0; i < 6; i+=2) {
            centerDir[i] = (trianglePoints[i] - centerX > 0);
            centerDir[i+1] = (trianglePoints[i+1] - centerY > 0);
        }
        return centerDir;
    }

    // returns the outside directions for those points that are "outside"
    private static byte[] getOutsideDirections(
            HashSet<String> seenTrianglePoints, DelaunayTriangle triangle, int[] roundedTrianglePoints) {

        TPoint triangle_center = triangle.centroid();
        boolean[] centerDirection =
                getCenterDirection(roundedTrianglePoints, triangle_center.getXf(), triangle_center.getYf());

        byte[] outsideDirection = new byte[6];

        for (int i = 0; i < 3; i++) {
            int x = i*2;
            int y = x+1;

            // how the check is done
            // [o] [] | [] [o] | [] []  | [] []
            //    X   |   X    |   X    |   X
            //  [] [] | [] []  | [o] [] | [] [o]
            if (!seenTrianglePoints.contains((roundedTrianglePoints[x] - 1) + "_" + (roundedTrianglePoints[y] - 1))) {
                outsideDirection[x] -= 1;
                outsideDirection[y] -= 1;
            }
            if (!seenTrianglePoints.contains((roundedTrianglePoints[x]) + "_" + (roundedTrianglePoints[y] - 1))) {
                outsideDirection[x] += 1;
                outsideDirection[y] -= 1;
            }
            if (!seenTrianglePoints.contains((roundedTrianglePoints[x] - 1) + "_" + (roundedTrianglePoints[y]))) {
                outsideDirection[x] -= 1;
                outsideDirection[y] += 1;
            }
            if (!seenTrianglePoints.contains((roundedTrianglePoints[x]) + "_" + (roundedTrianglePoints[y]))) {
                outsideDirection[x] += 1;
                outsideDirection[y] += 1;
            }

            // also adjust for center of the triangle
            // Note: boolean center direction will result in less noticeable edges
            // (opposed to having or rounded values)
            outsideDirection[x] += centerDirection[x]?1:-1;
            outsideDirection[y] += centerDirection[y]?1:-1;

            // normalize
            outsideDirection[x] = (byte) Math.signum(outsideDirection[x]);
            outsideDirection[y] = (byte) Math.signum(outsideDirection[y]);

        }

        return outsideDirection;
    }

    // the object that is used to refresh the texture of this 3D Object
    private TextureObject textureObject;

    // refresh the texture of this object
    public final void refreshTextureInterpolation() {
        assert textureObject != null;
        textureObject.refreshTexture(null, this);
    }

    // called when this object is no longer needed
    public final void freeTexture() {
        assert textureObject != null;
        textureObject.destroy();
    }

    // generate final interpolated points (to prevent see-through)
    private static SimpleVector[] getTrianglePointsInterpolated(Integer axis, Integer plane,
                                                                float move, int[] roundedTrianglePoints,
                                                                int minx, int miny, byte[] outsideDirection) {
        SimpleVector[] triangle = new SimpleVector[3];
        for (int i = 0; i < 3; i ++) {
            int x = i*2;
            int y = x+1;

            // only interpolates if this is a corner
            float interpolationX = -outsideDirection[x]*VitcoSettings.TRIANGLE_INTERPOLATION_VALUE;
            float interpolationY = -outsideDirection[y]*VitcoSettings.TRIANGLE_INTERPOLATION_VALUE;

            // generate the point
            triangle[i] = new SimpleVector(
                    (axis == 0 ? plane + move : roundedTrianglePoints[x] + minx - 0.5f - interpolationX) * VitcoSettings.VOXEL_SIZE,
                    (axis == 1 ? plane + move : (axis == 0 ? roundedTrianglePoints[x] + minx - 0.5f - interpolationX
                            : roundedTrianglePoints[y] + miny - 0.5f - interpolationY)) * VitcoSettings.VOXEL_SIZE,
                    (axis == 2 ? plane + move : roundedTrianglePoints[y] + miny - 0.5f - interpolationY) * VitcoSettings.VOXEL_SIZE
            );
        }
        return triangle;
    }

    // generate final interpolated points (to prevent see-through)
    private static SimpleVector[] getTrianglePoints(Integer axis, Integer plane,
                                                    float move, int[] roundedTrianglePoints,
                                                    int minx, int miny) {
        SimpleVector[] triangle = new SimpleVector[3];
        for (int i = 0; i < 3; i ++) {
            int x = i*2;
            int y = x+1;

            // generate the point
            triangle[i] = new SimpleVector(
                    (axis == 0 ? plane + move : roundedTrianglePoints[x] + minx - 0.5f) * VitcoSettings.VOXEL_SIZE,
                    (axis == 1 ? plane + move : (axis == 0 ? roundedTrianglePoints[x] + minx - 0.5f
                            : roundedTrianglePoints[y] + miny - 0.5f)) * VitcoSettings.VOXEL_SIZE,
                    (axis == 2 ? plane + move : roundedTrianglePoints[y] + miny - 0.5f) * VitcoSettings.VOXEL_SIZE
            );
        }
        return triangle;
    }

    // generate triangles and use adjustable edge interpolation
    private void generateAdvanced(ArrayList<DelaunayTriangle> triangleList, Collection<Voxel> faceList,
                                  int minx, int miny, int w, int h, Integer orientation, Integer axis,
                                  Integer plane, int side, HullManager<Voxel> hullManager) {

        canHaveBorder = side == -1;

        textureSizeX = ConversionTools.getTextureSize(w);
        textureSizeY = ConversionTools.getTextureSize(h);

        // todo: mirror textures if w > h to reduce used memory (!)

        // contains seen pixel
        HashSet<String> seenTrianglePoints = new HashSet<String>();
        // generate textureObject
        textureObject = new TextureObject(
                minx, miny, faceList, orientation,
                axis, hullManager, w, h, textureSizeX, textureSizeY
        );

        // generate the texture (and remember seen triangle points)
        textureObject.refreshTexture(seenTrianglePoints, this);

        // the texture id
        int textureId = textureObject.getTextureId();

        // compute values for inversion
        int sx0 = 0;
        int sx1 = 1;
        int x0 = 0;
        int y0 = 1;
        int x1 = 2;
        int y1 = 3;
        if (inverted) {
            sx0 = 1;
            sx1 = 0;
            x0 = 2;
            y0 = 3;
            x1 = 0;
            y1 = 1;
        }

        float move = orientation%2 == 0 ? 0.5f : -0.5f;

        // iterate over triangles
        for (DelaunayTriangle tri : triangleList) {
            // get the rounded points
            int[] roundedTrianglePoints = getRoundedPoints(tri);
            // get the outside direction
            byte[] outside_direction = getOutsideDirections(seenTrianglePoints, tri, roundedTrianglePoints);
            // the the interpolated points
            SimpleVector[] interpTrianglePoints = getTrianglePointsInterpolated(axis, plane, move, roundedTrianglePoints, minx, miny, outside_direction);
            // get the appropriate interpolation for the texture
            float textureInterpolation = canHaveBorder && hasBorder
                    ? VitcoSettings.BORDER_INSET_VALUE
                    : -VitcoSettings.TEXTURE_INTERPOLATION_VALUE;
            // add the triangle to this object
            this.addTriangle(
                    interpTrianglePoints[sx0],
                    (roundedTrianglePoints[x0] + outside_direction[x0]*textureInterpolation + 1)/textureSizeX,
                    (roundedTrianglePoints[y0] + outside_direction[y0]*textureInterpolation + 1)/textureSizeY,
                    interpTrianglePoints[sx1],
                    (roundedTrianglePoints[x1] + outside_direction[x1]*textureInterpolation + 1)/textureSizeX,
                    (roundedTrianglePoints[y1] + outside_direction[y1]*textureInterpolation + 1)/textureSizeY,
                    interpTrianglePoints[2],
                    (roundedTrianglePoints[4] + outside_direction[4]*textureInterpolation + 1)/textureSizeX,
                    (roundedTrianglePoints[5] + outside_direction[5]*textureInterpolation + 1)/textureSizeY,
                    textureId
            );

            if (canHaveBorder) {
                // memorize the uv positions
                uvPositions.add(new float[] {
                        roundedTrianglePoints[x0] + 1, outside_direction[x0],
                        roundedTrianglePoints[y0] + 1, outside_direction[y0],
                        roundedTrianglePoints[x1] + 1, outside_direction[x1],
                        roundedTrianglePoints[y1] + 1, outside_direction[y1],
                        roundedTrianglePoints[4] + 1, outside_direction[4],
                        roundedTrianglePoints[5] + 1, outside_direction[5]
                });
            }
        }

        // set the additional color
        this.setAdditionalColor(Color.WHITE);

    }

    // generate triangles without any interpolation
    private void generateSimple(ArrayList<DelaunayTriangle> triangleList, int minx, int miny,
                                Integer orientation, Integer axis, Integer plane) {
        float move = orientation%2 == 0 ? 0.5f : -0.5f;
        for (DelaunayTriangle triangle : triangleList) {
            int[] roundedTrianglePoints = getRoundedPoints(triangle);
            SimpleVector[] simpleVectors = getTrianglePoints(axis, plane, move, roundedTrianglePoints, minx, miny);
            this.addTriangle(simpleVectors[inverted?1:0],simpleVectors[inverted?0:1],simpleVectors[2]);
        }
    }

    // ------------------------------------

    // true if this plane needs to be inverted for culling)
    private final boolean inverted;
    // true if this plane can have a border
    private boolean canHaveBorder = false;
    // true if this plane has a border
    private boolean hasBorder = true;
    // contains the textureSize for the object and textureSize/32 for objects
    // that contain images in their textures
    private int textureSizeX = 0;
    private int textureSizeY = 0;
    // the uv positions of the triangles of this object and the interpolation directions
    private final ArrayList<float[]> uvPositions = new ArrayList<float[]>();
    // the polygon manager of this object
    private final transient PolygonManager polygonManager = this.getPolygonManager();

    // set the border state of this object (black outline of the edges)
    // IMPORTANT: This only works because we're using the software renderer(!!!)
    public final void setBorder(boolean border) {
        if (canHaveBorder && hasBorder != border) {
            hasBorder = border;
            float textureInterpolation = hasBorder
                    ? VitcoSettings.BORDER_INSET_VALUE
                    : -VitcoSettings.TEXTURE_INTERPOLATION_VALUE;
            int textureId = textureObject.getTextureId();
            for (int i = 0, size = uvPositions.size(); i < size; i++) {
                float[] uvInfo = uvPositions.get(i);
                polygonManager.setPolygonTexture(i, new TextureInfo(textureId,
                        (uvInfo[0] + uvInfo[1]*textureInterpolation)/textureSizeX,
                        (uvInfo[2] + uvInfo[3]*textureInterpolation)/textureSizeY,
                        (uvInfo[4] + uvInfo[5]*textureInterpolation)/textureSizeX,
                        (uvInfo[6] + uvInfo[7]*textureInterpolation)/textureSizeY,
                        (uvInfo[8] + uvInfo[9]*textureInterpolation)/textureSizeX,
                        (uvInfo[10] + uvInfo[11]*textureInterpolation)/textureSizeY
                ));
            }
        }
    }

    // ------------------------------------

    // constructor
    public BorderObject3D(ArrayList<DelaunayTriangle> tris, Collection<Voxel> faceList,
                          int minx, int miny, int w, int h, Integer orientation, Integer axis,
                          Integer plane, boolean simpleMode, int side,
                          boolean culling, boolean hasBorder, HullManager<Voxel> hullManager) {
        // construct this object 3D with correct triangle count
        super(tris.size());

        // compute inversion and store border state
        this.inverted = orientation%2 == (orientation/2 == 1 ? 0 : 1);
        this.hasBorder = hasBorder;

        if (simpleMode) {
            // generate triangles without any interpolation
            generateSimple(tris, minx, miny, orientation, axis, plane);
        } else {
            // generate triangles and use adjustable edge interpolation
            generateAdvanced(tris, faceList, minx, miny, w, h, orientation, axis,
                    plane, side, hullManager);
        }

        // set the correct culling
        this.setCulling(culling);
        // enable collision checking (needed for both - simpleMode or not)
        this.setCollisionMode(Object3D.COLLISION_CHECK_OTHERS);
        this.setCollisionOptimization(Object3D.COLLISION_DETECTION_OPTIMIZED);

        // shift to zero if this is a side view (for true orthogonal view)
        this.calcCenter();
        this.translate(
                side == 2 ? -this.getCenter().x : 0,
                side == 1 ? -this.getCenter().y : 0,
                side == 0 ? -this.getCenter().z : 0
        );

        // not really needed since we're using textures for everything
        //this.setShadingMode(Object3D.SHADING_FAKED_FLAT);

        // build this object
        this.build();
    }
}
