package com.vitco.logic.shortcut;

import com.vitco.util.FileTools;
import com.vitco.util.action.ActionManager;
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
import javax.swing.FocusManager;
import javax.swing.text.JTextComponent;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Handles shortcut linking (logic)
 */
public class ShortcutManager implements ShortcutManagerInterface {

    // what shortcuts we allow
    private final ArrayList<Integer> VALID_KEYS_WITH_MODIFIER =
            new ArrayList<Integer>(Arrays.asList(new Integer[]{
                    // A-Z
                    65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77,
                    78, 79, 80, 81, 82, 83, 84, 85, 86, 87, 88, 89, 90,
                    // 0-9
                    48, 49, 50, 51, 52, 53, 54, 55, 56, 57,
                    // f1 - f12
                    112, 113, 114, 115, 116, 117, 118, 119, 120, 121, 122, 123,
                    // delete
                    127
            }));
    private final ArrayList<Integer> VALID_KEYS_WITHOUT_MODIFIER =
            new ArrayList<Integer>(Arrays.asList(new Integer[]{
                    // A-Z
                    65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77,
                    78, 79, 80, 81, 82, 83, 84, 85, 86, 87, 88, 89, 90,
                    // 0-9
                    48, 49, 50, 51, 52, 53, 54, 55, 56, 57,
                    // f1 - f12
                    112, 113, 114, 115, 116, 117, 118, 119, 120, 121, 122, 123,
                    // delete
                    127
            }));

    // global setting for all activatable actions
    boolean enableAllActivatableActions = true;

    // prototype of an action that can be disabled
    private final class ActivatableAction extends AbstractAction {
        private final AbstractAction action;
        private ActivatableAction(AbstractAction action) {
            this.action = action;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (enableAllActivatableActions) {
                action.actionPerformed(e);
            }
        }
    }

    // holds the mapping: frame -> (KeyStroke, actionName)
    private final Map<String, ArrayList<ShortcutObject>> map = new HashMap<String, ArrayList<ShortcutObject>>();

    // holds the global shortcuts (KeyStroke, actionName)
    private final ArrayList<ShortcutObject> global = new ArrayList<ShortcutObject>();
    // updated when global changes
    private final Map<KeyStroke, ShortcutObject> globalByKeyStroke = new HashMap<KeyStroke, ShortcutObject>();
    private final Map<String, ShortcutObject> globalByAction = new HashMap<String, ShortcutObject>();
    // notified when global changes
    private final ArrayList<GlobalShortcutChangeListener> globalShortcutChangeListeners =
            new ArrayList<GlobalShortcutChangeListener>();
    // hook that catches KeyStroke if this is registered as global
    private final KeyEventDispatcher globalProcessor = new KeyEventDispatcher() {
        @Override
        public boolean dispatchKeyEvent(KeyEvent e) {
            KeyStroke keyStroke = KeyStroke.getKeyStrokeForEvent(e);
            if (globalByKeyStroke.containsKey(keyStroke)
                    // only fire if all actions are activated
                    && enableAllActivatableActions) {
                // fire new action
                actionManager.getAction(globalByKeyStroke.get(keyStroke).actionName).actionPerformed(
                        new ActionEvent(e.getSource(), e.hashCode(), e.toString()) {}
                );
                e.consume(); // no-one else needs to handle this now
                return true; // no further action
            }
            return false; // might need further action
        }
    };

    // register global shortcuts and make sure all shortcuts are correctly enabled
    @Override
    public void registerShortcuts(final Frame frame) {
        // register global actions
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(globalProcessor);
        // update activity / inactivity of shortcuts
        FocusManager.getCurrentManager().addPropertyChangeListener(new PropertyChangeListener() {
            private Object activeWindow = null;
            private Object focusOwner = null;

            private void update() {
                if (activeWindow != frame || focusOwner instanceof JTextComponent) {
                    deactivateShortcuts();
                } else {
                    activateShortcuts();
                }
            }

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (evt.getPropertyName().equals("focusOwner")) {
                    focusOwner = evt.getNewValue();
                    update();
                }
                if (evt.getPropertyName().equals("activeWindow")) {
                    activeWindow = evt.getNewValue();
                    update();
                }
            }
        });
    }

    // executed when "global" shortcut change
    private void handleGlobalUpdate() {
        // rewrite the fast access data for global
        globalByKeyStroke.clear();
        globalByAction.clear();
        for (ShortcutObject shortcutObject : global) {
            globalByKeyStroke.put(shortcutObject.keyStroke, shortcutObject);
            globalByAction.put(shortcutObject.actionName, shortcutObject);
        }
        // notify all listeners
        for (GlobalShortcutChangeListener gscl : globalShortcutChangeListeners) {
            gscl.onChange();
        }
    }

    @Override
    public void addGlobalShortcutChangeListener(GlobalShortcutChangeListener globalShortcutChangeListener) {
        globalShortcutChangeListeners.add(globalShortcutChangeListener);
    }

    @Override
    public void removeGlobalShortcutChangeListener(GlobalShortcutChangeListener globalShortcutChangeListener) {
        globalShortcutChangeListeners.remove(globalShortcutChangeListener);
    }

    // var & setter
    private PreferencesInterface preferences;
    @Override
    public final void setPreferences(PreferencesInterface preferences) {
        this.preferences = preferences;
    }

    // var & setter
    private String xmlFile;
    @Override
    public final void setConfigFile(String filename) {
        xmlFile = filename;
    }

    // var & setter
    private ActionManager actionManager;
    @Override
    public final void setActionManager(ActionManager actionManager) {
        this.actionManager = actionManager;
    }

    // var & setter
    private ErrorHandlerInterface errorHandler;
    @Override
    public final void setErrorHandler(ErrorHandlerInterface errorHandler) {
        this.errorHandler = errorHandler;
    }

    // var & setter
    private LangSelectorInterface langSel;
    @Override
    public final void setLangSelector(LangSelectorInterface langSel) {
        this.langSel = langSel;
    }

    // get all frames as string array (frameKey, localized frameCaption)
    @Override
    public final String[][] getFrames() {
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


    // get global KeyStroke by action
    // returns null if not registered
    @Override
    public final KeyStroke getGlobalShortcutByAction(String actionName) {
       if (globalByAction.containsKey(actionName)) {
           return globalByAction.get(actionName).keyStroke;
       } else {
           return null;
       }
    }

    // get shortcuts as string array (localized caption, str representation)
    // for null this will return the global shortcuts
    @Override
    public final String[][] getShortcuts(String frameKey) {
        if (frameKey != null) { // frame shortcuts
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
        } else { // global shortcuts
            String[][] result = new String[global.size()][];
            for (int i = 0, len = global.size(); i < len; i++) {
                result[i] = new String[]{
                        langSel.getString(global.get(i).caption),
                        asString(global.get(i).keyStroke)
                };
            }
            return result;
        }
    }

    // update (keystroke,action) registration for a frame and shortcut id
    // if frame == null this will update a global shortcut
    @Override
    public final boolean updateShortcutObject(KeyStroke keyStroke, String frame, int id) {
        boolean result = false;
        if (frame != null) { // update frame shortcut
            // for frame
            if (map.containsKey(frame)) {
                if (map.get(frame).size() > id) {
                    final ShortcutObject shortcutObject = map.get(frame).get(id);
                    if (shortcutObject.keyStroke != null) {
                        shortcutObject.linkedFrame.unregisterKeyboardAction(shortcutObject.keyStroke); // un-register old
                    }
                    shortcutObject.keyStroke = keyStroke;
                    if (shortcutObject.keyStroke != null) {
                        // remember *this* action name
                        final String actionName = shortcutObject.actionName;
                        // lazy shortcut registration (the action might not be ready!)
                        actionManager.performWhenActionIsReady(actionName, new Runnable() {
                            @Override
                            public void run() {
                                shortcutObject.linkedFrame.registerKeyboardAction(
                                        new ActivatableAction(actionManager.getAction(actionName)),
                                        shortcutObject.keyStroke,
                                        JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT
                                );
                            }
                        });
                    }
                    result = true;
                }
            } else {
                System.err.println("Error: Can not set KeyStroke for frame \"" + frame + "\" and id \"" + id + "\".");
            }
        } else { // update global shortcut
            if (global.size() > id) {
                global.get(id).keyStroke = keyStroke;
                handleGlobalUpdate(); // need to rewrite the fast access
                result = true;
            } else {
                System.err.println("Error: Can not set global KeyStroke for id \"" + id + "\".");
            }
        }
        return result;
    }

    // convert KeyStroke to string representation
    @Override
    public final String asString(KeyStroke keyStroke) {
        return keyStroke == null ? "-" : keyStroke.toString()
                .replaceFirst("^pressed ", "") // remove if at very beginning (e.g. for f-keys)
                .replace("pressed", "+")
                .toUpperCase();
    }

    // check if this shortcut is not already used
    // if frame == null this will treat it as a global shortcut
    // otherwise as a shortcut for the frame
    // null is always a valid keyStroke
    @Override
    public final boolean isFreeShortcut(String frame, KeyStroke keyStroke) {
        boolean result = true;
        if (keyStroke != null) { // null is valid
            if (frame != null) { // check for this frame
                // check that this shortcut is not already set for an action in this frame
                if (map.containsKey(frame)) {
                    for (ShortcutObject shortcutObject : map.get(frame)) {
                        if (shortcutObject.keyStroke != null && shortcutObject.keyStroke.equals(keyStroke)) {
                            result = false;
                        }
                    }
                } else {
                    System.err.println("Error: Can not find frame \"" + frame + "\".");
                }
            } else { // check for all frames
                // check that this shortcut is not already set in any frame
                for (String key : map.keySet()) {
                    for (ShortcutObject shortcutObject : map.get(key)) {
                        if (shortcutObject.keyStroke != null && shortcutObject.keyStroke.equals(keyStroke)) {
                            result = false;
                        }
                    }
                }
            }
            // check for global shortcuts
            for (ShortcutObject shortcutObject : global) {
                if (shortcutObject.keyStroke != null && shortcutObject.keyStroke.equals(keyStroke)) {
                    result = false;
                }
            }
        }
        return result;
    }

    // check if this is a KeyStroke that has a valid format
    // null is a valid shortcut
    @Override
    public final boolean isValidShortcut(KeyStroke keyStroke) {
        // check that this is a format that is allowed as shortcut
        return keyStroke == null || ((keyStroke.getModifiers() == (InputEvent.CTRL_DOWN_MASK | InputEvent.CTRL_MASK)) ||
                (keyStroke.getModifiers() == (InputEvent.ALT_DOWN_MASK | InputEvent.ALT_MASK)) ||
                (keyStroke.getModifiers() == (InputEvent.SHIFT_DOWN_MASK | InputEvent.SHIFT_MASK)) ||
                (keyStroke.getModifiers() == ((InputEvent.CTRL_DOWN_MASK | InputEvent.CTRL_MASK) | (InputEvent.ALT_DOWN_MASK | InputEvent.ALT_MASK))) ||
                (keyStroke.getModifiers() == ((InputEvent.CTRL_DOWN_MASK | InputEvent.CTRL_MASK) | (InputEvent.SHIFT_DOWN_MASK | InputEvent.SHIFT_MASK))) ||
                (keyStroke.getModifiers() == ((InputEvent.ALT_DOWN_MASK | InputEvent.ALT_MASK) | (InputEvent.SHIFT_DOWN_MASK | InputEvent.SHIFT_MASK))))
                // allow only certain keys as trigger keys
                && VALID_KEYS_WITH_MODIFIER.contains(keyStroke.getKeyCode()) ||
                // allow some keys without modifiers
                ((keyStroke.getModifiers() == 0)
                        && VALID_KEYS_WITHOUT_MODIFIER.contains(keyStroke.getKeyCode()));
    }

    // internal - check that this action is not yet in this frame
    // if frameName == null it will check this for global shortcut actions
    private boolean usedAction(String frameName, String actionName) {
        boolean result = false;
        if (frameName != null) {
            // check that this shortcut is not already set for an action
            if (map.containsKey(frameName)) {
                for (ShortcutObject shortcutObject : map.get(frameName)) {
                    if (shortcutObject.actionName.equals(actionName)) {
                        result = true;
                    }
                }
            } else {
                System.err.println("Error: Can not find frame \"" + frameName + "\".");
            }
        } else { // check for global shortcuts
            for (ShortcutObject shortcutObject : global) {
                if (shortcutObject.actionName.equals(actionName)) {
                    result = true;
                }
            }
        }
        return result;
    }

    // internal - check that this caption (pre)localization is not yet used in this frame
    // if frameName == null it will check this for global shortcut captions
    private boolean usedCaption(String frameName, String caption) {
        boolean result = false;
        if (frameName != null) {
            // check that this shortcut is not already set for an action
            if (map.containsKey(frameName)) {
                for (ShortcutObject shortcutObject : map.get(frameName)) {
                    if (shortcutObject.caption.equals(caption)) {
                        result = true;
                    }
                }
            } else {
                System.err.println("Error: Can not find frame \"" + frameName + "\".");
            }
        } else { // check for global shortcuts
            for (ShortcutObject shortcutObject : global) {
                if (shortcutObject.caption.equals(caption)) {
                    result = true;
                }
            }
        }
        return result;
    }

    // store shortcuts in preferences
    @PreDestroy
    public void onDestruct() {
        preferences.storeObject("all_shortcuts_as_map", map);
        preferences.storeObject("global_shortcuts_as_map", global);
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
                // loop over all the windows (and global)
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

        // check if we have frame shortcuts stored
        if (preferences.contains("all_shortcuts_as_map")) {
            Map<String, ArrayList> tmp = FileTools.castHash(
                    (HashMap) preferences.loadObject("all_shortcuts_as_map"),
                    String.class,
                    ArrayList.class
            );
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

        // check if we have global shortcuts stored
        if (preferences.contains("global_shortcuts_as_map")) {
            ArrayList tmp = (ArrayList)preferences.loadObject("global_shortcuts_as_map");
            for (ShortcutObject so1 : global) {
                for (Object so2 : tmp) {
                    // only update existing actions
                    if (so1.actionName.equals(((ShortcutObject)so2).actionName)) {
                        so1.keyStroke = ((ShortcutObject)so2).keyStroke;
                    }
                }
            }
        }

        // make sure the list updated
        handleGlobalUpdate();

    }

    // check if there are duplicate shortcuts
    public final void doSanityCheck() {
        // check global keystrokes for duplicates
        HashMap<String, ShortcutObject> globalShortcuts = new HashMap<String, ShortcutObject>();
        for (ShortcutObject so : global) {
            String stroke = asString(so.keyStroke);
            if (globalShortcuts.containsKey(stroke)) {
                ShortcutObject dup = globalShortcuts.get(stroke);
                System.err.println("Warning: The two actions " +
                        so.actionName + " and " + dup.actionName +
                        " have the same global shortcut (" + stroke + ").");
            }
            globalShortcuts.put(stroke, so);
        }

        // check frame keystrokes for duplicates
        HashMap<String, ShortcutObject> shortcuts = new HashMap<String, ShortcutObject>();
        for (String key : map.keySet()) {
            shortcuts.clear();
            for (ShortcutObject so : map.get(key)) {
                String stroke = asString(so.keyStroke);
                if (shortcuts.containsKey(stroke)) {
                    ShortcutObject dup = shortcuts.get(stroke);
                    System.err.println("Warning: The two actions " +
                            so.actionName + " and " + dup.actionName +
                            " for frame \"" + key + "\" have the same shortcut (" + stroke + ").");
                }
                // check if this clashes with a global shortcut
                if (globalShortcuts.containsKey(stroke)) {
                    ShortcutObject dup = globalShortcuts.get(stroke);
                    System.err.println("Warning: The two actions " +
                            dup.actionName + " (global) and " + so.actionName +
                            " (frame \"" + key + "\") have the same shortcut (" + stroke + ").");
                }
                shortcuts.put(stroke, so);
            }
        }
    }

    // convert string into KeyCode
    // returns zero if no conversion found
    private int strToKeyCode(String str) {
        int result = 0;
        if (str.length() == 1) {
            result = str.toCharArray()[0];
        } else {
            if (str.equals("DEL")) {
                result = 127;
            } else
            if (str.startsWith("F")) {
                str = str.substring(1);
                try {
                    int tmp = Integer.valueOf(str);
                    if ((tmp >= 1) && (tmp <= 12)) {
                        result = 111 + tmp;
                    }
                } catch (NumberFormatException e) {
                    errorHandler.handle(e);
                }
            }
        }
        return result;
    }

    // internal - build a ShortcutObject from xml element
    // performs some sanity checks, frameName is only needed for error reporting
    // frameName should be null for global shortcuts
    private ShortcutObject buildShortcut(Element e, String frameName) {
        ShortcutObject shortcutObject = new ShortcutObject();
        // store
        shortcutObject.actionName = e.getAttribute("action");
        shortcutObject.caption = e.getAttribute("caption");
        // build the keystroke
        KeyStroke keyStroke = null;
        if (e.hasAttribute("key")) {
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
            keyStroke = KeyStroke.getKeyStroke(strToKeyCode(e.getAttribute("key")), controller);
        }
        shortcutObject.keyStroke = keyStroke;
        // check if this keystroke is valid
        if (!isValidShortcut(keyStroke)) {
            System.err.println(
                    "Error: Invalid shortcut \"" + asString(keyStroke) +
                            "\" in xml file for action \"" + shortcutObject.actionName + "\" " +
                            (frameName == null
                                    ? "(global)."
                                    : "and frame \"" + frameName + "\".")
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
        // check if action is free (only allow one shortcut / action / frame)
        if (usedAction(frameName, shortcutObject.actionName)) {
            System.err.println(
                    "Error: Duplicate action \"" + shortcutObject.actionName +
                            "\" in xml file for shortcut \"" + asString(keyStroke) + "\" " +
                            "and frame \"" + frameName + "\"."
            );
        }
        // check if caption is free (only allow one shortcut / caption / frame)
        if (usedCaption(frameName, shortcutObject.caption)) {
            System.err.println(
                    "Error: Duplicate caption id \"" + shortcutObject.caption +
                            "\" in xml file for action \"" + shortcutObject.actionName + "\" " +
                            "and frame \"" + frameName + "\"."
            );
        }
        return shortcutObject;
    }

    // internal - add the shortcuts for this frame to the mapping
    private void addShortcuts(Node frame) {
        if (frame.getNodeName().equals("global")) { // this is the global shortcut list
            NodeList list = frame.getChildNodes();
            for (int i = 0, len = list.getLength(); i < len; i++) {
                if (list.item(i).getNodeName().equals("shortcut")) {
                    Element e = (Element) list.item(i);
                    global.add(buildShortcut(e, null)); // add to global
                }
            }

        } else if (frame.getNodeName().equals("frame")) { // this is a frame shortcut list
            // get the frame name
            String frameName = ((Element) frame).getAttribute("name");
            NodeList list = frame.getChildNodes();
            ArrayList<ShortcutObject> shortcutObjectArray = new ArrayList<ShortcutObject>();
            map.put(frameName, shortcutObjectArray); // add to mapping
            for (int i = 0, len = list.getLength(); i < len; i++) {
                if (list.item(i).getNodeName().equals("shortcut")) {
                    Element e = (Element) list.item(i);
                    // add to this frame
                    shortcutObjectArray.add(buildShortcut(e, frameName));
                }
            }
        }
    }

    // register all actions of global shortcuts, to perform validity check
    @Override
    public void registerGlobalShortcutActions() {
        for (ShortcutObject shortcutObject : global) {
            actionManager.registerActionIsUsed(shortcutObject.actionName);
        }
    }

    // activate global shortcuts
    @Override
    public void activateShortcuts() {
        enableAllActivatableActions = true;
    }

    // deactivate global shortcuts
    @Override
    public void deactivateShortcuts() {
        enableAllActivatableActions = false;
    }

    // initial registration of frames
    @Override
    public void registerFrame(final JComponent frame) {
        if (map.containsKey(frame.getName())) {
            ArrayList<ShortcutObject> shortcutObjectArray = map.get(frame.getName());
            for (final ShortcutObject entry : shortcutObjectArray) {
                // to perform validity check we need to register this name
                actionManager.registerActionIsUsed(entry.actionName);
                if (entry.keyStroke != null) {
                    // lazy shortcut registration (the action might not be ready!)
                    actionManager.performWhenActionIsReady(entry.actionName, new Runnable() {
                        @Override
                        public void run() {
                            frame.registerKeyboardAction(
                                    new ActivatableAction(actionManager.getAction(entry.actionName)),
                                    entry.keyStroke,
                                    JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT
                            );
                        }
                    });
                }
                // store reference to frame
                entry.linkedFrame = frame;
            }
        } else {
            System.err.println(
                    "Warning: No shortcut map defined for frame \"" + frame.getName() + "\"."
            );
        }

    }

}
