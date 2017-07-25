package screen.opt_menu;

import core.RobotRun;
import robot.RoboticArm;
import screen.ScreenMode;

public class ScreenUserFrameDetail extends ST_ScreenOptionsMenu {
	
	private int frameIdx;
	
	public ScreenUserFrameDetail(RobotRun r, int frameIdx) {
		super(ScreenMode.UFRAME_DETAIL, String.format("USER: %d DETAIL",
				frameIdx + 1), r);
		this.frameIdx = frameIdx;
	}
	
	public int getFrameIdx() {
		return frameIdx;
	}

	@Override
	protected String loadHeader() {
		return "";
	}

	@Override
	protected void loadContents() {
		RoboticArm r = robotRun.getActiveRobot();
		contents.setLines(loadFrameDetail(r.getUserFrame(frameIdx)));
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
