package com.vitco.export.container;

import com.vitco.core.data.Data;

import java.awt.*;
import java.util.HashMap;

/**
 * Represents a texture that belongs to a triangle.
 */
public class TriTexture {
    // reference to tex triangle
    private final TexTriangle texTri;

    // reference to the the uvs
    private final HashMap<TexTriUV, float[]> info = new HashMap<TexTriUV, float[]>();

    // holds the pixels in this triangle
    // the format is (x, y, color)
    private final HashMap<Point, int[]> pixels = new HashMap<Point, int[]>();

    public TriTexture(
            TexTriUV uv1, float xf1, float yf1,
            TexTriUV uv2, float xf2, float yf2,
            TexTriUV uv3, float xf3, float yf3,
            TexTriangle texTri, Data data
    ) {
        this.texTri = texTri;
        info.put(uv1, new float[]{xf1, yf1});
        info.put(uv2, new float[]{xf2, yf2});
        info.put(uv3, new float[]{xf3, yf3});
    }
}
