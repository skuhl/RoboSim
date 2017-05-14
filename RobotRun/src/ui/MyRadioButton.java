package ui;

import controlP5.ControlP5;
import controlP5.RadioButton;
import controlP5.Toggle;

/**
 * An extension of controlP5's RadioButton class, which has some necessary
 * methods for accessing fields in a RadioButton, which can be set by the
 * user.
 * 
 * @author Joshua Hooker
 */
public class MyRadioButton extends RadioButton {
	
	public MyRadioButton(ControlP5 controller, String name) {
		super(controller, name);
	}
	
	/**
	 * Returns the total height of the radio button set based off the number of
	 * toggles, toggles per row, the height of a toggle and row spacing.
	 * 
	 * @return	The total height compromised all toggles and spacing in a complete
	 * 			column of toggles of the radio button
	 */
	public float getTotalHeight() {
		return (_myRadioToggles.size() / itemsPerRow) * (_myHeight + spacingRow) - spacingRow;
	}
	
	/**
	 * Returns the total width of the set of toggles in the this radio button
	 * based off the number of toggles, the toggles per row, the width of a
	 * toggle, and column spacing.
	 * 
	 * @return	The total width of all toggles and spacing in a single row of
	 * 			the radio button
	 */
	public float getTotalWidth() {
		return Math.min(_myRadioToggles.size(), itemsPerRow) * (_myWidth + spacingColumn) - spacingColumn;
	}
	
	/* Why don't you have these methods contolP5!?!?!? */
	
	/**
	 * @return	The amount of space between toggle elements in the same row
	 */
	public int getColumnSpacing() {
		return spacingColumn;
	}
	
	/**
	 * @return	The amount of space between toggle elements in the same column
	 */
	public int getRowSpacing() {
		return spacingRow;
	}
	
	/**
	 * @return	The defined number of toggle elements to render in one row of
	 * 			the radio button set
	 */
	public int getItemsPerRow() {
		
		(new Toggle(null, "name")).onClick(null);
		
		return itemsPerRow;
	}
	
	/* My rant ends here */
}
