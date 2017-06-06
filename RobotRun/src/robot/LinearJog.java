package robot;

import enums.CoordFrame;
import frame.ToolFrame;
import frame.UserFrame;
import geom.Point;
import geom.RQuaternion;
import global.RMath;
import processing.core.PConstants;
import processing.core.PVector;

/**
 * Defines the robot's jog motion in the world, tool or user frames.
 * 
 * @author Joshua Hooker
 */
public class LinearJog implements RobotMotion {
	
	/**
	 * The robot's translation motion along the frame axes.
	 */
	private PVector translation;
	
	/**
	 * The robot's rotation motion about the frame axes.
	 */
	private PVector rotation;
	
	/**
	 * Has the motion incurred a fault by some means (i.e. inverse kinematics
	 * failure).
	 */
	private boolean motionFault;
	
	/**
	 * Initialize motion vectors to all zeros and motion fault to false.
	 */
	public LinearJog() {
		translation = new PVector();
		rotation = new PVector();
		motionFault = false;
	}
	
	@Override
	public int executeMotion(RoboticArm robot) {
		Point curPoint = robot.getToolTipNative();
		RQuaternion invFrameOrientation = null;
		
		PVector tgtPosition = curPoint.position.copy();
		RQuaternion tgtOrientation = curPoint.orientation.clone();
		// Find the inverse of the active frame's orientation
		if (robot.getCurCoordFrame() == CoordFrame.TOOL) {
			ToolFrame activeTool = robot.getActiveTool();
			
			if (activeTool != null) {
				Point defPt = robot.getDefaultPoint();
				RQuaternion diff = curPoint.orientation.transformQuaternion(
						defPt.orientation.conjugate()
				);
				
				invFrameOrientation = diff.transformQuaternion(
						activeTool.getOrientationOffset().clone()
				).conjugate();
			}
			
		} else if (robot.getCurCoordFrame() == CoordFrame.USER) {
			UserFrame activeUser = robot.getActiveUser();
			
			if (activeUser != null) {
				invFrameOrientation = activeUser.getOrientation().conjugate();
			}
		}

		// Apply translational motion vector
		if (translation.mag() > 0f) {
			// Respond to user defined movement
			float distance = robot.motorSpeed / 6000f * robot.getLiveSpeed();
			PVector translation = RMath.vFromWorld(this.translation);
			translation.mult(distance);

			if (invFrameOrientation != null) {
				// Convert the movement vector into the current reference frame
				translation = invFrameOrientation.rotateVector(translation);
			}

			tgtPosition.add(translation);
		} else {
			// No translational motion
			tgtPosition = curPoint.position;
		}

		// Apply rotational motion vector
		if (rotation.mag() > 0f) {
			// Respond to user defined movement
			float theta = PConstants.DEG_TO_RAD * 0.025f * robot.getLiveSpeed();
			PVector rotation = RMath.vFromWorld(this.rotation);

			if (invFrameOrientation != null) {
				// Convert the movement vector into the current reference frame
				rotation = invFrameOrientation.rotateVector(rotation);
			}
			rotation.normalize();

			tgtOrientation.rotateAroundAxis(rotation, theta);

			if (tgtOrientation.dot(curPoint.orientation) < 0f) {
				// Use -q instead of q
				tgtOrientation.scalarMult(-1);
			}
		} else {
			// No rotational motion
			tgtOrientation = curPoint.orientation;
		}

		int ret = robot.jumpTo(tgtPosition, tgtOrientation);
		
		if (ret == 1) {
			// An issue occured with inverse kinematics
			setFault(true);
		}
		
		return ret;
	}
	
	/**
	 * Creates a copy of the directions of linear motion defined by this
	 * object.
	 * 
	 * @return	A copy of the joint motion directions
	 */
	public int[] getJogMotion() {
		
		return new int[] {
			(int)translation.x,
			(int)translation.y,
			(int)translation.z,
			(int)rotation.x,
			(int)rotation.y,
			(int)rotation.z,
		};
	}

	@Override
	public void halt() {
		translation.x = 0f;
		translation.y = 0f;
		translation.z = 0f;
		
		rotation.x = 0f;
		rotation.y = 0f;
		rotation.z = 0f;
	}
	
	public boolean hasFault() {
		return motionFault;
	}
	
	@Override
	public boolean inMotion() {
		return !hasFault() && translation.x == 0f && translation.y == 0f
				&& translation.z == 0f && rotation.x == 0f
				&& rotation.y == 0f && rotation.z == 0f;
	}
	
	public void setFault(boolean newState) {
		motionFault = newState;
		
		if (newState) {
			// Stop motion on fault
			halt();
		}
	}
}
