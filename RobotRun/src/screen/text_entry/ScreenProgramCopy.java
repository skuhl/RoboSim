package screen.text_entry;

import core.RobotRun;
import global.Fields;
import io.DataManagement;
import programming.Program;
import robot.RoboticArm;
import screen.ScreenMode;

public class ScreenProgramCopy extends ST_ScreenTextEntry {

	public ScreenProgramCopy(RobotRun r) {
		super(ScreenMode.PROG_COPY, r);
	}

	@Override
	public void actionEntr() {
		if (workingText.length() > 0 && !workingText.equals("\0")) {
			if (workingText.charAt(workingText.length() - 1) == '\0') {
				// Remove insert character
				workingText.deleteCharAt(workingText.length() - 1);
			}
			
			String name = workingText.toString();
			Program prog = robotRun.getActiveProg();

			if (prog != null) {
				RoboticArm r = robotRun.getActiveRobot();
				Program withSameName = r.getProgram(name);
				
				if (withSameName == null) {
					Program newProg = prog.clone();
					newProg.setName(workingText.toString());
					int new_prog = r.addProgram(newProg);
					robotRun.setActiveProgIdx(new_prog);
					robotRun.setActiveInstIdx(0);
					DataManagement.saveProgram(r.RID, newProg);
					robotRun.lastScreen();
					
				} else {
					Fields.setMessage("A program with the name %s already exists",
							name);
				}
				
			} else {
				Fields.debug("No program is selected to copy!");
			}
			
		} else {
			Fields.setMessage("The program's name cannot be empty");
		}
	}

	@Override
	protected String loadHeader() {
		return "COPY PROGRAM";
	}

}
