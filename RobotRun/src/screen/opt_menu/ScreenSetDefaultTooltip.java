package screen.opt_menu;

import core.RobotRun;
import frame.ToolFrame;
import global.DataManagement;
import processing.core.PVector;
import robot.RoboticArm;
import screen.ScreenMode;

public class ScreenSetDefaultTooltip extends ST_ScreenOptionsMenu {
	
	private int frameIdx;
	
	public ScreenSetDefaultTooltip(RobotRun r, int frameIdx) {
		super(ScreenMode.SET_DEF_TOOLTIP, String.format("TOOL: %d DEFAULT",
				frameIdx + 1), r);
		this.frameIdx = frameIdx;
	}

	@Override
	public void actionEntr() {
		RoboticArm r = robotRun.getActiveRobot();
		ToolFrame selectedFrame = r.getToolFrame(frameIdx);
		PVector defToolTip = r.getToolTipDefault(options.getLineIdx());
		
		// Set the offset of the frame to the specified default tool tip
		selectedFrame.setTCPOffset(defToolTip.copy());
		DataManagement.saveRobotData(robotRun.getActiveRobot(), 2);
		
		robotRun.lastScreen();
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
	protected void loadOptions() {
		options.setLines(loadEEToolTipDefaults(robotRun.getActiveRobot()));
	}
}
