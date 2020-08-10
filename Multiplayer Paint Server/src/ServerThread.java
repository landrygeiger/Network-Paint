import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Map;

public class ServerThread extends Thread {
	
	Socket socket;
	String username;
	
	PrintWriter netOut;
	BufferedReader netIn;
	
	public ServerThread(Socket s) {
		
		socket = s;
		
	}
	
	//Thread method
	public void run() {
		
		//this will hold what packet the client sends to the server
		String packet = null;
		
		try {
			
			netIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			netOut = new PrintWriter(socket.getOutputStream(), true);
			
			Map<String, ServerThread> userMap = Server.getUserMap();

			//send all users to the client
			String usersPacket = "";
			for(String user : userMap.keySet()) {
				
				usersPacket += user + ","; 
				
			}
			
			netOut.println(usersPacket); //send packet to the client
			
			username = netIn.readLine(); //receive the client's name
			userMap.put(username, this);
			
			if(Server.getOvalData().length() > 0) netOut.println("ovals!server!" + Server.getOvalData()); //send all previously drawn ovals to the client
			
			System.out.println(username + " has connected to the server");
			Server.relayPacket("username!" + username);
			
			while((packet = netIn.readLine()) != null) {
				
				Server.receivePacket(packet);
				
			}
			
		} catch (IOException e) {
			
			System.out.println(username + " has disconnected from the server");
			Server.receivePacket("disconnect!" + username);
		}
		
		
		
	}
	
	public Socket getSocket() {
		
		return socket;
		
	}
	
	public PrintWriter getPrintWriter() {
		
		return netOut;
		
	}
	
	public BufferedReader getBufferedReader() {
		
		return netIn;
		
	}
	
}
