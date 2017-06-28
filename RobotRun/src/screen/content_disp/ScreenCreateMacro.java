package screen.content_disp;

import core.RobotRun;
import programming.Macro;
import robot.RoboticArm;
import screen.ScreenMode;
import screen.ScreenState;

public class ScreenCreateMacro extends ST_ScreenListContents {

	public ScreenCreateMacro(RobotRun r) {
		super(ScreenMode.CREATE_MACRO, r);
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
		Macro m = new Macro(contents.getLineIdx());
		robotRun.getMacroList().add(m);
		robotRun.switchScreen(ScreenMode.SET_MACRO_TYPE);
	}
}
