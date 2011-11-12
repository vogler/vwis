package vwis.client;

import java.io.IOException;

import vwis.server.ServerProxy;
import vwis.server.SimpleServerProxy;

// Aufgabenblatt 2: ClientProxy
public class SimpleClientProxy extends ClientProxy {

    private final String tableName;

    SimpleClientProxy(String tableName, String host, int port)
            throws IOException {
        super(host, port);
        this.tableName = tableName;
        this.openConnection();
    }

    @Override
    public void close() throws Exception {
        this.out.writeObject(SimpleServerProxy.Command.CLOSE);
        this.out.flush();
        this.closeConnection();
    }

    @Override
    public Object[] next() throws Exception {
        this.out.writeObject(SimpleServerProxy.Command.NEXT);
        this.out.flush();
        Object[] result = (Object[]) this.in.readObject();
        return result;
    }

    @Override
    public String[] open() throws Exception {
        this.out.writeObject(SimpleServerProxy.Command.OPEN);
        this.out.flush();
        String[] result = (String[]) this.in.readObject();
        return result;
    }

    @Override
    protected ServerProxy getServerCommand() {
        return new SimpleServerProxy(this.tableName);
    }

}
