package com.vitco.util;

import com.threed.jpct.SimpleVector;

/**
 * Helper class to perform 3D calculation tasks
 */
public final class Util3D {
    public static SimpleVector nearestPoint(SimpleVector[] line, SimpleVector point) {
        SimpleVector a = new SimpleVector(line[0]);
        a.sub(point);

        SimpleVector b = new SimpleVector(line[1]);
        b.sub(line[0]);

        float top = a.calcDot(b);

        double t = - top / Math.pow(line[1].distance(line[0]),2.0);

        return new SimpleVector(
                line[0].x + (line[1].x - line[0].x) * t,
                line[0].y + (line[1].y - line[0].y) * t,
                line[0].z + (line[1].z - line[0].z) * t
        );
    }

    public static SimpleVector[] nearestLinePoints(SimpleVector[] line1, SimpleVector[] line2) {
        SimpleVector d21 = new SimpleVector(line1[0]);
        d21.sub(line1[1]); // 3 sub x 3
        SimpleVector d34 = new SimpleVector(line2[1]);
        d34.sub(line2[0]);
        SimpleVector d13 = new SimpleVector(line2[0]);
        d13.sub(line1[0]);

        // m * u = x
        float a = d21.calcDot(d21); // (3 mul + 3 add ) x 5
        float b = d21.calcDot(d34);
        float c = d34.calcDot(d34);
        float d = -d13.calcDot(d21);
        float e = -d13.calcDot(d34);

        // Solve for u1 & u2
        float[] u = new float[2];
        u[0] = (d*c-e*b)/(c*a-b*b); // 4 mul, 2 sub, 1 div
        u[1] = (e - b * u[0]) / c; // 1 mul, 1 sub, 1 div

        return new SimpleVector[] {
                new SimpleVector(
                        line1[0].x + (line1[1].x - line1[0].x) * u[0],
                        line1[0].y + (line1[1].y - line1[0].y) * u[0],
                        line1[0].z + (line1[1].z - line1[0].z) * u[0]
                ),
                new SimpleVector(
                        line2[0].x + (line2[1].x - line2[0].x) * u[1],
                        line2[0].y + (line2[1].y - line2[0].y) * u[1],
                        line2[0].z + (line2[1].z - line2[0].z) * u[1]
                )
        };

    }

}
