package screen;

import core.RobotRun;
import enums.ScreenMode;

public class ScreenNavMFMacros extends ST_ScreenListContents {

	public ScreenNavMFMacros(RobotRun r) {
		super(ScreenMode.NAV_MF_MACROS, r);
	}

	@Override
	String loadHeader() {
		return "EXECUTE MANUAL FUNCTION";
	}

	@Override
	void loadContents() {
		contents.setLines(robotRun.loadManualFunct());
	}

	@Override
	void loadLabels() {
		labels[0] = "";
		labels[1] = "";
		labels[2] = "";
		labels[3] = "";
		labels[4] = "";
	}

	@Override
	void loadVars() {}

	@Override
	public void actionEntr() {
		int macro_idx = contents.get(contents.getItemIdx()).getItemIdx();
		robotRun.execute(robotRun.getMacro(macro_idx));
	}
}
