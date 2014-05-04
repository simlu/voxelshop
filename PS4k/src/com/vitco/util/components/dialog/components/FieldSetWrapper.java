package com.vitco.util.components.dialog.components;

import com.vitco.util.components.dialog.BlankDialogModule;
import com.vitco.util.components.dialog.DialogModuleChangeAdapter;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.font.TextAttribute;
import java.util.HashMap;
import java.util.Map;

/**
 * A fieldSet that wraps other fieldSets
 */
public class FieldSetWrapper extends BlankDialogModule {

    // space of the drawn border at the top
    // (so that the component is "half way on the line")
    private final int topSpace;

    // drop down menu used to select the active menu
    private final ComboBoxModule comboBox;

    // maps identifiers to ids
    private final HashMap<String, Integer> identifier2comboId = new HashMap<String, Integer>();

    // currently selected id
    private int selectedId = 0;

    // border spacing below every wrapper
    private static final int BORDER_BELOW = 10;

    // padding around content (inside of wrapper) - top, left, bottom, right
    private static final int[] PADDING = new int[] {10, 10, 5, 10};

    // component that holds the main content
    private JPanel content = new JPanel();

    // called to initialize this container
    private void init() {
        // set the layout
        setLayout(new BorderLayout());
        // add space below each wrapper
        setBorder(BorderFactory.createEmptyBorder(0, 0, BORDER_BELOW, 0));
        // add content container
        add(content, BorderLayout.CENTER);
        // make content container transparent
        content.setOpaque(false);
        // set layout for the content
        content.setLayout(new GridBagLayout());
        // create a border around the content
        content.setBorder(BorderFactory.createEmptyBorder(PADDING[0], PADDING[1], PADDING[2], PADDING[3]));
    }

    // add the title component (that lies on the drawn border)
    private void addTitleComponent(JComponent component) {
        // create wrapper for "left alignment"
        JPanel panel = new JPanel();
        // make wrapper transparent
        panel.setOpaque(false);
        // shift the title component a bit to the right
        panel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
        panel.setLayout(new BorderLayout());
        // add to left
        panel.add(component, BorderLayout.WEST);
        // add to top
        add(panel, BorderLayout.NORTH);
    }

    // sets/replaces the core component
    private void setBodyComponent(JComponent component) {
        // make sure there are no components in the content anymore
        content.removeAll();
        // add new component to body
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.weightx = 1;
        gbc.anchor = GridBagConstraints.NORTH;
        gbc.weighty = 1;
        content.add(component, gbc);
        // refresh and redraw the body
        content.revalidate();
        content.repaint();
    }

    // constructor (for multi select fieldSets)
    public FieldSetWrapper(String identifier, final FieldSet[] dropFieldSets, int selected) {
        super(identifier);
        init(); // initialize things

        // --------

        // add combo box menu
        String[][] displayedStrings = new String[dropFieldSets.length][];
        int maxHeight = 0;
        for (int i = 0; i < dropFieldSets.length; i++) {
            // set drop down text entries
            displayedStrings[i] = new String[] {dropFieldSets[i].getIdentifier(), dropFieldSets[i].getCaption()};
            maxHeight = Math.max(maxHeight, dropFieldSets[i].getPreferredSize().height);
            identifier2comboId.put(dropFieldSets[i].getIdentifier(), i);
        }
        // always enforce that content is maximum height
        content.setPreferredSize(new Dimension(content.getPreferredSize().width, maxHeight + PADDING[0] + PADDING[2]));
        // store selected id
        selectedId = selected;
        // create the combo box
        comboBox = new ComboBoxModule("combobox", displayedStrings, selected);
        // disable spacing
        comboBox.setBorder(BorderFactory.createEmptyBorder());
        // register comboBox as a module
        addModule(comboBox, false);
        // compute the top space (the drawn line offset from the top border)
        topSpace = comboBox.getPreferredSize().height/2;
        // listen to content changes
        addListener(new DialogModuleChangeAdapter() {
            @Override
            public void onContentChanged() {
                super.onContentChanged();
                Integer id = identifier2comboId.get(comboBox.getValue(null));
                // prevent unnecessary refresh
                if (id != null && id != selectedId) {
                    selectedId = id;
                    setBodyComponent(dropFieldSets[id]);
                }
            }
        });

        // --------

        // add the combo box as drop down menu
        addTitleComponent(comboBox);
        // add the selected fieldSet as content
        setBodyComponent(dropFieldSets[selected]);
    }

    // constructor (for single fieldSet - this used a JLabel instead of a comboBox)
    public FieldSetWrapper(FieldSet fieldSet) {
        super("");  // no need for identifier since this will not be accessed
        init(); // initialize things

        // -----

        // create a caption label
        JLabel label = new JLabel(fieldSet.getCaption());
        // overwrite background (i.e. the drawn border line)
        label.setOpaque(true);

        // make the font of the label a bit different
        Map<TextAttribute, Object> attributes = new HashMap<TextAttribute, Object>();
        attributes.put(TextAttribute.TRACKING, 0.1);
        attributes.put(TextAttribute.WEIGHT, TextAttribute.WEIGHT_BOLD);
        label.setFont(label.getFont().deriveFont(attributes));
        // compute the top space
        topSpace = label.getPreferredSize().height/2;
        // set drop list to null (unused)
        comboBox = null;

        // always enforce that content is maximum height
        content.setPreferredSize(new Dimension(content.getPreferredSize().width, fieldSet.getPreferredSize().height + PADDING[0] + PADDING[2]));

        // -----

        // add the label as "title" of this fieldSet
        addTitleComponent(label);
        // add the fieldSet as content
        setBodyComponent(fieldSet);
    }

    // used to draw the border "manually"
    private final TitledBorder titledBorder = BorderFactory.createTitledBorder("");

    // retrieve the selector that was selected in the combo box
    @Override
    public String getValue(String identifier) {
        return comboBox.getValue(identifier);
    }

    // overwrite paint method to draw border
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        // draw the outline (i.e. titleBorder)
        titledBorder.paintBorder(this, g, 0, topSpace, getWidth(), getHeight()-topSpace-BORDER_BELOW);
    }
}
