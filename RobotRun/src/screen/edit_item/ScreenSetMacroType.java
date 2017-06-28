package screen.edit_item;

import core.RobotRun;
import screen.ScreenMode;
import screen.ScreenState;

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
		contents.setLines(robotRun.loadMacros());
	}

	@Override
	protected void loadOptions() {
		options.addLine("1. Shift + User Key");
		options.addLine("2. Manual Function");
	}

	@Override
	public void actionEntr() {
		int idx = robotRun.getLastScreen().getContentIdx();
		if (options.getLineIdx() == 0) {
			robotRun.getMacro(idx).setManual(false);
			robotRun.switchScreen(ScreenMode.SET_MACRO_BINDING);
		} else if (options.getLineIdx() == 1) {
			robotRun.getMacro(idx).setManual(true);
			robotRun.getMacro(idx).clearNum();
			robotRun.lastScreen();
		}
	}

}
