package screen.num_entry;

import core.RobotRun;
import enums.ScreenMode;
import expression.OperandPRegIdx;
import robot.RoboticArm;

public class ScreenInputPosRegSubIdx extends ST_ScreenNumEntry {

	public ScreenInputPosRegSubIdx(RobotRun r) {
		super(ScreenMode.INPUT_PREG_IDX2, r);
	}
	
	@Override
	protected void loadOptions() {
		options.addLine("Input position value index:");
		options.addLine("\0" + workingText);
	}

	@Override
	public void actionEntr() {
		RoboticArm r = robotRun.getActiveRobot();
		int idx = Integer.parseInt(workingText.toString());
		
		if (idx < 1 || idx > 6) {
			System.err.println("Invalid index!");
		} else {
			r.getInstToEdit(robotRun.getActiveProg(), robotRun.getActiveInstIdx());
			((OperandPRegIdx)robotRun.opEdit).setSubIdx(idx - 1);
		}
		
		robotRun.lastScreen();
	}
}
