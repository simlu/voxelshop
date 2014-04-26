package com.vitco.util.misc;

import org.junit.Test;

public class IntegerToolsTest {

    // test that the conversion is always correct
    @Test
    public void testMakeInt() throws Exception {
        for (short x = Short.MIN_VALUE; x < Short.MAX_VALUE; x++) {
            for (short y = Short.MIN_VALUE; y < Short.MAX_VALUE; y++) {
                //System.out.println(" :: " + y);
                short[] result = IntegerTools.getShorts(IntegerTools.makeInt(x,y));
                assert result[0] == x;
                assert result[1] == y;
            }
        }
    }
}