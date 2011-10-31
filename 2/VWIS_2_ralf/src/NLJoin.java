import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class NLJoin extends DBIterator {

	private DBIterator left;
	private DBIterator right;
	private List<String> lleft;
	private List<String> lright;
	private List<String> intersection;
	private List<String> union;

	public NLJoin(DBIterator left, DBIterator right) throws Exception {
		this.left = left;
		this.right = right;
	}

	@Override
	String[] open() throws Exception {
		String[] aleft = left.open();
		String[] aright = right.open();
		lleft = Arrays.asList(aleft);
		lright = Arrays.asList(aright);
		intersection = new ArrayList<String>(lleft);
		intersection.retainAll(lright);
		// avoid sets to preserve ordering
		union = new ArrayList<String>(lleft);
		union.addAll(lright);
		// remove duplicate attributes
		for(String s : intersection){
			union.remove(s);
		}
		return merge(aleft, aright);
	}
	
	private <T> T[] merge(T[] a, T[] b){
		List<T> merge = new ArrayList<T>(Arrays.asList(a));
		merge.addAll(Arrays.asList(b));
		return merge.toArray(a);
	}

	@Override
	Object[] next() throws Exception {
//	  For each tuple r in R do
//		     For each tuple s in S do
//		        If r and s satisfy the join condition
//		           Then output the tuple <r,s>
		Object[] l, r;
		while((l = left.next()) != null){
			while((r = right.next()) != null){
				for(String col : intersection){
					if(l[lleft.indexOf(col)].equals(r[lright.indexOf(col)])){
						return merge(l, r);
					}
				}
			}
			right.open();
		}
		return null;
	}

	@Override
	void close() throws Exception {
		left.close();
		right.close();
	}
}
