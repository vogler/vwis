package de.tum.in.vwis.group14.db.net;

import de.tum.in.vwis.group14.db.DBIterator;
import de.tum.in.vwis.group14.db.Tablescan;
import java.io.Closeable;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;

/**
 * Serves a relation from a file over network.
 */
public class TablescanServer implements Closeable {

    /**
     * Serves the relation to a single client in a thread.
     */
    private static class ProxyRunnable implements Runnable {

        /**
         * Creates a new proxy runnable.
         * 
         * @param client the client to serve
         * @param relation the relation to serve
         */
        public ProxyRunnable(final Socket client, final DBIterator relation) {
            this.relation = relation;
            this.client = client;
        }

        public void run() {
            try (final ServerProxy proxy = new ServerProxy(
                            this.client, this.relation)) {
                proxy.serveForever();
            } catch (Exception exc) {
                exc.printStackTrace();
            }
        }
        private DBIterator relation;
        private Socket client;
    }

    /**
     * Creates a new serves.
     * 
     * @param endpoint the endpoint to bind to
     * @param filename a relation file to serve
     */
    public TablescanServer(SocketAddress endpoint, String filename) {
        this.filename = filename;
        this.endpoint = endpoint;
        this.socket = null;
    }

    /**
     * Closes the server.
     * 
     * @throws IOException if network connection failed
     */
    public void close() throws IOException {
        if (this.socket != null) {
            this.socket.close();
        }
        this.socket = null;
    }

    /**
     * Serves the relation forever.
     * 
     * @throws IOException if network connection failed
     */
    public void serveForever() throws IOException {
        this.socket = new ServerSocket();
        this.socket.bind(this.endpoint);

        while (true) {
            final Socket client = this.socket.accept();
            final DBIterator relation = new Tablescan(this.filename);

            new Thread(new ProxyRunnable(client, relation)).start();
        }
    }
    
    /**
     * The endpoint to bind to.
     */
    private SocketAddress endpoint;
    
    /**
     * The filename from which to read the relation.
     */
    private String filename;
    
    /**
     * The server socket, or null, if the server is closed.
     */
    private ServerSocket socket;
}
