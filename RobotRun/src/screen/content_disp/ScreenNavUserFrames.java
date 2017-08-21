package screen.content_disp;

import core.RobotRun;
import enums.CoordFrame;
import frame.UserFrame;
import robot.RoboticArm;
import screen.ScreenMode;
import screen.ScreenState;

public class ScreenNavUserFrames extends ST_ScreenListContents {

	public ScreenNavUserFrames(RobotRun r) {
		super(ScreenMode.NAV_USER_FRAMES, r);
	}

	@Override
	public void actionEntr() {
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
		robotRun.switchScreen(ScreenMode.NAV_TOOL_FRAMES, false);
	}

	@Override
	protected void loadContents() {
		contents.setLines(loadFrames(robotRun.getActiveRobot(), CoordFrame.USER));
	}

	protected @Override
	String loadHeader() {
		return "USER FRAMES";
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
	protected void loadOptions() {
		RoboticArm r = robotRun.getActiveRobot();
		int itemIdx = contents.getCurrentItemIdx();
		UserFrame frame = r.getUserFrame(itemIdx);
		
		if (frame != null) {
			// Load frame detail for the frame associated with the selected line
			options.setLines(loadFrameDetail(frame));
		}
	}
	
	@Override
	protected void loadVars(ScreenState s) {
		setScreenIndices(0, 0, 0, -1, 0);
	}
}
