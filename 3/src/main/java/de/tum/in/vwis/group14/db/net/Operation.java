package de.tum.in.vwis.group14.db.net;

import java.io.IOException;
import java.io.InputStream;
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
    public void sendTo(final Socket socket) throws IOException {
        this.writeTo(socket.getOutputStream());
    }
    
    /**
     * Writes an operation to a stream.
     * 
     * @param out the stream to writeTo to
     * @throws IOException if writing failed
     */
    public void writeTo(final OutputStream out) throws IOException {
        out.write(this.ordinal());
        out.flush();
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
    static public Operation receiveFrom(final Socket socket) throws IOException,
            UnknownOperationException {
        return readFrom(socket.getInputStream());
    }

    /**
     * Reads an operation from a stream.
     * 
     * @param in the stream to readFrom from
     * @return the readFrom operation
     * @throws IOException if reading failed
     * @throws UnknownOperationException if an unknown operation code was readFrom
     */
    static public Operation readFrom(final InputStream in) throws IOException, 
            UnknownOperationException {
        final int readByte = in.read();
        if (readByte == -1) {
            return null;
        } else {
            try {
                return Operation.values()[readByte];
            } catch (IndexOutOfBoundsException error) {
                throw new UnknownOperationException(
                        String.format("Unknown operation code: %s", readByte),
                        error);
            }
        }
    }
}
