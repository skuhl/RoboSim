package screen.instr_edit;

import core.RobotRun;
import expression.OperandBool;
import robot.RoboticArm;
import screen.ScreenMode;

public class ScreenSetBoolConst extends ST_ScreenInstructionEdit {

	public ScreenSetBoolConst(RobotRun r) {
		super(ScreenMode.SET_BOOL_CONST, r);
	}
	
	@Override
	protected void loadOptions() {
		options.addLine("1. False");
		options.addLine("2. True");
	}

	@Override
	public void actionEntr() {
		RoboticArm r = robotRun.getActiveRobot();
		r.getInstToEdit(robotRun.getActiveProg(), robotRun.getActiveInstIdx());
		
		if (options.getLineIdx() == 0) {
			((OperandBool)robotRun.opEdit).setValue(true);
		} else {
			((OperandBool)robotRun.opEdit).setValue(false);
		}

		robotRun.lastScreen();
	}
}
