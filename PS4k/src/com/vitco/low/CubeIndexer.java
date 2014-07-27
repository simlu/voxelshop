package com.vitco.low;

import com.threed.jpct.SimpleVector;
import com.vitco.util.graphic.Util3D;
import com.vitco.util.misc.IntegerTools;

/**
 * Indexes a 1600 ^ 3 cube.
 *
 * Defines mappings X x Y x Z -> Integer and reverse.
 */
public class CubeIndexer {

    // dimension/radius
    public static final short width = 1600; // up to 1600 is fine, because 1625^3 < Integer.MAX_VALUE - Integer.MIN_VALUE
    public static final short radius = 800;
    public static final int widthwidth = width * width;

    // needs to be dividable by width ^ 2 and not smaller than Integer.MIN_VALUE
    public static final int min = - 2145280000;
    private static final int minOffset = radius + min/widthwidth;

    // compute the 1d representation for the position (x right, y up, z into background)
    public static int getId(short[] pos) {
        return min + (pos[0] + radius) + (pos[2] + radius) * width + (pos[1] + radius) * widthwidth;
    }

    // compute the 1d representation for the position (x right, y up, z into background)
    public static int getId(int x, int y, int z) {
        return min + (x + radius) + (z + radius) * width + (y + radius) * widthwidth;
    }

    // get shift value (value that needs to be added to another pos id to
    // shift it by that much into that direction)
    public static int getShiftOperand(int shiftX, int shiftY, int shiftZ) {
        return getId(shiftX,shiftY,shiftZ) + 95999200;
    }

    // compute the 1d representation for the position (x right, y up, z into background)
    public static short[] getPos(int id) {
        short x = (short) (IntegerTools.ifloormod2(id, width) - radius);
        short z = (short) (IntegerTools.ifloordiv2(IntegerTools.ifloormod2(id,widthwidth),width) - radius);
        short y = (short) (IntegerTools.ifloordiv2(id,widthwidth) - minOffset);
        return new short[]{x,y,z};
    }

    // change position depending on orientation (move into direction)
    public static int change(int pos, int orientation) {
        switch (orientation) {
            case 0: return changeX(pos, true);
            case 1: return changeX(pos, false);
            case 2: return changeY(pos, true);
            case 3: return changeY(pos, false);
            case 4: return changeZ(pos, true);
            default: return changeZ(pos, false);
        }
    }

    public static int changeX(int pos, boolean add) {
        return pos + (add ? 1 : -1);
    }
    public static int changeY(int pos, boolean add) {
        return pos + (add ? widthwidth : -widthwidth);
    }
    public static int changeZ(int pos, boolean add) {
        return pos + (add ? width : -width);
    }

    // --------------------

    // declare all triangles of the max box of this indexer
    // Note: this is required to check if a ray hits the bounding box
    private static final int intRadius = radius;
    private static final SimpleVector upperLeftFront=new SimpleVector(-intRadius,-intRadius,-intRadius);
    private static final SimpleVector upperRightFront=new SimpleVector(intRadius,-intRadius,-intRadius);
    private static final SimpleVector lowerLeftFront=new SimpleVector(-intRadius, intRadius,-intRadius);
    private static final SimpleVector lowerRightFront=new SimpleVector(intRadius, intRadius,-intRadius);
    private static final SimpleVector upperLeftBack = new SimpleVector( -intRadius, -intRadius, intRadius);
    private static final SimpleVector upperRightBack = new SimpleVector(intRadius, -intRadius, intRadius);
    private static final SimpleVector lowerLeftBack = new SimpleVector( -intRadius, intRadius, intRadius);
    private static final SimpleVector lowerRightBack = new SimpleVector(intRadius, intRadius, intRadius);
    public static final SimpleVector[][] triangles = new SimpleVector[][]{
            // Front
            new SimpleVector[]{CubeIndexer.upperLeftFront, CubeIndexer.upperRightFront, CubeIndexer.lowerLeftFront}, // xy
            new SimpleVector[]{CubeIndexer.upperRightFront, CubeIndexer.lowerRightFront, CubeIndexer.lowerLeftFront},
            // Back
            new SimpleVector[]{CubeIndexer.upperLeftBack, CubeIndexer.lowerLeftBack, CubeIndexer.upperRightBack},
            new SimpleVector[]{CubeIndexer.upperRightBack, CubeIndexer.lowerLeftBack, CubeIndexer.lowerRightBack},
            // Upper
            new SimpleVector[]{CubeIndexer.upperLeftBack, CubeIndexer.upperRightBack, CubeIndexer.upperLeftFront}, // xz
            new SimpleVector[]{CubeIndexer.upperRightBack, CubeIndexer.upperRightFront, CubeIndexer.upperLeftFront},
            // Lower
            new SimpleVector[]{CubeIndexer.lowerLeftBack, CubeIndexer.lowerLeftFront, CubeIndexer.lowerRightBack},
            new SimpleVector[]{CubeIndexer.lowerRightBack, CubeIndexer.lowerLeftFront, CubeIndexer.lowerRightFront},
            // Left
            new SimpleVector[]{CubeIndexer.upperLeftBack, CubeIndexer.upperLeftFront, CubeIndexer.lowerLeftBack}, // yz
            new SimpleVector[]{CubeIndexer.lowerLeftBack, CubeIndexer.upperLeftFront, CubeIndexer.lowerLeftFront},
            // Right
            new SimpleVector[]{CubeIndexer.upperRightBack, CubeIndexer.lowerRightBack, CubeIndexer.upperRightFront},
            new SimpleVector[]{CubeIndexer.lowerRightBack, CubeIndexer.lowerRightFront, CubeIndexer.upperRightFront}
    };

    // If the origin is outside the limits of the max box this function tries to shift the
    // origin into the max box or returns null if this is not possible
    public static SimpleVector validateRay(SimpleVector origin, SimpleVector dir) {
        if (origin.x < -CubeIndexer.radius || origin.x > CubeIndexer.radius ||
                origin.y < -CubeIndexer.radius || origin.y > CubeIndexer.radius ||
                origin.z < -CubeIndexer.radius || origin.z > CubeIndexer.radius
                ) {
            // move origin into max box of hull manager
            // We do this by checking all triangles of the bounding box until we hit one (or not)
            boolean found = false;
            for (SimpleVector[] triangle : CubeIndexer.triangles) {
                SimpleVector newOrigin = Util3D.rayTriangleIntersects(triangle[0], triangle[1], triangle[2], origin, dir, true);
                if (newOrigin != null) {
                    origin = newOrigin;
                    found = true;
                    break;
                }
            }
            // no scaling possible since the CubeIndexer max box was not hit
            if (!found) {
                return null;
            }
        }
        return origin;
    }
}
