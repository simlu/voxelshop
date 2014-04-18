package com.vitco.export.container;

import com.vitco.core.data.Data;
import com.vitco.core.data.container.Voxel;
import com.vitco.util.graphic.G2DUtil;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashMap;

/**
 * Represents a texture that belongs to a triangle.
 */
public class TriTexture {

    // interpolation value
    private static final float interp = 0.00001f;

    // ---------------

    // reference to the the uvs
    private final TexTriUV[] texTriUVs = new TexTriUV[3];

    // reference to the uv points
    private final float[][] uvPoints = new float[3][2];

    // holds the pixels in this triangle
    // the format is (x, y, color)
    private final HashMap<Point, int[]> pixels = new HashMap<Point, int[]>();

    // size of this texture image
    private final int width;
    private final int height;

    // reference to texture manager
    private final TriTextureManager textureManager;

    // ---------------

    // -- parent texture (i.e. this texture is part of the parent texture)
    private TriTexture parentTexture = null;

    private final float[] topLeft = new float[] {0,0};
    private final float[] scale = new float[] {1,1};
    private boolean flip = false;
    private int rotation = 0;

    // false if the uv of this texture is outdated
    private TriTexture lastTopTexture = null;

    // #################################

    // set parent texture for this texture
    // Note: A parent texture is a texture that contains this texture
    public final void setParentTexture(TriTexture parentTexture, float[] topLeft, float[] scale, boolean flip, int rotation) {
        this.parentTexture = parentTexture;
        // store the uv translation values for this texture
        if (topLeft != null) {
            this.topLeft[0] = topLeft[0];
            this.topLeft[1] = topLeft[1];
        }
        if (scale != null) {
            this.scale[0] = scale[0];
            this.scale[1] = scale[1];
        }
        this.flip = flip;
        this.rotation = rotation;
    }

    // get identifier of this texture
    // (only assigned when this function is accessed to avoid high ids)
    public final int getId() {
        // return parent id
        if (parentTexture != null) {
            return parentTexture.getId();
        }
        // else return this id
        return textureManager.getId(this);
    }

    // retrieve image representation of this texture
    public final BufferedImage getImage() {
        // return parent texture image
        if (parentTexture != null) {
            return parentTexture.getImage();
        }
        // else compute the image for this texture
        BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        for (int[] pixel : pixels.values()) {
            result.setRGB(pixel[0], pixel[1], pixel[2]);
        }
        return result;
    }

    // recursively retrieve parent texture
    // (or the texture itself if no parent is set)
    public final TriTexture getTopTexture() {
        if (parentTexture == null) {
            return this;
        } else {
            return parentTexture.getTopTexture();
        }
    }

    // check if the uv mapping of this texture is valid
    public final boolean hasValidUV() {
        // the uv mapping is likely to have changed when the parent texture changed
        return lastTopTexture == getTopTexture();
    }

    // ################################

    // make sure the uv of this texture is validated
    // against the assigned triangle
    public final void validateUVMapping() {
        if (!hasValidUV()) {
            // -- set the uv for the points
            // compute top left point and range
            float minCornerX = Math.min(Math.min(uvPoints[0][0], uvPoints[1][0]), uvPoints[2][0]);
            float rangeX = Math.max(Math.max(uvPoints[0][0], uvPoints[1][0]), uvPoints[2][0]) - minCornerX;
            float minCornerY = Math.min(Math.min(uvPoints[0][1], uvPoints[1][1]), uvPoints[2][1]);
            float rangeY = Math.max(Math.max(uvPoints[0][1], uvPoints[1][1]), uvPoints[2][1]) - minCornerY;
            // compute uvs
            float[][] uvs = new float[][]{
                    new float[]{(uvPoints[0][0] - minCornerX) / rangeX, 1 - (uvPoints[0][1] - minCornerY) / rangeY},
                    new float[]{(uvPoints[1][0] - minCornerX) / rangeX, 1 - (uvPoints[1][1] - minCornerY) / rangeY},
                    new float[]{(uvPoints[2][0] - minCornerX) / rangeX, 1 - (uvPoints[2][1] - minCornerY) / rangeY}
            };
            // -- interpolation
            // compute the center of the uv triangle
            float[] center = new float[]{
                    (uvs[0][0] + uvs[1][0] + uvs[2][0]) / 3,
                    (uvs[0][1] + uvs[1][1] + uvs[2][1]) / 3
            };
            // compute the offsets (the direction we need to interpolate in)
            float[][] offsets = new float[][]{
                    new float[]{center[0] - uvs[0][0], center[1] - uvs[0][1]},
                    new float[]{center[0] - uvs[1][0], center[1] - uvs[1][1]},
                    new float[]{center[0] - uvs[2][0], center[1] - uvs[2][1]}
            };
            // normalize these offsets using the minimum
            // Note: The minimum is used to ensure that sides of the uv
            // triangle that were parallel stay parallel after interpolation
            double dist = Math.min(
                    Math.min(
                            Math.sqrt(Math.pow(offsets[0][0], 2) + Math.pow(offsets[0][1], 2)),
                            Math.sqrt(Math.pow(offsets[1][0], 2) + Math.pow(offsets[1][1], 2))
                    ),
                    Math.sqrt(Math.pow(offsets[2][0], 2) + Math.pow(offsets[2][1], 2))
            );
            offsets[0][0] /= dist * width;
            offsets[0][1] /= dist * height;
            offsets[1][0] /= dist * width;
            offsets[1][1] /= dist * height;
            offsets[2][0] /= dist * width;
            offsets[2][1] /= dist * height;
            // do the actual interpolation and set the uvs
            texTriUVs[0].set(uvs[0][0] + offsets[0][0] * interp, uvs[0][1] + offsets[0][1] * interp);
            texTriUVs[1].set(uvs[1][0] + offsets[1][0] * interp, uvs[1][1] + offsets[1][1] * interp);
            texTriUVs[2].set(uvs[2][0] + offsets[2][0] * interp, uvs[2][1] + offsets[2][1] * interp);
            // validate (that the uv representation is ok now)
            lastTopTexture = getTopTexture();
        }
    }

    // constructor
    public TriTexture(
            TexTriUV uv1, float xf1, float yf1,
            TexTriUV uv2, float xf2, float yf2,
            TexTriUV uv3, float xf3, float yf3,
            int depth,
            TexTriangle texTri, Data data,
            TriTextureManager textureManager
    ) {
        // store variables internally
        uvPoints[0][0] = xf1;
        uvPoints[0][1] = yf1;
        uvPoints[1][0] = xf2;
        uvPoints[1][1] = yf2;
        uvPoints[2][0] = xf3;
        uvPoints[2][1] = yf3;
        texTriUVs[0] = uv1;
        texTriUVs[1] = uv2;
        texTriUVs[2] = uv3;

        // store texture manager reference
        this.textureManager = textureManager;

        // compute the voxels that are inside this triangle
        // (so we're not using any "extra" pixels in the buffered image)
        int[][] points = G2DUtil.getTriangleGridIntersection(
                (int)(xf1), (int)(yf1),
                (int)(xf2), (int)(yf2),
                (int)(xf3), (int)(yf3)
        );

        // get min/max
        int minX = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int minY = Integer.MAX_VALUE;
        int maxY = Integer.MIN_VALUE;
        for (int[] p : points) {
            minX = Math.min(p[0], minX);
            maxX = Math.max(p[0], maxX);
            minY = Math.min(p[1], minY);
            maxY = Math.max(p[1], maxY);
        }

        // compute the image size for this texture image
        int width = maxX - minX + 1;
        int height = maxY - minY + 1;

        // get orientation
        int axis = texTri.getOrientation()/2;

        // fetch colors
        for (int[] point : points) {
            // set the position (for this color)
            Point p = new Point(point[0] - minX, point[1] - minY);
            // get the pixel color
            Voxel voxel = data.searchVoxel(new int[] {
                    axis == 0 ? depth : point[0],
                    axis == 1 ? depth : (axis == 0 ? point[0] : point[1]),
                    axis == 2 ? depth : point[1],
            }, false);
            assert voxel != null;
            // add the pixel
            pixels.put(p, new int[] {p.x, p.y, voxel.getColor().getRGB()});
        }

        // compress this texture
        int[] size = compress(width, height);

        // finalize width and height
        this.width = size[0];
        this.height = size[1];
    }

    // helper to compress this image
    // returns the new width of the image
    @SuppressWarnings("ConstantConditions")
    private int[] compress(int width, int height) {
        // -- check if texture can be downscaled
        if (width > 1) {
            for (int d = 1, len = (int) Math.sqrt(width) + 1; d < len; d++) {
                loop:
                if (width % d == 0) {
                    // potential new pixel representation
                    HashMap<Point, int[]> result = new HashMap<Point, int[]>();
                    // the step width that would be compressed to one pixel
                    int stepSize = width / d;
                    // loop over all steps
                    for (int x = 0; x < d; x++) {
                        // loop over height
                        for (int y = 0; y < height; y++) {
                            Integer lastColor = null;
                            // loop over step
                            for (int i = 0; i < stepSize; i++) {
                                // compute the current point
                                Point p = new Point(x * stepSize + i, y);
                                // obtain the pixel
                                int[] pixel = pixels.get(p);
                                if (pixel != null) {
                                    // check if the pixel color is consistent through this step
                                    if (lastColor == null) {
                                        lastColor = pixel[2];
                                        result.put(new Point(x, y), new int[] {x,y,pixel[2]});
                                    } else {
                                        if (lastColor != pixel[2]) {
                                            break loop;
                                        }
                                    }
                                }
                            }
                        }
                    }
                    // loop was successful
                    pixels.clear();
                    pixels.putAll(result);
                    width /= stepSize;
                    break;
                }
            }
        }

        if (height > 1) {
            for (int d = 1, len = (int) Math.sqrt(height) + 1; d < len; d++) {
                loop:
                if (height % d == 0) {
                    // potential new pixel representation
                    HashMap<Point, int[]> result = new HashMap<Point, int[]>();
                    // the step height that would be compressed to one pixel
                    int stepSize = height / d;
                    // loop over all steps
                    for (int y = 0; y < d; y++) {
                        // loop over width
                        for (int x = 0; x < width; x++) {
                            Integer lastColor = null;
                            // loop over step
                            for (int i = 0; i < stepSize; i++) {
                                // compute the current point
                                Point p = new Point(x, y * stepSize + i);
                                // obtain the pixel
                                int[] pixel = pixels.get(p);
                                if (pixel != null) {
                                    // check if the pixel color is consistent through this step
                                    if (lastColor == null) {
                                        lastColor = pixel[2];
                                        result.put(new Point(x, y), new int[] {x,y,pixel[2]});
                                    } else {
                                        if (lastColor != pixel[2]) {
                                            break loop;
                                        }
                                    }
                                }
                            }
                        }
                    }
                    // loop was successful
                    pixels.clear();
                    pixels.putAll(result);
                    height /= stepSize;
                    break;
                }
            }
        }

        return new int[] {width, height};
    }
}
