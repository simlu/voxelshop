package com.vitco.low.triangulate.tests;

import com.vitco.low.triangulate.Grid2TriPolySlow;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.impl.CoordinateArraySequence;
import org.jaitools.imageutils.ImageUtils;
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
 * Abstract test class for all triangulation that implements the basics.
 */
public abstract class AbstractTriangulationTest {

    // helper - true if point c is in between point a and b
    private static boolean inBetween(Point a, Point b, Point c) {
        return (!a.equals(c) && !b.equals(c)) && // not the same points
                ((b.x - a.x) * (c.y - a.y) == (c.x - a.x) * (b.y - a.y)) && // on one line
                ((a.x < c.x == c.x < b.x) && (a.y < c.y == c.y < b.y)); // in between on that line
    }

    // execute the test
    public final void testTriangulation(int start, int stop, boolean printDebugImage, boolean tJunctionCheck) throws IOException {
        // do test for the specified range
        for (int i = start; i < stop; i++) {
            Random rand = new Random(i);
            // create image
            int sizex = rand.nextInt(100)+5;
            int sizey = rand.nextInt(100)+5;
            boolean[][] data = new boolean[sizex][sizey];
            TiledImage src = ImageUtils.createConstantImage(sizex, sizey, 0);

            // fill with random data
            int count = rand.nextInt(sizex * sizey * 2);
            for (int j = 0; j < count; j++) {
                int x = rand.nextInt(sizex);
                int y = rand.nextInt(sizey);
                data[x][y] = true;
                src.setSample(x, y, 0, 1);
            }

            if (printDebugImage) {
                // save image (for checking)
                BufferedImage bufferedImage = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_INT_RGB);
                for (int x = 0; x < src.getWidth(); x++) {
                    for (int y = 0; y < src.getHeight(); y++) {
                        bufferedImage.setRGB(x, y, src.getSample(x, y, 0) == 1 ? Color.BLACK.getRGB() : Color.WHITE.getRGB());
                    }
                }
                File outputfile = new File("image" + i + ".png");
                try {
                    ImageIO.write(bufferedImage, "png", outputfile);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            // print information
            System.out.print("Test " + i + " @ " + sizex + " x " + sizey + " :: ");
            Collection<Polygon> geometry = Grid2TriPolySlow.doVectorize(src);
            System.out.print(geometry.size());
            System.out.print(" :: ");
            ArrayList<DelaunayTriangle> tris = triangulate(data);
            System.out.print(tris.size());

            // =========

            if (tJunctionCheck) {
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
            }

            // =========

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
                    if (poly.contains(triPoly)) {
                        contain = true;
                        break;
                    }
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

    // execute the case test
    public final void testTriangulationCase(String filename, String outputFile, int zoom, boolean drawCoordinates) throws IOException {

        // load the image into the data array
        BufferedImage imgIn = ImageIO.read(new File(filename));
        boolean[][] data = new boolean[imgIn.getWidth()][imgIn.getHeight()];
        for (int x = 0; x < imgIn.getWidth(); x++) {
            for (int y = 0; y < imgIn.getHeight(); y++) {
                //System.out.println(img.getRGB(x,y));
                data[x][y] = imgIn.getRGB(x,y) != -1;
            }
        }

        // create triangles
        java.util.List<DelaunayTriangle> tris = triangulate(data);

        // print triangle information
        for (DelaunayTriangle tri : tris) {
            System.out.println(tri.points[0] + " " + tri.points[1] + " " + tri.points[2]);
        }

        BufferedImage img = new BufferedImage(zoom*imgIn.getWidth() + zoom, zoom*imgIn.getHeight() + zoom, BufferedImage.TYPE_INT_RGB);
        Graphics2D gr = (Graphics2D) img.getGraphics();
        gr.setColor(Color.WHITE);
        gr.fillRect(0, 0, img.getWidth(), img.getHeight());
        gr.setFont(gr.getFont().deriveFont(Font.BOLD,15f));

        // shift to center
        AffineTransform af = new AffineTransform();
        af.setToTranslation(zoom/2, zoom/2);
        gr.setTransform(af);

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

        int i = 0;
        for (DelaunayTriangle tri : tris) {
            gr.setColor(Color.GRAY);
            gr.drawLine((int)Math.round(tri.points[0].getX()*zoom), (int)Math.round(tri.points[0].getY()*zoom), (int)Math.round(tri.points[1].getX()*zoom), (int)Math.round(tri.points[1].getY()*zoom));
            gr.drawLine((int)Math.round(tri.points[1].getX()*zoom), (int)Math.round(tri.points[1].getY()*zoom), (int)Math.round(tri.points[2].getX()*zoom), (int)Math.round(tri.points[2].getY()*zoom));
            gr.drawLine((int)Math.round(tri.points[2].getX()*zoom), (int)Math.round(tri.points[2].getY()*zoom), (int)Math.round(tri.points[0].getX()*zoom), (int)Math.round(tri.points[0].getY()*zoom));
            gr.setColor(Color.BLACK);
            gr.drawString(String.valueOf(++i), (int)(tri.centroid().getX() * zoom) - 5, (int)(tri.centroid().getY() * zoom) + 5);
        }

        gr.setFont(gr.getFont().deriveFont(Font.PLAIN,15f));
        for (DelaunayTriangle tri : tris) {
            gr.setColor(Color.RED);
            gr.drawRect((int) Math.round(tri.points[0].getX() * zoom) - 2, (int) Math.round(tri.points[0].getY() * zoom) - 2, 4, 4);
            gr.drawRect((int) Math.round(tri.points[1].getX() * zoom) - 2, (int) Math.round(tri.points[1].getY() * zoom) - 2, 4, 4);
            gr.drawRect((int) Math.round(tri.points[2].getX() * zoom) - 2, (int) Math.round(tri.points[2].getY() * zoom) - 2, 4, 4);
            if (drawCoordinates) {
                gr.setColor(Color.GRAY);
                gr.drawString("(" + (int) Math.round(tri.points[0].getX()) + "," + (int) Math.round(tri.points[0].getY()) + ")", (int) Math.round(tri.points[0].getX() * zoom), (int) Math.round(tri.points[0].getY() * zoom) + 5);
                gr.drawString("(" + (int) Math.round(tri.points[1].getX()) + "," + (int) Math.round(tri.points[1].getY()) + ")", (int) Math.round(tri.points[1].getX() * zoom), (int) Math.round(tri.points[1].getY() * zoom) + 5);
                gr.drawString("(" + (int) Math.round(tri.points[2].getX()) + "," + (int) Math.round(tri.points[2].getY()) + ")", (int) Math.round(tri.points[2].getX() * zoom), (int) Math.round(tri.points[2].getY() * zoom) + 5);
            }
        }
        gr.dispose();
        ImageIO.write(img, "png", new File(outputFile));

        // ----------
    }

    // to implement by the test
    abstract ArrayList<DelaunayTriangle> triangulate(boolean[][] data);

}
