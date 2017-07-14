package screen.num_entry;

import core.RobotRun;
import expression.OperandFloat;
import global.RMath;
import robot.RoboticArm;
import screen.ScreenMode;

public class ScreenInputConst extends ST_ScreenNumEntry {

	public ScreenInputConst(RobotRun r) {
		super(ScreenMode.INPUT_CONST, r);
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
			((OperandFloat)robotRun.opEdit).setValue(RMath.clamp(data, -9999f, 9999f));
			robotRun.lastScreen();
			
		} catch (NumberFormatException e) {
			errorMessage("The constant must be a real number");
		}
	}
}
