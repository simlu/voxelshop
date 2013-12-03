package com.vitco.engine.adapter.tools;

import com.vitco.engine.view.DrawContainer;

/**
 * Abstract of a animation tool that can be used to alter a animation data.
 */
public abstract class AbstractAnimationTool extends AbstractBasicTool {

    // constructor
    public AbstractAnimationTool(DrawContainer container, int side) {
        super(container, side);
    }

    @Override
    protected final void softCleanUp() {
        data.highlightPoint(-1);
    }

    @Override
    protected final boolean allowDrag() {
        return data.getHighlightedPoint() != -1;
    }

}
