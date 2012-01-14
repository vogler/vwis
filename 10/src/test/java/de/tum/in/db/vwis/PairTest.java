package de.tum.in.db.vwis;

import static org.junit.Assert.assertEquals;

public class PairTest {

    @org.junit.Test
    public void testPair() {
        Pair<String> pair = new Pair<>("a", "b");
        assertEquals("a", pair.getFirst());
        assertEquals("b", pair.getSecond());
    }
}
