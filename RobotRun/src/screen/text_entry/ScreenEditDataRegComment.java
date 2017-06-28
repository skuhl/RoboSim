package screen.text_entry;

import core.RobotRun;
import global.DataManagement;
import robot.RoboticArm;
import screen.ScreenMode;
import screen.ScreenState;

public class ScreenEditDataRegComment extends ST_ScreenTextEntry {

	public ScreenEditDataRegComment(RobotRun r) {
		super(ScreenMode.EDIT_DREG_COM, r);
	}

	@Override
	protected String loadHeader() {
		return String.format("R[%d]: COMMENT EDIT", robotRun.getLastScreen().getContentIdx() + 1);
	}

	@Override
	public void actionEntr() {
		RoboticArm r = robotRun.getActiveRobot();
		
		if (!workingText.equals("\0")) {
			if (workingText.charAt(workingText.length() - 1) == '\0') {
				workingText.deleteCharAt(workingText.length() - 1);
			}
			// Save the given comment to the selected register
			r.getDReg(robotRun.getLastScreen().getContentIdx()).comment = workingText.toString();
			DataManagement.saveRobotData(r, 3);
			workingText = new StringBuilder();
			robotRun.lastScreen();
		}
	}

}
