package com.vitco.manager.help;

import com.jidesoft.docking.DockableFrame;
import com.jidesoft.docking.FrameContainer;
import com.jidesoft.plaf.basic.BasicJideTabbedPaneUI;
import com.jidesoft.swing.JideButton;
import com.jidesoft.swing.JideMenu;
import com.jidesoft.swing.JideSplitButton;
import com.jidesoft.swing.JideTabbedPane;
import com.vitco.Main;
import com.vitco.layout.frames.custom.FrameGenericJideButton;
import com.vitco.manager.action.ActionManager;
import com.vitco.manager.action.ComplexActionManager;
import com.vitco.manager.lang.LangSelectorInterface;
import com.vitco.settings.VitcoSettings;
import com.vitco.util.misc.SaveResourceLoader;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.CubicCurve2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Help overlay class that displays the help on top
 * over the frame that the help was "clicked on".
 *
 */
public class FrameHelpOverlay extends JComponent {
    // the frame this overlay lives in
    private final JRootPane frame;
    // the action managers that are used to acquire the actions
    private final ActionManager actionManager;
    private ComplexActionManager complexActionManager;
    // language selector that is used to lookup the help texts
    private LangSelectorInterface langSelector;

    // custom rectangle class that can store string information
    private final static class CRectangle extends Rectangle {
        // constructor
        public CRectangle(int x, int y, int w, int h, String info) {
            super(x,y,w,h);
            this.info = info;
        }

        // retrieve the stored information
        private String info = null;
        public final String getInfo() {
            return info;
        }
    }

    // list of known rectangles (that can trigger a refresh)
    private final ArrayList<CRectangle> rects = new ArrayList<CRectangle>();

    // currently active rectangle
    private CRectangle activeRect = null;

    // reference to this help overlay instance
    private final JComponent thisInstance = this;

    // handwriting font
    private static Font font = null;

    // close button image
    private static Image closeButton = null;

    // background information (this is stored to fake transparency without
    // actually needing to refresh the components that are "underneath")
    private static BufferedImage image = null;

    // the rectangle that we want to display our information into
    private Rectangle displayRect = null;

    // ---------------

    // static constructor
    static {
        // read handwriting font from file
        try {
            font = Font.createFont(Font.TRUETYPE_FONT,
                    new SaveResourceLoader("resource/font/font.ttf").asInputStream()).deriveFont(Font.PLAIN, 18f);
        } catch (FontFormatException e) {
            // should never happen
            e.printStackTrace();
        } catch (IOException e) {
            // should never happen
            e.printStackTrace();
        }
        // read close button image
        closeButton = new SaveResourceLoader("resource/img/icons/close.png").asImage();
    }

    // constructor
    public FrameHelpOverlay(final JRootPane frame, ActionManager actionManager,
                            ComplexActionManager complexActionManager, LangSelectorInterface langSelector) {
        // set final parameters
        this.frame = frame;
        this.actionManager = actionManager;
        this.complexActionManager = complexActionManager;
        this.langSelector = langSelector;
        // set this to "not transparent" (we handle fake transparency our self)
        this.setOpaque(true);
        // register mouse events
        MouseAdapter adapter = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                super.mousePressed(e);
                // hide this instance when clicked
                setActive(false);
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                super.mouseMoved(e);
                handleMoveEvent(e.getPoint());
            }

            @Override
             public void mouseExited(MouseEvent e) {
                super.mouseExited(e);
                handleMoveEvent(new Point(-1,-1));
            }
        };
        this.addMouseMotionListener(adapter);
        this.addMouseListener(adapter);
        // register "hide when focus lost"
        this.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                super.focusLost(e);
                setActive(false);
            }
        });
        // listen to show and resize actions
        this.addComponentListener(new ComponentAdapter () {
            @Override
            public void componentShown(ComponentEvent e) {
                super.componentShown(e);
                updateInternal();
                // reset mouse position (highlighting)
                handleMoveEvent(new Point(-1,-1));
            }

            @Override
            public void componentHidden(ComponentEvent e) {
                super.componentHidden(e);
                image = null; // free memory
            }


            @Override
            public void componentResized(ComponentEvent e) {
                super.componentResized(e);
                updateInternal();
            }
        });
        // register "hide when esc pressed"
        this.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                super.keyPressed(e);
                if (e.getKeyCode() == 27) {
                    setActive(false);
                }
            }
        });
    }

    // ====================
    // Internal logic
    // ====================

    // enable/disable this glasspane
    private boolean active = false;
    public final void setActive(boolean active) {
        this.active = active;
        frame.setGlassPane(this);
        this.setVisible(active); // this will trigger updating
        if (active) {
            // request focus if visible
            thisInstance.requestFocusInWindow();
        }
    }
    public final boolean isActive() {
        return active;
    }

    // helper - repaint this overlay if rectangle of interest changes
    private void handleMoveEvent(Point point) {
        if (isActive()) {
            boolean found = false;
            // check if position is inside a rectangle
            for (CRectangle rect : rects) {
                if (rect.contains(point)) {
                    if (!rect.equals(activeRect)) {
                        activeRect = rect;
                        thisInstance.repaint();
                    }
                    found = true;
                    break;
                }
            }
            // check if position is not inside any rectangle
            if (!found) {
                if (activeRect != null) {
                    activeRect = null;
                    thisInstance.repaint();
                }
            }
        }
    }

    // helper - invalidate internal cache structure
    private void updateInternal() {
        if (isActive()) {
            // analyse rectangle structure
            rects.clear();
            rAnalyze(frame.getContentPane(), 0, 0);
            // check for missing help information
            if (Main.isDebugMode()) {
                for (CRectangle rect : rects) {
                    if (!langSelector.containsString("help_overlay_" + rect.getInfo())) {
                        System.out.println("Error: Missing help for: " + "help_overlay_" + rect.getInfo());
                    }
                }
            }
            // determine the display rect (where the information is shown in)
            int left = thisInstance.getWidth()/2 - 250;
            displayRect = new Rectangle(
                    Math.max(left, 20),
                    0,
                    Math.min(left+500, thisInstance.getWidth()) - Math.max(left, 40),
                    thisInstance.getHeight()
            );
            // trigger a repaint
            thisInstance.repaint();
        }
    }

    // helper - recursively find components and convert them
    // to rectangles with identifier text
    private void rAnalyze(Container cont, int x, int y) {
        // loop over all contained components
        for (int i = 0; i < cont.getComponentCount(); i++) {
            Component comp = cont.getComponent(i);
            if (comp.isVisible()) { // only handle visible components
                if (comp instanceof JideSplitButton) {
                    // handle "complex" action buttons (popup component)
                    for (Component complexAction : ((JideSplitButton)comp).getMenuComponents()) {
                        String[] actionKeys = complexActionManager.getActionKeys(complexAction);
                        if (actionKeys.length > 0) {
                            int xn = x + comp.getX();
                            int yn = y + comp.getY();
                            int w = comp.getWidth();
                            int h = comp.getHeight();
                            // add to front
                            rects.add(0, new CRectangle(xn, yn, w, h, "complex_action_" + actionKeys[0]));
                            break;
                        }
                    }
                } else if (comp instanceof JideButton) {
                    // handle "normal" action buttons
                    for (ActionListener actionListener : ((AbstractButton)comp).getActionListeners()) {
                        if (actionListener instanceof AbstractAction) {
                            String[] actionKeys = actionManager.getActionKeys((AbstractAction)actionListener);
                            if (actionKeys.length > 0) {
                                int xn = x + comp.getX();
                                int yn = y + comp.getY();
                                int w = comp.getWidth();
                                int h = comp.getHeight();
                                // add to front
                                rects.add(0, new CRectangle(xn, yn, w, h, "action_" + actionKeys[0]));
                                break;
                            } else {
                                if (comp instanceof FrameGenericJideButton) {
                                    int xn = x + comp.getX();
                                    int yn = y + comp.getY();
                                    int w = comp.getWidth();
                                    int h = comp.getHeight();
                                    // add to front
                                    rects.add(0, new CRectangle(xn, yn, w, h,
                                            "generic_header_button_" + ((JideButton) comp).getToolTipText()
                                                    .replace(" ", "_").toLowerCase())
                                    );
                                }
                            }
                        }
                    }
                } else if (comp instanceof DockableFrame) {
                    // handle dockable sub-frames (i.e. the entire sub-frames)
                    int xn = x + comp.getX() - 2;
                    int yn = y + comp.getY() - 2;
                    int w = comp.getWidth() + 4;
                    int h = comp.getHeight() + 4;
                    // append
                    rects.add(new CRectangle(xn, yn, w, h, "window_" + ((DockableFrame)comp).getKey()));
                } else if (comp instanceof BasicJideTabbedPaneUI.ScrollableTabViewport) {
                    // handle window tab bars (if multiple windows are stacked)
                    Component tabbedPaneUncast = comp.getParent();
                    // check if parent is really a JideTabbedPane
                    if (tabbedPaneUncast != null && tabbedPaneUncast instanceof JideTabbedPane) {
                        JideTabbedPane tabbedPane = (JideTabbedPane)tabbedPaneUncast;
                        Rectangle visibleRect = comp.getBounds();
                        // loop over all tabs
                        for (int k = 0; k < tabbedPane.getTabCount(); k++) {
                            // compute visible part of rectangle of this tab
                            Rectangle rect = tabbedPane.getBoundsAt(k).intersection(visibleRect);
                            // check if this tab is actually visible
                            if (!rect.isEmpty()) { // check that this tab is actually visible
                                if (tabbedPane instanceof FrameContainer) {
                                    // this is a "proper" dockable jide window
                                    // i.e. several dockable windows are grouped together
                                    String name = tabbedPane.getComponentAt(k).getName();
                                    if (name != null) {
                                        int xn = x + rect.x;
                                        int yn = y + rect.y;
                                        int w = rect.width;
                                        int h = rect.height;
                                        // add to front
                                        rects.add(0, new CRectangle(xn, yn, w, h, "window_" + name));
                                    } else {
                                        System.out.println("Error: No name detected for help identifier (#1).");
                                    }
                                } else {
                                    // this lives inside a dockable window (custom defined)
                                    // find the "proper" parent dockable frame
                                    Component parent = tabbedPane.getParent();
                                    while (parent != null && !(parent instanceof DockableFrame)) {
                                        parent = parent.getParent();
                                    }
                                    if (parent != null) {
                                        String name = "window_" + ((DockableFrame)parent).getKey() + "_tab_" + tabbedPane.getTitleAt(k).replace(" ", "_").toLowerCase();
                                        int xn = x + rect.x;
                                        int yn = y + rect.y;
                                        int w = rect.width;
                                        int h = rect.height;
                                        // add to front
                                        rects.add(0, new CRectangle(xn, yn, w, h, name));
                                    } else {
                                        System.out.println("Error: No name detected for help identifier (#2).");
                                    }
                                }
                            }
                        }
                    }
                } else if (comp instanceof JideMenu) {
                    // handle menu items (text menu)
                    String name = comp.getName();
                    if (name != null && !name.equals("")) {
                        int xn = x + comp.getX();
                        int yn = y + comp.getY();
                        int w = comp.getWidth();
                        int h = comp.getHeight();
                        // append
                        rects.add(new CRectangle(xn, yn, w, h, "menu_item_" + name));
                    }
                }
                // -- recursively analyze the sub-components
                if (comp instanceof Container) {
                    rAnalyze((Container) comp, x + comp.getX(), y + comp.getY());
                }
            }
        }
    }

    // ====================
    // Drawing logic
    // ====================

    // Draw a string consisting of words with automatic line breaks.
    // Returns the dimensions that drawing this string takes (drawing can be prevented with the draw flag)
    public Rectangle drawString(Graphics g, String str, int x, int y, int width, boolean draw) {
        FontMetrics fm = g.getFontMetrics();
        int lineHeight = fm.getHeight(); // get line hight
        int curX = x;
        int curY = y;
        int maxX = x;
        // replace line breaks (the "|" characters)
        str = str.replace("|","| ");
        for (String word : str.split(" ")) { // split into words
            boolean lineEnd = word.endsWith("|");
            if (lineEnd) {
                // remove line break
                word = word.substring(0, word.length()-1);
            }
            int wordWidth = fm.stringWidth(word + (lineEnd ? "" : " ")); // get word width
            if (curX + wordWidth >= x + width) { // check if we need to do a line break
                curY += lineHeight;
                curX = x;
            }
            if (!word.equals("")) {
                maxX = Math.max(maxX, curX + wordWidth);
                if (draw) {
                    g.drawString(word, curX, curY);
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

    // helper - draw information to rectangle and also draw a connections curve (Bezier)
    // if a rectangle is hovered (selected)
    private void drawHelpContent(Graphics2D g2, String identifier) {
        if (displayRect != null) {
            // extract help identifier
            String help_text;
            if (langSelector.containsString(identifier)) {
                help_text = langSelector.getString(identifier);
            } else {
                help_text = langSelector.getString("help_overlay_no_help_available") + " (#" + identifier + ")";
            }
//            g2.drawRoundRect(displayRect.x, displayRect.y, displayRect.width, displayRect.height, 4, 4); // debug
            g2 = (Graphics2D) g2.create();
            // extract the dimension of the rect that drawing the string will take
            Rectangle rect = drawString(g2, help_text, displayRect.x, displayRect.y, displayRect.width, false);
            // compute the top left point for our info text
            int topX = displayRect.x + (displayRect.width - rect.width) / 2;
            int topY = displayRect.y + g2.getFontMetrics().getHeight()/2 + 30;
            // draw the Bezier curve (if activeRect is available) from activeRect to displayRect
            g2.setColor(VitcoSettings.HELP_OVERLAY_HIGHLIGHT_COLOR);
            drawBezier(g2, rect, topX, topY);
            // draw outline for text box
            g2.setStroke(new BasicStroke(2f));
            g2.setColor(new Color(0,0,0,100));
            g2.fillRoundRect(topX - 10, topY - 10, rect.width + 20, rect.height + 20, 15, 15);
            g2.setColor(VitcoSettings.HELP_OVERLAY_DEFAULT_COLOR);
            g2.drawRoundRect(topX - 10, topY - 10, rect.width + 20, rect.height + 20, 15, 15);
            // draw text into text box
            g2.setStroke(new BasicStroke(1f));
            drawString(g2, help_text, topX, topY + g2.getFontMetrics().getHeight()/2, displayRect.width, true);
            // dispose graphics element
            g2.dispose();
        }
    }

    // helper - draw Bezier curve
    private void drawBezier(Graphics2D g2, Rectangle2D rect, int topX, int topY) {
        if (activeRect != null) {
            g2.setStroke(new BasicStroke(2f));
            // compute the two rectangles that we want to connect
            // with the bezier curve
            int r1_left = topX - 10;
            int r1_top = topY - 10;
            int r1_right = (int) (topX + rect.getWidth() + 10);
            int r1_bottom = (int) (topY + rect.getHeight() + 10);
            int r1_center_x = (r1_left + r1_right)/2;
            int r1_center_y = (r1_top + r1_bottom)/2;

            int r2_left = activeRect.x;
            int r2_top = activeRect.y;
            int r2_right = activeRect.x + activeRect.width;
            int r2_bottom = activeRect.y + activeRect.height;
            int r2_center_x = (r2_left + r2_right)/2;
            int r2_center_y = (r2_top + r2_bottom)/2;

            // define the points and control points according to
            // the rectangle position (making sure they are drawn
            // from connecting sides)
            int p1x, p1y, p2x, p2y, c1x, c1y, c2x, c2y;
            if (r1_center_x < r2_center_x) {
                if (r1_center_y < r2_center_y) {
                    p1x = (r1_left + r1_right)/2;
                    p1y = r1_bottom;
                    p2x = r2_left;
                    p2y = (r2_top + r2_bottom)/2;
                    c1x = p1x;
                    c1y = p1y + 100;
                    c2x = p2x - 100;
                    c2y = p2y;
                } else {
                    p1x = r1_right;
                    p1y = (r1_bottom + r1_top)/2;
                    p2x = (r2_left + r2_right)/2;
                    p2y = r2_bottom;
                    c1x = p1x + 100;
                    c1y = p1y;
                    c2x = p2x;
                    c2y = p2y + 100;
                }
            } else {
                if (r1_center_y < r2_center_y) {
                    p1x = (r1_left + r1_right)/2;
                    p1y = r1_bottom;
                    p2x = r2_right;
                    p2y = (r2_top + r2_bottom)/2;
                    c1x = p1x;
                    c1y = p1y + 100;
                    c2x = p2x + 100;
                    c2y = p2y;
                } else {
                    p1x = r1_left;
                    p1y = (r1_bottom + r1_top)/2;
                    p2x = (r2_left + r2_right)/2;
                    p2y = r2_bottom;
                    c1x = p1x - 100;
                    c1y = p1y;
                    c2x = p2x;
                    c2y = p2y + 100;
                }
            }
            // draw the actual curve
            CubicCurve2D cubicCurve = new CubicCurve2D.Double(p1x, p1y, c1x, c1y, c2x, c2y, p2x, p2y);
            g2.draw(cubicCurve);
        }
    }

    // helper - draw known rectangles (and highlight selected)
    private void drawRects(Graphics2D g2) {
        for (CRectangle rect : rects) {
            if (rect.equals(activeRect)) {
                g2.setColor(VitcoSettings.HELP_OVERLAY_HIGHLIGHT_COLOR);
                g2.drawRoundRect(rect.x + 2, rect.y + 2, rect.width - 4, rect.height - 4, 4, 4);
                g2.setColor(VitcoSettings.HELP_OVERLAY_DEFAULT_COLOR);
            } else {
                g2.drawRoundRect(rect.x + 2, rect.y + 2, rect.width - 4, rect.height - 4, 4, 4);
            }
        }
    }

    // native paint method call
    @Override
    public void paint(Graphics g) {
        super.paint(g);
        // only draw information if this overlay is active
        // Note: this is necessary since the glasspane is also displayed when
        // the dockable frames are dragged
        if (isActive()) {
            // draw overlay
            Graphics2D g2 = (Graphics2D)g.create();
            // capture background (for transparency) if outdated
            if (image == null || image.getWidth() != thisInstance.getWidth() ||
                    image.getHeight() != thisInstance.getHeight()) {
                image = new BufferedImage(thisInstance.getWidth(), thisInstance.getHeight(), BufferedImage.TYPE_INT_RGB);
                frame.getContentPane().paint(image.getGraphics());
                // draw it a bit darker (to show that this is an overlay)
                Graphics gr = image.createGraphics();
                gr.setColor(new Color(0, 0, 0, 100));
                gr.fillRect(0, 0, this.getWidth(), this.getHeight());
                gr.dispose();
            }
            // draw static image (background)
            g2.drawImage(image, 0, 0, null);
            // prepare graphics settings
            g2.setRenderingHint(
                    RenderingHints.KEY_TEXT_ANTIALIASING,
                    RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            if (font != null) {
                g2.setFont(font);
            }
            g2.setColor(VitcoSettings.HELP_OVERLAY_DEFAULT_COLOR);
            g2.setStroke(new BasicStroke(2f));
            // paint all known rectangles
            drawRects(g2);
            // paint the help content using the active rectangle
            if (activeRect != null) {
                // a rectangle is hovered (selected)
                drawHelpContent(g2, "help_overlay_" + activeRect.getInfo());
            } else {
                // paint "root" of current help view if no sub-component is hovered
                Component comp = thisInstance.getRootPane().getParent();
                if (comp instanceof DockableFrame) {
                    // dockable frame is parent
                    drawHelpContent(g2, "help_overlay_window_" + ((DockableFrame) comp).getKey());
                } else {
                    // the entire window is parent
                    drawHelpContent(g2, "help_overlay_abtract_overview_information");
                }
            }
            // draw the closing button into the top right corner (this is just to give ppl something to
            // click as clicking anywhere will close the overlay)
            if (closeButton != null) {
                g2.drawImage(closeButton, this.getWidth() - closeButton.getWidth(null) - 10, 10, null);
            }
            // free the graphics element that we created
            g2.dispose();
        }
    }

}
