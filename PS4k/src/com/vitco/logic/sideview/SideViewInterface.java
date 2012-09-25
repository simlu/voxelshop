package com.vitco.logic.sideview;

import javax.swing.*;

/**
 * Creates one side view instance (one perspective) and the specific user interaction.
 */
public interface SideViewInterface {
    JPanel build();
    int getSide();
}
