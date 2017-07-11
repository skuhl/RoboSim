package screen.num_entry;

import core.RobotRun;
import expression.OperandIOReg;
import global.Fields;
import robot.RoboticArm;
import screen.ScreenMode;

public class ScreenInputIORegIdx extends ST_ScreenNumEntry {

	public ScreenInputIORegIdx(RobotRun r) {
		super(ScreenMode.INPUT_IOREG_IDX, r);
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
			Fields.setMessage("Invalid index!");

		} else {
			r.getInstToEdit(robotRun.getActiveProg(), robotRun.getActiveInstIdx());
			((OperandIOReg)robotRun.opEdit).setValue(robotRun.getActiveRobot().getIOReg(idx));
		}
		
		robotRun.lastScreen();
	}
}
