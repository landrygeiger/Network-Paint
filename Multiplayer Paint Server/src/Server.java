import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Server {
	
	//net info
	private static final int PORT = 4454;
	private static ServerSocket serverSocket;
	
	/*
	private static ArrayList<Socket> userSockets = new ArrayList<Socket>();
	private static ArrayList<String> users = new ArrayList<String>(); //clients connected to the server
	*/
	
	private static Map<String, ServerThread> userMap = new HashMap<String, ServerThread>();
	private static String ovalData = ""; //contains every oval on the canvas in string form (so it can be easily send to any connecting client)
	
	public static void main(String args[]) {
		
		try {
			serverSocket = new ServerSocket(PORT);
			System.out.println("Server started on port " + PORT);
			
			while(true) {
				
				Socket socket = serverSocket.accept(); //await client connection
				
				
				new ServerThread(socket).start();
				
			}
			
		} catch (IOException e) {
			
			System.out.println("Failed to bind port");
			
		}
			
	}
	
	//send packet to all connected clients
	public static void relayPacket(String packet) {
		
		for(ServerThread serverThread : userMap.values()) {
			
			serverThread.getPrintWriter().println(packet);
			
		}
		
	}
	
	//receive and parse incomming packets
	public static void receivePacket(String packet) {
		
		String[] parsedPacket = packet.split("!");
		String packetType = parsedPacket[0];
		
		if(packetType.equals("username")) {
			
			relayPacket(packet);
			
		} else if(packetType.equals("disconnect")) {
			
			userMap.remove(parsedPacket[1]);
			relayPacket(packet);
			
		} else if(packetType.equals("ovals")) {
			
			relayPacket(packet);
			ovalData += parsedPacket[2];
			
		}
		
	}
	
	public static Map<String, ServerThread> getUserMap() {
		
		return userMap;
		
	}
	
	public static String getOvalData() {
		
		return ovalData;
		
	}
	
}
