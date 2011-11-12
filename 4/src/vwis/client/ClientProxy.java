package vwis.client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import vwis.common.DBIterator;
import vwis.server.ServerProxy;

/**
 * Class for simple network communication. This is not the ClientProxy from
 * Assignment 2, this class is now named SimpleClientProxy instead.
 *
 */
public abstract class ClientProxy implements DBIterator {

    protected Socket socket;
    protected ObjectOutputStream out;
    protected ObjectInputStream in;
    private String host;
    private int port;
    private boolean connectionOpen;

    protected ClientProxy(String host, int port) throws IOException {
        this.host = host;
        this.port = port;
    }

    /**
     * Opens the connection to the server unless it is already open.
     *
     * @throws IOException
     */
    protected void openConnection() throws IOException {
        if (this.connectionOpen) {
            return;
        }
        this.connectionOpen = true;
        this.socket = new Socket(this.host, this.port);
        this.out = new ObjectOutputStream(this.socket.getOutputStream());
        this.in = new ObjectInputStream(this.socket.getInputStream());
        this.out.writeObject(this.getServerCommand());
    }

    /**
     * Closes the connection to the server. If the connection is not yet open
     *
     * @throws IOException
     */
    protected void closeConnection() throws IOException {
        if (!this.connectionOpen) {
            return;
        }
        this.connectionOpen = false;
        this.out.close();
        this.in.close();
        this.socket.close();
    }

    /**
     * Template method which returns the server side of this client proxy. Once
     * a connection is opened, the proxy returned by this method will be
     * transferred to the server and executed there and the opened connection
     * will communicate with this proxy.
     *
     * Make sure that a proxy returned by this method is serializable!
     *
     * @return
     */
    protected abstract ServerProxy getServerCommand();

}
