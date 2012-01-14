package de.tum.in.db.vwis;

/**
 * A generic pair whose components have the same type.
 *
 * @param <T> the component type
 */
public class Pair<T> extends GenericPair<T, T> {

    /**
     * Creates a new pair.
     *
     * @param first the first component
     * @param second the second component
     */
    public Pair(T first, T second) {
        super(first, second);
    }
    
}
