
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * Serves a relation over network.
 */
public class ServerProxy implements AutoCloseable {

    private static final int OP_OPEN = 0;
    private static final int OP_NEXT = 1;
    private static final int OP_CLOSE = 2;

    /**
     * Creates a new proxy.
     * 
     * @param client the client to serve the relation to
     * @param relation the relation to serve
     */
    public ServerProxy(Socket client, DBIterator relation) {
        this.relation = relation;
        this.client = client;
    }

    /**
     * Sends an array to the client.
     * 
     * An array is send by first sending its length, and then each of the 
     * contained objects over the network.
     * 
     * @param array the array to send, or null
     * @throws IOException if network connection failed
     */
    private void sendArray(final Object[] array) throws IOException {
        final ObjectOutputStream sink = new ObjectOutputStream(
                this.client.getOutputStream());
        if (array == null) {
            sink.writeInt(-1);
        } else {
            sink.writeInt(array.length);
            for (final Object obj : array) {
                sink.writeObject(obj);
            }
        }
        sink.flush();
    }

    /**
     * Serves the relation to the client.
     * 
     * @throws IOException if the network connection failed
     * @throws Exception if access to the underlying relation failed
     */
    public void serveForever() throws IOException, Exception {
        while (true) {
            int request = this.client.getInputStream().read();
            switch (request) {
                case OP_OPEN:
                    this.sendArray(this.relation.open());
                    break;
                case OP_NEXT:
                    this.sendArray(this.relation.next());
                    break;
                case OP_CLOSE:
                    this.close();
                    return;
                case -1:
                    // end of stream
                    this.close();
                    return;
                default:
                    ObjectOutputStream sink = new ObjectOutputStream(
                            this.client.getOutputStream());
                    sink.writeObject(String.format("unknown request: %s",
                            request));
                    break;
            }
        }
    }

    @Override
    public void close() throws Exception {
        this.relation.close();
        this.client.close();
    }
    private DBIterator relation;
    private Socket client;
}
