package screen.num_entry;

import core.RobotRun;
import programming.MotionInstruction;
import robot.RoboticArm;
import screen.ScreenMode;

public class ScreenSetMotionInstrTerm extends ST_ScreenNumEntry {

	public ScreenSetMotionInstrTerm(RobotRun r) {
		super(ScreenMode.SET_MINST_TERM, r);
	}

	@Override
	public void actionEntr() {
		try {
			RoboticArm r = robotRun.getActiveRobot();
			MotionInstruction m = (MotionInstruction) r.getInstToEdit(robotRun.getActiveProg(), 
					robotRun.getActiveInstIdx());
			int tempTerm = Integer.parseInt(workingText.toString());

			if (tempTerm >= 0 && tempTerm <= 100) {
				m.setTermination(tempTerm);
				robotRun.lastScreen();
				
			} else {
				errorMessage("The termination must be within the range 0 and 100");
			}
			
		} catch (NumberFormatException NFEx) {
			// Not an integer
			errorMessage("The termination must be an integer");
		}
	}

	@Override
	protected void loadOptions() {
		options.addLine("Enter desired termination %(0-100):");
		options.addLine("\0" + workingText);
	}
}
