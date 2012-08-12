package com.vitco.logic.frames.shortcut;

import com.vitco.util.action.ActionManagerInterface;
import com.vitco.util.FileTools;
import com.vitco.util.error.ErrorHandlerInterface;
import com.vitco.util.lang.LangSelectorInterface;
import com.vitco.util.pref.PreferencesInterface;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.swing.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Handles shortcut linking (logic)
 */
public class ShortcutManager implements ShortcutManagerInterface {

    // holds the mapping: frame -> (KeyStroke, actionName)
    private final Map<String, ArrayList<ShortcutObject>> map = new HashMap<String, ArrayList<ShortcutObject>>();

    // var & setter
    private PreferencesInterface preferences;
    public void setPreferences(PreferencesInterface preferences) {
        this.preferences = preferences;
    }

    // var & setter
    private String xmlFile;
    @Override
    public void setConfigFile(String filename) {
        xmlFile = filename;
    }

    // var & setter
    private ActionManagerInterface actionManager;
    @Override
    public void setActionManager(ActionManagerInterface actionManager) {
        this.actionManager = actionManager;
    }

    // var & setter
    private ErrorHandlerInterface errorHandler;
    @Override
    public void setErrorHandler(ErrorHandlerInterface errorHandler) {
        this.errorHandler = errorHandler;
    }

    // var & setter
    private LangSelectorInterface langSel;
    @Override
    public void setLangSelector(LangSelectorInterface langSel) {
        this.langSel = langSel;
    }

    // get all frames as string array (frameKey, loc frameCaption)
    @Override
    public String[][] getFrames() {
        String[][] result = new String[map.size()][];
        // loop over all the frames
        int i = 0;
        for (String key : map.keySet()) {
            result[i++] = new String[]{
                    key,
                    langSel.getString(key + "_caption")
            };
        }
        return result;
    }

    // get shortcuts as string array (loc caption, str representation)
    @Override
    public String[][] getShortcuts(String frameKey) {
        if (map.containsKey(frameKey)) {
            ArrayList<ShortcutObject> shortcuts = map.get(frameKey);
            String[][] result = new String[shortcuts.size()][];
            for (int i = 0, len = shortcuts.size(); i < len; i++) {
                result[i] = new String[]{
                        langSel.getString(shortcuts.get(i).caption),
                        asString(shortcuts.get(i).keyStroke)
                };
            }
            return result;
        } else {
            System.err.println("Error: Shortcuts for the non-existing frame \"" + frameKey + "\" were requested.");
            return null;
        }
    }

    // update (keystroke,action) registration for a frame and shortcut id
    @Override
    public boolean updateShortcutObject(KeyStroke keyStroke, String frame, int id) {
        if (map.containsKey(frame)) {
            if (map.get(frame).size() > id) {
                ShortcutObject shortcutObject = map.get(frame).get(id);
                shortcutObject.linkedFrame.unregisterKeyboardAction(shortcutObject.keyStroke); // un-register old
                shortcutObject.keyStroke = keyStroke;
                final String actionName = shortcutObject.actionName;
                AbstractAction action = new AbstractAction() {
                    @Override
                    public void actionPerformed(ActionEvent evt) {
                        actionManager.getAction(actionName).actionPerformed(evt);
                    }
                };
                shortcutObject.linkedFrame.registerKeyboardAction(
                        action, shortcutObject.keyStroke, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT
                );
                return true;
            }
        }
        System.err.println("Error: Can not set KeyStroke for frame \"" + frame + "\" and id \"" + id + "\".");
        return false;
    }

    // convert KeyStroke to string representation
    @Override
    public String asString(KeyStroke keyStroke) {
        return keyStroke.toString().replace("pressed", "+").toUpperCase();
    }

    // check if this shortcut is not already used
    @Override
    public boolean isFreeShortcut(String frame, KeyStroke keyStroke) {
        // check that this shortcut is not already set for an action
        if (map.containsKey(frame)) {
            for (ShortcutObject shortcutObject : map.get(frame)) {
                if (shortcutObject.keyStroke.equals(keyStroke)) {
                    return false;
                }
            }
            return true;
        }
        System.err.println("Error: Can not find frame \"" + frame + "\".");
        return true;
    }

    // check if we want to allow this shortcut
    @Override
    public boolean isValidShortcut(KeyStroke keyStroke) {
        // check that this is a format that is allowed as shortcut
        return ((keyStroke.getModifiers() == (InputEvent.CTRL_DOWN_MASK | InputEvent.CTRL_MASK)) ||
                (keyStroke.getModifiers() == (InputEvent.ALT_DOWN_MASK | InputEvent.ALT_MASK)) ||
                (keyStroke.getModifiers() == (InputEvent.SHIFT_DOWN_MASK | InputEvent.SHIFT_MASK)) ||
                (keyStroke.getModifiers() == ((InputEvent.CTRL_DOWN_MASK | InputEvent.CTRL_MASK) | (InputEvent.ALT_DOWN_MASK | InputEvent.ALT_MASK))) ||
                (keyStroke.getModifiers() == ((InputEvent.CTRL_DOWN_MASK | InputEvent.CTRL_MASK) | (InputEvent.SHIFT_DOWN_MASK | InputEvent.SHIFT_MASK))) ||
                (keyStroke.getModifiers() == ((InputEvent.ALT_DOWN_MASK | InputEvent.ALT_MASK) | (InputEvent.SHIFT_DOWN_MASK | InputEvent.SHIFT_MASK))))
                // allow only certain keys as trigger keys
                && new ArrayList<Integer>(Arrays.asList(new Integer[]{
                // A-Z
                65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77,
                78, 79, 80, 81, 82, 83, 84, 85, 86, 87, 88, 89, 90,
                // 0-9
                30, 31, 32, 33, 34, 35, 36, 37, 38, 39
        })).contains(keyStroke.getKeyCode());
    }

    // internal - check that this action is not yet in the frame
    private boolean usedAction(String frameName, String actionName) {
        // check that this shortcut is not already set for an action
        if (map.containsKey(frameName)) {
            for (ShortcutObject shortcutObject : map.get(frameName)) {
                if (shortcutObject.actionName.equals(actionName)) {
                    return true;
                }
            }
            return false;
        }
        System.err.println("Error: Can not find frame \"" + frameName + "\".");
        return false;
    }

    // internal - check that this caption localization is not yet used
    private boolean usedCaption(String frameName, String caption) {
        // check that this shortcut is not already set for an action
        if (map.containsKey(frameName)) {
            for (ShortcutObject shortcutObject : map.get(frameName)) {
                if (shortcutObject.caption.equals(caption)) {
                    return true;
                }
            }
            return false;
        }
        System.err.println("Error: Can not find frame \"" + frameName + "\".");
        return false;
    }

    // store shortcuts in file
    @PreDestroy
    public void onDestruct() {
        // store the shortcut map in file
        preferences.storeObject("all_shortcuts_as_map", map);
    }

    // load the xml files that maps (shortcut, action)
    // and then load custom user defined shortcuts
    @PostConstruct
    @Override
    public void loadConfig() {
        // load and parse the xml file so we
        // can access it to hook the key bindings
        try {
            // load the xml document
            DocumentBuilderFactory factory = DocumentBuilderFactory
                    .newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();

            Document doc = builder.parse(ClassLoader.getSystemResourceAsStream(xmlFile));

            // load the mapping
            Node node = doc.getFirstChild(); // head node
            if (node.getNodeName().equals("head")) {
                NodeList list = node.getChildNodes();
                // loop over all the windows
                for (int i = 0, len = list.getLength(); i < len; i++) {
                    addShortcuts(list.item(i));
                }
            }

        } catch (ParserConfigurationException e) {
            errorHandler.handle(e); // should not happen
        } catch (SAXException e) {
            errorHandler.handle(e); // should not happen
        } catch (IOException e) {
            errorHandler.handle(e); // should not happen
        }

        // check if we have a file stored
        Object storedShortcuts = preferences.loadObject("all_shortcuts_as_map");
        if (storedShortcuts != null) {
            Map<String, ArrayList> tmp = FileTools.castHash((HashMap) storedShortcuts, String.class, ArrayList.class);
            for (String key : map.keySet()) {
                if (tmp.containsKey(key)) {
                    for (ShortcutObject so1 : map.get(key)) {
                        for (Object so2 : tmp.get(key)) {
                            // only update existing actions
                            if (so1.actionName.equals(((ShortcutObject)so2).actionName)) {
                                so1.keyStroke = ((ShortcutObject)so2).keyStroke;
                            }
                        }
                    }
                }
            }
        }

    }

    // internal - add the shortcuts for this frame to the mapping
    private void addShortcuts(Node frame) {
        if (frame.getNodeName().equals("frame")) {
            // get the frame name
            String frameName = ((Element) frame).getAttribute("name");
            NodeList list = frame.getChildNodes();
            ArrayList<ShortcutObject> shortcutObjectArray = new ArrayList<ShortcutObject>();
            map.put(frameName, shortcutObjectArray); // add to mapping
            for (int i = 0, len = list.getLength(); i < len; i++) {
                if (list.item(i).getNodeName().equals("shortcut")) {
                    ShortcutObject shortcutObject = new ShortcutObject();

                    Element e = (Element) list.item(i);
                    // store
                    shortcutObject.actionName = e.getAttribute("action");
                    shortcutObject.caption = e.getAttribute("caption");
                    // build the keystroke
                    int controller = 0;
                    if (e.getAttribute("ctrl").equals("yes")) {
                        controller = controller | InputEvent.CTRL_DOWN_MASK | InputEvent.CTRL_MASK;
                    }
                    if (e.getAttribute("alt").equals("yes")) {
                        controller = controller | InputEvent.ALT_DOWN_MASK | InputEvent.ALT_MASK;
                    }
                    if (e.getAttribute("shift").equals("yes")) {
                        controller = controller | InputEvent.SHIFT_DOWN_MASK | InputEvent.SHIFT_MASK;
                    }
                    KeyStroke keyStroke = KeyStroke.getKeyStroke(e.getAttribute("key").toCharArray()[0], controller);
                    shortcutObject.keyStroke = keyStroke;
                    // check if this keystroke is valid
                    if (!isValidShortcut(keyStroke)) {
                        System.err.println(
                                "Error: Invalid shortcut \"" + asString(keyStroke) +
                                        "\" in xml file for action \"" + shortcutObject.actionName + "\" " +
                                        "and frame \"" + frameName + "\"."
                        );
                    }
                    // check if KeyStroke is free
                    if (!isFreeShortcut(frameName, keyStroke)) {
                        System.err.println(
                                "Error: Duplicate shortcut \"" + asString(keyStroke) +
                                        "\" in xml file for action \"" + shortcutObject.actionName + "\" " +
                                        "and frame \"" + frameName + "\"."
                        );
                    }
                    // check if action is free (only allow one shortcut / action)
                    if (usedAction(frameName, shortcutObject.actionName)) {
                        System.err.println(
                                "Error: Duplicate action \"" + shortcutObject.actionName +
                                        "\" in xml file for shortcut \"" + asString(keyStroke) + "\" " +
                                        "and frame \"" + frameName + "\"."
                        );
                    }
                    // check if caption is free (only allow one shortcut / caption)
                    if (usedCaption(frameName, shortcutObject.caption)) {
                        System.err.println(
                                "Error: Duplicate caption id \"" + shortcutObject.caption +
                                        "\" in xml file for action \"" + shortcutObject.actionName + "\" " +
                                        "and frame \"" + frameName + "\"."
                        );
                    }
                    shortcutObjectArray.add(shortcutObject);
                }
            }
        }
    }

    // initial registration of frames
    @Override
    public void registerFrame(final JComponent frame) {
        if (map.containsKey(frame.getName())) {
            ArrayList<ShortcutObject> shortcutObjectArray = map.get(frame.getName());
            for (final ShortcutObject entry : shortcutObjectArray) {
                // to perform validity check we need to register this name
                actionManager.registerActionName(entry.actionName);
                // lazy action execution (the action might not be ready!)
                AbstractAction action = new AbstractAction() {
                    @Override
                    public void actionPerformed(ActionEvent evt) {
                        actionManager.getAction(entry.actionName).actionPerformed(evt);
                    }
                };
                frame.registerKeyboardAction(action, entry.keyStroke, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
                // store reference to frame
                entry.linkedFrame = frame;

                // to unregister
                // frame.unregisterKeyboardAction(keyStroke);

            }
        } else {
            System.err.println(
                    "Warning: No shortcut map defined for frame \"" + frame.getName() + "\"."
            );
        }

    }

}
