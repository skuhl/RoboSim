package screen.content_disp;

import core.RobotRun;
import enums.CoordFrame;
import screen.ScreenMode;
import screen.ScreenState;

public class ScreenNavToolFrames extends ST_ScreenListContents {

	public ScreenNavToolFrames(ScreenState prevState, RobotRun r) {
		super(ScreenMode.NAV_TOOL_FRAMES, prevState, r);
	}

	@Override
	protected String loadHeader() {
		return "TOOL FRAMES";
	}

	@Override
	protected void loadContents() {
		contents.setLines(robotRun.loadFrames(robotRun.getActiveRobot(), CoordFrame.TOOL));
	}

	@Override
	protected void loadLabels() {
		// F1, F2, F3
		if (robotRun.isShift()) {
			labels[0] = "[Clear]";
			labels[1] = "";
			labels[2] = "[Switch]";
			labels[3] = "";
			labels[4] = "";
		} else {
			labels[0] = "[Set]";
			labels[1] = "";
			labels[2] = "[Switch]";
			labels[3] = "";
			labels[4] = "";
		}
	}

	@Override
	public void actionEntr() {
		robotRun.curFrameIdx = contents.getCurrentItemIdx();
		robotRun.nextScreen(ScreenMode.TFRAME_DETAIL);
	}

	@Override
	public void actionF1() {
		int frame = contents.getCurrentItem().getItemIdx();
		
		if (robotRun.isShift()) {
			// Reset the highlighted frame in the tool frame list
			robotRun.getActiveRobot().getToolFrame(frame).reset();
			robotRun.updatePendantScreen();
		} else {
			// Set the current tool frame
			robotRun.getActiveRobot().setActiveToolFrame(frame);
		}
	}
	
	@Override
	public void actionF3() {
		robotRun.switchScreen(ScreenMode.NAV_USER_FRAMES);
	}
}
