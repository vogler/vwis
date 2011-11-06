package de.tum.in.vwis.group14.db;

/**
 * Thrown to indicate a table of invalid format.
 */
@SuppressWarnings("serial")
public class TableFormatException extends Exception {

    /**
     * Creates an exception with the specified message.
     *
     * @param message
     *            a message
     */
    public TableFormatException(String message) {
        super(message);
    }

    /**
     * Creates an exception with the specified message and cause.
     *
     * @param message
     *            a message
     * @param cause
     *            the cause
     */
    public TableFormatException(String message, Throwable cause) {
        super(message, cause);
    }

}
