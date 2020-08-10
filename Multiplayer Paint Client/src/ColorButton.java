import java.awt.Color;

import acm.graphics.*;

public class ColorButton {
	
	private Color color;
	private GRect rect;
	
	ColorButton(Color c, GRect r) {
		
		color = c;
		rect = r;
		
		rect.setColor(c);
		rect.setFillColor(c);
		rect.setFilled(true);
		
	}
	
	Color getColor() {
		
		return color;
		
	}
	
	GRect getRect() {
		
		return rect;
		
	}
	
}
