package de.tum.in.vwis.group14.db;

import java.io.IOException;
import java.util.List;

/**
 * A DBIterator interfacing a list.
 */
public class ListDBIterator implements DBIterator {

    /**
     * Creates a new iterator for the specified list.
     * 
     * @param names the attribute names
     * @param tuples the tuples to iterate over
     */
    public ListDBIterator(String[] names, List<Object[]> tuples) {
        if (names == null) {
            throw new IllegalArgumentException("names null");
        } else if (names.length < 1) {
            throw new IllegalArgumentException("names empty");
        }
        if (tuples == null) {
            throw new IllegalArgumentException("tuples null");
        }
        this.names = names;
        this.tuples = tuples;
        this.current = -1;
    }

    /**
     * Opens the relation.
     * 
     * Resets the reference to the current row to the first row.  A subsequent 
     * invocation of next() consequently returns the first tuple of the 
     * relation.
     * 
     * @return the attribute names of this relation
     */
    @Override
    public String[] open() {
        this.current = 0;
        return this.names;
    }

    /**
     * Gets the next tuple from this relation.
     * 
     * @return the next tuple, or null, if there are no further tuples
     * @throws IOException if the relation is closed
     */
    @Override
    public Object[] next() throws IOException {
        if (this.current < 0) {
            throw new IOException("Relation closed");
        } else if (this.current >= this.tuples.size()) {
            return null;
        } else {
            final Object[] tuple = this.tuples.get(this.current);
            this.current++;
            return tuple;
        }
    }

    /**
     * Closes this relation.
     */
    @Override
    public void close() {
        this.current = -1;
    }
    /**
     * The attribute names of this relation
     */
    private final String[] names;
    /**
     * The tuples interfaced by this iterator
     */
    private final List<Object[]> tuples;
    /**
     * The current row
     */
    private int current;
}
