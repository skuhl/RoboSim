package screen.num_entry;

import core.RobotRun;
import expression.OperandPRegIdx;
import robot.RoboticArm;
import screen.ScreenMode;

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
		
		try {
			int idx = Integer.parseInt(workingText.toString());
			
			if (idx < 1 || idx > 6) {
				errorMessage("The index must be within the range 1 and 6");
				
			} else {
				r.getInstToEdit(robotRun.getActiveProg(), robotRun.getActiveInstIdx());
				((OperandPRegIdx)robotRun.opEdit).setSubIdx(idx - 1);
				robotRun.lastScreen();
			}
			
		} catch (NumberFormatException NFEx) {
			errorMessage("The index must be an integer");
		}
	}
}
