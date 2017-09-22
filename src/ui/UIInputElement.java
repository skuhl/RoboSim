package ui;

/**
 * Defines the type relationship between input UI elements. This is used to
 * determine how to clear certain UI elements when transitioning between
 * windows.
 * 
 * @author Joshua Hooker
 */
public interface UIInputElement {
	
	/**
	 * Resets the UI element to its state before the user modified the element.
	 */
	public abstract void clearInput();
	
	/**
	 * Returns the input type of the UI element.
	 * 
	 * @return	The UI element's input type
	 */
	public abstract int getInputType();
}
