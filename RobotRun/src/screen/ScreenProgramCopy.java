package screen;

import core.RobotRun;
import enums.ScreenMode;
import global.DataManagement;
import programming.Program;

public class ScreenProgramCopy extends ST_ScreenTextEntry {

	public ScreenProgramCopy(RobotRun r) {
		super(ScreenMode.PROG_COPY, r);
	}

	@Override
	String loadHeader() {
		return "COPY PROGRAM";
	}

	@Override
	public void actionEntr() {
		if (workingText.length() > 0 && !workingText.equals("\0")) {
			if (workingText.charAt(workingText.length() - 1) == '\0') {
				// Remove insert character
				workingText.deleteCharAt(workingText.length() - 1);
			}

			Program prog = robotRun.getActiveProg();

			if (prog != null) {
				Program newProg = prog.clone();
				newProg.setName(workingText.toString());
				int new_prog = robotRun.getActiveRobot().addProgram(newProg);
				robotRun.setActiveProgIdx(new_prog);
				robotRun.setActiveInstIdx(0);
				DataManagement.saveRobotData(robotRun.getActiveRobot(), 1);
			}

			robotRun.lastScreen();
		}
	}

}
