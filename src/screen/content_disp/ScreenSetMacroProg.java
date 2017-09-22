package screen.content_disp;

import core.RobotRun;
import io.DataManagement;
import programming.Program;
import robot.RoboticArm;
import screen.ScreenMode;

public class ScreenSetMacroProg extends ST_ScreenListContents {

	public ScreenSetMacroProg(RobotRun r) {
		super(ScreenMode.SET_MACRO_PROG, r);
	}

	@Override
	public void actionEntr() {
		RoboticArm r = robotRun.getActiveRobot();
		int idx = robotRun.getLastScreen().getContentIdx();
		Program p = r.getProgram(contents.getLineIdx());
		
		r.getMacro(idx).setProg(p);
		DataManagement.saveRobotData(r, 8);
		robotRun.lastScreen();
	}

	@Override
	protected void loadContents() {
		RoboticArm r = robotRun.getActiveRobot();
		contents.setLines(loadPrograms(r));
	}

	@Override
	protected String loadHeader() {
		return "SELECT MACRO PROGRAM";
	}
}
