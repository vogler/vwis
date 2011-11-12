package vwis.blatt4;

import vwis.server.ServerProxy;

public class BloomFilterServer extends ServerProxy {

    private static final long serialVersionUID = 1L;
    private final String table;

    public BloomFilterServer(String remoteTable) {
        this.table = remoteTable;
    }

    @Override
    public void doExecute() throws Exception {
        // TODO Implement

        // This method is executed on the server after a client calls
        // openConnection()
        // The connection is already set up proporly when this method is
        // executed.
        // You can use the streams in and out
        // for your communication.

        // Example code for using the streams (can be removed):
        Object o = in.readObject();
        out.writeObject(o);

    }

}
