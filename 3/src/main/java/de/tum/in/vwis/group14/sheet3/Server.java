package de.tum.in.vwis.group14.sheet3;

import java.io.IOException;
import java.net.InetSocketAddress;

/**
 * A test server.
 */
public class Server {

    public static void main(String[] args) throws IOException {
        final de.tum.in.vwis.group14.db.net.TablescanServer server = new de.tum.in.vwis.group14.db.net.TablescanServer(
                new InetSocketAddress("127.0.0.1", 5000), "rel_b.txt");
        server.serveForever();
    }
}
