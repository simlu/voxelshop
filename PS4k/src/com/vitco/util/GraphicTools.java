package com.vitco.util;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;

/**
 * Some basic functionality for graphics/images.
 */
public class GraphicTools {

    // create a deep copy of a bufferedImage (fast)
    public static BufferedImage deepCopy(BufferedImage bi) {
        ColorModel cm = bi.getColorModel();
        boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
        WritableRaster raster = bi.copyData(null);
        return new BufferedImage(cm, raster, isAlphaPremultiplied, null);
    }

    // rotate an image
    public static Image rotate(Image img, int orientation) {
        assert img != null;

        int w = img.getWidth(null);
        int h = img.getHeight(null);

        BufferedImage tmp = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = (Graphics2D) tmp.getGraphics();
        g2d.drawImage(img, 0, 0, null);
        g2d.dispose();

        AffineTransform affineTransform;

        switch (orientation) {
            case 1: // FLIP_HORIZONTAL
                affineTransform = new AffineTransform();
                affineTransform.scale(-1, 1);
                affineTransform.translate ( -w, 0 ) ;
                break;
            case 2: // ROTATE_90
                affineTransform = AffineTransform.getRotateInstance(Math.toRadians(90));
                affineTransform.translate ( 0, -h ) ;
            break;
            case 3: // FLIP_ANTIDIAGONAL
                affineTransform = AffineTransform.getRotateInstance(Math.toRadians(90));
                affineTransform.scale(-1, 1);
                affineTransform.translate ( -w, -h ) ;
                break;
            case 4: // ROTATE_180
                affineTransform = AffineTransform.getRotateInstance(Math.toRadians(180));
                affineTransform.translate(-w, -h) ;
                break;
            case 5: // FLIP_VERTICAL
                affineTransform = AffineTransform.getRotateInstance(Math.toRadians(180));
                affineTransform.scale(-1, 1);
                affineTransform.translate(0, -h) ;
                break;
            case 6: // ROTATE_270
                affineTransform = AffineTransform.getRotateInstance(Math.toRadians(270));
                affineTransform.translate(-w, 0) ;
                break;
            case 7: // FLIP_DIAGONAL
                affineTransform = AffineTransform.getRotateInstance(Math.toRadians(270));
                affineTransform.scale(-1, 1);
                affineTransform.translate(0, 0) ;
                break;
            default:
                affineTransform = new AffineTransform();
                break;
        }

        AffineTransformOp affineTransformOp = new AffineTransformOp(affineTransform, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
        return affineTransformOp.filter(tmp, null);
    }

}
