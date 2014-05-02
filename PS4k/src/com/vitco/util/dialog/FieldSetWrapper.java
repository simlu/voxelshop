package com.vitco.util.dialog;

import com.jidesoft.swing.JideComboBox;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
    private final JideComboBox comboBox;

    // list that maps ids to identifier (for passed fieldSet Array)
    private final HashMap<Integer, String> id2Identifier = new HashMap<Integer, String>();

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
        String[] displayedStrings = new String[dropFieldSets.length];
        String longestString = "";
        int maxHeight = 0;
        for (int i = 0; i < dropFieldSets.length; i++) {
            // set drop down text entries
            displayedStrings[i] = dropFieldSets[i].getCaption();
            if (longestString.length() < displayedStrings[i].length()) {
                longestString = displayedStrings[i];
            }
            maxHeight = Math.max(maxHeight, dropFieldSets[i].getPreferredSize().height);
            // update identifier map
            id2Identifier.put(i, dropFieldSets[i].getIdentifier());
        }
        // always enforce that content is maximum height
        content.setPreferredSize(new Dimension(content.getPreferredSize().width, maxHeight + PADDING[0] + PADDING[2]));
        // create the combo box
        comboBox = new JideComboBox(displayedStrings) {
            @Override
            public Dimension getPreferredSize() {
                Dimension dimension = super.getPreferredSize();
                // add width to prevent dots in menu items (they show at the end otherwise!)
                return new Dimension(dimension.width + 30, dimension.height);
            }
        };
        // compute the top space
        topSpace = comboBox.getPreferredSize().height/2;
        // disable focus for combo box
        comboBox.setFocusable(false);
        // make sure the combo box is "long enough"
        comboBox.setPrototypeDisplayValue(longestString);
        // validate and set selected index
        selected = Math.min(dropFieldSets.length - 1, Math.max(0, selected));
        comboBox.setSelectedIndex(selected);
        // listen to select events of combo box
        comboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setBodyComponent(dropFieldSets[comboBox.getSelectedIndex()]);
                // notify that the content has changed
                notifyContentChanged();
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
    public Object getValue(String identifier) {
        if (comboBox != null) {
            return id2Identifier.get(comboBox.getSelectedIndex());
        } else {
            // not used since the single wrapper is not added to the main dialog content
            return super.getValue(identifier);
        }
    }

    // overwrite paint method to draw border
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        // draw the outline (i.e. titleBorder)
        titledBorder.paintBorder(this, g, 0, topSpace, getWidth(), getHeight()-topSpace-BORDER_BELOW);
    }
}
