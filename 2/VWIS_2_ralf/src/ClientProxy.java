import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.net.UnknownHostException;


public class ClientProxy extends DBIterator {

	private ObjectInputStream ois;
	private DataOutputStream dos;
	private Socket s;

	public ClientProxy(String server, int port) throws UnknownHostException, IOException {
		s = new Socket(server, port);
		ois = new ObjectInputStream(s.getInputStream());
		dos = new DataOutputStream(s.getOutputStream());
	}

	@Override
	String[] open() throws Exception {
		dos.writeInt(1);
		return (String[]) ois.readObject();
	}

	@Override
	Object[] next() throws Exception {
		dos.writeInt(2);
		return (Object[]) ois.readObject();
	}

	@Override
	void close() throws Exception {
		dos.writeInt(3);
		s.close();
	}

}
