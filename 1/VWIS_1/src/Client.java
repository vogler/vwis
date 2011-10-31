import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.UnknownHostException;


public class Client {
	
	/**
	 * @param args
	 * @throws IOException 
	 * @throws UnknownHostException 
	 */
	public static void main(String[] args) throws UnknownHostException, IOException {
		// TODO Auto-generated method stub
		String host = null;
		Socket s = new Socket(host, Server.PORT);
		System.out.println("Client connecting to port "+Server.PORT);
		BufferedReader br = new BufferedReader(new InputStreamReader(s.getInputStream()));
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));
		BufferedReader cbr = new BufferedReader(new InputStreamReader(System.in));
		while(true){
			System.out.println("Please enter text");
			String str = cbr.readLine();
			bw.write(str+"\n");
			bw.flush();
			System.out.println("Sent to server: "+str);
			String response = br.readLine();
			System.out.println("Server responded: "+response);
		}
	}
}
