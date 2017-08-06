package screen.opt_menu;

import core.RobotRun;
import robot.RoboticArm;
import screen.ScreenMode;

public class ScreenToolFrameDetail extends ST_ScreenOptionsMenu {
	
	private int frameIdx;
	
	public ScreenToolFrameDetail(RobotRun r, int frameIdx) {
		super(ScreenMode.TFRAME_DETAIL, String.format("TOOL: %d DETAIL",
				frameIdx + 1), r);
		this.frameIdx = frameIdx;
	}
	
	@Override
	public void actionEntr() {
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
	
	public int getFrameIdx() {
		return frameIdx;
	}
	
	@Override
	protected void loadContents() {
		RoboticArm r = robotRun.getActiveRobot();
		contents.setLines(loadFrameDetail(r.getToolFrame(frameIdx)));
	}

	@Override
	protected String loadHeader() {
		return "";
	}

	@Override
	protected void loadLabels() {
		// F1, F2
		labels[0] = "[Rename]";
		labels[1] = "[Method]";
		labels[2] = "";
		labels[3] = "";
		labels[4] = "[Default]";
	}
	
	@Override
	protected void loadOptions() {
		options.addLine("1. Three Point Method");
		options.addLine("2. Six Point Method");
		options.addLine("3. Direct Entry Method");
	}
}
