package com.vitco.util.misc;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashSet;

/**
 * Basic color conversion tools
 */
public class ColorTools {
    public static float[] colorToHSB(Color color) {
        return Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null);
    }

    public static Color hsbToColor(float[] hsb) {
        return Color.getHSBColor(hsb[0], hsb[1], hsb[2]);
    }

    public static double perceivedBrightness(Color color) {
        return 0.299*color.getRed() + 0.587*color.getGreen() + 0.114*color.getBlue();
    }

    public static double perceivedBrightness(float[] hsb) {
        return perceivedBrightness(hsbToColor(hsb));
    }

    // compare two colors (get perceived similarity)
    public static double perceivedSimilarity(Color col1, Color col2) {
        float[] hsb1 = colorToHSB(col1);
        float[] hsb2 = colorToHSB(col2);
        return 1 - (0.8 * Math.abs((hsb1[0] - Math.floor(hsb1[0])) - (hsb2[0] - Math.floor(hsb2[0]))) +
                0.1 * Math.abs(hsb1[1] - hsb2[1]) +
                0.1 * Math.abs(hsb1[2] - hsb2[2]));
    }

    public static Color cmykToColor(float[] cmyk) {

//        float C = ( cmyk[0] * ( 1 - cmyk[3] ) + cmyk[3] );
//        float M = ( cmyk[1] * ( 1 - cmyk[3] ) + cmyk[3] );
//        float Y = ( cmyk[2] * ( 1 - cmyk[3] ) + cmyk[3] );
//
//        int R = Math.round(( 1 - C ) * 255);
//        int G = Math.round(( 1 - M ) * 255);
//        int B = Math.round(( 1 - Y ) * 255);
//
//        return new Color(R, G, B);


        float C3 = cmyk[0] * (1 - cmyk[3]) + 1 * cmyk[3];
        if (C3 > 1) {
            C3 = 1;
        }

        float M3 = cmyk[1] * (1 - cmyk[3]) + 1 * cmyk[3];
        if (M3 > 1) {
            M3 = 1;
        }

        float Y3 = cmyk[2] * (1 - cmyk[3]) + 1 * cmyk[3];
        if (Y3 > 1) {
            Y3 = 1;
        }

        return new Color((1 - C3), (1 - M3), (1 - Y3));
    }

    public static float[] colorToCMYK(Color color) {

//        int R = color.getRed();
//        int G = color.getGreen();
//        int B = color.getBlue();
//
//        float C = 1 - ( (float)R / 255 );
//        float M = 1 - ( (float)G / 255 );
//        float Y = 1 - ( (float)B / 255 );
//
//        float var_K = 1;
//
//        if ( C < var_K )   var_K = C;
//        if ( M < var_K )   var_K = M;
//        if ( Y < var_K )   var_K = Y;
//        if ( var_K == 1 ) { //Black
//            C = 0;
//            M = 0;
//            Y = 0;
//        }
//        else {
//            C = ( C - var_K ) / ( 1 - var_K );
//            M = ( M - var_K ) / ( 1 - var_K );
//            Y = ( Y - var_K ) / ( 1 - var_K );
//        }
//        float K = var_K;
//
//        return new float[] {C,M,Y,K};

        float[] result = new float[4];

        int r = color.getRed();
        int g = color.getGreen();
        int b = color.getBlue();

        // BLACK
        if (r==0 && g==0 && b==0) {
            result = new float[] {0,0,0,1};
        }

        result[0] = 1 - ((float)r/255);
        result[1] = 1 - ((float)g/255);
        result[2] = 1 - ((float)b/255);

        float minCMY = Math.min(result[0], Math.min(result[1],result[2]));
        result[0] = (result[0] - minCMY) / (1 - minCMY) ;
        result[1] = (result[1] - minCMY) / (1 - minCMY) ;
        result[2] = (result[2] - minCMY) / (1 - minCMY) ;
        result[3] = minCMY;

        return result;
    }

    // get ordered list of given colors (by perceived similarity
    public static ArrayList<Color> orderColors(HashSet<Integer> colors) {
        // check if empty
        if (colors.isEmpty()) {
            return new ArrayList<Color>();
        }

        // extract colors
        ArrayList<Color> list = new ArrayList<Color>();
        for (Integer val : colors) {
            list.add(new Color(val));
        }
        // -- order
        ArrayList<Color> sorted = new ArrayList<Color>();
        sorted.add(list.remove(0));
        while (!list.isEmpty()) {
            double simFront = 0;
            double simBack = 0;
            int valFront = 0;
            int valBack = 0;
            for (int i = 0; i < list.size(); i++) {
                double simFrontTmp = ColorTools.perceivedSimilarity(list.get(i), sorted.get(0));
                if (simFrontTmp > simFront) {
                    valFront = i;
                    simFront = simFrontTmp;
                }
                double simBackTmp = ColorTools.perceivedSimilarity(list.get(i), sorted.get(sorted.size()-1));
                if (simBackTmp > simBack) {
                    valBack = i;
                    simBack = simBackTmp;
                }
            }
            if (simFront > simBack) {
                sorted.add(0, list.remove(valFront));
            } else {
                sorted.add(list.remove(valBack));
            }
        }
        // return sorted colors
        return sorted;
    }
}
