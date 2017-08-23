package screen.text_entry;

import core.RobotRun;
import io.DataManagement;
import robot.RoboticArm;
import screen.ScreenMode;

public class ScreenEditDataRegComment extends ST_ScreenTextEntry {

	public ScreenEditDataRegComment(RobotRun r) {
		super(ScreenMode.EDIT_DREG_COM, r);
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
			DataManagement.saveRobotData(r, 4);
			workingText = new StringBuilder();
			robotRun.lastScreen();
		}
	}

	@Override
	protected String loadHeader() {
		return String.format("R[%d]: COMMENT EDIT", robotRun.getLastScreen().getContentIdx() + 1);
	}

}
