package com.vitco.util;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * Image helper calss.
 */
public class ImgTools {
    // load a buffered image
    public static BufferedImage loadImage(String imgName) {
        BufferedImage texture = null;
        try {
            texture = ImageIO.read(new File(imgName));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return texture;
    }

    // write image to png file
    public static boolean writeAsPNG(BufferedImage img, String fileName) {
        try {
            ImageIO.write(img, "png", new File(fileName));
        } catch (IOException e) {
            return false;
        }
        return true;
    }
}
