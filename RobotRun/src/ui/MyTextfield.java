package ui;

import controlP5.ControlP5;
import controlP5.ControllerGroup;
import controlP5.Textfield;
import processing.event.KeyEvent;

/**
 * My version of the Textfield, that will hopefully fix the delete key function ...
 * 
 * @author Joshua Hooker
 */
public class MyTextfield extends Textfield {
	
	public MyTextfield(ControlP5 theControlP5, String theName) {
		super( theControlP5, theControlP5.getDefaultTab(), theName, "", 0, 0,
				199, 19);
	}
	
	public MyTextfield(ControlP5 theControlP5, String theName, int theX,
			int theY, int theWidth, int theHeight) {
		
		super( theControlP5, theControlP5.getDefaultTab(), theName, "", theX,
				theY, theWidth, theHeight);
		
		theControlP5.register(theControlP5.papplet, theName, this);
	}
	
	@Override
	public void keyEvent(KeyEvent e) {
		
		// TODO Remap delete key
		if (e.getKeyCode() == 147) {
			/* System.out.printf("Remap: %s\n", this.keyMapping.get(e.getKeyCode())); */
			KeyEvent copy = new KeyEvent(e.getNative(), e.getMillis(), e.getAction(), e.getModifiers(), e.getKey(), DELETE);
			super.keyEvent(copy);
			
		} else {
			super.keyEvent(e);
		}
	}
	
}
