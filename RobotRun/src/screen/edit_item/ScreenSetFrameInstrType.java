package screen.edit_item;

import core.RobotRun;
import global.Fields;
import screen.ScreenMode;
import screen.ScreenState;

public class ScreenSetFrameInstrType extends ST_ScreenEditItem {
	
	public ScreenSetFrameInstrType(RobotRun r) {
		super(ScreenMode.SELECT_FRAME_INSTR_TYPE, r);
		
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

		robotRun.popScreenStack(1);
		robotRun.switchScreen(ScreenMode.SET_FRAME_INSTR_IDX);
	}
}
