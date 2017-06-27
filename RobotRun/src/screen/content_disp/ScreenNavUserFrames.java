package screen.content_disp;

import core.RobotRun;
import enums.CoordFrame;
import screen.ScreenMode;

public class ScreenNavUserFrames extends ST_ScreenListContents {

	public ScreenNavUserFrames(RobotRun r) {
		super(ScreenMode.NAV_USER_FRAMES, r);
	}

	protected @Override
	String loadHeader() {
		return "USER FRAMES";
	}

	@Override
	protected void loadContents() {
		contents.setLines(robotRun.loadFrames(robotRun.getActiveRobot(), CoordFrame.USER));
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
		robotRun.nextScreen(ScreenMode.UFRAME_DETAIL);
	}

	@Override
	public void actionF1() {
		int frame = contents.getCurrentItem().getItemIdx();
		
		if (robotRun.isShift()) {
			// Reset the highlighted frame in the user frames list
			robotRun.getActiveRobot().getUserFrame(frame).reset();
			robotRun.updatePendantScreen();
		} else {
			// Set the current user frame
			robotRun.getActiveRobot().setActiveUserFrame(frame);
		}
	}
	
	@Override
	public void actionF3() {
		robotRun.switchScreen(ScreenMode.NAV_TOOL_FRAMES);
	}
}
