package com.vitco.util.menu;

import com.jidesoft.action.CommandBarSeparator;
import com.jidesoft.action.CommandMenuBar;
import com.jidesoft.swing.JideButton;
import com.jidesoft.swing.JideMenu;
import com.jidesoft.swing.JideSplitButton;
import com.jidesoft.swing.JideToggleButton;
import com.vitco.logic.shortcut.GlobalShortcutChangeListener;
import com.vitco.logic.shortcut.ShortcutManagerInterface;
import com.vitco.util.action.ActionManager;
import com.vitco.util.action.ChangeListener;
import com.vitco.util.action.ComplexActionManager;
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
    private ActionManager actionManager;
    @Override
    public void setActionManager(ActionManager actionManager) {
        this.actionManager = actionManager;
    }

    // var & setter
    private ComplexActionManager complexActionManager;
    @Override
    public void setComplexActionManager(ComplexActionManager complexActionManager) {
        this.complexActionManager = complexActionManager;
    }

    @Override
    public void buildMenuFromXML(JComponent jComponent, String xmlFile) {
        if (jComponent instanceof CommandMenuBar) { // disable the chevron by default
            ((CommandMenuBar)jComponent).setChevronAlwaysVisible(false);
        }
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
                addItem(component, e,
                        e.hasAttribute("checkable") && e.getAttribute("checkable").equals("true"),
                        e.hasAttribute("grayable") && e.getAttribute("grayable").equals("true"),
                        e.hasAttribute("hideable") && e.getAttribute("hideable").equals("true")
                );
            } else if (name.equals("separator")) {
                component.add(new CommandBarSeparator());
            } else if (name.equals("icon-item")) {
                Element e = (Element) node;
                addIconItem(component, e,
                        e.hasAttribute("checkable") && e.getAttribute("checkable").equals("true"),
                        e.hasAttribute("grayable") && e.getAttribute("grayable").equals("true"),
                        e.hasAttribute("hideable") && e.getAttribute("hideable").equals("true")
                );
            } else if (name.equals("split-item")) {
                Element e = (Element) node;
                addSplitItem(component, e,
                        e.hasAttribute("checkable") && e.getAttribute("checkable").equals("true"),
                        e.hasAttribute("grayable") && e.getAttribute("grayable").equals("true"),
                        e.hasAttribute("hideable") && e.getAttribute("hideable").equals("true")
                );
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

    private void handleButtonShortcutAndTooltip(final JComponent button, final Element e) {
        // shortcut change events
        KeyStroke accelerator = shortcutManager.getGlobalShortcutByAction(e.getAttribute("action"));
        if (accelerator != null) {
            button.setToolTipText(
                    langSel.getString(e.getAttribute("tool-tip"))
                            + " (" + shortcutManager.asString(accelerator) + ")"
            );
        } else {
            // might still have a frame shortcut, we don't know
            button.setToolTipText(
                    langSel.getString(e.getAttribute("tool-tip"))
            );
        }
        shortcutManager.addGlobalShortcutChangeListener(new GlobalShortcutChangeListener() {
            @Override
            public void onChange() {
                KeyStroke accelerator = shortcutManager.getGlobalShortcutByAction(e.getAttribute("action"));
                if (accelerator != null) {
                    button.setToolTipText(
                            langSel.getString(e.getAttribute("tool-tip"))
                                    + " (" + shortcutManager.asString(accelerator) + ")"
                    );
                }
            }
        });
    }

    // =========================
    // adds a default menu item
    private void addItem(JComponent component, final Element e,
                         final boolean checkable, final boolean grayable, final boolean hideable) {
        final JMenuItem item = checkable ? new JCheckBoxMenuItem() : new JMenuItem();

        // to perform validity check we need to register this name
        actionManager.registerActionIsUsed(e.getAttribute("action"));
        // lazy action linking (the action might not be ready!)
        actionManager.performWhenActionIsReady(e.getAttribute("action"), new Runnable() {
            @Override
            public void run() {
                item.addActionListener(actionManager.getAction(e.getAttribute("action")));
            }
        });
        handleMenuShortcut(item, e);
        item.setText(langSel.getString(e.getAttribute("caption")));

        if (checkable || grayable || hideable) {
            // look up current check status
            item.addPropertyChangeListener("ancestor",new PropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    if (evt.getNewValue() != null) {
                        boolean invert = e.hasAttribute("invert") && e.getAttribute("invert").equals("true");
                        StateActionPrototype action = ((StateActionPrototype) actionManager.getAction(e.getAttribute("action")));
                        if (checkable) {
                            item.setSelected(
                                    // triggered when the menu item is show
                                    // this makes sure the "checked" is always current
                                    invert ? !action.isChecked() : action.isChecked()
                            );
                        }
                        if (grayable) {
                            item.setEnabled(
                                    // triggered when the menu item is show
                                    // this makes sure the "checked" is always current
                                    invert ? !action.isEnabled() : action.isEnabled()
                            );
                        }
                        if (hideable) {
                            item.setVisible(
                                    // triggered when the menu item is show
                                    // this makes sure the "checked" is always current
                                    invert ? !action.isVisible() : action.isVisible()
                            );
                        }
                    }
                }
            });
        }

        component.add(item);
    }

    // handles correct states of gray, checked, hide button
    private void handleAbstractButton(final AbstractButton abstractButton, final Element e, final boolean checkable, final boolean grayable, final boolean hideable) {
        abstractButton.setFocusable(false);
        handleButtonShortcutAndTooltip(abstractButton, e);

        if (grayable) {
            // check if there is a custom gray icon defined
            if (e.hasAttribute("src-gray")) {
                abstractButton.setDisabledIcon(new ImageIcon(Toolkit.getDefaultToolkit().getImage(
                        ClassLoader.getSystemResource(e.getAttribute("src-gray"))
                )));
            }
        }

        if (checkable || grayable || hideable) {
            actionManager.performWhenActionIsReady(e.getAttribute("action"), new Runnable() {
                @Override
                public void run() {
                    final StateActionPrototype stateActionPrototype =
                            ((StateActionPrototype) actionManager.getAction(e.getAttribute("action")));
                    final boolean invert = e.hasAttribute("invert") && e.getAttribute("invert").equals("true");
                    stateActionPrototype.addChangeListener(new ChangeListener() {
                        @Override
                        public void actionFired(boolean b) {
                            if (checkable) {
                                abstractButton.setSelected(invert ? !stateActionPrototype.isChecked() : stateActionPrototype.isChecked());
                            }
                            if (grayable) {
                                abstractButton.setEnabled(invert ? !stateActionPrototype.isEnabled() : stateActionPrototype.isEnabled());
                            }
                            if (hideable) {
                                abstractButton.setVisible(invert ? !stateActionPrototype.isVisible() : stateActionPrototype.isVisible());
                            }
                        }
                    });
                }
            });
        }
    }

    // =========================
    // adds an item that has an icon and a tooltip
    private void addIconItem(JComponent component, final Element e,
                             final boolean checkable, final boolean grayable, final boolean hideable) {
        final JideButton jideButton = checkable
                ? new JideToggleButton(new ImageIcon(Toolkit.getDefaultToolkit().getImage(
                ClassLoader.getSystemResource(e.getAttribute("src")))))
                : new JideButton(new ImageIcon(Toolkit.getDefaultToolkit().getImage(
                ClassLoader.getSystemResource(e.getAttribute("src")))));

        // to perform validity check we need to register this name
        actionManager.registerActionIsUsed(e.getAttribute("action"));
        // lazy action linking (the action might not be ready!)
        actionManager.performWhenActionIsReady(e.getAttribute("action"), new Runnable() {
            @Override
            public void run() {
                jideButton.addActionListener(actionManager.getAction(e.getAttribute("action")));
            }
        });

        handleAbstractButton(jideButton, e, checkable, grayable, hideable);

        component.add(jideButton);
    }

    // =========================
    // adds a split item (submenu)
    private void addSplitItem(JComponent component, final Element e,
                              final boolean checkable, final boolean grayable, final boolean hideable) {

        final JideSplitButton splitButton = new JideSplitButton(new ImageIcon(Toolkit.getDefaultToolkit().getImage(
                ClassLoader.getSystemResource(e.getAttribute("src")))));

        // check if we have an action
        if (e.hasAttribute("action") && !(e.hasAttribute("disable-action") && e.getAttribute("disable-action").equals("true"))) {
            // to perform validity check we need to register this name
            actionManager.registerActionIsUsed(e.getAttribute("action"));
            // lazy action linking (the action might not be ready!)
            actionManager.performWhenActionIsReady(e.getAttribute("action"), new Runnable() {
                @Override
                public void run() {
                    splitButton.addActionListener(actionManager.getAction(e.getAttribute("action")));
                }
            });
        } else {
            // the whole button is now used to open the dropdown menu
            splitButton.setAlwaysDropdown(true);
        }

        // disable the border of the shown dialog
        splitButton.getPopupMenu().setBorder(BorderFactory.createEmptyBorder());

        // to perform validity check we need to register this name
        complexActionManager.registerActionIsUsed(e.getAttribute("complex-action"));
        // lazy action linking (the action might not be ready!)
        complexActionManager.performWhenActionIsReady(e.getAttribute("complex-action"), new Runnable() {
            @Override
            public void run() {
                splitButton.add(complexActionManager.getAction(e.getAttribute("complex-action")));
            }
        });

        handleAbstractButton(splitButton, e, checkable, grayable, hideable);

        component.add(splitButton);

    }

}
