package vwis.client;

import vwis.common.NLJoin;
import vwis.common.Tablescan;

public class ClientMain {

    public static void main(String[] args) throws Exception {

        /** TODO: set up host and port (optional: file) */
        String host = "localhost";
        int port = 8080;
        String file = "./personen";

        Tablescan ts = new Tablescan(file);
        ClientProxy client = new SimpleClientProxy("laender", host, port);
        NLJoin join = new NLJoin(ts, client);

        // do join and print result
        String[] attr = join.open();
        printArray(attr);
        Object[] tuple;
        while ((tuple = join.next()) != null) {
            printArray(tuple);
        }

        join.close();

    }

    public static void printArray(Object[] a) {
        for (int i = 0; i < a.length; i++) {
            System.out.print(a[i] + "\t");
        }
        System.out.println();
    }

}
