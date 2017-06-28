package screen.opt_menu;

import core.RobotRun;
import enums.CoordFrame;
import robot.RoboticArm;
import screen.ScreenMode;
import screen.ScreenState;

public class ScreenUserFrameDetail extends ST_ScreenOptionsMenu {

	public ScreenUserFrameDetail(RobotRun r) {
		super(ScreenMode.UFRAME_DETAIL, r);
	}

	@Override
	protected String loadHeader() {
		return String.format("USER %d: DETAIL", robotRun.curFrameIdx + 1);
	}

	@Override
	protected void loadContents() {
		RoboticArm r = robotRun.getActiveRobot();
		contents.setLines(robotRun.loadFrameDetail(r, CoordFrame.USER, robotRun.curFrameIdx));
	}
	
	@Override
	protected void loadOptions() {
		options.addLine("1. Three Point Method");
		options.addLine("2. Four Point Method");
		options.addLine("3. Direct Entry Method");
	}
	
	@Override
	protected void loadLabels() {
		// F1, F2
		labels[0] = "[Rename]";
		labels[1] = "[Method]";
		labels[2] = "";
		labels[3] = "";
		labels[4] = "";
	}

	@Override
	public void actionEntr() {
		// User Frame teaching methods
		robotRun.teachFrame = robotRun.getActiveRobot().getUserFrame(robotRun.curFrameIdx);
		if (options.getLineIdx() == 0) {
			robotRun.nextScreen(ScreenMode.TEACH_3PT_USER);
		} else if (options.getLineIdx() == 1) {
			robotRun.nextScreen(ScreenMode.TEACH_4PT);
		} else if (options.getLineIdx() == 2) {
			robotRun.nextScreen(ScreenMode.DIRECT_ENTRY_USER);
		}
	}

	@Override
	public void actionF1() {
		robotRun.nextScreen(ScreenMode.UFRAME_RENAME);
	}
}
