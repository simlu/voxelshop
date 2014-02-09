package com.vitco.util.graphic;

import com.vitco.util.misc.ConversionTools;
import org.apache.commons.codec.digest.DigestUtils;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.*;

/**
 * Some basic functionality for graphics/images.
 */
public class GraphicTools {

    // returns the hash for a BufferedImage
    public static String getHash(BufferedImage image) {
        DataBuffer buffer = image.getRaster().getDataBuffer();
        int type = image.getRaster().getDataBuffer().getDataType();
        byte[] bytes = new byte[0];
        switch (type) {
            case DataBuffer.TYPE_BYTE:
                bytes = ((DataBufferByte)buffer).getData();
                break;
            case DataBuffer.TYPE_INT:
                bytes = ConversionTools.int2byte(((DataBufferInt)buffer).getData());
                break;
        }
        return DigestUtils.md5Hex(bytes);
    }

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
