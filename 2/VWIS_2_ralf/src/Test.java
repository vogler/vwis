import java.util.Arrays;


public class Test {

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		Tablescan rel_a = new Tablescan("rel_a.txt");
		Tablescan rel_b = new Tablescan("rel_b.txt");
		
		System.out.println("--- Relation A ---");
		String[] headers_a = rel_a.open();
		System.out.println("Attributes: "+Arrays.toString(headers_a));
		Object[] left;
		while((left = rel_a.next()) != null){
			System.out.println(Arrays.toString(left));
		}
		
		System.out.println();
		
		System.out.println("--- Relation B ---");
		String[] headers_b = rel_b.open();
		System.out.println("Attributes: "+Arrays.toString(headers_b));
		Object[] right;
		while((right = rel_b.next()) != null){
			System.out.println(Arrays.toString(right));
		}
		
		System.out.println();
		
		NLJoin nljoin = new NLJoin(rel_a, rel_b);
		System.out.println("--- NLJoin A B ---");
		String[] headers_j = nljoin.open();
		System.out.println("Attributes: "+Arrays.toString(headers_j));
		Object[] join;
		while((join = nljoin.next()) != null){
			System.out.println(Arrays.toString(join));
		}
		
	}

}
