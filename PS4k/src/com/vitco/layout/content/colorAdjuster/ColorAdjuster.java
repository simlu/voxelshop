package com.vitco.layout.content.colorAdjuster;

import com.vitco.core.data.Data;
import com.vitco.core.data.container.Voxel;
import com.vitco.layout.content.colorchooser.basic.ColorChangeListener;
import com.vitco.layout.content.colorchooser.components.colorslider.HSBTab;
import com.vitco.manager.lang.LangSelectorInterface;
import com.vitco.util.misc.ColorTools;
import org.springframework.beans.factory.annotation.Autowired;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Content for the color adjuster frame.
 */
public class ColorAdjuster implements ColorAdjusterInterface {

    // var & setter (can not be interface!!)
    protected Data data;
    @Autowired
    public final void setData(Data data) {
        this.data = data;
    }

    // var & setter
    protected LangSelectorInterface langSelector;
    @Override
    public final void setLangSelector(LangSelectorInterface langSelector) {
        this.langSelector = langSelector;
    }

    private final HSBTab hsb = new HSBTab();
    private final JButton apply = new JButton();
    private final JButton cancel = new JButton();

    private boolean active = false;
    private void setActive(boolean flag) {
        active = flag;
        apply.setEnabled(flag);
        apply.setBackground(flag ? Color.red : Color.gray);
        cancel.setEnabled(flag);
        if (!active) {
            hsb.setColor(ColorTools.hsbToColor(new float[]{0.5f, 0.5f, 0.5f}));
        }
        data.freeze(flag);
    }
    private boolean isActive() {
        return active;
    }

    @Override
    public JComponent build(Frame frame) {
        setActive(false);

        apply.setText(langSelector.getString("apply"));
        cancel.setText(langSelector.getString("cancel"));

        JPanel wrapper = new JPanel();
        wrapper.setLayout(new BorderLayout());

        final JPanel action = new JPanel();
        action.setLayout(new GridLayout());
        wrapper.add(action, BorderLayout.SOUTH);

        apply.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                setActive(false);
            }
        });
        action.add(apply);

        cancel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                setActive(false);
                data.undoV();
            }
        });
        action.add(cancel);

        hsb.addColorChangeListener(new ColorChangeListener() {
            @Override
            public void colorChanged(float[] hsb) {
                data.freeze(false);
                if (isActive()) {
                    data.undoV();
                }
                data.massShiftColor(Voxel.convertVoxelsToIdArray(data.getSelectedVoxels()), new float[] {
                        hsb[0] * 2 - 1f, hsb[1] * 2 - 1f, hsb[2] * 2 - 1f
                });
                setActive(true);
            }
        });
        wrapper.add(hsb, BorderLayout.CENTER);

        return wrapper;
    }
}
