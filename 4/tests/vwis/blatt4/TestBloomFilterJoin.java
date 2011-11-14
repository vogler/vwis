package vwis.blatt4;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import vwis.server.ServerProxy;

public class TestBloomFilterJoin {

    class ServerThread extends Thread {
        private final ServerSocket server;
        private Exception exception = null;

        public ServerThread() throws IOException {
            this.server = new ServerSocket(0);
        }

        public int getPort() {
            return this.server.getLocalPort();
        }

        @Override
        public void run() {
            try {
                final Socket client = this.server.accept();
                final ObjectInputStream source = new ObjectInputStream(
                        client.getInputStream());
                final ObjectOutputStream sink = new ObjectOutputStream(
                        client.getOutputStream());
                final ServerProxy command = (ServerProxy) source.readObject();
                command.execute(client, source, sink);
            } catch (IOException e) {
                this.exception = e;
            } catch (ClassNotFoundException e) {
                this.exception = e;
            }
        }

        public void close() throws Exception {
            this.interrupt();
            this.join();
            this.server.close();
            if (this.exception != null) {
                throw this.exception;
            }
        }
    }

    @Rule
    public TemporaryFolder testFolder = new TemporaryFolder();

    private ServerThread server;
    private String laenderRelation;
    private String personenRelation;
    private static Object[][] JOIN_TUPLES = new Object[][] {
            new Object[] { 1, " D ", 357111.91, 1, "Hans", "Haus", 5.0f },
            new Object[] { 1, " D ", 357111.91, 5, "Karl", "Hainz", 1.0f },
            new Object[] { 3, "USA", 9629091.0, 3, "Peter", "Jackson", 2.0f },
            new Object[] { 3, "USA", 9629091.0, 6, "Ridley", "Scott", 2.7f },
            new Object[] { 1, "CH ", 41285.0, 4, "Toni", "Anton", 1.7f }, };
    private static String[] JOIN_NAMES = new String[] { "land_id", "land",
            "flaeche", "pers_id", "vorname", "name", "note" };
    private BloomFilterJoin join;

    private Path copyResource(final String name) throws IOException {
        final URL resource = this.getClass().getResource("/" + name);
        final Path dest = FileSystems.getDefault().getPath(
                this.testFolder.getRoot().getPath(), name);
        try (final InputStream source = resource.openStream()) {
            Files.copy(source, dest);
        }
        return dest;
    }

    @Before
    public void setUp() throws Exception {
        this.laenderRelation = copyResource("laender").toString();
        this.personenRelation = copyResource("personen").toString();
        this.server = new ServerThread();
        this.server.start();
        this.join = new BloomFilterJoin(this.laenderRelation,
                this.personenRelation, 3, 5, "localhost", this.server.getPort());
    }

    @After
    public void tearDown() throws Exception {
        this.join.close();
        this.server.close();
        this.server = null;
    }

    @Test
    public void testOpen() throws Exception {
        final String names[] = this.join.open();
        assertArrayEquals(JOIN_NAMES, names);
    }

    @Test
    public void testJoin() throws Exception {
        this.join.open();
        final List<Object[]> tuples = new ArrayList<>();
        Object[] tuple;
        while ((tuple = this.join.next()) != null) {
            tuples.add(tuple);
        }
        assertEquals(JOIN_TUPLES.length, tuples.size());
        for (int i = 0; i < JOIN_TUPLES.length; ++i) {
            assertArrayEquals(JOIN_TUPLES[i], tuples.get(i));
        }

    }
}
