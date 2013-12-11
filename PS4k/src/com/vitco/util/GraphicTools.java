package com.vitco.util;

import javax.media.jai.JAI;
import javax.media.jai.ParameterBlockJAI;
import javax.media.jai.RenderedOp;
import javax.media.jai.operator.TransposeDescriptor;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;

/**
 * Some basic functionality for graphics/images.
 */
public class GraphicTools {
    public static BufferedImage deepCopy(BufferedImage bi) {
        ColorModel cm = bi.getColorModel();
        boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
        WritableRaster raster = bi.copyData(null);
        return new BufferedImage(cm, raster, isAlphaPremultiplied, null);
    }

    private final static ParameterBlockJAI pb = new ParameterBlockJAI("Transpose");
    static {
        // disable error for rotation
        System.setProperty("com.sun.media.jai.disableMediaLib", "true");
    }
    // rotate an image
    public static Image rotate(Image img, int orientation) {
        assert img != null;
        pb.setSource("source0", img);

        switch (orientation) {
            case 1:
                pb.setParameter("type", TransposeDescriptor.FLIP_VERTICAL);
                break;
            case 2:
                pb.setParameter("type", TransposeDescriptor.ROTATE_90);
                break;
            case 3:
                pb.setParameter("type", TransposeDescriptor.FLIP_DIAGONAL);
                break;
            case 4:
                pb.setParameter("type", TransposeDescriptor.ROTATE_180);
                break;
            case 5:
                pb.setParameter("type", TransposeDescriptor.FLIP_HORIZONTAL);
                break;
            case 6:
                pb.setParameter("type", TransposeDescriptor.ROTATE_270);
                break;
            case 7:
                pb.setParameter("type", TransposeDescriptor.FLIP_ANTIDIAGONAL);
                break;
            default:
                break;
        }

        RenderedOp renderedOp = JAI.create("Transpose", pb);
        pb.removeSources(); // free data references
        return renderedOp.getRendering().getAsBufferedImage();
    }

}
