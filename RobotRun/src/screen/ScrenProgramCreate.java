package screen;

import core.RobotRun;
import enums.ScreenMode;
import global.DataManagement;
import programming.Program;
import robot.RoboticArm;

public class ScrenProgramCreate extends ST_ScreenTextEntry {

	public ScrenProgramCreate(RobotRun r) {
		super(ScreenMode.PROG_CREATE, r);
	}

	@Override
	String loadHeader() {
		return "NAME PROGRAM";
	}

	@Override
	public void actionEntr() {
		RoboticArm r = robotRun.getActiveRobot();
		if (workingText.length() > 0 && !workingText.equals("\0")) {
			if (workingText.charAt(workingText.length() - 1) == '\0') {
				// Remove insert character
				workingText.deleteCharAt(workingText.length() - 1);
			}

			int new_prog = r.addProgram(new Program(workingText.toString(), r));
			robotRun.setActiveProgIdx(new_prog);
			robotRun.setActiveInstIdx(0);

			DataManagement.saveRobotData(robotRun.getActiveRobot(), 1);
			robotRun.switchScreen(ScreenMode.NAV_PROG_INSTR);
		}
	}

}
