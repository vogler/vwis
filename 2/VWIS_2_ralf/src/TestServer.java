

public class TestServer {

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		Tablescan rel_b = new Tablescan("rel_b.txt");
		new ServerProxy(rel_b, 1337);
	}

}
