package com.vitco.logic.colorpicker;

import com.vitco.engine.data.Data;
import com.vitco.res.VitcoSettings;
import com.vitco.util.ColorTools;
import com.vitco.util.action.ActionManager;
import com.vitco.util.error.ErrorHandlerInterface;
import com.vitco.util.pref.PrefChangeListener;
import com.vitco.util.pref.PreferencesInterface;
import com.vitco.util.thread.LifeTimeThread;
import com.vitco.util.thread.ThreadManagerInterface;
import org.springframework.beans.factory.annotation.Autowired;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.basic.BasicSliderUI;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;

/**
 * Builds the color picker.
 */
public class ColorPickerView implements ColorPickerViewInterface {

    // var & setter
    protected PreferencesInterface preferences;
    @Autowired(required=true)
    public final void setPreferences(PreferencesInterface preferences) {
        this.preferences = preferences;
    }

    // var & setter
    private ErrorHandlerInterface errorHandler;
    @Autowired
    public final void setErrorHandler(ErrorHandlerInterface errorHandler) {
        this.errorHandler = errorHandler;
    }

    // var & setter (can not be interface!!)
    protected Data data;
    @Autowired
    public final void setData(Data data) {
        this.data = data;
    }

    // var & setter
    private ActionManager actionManager;
    @Autowired
    public final void setActionManager(ActionManager actionManager) {
        this.actionManager = actionManager;
    }

    private ThreadManagerInterface threadManager;
    // set the action handler
    @Override
    @Autowired
    public final void setThreadManager(ThreadManagerInterface threadManager) {
        this.threadManager = threadManager;
    }

    // image that serves as a buffer for the color picker panel background
    private BufferedImage image = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
    // the current brightness
    private float hue = 1;

    private static final int HUE_STEPS = 1000;

    // currently selected color
    private float[] currentColor = ColorTools.colorToHSB(VitcoSettings.INITIAL_CURRENT_COLOR);

    // manages a complete repaint of the image buffer (several iterations)
    // and then stops itself (can also be stopped from outside)
    private final class PaintThread extends LifeTimeThread {
        // get some needed components/settings
        private static final int SUBFRAMES = 30;
        final int width = image.getWidth() - 1;
        final int height = image.getHeight() - 1;
        int curPos = 0;
        final Graphics2D ig = (Graphics2D)image.getGraphics();
        final int count = width/SUBFRAMES + 1;

        // main work, this will loop until stopped (external or internal)
        @Override
        public void loop() throws InterruptedException {
            for (int i=0; i<SUBFRAMES; i++) {
                int pos = (curPos + count*i);
                ig.setPaint(new GradientPaint(
                        pos, 0,
                        Color.getHSBColor(hue, (pos/(float)width), 1),
                        pos, height,
                        Color.getHSBColor(hue, (pos/(float)width), 0),
                        false));
                ig.fillRect(pos, 0, count - curPos, height);
            }

            panel.repaint();
            curPos++;

            if (curPos < count) {
                sleep(100);
            } else { // stop when finished
                stopThread();
            }

        }
    }

    // panel makes sure everything is up-to-date and repaints (image + cross position)
    private final MPanel panel = new MPanel();
    private final class MPanel extends JPanel {
        // the current position of the selected color
        private Point crossPosition = new Point(0,0);
        // to check if the currentColor has changed
        private float[] prevCurrentColor = new float[] {-1, -1, -1};
        @Override
        protected final void paintComponent(Graphics g1) {
            if (currentColor[0] != prevCurrentColor[0] || currentColor[1] != prevCurrentColor[1] || currentColor[2] != prevCurrentColor[2]) {
                crossPosition = new Point(
                        Math.round(currentColor[1]*panel.getWidth()),
                        Math.round((1-currentColor[2])*panel.getHeight())
                );
                hue = currentColor[0];
                slider.setValue(Math.round(hue * HUE_STEPS));
                prevCurrentColor = currentColor.clone();
            }

            Graphics2D ig = (Graphics2D) g1;
            // draw colors
            ig.drawImage(image, 0, 0, null);
            // Anti-alias
            ig.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            // draw cirlce (selected color)
            ig.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL)); // line size
            ig.setColor(ColorTools.perceivedBrightness(currentColor) > 127 ? Color.BLACK : Color.WHITE);
            ig.drawOval(crossPosition.x - 5, crossPosition.y - 5, 10, 10);

        }
    }

    // adapter for resize and slider change
    // manages the redrawing of the buffer image
    private final Adapter adapter = new Adapter();
    private final class Adapter extends ComponentAdapter implements ChangeListener {

        PaintThread paintThread = new PaintThread();

        // initializes a complete redraw of the buffer image
        private void computeColorPicker() {
            threadManager.remove(paintThread);
            paintThread.stopThread();
            paintThread = new PaintThread();
            threadManager.manage(paintThread);
        }

        @Override
        public void componentResized(ComponentEvent e) {
            image = new BufferedImage(e.getComponent().getWidth(), e.getComponent().getHeight(), BufferedImage.TYPE_INT_RGB);
            computeColorPicker();
        }

        // change of brightness
        @Override
        public void stateChanged(ChangeEvent e) {
            JSlider source = (JSlider) e.getSource();
            if (!source.getValueIsAdjusting()) {
                hue = source.getValue()/(float) HUE_STEPS;
                preferences.storeObject("currently_used_color", new float[] {hue, currentColor[1], currentColor[2]});
                computeColorPicker();
                panel.repaint();
            }
        }
    }

    // set the current color when clicked click
    private final MAdapter mouseAdapter = new MAdapter();
    private final class MAdapter extends MouseAdapter {
        private void updateCurrentColor(Point pos) {
            float[] val = new float[] {
                    Math.max(0, Math.min(1, hue)),
                    Math.max(0, Math.min(1, (float)((pos.getX() / (double) panel.getWidth())))),
                    Math.max(0, Math.min(1, 1-(float)((pos.getY() / (double) panel.getHeight()))))
            };
            preferences.storeObject("currently_used_color", val);
        }

        private void setColor(Point point) {
            updateCurrentColor(point);
        }

        @Override
        public void mousePressed(MouseEvent e) {
            setColor(e.getPoint());
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            Point point = new Point(
                    Math.max(0, Math.min(panel.getWidth()-1, e.getX())),
                    Math.max(0, Math.min(panel.getHeight()-1, e.getY()))
            );
            setColor(point);
        }
    }

    // Create the slider
    final JSlider slider = new JSlider(JSlider.VERTICAL, 0, HUE_STEPS, Math.round(hue * HUE_STEPS));

    @Override
    public final JPanel build() {
        final JPanel wrapper = new JPanel();
        wrapper.setLayout(new BorderLayout());
        wrapper.setBorder(BorderFactory.createLineBorder(VitcoSettings.DEFAULT_BORDER_COLOR));

        // ===========
        // initialize the robot (for global color picker)
        Robot tmp = null;
        try {
            tmp = new Robot();
        } catch (AWTException e) {
            errorHandler.handle(e);
        }
        final Robot robot = tmp;
        // register global color picker action
        actionManager.registerAction("pick_color_under_mouse_as_current_color", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (robot != null) {
                    Point mousePosition = MouseInfo.getPointerInfo().getLocation();
                    preferences.storeObject("currently_used_color",
                            ColorTools.colorToHSB(robot.getPixelColor(mousePosition.x, mousePosition.y)));
                }
            }
        });
        // ===========

        // prepare panel events
        panel.addComponentListener(adapter);
        panel.addMouseListener(mouseAdapter);
        panel.addMouseMotionListener(mouseAdapter);

        // register change of current color
        preferences.addPrefChangeListener("currently_used_color", new PrefChangeListener() {
            @Override
            public void onPrefChange(Object newValue) {
                currentColor = (float[])newValue;
                panel.repaint();
            }
        });

        wrapper.add(panel, BorderLayout.CENTER);

        // slider settings
        slider.setPreferredSize(new Dimension(25, slider.getPreferredSize().height));
        slider.setBackground(VitcoSettings.COLOR_PICKER_SLIDER_KNOB_COLOR);
        final BasicSliderUI sliderUI = new BasicSliderUI(slider) {

            BufferedImage bgBuffer = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
            BufferedImage thumbBuffer = null;
            Point prevContentRect = new Point(0,0);

            private final static int SIZE = 5;
            //private final int INNER_WIDTH = 9;

            @Override
            public void paint(Graphics g, JComponent c) {
                super.paint(g, c);

                // only generate background on resize
                if (prevContentRect.x != contentRect.width || prevContentRect.y != contentRect.height) {
                    prevContentRect = new Point(contentRect.width, contentRect.height);
                    int w = slider.getWidth();
                    int h = slider.getHeight();
                    bgBuffer = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
                    Graphics2D ig = (Graphics2D)bgBuffer.getGraphics();
                    ig.setColor(Color.GRAY);
                    ig.fillRect(0, 0, w, h);

                    for (int i = 0; i < h; i++) {
                        // 1 - (float)(i-7) / (height - 15)
                        ig.setColor(ColorTools.hsbToColor(new float[] {(float)valueForYPosition(i)/ HUE_STEPS, 1, 1}));
                        ig.drawLine(1, i, w, i);
                    }

                    ig.setColor(VitcoSettings.DEFAULT_BORDER_COLOR);
                    ig.drawLine(0, 0, 0, h);
                }

                // draw the background
                g.drawImage(bgBuffer, 0, 0, null);

                if (g.getClipBounds().intersects(thumbRect)) {
                    // make sure the thumbRect covers the whole width
                    thumbRect.x = 0;
                    thumbRect.width = slider.getWidth();
                    // only create the thumb once
                    if (thumbBuffer == null) {
                        thumbBuffer = new BufferedImage(slider.getWidth(), 11, BufferedImage.TYPE_INT_ARGB);
                        Graphics2D ig = (Graphics2D)thumbBuffer.getGraphics();
                        // Anti-alias
                        ig.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                RenderingHints.VALUE_ANTIALIAS_ON);


                        ig.setColor(Color.BLACK);
                        ig.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL)); // line size
                        ig.drawRect(1, SIZE - 4, slider.getWidth() - 2, 8);
                        ig.setColor(Color.WHITE);
                        ig.setStroke(new BasicStroke(1, BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL)); // line size
                        ig.drawRect(1, SIZE - 4, slider.getWidth() - 2, 8);

//                        ig.setStroke(new BasicStroke(0.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL)); // line size
//                        int[] yPos = new int[] {0, SIZE, SIZE*2};
//                        ig.setColor(Color.WHITE);
//                        ig.fillPolygon( new int[] {1, INNER_WIDTH, 1}, yPos, 3);
//                        ig.setColor(Color.BLACK);
//                        ig.drawPolygon( new int[] {1, INNER_WIDTH, 1}, yPos, 3);
//
//                        ig.setColor(Color.WHITE);
//                        ig.fillPolygon( new int[] {slider.getWidth() - 1, slider.getWidth() - INNER_WIDTH, slider.getWidth() - 1}, yPos, 3);
//                        ig.setColor(Color.BLACK);
//                        ig.drawPolygon( new int[] {slider.getWidth() - 1, slider.getWidth() - INNER_WIDTH, slider.getWidth() - 1}, yPos, 3);
                    }
                    // draw the thumb
                    g.drawImage(thumbBuffer, 0, yPositionForValue(slider.getValue()) - SIZE, null);
                }
            }
        };
        // move the thumb to the position we pressed instantly
        slider.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                slider.setValue(sliderUI.valueForYPosition(e.getY()));
                slider.repaint();
            }
        });
        slider.setUI(sliderUI);
        slider.addChangeListener(adapter);

        wrapper.add(slider, BorderLayout.EAST);

        return wrapper;
    }

}
