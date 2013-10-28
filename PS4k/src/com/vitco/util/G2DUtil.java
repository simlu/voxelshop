package com.vitco.util;

import com.threed.jpct.SimpleVector;

import java.awt.*;

/**
 * Helps with basic drawing tasks for Graphic2D objects.
 */
public class G2DUtil {
    // draw a point with a border
    public static void drawPoint(SimpleVector point,
                                 Graphics2D ig, Color innerColor, Color outerColor,
                                 float radius, float borderSize) {
        // set outer line size
        ig.setStroke(new BasicStroke(borderSize, BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL));
        ig.setColor(innerColor);
        int rad2times = Math.round(radius * 2);
        ig.fillOval(Math.round(point.x - radius), Math.round(point.y - radius), rad2times, rad2times);
        ig.setColor(outerColor);
        ig.drawOval(Math.round(point.x - radius), Math.round(point.y - radius), rad2times, rad2times);
    }

    // draw a line with an outline
    public static void drawLine(SimpleVector p1, SimpleVector p2,
                                Graphics2D ig, Color innerColor, Color outerColor,
                                float size) {
        if (p1 != null && p2 != null) {
            // outer line
            ig.setColor(outerColor); // line color
            ig.setStroke(new BasicStroke(size * 1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL)); // line size
            ig.drawLine(Math.round(p1.x), Math.round(p1.y),
                    Math.round(p2.x), Math.round(p2.y));
            // inner line
            ig.setColor(innerColor); // line color
            ig.setStroke(new BasicStroke(size, BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL)); // line size
            ig.drawLine(Math.round(p1.x), Math.round(p1.y),
                    Math.round(p2.x), Math.round(p2.y));
        }
    }
}
