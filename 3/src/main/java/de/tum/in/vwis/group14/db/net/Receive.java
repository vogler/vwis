package de.tum.in.vwis.group14.db.net;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.SynchronousQueue;

import de.tum.in.vwis.group14.db.DBIterator;

/**
 * Receive a relation from a remote endpoint.
 */
public class Receive implements DBIterator {

    /**
     * A thread to receive tuples in background.
     */
    private static class ReceiverThread extends Thread {

        /**
         * Wraps a reference to an object.
         *
         * Required to store null references in a Queue, which would cause a
         * NullPointerException otherwise.
         */
        private static class ObjectWrapper {

            /**
             * The wrapped object
             */
            public Object object = null;

            /**
             * Wraps the given object.
             *
             * @param o
             *            the object to wrap
             */
            public ObjectWrapper(Object o) {
                this.object = o;
            }
        }

        private final SocketAddress endpoint;

        /**
         * Buffer for received objects.
         */
        private final BlockingQueue<ObjectWrapper> buffer;

        /**
         * Holds the amount of free places in a buffer.
         */
        private final Semaphore bufferPlaces;

        /**
         * The socket to receive from.
         */
        private final Socket socket;

        /**
         * Exchange exception between thread and caller.
         */
        private final SynchronousQueue<Exception> exception;

        /**
         * Amount of tuples to receive in a single operationl.
         */
        private static int CHUNK_SIZE = 100;

        /**
         * Creates a receiving thread for the given endpoint.
         *
         * @param endpoint
         *            the endpoint to connect to
         */
        public ReceiverThread(final SocketAddress endpoint) {
            // a synchronous queue to move exceptions from the thread to the
            // caller
            this.exception = new SynchronousQueue<>();
            // create the buffer
            this.buffer = new LinkedBlockingQueue<>();
            // the buffer can hold three chunks
            this.bufferPlaces = new Semaphore(CHUNK_SIZE * 3);
            // create the socket
            this.socket = new Socket();
            this.endpoint = endpoint;
        }

        /**
         * Opens the connection to the remote side and retrieves the relation
         * attributes.
         *
         * @throws InterruptedException
         */
        private void openConnection() throws InterruptedException {
            try {
                // connect to the remote side
                this.socket.connect(this.endpoint);

                // send an open request to the server
                Operation.Open.sendTo(this.socket);

                // retrieve the list of attributes and put it into the buffer
                final ObjectInputStream source = new ObjectInputStream(
                        this.socket.getInputStream());
                this.bufferPlaces.acquire();
                this.buffer.add(new ObjectWrapper(source.readObject()));
            } catch (final Exception exc) {
                this.exception.put(exc);
            }
        }

        /**
         * Retrieves a chunk of tuples.
         *
         * @throws InterruptedException
         */
        private void receiveTuples() throws InterruptedException {
            // wait until a chunk is available in the buffer
            this.bufferPlaces.acquire(CHUNK_SIZE);
            try {
                // request the chunk of tuples
                Operation.MultipleNext.sendTo(this.socket);
                final ObjectOutputStream sink = new ObjectOutputStream(
                        this.socket.getOutputStream());
                sink.writeInt(CHUNK_SIZE);
                sink.flush();

                // retrieve all the tuples and put them into the buffer
                final ObjectInputStream source = new ObjectInputStream(
                        this.socket.getInputStream());
                for (int i = 0; i < CHUNK_SIZE; ++i) {
                    this.buffer.put(new ObjectWrapper(source.readObject()));
                }
            } catch (IOException exc) {
                this.exception.put(exc);
            } catch (ClassNotFoundException exc) {
                this.exception.put(exc);
            }
        }

        /**
         * Closes the remote relation and the connection.
         *
         * @throws InterruptedException
         */
        private void closeConnection() throws InterruptedException {
            try {
                // close the remote relation and the socket
                Operation.Close.sendTo(this.socket);
                this.socket.close();
            } catch (IOException exc) {
                this.exception.put(exc);
            }
        }

        /**
         * Opens the connection, retrieves tuples and puts them into the buffer
         * until interrupted and eventually closes the connection and the
         * relation.
         */
        @Override
        public void run() {
            try {
                // open the connection and receive tuples until interrupted
                this.openConnection();
                while (true) {
                    this.receiveTuples();
                    if (interrupted()) {
                        throw new InterruptedException();
                    }
                }
            } catch (InterruptedException exc) {
                try {
                    // close the connection when interrupted
                    this.closeConnection();
                } catch (InterruptedException e) {
                    // ignore interruptions while closing
                }
            }
        }

        /**
         * Closes the connection and stops the thread.
         *
         * @throws Exception
         *             if closing failed
         */
        public void close() throws Exception {
            // interrupt the thread to abort any blocking operation like waiting
            // for free place in the buffer
            this.interrupt();
            // wait until the thread shuts down the connection and terminates
            this.join();
            // retrieve any exception still unthrown from the thread
            final Exception exc = this.exception.poll();
            if (exc != null) {
                throw exc;
            }
        }

        /**
         * Takes a single object from the buffer of received objects.
         *
         * @return the received object, which can be null
         * @throws Exception
         *             if receiving of an object failed
         */
        public Object takeObject() throws Exception {
            final Exception exc = this.exception.poll();
            if (exc != null) {
                throw exc;
            }
            final Object obj = this.buffer.take().object;
            this.bufferPlaces.release();
            return obj;
        }
    }

    /**
     * The endpoint to receive the tuples from.
     */
    private final SocketAddress endpoint;

    /**
     * The thread to receive tuples in background.
     */
    private ReceiverThread receiver = null;

    /**
     * Creates a new receive operation for the specified endpoint.
     *
     * @param endpoint
     *            the endpoint to connect to
     */
    public Receive(final SocketAddress endpoint) {
        this.endpoint = endpoint;
    }

    /**
     * Whether this receiver is closed.
     *
     * @return true, if the receiver is closed, false otherwise
     */
    public boolean isClosed() {
        return this.receiver == null;
    }

    /**
     * Closes this relation.
     *
     * Closes the relation on the remote side and the socket.
     *
     * @throws Exception
     *             if closing failed
     */
    @Override
    public void close() throws Exception {
        if (!this.isClosed()) {
            this.receiver.close();
        }
        this.receiver = null;
    }

    /**
     * Opens the connection to the remote relation.
     *
     * Starts a background thread to receive tuples in advance.
     *
     * @return the next tuple, or null, if there are no further tuples
     * @throws Exception
     *             if opening the relation failed
     */
    @Override
    public String[] open() throws Exception {
        // create and start the thread to receive tuples in background
        this.receiver = new ReceiverThread(this.endpoint);
        this.receiver.start();
        // take the retrieved relation names from the receive buffer and return
        // them
        return (String[]) this.receiver.takeObject();
    }

    /**
     * Receives the next tuple from the remote relation.
     *
     * This merely takes the tuple from the internal receive buffer, and thus
     * returns very quickly.
     *
     * @return the next tuple, or null, if there are no further tuples
     * @throws Exception
     *             if receiving of the next tuple failed
     */
    @Override
    public Object[] next() throws Exception {
        if (this.isClosed()) {
            throw new IOException("Relation closed");
        }

        return (Object[]) this.receiver.takeObject();
    }

}
