package com.vitco.layout;

import com.jidesoft.action.CommandBar;
import com.jidesoft.action.DefaultDockableBarDockableHolder;
import com.jidesoft.action.DockableBar;
import com.jidesoft.action.DockableBarFactory;
import com.jidesoft.docking.DockableFrame;
import com.jidesoft.docking.DockableFrameFactory;
import com.vitco.layout.bars.BarLinkageInterface;
import com.vitco.layout.frames.FrameLinkageInterface;
import com.vitco.util.lang.LangSelectorInterface;
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
import java.io.File;
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
    private Map<String, BarLinkageInterface> barLinkageMap;
    // set the map
    @Override
    public void setBarLinkageMap(Map<String, BarLinkageInterface> map) {
        this.barLinkageMap = map;
    }

    // maps the frames to the linkage class that deals with them
    private Map<String, FrameLinkageInterface> frameLinkageMap;
    // set the map
    @Override
    public void setFrameLinkageMap(Map<String, FrameLinkageInterface> map) {
        this.frameLinkageMap = map;
    }

    // handles the fetching of strings for keys for localization
    private LangSelectorInterface langSel;
    // setter method for langSel
    @Override
    public void setLangSelector(LangSelectorInterface langSel) {
        this.langSel = langSel;
    }

    // prepare all frames
    @Override
    public DockableFrame prepareFrame(String key) {
        DockableFrame frame = null;
        if (frameLinkageMap.containsKey(key)) {
            frame = frameLinkageMap.get(key).buildFrame(key, langSel);
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
            bar = barLinkageMap.get(key).buildBar(key, langSel);
        } else {
            System.err.println("Error: No linkage class defined for bar \"" + key + "\"");
        }

        return bar;
    }

    // holds the layout settings file uri
    private String layoutFileURI;
    // setter method for layoutFileURI
    @Override
    public void setLayoutFileURI(String layoutFileURI) {
        this.layoutFileURI = layoutFileURI;
    }

    // constructor
    public WindowManager(String title) throws HeadlessException {
        super(title);

        // save the state on exit of the program
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                getDockingManager().saveLayoutDataToFile(layoutFileURI);
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
            File file=new File(layoutFileURI);
            this.getLayoutPersistence().loadLayoutData();
            if(file.isFile()) {
                this.getDockingManager().loadLayoutDataFromFile(layoutFileURI);
            } else {
                this.getLayoutPersistence().loadLayoutData();
            }

            this.toFront();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
