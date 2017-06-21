package screen.num_entry;

import core.RobotRun;
import enums.ScreenMode;
import programming.MotionInstruction;
import robot.RoboticArm;

public class ScreenSetMotionInstrTerm extends ST_ScreenNumEntry {

	public ScreenSetMotionInstrTerm(RobotRun r) {
		super(ScreenMode.SET_MINST_TERM, r);
	}

	@Override
	protected void loadOptions() {
		options.addLine("Enter desired termination %(0-100):");
		options.addLine("\0" + workingText);
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
			}
		} catch (NumberFormatException NFEx) {/* Ignore invalid input */}

		robotRun.lastScreen();
	}

}
