package screen;

import core.RobotRun;
import enums.ScreenMode;
import global.DataManagement;
import programming.Program;

public class ScreenProgramRename extends ST_ScreenTextEntry {

	public ScreenProgramRename(RobotRun r) {
		super(ScreenMode.PROG_RENAME, r);
	}

	@Override
	String loadHeader() {
		return "RENAME PROGRAM";
	}

	@Override
	public void actionEntr() {
		if (workingText.length() > 0 && !workingText.equals("\0")) {
			if (workingText.charAt(workingText.length() - 1) == '\0') {
				// Remove insert character
				workingText.deleteCharAt(workingText.length() - 1);
			}
			// Rename the active program
			Program prog = robotRun.getActiveProg();
			if (prog != null) {
				prog.setName(workingText.toString());
				robotRun.getActiveRobot().reorderPrograms();
				DataManagement.saveRobotData(robotRun.getActiveRobot(), 1);
			}

			robotRun.lastScreen();
		}
	}

}
