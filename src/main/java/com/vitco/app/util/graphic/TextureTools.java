package com.vitco.app.util.graphic;

import com.vitco.app.util.misc.IntegerTools;
import gnu.trove.map.hash.TIntObjectHashMap;

/**
 * Basic and Advanced functionality for textures.
 */
public class TextureTools {

    // helper - determine "offsets" for a texture
    // Note: "Front width offset" means how many "equal" columns are at the left side of the texture (similar for others...)
    public static int[] getOffsets(int width, int height, boolean checkHeight, TIntObjectHashMap<int[]> pixels) {
        int[] result = new int[]{0, width};

        if (checkHeight) {
            // fetch front offset
            Integer[] scanline = new Integer[width];
            loop:
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    int[] pixel = pixels.get(IntegerTools.makeInt(x, y));
                    if (pixel != null) {
                        if (scanline[x] == null) {
                            scanline[x] = pixel[2];
                        } else if (scanline[x] != pixel[2]) {
                            break loop;
                        }
                    }
                }
                result[0] = y + 1;
            }

            // fetch back offset
            scanline = new Integer[width];
            loop:
            for (int y = height - 1; y >= 0; y--) {
                for (int x = 0; x < width; x++) {
                    int[] pixel = pixels.get(IntegerTools.makeInt(x, y));
                    if (pixel != null) {
                        if (scanline[x] == null) {
                            scanline[x] = pixel[2];
                        } else if (scanline[x] != pixel[2]) {
                            break loop;
                        }
                    }
                }
                result[1] = y;
            }

            // adjust offsets (i.e. when the texture has only one color)
            if (result[0] > result[1]) {
                result[0] = 0;
                result[1] = height;
            } else if (result[0] == result[1]) {
                if (result[0] > (height - result[1])) {
                    result[0] = 0;
                } else {
                    result[1] = height;
                }
            }
        } else {
            // fetch front offset
            Integer[] scanline = new Integer[height];
            loop:
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    int[] pixel = pixels.get(IntegerTools.makeInt(x, y));
                    if (pixel != null) {
                        if (scanline[y] == null) {
                            scanline[y] = pixel[2];
                        } else if (scanline[y] != pixel[2]) {
                            break loop;
                        }
                    }
                }
                result[0] = x + 1;
            }

            // fetch back offset
            scanline = new Integer[height];
            loop:
            for (int x = width - 1; x >= 0; x--) {
                for (int y = 0; y < height; y++) {
                    int[] pixel = pixels.get(IntegerTools.makeInt(x, y));
                    if (pixel != null) {
                        if (scanline[y] == null) {
                            scanline[y] = pixel[2];
                        } else if (scanline[y] != pixel[2]) {
                            break loop;
                        }
                    }
                }
                result[1] = x;
            }

            // adjust offsets (i.e. when the texture has only one color)
            if (result[0] > result[1]) {
                result[0] = 0;
                result[1] = width;
            } else if (result[0] == result[1]) {
                if (result[0] > (width - result[1])) {
                    result[0] = 0;
                } else {
                    result[1] = width;
                }
            }
        }

        return result;
    }

    // helper - compress function (with offsets that are ignored)
    // returns the new dimension and the compression factor (by that the valid area was compressed)
    // format (width, height, factor)
    public static int[] compress(int width, int height, int front, int back, boolean useHeight, TIntObjectHashMap<int[]> pixels) {
        int factor = 1;
        // compute inside dist
        int dist = back - front;
        if (dist > 1) {
            if (useHeight) {
                for (int d = 1, len = dist / 2 + 1; d < len; d++) {
                    loop:
                    if (dist % d == 0) {
                        // potential new pixel representation
                        TIntObjectHashMap<int[]> result = new TIntObjectHashMap<int[]>();
                        // the step width that would be compressed to one pixel
                        int stepSize = dist / d;
                        // loop over all steps
                        for (int y = 0; y < d; y++) {
                            // loop over width
                            for (int x = 0; x < width; x++) {
                                Integer lastColor = null;
                                // loop over step
                                for (int i = 0; i < stepSize; i++) {
                                    // compute the current point
                                    int p = IntegerTools.makeInt(x, front + y * stepSize + i);
                                    // obtain the pixel
                                    int[] pixel = pixels.get(p);
                                    if (pixel != null) {
                                        // check if the pixel color is consistent through this step
                                        if (lastColor == null) {
                                            lastColor = pixel[2];
                                            result.put(IntegerTools.makeInt(x, front + y), new int[]{x, front + y, pixel[2]});
                                        } else {
                                            if (lastColor != pixel[2]) {
                                                break loop;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        // -- loop was successful
                        // add offset pixel to result array
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < front; y++) {
                                int p = IntegerTools.makeInt(x, y);
                                int[] pixel = pixels.get(p);
                                if (pixel != null) {
                                    result.put(p, pixel);
                                }
                            }
                            for (int y = back; y < height; y++) {
                                int p = IntegerTools.makeInt(x, y);
                                int[] pixel = pixels.get(p);
                                if (pixel != null) {
                                    int[] newPixel = new int[]{
                                            x , y - dist + dist / stepSize, pixel[2]
                                    };
                                    result.put(IntegerTools.makeInt(newPixel[0], newPixel[1]), newPixel);
                                }
                            }
                        }
                        pixels.clear();
                        pixels.putAll(result);
                        height = front + (height - back) + dist / stepSize;
                        factor = stepSize;
                        break;
                    }
                }
            } else {
                for (int d = 1, len = dist / 2 + 1; d < len; d++) {
                    loop:
                    if (dist % d == 0) {
                        // potential new pixel representation
                        TIntObjectHashMap<int[]> result = new TIntObjectHashMap<int[]>();
                        // the step width that would be compressed to one pixel
                        int stepSize = dist / d;
                        // loop over all steps
                        for (int x = 0; x < d; x++) {
                            // loop over height
                            for (int y = 0; y < height; y++) {
                                Integer lastColor = null;
                                // loop over step
                                for (int i = 0; i < stepSize; i++) {
                                    // compute the current point
                                    int p = IntegerTools.makeInt(front + x * stepSize + i, y);
                                    // obtain the pixel
                                    int[] pixel = pixels.get(p);
                                    if (pixel != null) {
                                        // check if the pixel color is consistent through this step
                                        if (lastColor == null) {
                                            lastColor = pixel[2];
                                            result.put(IntegerTools.makeInt(front + x, y), new int[]{front + x, y, pixel[2]});
                                        } else {
                                            if (lastColor != pixel[2]) {
                                                break loop;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        // -- loop was successful
                        // add offset pixel to result array
                        for (int y = 0; y < height; y++) {
                            for (int x = 0; x < front; x++) {
                                int p = IntegerTools.makeInt(x, y);
                                int[] pixel = pixels.get(p);
                                if (pixel != null) {
                                    result.put(p, pixel);
                                }
                            }
                            for (int x = back; x < width; x++) {
                                int p = IntegerTools.makeInt(x, y);
                                int[] pixel = pixels.get(p);
                                if (pixel != null) {
                                    int[] newPixel = new int[]{
                                            x - dist + dist / stepSize, y, pixel[2]
                                    };
                                    result.put(IntegerTools.makeInt(newPixel[0], newPixel[1]), newPixel);
                                }
                            }
                        }
                        pixels.clear();
                        pixels.putAll(result);
                        width = front + (width - back) + dist / stepSize;
                        factor = stepSize;
                        break;
                    }
                }
            }
        }
        return new int[]{width, height, factor};
    }
}
