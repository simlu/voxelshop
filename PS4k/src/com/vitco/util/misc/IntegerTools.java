package com.vitco.util.misc;

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
}
