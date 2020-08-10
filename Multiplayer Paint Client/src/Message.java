import java.awt.Color;

import acm.graphics.GLabel;
import acm.graphics.GRect;

public class Message {
	
	String content;
	Color color;
	GLabel messageLabel;
	GRect messageBackground;
	
	int alpha = 255;
	
	int displayTime = 150;
	int elapsed = 0;
	
	Color clearGray = new Color(Color.gray.getRed(), Color.gray.getGreen(), Color.gray.getBlue(), 64);
	
	public Message(String content, Color color) {
		
		this.content = content;
		this.color = color;
		
		//instantiate the glabel that will represent the message
		messageLabel = new GLabel(this.content);
		messageLabel.setColor(this.color);
		
		//rectangle that will be the background for the message (5 pixel padding)
		messageBackground = new GRect(messageLabel.getX() - 5, messageLabel.getY() - messageLabel.getHeight() - 5, messageLabel.getWidth() + 10, messageLabel.getHeight() + 10);
		messageBackground.setFilled(true);
		messageBackground.setFillColor(clearGray);
		messageBackground.setColor(clearGray);
		
	}
	
	public void update() {
		
		//if the message has been around longer than the specified time, start fading it out.
		if(elapsed >= displayTime) {
			
			alpha -= 10;
			if(alpha <= 0) alpha = 0;
			color = new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
			clearGray = new Color(clearGray.getRed(), clearGray.getBlue(), clearGray.getBlue(), alpha / 4);
			messageLabel.setColor(color);
			messageBackground.setFillColor(clearGray);
			messageBackground.setColor(clearGray);
		}
		elapsed++;
		
	}
	
	public GLabel getGLabel() {
		
		return messageLabel;
		
	}
	
	public GRect getRect() {
		
		return messageBackground;
		
	}
	
}
