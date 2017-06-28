package screen.num_entry;

import core.RobotRun;
import expression.OperandFloat;
import robot.RoboticArm;
import screen.ScreenMode;
import screen.ScreenState;

public class ScreenInputConst extends ST_ScreenNumEntry {

	public ScreenInputConst(ScreenState prevState, RobotRun r) {
		super(ScreenMode.INPUT_CONST, prevState, r);
	}

	@Override
	protected String loadHeader() {
		return robotRun.getActiveProg().getName();
	}
	
	@Override
	protected void loadOptions() {
		options.addLine("Input constant value:");
		options.addLine("\0" + workingText);
	}

	@Override
	public void actionEntr() {
		try {
			RoboticArm r = robotRun.getActiveRobot();
			float data = Float.parseFloat(workingText.toString());
			r.getInstToEdit(robotRun.getActiveProg(), robotRun.getActiveInstIdx());
			((OperandFloat)robotRun.opEdit).setValue(data);
			
		} catch (NumberFormatException e) {
			//TODO report error to user
			e.printStackTrace();
		}

		robotRun.lastScreen();
	}
}
