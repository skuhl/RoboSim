package screen.edit_item;

import core.RobotRun;
import global.DataManagement;
import global.Fields;
import robot.RoboticArm;
import screen.ScreenMode;

public class ScreenSetMacroType extends ST_ScreenEditItem {

	public ScreenSetMacroType(RobotRun r) {
		super(ScreenMode.SET_MACRO_TYPE, r);
	}

	@Override
	protected String loadHeader() {
		return "VIEW/ EDIT MACROS";
	}
	
	@Override
	protected void loadContents() {
		contents.setLines(loadMacros());
	}

	@Override
	protected void loadOptions() {
		options.addLine("1. Shift + User Key");
		options.addLine("2. Manual Function");
	}

	@Override
	public void actionEntr() {
		RoboticArm r = robotRun.getActiveRobot();
		int idx = robotRun.getLastScreen().getContentIdx();
		Fields.debug("%d\n", idx);
		if (options.getLineIdx() == 0) {
			r.getMacro(idx).setManual(false);
			robotRun.switchScreen(ScreenMode.SET_MACRO_BINDING);
		} else if (options.getLineIdx() == 1) {
			r.getMacro(idx).setManual(true);
			r.getMacro(idx).clearNum();
			robotRun.lastScreen();
		}
		
		DataManagement.saveRobotData(r, 8);
	}

}
