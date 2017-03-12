package ui;
import java.util.HashMap;
import java.util.List;

import controlP5.ButtonBar;
import controlP5.ControlP5;
import robot.RobotRun;

/**
 * A extension of ControlP5's ButtonBar object that actually bloody
 * lets you figure out which button is active in a reasonable manner.
 */
public class ButtonTabs extends ButtonBar {

	private String selectedButtonName;

	public ButtonTabs(ControlP5 parent, String name) {
		super(parent, name);
		selectedButtonName = null;
	}

	/**
	 * Return the name of the button which is currenty active, or
	 * null if no button is active.
	 */
	public String getActiveButtonName() {
		return selectedButtonName;
	}

	public void onClick() {
		// Update active button state
		super.onClick();

		List<?> items = getItems();
		selectedButtonName = null;
		// Determine which button is active
		for (Object item : items) {
			HashMap<?, ?> map = (HashMap<?, ?>)item;
			Object value = map.get("selected");

			if (value instanceof Boolean && (Boolean)value) {
				// Update selectedButtonName
				selectedButtonName = (String)map.get("name");
			}
		}
		
		// Set the active robot based on which tab was selected
		 if (selectedButtonName.equals("Robot1")) {
			 RobotRun.getInstance().setRobot(0);
			 
		 } else if (selectedButtonName.equals("Robot2")) {
			 RobotRun.getInstance().setRobot(1);
		 }
	}
}