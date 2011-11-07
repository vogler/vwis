package de.tum.in.vwis.group14.db.net;

import static de.tum.in.vwis.group14.db.IsClosed.isClosed;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.nio.channels.ClosedByInterruptException;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import de.tum.in.vwis.group14.db.DBIterator;
import de.tum.in.vwis.group14.db.ListDBIterator;

public class TestClientServer {

    private static final int NUMBER_OF_TUPLES = 100;
    private static final String[] NAMES = new String[] { "id", "name" };
    private List<Object[]> tuples;
    private ServerThread server;
    private ListDBIterator relation;
    private ClientProxy client;

    private static class ServerThread extends Thread {

        private Exception exception;
        private final ServerSocketChannel server;
        private final DBIterator relation;

        public ServerThread(DBIterator relation) throws IOException {
            this.relation = relation;
            this.server = ServerSocketChannel.open();
            this.server.bind(new InetSocketAddress(0));
        }

        public int getPort() {
            return this.server.socket().getLocalPort();
        }

        public Exception getException() {
            return this.exception;
        }

        @Override
        public void run() {
            try {
                final SocketChannel client = this.server.accept();
                final ServerProxy proxy = new ServerProxy(new Send(
                        client.socket(), this.relation));
                proxy.serveForever();
                this.relation.close();
                return;
            } catch (ClosedByInterruptException ex) {
                return;
            } catch (Exception ex) {
                this.exception = ex;
                return;
            }
        }
    }

    public TestClientServer() {
    }

    @Before
    public void setUp() throws IOException {
        this.tuples = new ArrayList<>();
        final Random rng = new Random();
        for (int i = 0; i < NUMBER_OF_TUPLES; ++i) {
            final Object[] tuple = new Object[] { i,
                    new BigInteger(32, rng).toString() };
            this.tuples.add(tuple);
        }
        this.relation = new ListDBIterator(NAMES, this.tuples);
        this.server = new ServerThread(this.relation);
        this.server.start();
        this.client = new ClientProxy(new Receive(new InetSocketAddress(
                this.server.getPort())));
    }

    @After
    public void tearDown() throws Exception {
        this.client.close();
        this.server.interrupt();
        this.server.join();
        if (this.server.getException() != null) {
            throw this.server.getException();
        }
    }

    @Test
    public void testOpen() throws IOException, ClassNotFoundException {
        assertTrue(this.client.isClosed());
        final String[] names = this.client.open();
        assertArrayEquals(NAMES, names);
        assertFalse(this.client.isClosed());
    }

    @Test
    public void testNext() throws IOException, ClassNotFoundException {
        this.client.open();
        for (final Object[] tuple : this.tuples) {
            assertArrayEquals(tuple, this.client.next());
        }
        assertNull(this.client.next());
    }

    @Test(expected = IOException.class)
    public void testNextBeforeOpen() throws IOException, ClassNotFoundException {
        this.client.next();
    }

    @Test
    public void testClose() throws IOException, ClassNotFoundException {
        this.client.open();
        assertFalse(this.client.isClosed());
        assertNotNull(this.client.next());
        this.client.close();
        assertThat(this.client, isClosed());
        assertTrue(this.client.isClosed());
    }
}
