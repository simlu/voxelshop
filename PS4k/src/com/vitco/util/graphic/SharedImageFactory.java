package com.vitco.util.graphic;

import org.jaitools.imageutils.ImageUtils;

import javax.media.jai.TiledImage;
import java.awt.image.BufferedImage;
import java.util.HashMap;

/**
 * Provides static access to images by size. The images are shares and the process
 * that uses them is responsible for cleaning them up when they are no longer needed.
 *
 * Note: The TiledImages are filled with zeros and expected to be restored to that state.
 *
 * Note: The BufferedImages are not expected to have any filling.
 */
public class SharedImageFactory {

    // prevent instantiation
    private SharedImageFactory() {}

    // holds all the shared images
    private final static HashMap<String, TiledImage> tiledImageBuffer = new HashMap<String, TiledImage>();

    // retrieve a shared image
    public static TiledImage getTiledImage(int w, int h) {
        String key = w + "_" + h;
        TiledImage img = tiledImageBuffer.get(key);
        if (img == null) {
            img = ImageUtils.createConstantImage(w, h, 0);
            tiledImageBuffer.put(key, img);
        }
        return img;
    }

    // check if a size is already buffered
    public static boolean isTiledImageAllocated(int w, int h) {
        return tiledImageBuffer.containsKey(w + "_" + h);
    }

    // holds all the shared images
    private final static HashMap<String, BufferedImage> bufferedImageBuffer = new HashMap<String, BufferedImage>();

    // retrieve a shared image
    public static BufferedImage getBufferedImage(int w, int h) {
        String key = w + "_" + h;
        BufferedImage img = bufferedImageBuffer.get(key);
        if (img == null) {
            img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
            bufferedImageBuffer.put(key, img);
        }
        return img;
    }

    // check if a size is already buffered
    public static boolean isBufferedImageAllocated(int w, int h) {
        return bufferedImageBuffer.containsKey(w + "_" + h);
    }


}
