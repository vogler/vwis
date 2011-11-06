package de.tum.in.vwis.group14.db;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Join two relations by their common attributes.
 */
public class NLJoin implements DBIterator {

    /**
     * Creates a new relation joining the given relations.
     *
     * @param left
     *            the left hand side relation of the join operation
     * @param right
     *            the right hand side relation of the join operation
     * @throws IllegalArgumentException
     *             if left and right reference the same object
     */
    public NLJoin(DBIterator left, DBIterator right) {
        this.left = left;
        this.right = right;
        if (this.left == this.right) {
            throw new IllegalArgumentException(
                    "Cannot join iterator with itself");
        }
    }

    /**
     * Opens the joined relations.
     *
     * Opens the underlying relations and computes the set of attributes to join
     * these relations by.
     *
     * @return the attributes of the joined relation
     * @throws Exception
     *             if opening of the underlying relations failed
     */
    @Override
    public String[] open() throws Exception {
        this.leftAttributes = new ArrayList<>(Arrays.asList(this.left.open()));
        this.rightAttributes = new ArrayList<>(Arrays.asList(this.right.open()));

        this.joinAttributes = new HashSet<>(this.leftAttributes);
        this.joinAttributes.retainAll(this.rightAttributes);

        this.allAttributes = new ArrayList<>(this.rightAttributes);
        // avoid duplicates in the final set of attributes
        this.allAttributes.removeAll(this.leftAttributes);
        this.allAttributes.addAll(0, this.leftAttributes);

        return this.allAttributes.toArray(new String[] {});
    }

    /**
     * Return the next tuple of the joined relation.
     *
     * @return the next tuple of the joined relation.
     * @throws Exception
     *             if retrieving tuples from the underlying relations failed
     */
    @Override
    public Object[] next() throws Exception {
        // For each tuple r in R do
        // For each tuple s in S do
        // If r and s satisfy the join condition
        // Then output the tuple <r,s>
        Object[] l, r;
        while ((l = this.left.next()) != null) {
            while ((r = this.right.next()) != null) {
                final Map<String, Object> joined = this.join(
                        this.tupleToMap(this.leftAttributes, l),
                        this.tupleToMap(this.rightAttributes, r));
                if (joined != null) {
                    List<Object> tuple = new ArrayList<>(joined.size());
                    for (String attribute : this.allAttributes) {
                        tuple.add(joined.get(attribute));
                    }
                    return tuple.toArray();
                }
            }
            // reopen the right relation to reset its pointer and get all tuples
            // again in the next iteration over the left relation
            this.right.open();
        }
        return null;
    }

    /**
     * Converts a tuple from a relation into a map.
     *
     * The returned map maps the attribute name to the value and thus provides
     * an easier interface to access values of the tuple.
     *
     * @param attributes
     *            the attribute names
     * @param tuple
     *            the tuple
     * @return a map mapping the attribute names to the corresponding values
     *         from the tuple
     * @throws IllegalArgumentException
     *             if attributes and tuple have different sizes
     */
    private Map<String, Object> tupleToMap(final List<String> attributes,
            final Object[] tuple) {
        if (attributes.size() != tuple.length) {
            throw new IllegalArgumentException("length mismatch");
        }

        final Map<String, Object> map = new HashMap<>(attributes.size());
        for (int i = 0; i < attributes.size(); ++i) {
            map.put(attributes.get(i), tuple[i]);
        }
        return map;
    }

    /**
     * Attempt to join two tuples from the relations.
     *
     * @param l
     *            the left tuple
     * @param r
     *            the right tuple
     * @return the joined tuple or null, if the tuples could not be joined
     */
    private Map<String, Object> join(final Map<String, Object> l,
            final Map<String, Object> r) {
        final Map<String, Object> joined = new HashMap<>(
                this.allAttributes.size());
        for (String attribute : this.allAttributes) {
            final Object leftValue = l.get(attribute);
            final Object rightValue = r.get(attribute);
            if (leftValue != null && rightValue != null) {
                // verify join condition
                if (!leftValue.equals(rightValue)) {
                    // join attributes inequal in relations, cannot join
                    return null;
                }
            }
            if (leftValue != null) {
                joined.put(attribute, leftValue);
            }
            if (rightValue != null) {
                joined.put(attribute, rightValue);
            }

        }
        return joined;
    }

    /**
     * Close the underlying relations.
     *
     * @throws Exception
     *             if closing failed
     */
    @Override
    public void close() throws Exception {
        this.left.close();
        this.right.close();
    }

    /**
     * Left relation for join.
     */
    private DBIterator left;
    /**
     * Right relation for join.
     */
    private DBIterator right;
    /**
     * The attributes of the left hand side relation.
     */
    private List<String> leftAttributes;
    /**
     * The attributes of the right hand side relation.
     */
    private List<String> rightAttributes;
    /**
     * The set of attributes to join the two relations by.
     */
    private Set<String> joinAttributes;
    /**
     * The attributes of the final relation.
     */
    private List<String> allAttributes;
}
