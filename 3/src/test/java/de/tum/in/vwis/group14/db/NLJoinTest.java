package de.tum.in.vwis.group14.db;

import java.util.ArrayList;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import static de.tum.in.vwis.group14.db.IsClosed.isClosed;

public class NLJoinTest {

    private final static String[] LEFT_NAMES =
            new String[]{"id", "name", "height"};
    private final static String[] RIGHT_NAMES =
            new String[]{"id", "name", "age"};
    private final static String[] JOIN_NAMES =
            new String[]{"id", "name", "height", "age"};
    private final static List<Object[]> LEFT_TUPLES = new ArrayList<>();
    private final static List<Object[]> RIGHT_TUPLES = new ArrayList<>();
    private ListDBIterator left;
    private ListDBIterator right;
    private NLJoin relation;

    public NLJoinTest() {
    }

    static {
        LEFT_TUPLES.add(new Object[]{0, "foo", 10});
        LEFT_TUPLES.add(new Object[]{1, "spam", 20});
        LEFT_TUPLES.add(new Object[]{2, "nobody", 30});
        RIGHT_TUPLES.add(new Object[]{1, "spam", 100});
        RIGHT_TUPLES.add(new Object[]{2, "somebody", 200});
        RIGHT_TUPLES.add(new Object[]{3, "eggs", 300});
    }

    @Before
    public void setUp() {
        this.left = new ListDBIterator(LEFT_NAMES, LEFT_TUPLES);
        this.right = new ListDBIterator(RIGHT_NAMES, RIGHT_TUPLES);
        this.relation = new NLJoin(this.left, this.right);
    }

    @After
    public void tearDown() throws Exception {
        this.left.close();
        this.right.close();
        this.relation.close();
    }

    @Test
    public void testOpen() throws Exception {
        assertArrayEquals(JOIN_NAMES, this.relation.open());
        // check re-opening
        assertArrayEquals(JOIN_NAMES, this.relation.open());
    }

    @Test
    public void testNext() throws Exception {
        this.relation.open();
        assertArrayEquals(new Object[]{1, "spam", 20, 100},
                this.relation.next());
        assertNull(this.relation.next());
    }

    @Test
    public void testClose() throws Exception {
        this.relation.open();
        this.relation.close();
        assertThat(this.relation, isClosed());
        assertThat(this.left, isClosed());
        assertThat(this.right, isClosed());
    }

    @Test
    public void testLeftSideEmpty() throws Exception {
        this.left = new ListDBIterator(LEFT_NAMES, new ArrayList<Object[]>());
        this.relation = new NLJoin(this.left, this.right);
        assertArrayEquals(JOIN_NAMES, this.relation.open());
        assertNull(this.relation.next());
    }
    
    @Test
    public void testEmptySideEmpty() throws Exception {
        this.right = new ListDBIterator(RIGHT_NAMES, new ArrayList<Object[]>());
        this.relation = new NLJoin(this.left, this.right);
        assertArrayEquals(JOIN_NAMES, this.relation.open());
        assertNull(this.relation.next());
    }
    
    @Test
    public void testBothSidesEmpty() throws Exception {
        this.left = new ListDBIterator(LEFT_NAMES, new ArrayList<Object[]>());
        this.right = new ListDBIterator(RIGHT_NAMES, new ArrayList<Object[]>());
        this.relation = new NLJoin(this.left, this.right);
        assertArrayEquals(JOIN_NAMES, this.relation.open());
        assertNull(this.relation.next());
    }
    
    @Test
    public void testIdentity() throws Exception {
        this.right = new ListDBIterator(LEFT_NAMES, LEFT_TUPLES);
        this.relation = new NLJoin(this.left, this.right);
        assertArrayEquals(LEFT_NAMES, this.relation.open());
        assertArrayEquals(LEFT_TUPLES.get(0), this.relation.next());
        assertArrayEquals(LEFT_TUPLES.get(1), this.relation.next());
        assertArrayEquals(LEFT_TUPLES.get(2), this.relation.next());
        assertNull(this.relation.next());
    }
}
