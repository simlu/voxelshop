package com.vitco.util.misc;

import java.awt.*;

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
}
