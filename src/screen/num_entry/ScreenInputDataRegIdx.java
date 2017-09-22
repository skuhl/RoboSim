package screen.num_entry;

import core.RobotRun;
import expression.OperandDReg;
import robot.RoboticArm;
import screen.ScreenMode;

public class ScreenInputDataRegIdx extends ST_ScreenNumEntry {

	public ScreenInputDataRegIdx(RobotRun r) {
		super(ScreenMode.INPUT_DREG_IDX, r);
	}

	@Override
	public void actionEntr() {
		RoboticArm r = robotRun.getActiveRobot();
		try {
			int idx = Integer.parseInt(workingText.toString());
				
			if (idx < 1 || idx > 100) {
				errorMessage("The index must be within range 1 and 100");
	
			} else {
				r.getInstToEdit(robotRun.getActiveProg(), robotRun.getActiveInstIdx());
				((OperandDReg)robotRun.opEdit).setValue(robotRun.getActiveRobot().getDReg(idx - 1));
				robotRun.lastScreen();
			}

		} catch (NumberFormatException NFEx) {
			errorMessage("The index must be an integer");
		}
	}

	@Override
	protected void loadOptions() {
		options.addLine("Input register index:");
		options.addLine("\0" + workingText);
	}
}
