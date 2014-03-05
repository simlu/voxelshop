package com.vitco.low.triangulate.util;

import com.vitco.low.triangulate.Grid2TriPolyFast;
import com.vitco.low.triangulate.Grid2TriPolySlow;
import com.vividsolutions.jts.geom.Coordinate;
import org.jaitools.imageutils.ImageUtils;
import org.junit.Test;

import javax.imageio.ImageIO;
import javax.media.jai.TiledImage;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Random;

/**
 * Test for the Grid2PolyHelper class
 */
public class Grid2PolyHelperTest {

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

            short[][][] polys = Grid2PolyHelper.convert(data);

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
        short[][][] polys = Grid2PolyHelper.convert(data);

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
        gr.dispose();

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
        Collection<com.vividsolutions.jts.geom.Polygon> polys = Grid2TriPolySlow.doVectorize(src);

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
        for (com.vividsolutions.jts.geom.Polygon poly : polys) {
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
        for (com.vividsolutions.jts.geom.Polygon poly : polys) {
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
        gr.dispose();

        ImageIO.write(img, "png", new File("out.png"));

    }

}
