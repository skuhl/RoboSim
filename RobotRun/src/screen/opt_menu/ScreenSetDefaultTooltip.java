package screen.opt_menu;

import core.RobotRun;
import enums.CoordFrame;
import frame.ToolFrame;
import global.DataManagement;
import processing.core.PVector;
import robot.RoboticArm;
import screen.ScreenMode;

public class ScreenSetDefaultTooltip extends ST_ScreenOptionsMenu {
	
	private ToolFrame selectedFrame;
	
	public ScreenSetDefaultTooltip(RobotRun r, ToolFrame tFrame) {
		super(ScreenMode.SET_DEF_TOOLTIP, r);
		selectedFrame = tFrame;
	}

	@Override
	protected String loadHeader() {
		return "DEFAULT TOOL TIPS";
	}
	
	@Override
	protected void loadContents() {
		RoboticArm r = robotRun.getActiveRobot();
		contents.setLines(loadFrameDetail(selectedFrame));
	}

	@Override
	protected void loadOptions() {
		options.setLines(loadEEToolTipDefaults(robotRun.getActiveRobot()));
	}

	@Override
	public void actionEntr() {
		RoboticArm r = robotRun.getActiveRobot();
		// Set the offset of the frame to the specified default tool tip
		PVector defToolTip = r.getToolTipDefault(options.getLineIdx());
		selectedFrame.setTCPOffset(defToolTip.copy());
		DataManagement.saveRobotData(robotRun.getActiveRobot(), 2);
		robotRun.lastScreen();
	}
}
