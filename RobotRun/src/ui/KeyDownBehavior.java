package ui;

import controlP5.ControlBehavior;
import controlP5.Controller;

/**
 * Handles the key down functionality of text-fields.
 * 
 * @author Joshua Hooker
 *
 */
public class KeyDownBehavior extends ControlBehavior {
	
	/**
	 * A convience reference for the controller associated with this behavior.
	 */
	private MyTextfield controller;
	
	/**
	 * The map of key codes to key states, which is updated by the PApplet.
	 */
	private final KeyCodeMap keyMap;
	
	/**
	 * The length of time between checking for key down events.
	 */
	private int interval;
	
	/**
	 * The time of the next key down event.
	 */
	private long nextEvent;
	
	/**
	 * Contructs a key down behavior object with the given KeyCodeMap reference.
	 * 
	 * @param keyMap
	 */
	public KeyDownBehavior(KeyCodeMap keyMap) {
		super();
		this.keyMap = keyMap;
		resetInterval();
		
		controller = (MyTextfield) this.getController();
	}
	
	@Override
	protected void init(Controller<?> c) {
		// Set text-field reference when the controller reference is set
		if (c instanceof MyTextfield) {
			controller = (MyTextfield)c;
		}
	}
	
	/**
	 * Set the time for the next event and update the interval.
	 */
	protected void nextInterval() {
		
		if (interval > 100) {
			interval /= 2;
		}
		
		
		
		nextEvent = System.currentTimeMillis() + interval;
	}
	
	/**
	 * Reset the interval.
	 */
	protected void resetInterval() {
		interval = 3000;
		nextEvent = System.currentTimeMillis() + interval;
		
	}
	
	/* TODO deal with double-typing issue */
	
	@Override
	public void update() {
		/**/
		if (controller != null && controller.isActive()) {
			
			/* When the controller is active, check every so often if a key is
			 * down and update the text-field as necessary */
			if (System.currentTimeMillis() >= nextEvent) {
				Character last = keyMap.lastKeyPressed();
				
				if (last != null) {
					controller.append(last.charValue());
				}
				
				nextInterval();
			}
			
		} else {
			resetInterval();
		}
		/**/
	}
}
