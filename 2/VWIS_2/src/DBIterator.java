
public abstract class DBIterator {
	
	abstract String[] open() throws Exception;

	abstract Object[] next() throws Exception;

	abstract void close() throws Exception;
}
