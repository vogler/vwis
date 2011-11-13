package vwis.blatt4;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import vwis.client.ClientProxy;
import vwis.common.DBIterator;
import vwis.common.Tablescan;
import vwis.server.ServerProxy;

public class BloomFilterJoin extends ClientProxy {

    /**
     * Local table name
     */
    private final String localTable;

    /**
     * Local relation
     */
    private final DBIterator localRelation;

    /**
     * Remote table name
     */
    private final String remoteTable;

    /**
     * Bloom filter bitmap size in bytes
     */
    private final int bitmapSize;

    /**
     * Number of hash functions to apply in bloom filters
     */
    private final int numHashfuncs;

    /**
     * Attribute names of the local relation
     */
    private List<String> localNames;

    /**
     * Attribute names of the remote relation
     */
    private List<String> remoteNames;

    /**
     * Attribute names to join the relations by
     */
    private Set<String> joinNames;

    /**
     * Names of the joined relation
     */
    private List<String> allNames;

    /**
     * Current local tuple to join with
     */
    private Object[] currentLocal;

    /**
     * Current remote tuple to join with
     */
    private Object[] currentRemote;

    /**
     * Creates a new bloom filter join
     *
     * @param localTable
     *            the local table name
     * @param remoteTable
     *            the remote table name
     * @param bitmapSize
     *            the bitmap size for bloom filters
     * @param numHashfuncs
     *            the number of hash functions for bloom filters
     * @param host
     *            the host to connect to
     * @param port
     *            the port to connect to
     * @throws Exception
     *             if anything fails
     */
    public BloomFilterJoin(String localTable, String remoteTable,
            int bitmapSize, int numHashfuncs, String host, int port)
            throws Exception {
        super(host, port);
        this.localTable = localTable;
        this.localRelation = new Tablescan(this.localTable);
        this.remoteTable = remoteTable;
        this.bitmapSize = bitmapSize;
        this.numHashfuncs = numHashfuncs;
        openConnection();
    }

    /**
     * Opens the relation.
     *
     * Retrieves the names of the remote relation, compute the join attributes
     * and update the bloom filters on the server side.
     *
     * @throws IOException
     *             if network connection fails
     * @throws Exception
     *             if retrieving the names of the local relation failed
     */
    @Override
    public String[] open() throws Exception {
        this.localNames = new ArrayList<>(Arrays.asList(localRelation.open()));
        Operation.Open.sendTo(this.socket);
        Operation.Names.sendTo(this.socket);
        this.remoteNames = new ArrayList<>(Arrays.asList(((String[]) this.in
                .readObject())));
        this.updateNames();
        this.updateServerSideFilter();
        this.currentLocal = this.localRelation.next();
        return this.allNames.toArray(new String[] {});
    }

    /**
     * Update the attribute names.
     *
     * Computes the join names and the names of the joined relation.
     */
    private void updateNames() {
        this.joinNames = new HashSet<>(this.localNames);
        this.joinNames.retainAll(this.remoteNames);
        this.allNames = new ArrayList<>(this.remoteNames);
        this.allNames.removeAll(this.localNames);
        this.allNames.addAll(0, this.localNames);
    }

    /**
     * Updates the bloom filters on the server side.
     *
     * Creates a new bloom filter for each attribute to join by and sends the
     * filter to the server.
     *
     * @throws IOException
     *             if network access failed
     * @throws Exception
     *             if retrieval of tuples from the local relation failed
     */
    private void updateServerSideFilter() throws Exception {
        // create a map storing bloom filters for all join attributes
        final Map<String, BloomFilter> filters = new HashMap<>();
        for (final String name : this.joinNames) {
            filters.put(name, new BloomFilter(this.bitmapSize,
                    this.numHashfuncs));
        }
        // update the bloom filters with the tuples from the local relation
        Object[] tuple;
        while ((tuple = this.localRelation.next()) != null) {
            for (final String name : this.joinNames) {
                final int index = this.localNames.indexOf(name);
                filters.get(name).addObject(tuple[index]);
            }
        }
        // reset the local relation iterator
        this.localRelation.open();
        // send the filters to the remote side
        for (final String name : this.joinNames) {
            Operation.Filter.sendTo(this.socket);
            this.out.writeObject(name);
            this.out.writeObject(filters.get(name));
            this.out.flush();
        }
    }

    /**
     * Receives the next tuple from the server side.
     *
     * @return the next tuple, or null if the server doesn't provide further
     *         tuples
     * @throws ClassNotFoundException
     *             if de-serialization failed
     * @throws IOException
     *             if network access failed
     */
    private Object[] receiveNextTuple() throws ClassNotFoundException,
            IOException {
        Operation.Next.sendTo(this.socket);
        return (Object[]) this.in.readObject();
    }

    /**
     * Retrieves the next tuple from the joined relation.
     *
     * @throws IOException
     *             if network access failed
     * @throws Exception
     *             if retrieval of tuples from the local relation failed
     */
    @Override
    public Object[] next() throws Exception {
        this.currentRemote = this.receiveNextTuple();
        while (this.currentLocal != null) {
            while (this.currentRemote != null) {
                final Object[] joined = this.join(this.currentLocal,
                        this.currentRemote);
                if (joined != null) {
                    return joined;
                }
                this.currentRemote = this.receiveNextTuple();
            }
            this.currentLocal = this.localRelation.next();
            Operation.Open.sendTo(this.socket);
            this.currentRemote = this.receiveNextTuple();
        }
        return null;
    }

    /**
     * Joins the given tuples.
     *
     * If the join attributes are inequal in the given tuples, null is returned.
     * Otherwise the tuples are joined.
     *
     * @param l
     *            the local tuple
     * @param r
     *            the remote tuple
     * @return the joined tuple, or null, if the tuples cannot be joined
     */
    private Object[] join(Object[] l, Object[] r) {
        final Object[] joined = new Object[this.allNames.size()];
        for (int i = 0; i < joined.length; ++i) {
            final String name = this.allNames.get(i);
            final int li = this.localNames.indexOf(name);
            final int ri = this.remoteNames.indexOf(name);
            if (li != -1 && ri != -1) {
                if (!l[li].equals(r[ri])) {
                    return null;
                }
            }
            if (li != -1) {
                joined[i] = l[li];
            }
            if (ri != -1) {
                joined[i] = r[ri];
            }
        }
        return joined;
    }

    /**
     * Closes the local and remote relations and the network connection.
     *
     * @throws IOException if network access failed
     * @throws Exception if closing the local relation failed
     */
    @Override
    public void close() throws Exception {
        this.localRelation.close();
        Operation.Close.sendTo(this.socket);
        this.closeConnection();
    }

    /**
     * Is called during openConnection() to provide the server side of this
     * command. The server proxy returned by this method is sent to the server
     * which starts its do execute method.
     *
     * If you add additional parameters to the BloomFilterServer's constructor,
     * you must add them here, too.
     *
     */
    @Override
    protected ServerProxy getServerCommand() {
        return new BloomFilterServer(remoteTable);
    }

}
