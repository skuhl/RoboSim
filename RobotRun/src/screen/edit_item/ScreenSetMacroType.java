package screen.edit_item;

import core.RobotRun;
import global.Fields;
import io.DataManagement;
import robot.RoboticArm;
import screen.ScreenMode;

public class ScreenSetMacroType extends ST_ScreenEditItem {

	public ScreenSetMacroType(RobotRun r) {
		super(ScreenMode.SET_MACRO_TYPE, r);
	}

	@Override
	public void actionEntr() {
		RoboticArm r = robotRun.getActiveRobot();
		int idx = robotRun.getLastScreen().getContentIdx();
		
		if (options.getLineIdx() == 0) {
			r.setMacroType(idx, false);
			robotRun.switchScreen(ScreenMode.SET_MACRO_BINDING, false);
		} else if (options.getLineIdx() == 1) {
			r.setMacroType(idx, true);
			robotRun.lastScreen();
		}
		
		DataManagement.saveRobotData(r, 8);
	}
	
	@Override
	protected void loadContents() {
		contents.setLines(loadMacros());
	}

	@Override
	protected String loadHeader() {
		return "VIEW/ EDIT MACROS";
	}

	@Override
	protected void loadOptions() {
		options.addLine("1. Shift + User Key");
		options.addLine("2. Manual Function");
	}

}
