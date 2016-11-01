package com.vitco.layout.content.mainview;

import javax.swing.*;
import com.vitco.layout.frames.MainViewLinkage;

/**
 * Interface: Creates the mian view instance and attaches the specific user interaction.
 */
public interface MainViewInterface {
    public JPanel build(MainViewLinkage linkage);
}
