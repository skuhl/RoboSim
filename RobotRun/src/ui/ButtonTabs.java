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
	
	private WindowManager manager;

	public ButtonTabs(WindowManager m, ControlP5 parent, String name) {
		super(parent, name);
		manager = m;
	}
	
	/**
	 * Gets the label for the button, which is active, in this button bar.
	 * 
	 * @return	The label of the active button
	 */
	@SuppressWarnings("unchecked")
	private String getActButLbl() {
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
	public void onClick() {
		// Update active button state
		super.onClick();
		
		// Update the window based on the button selected
		String actLbl = getActButLbl();
		
		if (actLbl.equals("Robot1")) {
			manager.updateActRobot( WindowTab.ROBOT1 );
			
		} else if (actLbl.equals("Robot2")) {
			manager.updateActRobot( WindowTab.ROBOT2 );
			
		} else if (actLbl.equals("Create")) {
			manager.updateActRobot( WindowTab.CREATE );
			
		} else if (actLbl.equals("Edit")) {
			manager.updateActRobot( WindowTab.EDIT );
			
		} else if (actLbl.equals("Scenario")) {
			manager.updateActRobot( WindowTab.SCENARIO );
			
		} else if (actLbl.equals("Misc")) {
			manager.updateActRobot( WindowTab.MISC );
			
		} else {
			manager.updateActRobot( null );
		}
	}
}