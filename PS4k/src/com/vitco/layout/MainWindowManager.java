package com.vitco.layout;

import com.jidesoft.action.CommandBar;
import com.jidesoft.action.DefaultDockableBarDockableHolder;
import com.jidesoft.action.DockableBar;
import com.jidesoft.docking.DockableFrame;
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

/*
 * Manages the creation of the main window. Requires a SubWindowManager implementation to work.
 * Init must be called to initialize the window.
 */

public class MainWindowManager extends DefaultDockableBarDockableHolder {

    // holds the layout settings file uri
    protected String layoutFileURI;
    // setter method for layoutFileURI
    public void setLayoutFileURI(String layoutFileURI) {
        this.layoutFileURI = layoutFileURI;
    }

    // SubWindowManager implementation that initializes all the the sub-windows.
    protected SubWindowManagerInterface subWindowManager;
    // setter method for SubWindowManager
    public void setSubWindowManager(SubWindowManagerInterface subWindowManager) {
        this.subWindowManager = subWindowManager;
    }

    // constructor
    public MainWindowManager(String title) throws HeadlessException {
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
            // load the layout file
            this.loadInitialLayout("resource/layout/TopLayout.ilayout");

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

    /**
     * Loads initial layout data from file.
     *
     * @param fileName
     * @throws SAXException
     * @throws ParserConfigurationException
     * @throws IOException
     */
    private void loadInitialLayout(String fileName) throws SAXException, ParserConfigurationException, IOException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(ClassLoader.getSystemResourceAsStream(fileName));
        loadInitialLayout(document);
    }

    /**
     * Loads initial layout data from Document.
     *
     * @param document
     */
    private void loadInitialLayout(Document document) {

        // add dock-able frames
        getDockingManager().beginLoadLayoutData();
        DockableFrame dockableFrame;

        dockableFrame = new DockableFrame("sideView", null);
        dockableFrame = subWindowManager.prepareSideView(dockableFrame);
        getDockingManager().addFrame(dockableFrame);

        dockableFrame = new DockableFrame("console", null);
        dockableFrame = subWindowManager.prepareConsole(dockableFrame);
        getDockingManager().addFrame(dockableFrame);

        dockableFrame = new DockableFrame("mainView", null);
        dockableFrame = subWindowManager.prepareMainView(dockableFrame);
        getDockingManager().addFrame(dockableFrame);

        dockableFrame = new DockableFrame("timeLine", null);
        dockableFrame = subWindowManager.prepareTimeLine(dockableFrame);
        getDockingManager().addFrame(dockableFrame);

        // finish adding dockable frames
        getDockingManager().loadInitialLayout(document);

        ////////////////////////////////////////////////

        // add menu bars
        getDockableBarManager().beginLoadLayoutData();
        DockableBar dockableBar;

        // add the main menu
        dockableBar = new CommandBar("mainMenu", null);
        dockableBar = subWindowManager.prepareMainMenu(dockableBar);
        getDockableBarManager().addDockableBar(dockableBar);

        // add the tool bar
        dockableBar = new CommandBar("toolBar", null);
        dockableBar = subWindowManager.prepareToolBar(dockableBar);
        getDockableBarManager().addDockableBar(dockableBar);

        // finish adding menu bars
        getDockableBarManager().loadInitialLayout(document);

    }

}
