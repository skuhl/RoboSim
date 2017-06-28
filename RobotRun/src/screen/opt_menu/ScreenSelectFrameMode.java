package screen.opt_menu;

import core.RobotRun;
import screen.ScreenMode;
import screen.ScreenState;

public class ScreenSelectFrameMode extends ST_ScreenOptionsMenu {

	public ScreenSelectFrameMode(ScreenState prevState, RobotRun r) {
		super(ScreenMode.SELECT_FRAME_MODE, prevState, r);
	}

	@Override
	protected String loadHeader() {
		return "FRAME MODE";
	}

	@Override
	protected void loadOptions() {
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
