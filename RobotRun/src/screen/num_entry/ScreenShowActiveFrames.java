package screen.num_entry;

import core.RobotRun;
import robot.RoboticArm;
import screen.ScreenMode;

public class ScreenShowActiveFrames extends ST_ScreenNumEntry {

	public ScreenShowActiveFrames(RobotRun r) {
		super(ScreenMode.ACTIVE_FRAMES, r);
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
	public void actionUp() {
		robotRun.updateActiveFramesDisplay();
		workingText = new StringBuilder(Integer.toString(robotRun.getActiveRobot().getActiveToolIdx() + 1));
		contents.moveUp(false);
	}
	
	@Override
	public void actionDn() {
		robotRun.updateActiveFramesDisplay();
		workingText = new StringBuilder(Integer.toString(robotRun.getActiveRobot().getActiveUserIdx() + 1));

		contents.moveDown(false);
	}

	@Override
	public void actionEntr() {
		robotRun.updateActiveFramesDisplay();
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
