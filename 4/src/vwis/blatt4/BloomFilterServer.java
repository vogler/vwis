package vwis.blatt4;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import vwis.common.DBIterator;
import vwis.common.Tablescan;
import vwis.server.ServerProxy;

/**
 * Bloom filter server.
 */
public class BloomFilterServer extends ServerProxy {

    /**
     * Serialization ID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Table name.
     */
    private final String table;

    /**
     * Relation to serve
     */
    private transient DBIterator relation;

    /**
     * Attribute names of the relation
     */
    private transient List<String> names;

    /**
     * Bloom filters for the relation
     */
    private transient Map<String, BloomFilter> filters;

    /**
     * Creates a new server which serves the given relation
     *
     * @param remoteTable
     *            the relation to serve
     */
    public BloomFilterServer(String remoteTable) {
        this.table = remoteTable;
    }

    /**
     * Serve the relation to the client.
     */
    @Override
    public void doExecute() throws Exception {
        this.relation = new Tablescan(this.table);
        this.filters = new HashMap<>();

        while (true) {
            // receive the operation request from the client
            final Operation op = Operation.receiveFrom(this.socket);
            switch (op) {
            case Open:
                // open the relation
                this.names = new ArrayList<>(
                        Arrays.asList(this.relation.open()));
                break;
            case Names:
                // send the attribute names to the client
                this.out.writeObject(this.names.toArray(new String[] {}));
                this.out.flush();
                break;
            case Filter:
                // receive a bloom filter for an attribute
                this.receiveFilter();
                break;
            case Next:
                // send the next matching tuple to the client
                Object[] tuple;
                do {
                    tuple = this.relation.next();
                } while (tuple != null && (!this.allFiltersMatch(tuple)));
                this.out.writeObject(tuple);
                this.out.flush();
                break;
            case Close:
                // close the relation and the network connection
                this.relation.close();
                this.socket.close();
                this.relation = null;
                this.names = null;
                this.filters = null;
                break;
            }
        }
    }

    /**
     * Receives a bloom filter for an attribute.
     *
     * @throws ClassNotFoundException
     *             if de-serialization failed
     * @throws IOException
     *             if the network connection failed
     */
    private void receiveFilter() throws ClassNotFoundException, IOException {
        // read attribute name and filter object and put them in the filter map
        final String name = (String) this.in.readObject();
        final BloomFilter filter = (BloomFilter) this.in.readObject();
        this.filters.put(name, filter);
    }

    /**
     * Checks the given tuple against the received bloom filters.
     *
     * Each attribute in the given tuple for which a bloom filter was installed
     * is checked against the bloom filter. If all attribute values match their
     * corresponding filters, true is returned.
     *
     * @param tuple
     *            the tuple to match
     * @return true, if all bloom filters match the tuple, false otherwise
     * @throws IOException
     *             if hashing failed
     */
    private boolean allFiltersMatch(Object[] tuple) throws IOException {
        for (int i = 0; i < this.names.size(); ++i) {
            final String name = this.names.get(i);
            final BloomFilter filter = this.filters.get(name);
            if (filter != null && (!filter.matches(tuple[i]))) {
                return false;
            }
        }
        return true;
    }

}
