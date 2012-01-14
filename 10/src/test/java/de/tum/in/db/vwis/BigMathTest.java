package de.tum.in.db.vwis;

import org.junit.Test;

import java.math.BigInteger;

import static de.tum.in.db.vwis.BigMath.*;
import static org.junit.Assert.*;


public class BigMathTest {

    @Test
    public void testEvenOdd() {
        assertFalse(isOdd(BigInteger.ZERO));
        assertTrue(isEven(BigInteger.ZERO));

        assertTrue(isOdd(BigInteger.ONE));
        assertFalse(isEven(BigInteger.ONE));

        assertFalse(isOdd(BigInteger.TEN));
        assertTrue(isEven(BigInteger.TEN));

        final BigInteger p1 = new BigInteger("1000");
        assertFalse(isOdd(p1));
        assertTrue(isEven(p1));

        final BigInteger p2 = new BigInteger("1001");
        assertTrue(isOdd(p2));
        assertFalse(isEven(p2));
    }

    @Test
    public void testRandomOddInteger() {
        assertTrue(isOdd(randomOddInteger(50)));
    }

    @Test
    public void testExtendedEuclidian() {
        final BigInteger[] abc = extendedEuclidian(BigMath.TWO,
                BigInteger.ZERO);
        assertArrayEquals(new BigInteger[]{BigMath.TWO, BigInteger.ONE,
                BigInteger.ZERO}, abc);
    }
}
