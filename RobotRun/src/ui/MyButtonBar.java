package ui;
import java.util.HashMap;
import java.util.List;

import controlP5.ButtonBar;
import controlP5.ControlP5;

/**
 * A extension of ControlP5's ButtonBar class, which includes a method to get
 * the label of the active button on the bar as well as functionality for
 * drag clicks.
 * 
 * @author Joshua Hooker
 */
public class MyButtonBar extends ButtonBar {

	public MyButtonBar(ControlP5 parent, String name) {
		super(parent, name);
	}
	
	/**
	 * Gets the label for the button, which is active, in this button bar.
	 * 
	 * @return	The label of the active button
	 */
	@SuppressWarnings("unchecked")
	public String getActButLbl() {
		List<HashMap<?, ?>> items = getItems();
		
		// Determine which button is active
		for (HashMap<?, ?> item : items) {
			assert item.get("selected") instanceof Boolean;
			Boolean value = (Boolean)item.get("selected");
			
			if (value) {
				// Update selectedButtonName
				return (String)item.get("name");
			}
		}
		
		// No active button?
		return null;
	}
	
	@Override
	protected void onEndDrag() {
		// Allow drag clicks
		onClick();
	}
	
	/**
	 * TODO comment this
	 * 
	 * @param name
	 */
	@SuppressWarnings("unchecked")
	public void setActiveButton(String label) {
		List<HashMap<Object, Object>> items = (List<HashMap<Object, Object>>) getItems();
		
		for (HashMap<Object, Object> item : items) {
			String itemName = (String) item.get("name");
			
			if (itemName != null && itemName.equals(label)) {
				// Set the specified button as active
				item.put("selected", new Boolean(true));
				
			} else {
				// Set all other buttons as inactive
				item.put("selected", new Boolean(false));
			}
		}
	}
}