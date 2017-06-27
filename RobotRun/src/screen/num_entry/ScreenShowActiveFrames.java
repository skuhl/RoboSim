package screen.num_entry;

import core.RobotRun;
import robot.RoboticArm;
import screen.ScreenMode;
import screen.ScreenState;
import ui.DisplayLine;

public class ScreenShowActiveFrames extends ST_ScreenNumEntry {

	public ScreenShowActiveFrames(RobotRun r) {
		super(ScreenMode.ACTIVE_FRAMES, r);
		
		contents.setSelectedLineIdx(0);
		contents.setSelectedColumnIdx(1);
		String initialText = Integer.toString(robotRun.getActiveRobot().getActiveToolIdx() + 1);
		workingText = new StringBuilder(initialText);
	}

	@Override
	protected String loadHeader() {
		return "ACTIVE FRAMES";
	}
	
	@Override
	protected void loadContents() {
		/* workingText corresponds to the active row's index display */
		RoboticArm r = robotRun.getActiveRobot();
		if (contents.getLineIdx() == 0) {
			contents.addLine("Tool: ", workingText.toString());
			contents.addLine("User: ", Integer.toString(r.getActiveUserIdx() + 1));

		} else {
			contents.addLine("Tool: ", Integer.toString(r.getActiveToolIdx() + 1));
			contents.addLine("User: ", workingText.toString());
		}
	}
	
	@Override
	protected void loadOptions() {}
	
	@Override
	protected void loadLabels() {
		// F1, F2
		labels[0] = "[List]";
		labels[1] = "";
		labels[2] = "";
		labels[3] = "";
		labels[4] = "";
	}
	
	@Override
	protected void loadVars(ScreenState s) {
		setScreenIndices(contents.getCurrentItemIdx(), contents.getItemColumnIdx(),
				contents.getRenderStart(), 0, 0);
	}
	
	@Override
	public void actionUp() {
		updateActiveFramesDisplay();
		workingText = new StringBuilder(Integer.toString(robotRun.getActiveRobot().getActiveToolIdx() + 1));
		contents.moveUp(false);
	}
	
	@Override
	public void actionDn() {
		updateActiveFramesDisplay();
		workingText = new StringBuilder(Integer.toString(robotRun.getActiveRobot().getActiveUserIdx() + 1));

		contents.moveDown(false);
	}

	@Override
	public void actionEntr() {
		updateActiveFramesDisplay();
		robotRun.updatePendantScreen();
	}
	
	/**
	 * Updates the index display in the Active Frames menu based on the current
	 * value of workingText
	 */
	private void updateActiveFramesDisplay() {
		RoboticArm robot = robotRun.getActiveRobot();
		
		// Attempt to parse the inputed integer value
		try {
			int frameIdx = Integer.parseInt(workingText.toString()) - 1;

			if (frameIdx >= -1 && frameIdx < 10) {
				// Update the appropriate active Frame index
				if (getContentIdx() == 0) {
					robot.setActiveToolFrame(frameIdx);
				} else {
					robot.setActiveUserFrame(frameIdx);
				}
			}

		} catch (NumberFormatException NFEx) {
			// Non-integer value
		}
		// Update display
		if (getContentIdx() == 0) {
			workingText = new StringBuilder(Integer.toString(robot.getActiveToolIdx() + 1));

		} else {
			workingText = new StringBuilder(Integer.toString(robot.getActiveUserIdx() + 1));
		}
		
		int colIdx = getContentIdx();
		DisplayLine activeLn = getContents().getCurrentItem();
		
		if (activeLn != null) {
			activeLn.set(colIdx, workingText.toString());
		}
	}
	
	@Override
	public void actionF1() {
		if (contents.getLineIdx() == 0) {
			robotRun.nextScreen(ScreenMode.NAV_TOOL_FRAMES);
		} else if (contents.getLineIdx() == 1) {
			robotRun.nextScreen(ScreenMode.NAV_USER_FRAMES);
		}
	}
}
