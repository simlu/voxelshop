package com.vitco.layout.bars;

import com.jidesoft.action.CommandMenuBar;
import com.jidesoft.swing.JideButton;
import com.vitco.manager.action.ActionManager;
import com.vitco.settings.VitcoSettings;
import com.vitco.util.misc.SaveResourceLoader;
import org.springframework.beans.factory.annotation.Autowired;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * the main menu, uses menu generator to load content from file
 *
 * defines interactions
 */
public class MainMenuLinkage extends BarLinkagePrototype {

    // true if the frame is currently maximized
    private boolean maximized = false;

    // fullscreen button
    private JideButton fullscreenButton;

    private final Icon enable_fullscreen = new SaveResourceLoader(
            "resource/img/icons/application/enable_fullscreen.png"
    ).asIconImage();
    private final Icon disable_fullscreen = new SaveResourceLoader(
            "resource/img/icons/application/disable_fullscreen.png"
    ).asIconImage();

    // var & setter
    private ActionManager actionManager;
    @Autowired
    public final void setActionManager(ActionManager actionManager) {
        this.actionManager = actionManager;
    }

    private void setMaximized(Frame frame, boolean state) {
        if (state) {
            // find out which screen we have the most overlap
            Rectangle maximizedBounds = null;
            int overlap = -1;
            Rectangle frameBounds = frame.getBounds();
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            for (GraphicsDevice gd : ge.getScreenDevices()) {
                GraphicsConfiguration defaultConfiguration = gd.getDefaultConfiguration();
                Insets screenInsets = Toolkit.getDefaultToolkit().getScreenInsets(defaultConfiguration);
                Rectangle usableScreenArea = defaultConfiguration.getBounds();
                Rectangle withoutTaskBarOnScreen = new Rectangle(
                        screenInsets.left,
                        screenInsets.top,
                        usableScreenArea.width - screenInsets.right,
                        usableScreenArea.height - screenInsets.bottom
                );
                Rectangle overlapRect = usableScreenArea.intersection(frameBounds);
                int cOverlap = overlapRect.width * overlapRect.height;
                if (overlap < cOverlap) {
                    maximizedBounds = withoutTaskBarOnScreen;
                    overlap = cOverlap;
                }
            }
            // restrict maximize to that screen (coordinates on the particular screen!)
            frame.setMaximizedBounds(maximizedBounds);
            frame.setExtendedState(frame.getExtendedState()|JFrame.MAXIMIZED_BOTH);
        } else {
            frame.setExtendedState(JFrame.NORMAL);
        }
    }

    private boolean isMaximized(Frame frame) {
        int state = frame.getExtendedState();
        return (state & JFrame.MAXIMIZED_BOTH) == JFrame.MAXIMIZED_BOTH;
    }

    @Override
    public CommandMenuBar buildBar(String key, final Frame frame) {
        final CommandMenuBar bar = new CommandMenuBar(key);

        // build the menu
        menuGenerator.buildMenuFromXML(bar, "com/vitco/layout/bars/main_menu.xml");

        // add buttons to the titlebar
        bar.addExpansion();
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setOpaque(false);
        JideButton minimize = new JideButton(new SaveResourceLoader(
                "resource/img/icons/application/minimize.png"
        ).asIconImage());
        minimize.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
        minimize.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                frame.setState(Frame.ICONIFIED);
            }
        });
        panel.add(minimize, BorderLayout.WEST);
        fullscreenButton = new JideButton();
        fullscreenButton.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
        fullscreenButton.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setMaximized(frame, !isMaximized(frame));
            }
        });
        panel.add(fullscreenButton, BorderLayout.CENTER);
        JideButton close = new JideButton(new SaveResourceLoader(
                "resource/img/icons/application/close.png"
        ).asIconImage());
        close.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 15));
        close.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                // execute closing action
                actionManager.performWhenActionIsReady("close_program_action", new Runnable() {
                    @Override
                    public void run() {
                        actionManager.getAction("close_program_action").actionPerformed(
                                new ActionEvent(e.getSource(), e.getID(), e.paramString())
                        );
                    }
                });
            }
        });
        panel.add(close, BorderLayout.EAST);
        bar.add(panel);

        // make borderless
        frame.setUndecorated(true);

        // make menu bar draggable
        new MoveMouseAdapter(bar, frame);

        // listen to border events
        new ResizeMouseAdapter(frame);

        // add listener to react to MAXIMIZED changes
        frame.addComponentListener(
                new ComponentAdapter() {
                    public void componentResized(ComponentEvent e) {
                        if (isMaximized(frame)) {
                            ((JFrame)frame).getRootPane().setBorder(BorderFactory.createEmptyBorder());
                            fullscreenButton.setIcon(disable_fullscreen);
                            maximized = true;
                        } else {
                            ((JFrame)frame).getRootPane().setBorder(VitcoSettings.FRAME_BORDER);
                            fullscreenButton.setIcon(enable_fullscreen);
                            maximized = false;
                        }
                    }
                }
        );

        // register the logic for this menu
        menuLogic.registerLogic(frame);

        return bar;
    }

    // listen to border events
    private final class ResizeMouseAdapter extends MouseAdapter {
        // the default mouse cursor
        private final Cursor DEFAULT_CURSOR = new Cursor(Cursor.DEFAULT_CURSOR);

        // border (outside and inside the rect)
        private static final int BORDER_DIST_DOUBLE = VitcoSettings.FRAME_BORDER_SIZE * 2;
        // border size outside (or inside) the rect
        private static final int BORDER_DIST = VitcoSettings.FRAME_BORDER_SIZE;

        // the related frame
        private final Frame frame;

        private final int locations[] = {
                SwingConstants.NORTH_WEST, SwingConstants.NORTH_EAST,
                SwingConstants.SOUTH_WEST, SwingConstants.SOUTH_EAST,
                SwingConstants.NORTH, SwingConstants.SOUTH,
                SwingConstants.WEST, SwingConstants.EAST
        };

        private final Cursor cursors[] = {
                new Cursor(Cursor.NW_RESIZE_CURSOR), new Cursor(Cursor.NE_RESIZE_CURSOR),
                new Cursor(Cursor.SW_RESIZE_CURSOR), new Cursor(Cursor.SE_RESIZE_CURSOR),
                new Cursor(Cursor.N_RESIZE_CURSOR), new Cursor(Cursor.S_RESIZE_CURSOR),
                new Cursor(Cursor.W_RESIZE_CURSOR), new Cursor(Cursor.E_RESIZE_CURSOR)
        };

        // constructor
        public ResizeMouseAdapter(Frame frame) {
            this.frame = frame;
            frame.addMouseListener(this);
            frame.addMouseMotionListener(this);
        }

        // helper - get the rectangle for border so we can check for containment
        private Rectangle getRectangle(int x, int y, int w, int h, int location) {
            switch (location) {
                case SwingConstants.NORTH:
                    return new Rectangle(x, y, w, BORDER_DIST_DOUBLE);
                case SwingConstants.SOUTH:
                    return new Rectangle(x, y + h - BORDER_DIST_DOUBLE, w, BORDER_DIST_DOUBLE);
                case SwingConstants.WEST:
                    return new Rectangle(x, y, BORDER_DIST_DOUBLE, h);
                case SwingConstants.EAST:
                    return new Rectangle(x + w - BORDER_DIST_DOUBLE, y, BORDER_DIST_DOUBLE, h);
                case SwingConstants.NORTH_WEST:
                    return new Rectangle(x, y, BORDER_DIST_DOUBLE, BORDER_DIST_DOUBLE);
                case SwingConstants.NORTH_EAST:
                    return new Rectangle(x + w - BORDER_DIST_DOUBLE, y, BORDER_DIST_DOUBLE, BORDER_DIST_DOUBLE);
                case SwingConstants.SOUTH_WEST:
                    return new Rectangle(x, y + h - BORDER_DIST_DOUBLE, BORDER_DIST_DOUBLE, BORDER_DIST_DOUBLE);
                case SwingConstants.SOUTH_EAST:
                    return new Rectangle(x + w - BORDER_DIST_DOUBLE, y + h - BORDER_DIST_DOUBLE, BORDER_DIST_DOUBLE, BORDER_DIST_DOUBLE);
            }
            return null;
        }

        // get the appropriate cursor for a request point (w.r.t. drag type)
        public final Cursor getCursor(Point p) {
            if (!maximized) {
                for (int i = 0; i < locations.length; i++) {
                    Rectangle rect = getRectangle(
                            -BORDER_DIST, -BORDER_DIST,
                            frame.getWidth() + BORDER_DIST_DOUBLE, frame.getHeight() + BORDER_DIST_DOUBLE,
                            locations[i]);
                    if (rect.contains(p)) {
                        return cursors[i];
                    }
                }
            }
            return DEFAULT_CURSOR;
        }

        // get the appropriate drag type for a request point
        public final int getDragType(Point p) {
            if (!maximized) {
                for (int location : locations) {
                    Rectangle rect = getRectangle(
                            -BORDER_DIST, -BORDER_DIST,
                            frame.getWidth() + BORDER_DIST_DOUBLE, frame.getHeight() + BORDER_DIST_DOUBLE,
                            location);
                    if (rect.contains(p)) {
                        return location;
                    }
                }
            }
            return -1;
        }

        // ============

        // current drag type
        private int dragType = -1;
        // current drag position
        private Point pos = new Point(0, 0);

        // get current location on screen
        private Point getScreenLocation(MouseEvent e) {
            Point cursor = e.getPoint();
            Point target_location = frame.getLocationOnScreen();
            return new Point((int) (target_location.getX() + cursor.getX()),
                    (int) (target_location.getY() + cursor.getY()));
        }

        @Override
        public void mousePressed(MouseEvent e) {
            super.mousePressed(e);
            // initialize position and drag type
            pos = this.getScreenLocation(e);
            dragType = getDragType(e.getPoint());
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            super.mouseDragged(e);
            // executing dragging event (resize)
            if (dragType != -1) {
                Point newPos = this.getScreenLocation(e);
                int xdiff = newPos.x - pos.x;
                int ydiff = newPos.y - pos.y;
                switch (dragType) {
                    case SwingConstants.NORTH_WEST:
                        frame.setBounds(frame.getX() + xdiff,
                                frame.getY() + ydiff,
                                frame.getWidth() - xdiff,
                                frame.getHeight() - ydiff);
                        break;
                    case SwingConstants.NORTH_EAST:
                        frame.setBounds(frame.getX(),
                                frame.getY() + ydiff,
                                frame.getWidth() + xdiff,
                                frame.getHeight() - ydiff);
                        break;
                    case SwingConstants.SOUTH_WEST:
                        frame.setBounds(frame.getX() + xdiff,
                                frame.getY(),
                                frame.getWidth() - xdiff,
                                frame.getHeight() + ydiff);
                        break;
                    case SwingConstants.SOUTH_EAST:
                        frame.setBounds(frame.getX(),
                                frame.getY(),
                                frame.getWidth() + xdiff,
                                frame.getHeight() + ydiff);
                        break;
                    case SwingConstants.NORTH:
                        frame.setBounds(frame.getX(),
                                frame.getY() + ydiff,
                                frame.getWidth(),
                                frame.getHeight() - ydiff);
                        break;
                    case SwingConstants.SOUTH:
                        frame.setBounds(frame.getX(),
                                frame.getY(),
                                frame.getWidth(),
                                frame.getHeight() + ydiff);
                        break;
                    case SwingConstants.WEST:
                        frame.setBounds(frame.getX() + xdiff,
                                frame.getY(),
                                frame.getWidth() - xdiff,
                                frame.getHeight());
                        break;
                    case SwingConstants.EAST:
                        frame.setBounds(frame.getX(),
                                frame.getY(),
                                frame.getWidth() + xdiff,
                                frame.getHeight());
                        break;
                    default:
                        frame.setBounds(frame.getX() + xdiff,
                                frame.getY() + ydiff,
                                frame.getWidth(),
                                frame.getHeight());
                        break;
                }
                pos = newPos;
            }
        }

        @Override
        public void mouseMoved(MouseEvent e) {
            super.mouseMoved(e);
            if (!maximized) {
                frame.setCursor(getCursor(e.getPoint()));
            }
        }

        @Override
        public void mouseExited(MouseEvent e) {
            super.mouseExited(e);
            frame.setCursor(DEFAULT_CURSOR);
        }
    }

    // helper class to drag frame
    private final class MoveMouseAdapter extends MouseAdapter {
        // the frame that is dragged
        private final Frame frame;
        // the component that is used for dragging
        private JComponent target;
        // locations
        private Point start_drag;
        private Point start_loc;

        // constructor
        public MoveMouseAdapter(JComponent target, Frame frame) {
            this.target = target;
            this.frame = frame;
            // add listener
            target.addMouseListener(this);
            target.addMouseMotionListener(this);
        }

        // --------

        // get current location on screen
        private Point getScreenLocation(MouseEvent e) {
            Point cursor = e.getPoint();
            Point target_location = this.target.getLocationOnScreen();
            return new Point((int) (target_location.getX() + cursor.getX()),
                    (int) (target_location.getY() + cursor.getY()));
        }

        // initialize locations
        public void mousePressed(MouseEvent e) {
            this.start_drag = this.getScreenLocation(e);
            this.start_loc = frame.getLocation();
        }

        public void mouseReleased(MouseEvent e) {
            // change maximized state on dbl click
            if (e.getClickCount() == 2) {
                setMaximized(frame, !isMaximized(frame));
            }
        }

        // update frame
        public void mouseDragged(MouseEvent e) {
            // no longer maximised
            if (isMaximized(frame)) {
                setMaximized(frame, false);
            }
            // update position
            Point current = this.getScreenLocation(e);
            Point offset = new Point(
                    current.x - start_drag.x,
                    current.y - start_drag.y
            );
            Point new_location = new Point(
                    this.start_loc.x + offset.x,
                    this.start_loc.y + offset.y
            );
            frame.setLocation(new_location);
        }
    }

}
