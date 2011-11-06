package de.tum.in.vwis.group14.db.net;

import de.tum.in.vwis.group14.db.DBIterator;
import de.tum.in.vwis.group14.db.ListDBIterator;
import static de.tum.in.vwis.group14.db.IsClosed.isClosed;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.channels.ClosedByInterruptException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Arrays;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class TestClientServer {

    private final static String[] NAMES = new String[]{"id", "name"};
    private final static Object[][] TUPLES = new Object[][]{
        new Object[]{1, "foo"}, new Object[]{2, "bar"}
    };
    private ServerThread server;
    private ListDBIterator relation;
    private ClientProxy client;

    private static class ServerThread extends Thread {

        private Exception exception;
        private ServerSocketChannel server;
        private DBIterator relation;

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
                final ServerProxy proxy = new ServerProxy(
                        client.socket(), this.relation);
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
        this.relation = new ListDBIterator(
                NAMES, new ArrayList<>(Arrays.asList(TUPLES)));
        this.server = new ServerThread(this.relation);
        this.server.start();
        this.client = new ClientProxy(
                new InetSocketAddress(this.server.getPort()));
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
        final String[] names = this.client.open();
        assertArrayEquals(NAMES, names);
    }

    @Test
    public void testNext() throws IOException, ClassNotFoundException {
        this.client.open();
        assertArrayEquals(TUPLES[0], this.client.next());
        assertArrayEquals(TUPLES[1], this.client.next());
        assertNull(this.client.next());
    }

    @Test(expected = IOException.class)
    public void testNextBeforeOpen() throws IOException,
            ClassNotFoundException {
        this.client.next();
    }

    @Test
    public void testClose() throws IOException, ClassNotFoundException {
        this.client.open();
        assertNotNull(this.client.next());
        this.client.close();
        assertThat(this.client, isClosed());
    }
}
