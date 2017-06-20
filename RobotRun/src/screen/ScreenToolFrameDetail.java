package screen;

import core.RobotRun;
import enums.CoordFrame;
import enums.ScreenMode;
import robot.RoboticArm;

public class ScreenToolFrameDetail extends ST_ScreenOptionsMenu {

	public ScreenToolFrameDetail(RobotRun r) {
		super(ScreenMode.TFRAME_DETAIL, r);
	}

	@Override
	String loadHeader() {
		return String.format("TOOL %d: DETAIL", robotRun.curFrameIdx + 1);
	}

	@Override
	void loadContents() {
		RoboticArm r = robotRun.getActiveRobot();
		contents.setLines(robotRun.loadFrameDetail(r, CoordFrame.TOOL, robotRun.curFrameIdx));
	}
	
	@Override
	void loadOptions() {
		options.addLine("1. Three Point Method");
		options.addLine("2. Six Point Method");
		options.addLine("3. Direct Entry Method");
	}
	
	@Override
	void loadLabels() {
		// F1, F2
		labels[0] = "[Rename]";
		labels[1] = "[Method]";
		labels[2] = "";
		labels[3] = "";
		labels[4] = "[Default]";
	}

	@Override
	public void actionEntr() {
		robotRun.teachFrame = robotRun.getActiveRobot().getToolFrame(robotRun.curFrameIdx);
		// Tool Frame teaching methods
		if (options.getLineIdx() == 0) {
			robotRun.nextScreen(ScreenMode.TEACH_3PT_TOOL);
		} else if (options.getLineIdx() == 1) {
			robotRun.nextScreen(ScreenMode.TEACH_6PT);
		} else if (options.getLineIdx() == 2) {
			robotRun.nextScreen(ScreenMode.DIRECT_ENTRY_TOOL);
		}
	}

	@Override
	public void actionF1() {
		robotRun.nextScreen(ScreenMode.TFRAME_RENAME);
	}
	
	@Override
	public void actionF5() {
		// Set a default tool tip for the selected tool frame
		robotRun.nextScreen(ScreenMode.SET_DEF_TOOLTIP);
	}
}
