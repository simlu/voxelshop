package com.vitco.export.container;

import com.vitco.core.data.Data;
import com.vitco.core.data.container.Voxel;
import com.vitco.util.graphic.G2DUtil;
import com.vitco.util.graphic.ImageComparator;
import com.vitco.util.graphic.TextureTools;

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

    // true if this texture has a corresponding triangle
    private final boolean hasTriangle;

    // reference to the the uvs
    private final TexTriUV[] texTriUVs = new TexTriUV[3];

    // reference to the uv points
    private final double[][] uvPoints = new double[3][2];

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

    // obtain the pixel count
    public final int getPixelCount() {
        return imageComparator.pixelCount;
    }

    // obtain the jaccard distance
    public final float jaccard(TriTexture other) {
        return this.imageComparator.jaccard(other.imageComparator);
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
        return lastTopTexture == getTopTexture() || !hasTriangle;
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
            // compute uvs (not adjusted yet)
            double[][] uvs = new double[][]{
                    new double[]{uvPoints[0][0], uvPoints[0][1]},
                    new double[]{uvPoints[1][0], uvPoints[1][1]},
                    new double[]{uvPoints[2][0], uvPoints[2][1]}
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

    // alternative constructor
    public TriTexture(TriTexture one, TriTexture two, TriTextureManager textureManager) {
        assert !one.hasParent();
        assert !two.hasParent();
        // set final variables
        this.textureManager = textureManager;
        // has no corresponding triangle
        this.hasTriangle = false;

        // obtain merge position and orientation
        int[] mergePos = ImageComparator.getMergePoint(one.imageComparator, two.imageComparator);

        // compute pixels
        int minX = Math.min(0, mergePos[0]);
        int minY = Math.min(0, mergePos[1]);
        int maxX = Math.max(0, mergePos[0]);
        int maxY = Math.max(0, mergePos[1]);
        for (int[] pixel : one.pixels.values()) {
            int x = pixel[0] - minX;
            int y = pixel[1] - minY;
            pixels.put(new Point(x, y), new int[] {x,y,pixel[2]});
        }
        for (int[] pixel : two.pixels.values()) {
            int x = pixel[0] + maxX;
            int y = pixel[1] + maxY;
            pixels.put(new Point(x, y), new int[] {x,y,pixel[2]});
        }
        // set the image comparator
        imageComparator = new ImageComparator(pixels.values());

        // compute new dimensions
        this.width = Math.max(two.width + maxX, one.width - minX);
        this.height = Math.max(two.height + maxY, one.height - minY);

        // -----------

        // make children
        this.makeChild(one);
        this.makeChild(two);
        assert one.hasParent();
        assert two.hasParent();
    }

    // constructor
    public TriTexture(
            TexTriUV uv1, int xf1, int yf1,
            TexTriUV uv2, int xf2, int yf2,
            TexTriUV uv3, int xf3, int yf3,
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

        // -- normalize uv points
        // compute top left point and range
        double minCornerX = Math.min(Math.min(uvPoints[0][0], uvPoints[1][0]), uvPoints[2][0]);
        double rangeX = Math.max(Math.max(uvPoints[0][0], uvPoints[1][0]), uvPoints[2][0]) - minCornerX;
        double minCornerY = Math.min(Math.min(uvPoints[0][1], uvPoints[1][1]), uvPoints[2][1]);
        double rangeY = Math.max(Math.max(uvPoints[0][1], uvPoints[1][1]), uvPoints[2][1]) - minCornerY;
        // compute uvs (not shifted yet)
        uvPoints[0][0] = (uvPoints[0][0] - minCornerX) / rangeX;
        uvPoints[1][0] = (uvPoints[1][0] - minCornerX) / rangeX;
        uvPoints[2][0] = (uvPoints[2][0] - minCornerX) / rangeX;
        uvPoints[0][1] = (uvPoints[0][1] - minCornerY) / rangeY;
        uvPoints[1][1] = (uvPoints[1][1] - minCornerY) / rangeY;
        uvPoints[2][1] = (uvPoints[2][1] - minCornerY) / rangeY;

        // has a corresponding triangle
        this.hasTriangle = true;

        // store texture manager reference
        this.textureManager = textureManager;

        // compute the voxels that are inside this triangle
        // (so we're not using any "extra" pixels in the buffered image)
        int[][] points = G2DUtil.getTriangleGridIntersection(
                xf1, yf1, xf2, yf2, xf3, yf3
        );

        // get min/max pixel values (this is different from UV!)
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

        // compress textures (scale if this can be done loss-less)
        int[] newSize = compress(width, height, pixels, uvPoints);

        // check for unnecessary pixels and resize if necessary (prune them)
        // Note: This can only happen after compression changed the image and uvs
        if (newSize[0] < width || newSize[1] < height) {
            newSize = prune(newSize[0], newSize[1], pixels, uvPoints);
        }

        // set the image comparator
        imageComparator = new ImageComparator(pixels.values());

        // overwrite uv to prevent unnecessary unique uv coordinates.
        // Note: this enables better compression for COLLADA
        if (imageComparator.pixelCount == 1) {
            uvPoints[0][0] = 0;
            uvPoints[0][1] = 0;
            uvPoints[1][0] = 1;
            uvPoints[1][1] = 0;
            uvPoints[2][0] = 0;
            uvPoints[2][1] = 1;
        }

        // finalize width and height
        this.width = newSize[0];
        this.height = newSize[1];
    }

    // prune unnecessary pixels from this texture (can occur after compression)
    private static int[] prune(int width, int height, HashMap<Point, int[]> pixels, double[][] uvPoints) {
        double[] minUV = new double[] {
                Math.min(Math.min(uvPoints[0][0], uvPoints[1][0]), uvPoints[2][0]) * width,
                Math.min(Math.min(uvPoints[0][1], uvPoints[1][1]), uvPoints[2][1]) * height
        };
        double[] maxUV = new double[] {
                Math.max(Math.max(uvPoints[0][0], uvPoints[1][0]), uvPoints[2][0]) * width,
                Math.max(Math.max(uvPoints[0][1], uvPoints[1][1]), uvPoints[2][1]) * height
        };
        // adjust to close int (cast to float to prevent rounding errors)
        minUV[0] = Math.floor((float)minUV[0]);
        minUV[1] = Math.floor((float)minUV[1]);
        maxUV[0] = Math.ceil((float)maxUV[0]);
        maxUV[1] = Math.ceil((float)maxUV[1]);

        // only proceed if something was pruned
        if (minUV[0] > 0 || minUV[1] > 0 || maxUV[0] < width || maxUV[1] < height) {
            // loop over pixels
            HashMap<Point, int[]> newPixels = new HashMap<Point, int[]>();
            for (int[] pixel : pixels.values()) {
                // check if pixel is in valid area
                if (pixel[0] >= minUV[0] && pixel[1] >= minUV[1] &&
                        pixel[0] < maxUV[0] && pixel[1] < maxUV[1]) {
                    // create shifted pixel
                    int x = (int) (pixel[0] - minUV[0]);
                    int y = (int) (pixel[1] - minUV[1]);
                    newPixels.put(new Point(x, y), new int[] {x, y, pixel[2]});
                }
            }
            // adjust uv points
            double newWidth = maxUV[0] - minUV[0];
            double newHeight = maxUV[1] - minUV[1];
            uvPoints[0][0] = (uvPoints[0][0] * width - minUV[0]) / newWidth;
            uvPoints[0][1] = (uvPoints[0][1] * height - minUV[1]) / newHeight;
            uvPoints[1][0] = (uvPoints[1][0] * width - minUV[0]) / newWidth;
            uvPoints[1][1] = (uvPoints[1][1] * height - minUV[1]) / newHeight;
            uvPoints[2][0] = (uvPoints[2][0] * width - minUV[0]) / newWidth;
            uvPoints[2][1] = (uvPoints[2][1] * height - minUV[1]) / newHeight;
            // update
            pixels.clear();
            pixels.putAll(newPixels);
            width = (int)newWidth;
            height = (int)newHeight;
        }
        return new int[] {width, height};
    }

    // compress the texture and return new size
    // Note: This changes the pixel array and also the uv positions (!)
    private static int[] compress(int width, int height, HashMap<Point, int[]> pixels, double[][] uvPoints) {
        // size array (that might still change!)
        int[] size = new int[] {width, height, 1};
        // -- compress this texture (scale if this can be done loss-less)
        // basic compression in x direction
        size = TextureTools.compress(size[0], size[1], 0, size[0], false, pixels);
        // basic compression in y direction
        size = TextureTools.compress(size[0], size[1], 0, size[1], true, pixels);

        // obtain offset and compress with offsets (X)
        int[] offsetsX = TextureTools.getOffsets(size[0], size[1], false, pixels);
        if (offsetsX[0] > 0) { // skip left
            int[] oldSize = new int[] {size[0], size[1]};
            size = TextureTools.compress(size[0], size[1], offsetsX[0], size[0], false, pixels);
            if (size[2] > 1) { // check if there has been a compression
                // move uvs
                for (double[] p : uvPoints) {
                    p[0] *= oldSize[0];
                    if (p[0] > offsetsX[0]) {
                        p[0] = ((p[0] - offsetsX[0]) / size[2]) + offsetsX[0];
                    } else {
                        p[0] += (offsetsX[0] - p[0]) * (1 - 1d / size[2]);
                    }
                    p[0] /= size[0];
                }
                // update offsets
                offsetsX = TextureTools.getOffsets(size[0], size[1], false, pixels);
            }
        }
        if (offsetsX[1] < size[0]) { // skip right
            int[] oldSize = new int[] {size[0], size[1]};
            size = TextureTools.compress(size[0], size[1], 0, offsetsX[1], false, pixels);
            if (size[2] > 1) { // check if there has been a compression
                // move uvs
                for (double[] p : uvPoints) {
                    p[0] *= oldSize[0];
                    if (p[0] < offsetsX[1]) {
                        p[0] = p[0] / size[2];
                    } else {
                        p[0] = (p[0] - offsetsX[1]) * (1d / size[2]) + offsetsX[1] / size[2];
                    }
                    p[0] /= size[0];
                }
                // update offsets
                offsetsX = TextureTools.getOffsets(size[0], size[1], false, pixels);
            }
        }
        if (offsetsX[0] > 0 || offsetsX[1] < size[0]) { // skip left and right
            int[] oldSize = new int[] {size[0], size[1]};
            size = TextureTools.compress(size[0], size[1], offsetsX[0], offsetsX[1], false, pixels);
            if (size[2] > 1) { // check if there has been a compression
                // move uvs
                for (double[] p : uvPoints) {
                    p[0] *= oldSize[0];
                    if (p[0] < offsetsX[0]) {
                        p[0] += (offsetsX[0] - p[0]) * (1 - 1d / size[2]);
                    } else if (p[0] < offsetsX[1]) {
                        p[0] = (p[0] - offsetsX[0]) / size[2] + offsetsX[0];
                    } else {
                        p[0] = (p[0] - offsetsX[1]) * (1d / size[2]) + offsetsX[0] + (offsetsX[1] - offsetsX[0]) / size[2];
                    }
                    p[0] /= size[0];
                }
            }
        }


        // obtain offset and compress with offsets (Y)
        int[] offsetsY = TextureTools.getOffsets(size[0], size[1], true, pixels);
        if (offsetsY[0] > 0) { // skip top
            int[] oldSize = new int[] {size[0], size[1]};
            size = TextureTools.compress(size[0], size[1], offsetsY[0], size[1], true, pixels);
            if (size[2] > 1) { // check if there has been a compression
                // move uvs
                for (double[] p : uvPoints) {
                    p[1] *= oldSize[1];
                    if (p[1] > offsetsY[0]) {
                        p[1] = ((p[1] - offsetsY[0]) / size[2]) + offsetsY[0];
                    } else {
                        p[1] += (offsetsY[0] - p[1]) * (1 - 1d / size[2]);
                    }
                    p[1] /= size[1];
                }
                // update offsets
                offsetsY = TextureTools.getOffsets(size[0], size[1], true, pixels);
            }
        }
        if (offsetsY[1] < size[1]) { // skip bottom
            int[] oldSize = new int[] {size[0], size[1]};
            size = TextureTools.compress(size[0], size[1], 0, offsetsY[1], true, pixels);
            if (size[2] > 1) { // check if there has been a compression
                // move uvs
                for (double[] p : uvPoints) {
                    p[1] *= oldSize[1];
                    if (p[1] < offsetsY[1]) {
                        p[1] = p[1] / size[2];
                    } else {
                        p[1] = (p[1] - offsetsY[1]) * (1d / size[2]) + offsetsY[1] / size[2];
                    }
                    p[1] /= size[1];
                }
                // update offsets
                offsetsY = TextureTools.getOffsets(size[0], size[1], true, pixels);
            }
        }
        if (offsetsY[0] > 0 || offsetsY[1] < size[1]) { // skip top and bottom
            int[] oldSize = new int[] {size[0], size[1]};
            size = TextureTools.compress(size[0], size[1], offsetsY[0], offsetsY[1], true, pixels);
            if (size[2] > 1) { // check if there has been a compression
                // move uvs
                for (double[] p : uvPoints) {
                    p[1] *= oldSize[1];
                    if (p[1] < offsetsY[0]) {
                        p[1] += (offsetsY[0] - p[1]) * (1 - 1d / size[2]);
                    } else if (p[1] < offsetsY[1]) {
                        p[1] = (p[1] - offsetsY[0]) / size[2] + offsetsY[0];
                    } else {
                        p[1] = (p[1] - offsetsY[1]) * (1d / size[2]) + offsetsY[0] + (offsetsY[1] - offsetsY[0]) / size[2];
                    }
                    p[1] /= size[1];
                }
            }
        }
        return size;
    }
}
