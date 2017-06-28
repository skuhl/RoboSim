package screen.num_entry;

import core.RobotRun;
import expression.OperandDReg;
import robot.RoboticArm;
import screen.ScreenMode;
import screen.ScreenState;

public class ScreenInputDataRegIdx extends ST_ScreenNumEntry {

	public ScreenInputDataRegIdx(ScreenState prevState, RobotRun r) {
		super(ScreenMode.INPUT_DREG_IDX, prevState, r);
	}

	@Override
	protected void loadOptions() {
		options.addLine("Input register index:");
		options.addLine("\0" + workingText);
	}

	@Override
	public void actionEntr() {
		RoboticArm r = robotRun.getActiveRobot();
		int idx = Integer.parseInt(workingText.toString());
			
		if (idx < 1 || idx > 100) {
			System.err.println("Invalid index!");

		} else {
			r.getInstToEdit(robotRun.getActiveProg(), robotRun.getActiveInstIdx());
			((OperandDReg)robotRun.opEdit).setValue(robotRun.getActiveRobot().getDReg(idx - 1));
		}

		robotRun.lastScreen();
	}
}
