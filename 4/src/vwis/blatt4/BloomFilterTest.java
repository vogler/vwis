package vwis.blatt4;

import vwis.client.Receive;
import vwis.common.NLJoin;
import vwis.common.Tablescan;
import vwis.common.Test;
import vwis.server.Server;

public class BloomFilterTest {

    public static void main(String[] args) throws Exception {
        // Start server in new thread
        new Thread() {
            @Override
            public void run() {
                try {
                    new Server().start(1338);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();

        // Dirty, wait until server is up
        Thread.sleep(500);

        // Bloom filter join and normal join
        String normal = Test.resultToString(new NLJoin(
                new Tablescan("laender"), new Receive("personen", 2,
                        "localhost", 1338)));
        String bloom = Test.resultToString(new BloomFilterJoin("laender",
                "personen", 3, 5, "localhost", 1338));

        // Print and compare results
        System.out.println("Normal:");
        System.out.println(normal);
        System.out.println("Bloom:");
        System.out.println(bloom);
        if (bloom.equals(normal)) {
            System.out.println("Results match.");
        } else {
            System.err.println("Results do not match!");
        }

        System.exit(0);// kill server
    }
}
