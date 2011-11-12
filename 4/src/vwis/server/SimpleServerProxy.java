package vwis.server;

import java.io.EOFException;
import java.io.IOException;

import vwis.common.Tablescan;

// Aufgabenblatt 2: ServerProxy
public class SimpleServerProxy extends ServerProxy {

    private static final long serialVersionUID = 1L;
    String tablename;
    protected transient Tablescan tablescan;

    public static enum Command {
        OPEN, CLOSE, NEXT;
    }

    public SimpleServerProxy(String tableName) {
        this.tablename = tableName;

    }

    @Override
    public void doExecute() throws IOException, Exception {

        this.tablescan = new Tablescan(this.tablename);
        try {
            while (true) {

                Command i = (Command) this.in.readObject();

                this.handleCommand(i);

            }
        } catch (EOFException e) {
            // System.out.println("Connection to " + socket.getInetAddress() +
            // " closed.");
        }
    }

    protected void handleCommand(Command i) throws Exception {
        switch (i) {
        case OPEN:
            this.out.writeObject(this.tablescan.open());
            break;
        case CLOSE:
            this.tablescan.close();
            break;
        case NEXT:
            this.out.writeObject(this.tablescan.next());
            break;
        }

    }

}
