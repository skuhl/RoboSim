package screen.teach_frame;

import java.util.ArrayList;

import core.RobotRun;
import enums.CoordFrame;
import enums.ScreenMode;
import robot.RoboticArm;
import ui.DisplayLine;

public class ScreenTeach3PtUser extends ST_ScreenTeachPoints {

	public ScreenTeach3PtUser(RobotRun r) {
		super(ScreenMode.TEACH_3PT_USER, r);
	}

	@Override
	protected String loadHeader() {
		return String.format("USER %d: 3P METHOD", robotRun.curFrameIdx + 1);
	}

	@Override
	protected void loadContents() {
		RoboticArm r = robotRun.getActiveRobot();
		contents.setLines(robotRun.loadFrameDetail(r, CoordFrame.USER, robotRun.curFrameIdx));
	}

	@Override
	protected void loadOptions() {
		RoboticArm r = robotRun.getActiveRobot();
		ArrayList<DisplayLine> lines = 
				robotRun.loadPointList(r.getUserFrame(robotRun.curFrameIdx), 0);
		options.setLines(lines);
	}

	@Override
	public void actionEntr() {
		robotRun.createFrame(robotRun.teachFrame, 0);
		robotRun.lastScreen();
	}

}
