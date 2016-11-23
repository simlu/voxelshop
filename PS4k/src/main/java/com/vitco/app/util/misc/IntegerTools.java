package com.vitco.app.util.misc;

import java.util.ArrayList;

/**
 * Fast integer tools.
 */
public final class IntegerTools {

    private IntegerTools() {}

    // =============
    // rounds towards infinity (instead of zero)

    // n/d - optimized version for positive d
    public static int ifloordiv2(int n, int d) {
        if (n >= 0)
            return n / d;
        else
            return ~(~n / d);
    }

    // n%d - optimized version for positive d
    public static int ifloormod2(int n, int d) {
        if (n >= 0)
            return n % d;
        else
            return d + ~(~n % d);
    }

    // ============

    // n/d - for any d
    public static int ifloordiv(int n, int d) {
        if (d >= 0)
            return n >= 0 ? n / d : ~(~n / d);
        else
            return n <= 0 ? n / d : (n - 1) / d - 1;
    }

    // n%d - for any d
    public static int ifloormod(int n, int d) {
        if (d >= 0)
            return n >= 0 ? n % d : d + ~(~n % d);
        else
            return n <= 0 ? n % d : d + 1 + (n - 1) % d;
    }

    // alternative signum
    public static int sign(int i) {
        if (i == 0) return 0;
        if (i >> 31 != 0) return -1;
        return +1;
    }

    // =============

    // convert two shorts into an integer id
    public static int makeInt(short x, short y) {
        return (x << 16) | (y & 0xFFFF);
    }
    // convert two integers into an integer id (assuming that the int
    // values are actually shorts);
    public static int makeInt(int x, int y) {
        return (((short)x) << 16) | (((short)y) & 0xFFFF);
    }
    // obtain the two values from an integer
    public static short[] getShorts(int val) {
        return new short[] {(short) (val >> 16), (short) (val & 0xFFFF)};
    }

    // =============

    // find all factors (not just prime factors) of an integer
    // Note: this includes one or the number itself (!)
    public static int[] findFactors(int input) {
        ArrayList<Integer> result = new ArrayList<Integer>();
        int p = 0;
        for (int i = 1; i < input; i++) {
            if (input%i == 0) {
                int other = input/i;
                if (i < other) {
                    result.add(p++, i);
                    result.add(p, other);
                } else if (i == other) {
                    result.add(p, i);
                    break;
                } else {
                    break;
                }
            }
        }
        // convert to array
        int[] resultArray = new int[result.size()];
        int i = 0;
        for (Integer val : result) {
            resultArray[i++] = val;
        }
        return resultArray;
    }
}
