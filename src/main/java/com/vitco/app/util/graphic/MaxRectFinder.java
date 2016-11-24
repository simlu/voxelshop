package com.vitco.app.util.graphic;

import java.awt.*;
import java.util.LinkedList;

/**
 * Handles finding of a maximal rectangle in a binary 2D array.
 *
 * Maximum is defined as "Math.min(width, height) * 16384 + Math.max(width, height)"
 *
 * Hence the maximum image is 16384 ^ 2
 *
 * Idea taken from:
 * www.drdobbs.com/database/the-maximal-rectangle-problem/184410529
 */
public class MaxRectFinder {

    // defines the size (for comparison)
    private static int getSize(short width, short height) {
        return Math.min(width, height) * 16384 + Math.max(width, height);
    }

    // takes as input a matrix with ones/zeros
    public static Rectangle maximalRectangle(short[][] matrix) {
        // extract static length information
        short lenX = (short) matrix.length;
        short lenY = (short) matrix[0].length;

        // loop over columns and fill in numbers that count how many
        // ones are above (# + 1)
        short previous;
        for (short x = 0; x < lenX; x++) {
            previous = matrix[x][0];
            for (short y = 1; y < lenY; y++) {
                if (matrix[x][y] == 1) {
                    previous++;
                    matrix[x][y] = previous;
                } else {
                    previous = 0;
                }
            }
        }

        // loop over rows and keep track of the rectangles that we find
        LinkedList<short[]> stack = new LinkedList<short[]>();

        // tmp variable
        short[] info;
        int size = 0;
        Rectangle result = null;

        // loop over rows
        short nHeight;
        for (short y = 0; y < lenY; y++) {
            short height = 0;
            for (short x = 0; x < lenX; x++) {
                nHeight = matrix[x][y];
                if (height < nHeight) {
                    // open rectangle (start position and height)
                    while (height < nHeight) {
                        height++;
                        stack.push(new short[]{x, height});
                    }
                } else if (height > nHeight) {
                    // close rectangles
                    height = nHeight;
                    do {
                        // close rectangles until the next doesn't need closing
                        info = stack.pop();
                        if (info[1] > height) {
                            int sizeTmp = getSize((short) (x - info[0]), info[1]);
                            if (sizeTmp > size) {
                                result = new Rectangle(info[0], y - info[1] + 1, x - info[0], info[1]);
                                size = sizeTmp;
                            }
                        }
                    } while (!stack.isEmpty() && info[1] > height);
                    // add missing rectangle
                    if (!(info[1] > height)) {
                        stack.push(info);
                    }
                }
            }
            // handle remaining stack rectangles
            while (!stack.isEmpty()) {
                info = stack.pop();
                int sizeTmp = getSize((short) (lenX - info[0]), info[1]);
                if (sizeTmp > size) {
                    result = new Rectangle(info[0], y - info[1] + 1, lenX - info[0], info[1]);
                    size = sizeTmp;
                }
            }
        }

        return result;
    }

}
