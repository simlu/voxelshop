package com.vitco.app.layout.content.coloradjuster;

import com.vitco.app.manager.lang.LangSelectorInterface;

import javax.swing.*;
import java.awt.*;

/**
 * Interface for the content for the color adjuster frame.
 */
public interface ColorAdjusterViewInterface {
    void setLangSelector(LangSelectorInterface langSelector);

    JComponent build(Frame frame);
}
