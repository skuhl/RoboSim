package screen;

import core.RobotRun;
import enums.ScreenMode;
import expression.OperandIOReg;
import robot.RoboticArm;
import screen.num_entry.ST_ScreenNumEntry;

public class ScreenInputIORegIdx extends ST_ScreenNumEntry {

	public ScreenInputIORegIdx(RobotRun r) {
		super(ScreenMode.INPUT_IOREG_IDX, r);
	}

	@Override
	protected String loadHeader() {
		return robotRun.getActiveProg().getName();
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
		
		if (idx < 1 || idx > robotRun.getActiveRobot().numOfEndEffectors()) {
			System.err.println("Invalid index!");

		} else {
			r.getInstToEdit(robotRun.getActiveProg(), robotRun.getActiveInstIdx());
			((OperandIOReg)robotRun.opEdit).setValue(robotRun.getActiveRobot().getIOReg(idx));
		}
	}
}
