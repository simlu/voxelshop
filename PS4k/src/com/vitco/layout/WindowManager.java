package com.vitco.layout;

import com.jidesoft.action.CommandBar;
import com.jidesoft.action.DefaultDockableBarDockableHolder;
import com.jidesoft.action.DockableBar;
import com.jidesoft.action.DockableBarFactory;
import com.jidesoft.docking.DockableFrame;
import com.jidesoft.docking.DockableFrameFactory;
import com.vitco.layout.bars.BarLinkagePrototype;
import com.vitco.layout.frames.FrameLinkagePrototype;
import com.vitco.logic.frames.shortcut.ShortcutManagerInterface;
import com.vitco.util.error.ErrorHandlerInterface;
import com.vitco.util.pref.PreferencesInterface;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.annotation.PostConstruct;
import javax.swing.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.awt.*;
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
    public void setBarLinkageMap(Map<String, BarLinkagePrototype> map) {
        this.barLinkageMap = map;
    }

    // var & setter
    private ErrorHandlerInterface errorHandler;
    @Override
    public void setErrorHandler(ErrorHandlerInterface errorHandler) {
        this.errorHandler = errorHandler;
    }

    // var & setter
    private PreferencesInterface preferences;
    public void setPreferences(PreferencesInterface preferences) {
        this.preferences = preferences;
    }

    // maps the frames to the linkage class that deals with them
    private Map<String, FrameLinkagePrototype> frameLinkageMap;
    // set the map
    @Override
    public void setFrameLinkageMap(Map<String, FrameLinkagePrototype> map) {
        this.frameLinkageMap = map;
    }

    // to hook the shortcut manager to the frames
    private ShortcutManagerInterface shortcutManager;
    @Override
    public void setShortcutManager(ShortcutManagerInterface shortcutManager) {
        this.shortcutManager = shortcutManager;
    }

    // prepare all frames
    @Override
    public DockableFrame prepareFrame(String key) {
        DockableFrame frame = null;
        if (frameLinkageMap.containsKey(key)) {
            frame = frameLinkageMap.get(key).buildFrame(key);
            shortcutManager.registerFrame(frame);
        } else {
            System.err.println("Error: No linkage class defined for frame \"" + key + "\"");
        }

        return frame;
    }

    // prepare all bars
    @Override
    public DockableBar prepareBar(String key) {

        CommandBar bar = null;
        if (barLinkageMap.containsKey(key)) {
            bar = barLinkageMap.get(key).buildBar(key);
        } else {
            System.err.println("Error: No linkage class defined for bar \"" + key + "\"");
        }

        return bar;
    }

    // constructor
    public WindowManager(String title) throws HeadlessException {
        super(title);

        // save the state on exit of the program
        // this needs to be done BEFORE the window is closing
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                preferences.storeObject("custom_raw_layout_data", getLayoutPersistence().getLayoutRawData());
            }
        });

    }

    @PostConstruct
    public void init() {

        // default close action
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // set the icon
        this.setIconImage(Toolkit.getDefaultToolkit().getImage(
                ClassLoader.getSystemResource("resource/img/icons/application/paintbucket.png")
        ));

        try {
            // init loading
            ////////////////
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(ClassLoader.getSystemResourceAsStream("resource/layout/TopLayout.ilayout"));

            // prepare
            getDockableBarManager().beginLoadLayoutData();
            getDockingManager().beginLoadLayoutData();

            // add menu bars
            getDockableBarManager().setDockableBarFactory(new DockableBarFactory() {
                public DockableBar create(String key) {
                    return prepareBar(key);
                }
            });

            // add dock-able frames
            getDockingManager().setDockableFrameFactory(new DockableFrameFactory() {
                public DockableFrame create(String key) {
                    return prepareFrame(key);
                }
            });

            // finish adding
            getDockableBarManager().loadInitialLayout(document);
            getDockingManager().loadInitialLayout(document);
            ////////////////////

            // try to load the saved layout
            this.getLayoutPersistence().beginLoadLayoutData();
            byte[] layoutData = (byte[]) preferences.loadObject("custom_raw_layout_data");
            if(layoutData != null) {
                getLayoutPersistence().setLayoutRawData(layoutData);
            } else {
                this.getLayoutPersistence().loadLayoutData();
            }
            this.toFront();
        } catch (ParserConfigurationException e) {
            errorHandler.handle(e); // should not happen
        } catch (SAXException e) {
            errorHandler.handle(e); // should not happen
        } catch (IOException e) {
            errorHandler.handle(e); // should not happen
        }
    }

}
