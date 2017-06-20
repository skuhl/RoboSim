package screen;

import java.util.ArrayList;

import core.RobotRun;
import enums.CoordFrame;
import enums.ScreenMode;
import robot.RoboticArm;
import ui.DisplayLine;

public class ScreenTeach3PtTool extends ST_ScreenTeachPoints {

	public ScreenTeach3PtTool(RobotRun r) {
		super(ScreenMode.TEACH_3PT_TOOL, r);
	}

	@Override
	String loadHeader() {
		return String.format("TOOL %d: 3P METHOD", robotRun.curFrameIdx + 1);
	}

	@Override
	void loadContents() {
		RoboticArm r = robotRun.getActiveRobot();
		contents.setLines(robotRun.loadFrameDetail(r, CoordFrame.TOOL, robotRun.curFrameIdx));
	}

	@Override
	void loadOptions() {
		RoboticArm r = robotRun.getActiveRobot();
		ArrayList<DisplayLine> lines = 
				robotRun.loadPointList(r.getToolFrame(robotRun.curFrameIdx), 0);
		options.setLines(lines);
	}

	@Override
	public void actionEntr() {
		robotRun.createFrame(robotRun.teachFrame, 0);
		robotRun.lastScreen();
	}
	
	
}
