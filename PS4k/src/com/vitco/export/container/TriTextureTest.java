package com.vitco.export.container;

import com.vitco.util.graphic.G2DUtil;
import org.junit.Test;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Random;

public class TriTextureTest {

    @Test
    public void compressionTest() throws Exception {
        // load image that we want to compress
        BufferedImage input = ImageIO.read(new File("C:\\Users\\flux\\Dropbox\\java\\VoxelShop\\Test Files\\Texture Compression\\sample25.png"));
        // create hashmap with pixels
        HashMap<Point, int[]> pixels = new HashMap<Point, int[]>();
        for (int x = 0, width = input.getWidth(); x < width; x++) {
            for (int y = 0, height = input.getHeight(); y < height; y++) {
                int rgb = input.getRGB(x,y);
                // check that this is not a fully transparent pixel
                if (((rgb >> 24) & 0xff) != 0) {
                    pixels.put(new Point(x,y), new int[]{x, y, rgb});
                }
            }
        }
        // enter uv points
        double[][] uvPoints = new double[][] {
                new double[] {1.0, 0.0} ,
                new double[] {0.0, 1.0},
                new double[] {0.8666666666666667, 0.0}
        };
        // compress input
        int[] newSize = TriTexture.compress(input.getWidth(), input.getHeight(), pixels, uvPoints);
        // alternative don't compress
        //int[] newSize = new int[] {input.getWidth(),input.getHeight()};

        System.out.println(input.getWidth() + " " + input.getHeight());
        System.out.println(newSize[0] + " " + newSize[1]);

        // -- create image
        int imgSize = 20; // grid cells
        int zoom = 50;
        BufferedImage img = new BufferedImage(imgSize, imgSize, BufferedImage.TYPE_INT_ARGB);

        // print found voxels
        for (int[] p : pixels.values()) {
            if (p[0] > -1 && p[1] > -1 && p[0] < img.getWidth() && p[1] < img.getHeight()) {
                img.setRGB(p[0], p[1], p[2]);
            }
        }

        // enlarge so we can draw the triangle line
        BufferedImage img2 = new BufferedImage(imgSize * zoom, imgSize * zoom, BufferedImage.TYPE_INT_ARGB);
        img2.getGraphics().drawImage(img.getScaledInstance(imgSize * zoom, imgSize * zoom, 0), 0, 0, null);

        Graphics2D g2 = (Graphics2D) img2.getGraphics();

        // draw outlines of the voxels
        g2.setColor(Color.WHITE);
        for (int[] p : pixels.values()) {
            g2.drawRect( (p[0])*zoom, (p[1])*zoom, zoom, zoom);
        }

        // draw the triangle
        g2.setColor(Color.RED);
        g2.drawLine(
                (int) ((uvPoints[0][0] * newSize[0]) * zoom), (int) ((uvPoints[0][1] * newSize[1]) * zoom),
                (int) ((uvPoints[1][0] * newSize[0]) * zoom), (int) ((uvPoints[1][1] * newSize[1]) * zoom)
        );
        g2.drawLine(
                (int) ((uvPoints[2][0] * newSize[0]) * zoom), (int) ((uvPoints[2][1] * newSize[1]) * zoom),
                (int) ((uvPoints[1][0] * newSize[0]) * zoom), (int) ((uvPoints[1][1] * newSize[1]) * zoom)
        );
        g2.drawLine(
                (int) ((uvPoints[0][0] * newSize[0]) * zoom), (int) ((uvPoints[0][1] * newSize[1]) * zoom),
                (int) ((uvPoints[2][0] * newSize[0]) * zoom), (int) ((uvPoints[2][1] * newSize[1]) * zoom)
        );

        // write the file
        try {
            ImageIO.write(img2, "png", new File("C:\\Users\\flux\\Dropbox\\java\\VoxelShop\\Test Files\\Texture Compression\\sample25_compressed.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void compressionTestBatch() throws Exception {

        for (int j = 0; j < 10000000; j++) {
            Random rand = new Random(j);

            // print current id
            if (j%10000 == 0) {
                System.out.println(":: " + j);
            }
//            System.out.println(":: " + j);

            // -- generate random triangle
            // size of image
            int[] size = new int[] {rand.nextInt(25)+1, rand.nextInt(25)+1};
            double[][] uvPoints;
            // triangle points
            do {
                uvPoints = new double[][]{
                        new double[]{rand.nextBoolean() ? rand.nextInt(100)/100d : rand.nextInt(2), rand.nextBoolean() ? rand.nextInt(100)/100d : rand.nextInt(2)},
                        new double[]{rand.nextBoolean() ? rand.nextInt(100)/100d : rand.nextInt(2), rand.nextBoolean() ? rand.nextInt(100)/100d : rand.nextInt(2)},
                        new double[]{rand.nextBoolean() ? rand.nextInt(100)/100d : rand.nextInt(2), rand.nextBoolean() ? rand.nextInt(100)/100d : rand.nextInt(2)}
                };
            } while (
                            // points not different
                            (uvPoints[0][0] == uvPoints[1][0] && uvPoints[0][1] == uvPoints[1][1]) ||
                            (uvPoints[0][0] == uvPoints[2][0] && uvPoints[0][1] == uvPoints[2][1]) ||
                            (uvPoints[2][0] == uvPoints[1][0] && uvPoints[2][1] == uvPoints[1][1]) ||
                            // points on one line
                            (uvPoints[0][0] == uvPoints[1][0] && uvPoints[2][0] == uvPoints[1][0]) ||
                            (uvPoints[0][1] == uvPoints[1][1] && uvPoints[2][1] == uvPoints[1][1])
                    );
            // prevent rounding problems
            uvPoints[0][0] = (double)Math.round(uvPoints[0][0] * 1000000000) / 1000000000;
            uvPoints[0][1] = (double)Math.round(uvPoints[0][1] * 1000000000) / 1000000000;
            uvPoints[1][0] = (double)Math.round(uvPoints[1][0] * 1000000000) / 1000000000;
            uvPoints[1][1] = (double)Math.round(uvPoints[1][1] * 1000000000) / 1000000000;
            uvPoints[2][0] = (double)Math.round(uvPoints[2][0] * 1000000000) / 1000000000;
            uvPoints[2][1] = (double)Math.round(uvPoints[2][1] * 1000000000) / 1000000000;
            // ---
            // print generated points
//            System.out.println(uvPoints[0][0] + " " + uvPoints[0][1] + " " + uvPoints[1][0] + " " + uvPoints[1][1] + " " + uvPoints[2][0] + " " + uvPoints[2][1]);
//            System.out.println(uvPoints[0][0] * size[0] + " " + uvPoints[0][1] * size[1] + " " +
//                    uvPoints[1][0] * size[0] + " " + uvPoints[1][1] * size[1] + " " + uvPoints[2][0] * size[0] + " " + uvPoints[2][1] * size[1]);
            // calculate required points
            int[][] points = G2DUtil.getTriangleGridIntersection(
                    uvPoints[0][0] * size[0],  uvPoints[0][1] * size[1],
                    uvPoints[1][0] * size[0],  uvPoints[1][1] * size[1],
                    uvPoints[2][0] * size[0],  uvPoints[2][1] * size[1]
            );
            // generate required points (two colors)
            HashMap<Point, int[]> pixels = new HashMap<Point, int[]>();
            for (int[] point : points) {
                pixels.put(new Point(point[0], point[1]), new int[] {point[0], point[1],
                        rand.nextBoolean() ? Color.RED.getRGB() : Color.BLUE.getRGB()
                });
            }

            // -- compress
            HashMap<Point, int[]> pixelsCompressed = new HashMap<Point, int[]>();
            pixelsCompressed.putAll(pixels);
            double[][] uvPointsCompressed = new double[][] {
                    new double[] {uvPoints[0][0], uvPoints[0][1]},
                    new double[] {uvPoints[1][0], uvPoints[1][1]},
                    new double[] {uvPoints[2][0], uvPoints[2][1]}
            };
            int[] sizeCompressed = TriTexture.compress(size[0], size[1], pixelsCompressed, uvPointsCompressed);

            // -- validate
            // print size
//            System.out.println(" === " + size[0] + " " + size[1] + " vs " + sizeCompressed[0] + " " + sizeCompressed[1]);
            // check that size is correct
            assert sizeCompressed[0] <= size[0];
            assert sizeCompressed[1] <= size[1];
            assert sizeCompressed[0] > 0;
            assert sizeCompressed[1] > 0;
            // validate pixel color for a number of random points
            for (int i = 0; i < 1000; i++) {
                // gives a uniform distribution over the triangle, see here:
                // http://stackoverflow.com/questions/4778147/sample-random-point-in-triangle
                double r1, r2;
                do {
                    // we do not care about triangle corner points
                    // (since we're interpolating later anyway!)
                    // Note: these points might cause trouble since they might be rounded to the next integer value
                    // and that pixel might outside the triangle area
                    r1 = rand.nextDouble();
                    r2 = rand.nextDouble();
                } while (r1 == 1 || r2 == 1 || r1 == 0 || r2 == 0);

                double v1 = ((1 - Math.sqrt(r1)) * uvPoints[0][0] +
                        (Math.sqrt(r1) * (1 - r2)) * uvPoints[1][0] + (Math.sqrt(r1) * r2) * uvPoints[2][0]) * size[0];
                double v2 = ((1 - Math.sqrt(r1)) * uvPoints[0][1] +
                        (Math.sqrt(r1) * (1 - r2)) * uvPoints[1][1] + (Math.sqrt(r1) * r2) * uvPoints[2][1]) * size[1];
                int x = (int)Math.floor(v1);
                int y = (int)Math.floor(v2);
                int xCompressed = (int)Math.floor(((1 - Math.sqrt(r1)) * uvPointsCompressed[0][0] +
                        (Math.sqrt(r1) * (1 - r2)) * uvPointsCompressed[1][0] + (Math.sqrt(r1) * r2) * uvPointsCompressed[2][0]) * sizeCompressed[0]);
                int yCompressed = (int)Math.floor(((1 - Math.sqrt(r1)) * uvPointsCompressed[0][1] +
                        (Math.sqrt(r1) * (1 - r2)) * uvPointsCompressed[1][1] + (Math.sqrt(r1) * r2) * uvPointsCompressed[2][1]) * sizeCompressed[1]);
                x = Math.max(0, Math.min(size[0] - 1, x));
                y = Math.max(0, Math.min(size[1] - 1, y));
                xCompressed = Math.max(0, Math.min(sizeCompressed[0] - 1, xCompressed));
                yCompressed = Math.max(0, Math.min(sizeCompressed[1] - 1, yCompressed));

                // print points that are checked
//                System.out.println(x + " " + y + " vs " + xCompressed + " " + yCompressed);
                // check that the color values match
                assert pixels.get(new Point(x, y))[2] == pixelsCompressed.get(new Point(xCompressed, yCompressed))[2];
            }

        }
    }
}