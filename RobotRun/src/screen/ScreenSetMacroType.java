package screen;

import core.RobotRun;
import enums.ScreenMode;

public class ScreenSetMacroType extends ST_ScreenOptionsMenu {

	public ScreenSetMacroType(RobotRun r) {
		super(ScreenMode.SET_MACRO_TYPE, r);
	}

	@Override
	String loadHeader() {
		return "VIEW/ EDIT MACROS";
	}
	
	@Override
	void loadContents() {
		contents.setLines(robotRun.loadMacros());
	}

	@Override
	void loadOptions() {
		options.addLine("1. Shift + User Key");
		options.addLine("2. Manual Function");
	}

	@Override
	public void actionEntr() {
		if (options.getLineIdx() == 0) {
			robotRun.macroEdit.setManual(false);
			robotRun.switchScreen(ScreenMode.SET_MACRO_BINDING);
		} else if (options.getLineIdx() == 1) {
			robotRun.macroEdit.setManual(true);
			robotRun.macroEdit.clearNum();
			robotRun.lastScreen();
		}
	}

}
