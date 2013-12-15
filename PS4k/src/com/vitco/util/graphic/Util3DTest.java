package com.vitco.util.graphic;

import com.threed.jpct.SimpleVector;
import org.junit.Test;

/**
 * Test for Util3D
 */
public class Util3DTest {
    @Test
    public void testNearestPoint() throws Exception {
        // todo verify
        SimpleVector result = Util3D.nearestPoint(
                new SimpleVector[]{
                        new SimpleVector(0, 0, 0),
                        new SimpleVector(5, 0, 0)},
                new SimpleVector(1,1,0)
        );
        System.out.println(result.x + ", " + result.y + ", " + result.z);
    }

    @Test
    public void testNearestLinePoints() throws Exception {
        // todo verify
        SimpleVector[] result = Util3D.nearestLinePoints(
                new SimpleVector[]{
                        new SimpleVector(-1, 0, 1),
                        new SimpleVector(1, 0, 1)},
                new SimpleVector[]{
                        new SimpleVector(1, 1, 0),
                        new SimpleVector(1, -1, 0)}
        );
        System.out.println(result[0].x + ", " + result[0].y + ", " + result[0].z);
        System.out.println(result[1].x + ", " + result[1].y + ", " + result[1].z);
    }
}
