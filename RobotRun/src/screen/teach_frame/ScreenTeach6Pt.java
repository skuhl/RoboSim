package screen.teach_frame;

import java.util.ArrayList;

import core.RobotRun;
import enums.CoordFrame;
import enums.ScreenMode;
import robot.RoboticArm;
import ui.DisplayLine;

public class ScreenTeach6Pt extends ST_ScreenTeachPoints {

	public ScreenTeach6Pt(RobotRun r) {
		super(ScreenMode.TEACH_6PT, r);
	}

	@Override
	protected String loadHeader() {
		return String.format("TOOL %d: 6P METHOD", robotRun.curFrameIdx + 1);
	}

	@Override
	protected void loadContents() {
		RoboticArm r = robotRun.getActiveRobot();
		contents.setLines(robotRun.loadFrameDetail(r, CoordFrame.TOOL, robotRun.curFrameIdx));
	}

	@Override
	protected void loadOptions() {
		RoboticArm r = robotRun.getActiveRobot();
		ArrayList<DisplayLine> lines = 
				robotRun.loadPointList(r.getToolFrame(robotRun.curFrameIdx), 1);
		options.setLines(lines);
	}

	@Override
	public void actionEntr() {
		robotRun.createFrame(robotRun.teachFrame, 1);
		robotRun.lastScreen();
	}

}
