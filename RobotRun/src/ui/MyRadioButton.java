package ui;

import controlP5.ControlP5;
import controlP5.RadioButton;
import controlP5.Toggle;
import global.Fields;

/**
 * An extension of controlP5's RadioButton class, which has some necessary
 * methods for accessing fields in a RadioButton, which can be set by the
 * user.
 * 
 * @author Joshua Hooker
 */
public class MyRadioButton extends RadioButton implements UIInputElement {
	
	private int inputType;
	
	public MyRadioButton(ControlP5 controller, String name,
			boolean isMultipleChoice, int inputType) {
		
		super(controller, name);
		
		this.isMultipleChoice = isMultipleChoice;
		this.inputType = inputType;
	}
	
	@Override
	public void clearInput() {
		activate(0);
	}
	
	/**
	 * @return	The amount of space between toggle elements in the same row
	 */
	public int getColumnSpacing() {
		return spacingColumn;
	}
	
	@Override
	public int getInputType() {
		return inputType;
	}
	
	/* Why don't you have these methods contolP5!?!?!? */
	
	/**
	 * @return	The defined number of toggle elements to render in one row of
	 * 			the radio button set
	 */
	public int getItemsPerRow() {
		return itemsPerRow;
	}

	/**
	 * @return	The amount of space between toggle elements in the same column
	 */
	public int getRowSpacing() {
		return spacingRow;
	}
	
	/**
	 * Returns the total height of the radio button set based off the number of
	 * toggles, toggles per row, the height of a toggle and row spacing.
	 * 
	 * @return	The total height compromised all toggles and spacing in a complete
	 * 			column of toggles of the radio button
	 */
	public int getTotalHeight() {
		return (_myRadioToggles.size() / itemsPerRow) * (itemHeight + spacingRow) - spacingRow;
	}
	
	/**
	 * Returns the total width of the set of toggles in the this radio button
	 * based off the number of toggles, the toggles per row, the width of a
	 * toggle, and column spacing.
	 * 
	 * @return	The total width of all toggles and spacing in a single row of
	 * 			the radio button
	 */
	public int getTotalWidth() {
		return Math.min(_myRadioToggles.size(), itemsPerRow) * (itemWidth + spacingColumn) - spacingColumn;
	}
	
	/* My rant ends here */
	
	/**
	 * Sets the column spacing of this radio button. The maximum width among
	 * all toggle labels is added to the given spacing value, in order to avoid
	 * toggle overlap.
	 * 
	 * @param spacing	The spacing to place between the columns of radio
	 * 					buttons
	 * @return			A reference to this
	 */
	public MyRadioButton setSpacingColumnOffset(int spacing) {
		int maxWidth = 0;
		// Find the maximum width among all toggle labels
		for (Toggle t : _myRadioToggles) {
			int width = t.getLabel().length() * Fields.CHAR_WDTH;
			
			if (width > maxWidth) {
				maxWidth = width;
			}
		}
		
		setSpacingColumn(spacing + maxWidth);
		
		return this;
	}
}
