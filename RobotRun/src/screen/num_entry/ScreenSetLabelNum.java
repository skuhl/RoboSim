package screen.num_entry;

import core.RobotRun;
import programming.LabelInstruction;
import robot.RoboticArm;
import screen.ScreenMode;
import screen.ScreenState;

public class ScreenSetLabelNum extends ST_ScreenNumEntry {

	public ScreenSetLabelNum(RobotRun r) {
		super(ScreenMode.SET_LBL_NUM, r);
	}

	@Override
	protected void loadOptions() {
		options.addLine("Set label number:");
		options.addLine("\0" + workingText);
	}

	@Override
	public void actionEntr() {
		try {
			RoboticArm r = robotRun.getActiveRobot();
			int idx = Integer.parseInt(workingText.toString());

			if (idx < 0 || idx > 99) {
				System.err.println("Invalid label index!");
			} else {
				((LabelInstruction) r.getInstToEdit(robotRun.getActiveProg(), 
						robotRun.getActiveInstIdx())).setLabelNum(idx);
			}
		} catch (NumberFormatException NFEx) {/* Ignore invalid input */}

		robotRun.lastScreen();
	}

}
