package screen.num_entry;

import core.RobotRun;
import expression.OperandDReg;
import global.Fields;
import robot.RoboticArm;
import screen.ScreenMode;

public class ScreenInputDataRegIdx extends ST_ScreenNumEntry {

	public ScreenInputDataRegIdx(RobotRun r) {
		super(ScreenMode.INPUT_DREG_IDX, r);
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
			Fields.setMessage("Invalid index!");

		} else {
			r.getInstToEdit(robotRun.getActiveProg(), robotRun.getActiveInstIdx());
			((OperandDReg)robotRun.opEdit).setValue(robotRun.getActiveRobot().getDReg(idx - 1));
		}

		robotRun.lastScreen();
	}
}
