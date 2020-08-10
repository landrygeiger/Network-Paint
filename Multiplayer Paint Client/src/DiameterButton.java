import java.awt.Color;

import acm.graphics.*;

public class DiameterButton {
	
	private int diam;
	private GOval oval;
	
	DiameterButton(int d, GOval o) {
		
		diam = d;
		oval = o;
		
		oval.setFillColor(Color.black);
		oval.setFilled(true);
		
	}
	
	int getDiameter() {
		
		return diam;
		
	}
	
	GOval getOval() {
		
		return oval;
		
	}
	
}
