package screen.opt_menu;

import core.RobotRun;
import enums.ScreenMode;

public class ScreenSelectJumpLabel extends ST_ScreenOptionsMenu {

	public ScreenSelectJumpLabel(RobotRun r) {
		super(ScreenMode.SELECT_JMP_LBL, r);
	}

	@Override
	protected String loadHeader() {
		return "INSERT JUMP/ LABEL INSTRUCTION";
	}

	@Override
	protected void loadOptions() {
		options.addLine("1. LBL[...]");
		options.addLine("2. JMP LBL[...]");
	}

	@Override
	public void actionEntr() {
		robotRun.screenStates.pop();

		if (options.getLineIdx() == 0) {
			robotRun.newLabel();
			robotRun.switchScreen(ScreenMode.SET_LBL_NUM);
		} else {
			robotRun.newJumpInstruction();
			robotRun.switchScreen(ScreenMode.SET_JUMP_TGT);
		}
	}

}
