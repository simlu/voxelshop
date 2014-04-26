package com.vitco.util.graphic;

import com.vitco.util.misc.ArrayUtil;
import gnu.trove.iterator.TIntIntIterator;
import gnu.trove.iterator.TIntIterator;
import gnu.trove.iterator.TObjectIntIterator;
import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import gnu.trove.set.hash.TIntHashSet;

import java.awt.*;
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
    private final TObjectIntHashMap<Point> pixels = new TObjectIntHashMap<Point>();

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
    private final int colorCount;

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
            this.pixels.put(new Point(pixel[0], pixel[1]), pixel[2]);

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
    public float jaccard(ImageComparator other) {
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

    // find the best "merge" position with orientation
    public static int[] getMergePoint(ImageComparator one, ImageComparator two) {

        // default result if nothing better is found
        int[] result = new int[]{one.width, 0, 0};
        int width = one.width + two.width;
        int height = Math.max(one.height, two.height);
        // add to bottom if the first image is wide
        if (width > height * 3) {
            result = new int[]{0, one.height, 0};
            width = Math.max(one.width, two.width);
            height = one.height + two.height;
        }
        int area = width * height;
        int pixelOverlap = 0;

        int originalWidth = width;
        int originalHeight = height;

        // loop over all "flipped" start positions
        // (i.e. the width and height of "two" are swapped)
        for (int x = -two.height + 1; x <= one.width; x++) {
            for (int y = -two.width + 1; y < one.height; y++) {
                // compute intersection area
                int minX = Math.max(0, x);
                int minY = Math.max(0, y);
                int maxX = Math.min(one.width, x + two.height);
                int maxY = Math.min(one.height, y + two.width);
                // compute new width, height and pixel count
                int widthTmp = Math.max(one.width, x + two.height) - Math.min(0, x);
                int heightTmp = Math.max(one.height, y + two.width) - Math.min(0, y);
                int areaTmp = widthTmp * heightTmp;
                // do some restriction checking
                if ((area >= areaTmp) &&
                        // ensure that the image can not only grow into one direction
                        (widthTmp < heightTmp * 3 || (originalWidth != width && width >= widthTmp)) &&
                        (heightTmp < widthTmp * 3 || (originalHeight != height && height >= heightTmp))) {
                    // initialize variables
                    boolean[] matched = new boolean[] {true, true, true, true};
                    int[] pixelOverlapTmp = new int[4];
                    // loop over all intersection points
                    loop: for (int i = minX; i < maxX; i++) {
                        for (int j = minY; j < maxY; j++) {
                            // compute point in static image ("one")
                            Point p1 = new Point(i, j);
                            // -- check for overlap of corresponding pixel
                            // 0 : check for "rotation 1" (1)
                            // 1 : check for "rotation 3" (3)
                            // 2 : check for "flipped and rotation 1" (5)
                            // 3 : check for "flipped and rotation 3" (7)
                            for (int k = 0; k < 4; k ++) {
                                // only proceed if this check has not already failed
                                if (matched[k]) {
                                    // compute the corresponding pixel in image "two"
                                    Point p2;
                                    switch (k) {
                                        case 0: p2 = new Point(j - y, two.height - 1 - (i - x)); break;
                                        case 1: p2 = new Point(two.width - 1 - (j - y), i - x); break;
                                        case 2: p2 = new Point(two.width - 1 - (j - y), two.height - 1 - (i - x)); break;
                                        default: p2 = new Point(j - y, i - x); break;
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
                            // if no matching is pending we break the loop
                            if (!matched[0] && !matched[1] && !matched[2] && !matched[3]) {
                                break loop;
                            }
                        }
                    }

                    // check if matches are better
                    for (int k = 0; k < 4; k ++) {
                        if (matched[k]) {
                            if (area > areaTmp || pixelOverlapTmp[k] > pixelOverlap) {
                                result = new int[]{x, y, k * 2 + 1};
                                area = areaTmp;
                                width = widthTmp;
                                height = heightTmp;
                                pixelOverlap = pixelOverlapTmp[k];
                            }
                        }
                    }
                }
            }
        }

        // loop over all "non flipped" start positions
        for (int x = -two.width + 1; x < one.width; x++) {
            for (int y = -two.height + 1; y < one.height; y++) {
                // compute intersection area
                int minX = Math.max(0, x);
                int minY = Math.max(0, y);
                int maxX = Math.min(one.width, x + two.width);
                int maxY = Math.min(one.height, y + two.height);
                // new width, height and pixel count
                int widthTmp = Math.max(one.width, x + two.width) - Math.min(0, x);
                int heightTmp = Math.max(one.height, y + two.height) - Math.min(0, y);
                int areaTmp = widthTmp * heightTmp;
                // do some restriction checking
                if ((area >= areaTmp) &&
                        // ensure that the image can not only grow into one direction
                        (widthTmp < heightTmp * 3 || (originalWidth != width && width >= widthTmp)) &&
                        (heightTmp < widthTmp * 3 || (originalHeight != height && height >= heightTmp))) {
                    // initialize variables
                    boolean[] matched = new boolean[] {true, true, true, true};
                    int[] pixelOverlapTmp = new int[4];
                    // loop over all intersection points
                    loop: for (int i = minX; i < maxX; i++) {
                        for (int j = minY; j < maxY; j++) {
                            // compute point in static image ("one")
                            Point p1 = new Point(i, j);
                            // -- check for overlap of corresponding pixel
                            // 0 : check for "default orientation" (0)
                            // 1 : check for "twice rotated" (2)
                            // 2 : check for "flipped" (4)
                            // 3 : check for "flipped and twice rotated" (6)
                            for (int k = 0; k < 4; k ++) {
                                // only proceed if this check has not already failed
                                if (matched[k]) {
                                    // compute the corresponding pixel in image "two"
                                    Point p2;
                                    switch (k) {
                                        case 0: p2 = new Point(i - x, j - y); break;
                                        case 1: p2 = new Point(two.width - 1 - (i - x), two.height - 1 - (j - y)); break;
                                        case 2: p2 = new Point(two.width - 1 - (i - x), j - y); break;
                                        default: p2 = new Point(i - x, two.height - 1 - (j - y)); break;
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
                            // if no matching is pending we break the loop
                            if (!matched[0] && !matched[1] && !matched[2] && !matched[3]) {
                                break loop;
                            }
                        }
                    }

                    // check if matches are better
                    for (int k = 0; k < 4; k ++) {
                        if (matched[k]) {
                            if (area > areaTmp || pixelOverlapTmp[k] > pixelOverlap) {
                                result = new int[]{x, y, k * 2};
                                area = areaTmp;
                                width = widthTmp;
                                height = heightTmp;
                                pixelOverlap = pixelOverlapTmp[k];
                            }
                        }
                    }
                }
            }
        }

        return result;
    }

    // ===========================

    // return the first position of this sub image in this image
    // or return null if no position is found
    // ----
    // Note: all orientations are analysed if not otherwise specified in the
    // restriction array:
    // 0 - original, 1 - rotated x 1, 2 - rotated x 2, 3 - rotated x 3,
    // 4 - flipped, 5 - flipped & rotated x 1, 6 - flipped & rotated x 2, 7 - flipped & rotated x 3
    public int[] getPosition(ImageComparator child, int[] restriction) {
        // -- do a quick return if child is one pixel in size
        if (child.pixelCount == 1) {
            int color = child.colors.keySet().iterator().next();
            // ensure that the color is present
            if (this.colors.containsKey(color)) {
                if (this.pixelCount == 1) {
                    return new int[] {0,0,0};
                } else {
                    // find location
                    for (TObjectIntIterator<Point> it = this.pixels.iterator(); it.hasNext();) {
                        it.advance();
                        if (it.value() == color) {
                            Point p = it.key();
                            return new int[]{p.x, p.y, 0};
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
        if (child.width <= this.width && child.height <= this.height) {
            // -- check for containment (Orientation 1)
            ArrayList<Integer> rowRow = getPossiblePositions(child.colorsPerRow, colorsPerRow, false);
            ArrayList<Integer> colCol = getPossiblePositions(child.colorsPerCol, colorsPerCol, false);
            if (restriction == null || ArrayUtil.contains(restriction, 0))
            for (int x : colCol) {
                for (int y : rowRow) {
                    // loop over all child pixels
                    boolean match = true;
                    for (TObjectIntIterator<Point> pixel = child.pixels.iterator(); pixel.hasNext();) {
                        pixel.advance();
                        // check for containment in parent
                        Point childPos = pixel.key();
                        int color = this.pixels.get(new Point(x + childPos.x, y + childPos.y));
                        // Note: "color" might be null
                        if (pixel.value() != color) {
                            match = false;
                            break;
                        }
                    }
                    if (match) {
                        return new int[]{x, y, 0};
                    }
                }
            }

            // -- check for containment (Flip 1)
            ArrayList<Integer> colColFlip = getPossiblePositions(child.colorsPerCol, colorsPerCol, true);
            if (restriction == null || ArrayUtil.contains(restriction, 4))
            for (int x : colColFlip) {
                for (int y : rowRow) {
                    // loop over all child pixels
                    boolean match = true;
                    for (TObjectIntIterator<Point> pixel = child.pixels.iterator(); pixel.hasNext();) {
                        pixel.advance();
                        // check for containment in parent
                        Point childPos = pixel.key();
                        int color = this.pixels.get(new Point(x + (child.widthM - childPos.x), y + childPos.y));
                        // Note: "color" might be null
                        if (pixel.value() != color) {
                            match = false;
                            break;
                        }
                    }
                    if (match) {
                        return new int[]{x, y, 4};
                    }
                }
            }

            // -- check for containment (Rotation 2)
            ArrayList<Integer> rowRowFlip = getPossiblePositions(child.colorsPerRow, colorsPerRow, true);
            if (restriction == null || ArrayUtil.contains(restriction, 2))
            for (int x : colColFlip) {
                for (int y : rowRowFlip) {
                    // loop over all child pixels
                    boolean match = true;
                    for (TObjectIntIterator<Point> pixel = child.pixels.iterator(); pixel.hasNext();) {
                        pixel.advance();
                        // check for containment in parent
                        Point childPos = pixel.key();
                        int color = this.pixels.get(new Point(x + (child.widthM - childPos.x), y + (child.heightM - childPos.y)));
                        // Note: "color" might be null
                        if (pixel.value() != color) {
                            match = false;
                            break;
                        }
                    }
                    if (match) {
                        return new int[]{x, y, 2};
                    }
                }
            }

            // -- check for containment (Flip + Rotation 2)
            if (restriction == null || ArrayUtil.contains(restriction, 6))
            for (int x : colCol) {
                for (int y : rowRowFlip) {
                    // loop over all child pixels
                    boolean match = true;
                    for (TObjectIntIterator<Point> pixel = child.pixels.iterator(); pixel.hasNext();) {
                        pixel.advance();
                        // check for containment in parent
                        Point childPos = pixel.key();
                        int color = this.pixels.get(new Point(x + childPos.x, y + (child.heightM - childPos.y)));
                        // Note: "color" might be null
                        if (pixel.value() != color) {
                            match = false;
                            break;
                        }
                    }
                    if (match) {
                        return new int[]{x, y, 6};
                    }
                }
            }
        }

        // ============

        // -- check for placement with "swap"
        if (child.height <= this.width && child.width <= this.height) {
            // -- check for containment (Flip + Rotation 3)
            ArrayList<Integer> colRow = getPossiblePositions(child.colorsPerCol, colorsPerRow, false);
            ArrayList<Integer> rowCol = getPossiblePositions(child.colorsPerRow, colorsPerCol, false);
            if (restriction == null || ArrayUtil.contains(restriction, 7))
            for (int x : rowCol) {
                for (int y : colRow) {
                    // loop over all child pixels
                    boolean match = true;
                    for (TObjectIntIterator<Point> pixel = child.pixels.iterator(); pixel.hasNext();) {
                        pixel.advance();
                        // check for containment in parent
                        Point childPos = pixel.key();
                        int color = this.pixels.get(new Point(x + childPos.y, y + childPos.x));
                        // Note: "color" might be null
                        if (pixel.value() != color) {
                            match = false;
                            break;
                        }
                    }
                    if (match) {
                        return new int[]{x, y, 7};
                    }
                }
            }

            // -- check for containment (Rotation 1)
            ArrayList<Integer> rowColFlip = getPossiblePositions(child.colorsPerRow, colorsPerCol, true);
            if (restriction == null || ArrayUtil.contains(restriction, 1))
            for (int x : rowColFlip) {
                for (int y : colRow) {
                    // loop over all child pixels
                    boolean match = true;
                    for (TObjectIntIterator<Point> pixel = child.pixels.iterator(); pixel.hasNext();) {
                        pixel.advance();
                        // check for containment in parent
                        Point childPos = pixel.key();
                        int color = this.pixels.get(new Point(x + (child.heightM - childPos.y), y + childPos.x));
                        // Note: "color" might be null
                        if (pixel.value() != color) {
                            match = false;
                            break;
                        }
                    }
                    if (match) {
                        return new int[]{x, y, 1};
                    }
                }
            }

            // -- check for containment (Rotation 3)
            ArrayList<Integer> colRowFlip = getPossiblePositions(child.colorsPerCol, colorsPerRow, true);
            if (restriction == null || ArrayUtil.contains(restriction, 3))
            for (int x : rowCol) {
                for (int y : colRowFlip) {
                    // loop over all child pixels
                    boolean match = true;
                    for (TObjectIntIterator<Point> pixel = child.pixels.iterator(); pixel.hasNext();) {
                        pixel.advance();
                        // check for containment in parent
                        Point childPos = pixel.key();
                        int color = this.pixels.get(new Point(x + childPos.y, y + (child.widthM - childPos.x)));
                        // Note: "color" might be null
                        if (pixel.value() != color) {
                            match = false;
                            break;
                        }
                    }
                    if (match) {
                        return new int[]{x, y, 3};
                    }
                }
            }

            // -- check for containment (Flip + Rotation 1)
            if (restriction == null || ArrayUtil.contains(restriction, 5))
            for (int x : rowColFlip) {
                for (int y : colRowFlip) {
                    // loop over all child pixels
                    boolean match = true;
                    for (TObjectIntIterator<Point> pixel = child.pixels.iterator(); pixel.hasNext();) {
                        pixel.advance();
                        // check for containment in parent
                        Point childPos = pixel.key();
                        int color = this.pixels.get(new Point(x + (child.heightM - childPos.y), y + (child.widthM - childPos.x)));
                        // Note: "color" might be null
                        if (pixel.value() != color) {
                            match = false;
                            break;
                        }
                    }
                    if (match) {
                        return new int[]{x, y, 5};
                    }
                }
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
