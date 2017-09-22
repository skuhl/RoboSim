package screen.num_entry;

import core.RobotRun;
import expression.OperandIOReg;
import robot.RoboticArm;
import screen.ScreenMode;

public class ScreenInputIORegIdx extends ST_ScreenNumEntry {

	public ScreenInputIORegIdx(RobotRun r) {
		super(ScreenMode.INPUT_IOREG_IDX, r);
	}

	@Override
	public void actionEntr() {
		RoboticArm r = robotRun.getActiveRobot();
		
		try {
			int idx = Integer.parseInt(workingText.toString());
			
			if (idx < 1 || idx >= r.numOfEndEffectors()) {
				errorMessage("The index must be with the range 1 and %d",
						r.numOfEndEffectors() - 1);
	
			} else {
				r.getInstToEdit(robotRun.getActiveProg(), robotRun.getActiveInstIdx());
				((OperandIOReg)robotRun.opEdit).setValue(robotRun.getActiveRobot().getIOReg(idx));
				robotRun.lastScreen();
			}
			
		} catch (NumberFormatException NFEx) {
			errorMessage("The index must be a integer");
		}
	}
	
	@Override
	protected void loadOptions() {
		options.addLine("Input register index:");
		options.addLine("\0" + workingText);
	}
}
