package vwis.server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;
import java.net.SocketException;

public abstract class ServerProxy implements Serializable {

    /**
	 *
	 */
    private static final long serialVersionUID = 1L;

    protected transient Socket socket;
    protected transient ObjectInputStream in;
    protected transient ObjectOutputStream out;

    public void execute(Socket connSocket, ObjectInputStream ois,
            ObjectOutputStream oos) {
        this.socket = connSocket;
        this.in = ois;
        this.out = oos;
        new T().start();
    }

    public abstract void doExecute() throws Exception;

    private class T extends Thread {
        @Override
        public void run() {
            try {
                ServerProxy.this.doExecute();
            } catch (SocketException e) {
                // System.out.println("Connection to " + socket.getInetAddress()
                // + " closed.");
            } catch (Exception e) {
                // System.err.println("Connection to " + socket.getInetAddress()
                // + " aborted due to following exception:");
                e.printStackTrace();
            } finally {
                try {
                    ServerProxy.this.socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
