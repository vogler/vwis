package de.tum.in.vwis.group14.db.net;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import de.tum.in.vwis.group14.db.DBIterator;

/**
 * Sends a relation to a client.
 */
public class Send implements AutoCloseable {

    /**
     * The client to send to.
     */
    private final Socket client;

    /**
     * The relation to send.
     */
    private final DBIterator relation;

    /**
     * Creates a new sender.
     *
     * The sender sends the given relation to the given client.
     *
     * @param client
     *            the client to send to
     * @param relation
     *            the relation to send to the client
     */
    public Send(final Socket client, final DBIterator relation) {
        this.client = client;
        this.relation = relation;
    }

    /**
     * Sends a single object to the client and flushes the stream.
     *
     * @param obj
     *            the object to send
     * @throws IOException
     *             if sending fails
     */
    private void sendObject(final Object obj) throws IOException {
        final ObjectOutputStream sink = new ObjectOutputStream(
                this.client.getOutputStream());
        sink.writeObject(obj);
        sink.flush();
    }

    /**
     * Sends the specified amount of tuples to the remote side.
     *
     * @param tuplesToSend
     *            the amount of tuples to send
     * @throws IOException
     *             if network access failed
     * @throws Exception
     *             if retrieving the next tuple from the underlying relation
     *             failed
     */
    private void sendTuples(int tuplesToSend) throws Exception {
        final ObjectOutputStream sink = new ObjectOutputStream(
                this.client.getOutputStream());
        while (tuplesToSend > 0) {
            sink.writeObject(this.relation.next());
            tuplesToSend--;
        }
        sink.flush();
    }

    /**
     * Receives and handles a single request.
     *
     * @throws Exception
     */
    public void handleRequest() throws Exception {
        try {
            final Operation operation = Operation.receiveFrom(this.client);
            if (operation == null) {
                // end of stream
                this.close();
            }
            switch (operation) {
            case Open:
                this.sendObject(this.relation.open());
                break;
            case Next:
                this.sendObject(this.relation.next());
                break;
            case MultipleNext:
                final ObjectInputStream source = new ObjectInputStream(
                        this.client.getInputStream());
                final int tuplesToSend = source.readInt();
                this.sendTuples(tuplesToSend);
                break;
            case Close:
                this.close();
                break;
            }
        } catch (UnknownOperationException error) {
            ObjectOutputStream sink = new ObjectOutputStream(
                    this.client.getOutputStream());
            sink.writeObject(error.getMessage());
        }
    }

    /**
     * Closes this send operator.
     *
     * @throws Exception
     *             if closing the underlying relation failed
     * @throws IOException
     *             if closing the underlying socket failed
     */
    @Override
    public void close() throws Exception, IOException {
        this.relation.close();
        this.client.close();
    }

    /**
     * Whether this sender is closed or not.
     *
     * @return true, if the sender is closed, false otherwise
     */
    public boolean isClosed() {
        return this.client.isClosed();
    }
}
