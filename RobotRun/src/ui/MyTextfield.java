package ui;

import controlP5.ControlFont;
import controlP5.ControlP5;
import controlP5.ControlWindow;
import controlP5.ControlWindow.Pointer;
import controlP5.Textfield;
import global.Fields;
import processing.core.PApplet;
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
	private int dragIdxBegin, dragIdxEnd;
	
	public MyTextfield(ControlP5 theControlP5, String theName) {
		super( theControlP5, theControlP5.getDefaultTab(), theName, "", 0, 0,
				199, 19);
		
		dragIdxBegin = -1;
		dragIdxEnd = -1;
	}
	
	public MyTextfield(ControlP5 theControlP5, String theName, int theX,
			int theY, int theWidth, int theHeight) {
		
		super( theControlP5, theControlP5.getDefaultTab(), theName, "", theX,
				theY, theWidth, theHeight);
		
		theControlP5.register(theControlP5.papplet, theName, this);
		dragIdxBegin = -1;
		dragIdxEnd = -1;
	}

	@Override
	public void draw( PGraphics theGraphics ) {

		theGraphics.pushStyle( );
		theGraphics.fill( color.getBackground( ) );
		theGraphics.pushMatrix( );
		theGraphics.translate( x( position ) , y( position ) );
		theGraphics.rect( 0 , 0 , getWidth( ) , getHeight( ) );
		theGraphics.noStroke( );

		theGraphics.fill( _myColorCursor );
		theGraphics.pushMatrix( );
		theGraphics.pushStyle( );

		buffer.beginDraw( );
		buffer.background( 0 , 0 );
		final String text = passCheck( getText( ) );
		final int textWidth = ControlFont.getWidthFor( text.substring( 0 , _myTextBufferIndex ) , _myValueLabel , buffer );
		final int dif = PApplet.max( textWidth - _myValueLabel.getWidth( ) , 0 );
		final int _myTextBufferIndexPosition = ControlFont.getWidthFor( text.substring( 0 , _myTextBufferIndex ) , _myValueLabel , buffer );
		_myValueLabel.setText( text );
		_myValueLabel.draw( buffer , -dif , 0 , this );
		buffer.noStroke( );
		if ( isTexfieldActive ) {
			if ( !cp5.papplet.keyPressed ) {
				buffer.fill( _myColorCursor , PApplet.abs( PApplet.sin( cp5.papplet.frameCount * 0.05f )) * 255 );
			} else {
				buffer.fill( _myColorCursor );
			}
			buffer.rect( PApplet.max( 1 , PApplet.min( _myTextBufferIndexPosition , _myValueLabel.getWidth( ) - 3 ) ) , 0 , 1 , getHeight( ) );
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
	
	/**
	 * TODO
	 * 
	 * @return
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
				
				if (_myTextBuffer.length() > 0) {
					
					if (_myTextBufferIndex < _myTextBuffer.length()) {
						_myTextBuffer.deleteCharAt( _myTextBufferIndex );
						
					} else if (_myTextBufferIndex > 0) {
						_myTextBuffer.deleteCharAt( --_myTextBufferIndex );
					}
				}
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
	 * TODO
	 * 
	 * @return
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
	
	@Override
	protected void onEndDrag() {
		dragIdxEnd = mouseXToIdx();
		System.out.printf("End: %d\n", dragIdxEnd);
	}
	
	@Override
	protected void onStartDrag() {
		dragIdxBegin = mouseXToIdx();
		System.out.printf("Start: %d\n", dragIdxBegin);
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
