package de.tum.in.vwis.group14.db;

/**
 * Iterator over a database relation.
 */
public interface DBIterator extends AutoCloseable {

    /**
     * Opens the relation.
     * 
     * Re-opening a relation which was already opened resets the iterator.  The
     * next call to next() will return the first tuple again.
     * 
     * @return the names of the attributes of the relation
     * @throws Exception if opening fails
     */
    public String[] open() throws Exception;

    /**
     * Gets the next tuple from the relation.
     * 
     * @return the next tuple, or null, if no further tuples exist in this relation
     * @throws Exception if the tuple could not be retrieved
     */
    public Object[] next() throws Exception;
}
