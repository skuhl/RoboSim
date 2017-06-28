package screen.text_entry;

import core.RobotRun;
import global.DataManagement;
import robot.RoboticArm;
import screen.ScreenMode;
import screen.ScreenState;

public class ScreenEditPosRegComment extends ST_ScreenTextEntry {

	public ScreenEditPosRegComment(ScreenState prevState, RobotRun r) {
		super(ScreenMode.EDIT_PREG_COM, prevState, r);
	}

	@Override
	protected String loadHeader() {
		return String.format("PR[%d]: COMMENT EDIT", robotRun.getLastScreen().getContentIdx() + 1);
	}

	@Override
	public void actionEntr() {
		RoboticArm r = robotRun.getActiveRobot();
		if (!workingText.equals("\0")) {
			if (workingText.charAt(workingText.length() - 1) == '\0') {
				workingText.deleteCharAt(workingText.length() - 1);
			}
			// Save the inputed comment to the selected register
			r.getPReg(robotRun.getLastScreen().getContentIdx()).comment = workingText.toString();
			DataManagement.saveRobotData(r, 3);
			workingText = new StringBuilder();
			robotRun.lastScreen();
		}
	}

}
