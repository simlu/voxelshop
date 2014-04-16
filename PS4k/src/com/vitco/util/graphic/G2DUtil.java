package com.vitco.util.graphic;

import com.threed.jpct.SimpleVector;

import java.awt.*;
import java.util.ArrayList;

/**
 * Helps with basic drawing tasks for Graphic2D objects.
 */
public class G2DUtil {
    // draw a point with a border
    public static void drawPoint(SimpleVector point,
                                 Graphics2D ig, Color innerColor, Color outerColor,
                                 float radius, float borderSize) {
        // set outer line size
        ig.setStroke(new BasicStroke(borderSize, BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL));
        ig.setColor(innerColor);
        int rad2times = Math.round(radius * 2);
        ig.fillOval(Math.round(point.x - radius), Math.round(point.y - radius), rad2times, rad2times);
        ig.setColor(outerColor);
        ig.drawOval(Math.round(point.x - radius), Math.round(point.y - radius), rad2times, rad2times);
    }

    // draw a line with an outline
    public static void drawLine(SimpleVector p1, SimpleVector p2,
                                Graphics2D ig, Color innerColor, Color outerColor,
                                float size) {
        if (p1 != null && p2 != null) {
            // outer line
            ig.setColor(outerColor); // line color
            ig.setStroke(new BasicStroke(size * 1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL)); // line size
            ig.drawLine(Math.round(p1.x), Math.round(p1.y),
                    Math.round(p2.x), Math.round(p2.y));
            // inner line
            ig.setColor(innerColor); // line color
            ig.setStroke(new BasicStroke(size, BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL)); // line size
            ig.drawLine(Math.round(p1.x), Math.round(p1.y),
                    Math.round(p2.x), Math.round(p2.y));
        }
    }

    // return intersection of two line segments (or null if none exists)
    public static float[] lineIntersection(
            float x1, float y1, float x2, float y2,
            float x3, float y3, float x4, float y4,
            boolean includeEndPoints
    ) {
        float s1_x = x2 - x1;
        float s1_y = y2 - y1;
        float s2_x = x4 - x3;
        float s2_y = y4 - y3;

        float s = (-s1_y * (x1 - x3) + s1_x * (y1 - y3)) / (-s2_x * s1_y + s1_x * s2_y);
        float t = (s2_x * (y1 - y3) - s2_y * (x1 - x3)) / (-s2_x * s1_y + s1_x * s2_y);

        if (includeEndPoints) {
            if (s >= 0 && s <= 1 && t >= 0 && t <= 1) {
                // intersection found
                return new float[]{x1 + (t * s1_x), y1 + (t * s1_y)};
            }
        } else {
            if (s > 0 && s < 1 && t > 0 && t < 1) {
                // intersection found
                return new float[]{x1 + (t * s1_x), y1 + (t * s1_y)};
            }
        }

        return null;
    }

    // return intersection of two line segments (or null if none exists)
    // integer variant
    public static float[] lineIntersection(
            int x1, int y1, int x2, int y2,
            int x3, int y3, int x4, int y4,
            boolean includeEndPoints
    ) {
        int s1_x = x2 - x1;
        int s1_y = y2 - y1;
        int s2_x = x4 - x3;
        int s2_y = y4 - y3;

        float s = (-s1_y * (x1 - x3) + s1_x * (y1 - y3)) / (float)(-s2_x * s1_y + s1_x * s2_y);
        float t = (s2_x * (y1 - y3) - s2_y * (x1 - x3)) / (float)(-s2_x * s1_y + s1_x * s2_y);

        if (includeEndPoints) {
            if (s >= 0 && s <= 1 && t >= 0 && t <= 1) {
                // intersection found
                return new float[]{x1 + (t * s1_x), y1 + (t * s1_y)};
            }
        } else {
            if (s > 0 && s < 1 && t > 0 && t < 1) {
                // intersection found
                return new float[]{x1 + (t * s1_x), y1 + (t * s1_y)};
            }
        }

        return null;
    }

    // test if a point is in between two other points (uses floating point "accuracy")
    public static boolean onLine(double ax, double ay, double bx, double by, double cx, double cy) {
        return ((float)((bx - ax) * (cy - ay)) == (float)((cx - ax) * (by - ay))) && // on one line
                ((ax < cx == cx < bx) && (ay < cy == cy < by)); // in between on that line
    }

    // test if two lines are parallel (uses floating point accuracy)
    public static boolean parallel(float x1, float y1, float x2, float y2,
                                   float x3, float y3, float x4, float y4) {
        double d1 = x2 - x1;
        double d2 = x4 - x3;
        if (d1 == 0 || d2 == 0) {
            return d1 == d2;
        }
        return (float)(((y2 - y1)/d1)) == (float)(((y4 - y3)/d2));
    }

    // test if point is in triangle
    public static boolean inTriangle(float px, float py,
                                          float p0x, float p0y, float p1x, float p1y, float p2x, float p2y) {
        float s = p0y * p2x - p0x * p2y + (p2y - p0y) * px + (p0x - p2x) * py;
        float t = p0x * p1y - p0y * p1x + (p0y - p1y) * px + (p1x - p0x) * py;

        if ((s < 0) != (t < 0))
            return false;

        float A = -p1y * p2x + p0y * (p2x - p1x) + p0x * (p1y - p2y) + p1x * p2y;
        if (A < 0) {
            s = -s;
            t = -t;
            A = -A;
        }
        return s > 0 && t > 0 && (s + t) < A;
    }

    // get the intersection of a line with a grid
    // assuming that the line starts and ends between four grid corners
    // Note: The line coordinates are given as (x1+0.5f, y1+0.5f, x2+0.5f, y2+0.5f);
    // adapted from: http://www.cse.yorku.ca/~amana/research/grid.pdf
    public static int[][] getLineGridIntersection(int x1, int y1, int x2, int y2) {
        // -- case: dot
        if (y1 == y2 && x1 == x2) {
            // return all four grid cells surrounding this dot
            int[][] result = new int[4][2];
            result[0][0] = x1;
            result[0][1] = y1;
            result[1][0] = x1 - 1;
            result[1][1] = y1;
            result[2][0] = x1;
            result[2][1] = y1 - 1;
            result[3][0] = x1 - 1;
            result[3][1] = y1 - 1;
            return result;
        }

        // -- case: horizontal line
        if (y1 == y2) {
            int[][] result = new int[Math.abs(x2-x1)*2][2];
            int i = 0;
            for (int x = Math.min(x1,x2), max = Math.max(x1, x2); x < max; x++) {
                result[i][0] = x;
                result[i++][1] = y1;
                result[i][0] = x;
                result[i++][1] = y1 - 1;
            }
            return result;
        }

        // -- case: vertical line
        if (x1 == x2) {
            int[][] result = new int[Math.abs(y2-y1)*2][2];
            int i = 0;
            for (int y = Math.min(y1,y2), max = Math.max(y1, y2); y < max; y++) {
                result[i][0] = x1;
                result[i++][1] = y;
                result[i][0] = x1 - 1;
                result[i++][1] = y;
            }
            return result;
        }

        // -- case: general
        // result array list
        ArrayList<int[]> result = new ArrayList<int[]>();

        // step direction
        int stepX = (x2 > x1) ? 1 : -1;
        int stepY = (y2 > y1) ? 1 : -1;

        // starting grid coordinates
        int x = x1 + (stepX == 1 ? 0 : -1);
        int y = y1 + (stepY == 1 ? 0 : -1);

        // stop grid coordinates
        int stopX = x2 + (stepX == 1 ? -1 : 0);
        int stopY = y2 + (stepY == 1 ? -1 : 0);

        // the "progress" value
        double val = Math.abs((y2 - y1) / (double)(x2 - x1));

        int tMaxX = 1;
        int tMaxY = 1;

        // add the initial point
        result.add(new int[] {x, y});

        while (x != stopX || y != stopY) {

            float cTMaxX = (float)(val * tMaxX);

            if (cTMaxX == tMaxY) {
                tMaxX++;
                tMaxY++;
                x = x + stepX;
                y = y + stepY;
            } else if (cTMaxX < tMaxY) {
                tMaxX++;
                x = x + stepX;
            } else {
                tMaxY++;
                y = y + stepY;
            }

            result.add(new int[] {x, y});
        }

        // convert result
        int[][] resultArray = new int[result.size()][2];
        result.toArray(resultArray);

        return resultArray;
    }

    // get the interior grid points of a triangle
    // assuming that the triangle points are between four grid corners
    // Note: The point coordinates are given as (x1+0.5f, y1+0.5f);
    public static int[][] getTriangleGridIntersection(int x1, int y1, int x2, int y2, int x3, int y3) {
        int minX = Math.min(Math.min(x1, x2), x3);
        int maxX = Math.max(Math.max(x1, x2), x3);
        int minY = Math.min(Math.min(y1, y2), y3);
        int maxY = Math.max(Math.max(y1, y2), y3);

        // initialize range
        int[][] range = new int[maxY - minY][2];
        for (int i = 0; i < range.length; i++) {
            range[i][0] = Integer.MAX_VALUE;
            range[i][1] = Integer.MIN_VALUE;
        }

        // get the line points
        int[][] p1 = getLineGridIntersection(x1, y1, x2, y2);
        int[][] p2 = getLineGridIntersection(x1, y1, x3, y3);
        int[][] p3 = getLineGridIntersection(x2, y2, x3, y3);

        // fill in range
        for (int[] p : p1) {
            int val = p[1]-minY;
            if (val > -1 && val < range.length) {
                int[] rangeEntry = range[val];
                rangeEntry[0] = Math.min(rangeEntry[0], p[0]);
                rangeEntry[1] = Math.max(rangeEntry[1], p[0]);
            }
        }
        for (int[] p : p2) {
            int val = p[1]-minY;
            if (val > -1 && val < range.length) {
                int[] rangeEntry = range[val];
                rangeEntry[0] = Math.min(rangeEntry[0], p[0]);
                rangeEntry[1] = Math.max(rangeEntry[1], p[0]);
            }
        }
        for (int[] p : p3) {
            int val = p[1]-minY;
            if (val > -1 && val < range.length) {
                int[] rangeEntry = range[val];
                rangeEntry[0] = Math.min(rangeEntry[0], p[0]);
                rangeEntry[1] = Math.max(rangeEntry[1], p[0]);
            }
        }

        // result array list
        ArrayList<int[]> result = new ArrayList<int[]>();

        // compute interior voxels
        for (int i = 0; i < range.length; i++) {
            int[] row = range[i];
            for (int x = Math.max(minX, row[0]),
                         lenX = Math.min(row[1] + 1, maxX); x < lenX; x++) {
                result.add(new int[] {
                        x, i + minY
                });
            }
        }

        // convert result
        int[][] resultArray = new int[result.size()][2];
        result.toArray(resultArray);

        return resultArray;

    }

}
