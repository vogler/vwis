package de.tum.in.vwis.group14.db.net;

import de.tum.in.vwis.group14.db.DBIterator;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * Serves a relation over network.
 */
public class ServerProxy implements AutoCloseable {

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
     * An array is sendTo by first sending its length, and then each of the 
     * contained objects over the network.
     * 
     * @param array the array to sendTo, or null
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
            try {
                final Operation operation = Operation.receiveFrom(this.client);
                if (operation == null) {
                    // end of stream
                    this.close();
                    return;
                }
                switch (operation) {
                    case Open:
                        this.sendArray(this.relation.open());
                        break;
                    case Next:
                        this.sendArray(this.relation.next());
                        break;
                    case Close:
                        this.close();
                        return;
                        
                }
            } catch (UnknownOperationException error) {
                ObjectOutputStream sink = new ObjectOutputStream(
                        this.client.getOutputStream());
                sink.writeObject(error.getMessage());
            }
        }
    }

    /**
     * Closes this server proxy.
     * 
     * @throws Exception if closing the underlying relation failed
     * @throws IOException if closing the underlying socket failed
     */
    @Override
    public void close() throws Exception, IOException {
        this.relation.close();
        this.client.close();
    }
    
    /**
     * The underlying relation.
     */
    private DBIterator relation;
    
    /**
     * The remote client.
     */
    private Socket client;
}
