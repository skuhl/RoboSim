package screen.content_disp;

import core.RobotRun;
import robot.RoboticArm;
import screen.ScreenMode;
import screen.ScreenState;

public class ScreenSetMacroProg extends ST_ScreenListContents {

	public ScreenSetMacroProg(ScreenState prevState, RobotRun r) {
		super(ScreenMode.SET_MACRO_PROG, prevState, r);
	}

	@Override
	protected String loadHeader() {
		return "SELECT MACRO PROGRAM";
	}

	@Override
	protected void loadContents() {
		RoboticArm r = robotRun.getActiveRobot();
		contents.setLines(robotRun.loadPrograms(r));
	}

	@Override
	public void actionEntr() {
		int idx = robotRun.getLastScreen().getContentIdx();
		robotRun.getMacro(idx).setProgram(contents.getLineIdx());
	}
}
