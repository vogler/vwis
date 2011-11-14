package vwis.blatt4;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

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

    public void testAddObjectNull() throws IOException {
        this.filter.addObject(null);
        assertTrue(this.filter.matches(null));
    }

    @Test
    public void testMatches() throws IOException {
        assertFalse(this.filter.matches(10));
        this.filter.addObject(10);
        assertTrue(this.filter.matches(10));
        assertFalse(this.filter.matches(20));
    }

}
