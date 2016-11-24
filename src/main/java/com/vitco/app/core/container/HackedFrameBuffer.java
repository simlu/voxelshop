package com.vitco.app.core.container;

import com.threed.jpct.FrameBuffer;

/**
 * Exposes the zbuffer of the FrameBuffer
 */
public class HackedFrameBuffer extends FrameBuffer {

    // access to z buffer
    public int[] getZBuffer() {
        return zbuffer;
    }

    // constructor
    public HackedFrameBuffer(int i, int i2, int i3) {
        super(i, i2, i3);
    }
}
