package screen.teach_frame;

import core.RobotRun;
import geom.Point;
import global.DataManagement;
import global.Fields;
import processing.core.PGraphics;
import robot.RoboticArm;
import screen.Screen;
import screen.ScreenMode;
import screen.ScreenState;

public abstract class ST_ScreenTeachPoints extends Screen {
	
	protected int frameIdx;
	
	public ST_ScreenTeachPoints(ScreenMode m, RobotRun r, int frameIdx) {
		super(m, r);
		this.frameIdx = frameIdx;
	}
	
	public ST_ScreenTeachPoints(ScreenMode m, String header, RobotRun r,
			int frameIdx) {
		
		super(m, header, r);
		this.frameIdx = frameIdx;
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
		if (robotRun.isShift()) {
			Point tgt = getTeachPoint(options.getLineIdx());

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

			setTeachPoint(pt, options.getLineIdx());
			DataManagement.saveRobotData(r, 2);
			robotRun.updatePendantScreen();
		}
	}
	
	/**
	 * TODO comment this
	 * 
	 * @param g
	 */
	public abstract void drawTeachPts(PGraphics g);
	
	/**
	 * TODO comment this
	 * 
	 * @param idx
	 * @return
	 */
	protected static int getPtColorForTool(int idx) {
		
		if (idx < 3) {
			// TCP teach points
			return Fields.color(130, 130, 130);
			
		} else if (idx == 3) {
			// Orient origin point
			
			return Fields.color(255, 130, 0);
		} else if (idx == 4) {
			// Axes X-Direction point
			return Fields.color(255, 0, 0);
			
		} else if (idx == 5) {
			// Axes Y-Diretion point
			return Fields.color(0, 255, 0);
		}
		
		return Fields.BLACK;
	}
	
	/**
	 * TODO comment this
	 * 
	 * @param idx
	 * @return
	 */
	protected static int getPtColorForUser(int idx) {
		
		if (idx == 0) {
			// Orient origin point
			return Fields.color(255, 130, 0);
			
		} else if (idx == 1) {
			// Axes X-Diretion point
			return Fields.color(255, 0, 0);
			
		} else if (idx == 2) {
			// Axes Y-Diretion point
			return Fields.color(0, 255, 0);
			
		} else if (idx == 3) {
			// Axes Origin point
			return Fields.color(0, 0, 255);
		}
		
		return Fields.BLACK;
	}
	
	/**
	 * TODO comment this
	 * 
	 * @param idx
	 */
	protected abstract Point getTeachPoint(int idx);
	
	/**
	 * TODO comment this
	 * 
	 * @return
	 */
	public abstract boolean readyToTeach();
	
	/**
	 * TODO comment this
	 * 
	 * @param pt
	 * @param idx
	 */
	protected abstract void setTeachPoint(Point pt, int idx);
}
