package de.tum.in.vwis.group14.db;

import java.io.IOException;
import org.hamcrest.Description;

/**
 * Matches a closed relation.
 */
public class IsClosed extends org.hamcrest.BaseMatcher<DBIterator> {

    /**
     * Creates a new matcher.
     * 
     * This is simply a convenience method.
     * 
     * @return a new matcher
     */
    public static IsClosed isClosed() {
        return new IsClosed();
    }

    /**
     * Creates a new matcher.
     */
    public IsClosed() {
    }

    /**
     * Matches a closed relation.
     * 
     * A relation is closed, if the attempt to call next() on it throws an 
     * IOException with message "Relation closed";
     * 
     * @param item a closed relation
     * @return true, if item is a closed relation, false otherwise
     */
    @Override
    public boolean matches(final Object item) {
        if (item instanceof DBIterator) {
            final DBIterator iterator = (DBIterator) item;
            try {
                iterator.next();
                return false;
            } catch (IOException error) {
                return error.getMessage().equals("Relation closed");
            } catch (Exception error) {
                return false;
            }
        } else {
            return false;
        }
    }

    /**
     * Adds a description of this object to the given description. 
     * 
     * @param description the description to add to
     */
    @Override
    public void describeTo(Description description) {
        description.appendText("closed relation");
    }
}
