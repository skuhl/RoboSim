package screen.edit_item;

import core.RobotRun;
import global.Fields;
import programming.FrameInstruction;
import screen.ScreenMode;

public class ScreenSetFrameInstrType extends ST_ScreenEditItem {
	
	public ScreenSetFrameInstrType(RobotRun r) {
		super(ScreenMode.SELECT_FRAME_INSTR_TYPE, r);
	}
	
	@Override
	public void actionEntr() {
		FrameInstruction fInst = (FrameInstruction) robotRun.getActiveInstruction();
		
		if (options.getLineIdx() == 0) {
			fInst.setFrameType(Fields.FTYPE_TOOL);
			
		} else {
			fInst.setFrameType(Fields.FTYPE_USER);
		}
		
		robotRun.lastScreen();
	}

	@Override
	protected void loadOptions() {
		options.addLine("1. TFRAME_NUM = ...");
		options.addLine("2. UFRAME_NUM = ...");
	}
}
