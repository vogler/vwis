package de.tum.in.db.vwis;

/**
 * A generic, immutable pair.
 *
 * @param <S> the type of the first component
 * @param <T> the type of the second component
 */
public class GenericPair<S, T> {
    private final S first;
    private final T second;

    public GenericPair(S first, T second) {
        this.first = first;
        this.second = second;
    }

    /**
     * Gets the first component of this pair.
     *
     * @return the first component
     */
    public S getFirst() {
        return first;
    }

    /**
     * Gets the second component of this pair.
     *
     * @return the second component
     */
    public T getSecond() {
        return second;
    }

    @Override
    public String toString() {
        return String.format("(%s, %s)", first, second);
    }
}
