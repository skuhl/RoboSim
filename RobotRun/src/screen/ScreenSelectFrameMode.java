package screen;

import core.RobotRun;
import enums.ScreenMode;

public class ScreenSelectFrameMode extends ST_ScreenOptionsMenu {

	public ScreenSelectFrameMode(RobotRun r) {
		super(ScreenMode.SELECT_FRAME_MODE, r);
	}

	@Override
	String loadHeader() {
		return "FRAME MODE";
	}

	@Override
	void loadOptions() {
		options.addLine("1. Tool Frame");
		options.addLine("2. User Frame");
	}

	@Override
	public void actionEntr() {
		if (options.getLineIdx() == 0) {
			robotRun.nextScreen(ScreenMode.NAV_TOOL_FRAMES);
		} else if (options.getLineIdx() == 1) {
			robotRun.nextScreen(ScreenMode.NAV_USER_FRAMES);
		}
	}
}
