import java.io.DataInputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerProxy {

	public ServerProxy(final DBIterator it, int port) throws IOException {
		ServerSocket socket = new ServerSocket(port);
		System.out.println("Server listening on port "+port);
		while (true) {
			final Socket client = socket.accept();
			System.out.println("Client: " + client.getLocalAddress() + ":"
					+ client.getLocalPort());
			new Thread() {
				public void run() {
					try {
						Socket c = client;
						DataInputStream dis = new DataInputStream(
								c.getInputStream());
						ObjectOutputStream oos = new ObjectOutputStream(
								c.getOutputStream());

						boolean run = true;
						while (run) {
							int i = dis.readInt();
							if (i == 1) {
								oos.writeObject(it.open());
							} else if (i == 2) {
								oos.writeObject(it.next());
							} else if (i == 3) {
								it.close();
								run = false;
							}
						}
					} catch (IOException e) {
						System.out.println("Communication failed: " + e);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}.start();
		}
	}
}
