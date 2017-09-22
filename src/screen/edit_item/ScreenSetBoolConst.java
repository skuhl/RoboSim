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
	public void actionEntr() {
		RoboticArm r = robotRun.getActiveRobot();
		r.getInstToEdit(robotRun.getActiveProg(), robotRun.getActiveInstIdx());
		
		if (options.getLineIdx() == 0) {
			((OperandBool)robotRun.opEdit).setValue(true);
		} else if(options.getLineIdx() == 1) {
			((OperandBool)robotRun.opEdit).setValue(false);
		}

		robotRun.lastScreen();
	}

	@Override
	protected void loadOptions() {
		options.addLine("True");
		options.addLine("False");
	}
}
