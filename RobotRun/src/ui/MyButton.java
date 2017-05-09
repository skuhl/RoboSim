package ui;

import controlP5.Button;
import controlP5.ControlP5;
import controlP5.ControllerGroup;
import global.Fields;

/**
 * An extension to the ControlP5 Button class that allows drag clicks.
 * 
 * @author Joshua Hooker
 */
public class MyButton extends Button {
	
	public MyButton(ControlP5 theControlP5, String theName) {
		super(theControlP5, theName);
	}

	protected MyButton(ControlP5 theControlP5, ControllerGroup<?> theParent,
			String theName, float theDefaultValue, int theX, int theY, int
			theWidth, int theHeight) {
		
		super(theControlP5, theParent, theName, theDefaultValue, theX, theY, 
				theWidth, theHeight);
	}
	
	@Override
	protected void onEndDrag() {
		// Allow drag clicks
		onClick();
	}
}
