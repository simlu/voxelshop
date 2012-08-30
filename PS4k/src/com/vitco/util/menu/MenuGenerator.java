package com.vitco.util.menu;

import com.jidesoft.swing.JideButton;
import com.jidesoft.swing.JideMenu;
import com.jidesoft.swing.JideToggleButton;
import com.vitco.frames.shortcut.GlobalShortcutChangeListener;
import com.vitco.frames.shortcut.ShortcutManagerInterface;
import com.vitco.util.action.ActionManagerInterface;
import com.vitco.util.action.ChangeListener;
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
    private ShortcutManagerInterface shortcutManager;
    @Override
    public void setShortcutManager(ShortcutManagerInterface shortcutManager) {
        this.shortcutManager = shortcutManager;
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
    public void buildMenuFromXML(JComponent jComponent, String xmlFile) {
        try {
            // load the xml document
            DocumentBuilderFactory factory = DocumentBuilderFactory
                    .newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();

            Document doc = builder.parse(ClassLoader.getSystemResourceAsStream(xmlFile));

            buildRecursive(doc.getFirstChild(), jComponent);

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
                if (e.hasAttribute("checkable") && e.getAttribute("checkable").equals("true")) {
                    addCheckIconItem(component, e);
                } else {
                    addIconItem(component, e);
                }
            }
        }
    }

    private void handleMenuShortcut(final JMenuItem item, final Element e) {
        // shortcut change events
        KeyStroke accelerator = shortcutManager.getGlobalShortcutByAction(e.getAttribute("action"));
        if (accelerator != null) {
            item.setAccelerator(accelerator);
        }
        shortcutManager.addGlobalShortcutChangeListener(new GlobalShortcutChangeListener() {
            @Override
            public void onChange() {
                KeyStroke accelerator = shortcutManager.getGlobalShortcutByAction(e.getAttribute("action"));
                if (accelerator != null) {
                    item.setAccelerator(accelerator);
                }
            }
        });
    }

    private void handleButtonShortcutAndTooltip(final JideButton jideButton, final Element e) {
        // shortcut change events
        KeyStroke accelerator = shortcutManager.getGlobalShortcutByAction(e.getAttribute("action"));
        if (accelerator != null) {
            jideButton.setToolTipText(
                    langSel.getString(e.getAttribute("tool-tip"))
                            + " (" + shortcutManager.asString(accelerator) + ")"
            );
        } else {
            // might still have a frame shortcut, we don't know
            jideButton.setToolTipText(
                    langSel.getString(e.getAttribute("tool-tip"))
            );
        }
        shortcutManager.addGlobalShortcutChangeListener(new GlobalShortcutChangeListener() {
            @Override
            public void onChange() {
                KeyStroke accelerator = shortcutManager.getGlobalShortcutByAction(e.getAttribute("action"));
                if (accelerator != null) {
                    jideButton.setToolTipText(
                            langSel.getString(e.getAttribute("tool-tip"))
                                    + " (" + shortcutManager.asString(accelerator) + ")"
                    );
                }
            }
        });
    }

    // adds an item that has an icon and a tooltip
    private void addIconItem(JComponent component, final Element e) {
        final JideButton jideButton = new JideButton(new ImageIcon(Toolkit.getDefaultToolkit().getImage(
                ClassLoader.getSystemResource(e.getAttribute("src"))
        )));
        // to perform validity check we need to register this name
        actionManager.registerActionName(e.getAttribute("action"));
        // lazy action linking (the action might not be ready!)
        actionManager.performWhenActionIsReady(e.getAttribute("action"), new Runnable() {
            @Override
            public void run() {
                jideButton.addActionListener(actionManager.getAction(e.getAttribute("action")));
            }
        });
        jideButton.setFocusable(false);
        handleButtonShortcutAndTooltip(jideButton, e);
        component.add(jideButton);

    }

    // adds a default menu item
    private void addDefaultItem(JComponent component, final Element e) {
        final JMenuItem item = new JMenuItem();
        // to perform validity check we need to register this name
        actionManager.registerActionName(e.getAttribute("action"));
        // lazy action linking (the action might not be ready!)
        actionManager.performWhenActionIsReady(e.getAttribute("action"), new Runnable() {
            @Override
            public void run() {
                item.addActionListener(actionManager.getAction(e.getAttribute("action")));
            }
        });
        handleMenuShortcut(item, e);
        item.setText(langSel.getString(e.getAttribute("caption")));
        component.add(item);
    }

    // adds an item that can be checked or unchecked
    private void addCheckItem(final JComponent component, final Element e) {
        final JCheckBoxMenuItem item = new JCheckBoxMenuItem();
        // to perform validity check we need to register this name
        actionManager.registerActionName(e.getAttribute("action"));
        // lazy action linking (the action might not be ready!)
        actionManager.performWhenActionIsReady(e.getAttribute("action"), new Runnable() {
            @Override
            public void run() {
                item.addActionListener(actionManager.getAction(e.getAttribute("action")));
            }
        });
        handleMenuShortcut(item, e);
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
                    if (e.hasAttribute("invert") && e.getAttribute("invert").equals("true")) {
                        item.setSelected(!item.isSelected());
                    }
                }
            }
        });
        component.add(item);
    }

    // adds an item that has an icon and a tooltip and is checkable
    private void addCheckIconItem(JComponent component, final Element e) {
        final JideToggleButton jideButton = new JideToggleButton(new ImageIcon(Toolkit.getDefaultToolkit().getImage(
                ClassLoader.getSystemResource(e.getAttribute("src"))
        )));
        // to perform validity check we need to register this name
        actionManager.registerActionName(e.getAttribute("action"));
        // lazy action linking (the action might not be ready!)
        actionManager.performWhenActionIsReady(e.getAttribute("action"), new Runnable() {
            @Override
            public void run() {
                jideButton.addActionListener(actionManager.getAction(e.getAttribute("action")));
            }
        });
        jideButton.setFocusable(false);
        handleButtonShortcutAndTooltip(jideButton, e);
        // make sure the action is ready
        actionManager.performWhenActionIsReady(e.getAttribute("action"), new Runnable() {
            @Override
            public void run() {
                StateActionPrototype stateActionPrototype =
                        ((StateActionPrototype) actionManager.getAction(e.getAttribute("action")));
                stateActionPrototype.addChangeListener(new ChangeListener() {
                    @Override
                    public void actionFired(boolean b) {
                        jideButton.setSelected(b);
                        if (e.hasAttribute("invert") && e.getAttribute("invert").equals("true")) {
                            jideButton.setSelected(!jideButton.isSelected());
                        }
                    }
                });
            }
        });
        component.add(jideButton);
    }

    // adds an item that can be grayed out
    private void addGrayItem(JComponent component, final Element e) {
        final JMenuItem item = new JMenuItem();
        // to perform validity check we need to register this name
        actionManager.registerActionName(e.getAttribute("action"));
        // lazy action linking (the action might not be ready!)
        actionManager.performWhenActionIsReady(e.getAttribute("action"), new Runnable() {
            @Override
            public void run() {
                item.addActionListener(actionManager.getAction(e.getAttribute("action")));
            }
        });
        handleMenuShortcut(item, e);
        item.setText(langSel.getString(e.getAttribute("caption")));
        // look up current gray status
        item.addPropertyChangeListener("ancestor",new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (evt.getNewValue() != null) {
                    item.setEnabled(
                            // triggered when the menu item is show
                            // this makes sure the "checked" is always current
                            ((StateActionPrototype) actionManager.getAction(e.getAttribute("action"))).getStatus()
                    );
                    if (e.hasAttribute("invert") && e.getAttribute("invert").equals("true")) {
                        item.setEnabled(!item.isEnabled());
                    }
                }
            }
        });
        component.add(item);
    }
}
