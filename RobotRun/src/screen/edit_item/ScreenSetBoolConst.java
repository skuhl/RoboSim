package screen.edit_item;

import core.RobotRun;
import expression.OperandBool;
import robot.RoboticArm;
import screen.ScreenMode;

public class ScreenSetBoolConst extends ST_ScreenEditItem {

	public ScreenSetBoolConst(RobotRun r) {
		super(ScreenMode.SET_BOOL_CONST, r);
	}
	
	@Override
	protected void loadOptions() {
		options.addLine("False");
		options.addLine("True");
	}

	@Override
	public void actionEntr() {
		RoboticArm r = robotRun.getActiveRobot();
		r.getInstToEdit(robotRun.getActiveProg(), robotRun.getActiveInstIdx());
		
		if (options.getLineIdx() == 0) {
			((OperandBool)robotRun.opEdit).setValue(false);
		} else {
			((OperandBool)robotRun.opEdit).setValue(true);
		}

		robotRun.lastScreen();
	}
}
