package screen.content_disp;

import core.RobotRun;
import global.DataManagement;
import robot.RoboticArm;
import screen.ScreenMode;

public class ScreenSetMacroProg extends ST_ScreenListContents {
	
	public ScreenSetMacroProg(RobotRun r) {
		super(ScreenMode.SET_MACRO_PROG, r);
	}

	@Override
	protected String loadHeader() {
		return "SELECT MACRO PROGRAM";
	}

	@Override
	protected void loadContents() {
		RoboticArm r = robotRun.getActiveRobot();
		contents.setLines(loadPrograms(r));
	}

	@Override
	public void actionEntr() {
		RoboticArm r = robotRun.getActiveRobot();
		int idx = robotRun.getLastScreen().getContentIdx();
		
		r.getMacro(idx).setProgram(contents.getLineIdx());
		DataManagement.saveRobotData(r, 8);
	}
}
