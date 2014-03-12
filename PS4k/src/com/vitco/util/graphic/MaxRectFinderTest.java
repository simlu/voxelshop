package com.vitco.util.graphic;

import org.junit.Test;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * Test the max rect finder implementation.
 */
public class MaxRectFinderTest {

    @Test
    public final void runTestCase() throws IOException {
        // load the image into the data array
        BufferedImage imgIn = ImageIO.read(new File("test.png"));
        short[][] matrix = new short[imgIn.getWidth()][imgIn.getHeight()];
        for (int x = 0; x < imgIn.getWidth(); x++) {
            for (int y = 0; y < imgIn.getHeight(); y++) {
                //System.out.println(img.getRGB(x,y));
                matrix[x][y] = (short) (imgIn.getRGB(x,y) != -1 ? 1 : 0);
            }
        }

        Rectangle rect = MaxRectFinder.maximalRectangle(matrix);
        System.out.println(rect);

    }

}
