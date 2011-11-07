package de.tum.in.vwis.group14.db.net;

import java.io.IOException;

/**
 * Serves a relation over network.
 */
public class ServerProxy implements AutoCloseable {

    private final Send sender;

    /**
     * Creates a new proxy.
     *
     * @param sender
     *            the send operator to use
     */
    public ServerProxy(Send sender) {
        this.sender = sender;
    }

    /**
     * Serves the relation to the client.
     *
     * @throws IOException
     *             if the network connection failed
     * @throws Exception
     *             if access to the underlying relation failed
     */
    public void serveForever() throws IOException, Exception {
        while (!this.sender.isClosed()) {
            this.sender.handleRequest();
        }
    }

    /**
     * Closes this server proxy.
     *
     * @throws Exception
     *             if closing the underlying sender failed
     */
    @Override
    public void close() throws Exception {
        this.sender.close();
    }
}
