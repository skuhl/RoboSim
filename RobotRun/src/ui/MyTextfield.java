package ui;

import com.sun.glass.events.KeyEvent;

import controlP5.ControlP5;
import controlP5.Textfield;

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
	public void keyEvent(processing.event.KeyEvent e) {
		
		if (e.getKeyCode() == 147) {
			
			if (isUserInteraction && isTexfieldActive && isActive &&
					e.getAction() == processing.event.KeyEvent.PRESS) {
					
				/* TODO fix edge cases *
				int rmAtIdx = _myTextBufferIndex;
				
				if (rmAtIdx > 0 && _myTextBufferIndex < _myTextBuffer.length()) {
					--rmAtIdx;
				}
				
				if (_myTextBuffer.length() > 0 && rmAtIdx < _myTextBuffer.length()
						&& rmAtIdx > 0) {
					
					_myTextBuffer.deleteCharAt( rmAtIdx );
					
					if ( _myTextBufferIndex > 0) {
						--_myTextBufferIndex;
						
					} else if (_myTextBufferIndex >= _myTextBuffer.length()) {
						_myTextBufferIndex = _myTextBuffer.length() - 1;
					}
				}
				/**/
			}	
			
		} else {
			super.keyEvent(e);
		}
		
		if (isUserInteraction && isTexfieldActive && isActive &&
				e.getAction() == processing.event.KeyEvent.PRESS) {
			// Set value every time a key is pressed
			setValue(e.getKeyCode());
		}
	}
	
	@Override
	public Textfield setValue(float newValue) {
		// Broadcast control events for text field input
		super.setValue(newValue);
		broadcast( FLOAT );
		return this;
	}
}
