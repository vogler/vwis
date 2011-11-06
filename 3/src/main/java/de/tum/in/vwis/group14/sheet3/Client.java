package de.tum.in.vwis.group14.sheet3;

import java.net.InetSocketAddress;

import de.tum.in.vwis.group14.db.NLJoin;
import de.tum.in.vwis.group14.db.Tablescan;
import de.tum.in.vwis.group14.db.net.ClientProxy;

/**
 * A test client.
 */
public class Client {

    public static void main(String[] args) throws Exception {
        Tablescan A = new Tablescan("rel_a.txt");
        ClientProxy B = new ClientProxy(
                new InetSocketAddress("127.0.0.1", 5000));
        try (NLJoin joined = new NLJoin(A, B)) {
            System.out.println("--- A |><| B (remote) ---");
            Util.printRelation(joined);
        }
    }

}
