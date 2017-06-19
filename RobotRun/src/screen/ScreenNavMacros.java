package screen;

import core.RobotRun;
import enums.ScreenMode;

public class ScreenNavMacros extends ST_ScreenListContents {

	public ScreenNavMacros(RobotRun r) {
		super(ScreenMode.NAV_MACROS, r);
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
	void loadLabels() {
		labels[0] = "[New]";
		labels[1] = "";
		labels[2] = "";
		labels[3] = "[Edit]";
		labels[4] = "";
	}

	@Override
	void loadVars() {}

	@Override
	public void actionEntr() {}

	@Override
	public void actionF1() {
		robotRun.macroEdit = null;
		robotRun.nextScreen(ScreenMode.SET_MACRO_PROG);
	}

	@Override
	public void actionF4() {
		if(robotRun.getMacroList().size() > 0) {
			robotRun.macroEdit = robotRun.getMacro(contents.getLineIdx());
			
			if (contents.getColumnIdx() == 1) {
				robotRun.nextScreen(ScreenMode.SET_MACRO_PROG);
			} else if (contents.getColumnIdx() == 2) {
				robotRun.nextScreen(ScreenMode.SET_MACRO_TYPE);
			} else if (contents.getColumnIdx() == 3){
				if (!robotRun.getMacro(contents.getLineIdx()).isManual())
					robotRun.nextScreen(ScreenMode.SET_MACRO_BINDING);
			}
		}
	}
}
