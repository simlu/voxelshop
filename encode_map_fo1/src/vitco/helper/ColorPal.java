package vitco.helper;

import vitco.main.Config;
import vitco.tools.FileTools;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

/**
 * Test for color palette generation
 */
public class ColorPal {
    private final static int tileSize = 15;
    private final static int rowCount = 20;

    private static float getSat(Integer c1) {
        Color c = new Color(c1);
        float[] hsbVals = new float[3];
        Color.RGBtoHSB(c.getRed(), c.getGreen(), c.getBlue(), hsbVals);
        return hsbVals[1];
    }

    private static float getBright(Integer c1) {
        Color c = new Color(c1);
        float[] hsbVals = new float[3];
        Color.RGBtoHSB(c.getRed(), c.getGreen(), c.getBlue(), hsbVals);
        return hsbVals[2];
    }

    private static void printList(ArrayList<Integer> list, String swatchFile) {
        // sort the color list
        Collections.sort(list, new Comparator<Integer>() {
            private float getHue(Integer c1) {
                Color c = new Color(c1);
                float[] hsbVals = new float[3];
                Color.RGBtoHSB(c.getRed(), c.getGreen(), c.getBlue(), hsbVals);
                return hsbVals[0];
            }
            @Override
            public int compare(Integer o1, Integer o2) {
                return (int)Math.signum(getHue(o1) - getHue(o2));
            }
        });
        // print the colors
        BufferedImage img = new BufferedImage(tileSize * rowCount, (int)Math.ceil(list.size() / (double) rowCount) * tileSize, BufferedImage.TYPE_INT_RGB);
        Graphics2D gr = (Graphics2D)img.getGraphics();
        for (int i = 0, len = list.size(); i < len; i++) {
            gr.setColor(new Color(list.get(i)));
            gr.fillRect((i % rowCount) * tileSize, (i / rowCount) * tileSize, tileSize, tileSize);
        }
        try {
            File outputfile = new File(swatchFile);
            ImageIO.write(img, "png", outputfile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void write(String tileFolder) {
        // make sure the folder exists
        if (!FileTools.createDir(Config.swatchFolder)) {
            FileTools.emptyDir(Config.swatchFolder);
        }

        HashMap<Integer, Integer> colors = new HashMap<Integer, Integer>();

        String[] pngFiles = FileTools.getFilesInFolder(new File(tileFolder), ".png");
        for (String filename : pngFiles) {
            try {
                BufferedImage img = ImageIO.read(new File(filename));
                for (int x = 0, lenx = img.getWidth(); x < lenx; x++) {
                    for (int y = 0, leny = img.getHeight(); y < leny; y++) {
                        int rgb = img.getRGB(x,y);
                        if (!colors.containsKey(rgb)) {
                            colors.put(rgb, 1);
                        }  else {
                            colors.put(rgb, colors.get(rgb) + 1);
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        ArrayList<Integer> rList = new ArrayList<Integer>();
        ArrayList<Integer> gList = new ArrayList<Integer>();
        ArrayList<Integer> bList = new ArrayList<Integer>();

        // group into red, green, blue
        for (Integer val : colors.keySet()) {
            Color col = new Color(val);
            int r = col.getRed();
            int g = col.getGreen();
            int b = col.getBlue();
            byte type = r > g && r > b
                    ? (byte)1
                    : (g > r && g > b
                    ? (byte)2
                    : (byte)3);

            switch (type) {
                case 1:
                    rList.add(val);
                    break;
                case 2:
                    gList.add(val);
                    break;
                case 3:
                    bList.add(val);
                    break;
            }
        }

        printList(rList, Config.swatchFolder + "redSwatch.png");
        printList(gList, Config.swatchFolder + "greenSwatch.png");
        printList(bList, Config.swatchFolder + "blueSwatch.png");

        //-------------------------

        ArrayList<Integer> darkList = new ArrayList<Integer>();
        ArrayList<Integer> medList = new ArrayList<Integer>();
        ArrayList<Integer> brightList = new ArrayList<Integer>();

        // group into brightness
        for (Integer val : colors.keySet()) {
            float brightness = getBright(val);
            if (brightness > 0.666) {
                brightList.add(val);
            } else if (brightness > 0.333) {
                medList.add(val);
            } else {
                darkList.add(val);
            }
        }

        printList(darkList, Config.swatchFolder + "darkSwatch.png");
        printList(medList, Config.swatchFolder + "medSwatch.png");
        printList(brightList, Config.swatchFolder + "brightSwatch.png");

        ArrayList<Integer> satList = new ArrayList<Integer>();
        ArrayList<Integer> medsatList = new ArrayList<Integer>();
        ArrayList<Integer> unsatList = new ArrayList<Integer>();

        // group into saturation
        for (Integer val : colors.keySet()) {
            float sat = getSat(val);
            if (sat > 0.666) {
                satList.add(val);
            } else if (sat > 0.333) {
                medsatList.add(val);
            } else {
                unsatList.add(val);
            }
        }

        printList(satList, Config.swatchFolder + "satSwatch.png");
        printList(medsatList, Config.swatchFolder + "medsatSwatch.png");
        printList(unsatList, Config.swatchFolder + "unsatSwatch.png");

        ArrayList<Integer> list = new ArrayList<Integer>();
        for (Integer col : colors.keySet()) {
            list.add(col);
        }

        printList(list, Config.swatchFolder + "swatch.png");
    }
}
