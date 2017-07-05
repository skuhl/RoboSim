package screen.opt_menu;

import core.RobotRun;
import enums.CoordFrame;
import global.DataManagement;
import robot.RoboticArm;
import screen.ScreenMode;

public class ScreenSetDefaultTooltip extends ST_ScreenOptionsMenu {

	public ScreenSetDefaultTooltip(RobotRun r) {
		super(ScreenMode.SET_DEF_TOOLTIP, r);
	}

	@Override
	protected String loadHeader() {
		return "DEFAULT TOOL TIPS";
	}
	
	@Override
	protected void loadContents() {
		RoboticArm r = robotRun.getActiveRobot();
		contents.setLines(loadFrameDetail(r, CoordFrame.TOOL, robotRun.curFrameIdx));
	}

	@Override
	protected void loadOptions() {
		options.setLines(loadEEToolTipDefaults(robotRun.getActiveRobot()));
	}

	@Override
	public void actionEntr() {
		robotRun.getInactiveRobot().setDefToolTip(robotRun.curFrameIdx, options.getLineIdx());
		robotRun.getInactiveRobot().setActiveToolFrame(robotRun.curFrameIdx);
		DataManagement.saveRobotData(robotRun.getActiveRobot(), 2);
		robotRun.lastScreen();
	}
}
