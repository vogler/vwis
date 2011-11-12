package vwis.client;

import java.io.IOException;
import java.net.SocketException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import vwis.common.DBIterator;
import vwis.server.Send;
import vwis.server.ServerProxy;
import vwis.server.SimpleServerProxy;
import vwis.server.SimpleServerProxy.Command;

public class Receive implements DBIterator {

    private String tableName;
    private int bufferSize = 2;
    private Receiver receiver;
    private int port;
    private String host;

    public Receive(String tableName, int bufferSize, String host, int port)
            throws IOException {
        this.tableName = tableName;
        if (bufferSize > 2) {
            this.bufferSize = bufferSize;
        }
        this.host = host;
        this.port = port;
    }

    @Override
    public String[] open() throws Exception {
        if (this.receiver != null) {
            // Close old buffer thread
            this.receiver.close();
        }
        this.receiver = new Receiver(this.host, this.port);
        return this.receiver.open();
    }

    @Override
    public Object[] next() throws Exception {
        return this.receiver.next();
    }

    @Override
    public void close() throws Exception {
        this.receiver.close();
    }

    private class Receiver extends ClientProxy implements Runnable {

        private final BlockingQueue<Object> buffer;
        private final int numToReceive = Receive.this.bufferSize / 2;
        private final Object sentinel = new Object();
        private Thread thread;
        private volatile boolean closed;

        public Receiver(String host, int port) throws IOException {
            super(host, port);
            this.buffer = new ArrayBlockingQueue<Object>(Receive.this.bufferSize);
        }

        @Override
        public void run() {
            while (true) {
                try {
                    if (this.closed) {
                        return;
                    }

                    // Ask server for <numToReceive> new elements
                    this.out.writeObject(Command.NEXT);
                    this.out.writeInt(this.numToReceive);
                    this.out.flush();

                    // Process response
                    for (int i = 0; i < this.numToReceive; i++) {
                        Object[] tuple = (Object[]) this.in.readObject();
                        if (tuple == null) {
                            // No more tuples available, add sentinel element
                            // and terminate thread.
                            this.buffer.put(this.sentinel);
                            return;
                        }
                        this.buffer.put(tuple);
                    }

                } catch (InterruptedException e) {
                    // The thread was closed while blocking at put.
                    return;
                } catch (SocketException e) {
                    // The thread was closed while blocking at readObject.
                    return;
                } catch (Exception e) {
                    e.printStackTrace();
                    return;
                }
            }
        }

        @Override
        public String[] open() throws Exception {
            this.openConnection();
            this.out.writeObject(SimpleServerProxy.Command.OPEN);
            this.out.flush();
            String[] result = (String[]) this.in.readObject();

            // Start thread
            this.thread = new Thread(this);
            this.thread.start();
            return result;
        }

        @Override
        public Object[] next() throws Exception {
            if (this.closed) {
                return null;
            }
            Object obj = this.buffer.take();
            if (obj == this.sentinel) {
                // No more elements.
                this.buffer.put(this.sentinel);
                return null;
            }
            return (Object[]) obj;
        }

        @Override
        public void close() throws Exception {
            this.closed = true;
            this.thread.interrupt(); // Interrupt thread in case it is blocking at
                                // buffer.put
            this.closeConnection();
        }

        @Override
        protected ServerProxy getServerCommand() {
            return new Send(Receive.this.tableName);
        }
    }

}
