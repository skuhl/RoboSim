package screen.text_entry;

import core.RobotRun;
import global.DataManagement;
import global.Fields;
import programming.Program;
import robot.RoboticArm;
import screen.ScreenMode;

public class ScreenProgramRename extends ST_ScreenTextEntry {

	public ScreenProgramRename(RobotRun r) {
		super(ScreenMode.PROG_RENAME, r);
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
				String name = workingText.toString();
				RoboticArm r = robotRun.getActiveRobot();
				Program withSameName = r.getProgram(name);
				
				if (withSameName == null) {
					String prevName = prog.getName();
					prog.setName(name);
					r.reorderPrograms();
					
					DataManagement.removeProgram(r.RID, prevName);
					DataManagement.saveProgram(r.RID, prog);
					robotRun.lastScreen();
					
				} else {
					Fields.setMessage("A program with the name %s already exists",
							name);
				}
				
			} else {
				Fields.debug("No program selected to rename!");
			}

			
		} else {
			Fields.setMessage("The program's name cannot be empty");
		}
	}

	@Override
	protected String loadHeader() {
		return "RENAME PROGRAM";
	}

}
