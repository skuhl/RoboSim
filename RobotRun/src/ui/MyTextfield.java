package ui;

import controlP5.ControlFont;
import controlP5.ControlP5;
import controlP5.ControlWindow.Pointer;
import controlP5.Textfield;
import processing.core.PGraphics;

/**
 * An extension of controlP5's Textfield class, which includes the correct
 * functionality for the delete key as well as controlEvents for entering
 * text in a text field.
 * 
 * @author Joshua Hooker
 */
public class MyTextfield extends Textfield {
	
	private PGraphics buffer;
	
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
	
	/**
	 * Append the given character to end of the textfield's input.
	 * 
	 * @param c	The character to append
	 */
	public void append(Character c) {
		setText( getText() + Character.toString(c) );
	}
	
	/**
	 * Deletes a character prior to the active string buffer index and
	 * decrements the string buffer index 
	 */
	public void backspace() {
		if (_myTextBuffer.length() > 0 && _myTextBufferIndex > 0) {
			_myTextBuffer.deleteCharAt( --_myTextBufferIndex );
		}
	}
	
	/**
	 * Move the text buffer cursor one character to the left.
	 */
	public void cursorLeft() {
		if (_myTextBufferIndex > 0) {
			_myTextBufferIndex = Math.max(0, _myTextBufferIndex - 1);
		}
	}
	
	/**
	 * Move the text buffer cursor one character to the right.
	 */
	public void cursorRight() {
		if (_myTextBufferIndex < _myTextBuffer.length()) {
			_myTextBufferIndex = Math.min(_myTextBuffer.length(),
					_myTextBufferIndex + 1);
		}
	}
	
	/**
	 * Deletes a character at the active string buffer index in the text
	 * buffer.
	 */
	public void delete() {
		if (_myTextBuffer.length() > 0) {
			
			if (_myTextBufferIndex < _myTextBuffer.length()) {
				_myTextBuffer.deleteCharAt( _myTextBufferIndex );
				
			} else if (_myTextBufferIndex > 0) {
				_myTextBuffer.deleteCharAt( --_myTextBufferIndex );
			}
		}
	}
	
	/**
	 * Calculates the draw width of a string based on the text-field's label
	 * and PGraphics buffer.
	 * 
	 * @return	The draw width of the given string
	 */
	private int getTextWidthFor(String text) {
		return ControlFont.getWidthFor(text, _myValueLabel, buffer);
	}
	
	@Override
	public void keyEvent(processing.event.KeyEvent e) {
		
		if (e.getKeyCode() == 147) {
			// Deletes a character in the text buffer
			if (isUserInteraction && isTexfieldActive && isActive &&
					e.getAction() == processing.event.KeyEvent.PRESS) {
				
				delete();
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
	protected void mousePressed() {
		super.mousePressed();
		
		if (isActive) {
			// Update text buffer index
			_myTextBufferIndex = mouseXToIdx();
		}
	}
	
	/**
	 * Converts the mouse's x position on the scene to an index of the
	 * text-fields text buffer.
	 * 
	 * @return	The text buffer index of the mouse's x position
	 */
	private int mouseXToIdx() {
		int TBIdx = 0;
		
		if (_myTextBuffer.length() > 0) {
			Pointer pt = getControlWindow().getPointer();
			float[] pos = getPosition();
			int mouseX = pt.getX() - (int)pos[0];
			String txt = passCheck( getText() );
			int idx = 0, prevWidth = 0;
			
			do {
				int width = getTextWidthFor( txt.substring(0, idx) );
				
				if (mouseX - prevWidth < width - mouseX) {
					--idx;
					break;
				}
					
				prevWidth = width;
				++idx;
				
			} while (idx < txt.length());
			
			TBIdx = Math.max(0, idx);
		}
		
		return TBIdx;
	}
	
	/**
	 * Check if the text is a password
	 * 
	 * @param label	The text to display on the text-field
	 * @return		The text (encoded in asterisks if it is a password)
	 */
	private String passCheck( String label ) {
		if ( !isPasswordMode ) {
			return label;
		}
		
		String newlabel = "";
		for ( int i = 0 ; i < label.length( ) ; i++ ) {
			newlabel += "*";
		}
		
		return newlabel;
	}
	
	@Override
	public MyTextfield setSize( int theWidth , int theHeight ) {
		super.setSize( theWidth , theHeight );
		buffer = cp5.papplet.createGraphics( getWidth( ) , getHeight( ) );
		return this;
	}
	
	@Override
	public Textfield setValue(float newValue) {
		// Broadcast control events for text field input
		super.setValue(newValue);
		broadcast( FLOAT );
		return this;
	}
}
