import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class ClientThread extends Thread {
	
Socket socket;
Client client;
	
	public ClientThread(Socket s, Client c) {
		
		socket = s;
		client = c;
		
	}
	
	public void run() {
		
		//packet the client receives
		String packet = null;
		
		try {
			
			BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			while((packet = in.readLine()) != null) {
				
				client.receivePacket(packet);
				
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
}
