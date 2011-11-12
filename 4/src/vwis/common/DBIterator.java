package vwis.common;

public interface DBIterator {

    String[] open() throws Exception;

    Object[] next() throws Exception;

    void close() throws Exception;
}