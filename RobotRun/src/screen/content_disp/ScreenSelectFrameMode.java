package screen.content_disp;

import core.RobotRun;
import screen.ScreenMode;

public class ScreenSelectFrameMode extends ST_ScreenListContents {

	public ScreenSelectFrameMode(RobotRun r) {
		super(ScreenMode.SELECT_FRAME_MODE, r);
	}

	@Override
	public void actionEntr() {
		if (contents.getLineIdx() == 0) {
			robotRun.nextScreen(ScreenMode.NAV_TOOL_FRAMES);
			
		} else if (contents.getLineIdx() == 1) {
			robotRun.nextScreen(ScreenMode.NAV_USER_FRAMES);
		}
	}
	
	@Override
	protected void loadContents() {
		contents.addLine("1. Tool Frame");
		contents.addLine("2. User Frame");
	}

	@Override
	protected String loadHeader() {
		return "FRAME MODE";
	}
}
