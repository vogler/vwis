
import java.util.Arrays;

public class Util {

   /**
     * Prints a relation to standard output.
     * 
     * @param relation the relattion to print
     * @throws Exception if access to the relation failed
     */
   public static void printRelation(final DBIterator relation)
            throws Exception {
        final String[] headers = relation.open();
        System.out.println(Arrays.toString(headers));
        Object[] tuple;
        while ((tuple = relation.next()) != null) {
            System.out.println(Arrays.toString(tuple));
        }
    }
}
