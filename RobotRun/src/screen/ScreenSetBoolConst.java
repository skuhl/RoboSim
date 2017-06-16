package screen;

import core.RobotRun;
import enums.ScreenMode;
import expression.OperandBool;

public class ScreenSetBoolConst extends ST_ScreenInstructionEdit {

	public ScreenSetBoolConst(RobotRun r) {
		super(ScreenMode.SET_BOOL_CONST, r);
	}
	
	@Override
	void loadOptions() {
		options.addLine("1. False");
		options.addLine("2. True");
	}

	@Override
	public void actionEntr() {
		if (options.getLineIdx() == 0) {
			((OperandBool)robotRun.opEdit).setValue(true);
		} else {
			((OperandBool)robotRun.opEdit).setValue(false);
		}

		robotRun.lastScreen();
	}
}
