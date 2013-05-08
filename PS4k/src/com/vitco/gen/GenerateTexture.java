package com.vitco.gen;

import com.vitco.res.VitcoSettings;

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
        generateTexture(128, true);
        generateTexture(256, true);
    }

    public static void generateTexture(int texSize, boolean drawSmallLines) {
        // create texture
        BufferedImage overlay = new BufferedImage(texSize, texSize, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = (Graphics2D) overlay.getGraphics();
        g2.setColor(new Color(0, 0, 0));
        g2.fillRect(0,0,texSize,texSize);
        float size = (VitcoSettings.VOXEL_SIZE/VitcoSettings.VOXEL_GROUND_PLANE_SIZE)*texSize;
        if (drawSmallLines) {
            g2.setStroke(new BasicStroke(1f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL));
            g2.setColor(new Color(170,170,170));
            for (float i = 0; i < texSize; i+= size * 1) {
                g2.drawLine(0, Math.round(i), texSize, Math.round(i));
                g2.drawLine(Math.round(i), 0, Math.round(i), texSize);
            }
        }
        g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL));
        g2.setColor(new Color(255,255,255));
        for (float i = 0; i < texSize; i+= size * 3) {
            g2.drawLine(0, Math.round(i), texSize, Math.round(i));
            g2.drawLine(Math.round(i), 0, Math.round(i), texSize);
        }
        try {
            ImageIO.write(overlay, "png", new File("resource/tex/bounding_box_" + texSize + ".png"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
