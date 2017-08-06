package screen.cnfrm_cncl;

import core.RobotRun;
import programming.Program;
import robot.RoboticArm;
import screen.ScreenMode;

public class ScreenConfirmProgramDelete extends ST_ScreenConfirmCancel {

	public ScreenConfirmProgramDelete(RobotRun r) {
		super(ScreenMode.CONFIRM_PROG_DELETE, r);
	}

	@Override
	public void actionF4() {
		RoboticArm r = robotRun.getActiveRobot();
		Program p = robotRun.getActiveProg();
		
		r.rmProg(p);
		robotRun.setActiveProg(null);
		robotRun.lastScreen();
	}
	
	@Override
	public void actionF5() {
		robotRun.lastScreen();
	}
	
	@Override
	protected void loadOptions() {
		options.addLine("Delete selected program?");
	}
}
