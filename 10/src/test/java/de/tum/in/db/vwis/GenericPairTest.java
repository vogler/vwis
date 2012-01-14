package de.tum.in.db.vwis;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class GenericPairTest {

    @Test
    public void testSameTypes() {
        GenericPair<String, String> pair = new GenericPair<>("a", "b");
        assertEquals("a", pair.getFirst());
        assertEquals("b", pair.getSecond());
    }

    @Test
    public void testDifferentTypes() {
        GenericPair<Integer, String> pair = new GenericPair<>(1, "foo");
        assertEquals(new Integer(1), pair.getFirst());
        assertEquals("foo", pair.getSecond());
    }

    @Test
    public void testToString() {
        GenericPair<Integer, String> pair = new GenericPair<>(1, "foo");
        assertEquals("(1, foo)", pair.toString());
    }
}
