package screen;

import core.RobotRun;
import enums.CoordFrame;
import enums.ScreenMode;
import global.DataManagement;
import robot.RoboticArm;

public class ScreenSetDefaultTooltip extends ST_ScreenOptionsMenu {

	public ScreenSetDefaultTooltip(RobotRun r) {
		super(ScreenMode.SET_DEF_TOOLTIP, r);
	}

	@Override
	String loadHeader() {
		return "DEFAULT TOOL TIPS";
	}
	
	@Override
	void loadContents() {
		RoboticArm r = robotRun.getActiveRobot();
		contents.setLines(robotRun.loadFrameDetail(r, CoordFrame.TOOL, robotRun.curFrameIdx));
	}

	@Override
	void loadOptions() {
		options.setLines(robotRun.loadEEToolTipDefaults(robotRun.getActiveRobot()));
	}

	@Override
	public void actionEntr() {
		robotRun.getInactiveRobot().setDefToolTip(robotRun.curFrameIdx, options.getLineIdx());
		robotRun.getInactiveRobot().setActiveToolFrame(robotRun.curFrameIdx);
		DataManagement.saveRobotData(robotRun.getActiveRobot(), 2);
		robotRun.lastScreen();
	}
}
