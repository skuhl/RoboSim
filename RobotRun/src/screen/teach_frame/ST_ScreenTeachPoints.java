package screen.teach_frame;

import core.RobotRun;
import geom.Point;
import global.Fields;
import io.DataManagement;
import processing.core.PGraphics;
import robot.RoboticArm;
import screen.Screen;
import screen.ScreenMode;
import screen.ScreenState;

public abstract class ST_ScreenTeachPoints extends Screen {
	
	/**
	 * Returns the color of a teach point for a tool frame based on the given
	 * index value:
	 * 
	 * 0-2	Gray	tool tip offset
	 * 3	Orange	orient origin
	 * 4	Red		X direction
	 * 5	Green	Y direction
	 * 
	 * @param idx	The index of a teach point for a tool frame
	 * @return		The color of the point rendered for the given index
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
	 * Returns the color of a teach point for a user frame based on the given
	 * index value:
	 * 
	 * 0	Orange	orient origin
	 * 1	Red		X direction
	 * 2	Green	Y direction
	 * 3	Blue	frame origin
	 * 
	 * @param idx	The index of a teach point for a user frame
	 * @return		The color of the point rendered for the given index
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
	 * The index of the frame, with respect to the active robot's set of frame,
	 * being taught.
	 */
	protected int frameIdx;
	
	public ST_ScreenTeachPoints(ScreenMode m, RobotRun r, int frameIdx) {
		super(m, r);
		this.frameIdx = frameIdx;
	}
	
	public ST_ScreenTeachPoints(ScreenMode m, RobotRun r, int cMax, int cX,
			int cY, int oMax, int oX, int oY, int frameIdx) {
		
		super(m, r, cMax, cX, cY, oMax, oX, oY);
		this.frameIdx = frameIdx;
	}
	
	public ST_ScreenTeachPoints(ScreenMode m, String header, RobotRun r,
			int frameIdx) {
		
		super(m, header, r);
		this.frameIdx = frameIdx;
	}
	
	public ST_ScreenTeachPoints(ScreenMode m, String header, RobotRun r,
			int cMax, int cX, int cY, int oMax, int oX, int oY, int frameIdx) {
		
		super(m, header, r, cMax, cX, cY, oMax, oX, oY);
		this.frameIdx = frameIdx;
	}
	
	@Override
	public void actionArrowDn() {
		options.moveDown(false);
	}

	@Override
	public void actionArrowLt() {}

	@Override
	public void actionArrowRt() {}

	@Override
	public void actionArrowUp() {
		options.moveUp(false);
	}

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

	@Override
	public void actionKeyPress(char key) {}
	
	/**
	 * Draws the points for the frame's selected teaching method. The frame's
	 * teaching method should be defined based on the screen mode.
	 * 
	 * @param g	The graphics object used to render the teach points
	 */
	public abstract void drawTeachPts(PGraphics g);
	
	/**
	 * Determines if all the points have been taught to the selected frame for
	 * the selected teaching method.
	 * 
	 * @return	If the frame has the necessary points to be taught
	 */
	public abstract boolean readyToTeach();
	
	/**
	 * Returns the point taught to the associated frame for the teaching method
	 * associated with this screen. The mapping between indices and points
	 * varies depending the teaching method and frame type, however, the
	 * maximum range of indices is 0 to 6, inclusive.
	 * 
	 * @param idx	The index of the teach point to get
	 */
	protected abstract Point getTeachPoint(int idx);
	
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
	
	/**
	 * Sets the teach point associated with the given index, if the given index
	 * is valid. See getTeachPoint() method. 
	 * 
	 * @param pt	The new teach point
	 * @param idx	The index associated with the teach point
	 */
	protected abstract void setTeachPoint(Point pt, int idx);
}
