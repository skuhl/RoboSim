package screen.text_entry;

import core.RobotRun;
import global.DataManagement;
import programming.Program;
import screen.ScreenMode;

public class ScreenProgramRename extends ST_ScreenTextEntry {

	private Program tgtProg;
	
	public ScreenProgramRename(RobotRun r, Program prog) {
		super(ScreenMode.PROG_RENAME, r);
		tgtProg = prog;
	}

	@Override
	protected String loadHeader() {
		return "RENAME PROGRAM";
	}

	@Override
	public void actionEntr() {
		if (workingText.length() > 0 && !workingText.equals("\0")) {
			if (workingText.charAt(workingText.length() - 1) == '\0') {
				// Remove insert character
				workingText.deleteCharAt(workingText.length() - 1);
			}
			
			// Rename the given program
			if (tgtProg != null) {
				tgtProg.setName(workingText.toString());
				robotRun.getActiveRobot().reorderPrograms();
				DataManagement.saveRobotData(robotRun.getActiveRobot(), 1);
			}
			
			robotRun.lastScreen();
		}
	}

}
