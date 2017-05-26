package ui;

import java.util.HashMap;
import java.util.Map;

import controlP5.ControlP5;
import controlP5.ControllerGroup;
import controlP5.DropdownList;

/**
 * An extension of the DropdownList class in ControlP5 that allows easier access
 * of the currently selected element's value as well as functionality for
 * controlEvents involving the change in value of the dropdown list.
 * 
 * @author Joshua Hooker
 */
public class MyDropdownList extends DropdownList {
	
	public MyDropdownList(ControlP5 theControlP5, String theName) {
		super(theControlP5, theName);
	}
	
	protected MyDropdownList(ControlP5 theControlP5, ControllerGroup<?> theGroup,
			String theName, int theX, int theY, int theW, int theH) {
		
		super(theControlP5, theGroup, theName, theX, theY, theW, theH);
	}
	
	/**
	 * Returns the element that is currently selected in the dropdown list, or
	 * null if no element is selected.
	 * 
	 * @return	The active element in the dropdown
	 */
	public Object getSelectedItem() {
		try {
			int idx = (int)getValue();
			Map<String, Object> associatedObjects = getItem(idx);

			if (associatedObjects != null) {
				return associatedObjects.get("value");
			}
			
		} catch (IndexOutOfBoundsException IOOBEx) {/* No elements */}
		
		// No element selected
		return null;
	}
	
	/**
	 * Returns the label associated with item that is currently selected in the
	 * dropdown list or null, if not item is currently selected.
	 * 
	 * @return	The label associated with the selected item or null
	 */
	private String getSelectedLabel() {
		try {
			int idx = (int)getValue();
			Map<String, Object> associatedObjects = getItem(idx);

			if (associatedObjects != null) {
				return (String) associatedObjects.get("name");
			}
			
		} catch (IndexOutOfBoundsException IOOBEx) {/* No elements */}
		
		// No element selected
		return null;
	}
	
	@Override
	protected void onDrag() {
		// Show what element is selected while dragging the mouse
		onMove();
	}
	
	@Override
	protected void onEndDrag() {
		// Allow drag clicks
		
		// I hate you, controlP5
		this.isDragged = false;
		onRelease();
		this.isDragged = true;
	}
	
	@Override
	protected void onRelease() {
		if(this.getItems().size() != 0) {
			super.onRelease();
		}
	}
	
	@Override
	public DropdownList setValue(float newValue) {
		super.setValue(newValue);
		updateLabel();
		return this;
	}
	
	/**
	 * If the given item exists in the list, then it is set as the selected
	 * item and true is returned. Otherwise, the list remains unchanged and
	 * null is returned.
	 * 
	 * @param e	The item, in the list, to set
	 * @return	Whether, the given item was successfully set in the list
	 */
	@SuppressWarnings("unchecked")
	public boolean setItem(Object e) {
		int val = 0;
		
		for (Object o : getItems()) {
			HashMap<String, Object> map = (HashMap<String, Object>)o;
			
			if (map != null && e == map.get("value")) {
				// The object exists in the list
				setValue(val);
				return true;
			}
			
			++val;
		}
		
		// The object does not exist
		return false;
	}
	
	/**
	 * Updates the label for the dropdown based on the currently selected item
	 * in the list.
	 */
	protected void updateLabel() {
		String label = getSelectedLabel();
		
		if (label == null) {
			// No selected item
			getCaptionLabel().setText( getName() );
			
		} else {
			getCaptionLabel().setText( label );
		}
	}
}