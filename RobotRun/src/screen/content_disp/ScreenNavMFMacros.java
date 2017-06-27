package screen.content_disp;

import core.RobotRun;
import screen.ScreenMode;

public class ScreenNavMFMacros extends ST_ScreenListContents {

	public ScreenNavMFMacros(RobotRun r) {
		super(ScreenMode.NAV_MF_MACROS, r);
	}

	@Override
	protected String loadHeader() {
		return "EXECUTE MANUAL FUNCTION";
	}

	@Override
	protected void loadContents() {
		contents.setLines(robotRun.loadManualFunct());
	}

	@Override
	protected void loadLabels() {
		labels[0] = "";
		labels[1] = "";
		labels[2] = "";
		labels[3] = "";
		labels[4] = "";
	}

	@Override
	public void actionEntr() {
		int macro_idx = contents.get(contents.getItemIdx()).getItemIdx();
		robotRun.execute(robotRun.getMacro(macro_idx));
	}
}
