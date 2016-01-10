package com.vitco.layout.content.colorAdjuster;

import com.vitco.manager.lang.LangSelectorInterface;

import javax.swing.*;
import java.awt.*;

/**
 * Interface for the content for the color adjuster frame.
 */
public interface ColorAdjusterInterface {
    void setLangSelector(LangSelectorInterface langSelector);

    JComponent build(Frame frame);
}
