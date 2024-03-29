
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketAddress;

/**
 * Remote proxy relation.
 * 
 * This relation provides a proxy relation for a remote relation.
 */
public class ClientProxy implements DBIterator {

    private static final int OP_OPEN = 0;
    private static final int OP_NEXT = 1;
    private static final int OP_CLOSE = 2;

    /**
     * Creates a new proxy.
     * 
     * @param endpoint the endpoint to connect to
     */
    public ClientProxy(SocketAddress endpoint) {
        this.endpoint = endpoint;
        this.socket = null;
    }

    /**
     * Opens this relation.
     * 
     * Etablishes a connection to the remote endpoint and open the remote 
     * relation.
     * 
     * @return the attributes of the remote relation
     * @throws IOException if network connection failed
     * @throws ClassNotFoundException if reading from the connection failed
     */
    @Override
    public String[] open() throws IOException, ClassNotFoundException {
        this.socket = new Socket();
        this.socket.connect(this.endpoint);

        // send an open request to the server
        final OutputStream sink = this.socket.getOutputStream();
        sink.write(OP_OPEN);
        sink.flush();

        // retrieve the list of attributes
        final ObjectInputStream source = new ObjectInputStream(
                this.socket.getInputStream());
        final int length = source.readInt();
        final String[] names = new String[length];
        for (int i = 0; i < length; ++i) {
            names[i] = (String) source.readObject();
        }
        return names;
    }

    /**
     * Retrieves the next tuple from the remote relation.
     * 
     * @return the next tuple, or null, if there are no further tuples
     * @throws IOException if the network connection failed
     * @throws ClassNotFoundException if reading from the connection failed
     */
    @Override
    public Object[] next() throws IOException, ClassNotFoundException {
        if (this.socket == null) {
            throw new IOException("Stream closed");
        }

        // send a next request to the server
        final OutputStream sink = this.socket.getOutputStream();
        sink.write(OP_NEXT);

        // read the result
        final ObjectInputStream source = new ObjectInputStream(
                this.socket.getInputStream());
        final int length = source.readInt();
        if (length < 0) {
            // server doesn't have no tuples anymore
            return null;
        } else {
            final Object[] values = new Object[length];
            for (int i = 0; i < length; ++i) {
                values[i] = source.readObject();
            }
            return values;
        }
    }

    /**
     * Closes this relation.
     * 
     * Closes the relation on the remote side and the socket.
     * 
     * @throws IOException if the network connection failed
     */
    @Override
    public void close() throws IOException {
        if (this.socket != null) {
            final OutputStream sink = this.socket.getOutputStream();
            sink.write(OP_CLOSE);
            sink.flush();
            this.socket.close();
        }
        this.socket = null;
    }
    
    /**
     * The endpoint to connect to.
     */
    SocketAddress endpoint;
    
    /**
     * The socket, or null if the relation is closed.
     */
    Socket socket;
}
