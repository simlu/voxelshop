package com.vitco.export.container;

import com.vitco.core.data.Data;
import com.vitco.core.data.container.Voxel;
import com.vitco.util.graphic.G2DUtil;
import com.vitco.util.graphic.ImageComparator;

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

    // used for pixel comparison
    private final ImageComparator imageComparator;

    // ---------------

    // -- parent texture (i.e. this texture is part of the parent texture)
    private TriTexture parentTexture = null;

    // left top corner and orientation of this image in its parent image
    private final int[] leftTop = new int[] {0,0};
    private int orientationFlag = 0;

    // false if the uv of this texture is outdated
    private TriTexture lastTopTexture = null;

    // #################################

    // set parent texture for this texture
    // Note: A parent texture is a texture that contains this texture
    public final void setParentTexture(TriTexture parentTexture, int[] leftTop, int orientationFlag) {
        this.parentTexture = parentTexture;
        // store the uv translation values for this texture to parent
        if (leftTop != null) {
            this.leftTop[0] = leftTop[0];
            this.leftTop[1] = leftTop[1];
        }
        // Indicates the different orientation changes
        // 0 - original, 1 - rotated x 1, 2 - rotated x 2, 3 - rotated x 3,
        // 4 - flipped, 5 - flipped & rotated x 1, 6 - flipped & rotated x 2, 7 - flipped & rotated x 3
        // Note: Rotation is clockwise
        this.orientationFlag = orientationFlag;
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

    // return true if this has a parent texture
    // (otherwise this is a "top" texture)
    public final boolean hasParent() {
        return parentTexture != null;
    }

    // make the passed texture a child (if possible)
    // return true on success
    public final boolean makeChild(TriTexture child) {
        // -- can only make child if it has no parent
        if (child.hasParent()) {
            return false;
        }

        // -- check that we don't create infinite recursion
        if (this.getTopTexture() == child) {
            return false;
        }

        // -- check for containment position
        int[] pos = imageComparator.getPosition(child.imageComparator, null);
        if (pos != null) {
            child.setParentTexture(this,
                    new int[] {pos[0], pos[1]},
                    pos[2]);
            return true;
        }

        // -- no containment position found
        return false;
    }

    // ################################

    // helper function - swap x and y values
    private void swap(double[][] array) {
        double tmp = array[0][0];
        array[0][0] = array[0][1];
        array[0][1] = tmp;
        tmp = array[1][0];
        array[1][0] = array[1][1];
        array[1][1] = tmp;
        tmp = array[2][0];
        array[2][0] = array[2][1];
        array[2][1] = tmp;
    }

    // make sure the uv of this texture is validated
    // against the assigned triangle
    @SuppressWarnings("SuspiciousNameCombination")
    public final void validateUVMapping() {
        if (!hasValidUV()) {
            // compute top left point and range
            float minCornerX = Math.min(Math.min(uvPoints[0][0], uvPoints[1][0]), uvPoints[2][0]);
            float rangeX = Math.max(Math.max(uvPoints[0][0], uvPoints[1][0]), uvPoints[2][0]) - minCornerX;
            float minCornerY = Math.min(Math.min(uvPoints[0][1], uvPoints[1][1]), uvPoints[2][1]);
            float rangeY = Math.max(Math.max(uvPoints[0][1], uvPoints[1][1]), uvPoints[2][1]) - minCornerY;

            // compute uvs (not shifted yet)
            double[][] uvs = new double[][]{
                    new double[]{(uvPoints[0][0] - minCornerX) / rangeX, (uvPoints[0][1] - minCornerY) / rangeY},
                    new double[]{(uvPoints[1][0] - minCornerX) / rangeX, (uvPoints[1][1] - minCornerY) / rangeY},
                    new double[]{(uvPoints[2][0] - minCornerX) / rangeX, (uvPoints[2][1] - minCornerY) / rangeY}
            };

            // compute position and size of top parent
            TriTexture list = this;
            int x = 0;
            int y = 0;
            double width = this.width;
            double height = this.height;
            boolean swapped = false;
            int tmp;
            while (list != null) {
                switch (list.orientationFlag) {
                    case 0:
                        // do nothing
                        break;
                    case 4:
                        uvs[0][0] = 1-uvs[0][0];
                        uvs[1][0] = 1-uvs[1][0];
                        uvs[2][0] = 1-uvs[2][0];
                        x = list.width - x - (swapped ? this.height : this.width);
                        break;
                    case 2:
                        uvs[0][0] = 1-uvs[0][0];
                        uvs[1][0] = 1-uvs[1][0];
                        uvs[2][0] = 1-uvs[2][0];
                        uvs[0][1] = 1-uvs[0][1];
                        uvs[1][1] = 1-uvs[1][1];
                        uvs[2][1] = 1-uvs[2][1];
                        x = list.width - x - (swapped ? this.height : this.width);
                        y = list.height - y - (swapped ? this.width : this.height);
                        break;
                    case 6:
                        uvs[0][1] = 1-uvs[0][1];
                        uvs[1][1] = 1-uvs[1][1];
                        uvs[2][1] = 1-uvs[2][1];
                        y = list.height - y - (swapped ? this.width : this.height);
                        break;

                    // ==========

                    case 7:
                        swapped = !swapped;
                        swap(uvs);
                        tmp = x;
                        x = y;
                        y = tmp;
                        break;
                    case 1:
                        swapped = !swapped;
                        swap(uvs);
                        uvs[0][0] = 1-uvs[0][0];
                        uvs[1][0] = 1-uvs[1][0];
                        uvs[2][0] = 1-uvs[2][0];
                        tmp = x;
                        x = list.height - y - (swapped ? this.height : this.width);
                        y = tmp;
                        break;
                    case 3:
                        swapped = !swapped;
                        swap(uvs);
                        uvs[0][1] = 1-uvs[0][1];
                        uvs[1][1] = 1-uvs[1][1];
                        uvs[2][1] = 1-uvs[2][1];
                        tmp = x;
                        x = y;
                        y = list.width - tmp - (swapped ? this.width : this.height);
                        break;
                    default: // case 5
                        swapped = !swapped;
                        swap(uvs);
                        uvs[0][0] = 1-uvs[0][0];
                        uvs[1][0] = 1-uvs[1][0];
                        uvs[2][0] = 1-uvs[2][0];
                        uvs[0][1] = 1-uvs[0][1];
                        uvs[1][1] = 1-uvs[1][1];
                        uvs[2][1] = 1-uvs[2][1];
                        tmp = x;
                        x = list.height - y - (swapped ? this.height : this.width);
                        y = list.width - tmp - (swapped ? this.width : this.height);
                        break;

                }

                // alter left top according to this parent
                x += list.leftTop[0];
                y += list.leftTop[1];

                // set to top width and height (maximum)
                width = list.width;
                height = list.height;

                // get next parent
                list = list.parentTexture;
            }

            // -----------
            // -- interpolation
            // compute the center of the uv triangle
            double[] center = new double[]{
                    (uvs[0][0] + uvs[1][0] + uvs[2][0]) / 3,
                    (uvs[0][1] + uvs[1][1] + uvs[2][1]) / 3
            };
            // compute the offsets (the direction we need to interpolate in)
            double[][] offsets = new double[][]{
                    new double[]{center[0] - uvs[0][0], center[1] - uvs[0][1]},
                    new double[]{center[0] - uvs[1][0], center[1] - uvs[1][1]},
                    new double[]{center[0] - uvs[2][0], center[1] - uvs[2][1]}
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
            // -----------

            // scale, shift and apply interpolation
            double offsetX = x/width;
            double offsetY = y/height;
            double scaleX = (swapped ? this.height : this.width)/width;
            double scaleY = (swapped ? this.width : this.height)/height;
            uvs[0][0] = offsetX + uvs[0][0] * scaleX + offsets[0][0] * interp;
            uvs[1][0] = offsetX + uvs[1][0] * scaleX + offsets[1][0] * interp;
            uvs[2][0] = offsetX + uvs[2][0] * scaleX + offsets[2][0] * interp;
            uvs[0][1] = offsetY + uvs[0][1] * scaleY + offsets[0][1] * interp;
            uvs[1][1] = offsetY + uvs[1][1] * scaleY + offsets[1][1] * interp;
            uvs[2][1] = offsetY + uvs[2][1] * scaleY + offsets[2][1] * interp;

            // set the uv positions
            texTriUVs[0].set((float)uvs[0][0], (float)(1 - uvs[0][1]));
            texTriUVs[1].set((float)uvs[1][0], (float)(1 - uvs[1][1]));
            texTriUVs[2].set((float)uvs[2][0], (float)(1 - uvs[2][1]));

            // validate - the uv representation is ok now for this top texture
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

        // compress this texture (scale if this can be done loss-less)
        int[] newSize = compress(width, height, pixels);

        // set the image comparator
        imageComparator = new ImageComparator(pixels.values());

        // finalize width and height
        this.width = newSize[0];
        this.height = newSize[1];
    }

    // ===============

    // helper to compress this image
    // returns the new width of the image
    @SuppressWarnings("ConstantConditions")
    private static int[] compress(int width, int height, HashMap<Point, int[]> pixels) {
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
