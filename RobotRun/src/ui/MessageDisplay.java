package ui;

import global.Fields;
import processing.core.PConstants;
import processing.core.PGraphics;

/**
 * Defines the functionality for rendering event-driven messages. For
 * simplicity's sake, only one message can be rendered by a single message
 * display.
 * 
 * @author Joshua Hooker
 */
public class MessageDisplay {
	
	private String msg;
	
	public MessageDisplay() {
		msg = null;
	}
	
	public int draw(PGraphics g, int lastPosX, int lastPosY) {
		
		if (msg != null) {
			g.pushStyle();
			g.textAlign(PConstants.RIGHT);
			g.fill( Fields.color(0, 115, 115) );
			g.text(msg, lastPosX, lastPosY);
			g.popStyle();
			
			return lastPosY + 20;
		}
		
		return lastPosY;
	}
	
	public String getMessage() {
		return msg;
	}
	
	public void resetMessage() {
		/* TEST CODE *
		System.err.println("MSG RESET");
		/**/
		msg = null;
	}
	
	public void setMessage(String newMsg) {
		/* TEST CODE *
		System.err.println(newMsg);
		/**/
		msg = newMsg;
	}
	
	@Override
	public String toString() {
		return (msg == null) ? "" : msg;
	}
}
