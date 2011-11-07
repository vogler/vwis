package de.tum.in.vwis.group14.db.net;

import java.io.IOException;

import de.tum.in.vwis.group14.db.DBIterator;

/**
 * Remote proxy relation.
 * 
 * This relation provides a proxy relation for a remote relation.
 */
public class ClientProxy implements DBIterator {
    /**
     * The receiver to receive data with.
     */
    private final Receive receiver;

    /**
     * Creates a new proxy.
     * 
     * @param receiver
     *            the receiver to receive from
     */
    public ClientProxy(final Receive receiver) {
        this.receiver = receiver;
    }

    /**
     * Opens this relation.
     * 
     * Etablishes a connection to the remote endpoint and open the remote
     * relation.
     * 
     * @return the attributes of the remote relation
     * @throws IOException
     *             if network connection failed
     * @throws ClassNotFoundException
     *             if reading from the connection failed
     */
    @Override
    public String[] open() throws IOException, ClassNotFoundException {
        return this.receiver.open();
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
        return this.receiver.next();
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
        this.receiver.close();
    }

    /**
     * Whether this relation is closed.
     * 
     * @return true, if the relation is closed, false otherwise
     */
    public boolean isClosed() {
        return this.receiver.isClosed();
    }

}
