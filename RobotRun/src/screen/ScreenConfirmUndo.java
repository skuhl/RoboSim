package screen;

import core.RobotRun;
import enums.ScreenMode;

public class ScreenConfirmUndo extends ST_ScreenConfirmCancel {

	public ScreenConfirmUndo(RobotRun r) {
		super(ScreenMode.CONFIRM_UNDO, r);
	}
	
	@Override
	void loadContents() {
		contents.setLines(robotRun.loadInstructions(robotRun.getActiveProg()));
	}

	@Override
	void loadOptions() {
		
	}
}
