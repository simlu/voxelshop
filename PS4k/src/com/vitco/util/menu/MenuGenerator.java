package com.vitco.util.menu;

import com.jidesoft.action.CommandBar;
import com.jidesoft.swing.JideButton;
import com.jidesoft.swing.JideMenu;
import com.vitco.util.action.ActionManagerInterface;
import com.vitco.util.action.types.StateActionPrototype;
import com.vitco.util.error.ErrorHandlerInterface;
import com.vitco.util.lang.LangSelectorInterface;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.swing.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;

/**
 * Generates menus from xml files and links actions to them (e.g. main menu, tool menu)
 */
public class MenuGenerator implements MenuGeneratorInterface {

    // var & setter
    private LangSelectorInterface langSel;
    @Override
    public void setLangSelector(LangSelectorInterface langSel) {
        this.langSel = langSel;
    }

    // var & setter
    private ErrorHandlerInterface errorHandler;
    @Override
    public void setErrorHandler(ErrorHandlerInterface errorHandler) {
        this.errorHandler = errorHandler;
    }

    // var & setter
    private ActionManagerInterface actionManager;
    @Override
    public void setActionManager(ActionManagerInterface actionManager) {
        this.actionManager = actionManager;
    }

    @Override
    public void buildMenuFromXML(CommandBar bar, String xmlFile) {
        try {
            // load the xml document
            DocumentBuilderFactory factory = DocumentBuilderFactory
                    .newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();

            Document doc = builder.parse(ClassLoader.getSystemResourceAsStream(xmlFile));

            buildRecursive(doc.getFirstChild(), bar);

        } catch (ParserConfigurationException e) {
            errorHandler.handle(e); // should not happen
        } catch (SAXException e) {
            errorHandler.handle(e); // should not happen
        } catch (IOException e) {
            errorHandler.handle(e); // should not happen
        }
    }

    private void buildRecursive(Node node, JComponent component) {

        // build the menu
        String name = node.getNodeName();
        if (node.hasChildNodes()) {
            if (name.equals("menu")) {
                Element e = (Element) node;
                JideMenu mnu = new JideMenu(langSel.getString(e.getAttribute("caption")));
                NodeList list = node.getChildNodes();
                component.add(mnu);
                int len = list.getLength();
                for (int i = 0; i < len; i++) {
                    buildRecursive(list.item(i), mnu);
                }
            } else if (name.equals("head")) {
                NodeList list = node.getChildNodes();
                int len = list.getLength();
                for (int i = 0; i < len; i++) {
                    buildRecursive(list.item(i), component);
                }
            }
        } else {
            if (name.equals("item")) {
                Element e = (Element) node;
                if (e.hasAttribute("checkable") && e.getAttribute("checkable").equals("true")) {
                    // check if this is a check menu button
                    addCheckItem(component, e);
                } else if (e.hasAttribute("grayable") && e.getAttribute("grayable").equals("true")) {
                    // check if this is a check menu button
                    addGrayItem(component, e);
                } else {
                    addDefaultItem(component, e);
                }
            } else if (name.equals("separator")) {
                component.add(new JSeparator());
            } else if (name.equals("icon-item")) {
                Element e = (Element) node;
                addIconItem(component, e);
            }
        }
    }

    // adds an item that has an icon and a tooltip
    private void addIconItem(JComponent component, final Element e) {
        JideButton jideButton = new JideButton(new ImageIcon(Toolkit.getDefaultToolkit().getImage(
                ClassLoader.getSystemResource(e.getAttribute("src"))
        )));
        // to perform validity check we need to register this name
        actionManager.registerActionName(e.getAttribute("action"));
        // lazy action execution (the action might not be ready!)
        jideButton.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                actionManager.getAction(e.getAttribute("action")).actionPerformed(evt);
            }
        });
        jideButton.setFocusable(false);
        jideButton.setToolTipText(langSel.getString(e.getAttribute("tool-tip")));
        component.add(jideButton);

    }

    // adds a default menu item
    private void addDefaultItem(JComponent component, final Element e) {
        JMenuItem item = new JMenuItem();
        // to perform validity check we need to register this name
        actionManager.registerActionName(e.getAttribute("action"));
        // lazy action execution (the action might not be ready!)
        item.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                actionManager.getAction(e.getAttribute("action")).actionPerformed(evt);
            }
        });
        item.setText(langSel.getString(e.getAttribute("caption")));
        component.add(item);
    }

    // adds an item that can be checked or unchecked
    private void addCheckItem(JComponent component, final Element e) {
        final JCheckBoxMenuItem item = new JCheckBoxMenuItem();
        // to perform validity check we need to register this name
        actionManager.registerActionName(e.getAttribute("action"));
        // lazy action execution (the action might not be ready!)
        item.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                actionManager.getAction(e.getAttribute("action")).actionPerformed(evt);
            }
        });
        item.setText(langSel.getString(e.getAttribute("caption")));

        // look up current check status
        item.addPropertyChangeListener("ancestor",new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (evt.getNewValue() != null) {
                    item.setSelected(
                            // triggered when the menu item is show
                            // this makes sure the "checked" is always current
                            ((StateActionPrototype) actionManager.getAction(e.getAttribute("action"))).getStatus()
                    );
                }
            }
        });
        component.add(item);
    }

    // adds an item that can be grayed out
    private void addGrayItem(JComponent component, final Element e) {
        final JCheckBoxMenuItem item = new JCheckBoxMenuItem();
        // to perform validity check we need to register this name
        actionManager.registerActionName(e.getAttribute("action"));
        // lazy action execution (the action might not be ready!)
        item.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                actionManager.getAction(e.getAttribute("action")).actionPerformed(evt);
            }
        });
        item.setText(langSel.getString(e.getAttribute("caption")));

        // look up current check status
        item.addPropertyChangeListener("ancestor",new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (evt.getNewValue() != null) {
                    item.setEnabled(
                            // triggered when the menu item is show
                            // this makes sure the "checked" is always current
                            ((StateActionPrototype) actionManager.getAction(e.getAttribute("action"))).getStatus()
                    );
                }
            }
        });
        component.add(item);
    }
}
