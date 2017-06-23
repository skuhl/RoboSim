package screen.cnfrm_cncl;

import core.RobotRun;
import enums.ScreenMode;
import robot.RoboticArm;

public class ScreenConfirmProgramDelete extends ST_ScreenConfirmCancel {

	public ScreenConfirmProgramDelete(RobotRun r) {
		super(ScreenMode.CONFIRM_PROG_DELETE, r);
	}

	@Override
	protected void loadOptions() {
		options.addLine("Delete selected program?");
	}
	
	@Override
	public void actionF4() {
		RoboticArm r = robotRun.getActiveRobot();
		int progIdx = robotRun.getActiveProgIdx();

		if (progIdx >= 0 && progIdx < r.numOfPrograms()) {
			r.rmProgAt(progIdx);
			robotRun.lastScreen();
		}
	}
	
	@Override
	public void actionF5() {
		robotRun.lastScreen();
	}
}
