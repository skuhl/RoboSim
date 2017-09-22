package ui;

import controlP5.ControlBehavior;

/**
 * A method used to update the key code map used by the application. Only one
 * controller should have this behavior.
 * 
 * @author Joshua Hooker
 */
public class KeyDownMgmt extends ControlBehavior {
	
	/**
	 * A reference to the key code map used in the application.
	 */
	private final KeyCodeMap keyMap; 
	
	/**
	 * Creates a behavior with the given key code map reference.
	 * 
	 * @param map	The application's key code map
	 */
	public KeyDownMgmt(KeyCodeMap map) {
		keyMap = map;
	}
	
	@Override
	public void update() {
		// Update the key code map
		keyMap.update();
	}
}
