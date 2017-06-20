package screen;

import java.util.ArrayList;

import core.RobotRun;
import enums.CoordFrame;
import enums.ScreenMode;
import robot.RoboticArm;
import ui.DisplayLine;

public class ScreenTeach4Pt extends ST_ScreenTeachPoints {

	public ScreenTeach4Pt(RobotRun r) {
		super(ScreenMode.TEACH_4PT, r);
	}

	@Override
	String loadHeader() {
		return String.format("USER %d: 4P METHOD", robotRun.curFrameIdx + 1);
	}

	@Override
	void loadContents() {
		RoboticArm r = robotRun.getActiveRobot();
		contents.setLines(robotRun.loadFrameDetail(r, CoordFrame.USER, robotRun.curFrameIdx));
	}

	@Override
	void loadOptions() {
		RoboticArm r = robotRun.getActiveRobot();
		ArrayList<DisplayLine> lines = 
				robotRun.loadPointList(r.getUserFrame(robotRun.curFrameIdx), 1);
		options.setLines(lines);
	}

	@Override
	public void actionEntr() {
		robotRun.createFrame(robotRun.teachFrame, 1);
		robotRun.lastScreen();
	}

}
