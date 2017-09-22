package screen.opt_menu;

import core.RobotRun;
import screen.ScreenMode;

public class ScreenSelectJumpLabel extends ST_ScreenOptionsMenu {

	public ScreenSelectJumpLabel(RobotRun r) {
		super(ScreenMode.SELECT_JMP_LBL, r, 0, 10, 20, 3, 10, 20);
	}

	@Override
	public void actionEntr() {
		if (options.getLineIdx() == 0) {
			robotRun.newLabel();
			robotRun.lastScreen();
			robotRun.switchScreen(ScreenMode.SET_LBL_NUM, true);
			
		} else {
			robotRun.newJumpInstruction();
			robotRun.lastScreen();
			robotRun.switchScreen(ScreenMode.SET_JUMP_TGT, true);
		}
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
}
