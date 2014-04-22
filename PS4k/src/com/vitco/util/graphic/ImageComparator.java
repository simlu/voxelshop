package com.vitco.util.graphic;

import com.vitco.util.misc.ArrayUtil;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Allows for fast comparison of images.
 *
 * - sub image detection
 */
public class ImageComparator {

    // holds the colors of this texture with count (color -> count)
    private final HashMap<Integer, Integer> colors = new HashMap<Integer, Integer>();

    // holds the colors of this texture row/column wise (row/column -> color -> count)
    private final HashMap<Integer, HashMap<Integer, Integer>> colorsPerRow = new HashMap<Integer, HashMap<Integer, Integer>>();
    private final HashMap<Integer, HashMap<Integer, Integer>> colorsPerCol = new HashMap<Integer, HashMap<Integer, Integer>>();

    // stores all the pixels of the encapsulated image
    private final HashMap<Point, Integer> pixels = new HashMap<Point, Integer>();

    // dimension of this image
    private final int width;
    private final int height;

    // amount of pixels in this image
    // Note: this is <= width * height as some pixels might not be set
    private final int pixelCount;

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
            Integer count = colors.get(pixel[2]);
            if (count == null) {
                count = 0;
            }
            colors.put(pixel[2], count+1);

            // set row count
            HashMap<Integer, Integer> row = colorsPerRow.get(pixel[1]);
            if (row == null) {
                row = new HashMap<Integer, Integer>();
                colorsPerRow.put(pixel[1], row);
            }
            count = row.get(pixel[2]);
            if (count == null) {
                count = 0;
            }
            row.put(pixel[2], count+1);

            // set col count
            HashMap<Integer, Integer> col = colorsPerCol.get(pixel[0]);
            if (col == null) {
                col = new HashMap<Integer, Integer>();
                colorsPerCol.put(pixel[0], col);
            }
            count = col.get(pixel[2]);
            if (count == null) {
                count = 0;
            }
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
        this.width = width;
        this.height = height;
    }

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
                    for (Map.Entry<Point, Integer> pixel : this.pixels.entrySet()) {
                        if (pixel.getValue().equals(color)) {
                            Point p = pixel.getKey();
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
                    for (Map.Entry<Point, Integer> pixel : child.pixels.entrySet()) {
                        // check for containment in parent
                        Point childPos = pixel.getKey();
                        Integer color = this.pixels.get(new Point(x + childPos.x, y + childPos.y));
                        // Note: "color" might be null
                        if (!pixel.getValue().equals(color)) {
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
                    for (Map.Entry<Point, Integer> pixel : child.pixels.entrySet()) {
                        // check for containment in parent
                        Point childPos = pixel.getKey();
                        Integer color = this.pixels.get(new Point(x + (child.width - childPos.x), y + childPos.y));
                        // Note: "color" might be null
                        if (!pixel.getValue().equals(color)) {
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
                    for (Map.Entry<Point, Integer> pixel : child.pixels.entrySet()) {
                        // check for containment in parent
                        Point childPos = pixel.getKey();
                        Integer color = this.pixels.get(new Point(x + (child.width - childPos.x), y + (child.height - childPos.y)));
                        // Note: "color" might be null
                        if (!pixel.getValue().equals(color)) {
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
                    for (Map.Entry<Point, Integer> pixel : child.pixels.entrySet()) {
                        // check for containment in parent
                        Point childPos = pixel.getKey();
                        Integer color = this.pixels.get(new Point(x + childPos.x, y + (child.height - childPos.y)));
                        // Note: "color" might be null
                        if (!pixel.getValue().equals(color)) {
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
                    for (Map.Entry<Point, Integer> pixel : child.pixels.entrySet()) {
                        // check for containment in parent
                        Point childPos = pixel.getKey();
                        Integer color = this.pixels.get(new Point(x + childPos.y, y + childPos.x));
                        // Note: "color" might be null
                        if (!pixel.getValue().equals(color)) {
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
                    for (Map.Entry<Point, Integer> pixel : child.pixels.entrySet()) {
                        // check for containment in parent
                        Point childPos = pixel.getKey();
                        Integer color = this.pixels.get(new Point(x + (child.height - childPos.y), y + childPos.x));
                        // Note: "color" might be null
                        if (!pixel.getValue().equals(color)) {
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
                    for (Map.Entry<Point, Integer> pixel : child.pixels.entrySet()) {
                        // check for containment in parent
                        Point childPos = pixel.getKey();
                        Integer color = this.pixels.get(new Point(x + childPos.y, y + (child.width - childPos.x)));
                        // Note: "color" might be null
                        if (!pixel.getValue().equals(color)) {
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
                    for (Map.Entry<Point, Integer> pixel : child.pixels.entrySet()) {
                        // check for containment in parent
                        Point childPos = pixel.getKey();
                        Integer color = this.pixels.get(new Point(x + (child.height - childPos.y), y + (child.width - childPos.x)));
                        // Note: "color" might be null
                        if (!pixel.getValue().equals(color)) {
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
            HashMap<Integer, HashMap<Integer, Integer>> childArray,
            HashMap<Integer, HashMap<Integer, Integer>> parentArray,
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
    private boolean contained(HashMap<Integer, Integer> child, HashMap<Integer, Integer> parent) {
        for (Map.Entry<Integer, Integer> entry : child.entrySet()) {
            // obtain parent color count
            Integer count = parent.get(entry.getKey());
            // check that the colors are contained in the necessary amount
            if (count == null || count < entry.getValue()) {
                return false;
            }
        }
        return true;
    }
}
