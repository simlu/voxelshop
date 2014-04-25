package com.vitco.util.graphic;

import org.junit.Test;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Test the G2DUtil class
 */
public class G2DUtilTest {
    // very basic manual test for line grid intersection
    @Test
    public void getLineGridIntersection() throws Exception {
        int[][] result;

        // test dot
        result = G2DUtil.getLineGridIntersection(10, 10, 10, 10);
        assert result.length == 4;
//        for (int[] res : result) {
//            System.out.println(res[0] + " " + res[1]);
//        }

        // test horizontal line
        result = G2DUtil.getLineGridIntersection(10, 10, 13, 10);
        assert result.length == 6;
//        for (int[] res : result) {
//            System.out.println(res[0] + " " + res[1]);
//        }

        // test vertical line
        result = G2DUtil.getLineGridIntersection(10, 10, 10, 13);
        assert result.length == 6;
//        for (int[] res : result) {
//            System.out.println(res[0] + " " + res[1]);
//        }

        // test general case
        result = G2DUtil.getLineGridIntersection(0, 0, 4, 3);
        assert result.length == 6;
//        for (int[] res : result) {
//            System.out.println(res[0] + " " + res[1]);
//        }

//        for (int x = 0; x < 10; x++) {
//            for (int y = 0; y < 10; y++) {
//                System.out.println(x + " " + y + " " + G2DUtil.getLineGridIntersection(0, 0, x, y).length);
//            }
//        }
    }

    // manual test for the triangle grid intersection that outputs an image
    @Test
    public void getTriangleGridIntersection() throws Exception {

        // define the triangle
        float xf1 = 34.5f;
        float yf1 = 43.5f;
        float xf2 = 8.5f;
        float yf2 = 9.5f;
        float xf3 = 48.5f;
        float yf3 = 19.5f;

        // obtain the intersected points
        int[][] points = G2DUtil.getTriangleGridIntersection(
                (int)(xf1 + 0.5f), (int)(yf1 + 0.5f),
                (int)(xf2 + 0.5f), (int)(yf2 + 0.5f),
                (int)(xf3 + 0.5f), (int)(yf3 + 0.5f)
        );

        // -----------
        // -- draw the image

        int zoom = 50;
        BufferedImage img = new BufferedImage(50, 50, BufferedImage.TYPE_INT_ARGB);

        // print found voxels
        for (int[] p : points) {
            if (p[0] > -1 && p[1] > -1 && p[0] < img.getWidth() && p[1] < img.getHeight()) {
                img.setRGB(p[0], p[1], Color.BLACK.getRGB());
            }
        }

        // enlarge so we can draw the triangle line
        BufferedImage img2 = new BufferedImage(50 * zoom, 50 * zoom, BufferedImage.TYPE_INT_ARGB);
        img2.getGraphics().drawImage(img.getScaledInstance(50 * zoom, 50 * zoom, 0), 0, 0, null);

        Graphics2D g2 = (Graphics2D) img2.getGraphics();

        // draw outlines of the voxels
        g2.setColor(Color.WHITE);
        for (int[] p : points) {
            g2.drawRect( (p[0])*zoom, (p[1])*zoom, zoom, zoom);
        }

        // draw the triangle
        g2.setColor(Color.RED);
        g2.drawLine((int) ((xf1 + 0.5f) * zoom), (int) ((yf1 + 0.5f) * zoom), (int) ((xf2 + 0.5f) * zoom), (int) ((yf2 + 0.5f) * zoom));
        g2.drawLine((int) ((xf3 + 0.5f) * zoom), (int) ((yf3 + 0.5f) * zoom), (int) ((xf2 + 0.5f) * zoom), (int) ((yf2 + 0.5f) * zoom));
        g2.drawLine((int) ((xf1 + 0.5f) * zoom), (int) ((yf1 + 0.5f) * zoom), (int) ((xf3 + 0.5f) * zoom), (int) ((yf3 + 0.5f) * zoom));

        // write the file
        try {
            ImageIO.write(img2, "png", new File("test.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // test that triangulation always runs without error (no validation!)
    @Test
    public void getTriangleGridIntersectionMass() throws Exception {
        Random rand = new Random();
        for (int i = 0; i < 100000; i++) {

            if (i%1000 == 0) {
                System.out.println(":: " + i);
            }

            float xf1 = rand.nextInt(512) - 0.5f;
            float yf1 = rand.nextInt(512) - 0.5f;
            float xf2 = rand.nextInt(512) - 0.5f;
            float yf2 = rand.nextInt(512) - 0.5f;
            float xf3 = rand.nextInt(512) - 0.5f;
            float yf3 = rand.nextInt(512) - 0.5f;

            G2DUtil.getTriangleGridIntersection(
                    (int) (xf1 + 0.5f), (int) (yf1 + 0.5f),
                    (int) (xf2 + 0.5f), (int) (yf2 + 0.5f),
                    (int) (xf3 + 0.5f), (int) (yf3 + 0.5f)
            );
        }
    }

    // helper - check if two line segments intersect (this can even happen if they're parallel)
    private boolean lineIntersectionParallel(float x1, float y1, float x2, float y2,
                                             float x3, float y3, float x4, float y4, boolean includeEndPoints) {
        // check if line intersects
        boolean result = G2DUtil.lineIntersection(x1, y1, x2, y2, x3, y3, x4, y4, includeEndPoints) != null;
        if (!result) { // no intersection
            //System.out.println(x1 + " " + y1 + " " + x2 + " " + y2 + " " + x3 + " " + y3 + " " + x4 + " " + y4);
            // check if lines are parallel
            if (G2DUtil.parallel(x1, y1, x2, y2, x3, y3, x4, y4)) {
                // check if parallel lines share a point
                boolean onLine1 = G2DUtil.onLine(x1, y1, x2, y2, x3, y3) || (x1 == x3 && y1 == y3) || (x2 == x3 && y2 == y3);
                if (onLine1
                        // include end points or overlap point is not end point of other line
                        && (includeEndPoints || (x1 != x3) || (y1 != y3))
                        && (includeEndPoints || (x2 != x3) || (y2 != y3))) {
                    return true;
                }
                boolean onLine2 = G2DUtil.onLine(x1, y1, x2, y2, x4, y4) || (x1 == x4 && y1 == y4) || (x2 == x4 && y2 == y4);
                if (onLine2
                        // include end points or overlap point is not end point of other line
                        && (includeEndPoints || (x1 != x4) || (y1 != y4))
                        && (includeEndPoints || (x2 != x4) || (y2 != y4))) {
                    return true;
                }
                // check if lines are identical (have same start and stop point)
                if (onLine1 && onLine2) {
                    return true;
                }
            }

            // check if this is a "point line" (consisting of just one point - degenerated)
            if (x1 == x2 && y1 == y2) {
                if ((x1 == x3 && y1 == y3) || (x1 == x4 && y1 == y4)) {
                    return true;
                }
            }
        }
        return result;
    }

    // helper - check if line intersects quad (defined by top left point (x,y))
    private boolean intersects(float x1, float y1, float x2, float y2, float x, float y) {
        // return true if
        //  -- line intersects a grid line (not the end point!) or
        //  -- line intersects the end points of all grid lines
        return lineIntersectionParallel(x1, y1, x2, y2, x, y, x + 1, y, false) ||
                lineIntersectionParallel(x1, y1, x2, y2, x, y, x, y + 1, false) ||
                lineIntersectionParallel(x1, y1, x2, y2, x, y + 1, x + 1, y + 1, false) ||
                lineIntersectionParallel(x1, y1, x2, y2, x + 1, y, x + 1, y + 1, false) ||
                (lineIntersectionParallel(x1, y1, x2, y2, x, y, x + 1, y, true) &&
                        lineIntersectionParallel(x1, y1, x2, y2, x, y, x, y + 1, true) &&
                        lineIntersectionParallel(x1, y1, x2, y2, x, y + 1, x + 1, y + 1, true) &&
                        lineIntersectionParallel(x1, y1, x2, y2, x + 1, y, x + 1, y + 1, true));

    }

    // verify the DDA supercover algorithm (for line on grid)
    // this is the integer version where the line starts and ends
    // in between four grid cells
    @Test
    public void getLineGridIntersectionVerify() throws Exception {

        for (int j = 0; j < 1000000; j++) {

            if (j % 1000 == 0) {
                System.out.println(":: " + j);
            }

            // define the line on the grid
            Random rand = new Random(j);
            float xf1 = rand.nextInt(512) - 0.5f;
            float yf1 = rand.nextInt(512) - 0.5f;
            float xf2 = rand.nextInt(512) - 0.5f;
            float yf2 = rand.nextInt(512) - 0.5f;

            //System.out.println(xf1 + " " + yf1 + " " + xf2 + " " + yf2);

            // compute result
            int[][] result = G2DUtil.getLineGridIntersection(
                    (int) (xf1 + 0.5f), (int) (yf1 + 0.5f),
                    (int) (xf2 + 0.5f), (int) (yf2 + 0.5f)
            );

            // sort result
            Arrays.sort(result, new Comparator<int[]>() {
                @Override
                public int compare(int[] o1, int[] o2) {
                    int sign = o1[0] - o2[0];
                    if (sign != 0) {
                        return sign;
                    } else {
                        return o1[1] - o2[1];
                    }
                }
            });

            // compute "save" result by computing for every grid cell if it intersects with the line
            ArrayList<int[]> saveResult = new ArrayList<int[]>();
            for (float x = Math.min(xf1,xf2) - 1; x <= Math.max(xf1,xf2); x++) {
                for (float y = Math.min(yf1,yf2) - 1; y <= Math.max(yf1,yf2); y++) {
                    if (intersects(xf1, yf1, xf2, yf2, x, y)) {
                        saveResult.add(new int[]{(int) (x + 0.5f), (int) (y + 0.5f)});
                    }
                }
            }
            int[][] saveResultArray = new int[saveResult.size()][2];
            saveResult.toArray(saveResultArray);

            // sort save result
            Arrays.sort(saveResultArray, new Comparator<int[]>() {
                @Override
                public int compare(int[] o1, int[] o2) {
                    int sign = o1[0] - o2[0];
                    if (sign != 0) {
                        return sign;
                    } else {
                        return o1[1] - o2[1];
                    }
                }
            });

            // compare results
            assert result.length == saveResultArray.length;

            for (int i = 0; i < result.length; i++) {
                assert result[i][0] == saveResultArray[i][0];
                assert result[i][1] == saveResultArray[i][1];
            }

        }

    }

    // verify the DDA supercover algorithm (for line on grid)
    // this is the precise version where the line starts and ends
    // anywhere on the grid
    @Test
    public void getLineGridIntersectionVerifyDouble() throws Exception {

        for (int j = 0; j < 10000000; j++) {

            if (j % 1000 == 0) {
                System.out.println(":: " + j);
            }

            // indicates the floating point precision that is used for generating
            // the test values and for computing the "save" result
            double scaleFactor;

            // generate the line on the grid
            Random rand = new Random(j);
            double xf1, yf1, xf2, yf2;
            switch (rand.nextInt(4)) {
                case 0:
                    scaleFactor = 100d;
                    xf1 = rand.nextInt(51200) / scaleFactor;
                    yf1 = rand.nextInt(51200) / scaleFactor;
                    xf2 = rand.nextInt(51200) / scaleFactor;
                    yf2 = rand.nextInt(51200) / scaleFactor;
                    break;
                case 1:
                    scaleFactor = 100d;
                    xf1 = rand.nextInt(512) / scaleFactor;
                    yf1 = rand.nextInt(512) / scaleFactor;
                    xf2 = rand.nextInt(512) / scaleFactor;
                    yf2 = rand.nextInt(512) / scaleFactor;
                    break;
                case 2:
                    scaleFactor = 100d;
                    xf1 = (rand.nextInt(512) - 256) / scaleFactor;
                    yf1 = (rand.nextInt(512) - 256) / scaleFactor;
                    xf2 = (rand.nextInt(512) - 256) / scaleFactor;
                    yf2 = (rand.nextInt(512) - 256) / scaleFactor;
                    break;
                default:
                    scaleFactor = 1000d;
                    xf1 = rand.nextInt(5120) / scaleFactor;
                    yf1 = rand.nextInt(5120) / scaleFactor;
                    xf2 = rand.nextInt(5120) / scaleFactor;
                    yf2 = rand.nextInt(5120) / scaleFactor;
                    break;
            }

//            System.out.println("============ " + j);
//            System.out.println((xf1) + " " + (yf1) + " " + (xf2) + " " + (yf2));

            // compute result
            int[][] result = G2DUtil.getLineGridIntersection(
                   xf1, yf1, xf2, yf2
            );

            // sort result
            Arrays.sort(result, new Comparator<int[]>() {
                @Override
                public int compare(int[] o1, int[] o2) {
                    int sign = o1[0] - o2[0];
                    if (sign != 0) {
                        return sign;
                    } else {
                        return o1[1] - o2[1];
                    }
                }
            });

            //System.out.println("=======");

            // compute "save" result
            // we do this by scaling the points and using the integer version
            // the resulting grid cells are then scaled down again
            int[][] saveResult = G2DUtil.getLineGridIntersection(
                    (int)Math.round(xf1 * scaleFactor), (int)Math.round (yf1 * scaleFactor),
                    (int)Math.round (xf2 * scaleFactor), (int)Math.round (yf2 * scaleFactor)
            );
            HashSet<Point> translation = new HashSet<Point>();
            for (int[] cell : saveResult) {
                translation.add(new Point((int)Math.floor(cell[0]/scaleFactor), (int)Math.floor(cell[1]/scaleFactor)));
            }
            int k = 0;
            int[][] saveResultArray = new int[translation.size()][2];
            for (Point p : translation) {
                saveResultArray[k][0] = p.x;
                saveResultArray[k++][1] = p.y;
            }

            // sort save result
            Arrays.sort(saveResultArray, new Comparator<int[]>() {
                @Override
                public int compare(int[] o1, int[] o2) {
                    int sign = o1[0] - o2[0];
                    if (sign != 0) {
                        return sign;
                    } else {
                        return o1[1] - o2[1];
                    }
                }
            });

//            if (result.length != saveResultArray.length) {
//                for (int i = 0; i < Math.max(result.length, saveResultArray.length); i++) {
//                    if (result.length > i && saveResultArray.length > i) {
//                        System.out.println(result[i][0] + "," + result[i][1] + " vs " + saveResultArray[i][0] + "," + saveResultArray[i][1]);
//                    } else if (result.length > i) {
//                        System.out.println(result[i][0] + "," + result[i][1] + " vs " + "...");
//                    } else {
//                        System.out.println("..." + " vs " + saveResultArray[i][0] + "," + saveResultArray[i][1]);
//                    }
//                }
//            }

//            for (int i = 0; i < Math.max(result.length, saveResultArray.length); i++) {
//                if (result.length > i && saveResultArray.length > i) {
//                    if (result[i][0] != saveResultArray[i][0] || result[i][1] != saveResultArray[i][1]) {
//                        System.out.println("@@@@@@@@@@@@");
//                    }
//                    System.out.println(result[i][0] + "," + result[i][1] + " vs " + saveResultArray[i][0] + "," + saveResultArray[i][1]);
//                } else if (result.length > i) {
//                    System.out.println(result[i][0] + "," + result[i][1] + " vs " + "...");
//                } else {
//                    System.out.println("..." + " vs " + saveResultArray[i][0] + "," + saveResultArray[i][1]);
//                }
//            }

            // compare results
            assert result.length == saveResultArray.length;

            for (int i = 0; i < result.length; i++) {
                assert result[i][0] == saveResultArray[i][0];
                assert result[i][1] == saveResultArray[i][1];
            }

//            boolean error = false;
//            int errorCount = 0;
//            int diff = 0;
//            if (result.length != saveResultArray.length) {
//                error = true;
//                diff = Math.abs(result.length - saveResultArray.length);
//            } else {
//                for (int i = 0; i < result.length; i++) {
//                    if (result[i][0] != saveResultArray[i][0] || result[i][1] != saveResultArray[i][1]) {
//                        errorCount++;
//                        error = true;
//                    }
//                }
//            }
//            if (error) {
//                System.out.println((xf1) + " " + (yf1) + " " + (xf2) + " " + (yf2) + " @@ " + errorCount + " diff " + diff);
//            }

        }

    }

    // manual test for line intersection
    @Test
    public void printLineGridIntersection() throws Exception {

        // define the line
        double xf1 = 15.0;
        double yf1 = 0.0;
        double xf2 = 0.0;
        double yf2 = 7.5;

        System.out.println(xf1 + " " + yf1 + " " + xf2 + " " + yf2);

        // how many pixels is the image high and width
        int imgSize = 50;

        // =================
        // obtain the intersected points
        int[][] points = G2DUtil.getLineGridIntersection(
                xf1, yf1, xf2, yf2
        );
        // ===============
//        // compute scaled result with integer values
//        int[][] saveResult = G2DUtil.getLineGridIntersection(
//                Math.round (xf1 * 100), Math.round (yf1 * 100),
//                Math.round (xf2 * 100), Math.round (yf2 * 100)
//        );
//        HashSet<Point> translation = new HashSet<Point>();
//        for (int[] cell : saveResult) {
//            translation.add(new Point(cell[0]/100, cell[1]/100));
//        }
//        int k = 0;
//        int[][] points = new int[translation.size()][2];
//        for (Point p : translation) {
//            points[k][0] = p.x;
//            points[k++][1] = p.y;
//        }
        // ===============


        // -----------
        // -- draw the image

        int zoom = 50;
        BufferedImage img = new BufferedImage(imgSize, imgSize, BufferedImage.TYPE_INT_ARGB);

        // print found voxels
        for (int[] p : points) {
            if (p[0] > -1 && p[1] > -1 && p[0] < img.getWidth() && p[1] < img.getHeight()) {
                img.setRGB(p[0], p[1], Color.BLACK.getRGB());
            }
        }

        // enlarge so we can draw the triangle line
        BufferedImage img2 = new BufferedImage(imgSize * zoom, imgSize * zoom, BufferedImage.TYPE_INT_ARGB);
        img2.getGraphics().drawImage(img.getScaledInstance(imgSize * zoom, imgSize * zoom, 0), 0, 0, null);

        Graphics2D g2 = (Graphics2D) img2.getGraphics();

        // draw outlines of the voxels
        g2.setColor(Color.WHITE);
        for (int[] p : points) {
            g2.drawRect( (p[0])*zoom, (p[1])*zoom, zoom, zoom);
        }

        // draw the triangle
        g2.setColor(Color.RED);
        g2.drawLine((int) ((xf1) * zoom), (int) ((yf1) * zoom), (int) ((xf2) * zoom), (int) ((yf2) * zoom));

        // write the file
        try {
            ImageIO.write(img2, "png", new File("test.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
