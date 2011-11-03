import java.util.Arrays;


public class TestClient {

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		Tablescan rel_a = new Tablescan("rel_a.txt");
		ClientProxy rel_b = new ClientProxy(null, 1337);
		
		NLJoin nljoin = new NLJoin(rel_a, rel_b);
		System.out.println("--- NLJoin Client-A Server-B ---");
		String[] headers_j = nljoin.open();
		System.out.println("Attributes: "+Arrays.toString(headers_j));
		Object[] join;
		while((join = nljoin.next()) != null){
			System.out.println(Arrays.toString(join));
		}
		
	}

}
