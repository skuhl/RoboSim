package screen.text_entry;

import core.RobotRun;
import global.DataManagement;
import programming.Program;
import screen.ScreenMode;

public class ScreenProgramCopy extends ST_ScreenTextEntry {
	
	private Program originProg;
	
	public ScreenProgramCopy(RobotRun r, Program prog) {
		super(ScreenMode.PROG_COPY, r);
		originProg = prog;
	}

	@Override
	protected String loadHeader() {
		return "COPY PROGRAM";
	}

	@Override
	public void actionEntr() {
		if (workingText.length() > 0 && !workingText.equals("\0")) {
			if (workingText.charAt(workingText.length() - 1) == '\0') {
				// Remove insert character
				workingText.deleteCharAt(workingText.length() - 1);
			}

			if (originProg != null) {
				Program newProg = originProg.clone();
				newProg.setName(workingText.toString());
				robotRun.getActiveRobot().addProgram(newProg);
				DataManagement.saveRobotData(robotRun.getActiveRobot(), 1);
			}

			robotRun.lastScreen();
		}
	}

}
