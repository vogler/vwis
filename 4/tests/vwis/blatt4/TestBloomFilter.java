package vwis.blatt4;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.BitSet;

import org.junit.Before;
import org.junit.Test;

public class TestBloomFilter {

    private BloomFilter filter;
    private static int BITMAP_SIZE = 3;
    private static int HASH_FUNCTIONS = 5;

    @Before
    public void setUp() {
        this.filter = new BloomFilter(BITMAP_SIZE, HASH_FUNCTIONS);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testTooFewHashFunctions() {
        new BloomFilter(BITMAP_SIZE, 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testTooSmallBitmapSizs() {
        new BloomFilter(0, HASH_FUNCTIONS);
    }

    @Test
    public void testGetBitmapSize() {
        assertEquals(BITMAP_SIZE, this.filter.getBitmapSize());
    }

    @Test
    public void testGetNumberOfBits() {
        assertEquals(BITMAP_SIZE*8, this.filter.getNumberOfBits());
    }

    @Test
    public void testGetNumberOfHashFunctions() {
        assertEquals(HASH_FUNCTIONS, this.filter.getNumberOfHashFunctions());
    }

    @Test(expected = NullPointerException.class)
    public void testAddObjectNull() {
        this.filter.addObject(null);
    }

    @Test
    public void testAddObject() {
        this.filter.addObject(10);
        final BitSet expected = new BitSet(this.filter.getNumberOfBits());
        expected.set(10);
        expected.set(20);
        expected.set(6);
        expected.set(16);
        expected.set(2);
        assertArrayEquals(expected.toByteArray(), this.filter.getBitVector());
    }

    @Test
    public void testMatches() {
        assertFalse(this.filter.matches(10));
        this.filter.addObject(10);
        assertTrue(this.filter.matches(10));
        assertFalse(this.filter.matches(20));
    }

}
