package de.tum.in.vwis.group14.db.net;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

/**
 * A network operation.
 */
public enum Operation {

    /**
     * Opens a remote collection.
     */
    Open,
    /**
     * Requests the next tuple from a remote collection.
     */
    Next,
    /**
     * Closes the remote collection.
     */
    Close;

    /**
     * Sends an operation to a remote endpoint.
     * 
     * Sends the request code of this operation over the given socket.
     * 
     * @param socket the socket to request the operation over
     * @throws IOException if the network request failed
     */
    public void send(final Socket socket) throws IOException {
        final OutputStream sink = socket.getOutputStream();
        sink.write(this.ordinal());
        sink.flush();
    }

    /**
     * Receives an operation from a remote endpoint.
     * 
     * Attempts to receive an operation code from the given socket.
     * 
     * @param socket the socket to receive the operation from
     * @return the received operation, or null, if end of stream is reached
     * @throws IOException if the network response failed
     * @throws UnknownOperationException if an unknown operation is received
     */
    static public Operation receive(final Socket socket) throws IOException,
            UnknownOperationException {
        final int receivedByte = socket.getInputStream().read();
        if (receivedByte == -1) {
            return null;
        } else {
            try {
                return Operation.values()[receivedByte];
            } catch (IndexOutOfBoundsException error) {
                throw new UnknownOperationException(
                        String.format("Unknown operation code: %s",
                        receivedByte), error);
            }
        }
    }
}
