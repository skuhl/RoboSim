package screen.opt_menu;

import core.RobotRun;
import enums.CoordFrame;
import global.DataManagement;
import robot.RoboticArm;
import screen.ScreenMode;
import screen.ScreenState;

public class ScreenSetDefaultTooltip extends ST_ScreenOptionsMenu {

	public ScreenSetDefaultTooltip(ScreenState prevState, RobotRun r) {
		super(ScreenMode.SET_DEF_TOOLTIP, prevState, r);
	}

	@Override
	protected String loadHeader() {
		return "DEFAULT TOOL TIPS";
	}
	
	@Override
	protected void loadContents() {
		RoboticArm r = robotRun.getActiveRobot();
		contents.setLines(robotRun.loadFrameDetail(r, CoordFrame.TOOL, robotRun.curFrameIdx));
	}

	@Override
	protected void loadOptions() {
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
