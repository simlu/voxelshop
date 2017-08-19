package com.vitco.app.util.graphic;

import com.vitco.app.util.misc.ArrayUtil;
import com.vitco.app.util.misc.IntegerTools;
import gnu.trove.iterator.TIntIntIterator;
import gnu.trove.iterator.TIntIterator;
import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.set.hash.TIntHashSet;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Allows for fast comparison of images.
 *
 * - sub image detection
 * - detection of best merging points for two images (slow)
 */
public class ImageComparator {

    // holds the colors of this texture with count (color -> count)
    private final TIntIntHashMap colors = new TIntIntHashMap();

    // holds the colors of this texture row/column wise (row/column -> color -> count)
    private final TIntObjectHashMap<TIntIntHashMap> colorsPerRow = new TIntObjectHashMap<TIntIntHashMap>();
    private final TIntObjectHashMap<TIntIntHashMap> colorsPerCol = new TIntObjectHashMap<TIntIntHashMap>();

    // stores all the pixels of the encapsulated image
    private final TIntIntHashMap pixels = new TIntIntHashMap();

    // dimension of this image
    private final int width;
    private final int height;
    // dimension minus one (used for position inversion)
    private final int widthM;
    private final int heightM;

    // amount of pixels in this image
    // Note: this is <= width * height as some pixels might not be set
    public final int pixelCount;

    // amount of different colors in this image
    public final int colorCount;

    // helper class - array list that has an init action executed on initialization
    private static abstract class InitArrayList extends ArrayList<int[]> {
        protected abstract void init();
        public InitArrayList() {
            super();
            init();
        }
    }

    // constructor
    public ImageComparator(final BufferedImage image) {
        // read the content of the buffered image and pass on to other constructor
        this(new InitArrayList() {
            @Override
            protected void init() {
                for (int x = 0, width = image.getWidth(); x < width; x++) {
                    for (int y = 0, height = image.getHeight(); y < height; y++) {
                        int rgb = image.getRGB(x,y);
                        // check that this is not a fully transparent pixel
                        if (((rgb >> 24) & 0xff) != 0) {
                            add(new int[]{x, y, rgb});
                        }
                    }
                }
            }
        });
    }

    // constructor
    public ImageComparator(Collection<int[]> pixels) {
        // image dimension (updated from the pixel data)
        int width = 0;
        int height = 0;

        // extract colors and count
        for (int[] pixel : pixels) {
            // set global color count
            int count = colors.get(pixel[2]);
            colors.put(pixel[2], count+1);

            // set row count
            TIntIntHashMap row = colorsPerRow.get(pixel[1]);
            if (row == null) {
                row = new TIntIntHashMap();
                colorsPerRow.put(pixel[1], row);
            }
            count = row.get(pixel[2]);
            row.put(pixel[2], count+1);

            // set col count
            TIntIntHashMap col = colorsPerCol.get(pixel[0]);
            if (col == null) {
                col = new TIntIntHashMap();
                colorsPerCol.put(pixel[0], col);
            }
            count = col.get(pixel[2]);
            col.put(pixel[2], count+1);

            // update pixel buffer (for fast access)
            this.pixels.put(IntegerTools.makeInt(pixel[0], pixel[1]), pixel[2]);

            // update width and height
            width = Math.max(pixel[0], width);
            height = Math.max(pixel[1], height);
        }

        // set pixel and color count
        this.pixelCount = pixels.size();
        this.colorCount = colors.size();

        // finalize the size
        this.widthM = width;
        this.heightM = height;
        this.width = width + 1;
        this.height = height + 1;
    }

    // ===========================

    // compute the Jaccard similarity coefficient (using the colors)
    public final float jaccard(ImageComparator other) {
        TIntHashSet uniqueColors = new TIntHashSet(this.colors.keySet());
        uniqueColors.addAll(other.colors.keySet());

        int intersection = 0;
        int union = 0;

        for (TIntIterator it = uniqueColors.iterator(); it.hasNext();) {
            int color = it.next();
            int count1 = this.colors.get(color);
            int count2 = other.colors.get(color);
            intersection += Math.min(count1, count2);
            union += Math.max(count1, count2);
        }

        return intersection / (float)union;
    }

    // =================

    // helper - check if a certain pixel is "ok" (matches
    // or "not set") for a specific matching
    private static void checkPixel(
            int x, int y, ImageComparator one, ImageComparator two,
            int i, int j, boolean[] matched, int[] pixelOverlapTmp,
            boolean flip
    ) {
        // compute point in static image ("one")
        int p1 = IntegerTools.makeInt(i, j);

        for (int k = 0; k < 4; k ++) {
            // only proceed if this check has not already failed
            if (matched[k]) {
                // compute the corresponding pixel in image "two"
                int p2;
                if (flip) {
                    // -- check for overlap of corresponding pixel
                    // 0 : check for "rotation 1" (1)
                    // 1 : check for "rotation 3" (3)
                    // 2 : check for "flipped and rotation 1" (5)
                    // 3 : check for "flipped and rotation 3" (7)
                    switch (k) {
                        case 0: p2 = IntegerTools.makeInt(j - y, two.height - 1 - (i - x)); break;
                        case 1: p2 = IntegerTools.makeInt(two.width - 1 - (j - y), i - x); break;
                        case 2: p2 = IntegerTools.makeInt(two.width - 1 - (j - y), two.height - 1 - (i - x)); break;
                        default: p2 = IntegerTools.makeInt(j - y, i - x); break;
                    }
                } else {
                    // -- check for overlap of corresponding pixel
                    // 0 : check for "default orientation" (0)
                    // 1 : check for "twice rotated" (2)
                    // 2 : check for "flipped" (4)
                    // 3 : check for "flipped and twice rotated" (6)
                    switch (k) {
                        case 0: p2 = IntegerTools.makeInt(i - x, j - y); break;
                        case 1: p2 = IntegerTools.makeInt(two.width - 1 - (i - x), two.height - 1 - (j - y)); break;
                        case 2: p2 = IntegerTools.makeInt(two.width - 1 - (i - x), j - y); break;
                        default: p2 = IntegerTools.makeInt(i - x, two.height - 1 - (j - y)); break;
                    }
                }
                // check for containment
                if (one.pixels.containsKey(p1) && two.pixels.containsKey(p2)) {
                    if (one.pixels.get(p1) == two.pixels.get(p2)) {
                        pixelOverlapTmp[k]++;
                    } else {
                        matched[k] = false;
                    }
                }
            }
        }
    }

    // helper - check if a certain offset allows placing
    // the second image onto the first one
    private static void checkPosition(
            int x, int y, ImageComparator one, ImageComparator two,
            int[] area, int[] size, int originalWidth, int originalHeight,
            int[] pixelOverlap, int[] result,
            boolean flip
    ) {
        // compute intersection area
        int minX = Math.max(0, x);
        int minY = Math.max(0, y);
        int maxX, maxY;
        int widthTmp, heightTmp;
        if (flip) {
            maxX = Math.min(one.width, x + two.height);
            maxY = Math.min(one.height, y + two.width);
            // compute new width, height and pixel count
            widthTmp = Math.max(one.width, x + two.height) - Math.min(0, x);
            heightTmp = Math.max(one.height, y + two.width) - Math.min(0, y);
        } else {
            maxX = Math.min(one.width, x + two.width);
            maxY = Math.min(one.height, y + two.height);
            // compute new width, height and pixel count
            widthTmp = Math.max(one.width, x + two.width) - Math.min(0, x);
            heightTmp = Math.max(one.height, y + two.height) - Math.min(0, y);
        }

        int areaTmp = widthTmp * heightTmp;
        // do some restriction checking
        if ((area[0] >= areaTmp) &&
                // ensure that the image can not only grow into one direction
                (widthTmp < heightTmp * 3 || (originalWidth != size[0] && size[0] >= widthTmp)) &&
                (heightTmp < widthTmp * 3 || (originalHeight != size[1] && size[1] >= heightTmp))) {
            // initialize variables
            boolean[] matched = new boolean[] {true, true, true, true};
            int[] pixelOverlapTmp = new int[4];
            // loop over all intersection points
            loop: for (int i = minX; i < maxX; i++) {
                for (int j = minY; j < maxY; j++) {
                    checkPixel(x, y, one, two, i, j, matched, pixelOverlapTmp, flip);
                    // if all overlap checks have already failed we can break the loop
                    if (!matched[0] && !matched[1] && !matched[2] && !matched[3]) {
                        break loop;
                    }
                }
            }

            // check if matches are better
            for (int k = 0; k < 4; k ++) {
                if (matched[k]) {
                    if (area[0] > areaTmp || pixelOverlapTmp[k] > pixelOverlap[0]) {
                        result[0] = x;
                        result[1] = y;
                        result[2] = k * 2 + (flip ? 1 : 0);
                        area[0] = areaTmp;
                        size[0] = widthTmp;
                        size[1] = heightTmp;
                        pixelOverlap[0] = pixelOverlapTmp[k];
                    }
                }
            }
        }
    }

    // find the best "merge" position with orientation
    public static int[] getMergePoint(ImageComparator one, ImageComparator two) {

        // default result if nothing better is found
        int[] result = new int[]{one.width, 0, 0};
        int[] size = new int[] {one.width + two.width, Math.max(one.height, two.height)};
        // add to bottom if the first image is wide
        if (size[0] > size[1] * 3) {
            result[0] = 0;
            result[1] = one.height;
            result[2] = 0;
            size[0] = Math.max(one.width, two.width);
            size[1] = one.height + two.height;
        }
        int[] area = new int[] {size[0] * size[1]};
        int[] pixelOverlap = new int[] {0};

        int[] originalSize = size.clone();

        // loop over all "non flipped" start positions
        for (int x = -two.width + 1; x < one.width; x++) {
            for (int y = -two.height + 1; y < one.height; y++) {
                checkPosition(x,y,one,two,area,size,originalSize[0],originalSize[1],pixelOverlap,result, false);
            }
        }

        // loop over all "flipped" start positions
        // (i.e. the width and height of "two" are swapped)
        for (int x = -two.height + 1; x <= one.width; x++) {
            for (int y = -two.width + 1; y < one.height; y++) {
                checkPosition(x,y,one,two,area,size,originalSize[0],originalSize[1],pixelOverlap,result, true);
            }
        }

        return result;
    }

    // ===========================

    // helper - check if child is contained in this image for a certain orientation given by "type" (explaination see below)
    private int[] getPosition(ImageComparator child, ArrayList<Integer> one, ArrayList<Integer> two, int[] restriction, int type) {
        if (restriction == null || ArrayUtil.contains(restriction, type)) {
            for (int x : one) {
                for (int y : two) {
                    // loop over all child pixels
                    boolean match = true;
                    for (TIntIntIterator pixel = child.pixels.iterator(); pixel.hasNext(); ) {
                        pixel.advance();
                        // check for containment in parent
                        short[] childPos = IntegerTools.getShorts(pixel.key());
                        int color;
                        switch (type) {
                            // 0 - original, 1 - rotated x 1, 2 - rotated x 2, 3 - rotated x 3,
                            // 4 - flipped, 5 - flipped & rotated x 1, 6 - flipped & rotated x 2, 7 - flipped & rotated x 3
                            case 0: color = this.pixels.get(IntegerTools.makeInt(x + childPos[0], y + childPos[1])); break;
                            case 4: color = this.pixels.get(IntegerTools.makeInt(x + (child.widthM - childPos[0]), y + childPos[1])); break;
                            case 2: color = this.pixels.get(IntegerTools.makeInt(x + (child.widthM - childPos[0]), y + (child.heightM - childPos[1]))); break;
                            case 6: color = this.pixels.get(IntegerTools.makeInt(x + childPos[0], y + (child.heightM - childPos[1]))); break;
                            case 7: color = this.pixels.get(IntegerTools.makeInt(x + childPos[1], y + childPos[0])); break;
                            case 1: color = this.pixels.get(IntegerTools.makeInt(x + (child.heightM - childPos[1]), y + childPos[0])); break;
                            case 3: color = this.pixels.get(IntegerTools.makeInt(x + childPos[1], y + (child.widthM - childPos[0]))); break;
                            default: color = this.pixels.get(IntegerTools.makeInt(x + (child.heightM - childPos[1]), y + (child.widthM - childPos[0]))); break; // case 5
                        }
                        // Note: "color" might be null
                        if (pixel.value() != color) {
                            match = false;
                            break;
                        }
                    }
                    if (match) {
                        return new int[]{x, y, type};
                    }
                }
            }
        }
        return null;
    }

    // return the first position of this sub image in this image
    // or return null if no position is found
    // ----
    // Note: all orientations are analysed if not otherwise specified in the
    // restriction array:
    // 0 - original, 1 - rotated x 1, 2 - rotated x 2, 3 - rotated x 3,
    // 4 - flipped, 5 - flipped & rotated x 1, 6 - flipped & rotated x 2, 7 - flipped & rotated x 3
    public final int[] getPosition(ImageComparator child, int[] restriction) {
        // -- do a quick return if child is one pixel in size
        if (child.pixelCount == 1) {
            int color = child.colors.keySet().iterator().next();
            // ensure that the color is present
            if (this.colors.containsKey(color)) {
                if (this.pixelCount == 1) {
                    return new int[] {0,0,0};
                } else {
                    // find location
                    for (TIntIntIterator it = this.pixels.iterator(); it.hasNext();) {
                        it.advance();
                        if (it.value() == color) {
                            short[] p = IntegerTools.getShorts(it.key());
                            return new int[]{p[0], p[1], 0};
                        }
                    }
                    // this should never be reached since the color is present
                    assert false;
                }
            }
            // color is not present
            return null;
        }

        // -- check if dimensions fit
        if ((child.width > this.width || child.height > this.height) && (child.height > this.width || child.width > this.height)) {
            return null;
        }

        // -- check if pixel count fits
        if (this.pixelCount < child.pixelCount) {
            return null;
        }

        // -- check if there are enough different colors
        if (this.colorCount < child.colorCount) {
            return null;
        }

        // -- check if contained colors are subset
        if (!contained(child.colors, colors)) {
            return null;
        }

        // ==============

        // -- check for placement without "swap"
        int[] result;
        if (child.width <= this.width && child.height <= this.height) {
            // -- check for containment (Orientation 1)
            ArrayList<Integer> rowRow = getPossiblePositions(child.colorsPerRow, colorsPerRow, false);
            ArrayList<Integer> colCol = getPossiblePositions(child.colorsPerCol, colorsPerCol, false);
            result = getPosition(child, colCol, rowRow, restriction, 0);
            if (result != null) {
                return result;
            }

            // -- check for containment (Flip 1)
            ArrayList<Integer> colColFlip = getPossiblePositions(child.colorsPerCol, colorsPerCol, true);
            result = getPosition(child, colColFlip, rowRow, restriction, 4);
            if (result != null) {
                return result;
            }

            // -- check for containment (Rotation 2)
            ArrayList<Integer> rowRowFlip = getPossiblePositions(child.colorsPerRow, colorsPerRow, true);
            result = getPosition(child, colColFlip, rowRowFlip, restriction, 2);
            if (result != null) {
                return result;
            }

            // -- check for containment (Flip + Rotation 2)
            result = getPosition(child, colCol, rowRowFlip, restriction, 6);
            if (result != null) {
                return result;
            }
        }

        // ============

        // -- check for placement with "swap"
        if (child.height <= this.width && child.width <= this.height) {
            // -- check for containment (Flip + Rotation 3)
            ArrayList<Integer> colRow = getPossiblePositions(child.colorsPerCol, colorsPerRow, false);
            ArrayList<Integer> rowCol = getPossiblePositions(child.colorsPerRow, colorsPerCol, false);
            result = getPosition(child, rowCol, colRow, restriction, 7);
            if (result != null) {
                return result;
            }

            // -- check for containment (Rotation 1)
            ArrayList<Integer> rowColFlip = getPossiblePositions(child.colorsPerRow, colorsPerCol, true);
            result = getPosition(child, rowColFlip, colRow, restriction, 1);
            if (result != null) {
                return result;
            }

            // -- check for containment (Rotation 3)
            ArrayList<Integer> colRowFlip = getPossiblePositions(child.colorsPerCol, colorsPerRow, true);
            result = getPosition(child, rowCol, colRowFlip, restriction, 3);
            if (result != null) {
                return result;
            }

            // -- check for containment (Flip + Rotation 1)
            result = getPosition(child, rowColFlip, colRowFlip, restriction, 5);
            if (result != null) {
                return result;
            }
        }

        // -- nothing found
        return null;
    }

    // check "containment" positions of child array in parent array
    private ArrayList<Integer> getPossiblePositions(
            TIntObjectHashMap<TIntIntHashMap> childArray,
            TIntObjectHashMap<TIntIntHashMap> parentArray,
            boolean flip) {
        ArrayList<Integer> result = new ArrayList<Integer>();

        if (flip) {
            // -- check for presence of inverted child array
            int lenChild = childArray.size();
            // loop over all possible starting positions
            for (int i = lenChild - 1, lenParent = parentArray.size(); i < lenParent; i++) {
                // loop over child positions
                boolean matched = true;
                for (int j = 0; j < lenChild; j++) {
                    // check if contained
                    if (!contained(childArray.get(j), parentArray.get(i - j))) {
                        matched = false;
                        break;
                    }
                }
                if (matched) {
                    result.add(i - lenChild + 1);
                }
            }
        } else {
            // -- check for presence of normal child array
            int lenChild = childArray.size();
            // loop over all possible starting positions
            for (int i = 0, len = parentArray.size() - lenChild + 1; i < len; i++) {
                // loop over child positions
                boolean matched = true;
                for (int j = 0; j < lenChild; j++) {
                    // check if contained
                    if (!contained(childArray.get(j), parentArray.get(i + j))) {
                        matched = false;
                        break;
                    }
                }
                if (matched) {
                    result.add(i);
                }
            }
        }

        return result;
    }

    // check if color list is contained in other color list
    private boolean contained(TIntIntHashMap child, TIntIntHashMap parent) {
        if (child == null) {
            System.err.println("ImageComparator Error 3941");
            return true;
        }
        if (parent == null) {
            System.err.println("ImageComparator Error 3942");
            return false;
        }
        for (TIntIntIterator it = child.iterator(); it.hasNext();) {
            it.advance();
            // check that the colors are contained in the necessary amount
            if (parent.get(it.key()) < it.value()) {
                return false;
            }
        }
        return true;
    }
}
