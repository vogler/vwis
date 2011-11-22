package vwis.blatt5;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

public class NRA {
	
	//Mietspiegel
	private DBIterator left;
	//Kindergarten
	private DBIterator right;
	private int numRows; // <n>

	public NRA(DBIterator left, DBIterator right, int numRows){
		this.left = left;
		this.right = right;
		this.numRows = numRows;
	}
	
	/**
	 * Executes NRA ranking on the two iterators left and right, yielding
	 * at most numRows rows.
	 * @return Resulting tuple array
	 * @throws Exception 
	 */
	public Object[][] execute() throws Exception{
		left.open();
		right.open();
		Object[] l, r;
		String ls, rs;
		int lv, rv;
		int threshold = 0;
		SortedSet<Row> m = new TreeSet<Row>();
		Map<String, Integer> lcache = new HashMap<String, Integer>();
		Map<String, Integer> rcache = new HashMap<String, Integer>();
		while((l = left.next()) != null && (r = right.next()) != null){
			ls = (String)l[0];
			rs = (String)r[0];
			lv = (Integer)l[1];
			rv = (Integer)r[1];
			threshold = lv + rv;
			lcache.put(ls, lv);
			rcache.put(rs, rv);
			if(rcache.containsKey(ls)){
				m.add(new Row(ls, lv+rcache.get(ls)));
			}
			if(lcache.containsKey(rs)){
				m.add(new Row(rs, rv+lcache.get(rs)));
			}
			if(ntop(m, threshold) >= numRows) break;
		}

		return toArray(m);
	}
	
	private Object[][] toArray(SortedSet<Row> m) {
		Object[][] o = new Object[numRows][2];
		int i = 0;
		for(Row r : m){
			o[i] = r.toArray();
			i++;
			if(i>=numRows) break;
		}
		return o;
	}

	private int ntop(SortedSet<Row> m, int threshold) {
		int i = 0;
		for(Row r : m){
			if(r.i <= threshold) i++;
			else break;
		}
		return i;
	}
	
	public static void main(String[] args) throws Exception {
		//TODO Execute NRA with Tablescans of Mietspiegel and Kindergarten and print result
		NRA t = new NRA(new Tablescan("mietspiegel"), new Tablescan("kindergarten"), 3);
		for(Object[] a : t.execute()){
			System.out.println(Arrays.toString(a));
		}
	}

	public class Row implements Comparable<Row> {
		String n;
		Integer i;
		
		public Row(String n, Integer i){
			this.n = n;
			this.i = i;
		}

		@Override
		public int compareTo(Row o) {
			if(n.equals(o.n)) return 0;
			return i.compareTo(o.i);
		}
		
		public Object[] toArray(){
			return new Object[] {n, i};
		}
	}
}
