package vwis.blatt4;

/**
 * Thrown if an unknown operation is requested.
 */
@SuppressWarnings("serial")
public class UnknownOperationException extends Exception {

    /**
     * Creates a new exception with the specified message.
     *
     * @param message
     *            a message
     */
    public UnknownOperationException(String message) {
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
    public UnknownOperationException(String message, Throwable cause) {
        super(message, cause);
    }

}
