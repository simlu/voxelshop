package com.vitco.util.graphic;

import com.threed.jpct.SimpleVector;
import org.junit.Test;

import java.util.Random;

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

    @Test
    public void rayTriangleIntersects() throws Exception {
        for (int i = 0; i < 10000000; i++) {

            Random rand = new Random(i);
            float x1 = rand.nextInt(51) - 25 + rand.nextFloat();
            float y1 = rand.nextInt(51) - 25 + rand.nextFloat();
            float x2 = rand.nextInt(51) - 25 + rand.nextFloat();
            float y2 = rand.nextInt(51) - 25 + rand.nextFloat();
            float x3 = rand.nextInt(51) - 25 + rand.nextFloat();
            float y3 = rand.nextInt(51) - 25 + rand.nextFloat();

            int px = rand.nextInt(51) - 25;
            int py = rand.nextInt(51) - 25;

            SimpleVector vec1 = new SimpleVector(x1, y1, 0);
            SimpleVector vec2 = new SimpleVector(x2, y2, 0);
            SimpleVector vec3 = new SimpleVector(x3, y3, 0);

            SimpleVector origin = new SimpleVector(px,py,-1);
            SimpleVector dir = new SimpleVector(0, 0, 1).normalize();

            // Note: This might fail in edge cases (i.e. when using integer values)
            boolean resultsEqual = G2DUtil.inTriangle(px, py, x1, y1, x2, y2, x3, y3) == Util3D.rayTriangleIntersects(vec1, vec2, vec3, origin, dir, false);
            if (!resultsEqual) {
                System.out.println(" :: " + i);
                System.out.println(x1 + " " + y1 + " " + x2 + " " + y2 + " " + x3 + " " + y3 + " @ " + px + " " + py);
                System.out.println(G2DUtil.inTriangle(px, py, x1, y1, x2, y2, x3, y3) + " vs " + Util3D.rayTriangleIntersects(vec1, vec2, vec3, origin, dir, false));

//                BufferedImage img = new BufferedImage(51, 51, BufferedImage.TYPE_INT_RGB);
//                Graphics2D g2 = (Graphics2D) img.getGraphics();
//                g2.drawLine(x1 + 25, y1 + 25, x2 + 25, y2 + 25);
//                g2.drawLine(x2 + 25, y2 + 25, x3 + 25, y3 + 25);
//                g2.drawLine(x3 + 25, y3 + 25, x1 + 25, y1 + 25);
//
//                img.setRGB(px + 25, py + 25, Color.RED.getRGB());
//
//                ImageIO.write(img, "png", new File("test" + i + ".png"));
            }

            assert resultsEqual;
        }
    }
}
