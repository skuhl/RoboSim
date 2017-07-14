package screen.num_entry;

import core.RobotRun;
import expression.OperandPReg;
import expression.OperandPRegIdx;
import robot.RoboticArm;
import screen.ScreenMode;

public class ScreenInputPosRegIdx extends ST_ScreenNumEntry {

	public ScreenInputPosRegIdx(RobotRun r) {
		super(ScreenMode.INPUT_PREG_IDX1, r);
	}

	@Override
	protected void loadOptions() {
		options.addLine("Input register index:");
		options.addLine("\0" + workingText);
	}

	@Override
	public void actionEntr() {
		RoboticArm r = robotRun.getActiveRobot();
		
		try {
			int idx = Integer.parseInt(workingText.toString());
			
			if (idx < 1 || idx > 100) {
				errorMessage("The index must be within the range 1 and 100");
				
			} else if(robotRun.opEdit instanceof OperandPReg) {
				r.getInstToEdit(robotRun.getActiveProg(), robotRun.getActiveInstIdx());
				((OperandPReg)robotRun.opEdit).setValue(robotRun.getActiveRobot().getPReg(idx - 1));
				robotRun.lastScreen();
				
			} else if(robotRun.opEdit instanceof OperandPRegIdx) {
				r.getInstToEdit(robotRun.getActiveProg(), robotRun.getActiveInstIdx());
				((OperandPRegIdx)robotRun.opEdit).setValue(robotRun.getActiveRobot().getPReg(idx - 1));
				robotRun.lastScreen();
			}
			
		} catch (NumberFormatException NFEx) {
			errorMessage("The index must be an integer");
		}
	}
}
