package screen.opt_menu;

import core.RobotRun;
import global.Fields;
import screen.ScreenMode;

public class ScreenSelectFrameInstrType extends ST_ScreenOptionsMenu {

	public ScreenSelectFrameInstrType(RobotRun r) {
		super(ScreenMode.SELECT_FRAME_INSTR_TYPE, r);
	}

	@Override
	protected String loadHeader() {
		return "SELECT FRAME INSTRUCTION TYPE";
	}

	@Override
	protected void loadOptions() {
		options.addLine("1. TFRAME_NUM = ...");
		options.addLine("2. UFRAME_NUM = ...");
	}

	@Override
	public void actionEntr() {
		if (options.getLineIdx() == 0) {
			robotRun.newFrameInstruction(Fields.FTYPE_TOOL);
		} else {
			robotRun.newFrameInstruction(Fields.FTYPE_USER);
		}

		robotRun.getScreenStack().pop();
		robotRun.switchScreen(ScreenMode.SET_FRAME_INSTR_IDX);
	}

}
