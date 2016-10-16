package com.vitco.layout;

import com.jidesoft.action.CommandMenuBar;
import com.jidesoft.action.DockableBar;
import com.jidesoft.action.DockableBarFactory;
import com.jidesoft.action.DockableBarManager;
import com.jidesoft.docking.*;
import com.jidesoft.docking.event.DockableFrameAdapter;
import com.jidesoft.docking.event.DockableFrameEvent;
import com.jidesoft.swing.LayoutPersistence;
import com.vitco.core.data.Data;
import com.vitco.layout.bars.BarLinkagePrototype;
import com.vitco.layout.content.shortcut.ShortcutManagerInterface;
import com.vitco.layout.frames.FrameLinkagePrototype;
import com.vitco.layout.frames.custom.CDockableFrame;
import com.vitco.manager.action.ActionManager;
import com.vitco.manager.action.ComplexActionManager;
import com.vitco.manager.action.types.StateActionPrototype;
import com.vitco.manager.error.ErrorHandlerInterface;
import com.vitco.manager.help.FrameHelpOverlay;
import com.vitco.manager.lang.LangSelectorInterface;
import com.vitco.manager.pref.PreferencesInterface;
import com.vitco.settings.VitcoSettings;
import com.vitco.util.misc.SaveResourceLoader;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.swing.*;
import javax.swing.plaf.InsetsUIResource;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;

/*
 * Manages the creation of the main window.
 *
 * Defers dealing with the content of frames and bars to the in config.xml
 * configured classes.
 */

public class WindowManager extends ExtendedDockableBarDockableHolder implements WindowManagerInterface {

    // maps the bars to the linkage class that deals with them
    private Map<String, BarLinkagePrototype> barLinkageMap;
    // set the map
    @Override
    public final void setBarLinkageMap(Map<String, BarLinkagePrototype> map) {
        this.barLinkageMap = map;
    }

    // var & setter
    private ErrorHandlerInterface errorHandler;
    @Override
    public final void setErrorHandler(ErrorHandlerInterface errorHandler) {
        this.errorHandler = errorHandler;
    }

    // var & setter (can not be interface!!)
    protected Data data;
    @Override
    public final void setData(Data data) {
        this.data = data;
    }

    // var & setter
    private PreferencesInterface preferences;
    @Override
    public final void setPreferences(PreferencesInterface preferences) {
        this.preferences = preferences;
    }

    // maps the frames to the linkage class that deals with them
    private Map<String, FrameLinkagePrototype> frameLinkageMap;
    // set the map
    @Override
    public final void setFrameLinkageMap(Map<String, FrameLinkagePrototype> map) {
        this.frameLinkageMap = map;
    }

    // to hook the shortcut manager to the frames
    private ShortcutManagerInterface shortcutManager;
    @Override
    public final void setShortcutManager(ShortcutManagerInterface shortcutManager) {
        this.shortcutManager = shortcutManager;
    }

    private ActionManager actionManager;
    // set the action handler
    @Override
    public final void setActionManager(ActionManager actionManager) {
        this.actionManager = actionManager;
    }

    private ComplexActionManager complexActionManager;
    // set the complex action handler
    @Override
    public final void setComplexActionManager(ComplexActionManager complexActionManager) {
        this.complexActionManager = complexActionManager;
    }

    // var & setter
    protected LangSelectorInterface langSelector;
    @Override
    public final void setLangSelector(LangSelectorInterface langSelector) {
        this.langSelector = langSelector;
    }

    // reference to this instance
    final JFrame thisFrame = this;

    // counts how often the program was started
    private int start_count = 0;

    // prepare all frames
    @Override
    public final CDockableFrame prepareFrame(final String key) {
        CDockableFrame frame = null;
        if (frameLinkageMap.containsKey(key)) {
            frame = frameLinkageMap.get(key).buildFrame(key, thisFrame);
            // add help overlay (this is only used if this frame is floated!)
            JRootPane rootPane = frame.getRootPane();
            final FrameHelpOverlay overlay = new FrameHelpOverlay(rootPane, actionManager, complexActionManager, langSelector);
            // add help button
            final DockableFrame finalFrame = frame;
            AbstractAction action = new AbstractAction("help", new SaveResourceLoader("resource/img/icons/frame_help_button_icon.png").asIconImage()) {
                @Override
                public void actionPerformed(final ActionEvent e) {
                    if (finalFrame.isFloated()) {
                        // for this sub-frame (if floated only)
                        overlay.setActive(!overlay.isActive());
                    } else {
                        // show help for entire window (if docked)
                        actionManager.performWhenActionIsReady("show_help_overlay", new Runnable() {
                            @Override
                            public void run() {
                                actionManager.getAction("show_help_overlay").actionPerformed(
                                        new ActionEvent(e.getSource(), e.getID(), e.paramString())
                                );
                            }
                        });
                    }
                }
            };
            frame.setHelpAction(action);
            action.putValue(AbstractAction.SHORT_DESCRIPTION, "Help"); // tooltip
            frame.addAdditionalButtonActions(action);
            // register the shortcuts for this frame
            shortcutManager.registerFrame(frame);
        } else {
            System.err.println("Error: No linkage class defined for frame \"" + key + "\"");
        }

        return frame;
    }

    // prepare all bars
    @Override
    public final CommandMenuBar prepareBar(String key) {
        CommandMenuBar bar = null;
        if (barLinkageMap.containsKey(key)) {
            bar = barLinkageMap.get(key).buildBar(key, thisFrame);
        } else {
            System.err.println("Error: No linkage class defined for bar \"" + key + "\"");
        }

        return bar;
    }

    // constructor
    public WindowManager() throws HeadlessException {
        super(VitcoSettings.TITLE_STRING);
        // save the state on exit of the program
        // this needs to be done BEFORE the window is closing
        addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosing(final WindowEvent e) {
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

    }

    // handle cursor (global)
    @Override
    public final void setCustomCursor(Cursor cursor) {
        for (String frameName : getDockingManager().getAllFrames()) {
            getDockingManager().getFrame(frameName).setCursor(cursor);
        }
        for (DockableBar dockableBar : getDockableBarManager().getAllDockableBars()) {
            dockableBar.setCursor(cursor);
        }
    }

    @PreDestroy
    @Override
    public final void finish() {
        // store the boundary of the program (current window position)
        preferences.storeObject("program_boundary_rect", this.getBounds());
        // store the startcount + 1
        preferences.storeInteger("program_start_count", start_count+1);
    }

    // handle borderless logic (make floated windows borderless)
    private void handleBorderLess(final DockingManager dockingManager) {
        // list of managed floating containers
        final HashSet<DialogFloatingContainer> containers = new HashSet<DialogFloatingContainer>();
        // the last mouse position (used to determine which container(s) need borders)
        final Point lastMousePos = new Point(0,0);
        // check if ctrl is currently pressed
        final boolean[] ctrlDown = new boolean[] {false};
        // the container that currently has a border
        final DialogFloatingContainer[] activeContainer = {null};

        // wrapper action to refresh the container borders
        final AbstractAction refreshBorderAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // loop over all managed containers
                for (DialogFloatingContainer container : containers) {
                    // check if we need to show/hide the border and title
                    boolean showBorder = ctrlDown[0] && container.getBounds().contains(lastMousePos);
                    // nested check
                    for (Component child : container.getContentPane().getComponents()) {
                        if (child instanceof ContainerContainer) {
                            Component[] comps = ((ContainerContainer) child).getComponents();
                            if (comps.length > 0) {
                                if (comps[0] instanceof FrameContainer) {
                                    for (Component comp : ((FrameContainer) comps[0]).getComponents()) {
                                        if (comp instanceof DockableFrame) {
                                            // show/hide title bar
                                            ((DockableFrame) comp).setShowTitleBar(showBorder);
                                            // show/hide resize border (different border depending if active or inactive)
                                            container.setBorder(showBorder
                                                    ? VitcoSettings.FLOATING_FRAME_BORDER
                                                    : BorderFactory.createEmptyBorder());
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        };

        // display the titlebar of frames when frame is docked and hide it when its floated
        // also handle border color change when active frame changes
        dockingManager.addDockableFrameListener(new DockableFrameAdapter() {
            @Override
            public void dockableFrameDocked(DockableFrameEvent dockableFrameEvent) {
                dockableFrameEvent.getDockableFrame().setShowTitleBar(true);
            }

            @Override
            public void dockableFrameFloating(DockableFrameEvent dockableFrameEvent) {
                dockableFrameEvent.getDockableFrame().setShowTitleBar(false);
            }

            @Override
            public void dockableFrameActivated(DockableFrameEvent dockableFrameEvent) {
                // call this to update the border color
                if (ctrlDown[0]) {
                    refreshBorderAction.actionPerformed(null);
                    // null the active container
                    activeContainer[0] = null;
                }
            }
        });

        // listen to mouse events
        Toolkit.getDefaultToolkit().addAWTEventListener(new AWTEventListener() {
            @Override
            public void eventDispatched(AWTEvent event) {
                if (event.getID() == MouseEvent.MOUSE_ENTERED) {
                    // remember the location
                    lastMousePos.setLocation(MouseInfo.getPointerInfo().getLocation());
                    if (ctrlDown[0]) {
                        // check which container is active
                        for (DialogFloatingContainer container : containers) {
                            if (container.getBounds().contains(lastMousePos)) {
                                if (activeContainer[0] != container) {
                                    // a new container is now active, we need to update
                                    activeContainer[0] = container;
                                    refreshBorderAction.actionPerformed(null);
                                }
                            }
                        }
                    }
                }
            }
        }, AWTEvent.MOUSE_EVENT_MASK);

        // handle keyboard events (global)
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(new KeyEventDispatcher() {

            @Override
            public boolean dispatchKeyEvent(final KeyEvent e) {
                // listen to ctrl events
                if (ctrlDown[0] != e.isControlDown()) {
                    ctrlDown[0] = !ctrlDown[0];
                    // update border state
                    refreshBorderAction.actionPerformed(null);
                    activeContainer[0] = null;
                }
                return false;
            }
        });

        // remove border from all FloatingContainer and store reference for dynamically changing them
        dockingManager.setFloatingContainerCustomizer(new DockingManager.FloatingContainerCustomizer() {
            @Override
            public void customize(FloatingContainer fc) {
                final DialogFloatingContainer dialogFloatingContainer = ((DialogFloatingContainer) fc);
                // always make sure this has no border
                dialogFloatingContainer.setBorder(BorderFactory.createEmptyBorder());
                // register container
                containers.add(dialogFloatingContainer);
            }
        });

        // toggle "highlight active window" setting
        actionManager.registerAction("toggle_active_window_highlighted", new StateActionPrototype() {
            @Override
            public void action(ActionEvent actionEvent) {
                CDockableFrame.setActiveWindowHighlighted(!CDockableFrame.isActiveWindowHighlighted());
                preferences.storeBoolean("use_highlight_active_window", CDockableFrame.isActiveWindowHighlighted());
                for (String frame : dockingManager.getAllFrames()) {
                    dockingManager.getFrame(frame).setBorder(null);
                }
            }

            @Override
            public boolean getStatus() {
                return CDockableFrame.isActiveWindowHighlighted();
            }
        });

        // remove extra spacing of frames
        UIManager.getDefaults().put("FrameContainer.contentBorderInsets", new InsetsUIResource(0, 0, 0, 0));

        // listen to main frame resize events and make sure the floated windows do not
        // disappear outside the frame area (prevent windows from disappearing)
        thisFrame.addComponentListener(new ComponentAdapter() {
            Integer xOld = thisFrame.getX();
            Integer yOld = thisFrame.getY();

            // validate the floated window position to be contained inside main jframe
            private void validateWindow(DialogFloatingContainer container) {
                container.setLocation(
                        Math.max(thisFrame.getX() + 20, Math.min(thisFrame.getX() + thisFrame.getWidth() - container.getWidth() - 20, container.getLocation().x)),
                        Math.max(thisFrame.getY() + 20, Math.min(thisFrame.getY() + thisFrame.getHeight() - container.getHeight() - 20, container.getLocation().y))
                );
            }

            @Override
            public void componentResized(ComponentEvent e) {
                // make sure all floating containers are "on main JFrame"
                for (DialogFloatingContainer container : containers) {
                    validateWindow(container);
                }
            }

            @Override
            public void componentMoved(ComponentEvent e) {
                // move all DialogFloatingContainer with this main JFrame
                int x = thisFrame.getX();
                int y = thisFrame.getY();
                for (DialogFloatingContainer container : containers) {
                    Point oldPosition = container.getLocation();
                    container.setLocation(oldPosition.x + (x - xOld), oldPosition.y + (y - yOld));
                }
                xOld = x;
                yOld = y;
            }
        });
    }

    // set the divider size for any ContainerContainerDivider contained in hierarchy
    private static void setDividerSizeDeep(Container component, int value) {
        ArrayList<Component> components = new ArrayList<Component>();
        components.add(component);
        while (!components.isEmpty()) {
            Component com = components.remove(0);
            if (com instanceof ContainerContainerDivider) {
                ((ContainerContainerDivider)com).setDividerSize(value);
            }
            if (com instanceof Container) {
                Collections.addAll(components, ((Container) com).getComponents());
            }
        }
    }

    @PostConstruct
    @Override
    public final void init() {
        if (preferences.contains("use_highlight_active_window")) {
            // load active window highlighted setting
            CDockableFrame.setActiveWindowHighlighted(preferences.loadBoolean("use_highlight_active_window"));
        }

        if (preferences.contains("program_boundary_rect")) {
            // load the boundary of the program (current window position)
            this.setBounds((Rectangle)preferences.loadObject("program_boundary_rect"));
        }
        // default close action
        this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        // set the icon
        this.setIconImage(
                new SaveResourceLoader("resource/img/icons/application/paintbucket.png").asImage()
        );

        try {
            DockingManager dockingManager = getDockingManager();
            DockableBarManager dockableBarManager = getDockableBarManager();
            LayoutPersistence layoutPersistence = getLayoutPersistence();

            // handles all the logic to make floating frames borderless
            handleBorderLess(dockingManager);

            // init loading
            ////////////////
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(
                    new SaveResourceLoader("resource/layout/TopLayout.ilayout").asInputStream()
            );

            // prepare
            dockableBarManager.beginLoadLayoutData();
            dockingManager.beginLoadLayoutData();

            // add menu bars
            dockableBarManager.setDockableBarFactory(new DockableBarFactory() {
                public DockableBar create(String key) {
                    return prepareBar(key);
                }
            });

            // add dock-able frames
            dockingManager.setDockableFrameFactory(new DockableFrameFactory() {
                public DockableFrame create(String key) {
                    return prepareFrame(key);
                }
            });

            // finish adding
            dockableBarManager.loadInitialLayout(document);
            dockingManager.loadInitialLayout(document);
            ////////////////////

            // register the shortcut action names
            shortcutManager.registerGlobalShortcutActions();

            // load the global hotkeys
            shortcutManager.registerShortcuts(thisFrame);

            // try to load the saved layout
            layoutPersistence.beginLoadLayoutData();
            byte[] layoutData = (byte[]) preferences.loadObject("custom_raw_layout_data");
            layoutPersistence.setUsePref(false);
            if(layoutData != null) {
                layoutPersistence.setLayoutRawData(layoutData);
            } else {
                layoutPersistence.loadLayoutData();
            }
            this.toFront();

            // allow frames to fill empty space
            dockingManager.getWorkspace().setAcceptDockableFrame(true);
            dockingManager.setEasyTabDock(true);
            //dockingManager.setUseGlassPaneEnabled(false);
            // set the grid snap size, e.g. when dragging
            dockingManager.setSnapGridSize(5);

            // set the draggable size between frames
            setDividerSizeDeep(dockingManager.getDockedFrameContainer(), 2);
        } catch (ParserConfigurationException e) {
            errorHandler.handle(e); // should not happen
        } catch (SAXException e) {
            errorHandler.handle(e); // should not happen
        } catch (IOException e) {
            errorHandler.handle(e); // should not happen
        }

        // register help overlay for entire window
        JRootPane rootPane = thisFrame.getRootPane();
        final FrameHelpOverlay overlay = new FrameHelpOverlay(rootPane, actionManager, complexActionManager, langSelector);
        actionManager.registerAction("show_help_overlay", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // toggle visibility of the glass "help" pane
                overlay.setActive(!overlay.isActive());
            }
        });

        // display help overlay on first three start
        if (preferences.contains("program_start_count")) {
            start_count = preferences.loadInteger("program_start_count");
        }
        if (start_count < 3) {
            overlay.setActive(true);
        }

        actionManager.registerAction("swap_mainView_with_xyView", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleFrameSwap("mainView", "xyView");
            }
        });

        actionManager.registerAction("swap_mainView_with_xzView", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleFrameSwap("mainView", "xzView");
            }
        });

        actionManager.registerAction("swap_mainView_with_yzView", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleFrameSwap("mainView", "yzView");
            }
        });

        actionManager.registerAction("swap_xyView_with_mainView", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleFrameSwap("xyView", "mainView");
            }
        });

        actionManager.registerAction("swap_xzView_with_mainView", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleFrameSwap("xzView", "mainView");
            }
        });

        actionManager.registerAction("swap_yzView_with_mainView", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleFrameSwap("yzView", "mainView");
            }
        });

    }

    // handle swap of frames (second frame is activated)
    private void handleFrameSwap(String frame1, String frame2) {
        DockingManager dm = getDockingManager();
        dm.addFrame(new DockableFrame("__dummy"));
        dm.moveFrame("__dummy", frame1);
        dm.moveFrame(frame1, frame2);
        dm.moveFrame(frame2, "__dummy");
        dm.removeFrame("__dummy");
        dm.activateFrame(frame2);
    }

}
