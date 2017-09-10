package ui;

import com.sun.glass.events.KeyEvent;

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
public class MyTextfield extends Textfield implements UIInputElement {
	
	private PGraphics buffer;
	private int textBufferRenderStart;
	private int inputType;
	private int selectionBegin;
	private int selectionEnd;
	
	public MyTextfield(ControlP5 theControlP5, String theName, int inputType) {
		super(theControlP5, theName);
		textBufferRenderStart = 0;
		this.inputType = inputType;
		selectionBegin = -1;
		selectionEnd = -1;
	}
	
	/**
	 * Deletes a character prior to the active string buffer index and
	 * decrements the string buffer index 
	 */
	public void backspace() {
		if (_myTextBuffer.length() > 0 && _myTextBufferIndex > 0) {
			cursorLeft();
			_myTextBuffer.deleteCharAt(_myTextBufferIndex);
		}
	}
	
	@Override
	public void clearInput() {
		_myTextBuffer.delete(0, _myTextBuffer.length());
		_myTextBufferIndex = 0;
		textBufferRenderStart = 0;
		clearSelection();
	}
	
	/**
	 * Move the text buffer cursor one character to the left.
	 */
	public void cursorLeft() {
		if (_myTextBufferIndex > 0) {
			_myTextBufferIndex = Math.max(0, _myTextBufferIndex - 1);
			
			if (_myTextBufferIndex < textBufferRenderStart) {
				// Update the render start index
				textBufferRenderStart = _myTextBufferIndex;
			}
		}

		clearSelection();
	}
	
	/**
	 * Move the text buffer cursor one character to the right.
	 */
	public void cursorRight() {
		if (_myTextBufferIndex < _myTextBuffer.length()) {
			_myTextBufferIndex = Math.min(_myTextBuffer.length(),
					_myTextBufferIndex + 1);
			
			String text = passCheck(getText());
			int widthDiff = getTextWidthFor(text.substring(
					textBufferRenderStart, _myTextBufferIndex));
			
			if (getWidth() <= widthDiff) {
				++textBufferRenderStart;
			}
		}
		
		clearSelection();
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
				_myTextBuffer.deleteCharAt(_myTextBufferIndex);
				cursorLeft();
			}
		}
	}
	
	@Override
	public void draw(PGraphics theGraphics) {
		theGraphics.pushStyle();
		theGraphics.fill(color.getBackground());
		theGraphics.pushMatrix();
		theGraphics.translate(x(position) , y(position));
		theGraphics.rect(0, 0, getWidth(), getHeight());
		theGraphics.noStroke();

		theGraphics.fill(_myColorCursor);
		theGraphics.pushMatrix();
		theGraphics.pushStyle();

		buffer.beginDraw();
		buffer.background(0, 0);
		final String text = passCheck(getText());
		final int dif = getTextWidthFor(text.substring(0, textBufferRenderStart));
		final int _myTextBufferIndexPosition = getTextWidthFor(text.substring(
				textBufferRenderStart, _myTextBufferIndex));
		_myValueLabel.setText(text);
		_myValueLabel.draw(buffer, -dif, 0, this);
		buffer.noStroke();
		
		if (selectionBegin != -1 && selectionEnd != -1) {
			buffer.pushStyle();
			// TODO tweek color
			buffer.fill(55, 55, 255, 90);
			// Draw highlighting over the text
			int rectXBegin = getTextWidthFor(text.substring(
					textBufferRenderStart, selectionBegin));
			int rectXEnd = getTextWidthFor(text.substring(
					textBufferRenderStart, selectionEnd));
			
			if (selectionBegin < selectionEnd) {
				buffer.rect(rectXBegin, 0, rectXEnd - rectXBegin, getHeight());
				
			} else {
				buffer.rect(rectXEnd, 0, rectXBegin - rectXEnd, getHeight());
			}
			
			buffer.popStyle();
		}
		
		
		if ( isTexfieldActive ) {
			if ( !cp5.papplet.keyPressed ) {
				buffer.fill( _myColorCursor , (float)Math.abs( Math.sin( cp5.papplet.frameCount * 0.05f )) * 255 );
			} else {
				buffer.fill( _myColorCursor );
			}
			buffer.rect( Math.max( 1 , Math.min( _myTextBufferIndexPosition , _myValueLabel.getWidth( ) - 3 ) ) , 0 , 1 , getHeight( ) );
		}
		buffer.endDraw( );
		theGraphics.image( buffer , 0 , 0 );

		theGraphics.popStyle( );
		theGraphics.popMatrix( );

		theGraphics.fill( isTexfieldActive ? color.getActive( ) : color.getForeground( ) );
		theGraphics.rect( 0 , 0 , getWidth( ) , 1 );
		theGraphics.rect( 0 , getHeight( ) - 1 , getWidth( ) , 1 );
		theGraphics.rect( -1 , 0 , 1 , getHeight( ) );
		theGraphics.rect( getWidth( ) , 0 , 1 , getHeight( ) );
		_myCaptionLabel.draw( theGraphics , 0 , 0 , this );
		theGraphics.popMatrix( );
		theGraphics.popStyle( );
	}
	
	@Override
	public int getInputType() {
		return inputType;
	}
	
	/**
	 * TODO comment this
	 * 
	 * @param c
	 */
	public void insert(Character c) {
		_myTextBuffer.insert(_myTextBufferIndex, c.charValue());
		cursorRight();
	}
	
	@Override
	public void keyEvent(processing.event.KeyEvent e) {
		if (isUserInteraction && isTexfieldActive && isActive &&
				e.getAction() == processing.event.KeyEvent.PRESS) {
			
			if (e.getKeyCode() == 147) {
				// Deletes a character in the text buffer
				if (selectionBegin != -1 && selectionEnd != -1) {
					// Remove highlighted segment
					removeSelectedSegment();
					
				} else {
					delete();
				}
				
			} else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
				// TODO jump to the end of the textfield
				
			} else if (e.getKeyCode() == KeyEvent.VK_BACKSPACE) {
				
				if (selectionBegin != -1 && selectionEnd != -1) {
					// Remove highlighted segment
					removeSelectedSegment();
					
				} else {
					backspace();
				}
				
			} else if (e.getKeyCode() == KeyEvent.VK_ENTER) {
				// Trigger a control event when enter is pressed
				setValue(e.getKeyCode());
				
			} else if (e.getKeyCode() == KeyEvent.VK_LEFT) {
				cursorLeft();
				
			} else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
				cursorRight();
				
			} else if (e.getKeyCode() == KeyEvent.VK_UP) {
				_myTextBufferIndex = 0;
				textBufferRenderStart = 0;
				
			} else if (e.getKeyCode() >= 32 && e.getKeyCode() <= 126) {
				removeSelectedSegment();
				insert(e.getKey());
			}
		}
	}
	
	@Override
	public MyTextfield setSize( int theWidth , int theHeight ) {
		super.setSize( theWidth , theHeight );
		buffer = cp5.papplet.createGraphics( getWidth( ) , getHeight( ) );
		// Why do I need this you ask? Because Processing is strange.
		buffer.beginDraw();
		buffer.endDraw();
		return this;
	}
	
	@Override
	public Textfield setValue(float newValue) {
		// Broadcast control events for text field input
		super.setValue(newValue);
		broadcast( FLOAT );
		return this;
	}
	
	@Override
	protected void mousePressed() {
		super.mousePressed();
		
		if (isActive) {
			try {
				// Update text buffer index
				_myTextBufferIndex = mouseXToIdx();
				clearSelection();
				
			} catch (NullPointerException NPEx) {
				/* An issue occurs with mapping the mouse click to a text
				 * buffer index */
				NPEx.printStackTrace();
			}
		}
	}
	
	@Override
	protected void mouseReleasedOutside() {	
		if (!isKeepFocus) {
			clearSelection();
		}
		
		super.mouseReleasedOutside();
	}
	
	@Override
	protected void onDrag() {
		// Update the selection bounds while the mouse is dragged
		if (selectionBegin == -1) {
			int newIdx = mouseXToIdx();
			
			if (newIdx >= 0 && newIdx <= _myTextBuffer.length()) {
				selectionBegin = newIdx;
				_myTextBufferIndex = selectionEnd;
			}
			
		}
		
		if (selectionBegin != -1) {
			int newIdx = mouseXToIdx();
			
			if (newIdx >= 0 && newIdx <= _myTextBuffer.length()) {
				selectionEnd = newIdx;
				_myTextBufferIndex = selectionEnd;
			}
		}
	}
	
	/**
	 * Resets the selection bounds for the textfield highlighting.
	 */
	private void clearSelection() {
		selectionBegin = -1;
		selectionEnd = -1;
	}
	
	/**
	 * Get the mouse's xy position relative to the position of this textfield.
	 * 
	 * @return	The mouse's position in the form of an array: [x, y]
	 */
	private int[] getMousePos() {
		Pointer pt = getControlWindow().getPointer();
		float[] pos = getPosition();
		
		return new int [] {
				pt.getX() - (int)pos[0],
				pt.getY() - (int)pos[1],
		};
	}
	
	/**
	 * Calculates the draw width of a string based on the text-field's label
	 * and PGraphics buffer.
	 * 
	 * @param text
	 * @return		The draw width of the given string
	 */
	private int getTextWidthFor(String text) {
		return ControlFont.getWidthFor(text, _myValueLabel, buffer);
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
			int[] mousePos = getMousePos();
			String txt = passCheck( getText() );
			int idx = textBufferRenderStart + 1;
			int prevWidth = 0;
			
			while (idx < txt.length()) {
				int width = getTextWidthFor(txt.substring(
						textBufferRenderStart, idx));
				
				if (mousePos[0] - prevWidth < width - mousePos[0]) {
					--idx;
					break;
				}
				
				prevWidth = width;
				++idx;
			}
			
			if (idx == txt.length()) {
				int width = getTextWidthFor(txt.substring(
						textBufferRenderStart, idx));
				
				if (mousePos[0] - prevWidth < width - mousePos[0]) {
					--idx;
				}
			}
			
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
	
	/**
	 * Removes all the characters between the begin and end selection indices
	 * from the text buffer and updates the cursor and render indices if
	 * necessary.
	 */
	private void removeSelectedSegment() {
		if (selectionBegin != -1 && selectionEnd != -1) {
			int lowerBound, upperBound;
			
			if (selectionBegin <= selectionEnd) {
				lowerBound = selectionBegin;
				upperBound = selectionEnd;
				
			} else {
				lowerBound = selectionEnd;
				upperBound = selectionBegin;
			}
			
			/* Update the cursor and render indices, if they fall within the
			 * range of removed characters */ 
			if (_myTextBufferIndex > lowerBound) {
				_myTextBufferIndex = Math.max(0, lowerBound);
				
				if (textBufferRenderStart > _myTextBufferIndex) {
					textBufferRenderStart = _myTextBufferIndex;
				}
			}
			
			_myTextBuffer.delete(lowerBound, upperBound);
			clearSelection();
		}
	}
}
