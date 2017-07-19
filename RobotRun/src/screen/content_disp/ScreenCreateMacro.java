package screen.content_disp;

import core.RobotRun;
import global.DataManagement;
import programming.Macro;
import robot.RoboticArm;
import screen.ScreenMode;

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
		contents.setLines(loadPrograms(r));
	}

	@Override
	public void actionEntr() {
		RoboticArm r = robotRun.getActiveRobot();
		r.getMacroList().add(new Macro(r, contents.getLineIdx()));
		DataManagement.saveRobotData(r, 8);
		
		robotRun.getLastScreen().setContentIdx(r.getMacroList().size() - 1);
		robotRun.switchScreen(ScreenMode.SET_MACRO_TYPE);
	}
}
