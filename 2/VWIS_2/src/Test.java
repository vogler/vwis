
import java.util.Arrays;

public class Test {
    
    private static void printRelation(final DBIterator relation) 
            throws Exception {
        final String[] headers = relation.open();
        System.out.println(Arrays.toString(headers));
        Object[] tuple;
        while ((tuple = relation.next()) != null) {
            System.out.println(Arrays.toString(tuple));
        }
    }

    public static void main(String[] args) throws Exception {
        final Tablescan A = new Tablescan("rel_a.txt");
        final Tablescan B = new Tablescan("rel_b.txt");
        
        try (final Tablescan r = A) {
            System.out.println("--- Relation A ---");
            printRelation(r);
        }
        
        System.out.println();
        
        try (final Tablescan r = B) {
            System.out.println("--- Relation B ---");
            printRelation(r);
        }
             
        System.out.println();
        
        try (final NLJoin joined = new NLJoin(A, B)) {
            System.out.println("--- A ⋈ B ---");
            printRelation(joined);
        }
        
    }
}
