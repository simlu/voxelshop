package com.vitco.app.util.offline;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Generate textures, so we don't have to do it on startup.
 */
public class GenerateTexture {
    public static void main(String[] args) {
        generateTexture(512, true);
    }

    public static void generateTexture(int texSize, boolean drawSmallLines) {
        // create texture
        BufferedImage overlay = new BufferedImage(texSize, texSize, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = (Graphics2D) overlay.getGraphics();
        g2.setColor(new Color(0, 0, 0));
        g2.fillRect(0,0,texSize,texSize);

        // draw the "light" lines
        if (drawSmallLines) {
            g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL));
            g2.setColor(new Color(170,170,170));
            for (int i = 0; i <= texSize; i += 32) {
                g2.drawLine(0, i, texSize, i);
                g2.drawLine(i, 0, i, texSize);
            }
        }

        // draw thick lines
        g2.setStroke(new BasicStroke(4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL));
        g2.setColor(new Color(255,255,255));
        for (int i = 0; i <= texSize; i += 32 * 4) {
            g2.drawLine(0, i, texSize, i);
            g2.drawLine(i, 0, i, texSize);
        }

        // write the texture file
        try {
            ImageIO.write(overlay, "png", new File("resource/tex/bounding_box_" + texSize + ".png"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
