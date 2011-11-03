package de.tum.in.vwis.group14.sheet3;


import de.tum.in.vwis.group14.db.NLJoin;
import de.tum.in.vwis.group14.db.Tablescan;


public class Test {

    public static void main(String[] args) throws Exception {
        final Tablescan A = new Tablescan("rel_a.txt");
        final Tablescan B = new Tablescan("rel_b.txt");

        try (final Tablescan r = A) {
            System.out.println("--- Relation A ---");
            Util.printRelation(r);
        }

        System.out.println();

        try (final Tablescan r = B) {
            System.out.println("--- Relation B ---");
            Util.printRelation(r);
        }

        System.out.println();

        try (final NLJoin joined = new NLJoin(A, B)) {
            System.out.println("--- A |><| B ---");
            Util.printRelation(joined);
        }

    }
}
