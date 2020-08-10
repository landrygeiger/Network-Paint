import acm.program.*;
import acm.graphics.*;
import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.awt.Color;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.io.*;

public class Client extends GraphicsProgram {
	
	/*
	 * Packets:
	 * * username - string containing a 'username'. also used a user connected packet
	 * * ovals - packet containing a list of ovals to be sent/received
	 */
	
	//network variables
	private Socket socket;
	private static String IP;
	private static final int PORT = 4454;
	private boolean netStatus; //is the client connected to a server or not
	
	//network I/O
	private BufferedReader netIn;
	private PrintWriter netOut;
	
	/*
	private ArrayList<String> users = new ArrayList<String>(); //names of all connected users
	private ArrayList<GLabel> visualUserList = new ArrayList<GLabel>();
	*/
	
	private Map<String, GLabel> userMap = new HashMap<String, GLabel>();
	
	private static String username;
	
	private ArrayList<GOval> toSend = new ArrayList<GOval>(); //all of the ovals that need to be sent to the server
	private ArrayList<Message> displayMessages = new ArrayList<Message>(); //messages being drawn on the screen 
	
	//application dimensions
	public static final int APPLICATION_WIDTH = 1000;
	public static final int APPLICATION_HEIGHT = 924;
	private static final int TICK_RATE = 1000/30; //ms between sending and receiving ovals
	
	//canvas dimensions
	private static final int CANVAS_WIDTH = 800;
	private static final int CANVAS_HEIGHT = 900;
	
	//user selections
	private static int selectedDiam = 40;
	private static Color selectedColor = Color.black;
	
	GRect canvasCover;
	
	ColorButton[] colorButtons = new ColorButton[8];
	DiameterButton[] diameterButtons = new DiameterButton[4];
	
	//colors to loop through when creating the color buttons
	Color[] colors = {
			
			Color.red,
			Color.orange,
			Color.yellow,
			Color.green,
			Color.cyan,
			Color.magenta,
			Color.black,
			Color.white
				
	};
	
	
	/*
	 * On application start 
	 */
	
	public void init() {
		
		addMouseListeners(); //ready the program to listen for mouse input
		
		setSize(APPLICATION_WIDTH, APPLICATION_HEIGHT); //set application size 
		
		//create a rectangle outlining the canvas
		GRect canvasOutline = new GRect(0, 0, CANVAS_WIDTH, CANVAS_HEIGHT);
		add(canvasOutline);
		
		//this rectangle is drawn in front of all the circles to cover the dots that go out of the canvas
		canvasCover = new GRect(CANVAS_WIDTH, 0, APPLICATION_WIDTH - CANVAS_WIDTH, CANVAS_HEIGHT);
		canvasCover.setFilled(true);
		canvasCover.setFillColor(Color.white);
		
		add(canvasCover);
		
		/*
		 * create buttons
		 */
		
		//create color buttons
		for(int j = 0; j < 4; j++) {
			
			for(int i = 0; i < 2; i++) {
				
				GRect buttonRect = new GRect(CANVAS_WIDTH + i * 100, j * 100, 100, 100);
				add(buttonRect);
				
				colorButtons[j * 2 + i] = new ColorButton(colors[j * 2 + i], buttonRect);
				
			}
			
		}
		
		//create diameter buttons
		for(int j = 0; j < 2; j++) {
			
			for(int i = 0; i < 2; i++) {
				
				int n = j * 2 + i; //represents which iteration the loop is on
				int diam = 80 - n * 20; //diameter as a function of n (starts at 80 and decreases by 20)
				
				
				GOval buttonOval = new GOval(800 + 100 * i + 50 - diam / 2, 400 + 100 * j + 50 - diam / 2, diam, diam);
				add(buttonOval);
				
				diameterButtons[n] = new DiameterButton(diam, buttonOval);
				
			}
			
		}
		
		//divide colors and diameters
		GLine colorDiv = new GLine(CANVAS_WIDTH, 400, APPLICATION_WIDTH, 400);
		add(colorDiv);
		
		//divide diameters and player list
		GLine diamDiv = new GLine(CANVAS_WIDTH, 600, APPLICATION_WIDTH, 600);
		add(diamDiv);
		
		GLine canvasDiv = new GLine(CANVAS_WIDTH, 0, CANVAS_WIDTH, 400);
		add(canvasDiv);
		
		
		//attempt to make a connection to the server
		try {
			
			socket = new Socket(IP, PORT);
			
			netIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			netOut = new PrintWriter(socket.getOutputStream(), true);
			
			//add self to users list
			GLabel userDisplay = new GLabel(username);
			userDisplay.setLocation(CANVAS_WIDTH + 20, 620);
			add(userDisplay);
			
			userMap.put(username, userDisplay);
			
			//wait for the server to send a list of all connected users
			String[] connected = netIn.readLine().split(",");
			for(String user : connected) {
				
				if(!user.equals("")) {
					addUser(user);
				}
				
			}
			
			netOut.println(username);
			
			//start listening for packets from the server
		    new ClientThread(socket, this).start();
		    
			System.out.println("Succesfully connected to " + IP);
			
			netStatus = true;
			
		} catch (IOException e) {
			
			System.out.println("Failed to connect to server...");
			netStatus = false;
			
		}
		
	}
	
	/*
	 * Mouse Listeners
	 */
	
	@Override
	public void mousePressed(MouseEvent e) {
		
		drawCircle(e.getX(), e.getY());
		
		//detect button clicks
		for(ColorButton button : colorButtons) {
			
			if(button.getRect().contains(new GPoint(e.getX(), e.getY()))) {
					
				selectedColor = button.getColor();	
					
			}
			
		}
		
		for(DiameterButton button : diameterButtons) {
			
			if(button.getOval().contains(new GPoint(e.getX(), e.getY()))) {
				
				selectedDiam = button.getDiameter();
				
			}
			
		}
		
	}
	
	@Override
	public void mouseDragged(MouseEvent e) {
		
		drawCircle(e.getX(), e.getY());
		
	}
	
	//method to draw a circle where the mouse is
	private void drawCircle(int x, int y) {
		
		if(x < CANVAS_WIDTH && y < CANVAS_HEIGHT) {
			
			GOval circle = new GOval(x - selectedDiam / 2, y - selectedDiam / 2, selectedDiam, selectedDiam);
			circle.setFillColor(selectedColor);
			circle.setColor(selectedColor);
			circle.setFilled(true);
			add(circle);
			for(int i = 0; i < 16 + userMap.size() + displayMessages.size() * 2; i++) { circle.sendBackward(); }
		
			toSend.add(circle);
		}
		
	}
	
	private void addUser(String user) {
		
		GLabel userDisplay = new GLabel(user);
		userDisplay.setLocation(CANVAS_WIDTH + 20, 620 + 20 * userMap.size());
		add(userDisplay);
		
		userMap.put(user, userDisplay);
		
	}
	
	private void refreshUserList() {
		
		int i = 0;
		for(GLabel userLabel : userMap.values()) {
			
			userLabel.setLocation(CANVAS_WIDTH + 20, 620 + i * 20);
			i++;
			
		}
		
	}
	
	public void receivePacket(String packet) {
		
		String[] parsedPacket = packet.split("!");
		String packetType = parsedPacket[0];
		
		if(packetType.equals("username") && !parsedPacket[1].equals(username)) {
			
			addUser(parsedPacket[1]);
			
			Message message = new Message(parsedPacket[1] + " has joined the server", Color.green);
			displayMessages.add(message);
			add(message.getRect());
			add(message.getGLabel());
			repositionMessages();
			
		} else if(packetType.equals("disconnect")) {
			
			remove(userMap.get(parsedPacket[1]));
			userMap.remove(parsedPacket[1]);
			refreshUserList();
			
			Message message = new Message(parsedPacket[1] + " has left the server", Color.red);
			displayMessages.add(message);
			add(message.getRect());
			add(message.getGLabel());
			repositionMessages();
			
		} else if(packetType.equals("ovals")) {
			
			//if the packet didn't come from this client
			if(!parsedPacket[1].equals(username)) {
				
				//split each oval in the packet up
				String[] ovals = parsedPacket[2].split(";");
				for(String oval : ovals) {
					
					if(!oval.equals("")) {
						
						//split up each attribute of the oval
						String[] ovalAttr = oval.split(",");
						
						//parse int from each string attribute
						int x = Integer.parseInt(ovalAttr[0]);
						int y = Integer.parseInt(ovalAttr[1]);
						int diam = Integer.parseInt(ovalAttr[2]);
						int red = Integer.parseInt(ovalAttr[3]);
						int green = Integer.parseInt(ovalAttr[4]);
						int blue = Integer.parseInt(ovalAttr[5]);
						
						//create an object with the parsed attributes
						GOval ovalObject = new GOval(x, y, diam, diam);
						ovalObject.setFilled(true);
						Color ovalColor = new Color(red, green, blue);
						ovalObject.setFillColor(ovalColor);
						ovalObject.setColor(ovalColor);
						
						add(ovalObject);
						
						for(int i = 0; i < 15 + userMap.size(); i++) { ovalObject.sendBackward(); }
						
					}
					
				}
				
			}
			
		}
		
	}
	
	//send newly drawn ovals to the server
	private void sendOvals() {
		
		String ovalPacket = "ovals!" + username + "!";
		ArrayList<GOval> toSendCopy = new ArrayList<GOval>(toSend);
		toSend.clear();
		for(GOval oval : toSendCopy) {
			
			//x, y, diameter, red, green, blue
			ovalPacket += ((int) oval.getX()) + "," + ((int) oval.getY()) + "," + ((int) oval.getWidth()) + "," + oval.getColor().getRed() + "," + oval.getColor().getGreen() + "," + oval.getColor().getBlue() + ";";
			
		}
		
		netOut.println(ovalPacket); //send oval packet
		
	}
	
	public static int getTickRate() {
		
		return TICK_RATE;
		
	}
	
	//reposition the messages on the screen after one has been added or removed (messages move up or down like a list)
	public void repositionMessages() {
		
		int i = 0;
		for(Message message : displayMessages) {
			
			GLabel messageLabel = message.getGLabel();
			messageLabel.setLocation(30, CANVAS_HEIGHT - 100 - 40 * i);
			message.getRect().setLocation(messageLabel.getX() - 5, messageLabel.getY() - messageLabel.getHeight() - 5);
			
			i++;
			
		}
		
	}
	
	public void run() {
		
		while(true) {
			
			if(toSend.size() > 0) sendOvals(); //if there are ovals to send, send them to the server
			
			if(displayMessages.size() > 0) {
				
				ArrayList<Integer> toRemove = new ArrayList<Integer>();
				
				for(Message message : displayMessages) {
					
					message.update();
					if(message.getGLabel().getColor().getAlpha() <= 0) {
						
						remove(message.getGLabel());
						remove(message.getRect());
						toRemove.add(displayMessages.indexOf(message));
						
					}
					
				}
				
				for(int message : toRemove) {
					
					displayMessages.remove(message);
					
				}
				
				repositionMessages();
				
			}
			
			
			pause(TICK_RATE);
		
		}
		
	}
	
	public static void main(String args[]) {
		
		username = args[0];
		IP = args[1];
		(new Client()).start(args);
		
	}
	
}
