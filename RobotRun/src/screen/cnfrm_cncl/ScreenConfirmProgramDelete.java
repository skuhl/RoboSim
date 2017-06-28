package screen.cnfrm_cncl;

import core.RobotRun;
import robot.RoboticArm;
import screen.ScreenMode;
import screen.ScreenState;

public class ScreenConfirmProgramDelete extends ST_ScreenConfirmCancel {

	public ScreenConfirmProgramDelete(ScreenState prevState, RobotRun r) {
		super(ScreenMode.CONFIRM_PROG_DELETE, prevState, r);
	}

	@Override
	protected void loadOptions() {
		options.addLine("Delete selected program?");
	}
	
	@Override
	protected void loadVars(ScreenState s) {
		setScreenIndices(s.conLnIdx, 0, s.conRenIdx, -1, 0);
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
