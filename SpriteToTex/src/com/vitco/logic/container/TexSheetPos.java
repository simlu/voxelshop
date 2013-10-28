package com.vitco.logic.container;

import java.awt.*;

/**
 * Position of a sprite in a texSheet
 */
public final class TexSheetPos {
    public final int positionIn;
    public final Point point;

    public TexSheetPos(int positionIn, Point point) {
        this.positionIn = positionIn;
        this.point = point;
    }
}
