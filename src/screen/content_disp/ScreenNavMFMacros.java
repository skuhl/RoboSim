package screen.content_disp;

import core.RobotRun;
import robot.RoboticArm;
import screen.ScreenMode;

public class ScreenNavMFMacros extends ST_ScreenListContents {

	public ScreenNavMFMacros(RobotRun r) {
		super(ScreenMode.NAV_MF_MACROS, r);
	}

	@Override
	public void actionEntr() {
		if(robotRun.isShift()) {
			RoboticArm r = robotRun.getActiveRobot();
			int idx = contents.getCurrentItemIdx();
			System.out.println(idx);
			robotRun.execute(r.getMacro(idx));
		}
	}

	@Override
	protected void loadContents() {
		contents.setLines(loadManualFunct());
	}

	@Override
	protected String loadHeader() {
		return "EXECUTE MANUAL FUNCTION";
	}

	@Override
	protected void loadLabels() {
		labels[0] = "";
		labels[1] = "";
		labels[2] = "";
		labels[3] = "";
		labels[4] = "";
	}
}
