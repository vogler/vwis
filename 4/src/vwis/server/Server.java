package vwis.server;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {

    public void start(int port) throws Exception {

        System.out.println("Server: running");

        ServerSocket socket = new ServerSocket(port);

        while (true) {
            Socket connSocket = socket.accept();
            ObjectInputStream ois = new ObjectInputStream(
                    connSocket.getInputStream());
            ObjectOutputStream oos = new ObjectOutputStream(
                    connSocket.getOutputStream());

            // The first object received from a client must always be a server
            // proxy
            // which will be used for the communication with that client.
            ServerProxy cmd = (ServerProxy) ois.readObject();

            // Execute the transferred proxy.
            cmd.execute(connSocket, ois, oos);

            // System.out.println("Server received command: " + cmd);

        }

    }
}
