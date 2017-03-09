package ui;
import java.util.Map;

import controlP5.ControlP5;
import controlP5.ControllerGroup;
import controlP5.DropdownList;
import robot.RobotRun;

/**
 * An extension of the DropdownList class in ControlP5 that allows easier access of
 * the currently selected element's value.
 */
public class MyDropdownList extends DropdownList {

	protected MyDropdownList( ControlP5 theControlP5 , ControllerGroup< ? > theGroup , String theName , int theX , int theY , int theW , int theH ) {
		super( theControlP5 , theGroup , theName , theX , theY , theW , theH );
	}

	public MyDropdownList( ControlP5 theControlP5 , String theName ) {
		super(theControlP5, theName);
	}

	/**
	 * Returns the value associated with the active label of the Dropdown list.
	 */
	public Object getActiveLabelValue() {
		Map<String, Object> associatedObjects = getItem( getCaptionLabel().getText() );

		if (associatedObjects != null) {
			return associatedObjects.get("value");
		}
		
		// You got problems ...
		return null;
	}

	protected void onRelease() {
		try {
			// Error occurs with empty list
			super.onRelease();
			
		} catch (IndexOutOfBoundsException IOOBEx) {
			//IOOBEx.printStackTrace();
		}
		// Some dropdown lists influence the display
		RobotRun.getInstance().getManager().updateWindowContentsPositions();
	}

	/**
	 * Deactivates the currently selected option
	 * in the Dropdown list.
	 */
	public void resetLabel() {
		getCaptionLabel().setText( getName() );
		setValue(0);
	}

	/**
	 * Updates the current active label for the dropdown list to the given
	 * label, if it exists in the list.
	 */
	public void setActiveLabel(String Elementlabel) {
		Map<String, Object> associatedObjects = getItem( getCaptionLabel().getText() );

		if (associatedObjects != null) {
			getCaptionLabel().setText(Elementlabel);
		}
	}

	/**
	 * Updates the currently active label on the dropdown list based
	 * on the current list of items.
	 */
	public void updateActiveLabel() {
		Map<String, Object> associatedObjects = getItem( getCaptionLabel().getText() );

		if (associatedObjects == null || associatedObjects.isEmpty()) {
			getCaptionLabel().setText( getName() );
		}
	}
}