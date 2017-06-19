package screen;

import core.RobotRun;
import enums.ScreenMode;
import global.DataManagement;
import robot.RoboticArm;

public class ScreenEditDataRegComment extends ST_ScreenTextEntry {

	public ScreenEditDataRegComment(RobotRun r) {
		super(ScreenMode.EDIT_DREG_COM, r);
	}

	@Override
	String loadHeader() {
		return String.format("R[%d]: COMMENT EDIT", robotRun.getLastScreenState().conLnIdx + 1);
	}

	@Override
	public void actionEntr() {
		RoboticArm r = robotRun.getActiveRobot();
		
		if (!workingText.equals("\0")) {
			if (workingText.charAt(workingText.length() - 1) == '\0') {
				workingText.deleteCharAt(workingText.length() - 1);
			}
			// Save the inputed comment to the selected register
			r.getDReg(robotRun.getLastScreenState().conLnIdx).comment =	workingText.toString();
			DataManagement.saveRobotData(r, 3);
			workingText = new StringBuilder();
			robotRun.lastScreen();
		}
	}

}
