package de.tum.in.db.vwis;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;

import static de.tum.in.db.vwis.BigMath.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(Parameterized.class)
public class BigMathRandomTest {

    private static final int NUMBER_OF_TESTS = 1000;
    private final BigInteger n;

    @Parameterized.Parameters
    public static Collection<Object[]> randomBigIntegers() {
        final Random rng = new Random();
        final Collection<Object[]> params = new ArrayList<>(NUMBER_OF_TESTS);
        for (int i = 0; i < NUMBER_OF_TESTS; ++i) {
            params.add(new Object[]{new BigInteger(50, rng)});
        }
        return params;
    }

    public BigMathRandomTest(final BigInteger n) {
        this.n = n;
    }

    @Test
    public void testEvenOdd() {
        assertTrue(2 >= Character.MIN_RADIX);
        assertTrue(2 <= Character.MAX_RADIX);
        final boolean lastBitSet = n.toString(2).endsWith("1");
        assertTrue(isOdd(n) == lastBitSet);
        assertFalse(isEven(n) == lastBitSet);
    }

    @Test
    public void testRandomOddInteger() throws Exception {
        assertTrue(isOdd(randomOddInteger(50)));
    }

    @Test
    public void testComputeWitnesses() {
        final List<BigInteger> witnesses = computeWitnesses(n, 20);
        assertEquals(20, witnesses.size());
        for (final BigInteger w : witnesses) {
            assertTrue(w.compareTo(n) < 0);
        }
    }

    @Test()
    public void testComputePrime() {
        /// FIXME: test case!
    }

    @Test
    public void testComputeLargerPrime() {
        final BigInteger p = computeLargerPrime(this.n, 20);
        assertTrue(p.compareTo(this.n) > 0);
    }
    
    @Test
    public void testExtendedEuclidian() {
        final BigInteger[] abc = extendedEuclidian(n, n.divide(BigMath.TWO));
        assertEquals(3, abc.length);
    }
}
