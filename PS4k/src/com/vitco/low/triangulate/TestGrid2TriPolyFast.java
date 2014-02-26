package com.vitco.low.triangulate;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.impl.CoordinateArraySequence;
import org.jaitools.imageutils.ImageUtils;
import org.junit.Test;
import org.poly2tri.triangulation.delaunay.DelaunayTriangle;

import javax.imageio.ImageIO;
import javax.media.jai.TiledImage;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Random;

/**
 * - Testing Poly2Tri and in particular new conversion "voxel -> polygon" by doing in depth validation of created geometry.
 *
 * - Manual testing internal implementation against the external implementation of "voxel -> polygon".
 *
 */
public class TestGrid2TriPolyFast {

    // helper - true if point c is in between point a and b
    private static boolean inBetween(Point a, Point b, Point c) {
        return (!a.equals(c) && !b.equals(c)) && // not the same points
                ((b.x - a.x) * (c.y - a.y) == (c.x - a.x) * (b.y - a.y)) && // on one line
                ((a.x < c.x == c.x < b.x) && (a.y < c.y == c.y < b.y)); // in between on that line
    }

    // test the Poly2Tri algorithm with the new algorithm that creates the polygon from
    // the voxel data and verify created geometry
    @Test
    public void testPolyTriangulation() {
        for (int i = 178; i < 20000; i++) {
            Random rand = new Random(i);
            // create image
            int sizex = rand.nextInt(100)+5;
            int sizey = rand.nextInt(100)+5;
            TiledImage src = ImageUtils.createConstantImage(sizex, sizey, 0);

            boolean[][] data = new boolean[sizex][sizey];

            // fill with random data
            int count = rand.nextInt(sizex * sizey * 2);
            for (int j = 0; j < count; j++) {
                int x = rand.nextInt(sizex);
                int y = rand.nextInt(sizey);
                src.setSample(x,y , 0, 1);
                data[x][y] = true;
            }

//            // save image (for checking)
//            BufferedImage bufferedImage = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_INT_RGB);
//            for (int x = 0; x < src.getWidth(); x++) {
//                for (int y = 0; y < src.getHeight(); y++) {
//                    bufferedImage.setRGB(x, y, src.getSample(x, y, 0) == 1 ? Color.BLACK.getRGB() : Color.WHITE.getRGB());
//                }
//            }
//            File outputfile = new File("image" + i + ".png");
//            try {
//                ImageIO.write(bufferedImage, "png", outputfile);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }


            // print information
            System.out.print("Test " + i + " @ " + sizex + " x " + sizey + " :: ");
            Collection<Polygon> geometry = Grid2TriPolySlow.doVectorize(src);
            System.out.print(geometry.size());
            System.out.print(" :: ");
            ArrayList<DelaunayTriangle> tris = Grid2TriPolyFast.triangulate(Grid2TriPolyFast.convert(data));

            System.out.print(tris.size());

            // check that no points are overlapping
            HashSet<Point> points = new HashSet<Point>();
            for (DelaunayTriangle tri : tris) {
                points.add(new Point((int)Math.round(tri.points[0].getX()),(int)Math.round(tri.points[0].getY())));
                points.add(new Point((int)Math.round(tri.points[1].getX()),(int)Math.round(tri.points[1].getY())));
                points.add(new Point((int)Math.round(tri.points[2].getX()),(int)Math.round(tri.points[2].getY())));
            }
            for (Point p : points) {
                for (DelaunayTriangle tri : tris) {
                    //System.out.println(p.toString() + " " + tri.points[0] + " " + tri.points[1] + " " + tri.points[2]);
                    assert !inBetween(new Point((int)Math.round(tri.points[0].getX()),(int)Math.round(tri.points[0].getY())),
                            new Point((int)Math.round(tri.points[1].getX()),(int)Math.round(tri.points[1].getY())), p);
                    assert !inBetween(new Point((int)Math.round(tri.points[0].getX()),(int)Math.round(tri.points[0].getY())),
                            new Point((int)Math.round(tri.points[2].getX()),(int)Math.round(tri.points[2].getY())), p);
                    assert !inBetween(new Point((int)Math.round(tri.points[1].getX()),(int)Math.round(tri.points[1].getY())),
                            new Point((int)Math.round(tri.points[2].getX()),(int)Math.round(tri.points[2].getY())), p);
                }
            }

            // variables
            GeometryFactory geometryFactory = new GeometryFactory();

            // stores the area sum of all triangles
            double aTri = 0;


            int statusCount = 0;
            for (DelaunayTriangle tri: tris) {
                // handle triangle area
                double area = tri.area();
                aTri += area;
                assert area > 0.25;

                // print info
                if (statusCount%((tris.size()/100)+1) == 0) {
                    System.out.print(".");
                }
                statusCount++;

                // convert into geometry
                LinearRing ring = new LinearRing(new CoordinateArraySequence(
                        new Coordinate[]{
                                new Coordinate(tri.points[0].getX(), tri.points[0].getY()),
                                new Coordinate(tri.points[1].getX(), tri.points[1].getY()),
                                new Coordinate(tri.points[2].getX(), tri.points[2].getY()),
                                new Coordinate(tri.points[0].getX(), tri.points[0].getY())
                        }
                ), geometryFactory);
                Polygon triPoly = new Polygon(ring, new LinearRing[0], geometryFactory);
                // check that points are different (area exists)
                assert triPoly.getArea() > 0.25;

                // check containment
                boolean contain = false;
                for (Polygon poly : geometry) {
                    // check for containment
                    if (poly.intersects(triPoly)) {
                        if (poly.contains(triPoly) || (poly.intersection(triPoly).getArea() - triPoly.getArea() < 0.00001)) {
                            contain = true;
                        }
                        break;
                    }
                }
                if (!contain) {
                    System.out.println(triPoly.toString());
                }
                assert contain;
            }
            // check that areas match
            double aPoly = 0;
            for (Polygon poly : geometry) {
                double area = poly.getArea();
                aPoly += area;
                assert area > 0.25;
            }
            assert Math.round(aTri) == Math.round(aPoly);

            System.out.println(" :: ");
        }
    }

    // batch run conversion and triangulation and check for any errors
    // in the output (no validation of output is done)
    @Test
    public void massTest() throws IOException {
        for (int i = 1; i < 20000; i++) {
            Random rand = new Random(i);
            // create image
            int sizex = rand.nextInt(200)+5;
            int sizey = rand.nextInt(200)+5;
            boolean[][] data = new boolean[sizex][sizey];

            // fill with random data
            int count = rand.nextInt(sizex * sizey * 2);
            for (int j = 0; j < count; j++) {
                int x = rand.nextInt(sizex);
                int y = rand.nextInt(sizey);
                data[x][y] = true;
            }

            short[][][] polys = Grid2TriPolyFast.convert(data);

            Grid2TriPolyFast.triangulate(polys);

            System.out.println("Test " + i);
        }
    }

    // manual test for the new algorithm "voxel -> polygon"
    // uses input image and creates output image with created polygon
    @Test
    public void testNew() throws IOException {
        BufferedImage imgIn = ImageIO.read(new File("test.png"));
        boolean[][] data = new boolean[imgIn.getWidth()][imgIn.getHeight()];
        for (int x = 0; x < imgIn.getWidth(); x++) {
            for (int y = 0; y < imgIn.getHeight(); y++) {
                //System.out.println(img.getRGB(x,y));
                data[x][y] = imgIn.getRGB(x,y) != -1;
            }
        }

        // create polygons
        short[][][] polys = Grid2TriPolyFast.convert(data);

        // print data
        int zoom = 40;

        BufferedImage img = new BufferedImage(zoom*imgIn.getWidth() + zoom, zoom*imgIn.getHeight() + zoom, BufferedImage.TYPE_INT_RGB);
        Graphics2D gr = (Graphics2D) img.getGraphics();
        gr.setColor(Color.WHITE);
        AffineTransform at = new AffineTransform();
        at.translate(zoom/2, zoom/2);
        gr.setTransform(at);
        gr.fillRect(-zoom/2, -zoom/2, img.getWidth(), img.getHeight());
        gr.setFont(gr.getFont().deriveFont(Font.BOLD,15f));

        gr.drawImage(imgIn,0,0,img.getWidth()-zoom,img.getHeight()-zoom, null);

        gr.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        gr.setRenderingHint(
                RenderingHints.KEY_FRACTIONALMETRICS,
                RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        gr.setRenderingHint(
                RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);

        for (short[][] poly : polys) {
            // draw holes outline
            for (int n = 1; n < poly.length; n++) {
                for (int i = 0, len = poly[n].length - 2; i < len; i+=2) {
                    gr.setColor(Color.BLUE);
                    gr.drawLine(poly[n][i] * zoom, poly[n][i+1] * zoom, poly[n][i+2] * zoom, poly[n][i+3] * zoom);
                }
            }
            // draw poly outline
            for (int i = 0; i < poly[0].length - 2; i+=2) {
                // draw point to image
                gr.setColor(Color.RED);
                gr.drawLine(poly[0][i] * zoom, poly[0][i+1] * zoom, poly[0][i+2] * zoom, poly[0][i+3] * zoom);
            }
        }
        for (short[][] poly : polys) {
            // draw holes text
            for (int n = 1; n < poly.length; n++) {
                for (int i = 0, len = poly[n].length - 2; i < len; i+=2) {
                    gr.setColor(Color.BLUE);
                    // draw number
                    gr.drawString(String.valueOf(i/2+1), poly[n][i] * zoom - 10, poly[n][i+1] * zoom + 5);
                }
            }
            // draw poly text
            for (int i = 0; i < poly[0].length - 2; i+=2) {
                // draw point to image
                gr.setColor(Color.RED);
                // draw number
                gr.drawString(String.valueOf(i/2+1), poly[0][i] * zoom + 5, poly[0][i+1] * zoom + 5);
            }
        }

        ImageIO.write(img, "png", new File("out.png"));

    }

    // manual test for the old algorithm "voxel -> polygon"
    // uses input image and creates output image with created polygon
    @Test
    public void testOriginal() throws IOException {
        BufferedImage imgIn = ImageIO.read(new File("test.png"));
        boolean[][] data = new boolean[imgIn.getWidth()][imgIn.getHeight()];
        for (int x = 0; x < imgIn.getWidth(); x++) {
            for (int y = 0; y < imgIn.getHeight(); y++) {
                //System.out.println(img.getRGB(x,y));
                data[x][y] = imgIn.getRGB(x,y) != -1;
            }
        }

        // test original algorithm
        TiledImage src = ImageUtils.createConstantImage(imgIn.getWidth(), imgIn.getHeight(), 0);

        // fill with data
        for (int x = 0; x < imgIn.getWidth(); x++) {
            for (int y = 0; y < imgIn.getHeight(); y++) {
                src.setSample(x, y, 0, data[x][y] ? 1 : 0);
            }
        }

        // create polygons
        Collection<Polygon> polys = Grid2TriPolySlow.doVectorize(src);

        // print data
        int zoom = 40;

        BufferedImage img = new BufferedImage(zoom*imgIn.getWidth() + zoom, zoom*imgIn.getHeight() + zoom, BufferedImage.TYPE_INT_RGB);
        Graphics2D gr = (Graphics2D) img.getGraphics();
        gr.setColor(Color.WHITE);
        AffineTransform at = new AffineTransform();
        at.translate(zoom/2, zoom/2);
        gr.setTransform(at);
        gr.fillRect(-zoom/2, -zoom/2, img.getWidth(), img.getHeight());
        gr.setFont(gr.getFont().deriveFont(Font.BOLD,15f));

        gr.drawImage(imgIn,0,0,img.getWidth()-zoom,img.getHeight()-zoom, null);

        gr.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        gr.setRenderingHint(
                RenderingHints.KEY_FRACTIONALMETRICS,
                RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        gr.setRenderingHint(
                RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);

        Coordinate[] coordinates;
        for (Polygon poly : polys) {
            // draw holes outline
            for (int n = 0, size = poly.getNumInteriorRing(); n < size; n++) {
                coordinates = poly.getInteriorRingN(n).getCoordinates();
                for (int i = 0; i < coordinates.length - 1; i++) {
                    gr.setColor(Color.BLUE);
                    gr.drawLine((int) (coordinates[i].x * zoom), (int) (coordinates[i].y * zoom), (int) (coordinates[i+1].x * zoom), (int) (coordinates[i+1].y * zoom));
                }
            }
            // draw poly outline
            coordinates = poly.getExteriorRing().getCoordinates();
            for (int i = 0; i < coordinates.length - 1; i++) {
                // draw point to image
                gr.setColor(Color.RED);
                gr.drawLine((int) (coordinates[i].x * zoom), (int) (coordinates[i].y * zoom), (int) (coordinates[i + 1].x * zoom), (int) (coordinates[i + 1].y * zoom));
            }
        }
        for (Polygon poly : polys) {
            // draw holes text
            for (int n = 0, size = poly.getNumInteriorRing(); n < size; n++) {
                coordinates = poly.getInteriorRingN(n).getCoordinates();
                for (int i = 0; i < coordinates.length - 1; i++) {
                    gr.setColor(Color.BLUE);
                    // draw number
                    gr.drawString(String.valueOf(i+1), (int) (coordinates[i].x * zoom) - 10, (int) (coordinates[i].y * zoom) + 5);
                }
            }
            // draw poly text
            coordinates = poly.getExteriorRing().getCoordinates();
            for (int i = 0; i < coordinates.length - 1; i++) {
                // draw point to image
                gr.setColor(Color.RED);
                // draw number
                gr.drawString(String.valueOf(i+1), (int) (coordinates[i].x * zoom) + 5, (int) (coordinates[i].y * zoom) + 5);
            }
        }

        ImageIO.write(img, "png", new File("out.png"));

    }
}
