package com.vitco.layout.content.mainview.components;


import javax.swing.*;
import java.awt.*;
import java.awt.font.GlyphVector;

/**
 * Label that has an outline
 */
public class OutlineLabel extends JLabel {

    // color of the text
    private final Color textColor;
    // outline color
    private final Color outlineColor;
    // shape that is used to draw
    private final Shape textShape;

    public OutlineLabel(String s, Color textColor, Color outlineColor) {
        super(s);
        // store colors
        this.textColor = textColor;
        this.outlineColor = outlineColor;

        // generate the shape
        Font font = this.getFont().deriveFont(Font.BOLD);
        GlyphVector v = font.createGlyphVector(getFontMetrics(font).getFontRenderContext(), getText());
        textShape = v.getOutline(0, v.getOutline().getBounds().height);
    }

    // called when this component is painted
    @Override
    public void paintComponent(Graphics g) {
        // note: default painting is omitted

        // set antialiasing
        Graphics2D g2 = (Graphics2D)g.create();
        g2.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
        g2.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

        // draw the outline
        g2.setStroke(new BasicStroke(1.5f));
        g2.setColor(outlineColor);
        g2.draw(textShape);

        // draw the interior
        g2.setStroke(new BasicStroke(1f));
        g2.setColor(textColor);
        g2.fill(textShape);

        // dispose the graphics element
        g2.dispose();
    }
}
