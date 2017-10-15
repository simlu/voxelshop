package com.vitco.app.util.misc;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.LineBreakMeasurer;
import java.awt.font.TextAttribute;
import java.awt.font.TextLayout;
import java.awt.geom.Rectangle2D;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;
import java.util.HashMap;
import java.util.Map;

public class FontUtil {
    public static Font getTitleFont(Font font) {
        Map<TextAttribute, Object> attributes = new HashMap<TextAttribute, Object>();
        attributes.put(TextAttribute.TRACKING, 0.1); // more spacing
        attributes.put(TextAttribute.WEIGHT, TextAttribute.WEIGHT_BOLD); // make bold
        return font.deriveFont(attributes);
    }

    /**
     * This method centers a <code>String</code> in
     * a bounding <code>Rectangle</code>.
     * @param g - The <code>Graphics</code> instance.
     * @param r - The bounding <code>Rectangle</code>.
     * @param s - The <code>String</code> to center in the
     * bounding rectangle.
     * @param font - The display font of the <code>String</code>
     *
     * @see java.awt.Graphics
     * @see java.awt.Rectangle
     * @see java.lang.String
     *
     * Reference: http://tiny.cc/4ny9gy
     */
    public static void centerString(Graphics g, Rectangle r, String s, Font font) {
        FontRenderContext frc = new FontRenderContext(null, true, true);
        ((Graphics2D)g).setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        Rectangle2D r2D = font.getStringBounds(s, frc);
        int rWidth = (int) Math.round(r2D.getWidth());
        int rHeight = (int) Math.round(r2D.getHeight());
        int rX = (int) Math.round(r2D.getX());
        int rY = (int) Math.round(r2D.getY());

        int a = (r.width / 2) - (rWidth / 2) - rX;
        int b = (r.height / 2) - (rHeight / 2) - rY;

        g.setFont(font);
        g.drawString(s, r.x + a, r.y + b);
    }

    // create attributed string that uses the main font, but fallbackFont if a character can't be rendered
    public static AttributedString createFallbackString(String text, Font mainFont, Font fallbackFont) {
        AttributedString result = new AttributedString(text);

        int textLength = text.length();
        result.addAttribute(TextAttribute.FONT, mainFont, 0, textLength);

        boolean fallback = false;
        int fallbackBegin = 0;
        for (int i = 0; i < text.length(); i++) {
            boolean curFallback = !mainFont.canDisplay(text.charAt(i));
            if (curFallback != fallback) {
                fallback = curFallback;
                if (fallback) {
                    fallbackBegin = i;
                } else {
                    result.addAttribute(TextAttribute.FONT, fallbackFont, fallbackBegin, i);
                }
            }
        }
        if (fallback) {
            result.addAttribute(TextAttribute.FONT, fallbackFont, fallbackBegin, text.length());
        }
        return result;
    }

    // obtain the correct width of an attributed string
    public static double getWidthOfAttributedString(Graphics2D graphics2D, AttributedString attributedString) {
        AttributedCharacterIterator characterIterator = attributedString.getIterator();
        FontRenderContext fontRenderContext = graphics2D.getFontRenderContext();
        LineBreakMeasurer lbm = new LineBreakMeasurer(characterIterator, fontRenderContext);
        TextLayout textLayout = lbm.nextLayout(Integer.MAX_VALUE);
        return textLayout.getBounds().getWidth();
    }

    // Draw a string consisting of words with automatic line breaks.
    // Returns the dimensions that drawing this string takes.
    // Drawing can be prevented with the draw flag
    public static Rectangle drawString(Graphics g, Font font, Font defaultFont, String str, int x, int y, int width, boolean draw) {
        FontMetrics fm = g.getFontMetrics();
        int lineHeight = fm.getHeight(); // get line hight
        int curX = x;
        int curY = y;
        int maxX = x;
        for (String word : str.replace("|","| ").split(" ")) { // split into words
            boolean lineEnd = word.endsWith("|");
            if (lineEnd) {
                // remove line break characters
                word = word.substring(0, word.length()-1);
            }
            if (!word.equals("")) {
                AttributedString attributedString = createFallbackString(word, font, defaultFont);
                int wordWidth = (int) (getWidthOfAttributedString((Graphics2D) g, attributedString) + fm.stringWidth(lineEnd ? "" : " ")); // get word width
                if (curX + wordWidth >= x + width) { // check if we need to do a line break
                    curY += lineHeight;
                    curX = x;
                }
                maxX = Math.max(maxX, curX + wordWidth);
                if (draw) {
                    g.drawString(attributedString.getIterator(), curX, curY);
                }
                // add word width to current x
                curX += wordWidth;
            }
            if (lineEnd) {
                curY += lineHeight;
                curX = x;
            }
        }
        // return dimensions
        return new Rectangle(x, y, maxX-x, (curY + lineHeight)-y);
    }
}
