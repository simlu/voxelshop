package com.vitco.layout;

import com.jidesoft.action.*;
import com.jidesoft.docking.DockableFrame;
import com.jidesoft.docking.DockableFrameFactory;
import com.jidesoft.docking.DockingManager;
import com.jidesoft.swing.LayoutPersistence;
import com.vitco.core.data.Data;
import com.vitco.layout.bars.BarLinkagePrototype;
import com.vitco.layout.content.shortcut.ShortcutManagerInterface;
import com.vitco.layout.frames.FrameLinkagePrototype;
import com.vitco.manager.action.ActionManager;
import com.vitco.manager.error.ErrorHandlerInterface;
import com.vitco.manager.lang.LangSelectorInterface;
import com.vitco.manager.pref.PreferencesInterface;
import com.vitco.settings.VitcoSettings;
import com.vitco.util.misc.SaveResourceLoader;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.swing.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.Map;

/*
 * Manages the creation of the main window.
 *
 * Defers dealing with the content of frames and bars to the in config.xml
 * configured classes.
 */

public class WindowManager extends DefaultDockableBarDockableHolder implements WindowManagerInterface {

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

    // var & setter
    protected LangSelectorInterface langSelector;
    @Override
    public final void setLangSelector(LangSelectorInterface langSelector) {
        this.langSelector = langSelector;
    }

    // prepare all frames
    @Override
    public final DockableFrame prepareFrame(final String key) {
        DockableFrame frame = null;
        if (frameLinkageMap.containsKey(key)) {
            frame = frameLinkageMap.get(key).buildFrame(key, thisFrame);
            shortcutManager.registerFrame(frame);
        } else {
            System.err.println("Error: No linkage class defined for frame \"" + key + "\"");
        }

        return frame;
    }

    final Frame thisFrame = this;

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
        super(VitcoSettings.VERSION_ID);
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
    }

    @PostConstruct
    @Override
    public final void init() {

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

        } catch (ParserConfigurationException e) {
            errorHandler.handle(e); // should not happen
        } catch (SAXException e) {
            errorHandler.handle(e); // should not happen
        } catch (IOException e) {
            errorHandler.handle(e); // should not happen
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
