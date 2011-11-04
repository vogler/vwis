package de.tum.in.vwis.group14.db;

import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;
import static de.tum.in.vwis.group14.db.IsClosed.isClosed;

/**
 *
 */
public class ListDBIteratorTest {

    private static final String[] NAMES = new String[]{"id", "name"};
    private static final List<Object[]> TUPLES;
    private ListDBIterator relation;

    static {
        TUPLES = new ArrayList<>();
        TUPLES.add(new Object[]{1, "bar"});
        TUPLES.add(new Object[]{2, "foo"});
    }

    public ListDBIteratorTest() {
    }

    @Before
    public void setUp() {
        this.relation = new ListDBIterator(NAMES, TUPLES);
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testNullNames() {
        new ListDBIterator(null, TUPLES);
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testEmptyNames() {
        new ListDBIterator(new String[] {}, TUPLES);
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testNullTuples() {
        new ListDBIterator(NAMES, null);
    }
    
    @Test
    public void testEmptyRelation() throws IOException {
        this.relation = new ListDBIterator(
                NAMES, new ArrayList<Object[]>());
        assertArrayEquals(NAMES, this.relation.open());
        assertNull(this.relation.next());
    }
    
    @Test
    public void testOpen() {
        assertArrayEquals(NAMES, this.relation.open());
        // test re-opening
        assertArrayEquals(NAMES, this.relation.open());
    }

    @Test
    public void testNext() throws IOException {
        this.relation.open();
        assertArrayEquals(TUPLES.get(0), this.relation.next());
        assertArrayEquals(TUPLES.get(1), this.relation.next());
        assertNull(this.relation.next());
    }
    
    @Test(expected=IOException.class)
    public void testNextBeforeOpen() throws IOException {
        this.relation.next();
    }

    @Test
    public void testClose() {
        this.relation.open();
        this.relation.close();
        assertThat(this.relation, isClosed());
    }
}
