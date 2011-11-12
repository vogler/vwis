package vwis.common;

public class Main {

    public static void main(String[] args) throws Exception {

        /** TODO: (optional: set up file path) */
        Tablescan t1 = new Tablescan("./personen");
        Tablescan t2 = new Tablescan("./laender");
        NLJoin j = new NLJoin(t1, t2);

        String[] attr;
        Object[] tuple;

        // scan and print first table
        System.out.println("Tablescan: table 1");
        attr = t1.open();
        printArray(attr);
        while ((tuple = t1.next()) != null) {
            printArray(tuple);
        }
        t1.close();

        // scan and print second table
        System.out.println("\nTablescan: table 2");
        attr = t2.open();
        printArray(attr);
        while ((tuple = t2.next()) != null) {
            printArray(tuple);
        }
        t2.close();

        // do and print local join
        System.out.println("\nlocal NLJoin");
        attr = j.open();
        printArray(attr);
        while ((tuple = j.next()) != null) {
            printArray(tuple);
        }
        j.close();

    }

    public static void printArray(Object[] a) {
        for (int i = 0; i < a.length; i++) {
            System.out.print(a[i] + "\t");
        }
        System.out.println();
    }

}
