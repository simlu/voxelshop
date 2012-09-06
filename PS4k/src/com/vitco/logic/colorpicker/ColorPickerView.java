package com.vitco.logic.colorpicker;

import com.vitco.engine.data.Data;
import com.vitco.engine.data.notification.DataChangeAdapter;
import com.vitco.res.VitcoSettings;
import com.vitco.util.pref.PreferencesInterface;
import com.vitco.util.thread.LifeTimeThread;
import com.vitco.util.thread.ThreadManagerInterface;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
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
    public void setPreferences(PreferencesInterface preferences) {
        this.preferences = preferences;
    }

    // var & setter (can not be interface!!)
    protected Data data;
    @Autowired
    public void setData(Data data) {
        this.data = data;
    }

    private ThreadManagerInterface threadManager;
    // set the action handler
    @Override
    @Autowired
    public void setThreadManager(ThreadManagerInterface threadManager) {
        this.threadManager = threadManager;
    }

    // image that serves as a buffer for the color picker panel background
    private BufferedImage image = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
    // the current brightness
    private float brightness = 1;
    // the current position of the selected color
    private Point crossPosition = new Point(0,0);

    // manages a complete repaint of the image buffer (several iterations)
    // and then stops itself (can also be stopped from outside)
    private final class PaintThread extends LifeTimeThread {
        // get some needed components/settings
        private static final int SUBFRAMES = 30;
        final int width = image.getWidth();
        final int height = image.getHeight();
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
                        Color.getHSBColor((pos/(float)width), 1, brightness),
                        pos, height,
                        Color.getHSBColor((pos/(float)width), 0, brightness),
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
        private Color colorRef = null;

        @Override
        protected final void paintComponent(Graphics g1) {
            Color color = data.getCurrentColor();
            if (color != colorRef) {
                float[] val = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null);
                crossPosition = new Point(
                        Math.round(val[0]*panel.getWidth()),
                        Math.round((1-val[1])*panel.getHeight())
                );
                brightness = val[2];
                slider.setValue(Math.round(brightness*255));
                colorRef = color;
            }

            Graphics2D ig = (Graphics2D) g1;
            ig.drawImage(image, 0, 0, null); // draw colors

            // draw cross
            ig.setColor(brightness > 0.7 ? Color.BLACK : Color.WHITE);
            ig.drawLine(crossPosition.x - 4, crossPosition.y, crossPosition.x + 4, crossPosition.y);
            ig.drawLine(crossPosition.x, crossPosition.y - 4, crossPosition.x, crossPosition.y + 4);

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
                brightness = source.getValue()/(float)255;
                Color color = data.getCurrentColor();
                float[] val = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null);
                data.setCurrentColor(Color.getHSBColor(val[0],val[1],brightness));
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
                    Math.max(0, Math.min(1, (float)((pos.getX() / (double) panel.getWidth())))),
                    Math.max(0, Math.min(1, 1-(float)((pos.getY() / (double) panel.getHeight())))),
                    Math.max(0, Math.min(1, brightness))
            };
            data.setCurrentColor(Color.getHSBColor(val[0], val[1], val[2]));
        }

        private void setColor(MouseEvent e) {
            updateCurrentColor(e.getPoint());
            crossPosition = e.getPoint();
        }

        @Override
        public void mousePressed(MouseEvent e) {
            setColor(e);
        }
    }

    // Create the slider
    final JSlider slider = new JSlider(JSlider.VERTICAL, 0, 255, Math.round(brightness*255));

    @Override
    public final JPanel build() {
        final JPanel wrapper = new JPanel();
        wrapper.setLayout(new BorderLayout());
        wrapper.setBorder(BorderFactory.createLineBorder(VitcoSettings.DEFAULT_BORDER_COLOR));

        // prepare panel events
        panel.addComponentListener(adapter);
        panel.addMouseListener(mouseAdapter);
        panel.addMouseMotionListener(mouseAdapter);

        // reset (redraw) when color data has changed
        data.addDataChangeListener(new DataChangeAdapter() {
            @Override
            public void onColorDataChanged() {
                panel.repaint();
            }
        });

        wrapper.add(panel, BorderLayout.CENTER);

        // slider settings
        slider.setPreferredSize(new Dimension(20, slider.getPreferredSize().height));
        slider.setBackground(VitcoSettings.COLOR_PICKER_SLIDER_KNOB_COLOR);
        slider.setUI(new BasicSliderUI(slider) {
            @Override
            public void paint(Graphics g, JComponent c) {
                super.paint(g, c);

                Graphics2D ig = (Graphics2D)g;
                ig.setPaint(new GradientPaint(
                        1, 1,
                        Color.WHITE,
                        1, slider.getHeight()-2,
                        Color.BLACK,
                        false));
                ig.fillRect(1, 0, slider.getWidth(), slider.getHeight());
                ig.setColor(VitcoSettings.DEFAULT_BORDER_COLOR);
                ig.drawLine(0, 0, 0, slider.getHeight());

                if (g.getClipBounds().intersects(thumbRect)) {
                    paintThumb(g);
                }
            }
        });
        slider.addChangeListener(adapter);

        wrapper.add(slider, BorderLayout.EAST);

        return wrapper;
    }

    @Override
    @PostConstruct
    public void init() { // load color from pref
        if (preferences.contains("previous_current_color")) {
            data.setCurrentColor((Color) preferences.loadObject("previous_current_color"));
        }
    }

    @Override
    @PreDestroy
    public void finish() { // store color to pref
        preferences.storeObject("previous_current_color", data.getCurrentColor());
    }

}
