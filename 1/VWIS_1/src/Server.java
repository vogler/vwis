import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;


public class Server {

	public static final int PORT = 12345;
	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		ServerSocket ss = new ServerSocket(PORT);
		System.out.println("Server listening on port "+PORT);
		while(true){
			Socket s = ss.accept();
			System.out.println("Server accepted new client");
			BufferedReader br = new BufferedReader(new InputStreamReader(s.getInputStream()));
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));
			while(true){
				String str = br.readLine();
				System.out.println("Client sent: "+str);
				bw.write("Hallo "+str+"\n");
				bw.flush();
			}
		}
	}

}
