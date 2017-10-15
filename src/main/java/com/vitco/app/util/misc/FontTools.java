package com.vitco.app.util.misc;

import java.awt.GraphicsEnvironment;
import java.awt.Font;
import java.awt.Graphics2D;

// Auther: MaiJZ
// Date: 2017/10/15
// Github: https://github.com/maijz128

public class FontTools {

    // make it display UTF-8 help text
    public static void setRightFont(Graphics2D g, String text) {
        Font gFont = g.getFont();
        setRightFont(g, text, gFont.getStyle(), gFont.getSize());
    }

    public static void setRightFont(Graphics2D g, String text, int fontStyle, int fontSize) {
        String localFontName = findRightFontName(text);
        if (localFontName != null) {
            Font newFont = new Font(localFontName, fontStyle, fontSize);
            g.setFont(newFont);
        }
    }

    public static String findRightFontName(String text) {
        Font[] allFonts = GraphicsEnvironment.getLocalGraphicsEnvironment()
                .getAllFonts();
        for (int i = 0; i < allFonts.length; i++) {
            boolean canDisplayText = (allFonts[i].canDisplayUpTo(text) == -1);
            if (canDisplayText) {
                return allFonts[i].getFontName();
            }
        }
        return null;
    }
}
