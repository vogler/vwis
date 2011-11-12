package vwis.common;

import java.io.IOException;

import vwis.client.Receive;
import vwis.server.Server;

public class Test extends Thread {

    public static void main(String[] args) throws IOException, Exception {
        new Test().start();
        printResult(new NLJoin(new Tablescan("laender"), new Receive(
                "personen", 2, "localhost", 1338)));
        printResult(new NLJoin(new Tablescan("laender"), new Receive(
                "personen", 2, "localhost", 1338)));
        System.exit(0); // Kills the server
    }

    @Override
    public void run() {
        try {
            new Server().start(1338);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void printResult(DBIterator it) throws Exception {
        System.out.println(resultToString(it));
    }

    public static String resultToString(DBIterator it) throws Exception {
        StringBuilder sb = new StringBuilder();
        Object[] attr = it.open();
        sb.append(arrayToString(attr));
        Object[] tuple;
        while ((tuple = it.next()) != null) {
            sb.append(arrayToString(tuple));
        }
        it.close();
        return sb.toString();
    }

    public static String arrayToString(Object[] a) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < a.length; i++) {
            sb.append(a[i] + "\t");
        }
        sb.append("\n");
        return sb.toString();
    }
}
