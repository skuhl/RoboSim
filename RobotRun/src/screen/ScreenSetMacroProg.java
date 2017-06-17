package screen;

import core.RobotRun;
import enums.ScreenMode;
import programming.Macro;
import robot.RoboticArm;

public class ScreenSetMacroProg extends ST_ScreenListContents {

	public ScreenSetMacroProg(RobotRun r) {
		super(ScreenMode.SET_MACRO_PROG, r);
	}

	@Override
	String loadHeader() {
		return "SELECT MACRO PROGRAM";
	}

	@Override
	void loadContents() {
		RoboticArm r = robotRun.getActiveRobot();
		contents.setLines(robotRun.loadPrograms(r));
	}

	@Override
	void loadVars() {}

	@Override
	public void actionEntr() {
		if (robotRun.macroEdit == null) {
			robotRun.macroEdit = new Macro(contents.getLineIdx());
			robotRun.getMacroList().add(robotRun.macroEdit);
			robotRun.switchScreen(ScreenMode.SET_MACRO_TYPE);
		} else {
			robotRun.macroEdit.setProgram(contents.getLineIdx());
		}
	}

}
