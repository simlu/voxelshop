package com.vitco.util.components.dialog;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Needs to be extended by all objects that are added to a UserInputDialog.
 */
public class BlankDialogModule extends JComponent {
    // indicates ready status of component
    private boolean ready = true;

    // the child modules of this module
    private final HashSet<BlankDialogModule> childModules = new HashSet<BlankDialogModule>();

    // maps ids to the child modules (needs to be separate if several child modules have the same identifier)
    private final HashMap<String, BlankDialogModule> id2ChildModule = new HashMap<String, BlankDialogModule>();

    // the identifier of this object
    private final String identifier;
    public final String getIdentifier() {
        return identifier;
    }

    // the listener of this module
    private final ArrayList<DialogModuleChangeListener> listeners = new ArrayList<DialogModuleChangeListener>();
    // add a state change listener
    protected final void addListener(DialogModuleChangeListener listener) {
        listeners.add(listener);
    }

    // listener that is used to propagate events from all children to
    // this module (so we always receive it at the top level)
    private final DialogModuleChangeListener propagationListener = new DialogModuleChangeAdapter() {
        @Override
        public void onReadyStateChanged() {
            notifyReadyStateChanged();
        }

        @Override
        public void onContentChanged() {
            notifyContentChanged();
        }
    };

    // called when the ready state of this component has changed
    protected final void notifyReadyStateChanged() {
        // notify all listeners (including parents)
        for (DialogModuleChangeListener listener : listeners) {
            listener.onReadyStateChanged();
        }
    }

    // called when the content of this component has changed
    // modules that overwrite their values should call this when the
    // value changes
    protected final void notifyContentChanged() {
        // notify all listeners (including parents)
        for (DialogModuleChangeListener listener : listeners) {
            listener.onContentChanged();
        }
    }

    // --------------

    // constructor
    public BlankDialogModule(String identifier) {
        // spacing below (is removed if child modules are added)
        setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        // set the layout
        setLayout(new GridBagLayout());
        // store identifer
        this.identifier = identifier;
    }

    // -------------------

    // set enabled lookup (format "[identifier]=[string]")
    private final String[] lookups = new String[4];

    public final void setEnabledLookup(String lookup) {
        lookups[0] = lookup;
    }
    public final void setDisabledLookup(String lookup) {
        lookups[1] = lookup;
    }
    public final void setVisibleLookup(String lookup) {
        lookups[2] = lookup;
    }
    public final void setInvisibleLookup(String lookup) {
        lookups[3] = lookup;
    }

    // called to update the state of this component
    protected void refreshState(BlankDialogModule topLevelParent) {
        boolean visible = true;
        boolean enabled = true;
        for (int i = 0; i < 4; i++) {
            if (lookups[i] != null) {
                // split into different parts
                String[] parts = lookups[i].split("&");
                int matchCount = 0;
                // loop over parts (we need to check all of them)
                for (String part : parts) {
                    // split into key and possible values
                    String[] lookup = part.split("=");
                    if (lookup.length == 2) {
                        // get the value for the key
                        Object value = topLevelParent.getValue(lookup[0]);
                        if (value != null) {
                            // split possible values and see if one matches
                            String[] possibleValues = lookup[1].split(",");
                            boolean matched = false;
                            for (String possibleValue : possibleValues) {
                                if (possibleValue.equals(value.toString())) {
                                    matched = true;
                                    break;
                                }
                            }
                            // we matched this part, increase counter
                            if (matched) {
                                matchCount++;
                            } else {
                                break;
                            }
                        }
                    }
                }
                boolean matched = matchCount == parts.length;
                switch (i) {
                    case 0:
                        if (!matched) {
                            enabled = false;
                        }
                        break;
                    case 1:
                        if (matched) {
                            enabled = false;
                        }
                        break;
                    case 2:
                        if (!matched) {
                            visible = false;
                        }
                        break;
                    default:
                        if (matched) {
                            visible = false;
                        }
                        break;
                }
            }
        }
        if (lookups[0] != null || lookups[1] != null) {
            this.setDeepEnabled(enabled);
        }
        if (lookups[2] != null || lookups[3] != null) {
            this.setVisible(visible);
        }
        // refresh all children
        for (BlankDialogModule child : childModules) {
            child.refreshState(topLevelParent);
        }
    }

    // disable all children when this is triggered
    private void setDeepEnabled(boolean value) {
        ArrayList<Component> components = new ArrayList<Component>();
        components.add(this);
        while (!components.isEmpty()) {
            Component com = components.remove(0);
            com.setEnabled(value);
            if (com instanceof Container) {
                Collections.addAll(components, ((Container) com).getComponents());
            }
        }
    }

    // ------------------

    // add a child module to this module
    protected final void addModule(BlankDialogModule module, boolean display) {
        // remove spacing (the child deals with that now)
        setBorder(BorderFactory.createEmptyBorder());
        // register this module
        childModules.add(module);
        id2ChildModule.put(module.getIdentifier(), module);
        // inject listener
        module.addListener(propagationListener);
        // add child module to this container to be displayed
        if (display) {
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.gridwidth = GridBagConstraints.REMAINDER;
            gbc.weightx = 1;
            this.add(module, gbc);
        }
    }

    // set ready status of component
    protected final void setReady(boolean ready) {
        if (this.ready != ready) {
            this.ready = ready;
            notifyReadyStateChanged();
        }
    }

    // returns true if this component and all sub components are ready
    protected final boolean isReady() {
        // check if visible (invisible components are ok to be not ready)
        if (!this.isVisible()) {
            return true;
        }
        if (!this.isDisplayable()) {
            return true;
        }
        // check if this is not ready
        if (!ready) {
            return false;
        }
        // check if any child is not ready
        for (BlankDialogModule module : childModules) {
            if (!module.isReady()) {
                return false;
            }
        }
        // all ready!
        return true;
    }

    // get the value of this object
    // needs to be overwritten for objects that do not
    // get their value from their children
    protected String getValue(String identifier) {
        if (identifier == null) {
            return null;
        }
        String[] path = identifier.split("\\.", 2);
        BlankDialogModule object = id2ChildModule.get(path[0]);
        if (object != null) {
            return object.getValue(path.length > 1 ? path[1] : null);
        }
        return null;
    }

    // retrieve the serialization of this object
    // needs to be overwritten if the object wants to store its value (serialization)
    protected ArrayList<String[]> getSerialization(String currentIdentifier) {
        ArrayList<String[]> keyValueSet = new ArrayList<String[]>();
        for (BlankDialogModule child : childModules) {
            keyValueSet.addAll(child.getSerialization(
                            currentIdentifier + (currentIdentifier.equals("") ? "" : ".") + child.getIdentifier())
            );
        }
        return keyValueSet;
    }

    // load a key
    // needs to be overwritten if the object wants to restore its value (deserialization)
    protected boolean loadValue(String[] pair) {
        String[] path = pair[0].split("\\.", 2);
        BlankDialogModule object = id2ChildModule.get(path[0]);
        pair = new String[]{path.length > 1 ? path[1] : "", pair[1]};
        return object != null && object.loadValue(pair);
    }

}
