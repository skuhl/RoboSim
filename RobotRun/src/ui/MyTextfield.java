package ui;

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
			/* TODO find out if the KeyEvent is pressed or released *
			if ( !e.isAutoRepeat() && _myTextBuffer.length() > 0
					&& _myTextBufferIndex < _myTextBuffer.length() && _myTextBufferIndex > 0 ) {
				
				_myTextBuffer.deleteCharAt( _myTextBufferIndex );
				
				if ( _myTextBufferIndex > 0 && _myTextBuffer.length() <= _myTextBufferIndex) {
					_myTextBufferIndex = _myTextBuffer.length() - 1;
				}
			}
			/**/
			
		} else {
			super.keyEvent(e);
		}
	}	
}
