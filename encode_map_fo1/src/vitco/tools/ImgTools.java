package vitco.tools;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Helper class for image operations
 */
public class ImgTools {
    public static String getHash(BufferedImage bi) {
        String result = null;
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        try {
            ImageIO.write(bi, "png", os);
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(os.toByteArray());
            byte[] hash = md.digest();
            result = HexTools.byteToHex(hash);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace(); // should never happen
        } catch (IOException e) {
            e.printStackTrace(); // should also never happen
        }
        return result;
    }

    public static BufferedImage deepCopy(BufferedImage bi) {
        ColorModel cm = bi.getColorModel();
        boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
        WritableRaster raster = bi.copyData(null);
        return new BufferedImage(cm, raster, isAlphaPremultiplied, null);
    }
}
