package vwis.blatt4;

import java.io.IOException;

import vwis.client.ClientProxy;
import vwis.server.ServerProxy;

public class BloomFilterJoin extends ClientProxy {

    // Name of the local table
    private final String localTable;

    // Name of the remote table
    private final String remoteTable;

    // Size of the bitmap (in byte)
    private final int bitmapSize;

    // Number of hash functions to be applied
    private final int numHashfuncs;

    // Do not modify signature.
    public BloomFilterJoin(String localTable, String remoteTable,
            int bitmapSize, int numHashfuncs, String host, int port)
            throws IOException {
        super(host, port);
        this.localTable = localTable;
        this.remoteTable = remoteTable;
        this.bitmapSize = bitmapSize;
        this.numHashfuncs = numHashfuncs;
        openConnection();
    }

    @Override
    public String[] open() throws Exception {
        // TODO Implement
        return null;
    }

    @Override
    public Object[] next() throws Exception {
        // TODO Implement
        return null;
    }

    @Override
    public void close() throws Exception {
        // TODO Implement
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
