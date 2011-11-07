package de.tum.in.vwis.group14.db.net;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.net.SocketAddress;

import de.tum.in.vwis.group14.db.DBIterator;

/**
 * Receive a relation from a remote endpoint.
 */
public class Receive implements DBIterator {

    /**
     * The socket, or null if the relation is closed.
     */
    private Socket socket;

    /**
     * The endpoint to receive the tuples from.
     */
    private final SocketAddress endpoint;

    public Receive(final SocketAddress endpoint) {
        this.endpoint = endpoint;
    }

    /**
     * Whether this receiver is closed.
     * 
     * @return true, if the receiver is closed, false otherwise
     */
    public boolean isClosed() {
        return this.socket == null || this.socket.isClosed();
    }

    /**
     * Closes this relation.
     * 
     * Closes the relation on the remote side and the socket.
     * 
     * @throws IOException
     *             if the network connection failed
     */
    @Override
    public void close() throws IOException {
        if (!this.isClosed()) {
            Operation.Close.sendTo(this.socket);
            this.socket.close();
        }
        this.socket = null;
    }

    /**
     * Retrieves the next tuple from the remote relation.
     * 
     * @return the next tuple, or null, if there are no further tuples
     * @throws IOException
     *             if the network connection failed
     * @throws ClassNotFoundException
     *             if reading from the connection failed
     */
    @Override
    public String[] open() throws IOException, ClassNotFoundException {
        this.socket = new Socket();
        this.socket.connect(this.endpoint);

        // send an open request to the server
        Operation.Open.sendTo(this.socket);

        // retrieve the list of attributes
        final ObjectInputStream source = new ObjectInputStream(
                this.socket.getInputStream());
        return (String[]) source.readObject();
    }

    /**
     * Retrieves the next tuple from the remote relation.
     * 
     * @return the next tuple, or null, if there are no further tuples
     * @throws IOException
     *             if the network connection failed
     * @throws ClassNotFoundException
     *             if reading from the connection failed
     */
    @Override
    public Object[] next() throws IOException, ClassNotFoundException {
        if (this.isClosed()) {
            throw new IOException("Relation closed");
        }

        // send a next request to the server
        Operation.Next.sendTo(this.socket);

        // read the result
        final ObjectInputStream source = new ObjectInputStream(
                this.socket.getInputStream());
        return (Object[]) source.readObject();
    }

}
