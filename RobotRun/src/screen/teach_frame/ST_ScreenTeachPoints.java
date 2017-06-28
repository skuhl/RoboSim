package screen.teach_frame;

import core.RobotRun;
import geom.Point;
import global.DataManagement;
import robot.RoboticArm;
import screen.Screen;
import screen.ScreenMode;
import screen.ScreenState;

public abstract class ST_ScreenTeachPoints extends Screen {

	public ST_ScreenTeachPoints(ScreenMode m, ScreenState prevState, RobotRun r) {
		super(m, prevState, r);
	}
	
	@Override
	protected void loadLabels() {
		// F1, F5
		labels[0] = "";
		labels[1] = "[Method]";
		labels[2] = "";
		labels[3] = "[Mov To]";
		labels[4] = "[Record]";
	}

	@Override
	protected void loadVars(ScreenState s) {
		setScreenIndices(0, 0, 0, 0, 0);
	}
	
	@Override
	public void actionKeyPress(char key) {}

	@Override
	public void actionUp() {
		options.moveUp(false);
	}

	@Override
	public void actionDn() {
		options.moveDown(false);
	}

	@Override
	public void actionLt() {}

	@Override
	public void actionRt() {}
	
	@Override
	public void actionBkspc() {}

	@Override
	public void actionF1() {}

	@Override
	public void actionF2() {
		robotRun.lastScreen();
	}

	@Override
	public void actionF3() {}

	@Override
	public void actionF4() {
		if (robotRun.isShift() && robotRun.teachFrame != null) {
			Point tgt = robotRun.teachFrame.getPoint(options.getLineIdx());

			if (mode == ScreenMode.TEACH_3PT_USER || mode == ScreenMode.TEACH_4PT) {
				if (tgt != null && tgt.position != null && tgt.orientation != null) {
					// Move to the point's position and orientation
					robotRun.getActiveRobot().updateMotion(tgt);
				}
			} else {
				if (tgt != null && tgt.angles != null) {
					// Move to the point's joint angles
					robotRun.getActiveRobot().updateMotion(tgt.angles);
				}
			}
		}
	}

	@Override
	public void actionF5() {
		if (robotRun.isShift()) {
			// Save the Robot's current position and joint angles
			RoboticArm r = robotRun.getActiveRobot();
			Point pt;
			
			if (mode == ScreenMode.TEACH_3PT_USER || mode == ScreenMode.TEACH_4PT) {
				pt = r.getToolTipNative();
				
			} else {
				pt = r.getFacePlatePoint();
			}

			robotRun.teachFrame.setPoint(pt, options.getLineIdx());
			DataManagement.saveRobotData(r, 2);
			robotRun.updatePendantScreen();
		}
	}
}
