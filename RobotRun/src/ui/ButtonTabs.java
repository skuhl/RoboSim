package ui;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import controlP5.ButtonBar;
import controlP5.ControlEvent;
import controlP5.ControlP5;
import robot.RobotRun;
import window.WindowManager;
import window.WindowTab;

/**
 * A extension of ControlP5's ButtonBar object that works with the WindowManager
 * to control the main UI view.
 * 
 * @author Joshua Hooker
 */
public class ButtonTabs extends ButtonBar {

	public ButtonTabs(ControlP5 parent, String name) {
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
}