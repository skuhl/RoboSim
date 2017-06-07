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
public class LinearJog extends LinearMotion {
	
	/**
	 * The robot's translation motion along the frame axes.
	 */
	private PVector translation;
	
	/**
	 * The robot's rotation motion about the frame axes.
	 */
	private PVector rotation;
	
	/**
	 * Initialize motion vectors to all zeros and motion fault to false.
	 */
	public LinearJog() {
		super();
		translation = new PVector();
		rotation = new PVector();
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
			// An issue occurred with inverse kinematics
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
	
	public int getMotion(int mdx) {
		if (mdx == 0) {
			return (int)translation.x;
			
		} else if (mdx == 1) {
			return (int)translation.y;
			
		} else if (mdx == 2) {
			return (int)translation.z;
			
		} else if (mdx == 3) {
			return (int)rotation.x;
			
		} else if (mdx == 4) {
			return (int)rotation.y;
			
		} else if (mdx == 5) {
			return (int)rotation.z;
			
		} else {
			return 0;
		}
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
	
	@Override
	public boolean hasMotion() {
		return !hasFault() && (translation.x != 0f || translation.y != 0f
				|| translation.z != 0f && rotation.x != 0f
				|| rotation.y != 0f || rotation.z != 0f);
	}
	
	public int setMotion(int mdx, int newDir) {
		int curDir = getMotion(mdx);
		
		if (curDir == newDir) {
			newDir = 0;
		}
		
		if (mdx == 0) {
			translation.x = newDir;
			
		} else if (mdx == 1) {
			translation.y = newDir;
			
		} else if (mdx == 2) {
			translation.z = newDir;
			
		} else if (mdx == 3) {
			rotation.x = newDir;
			
		} else if (mdx == 4) {
			rotation.y = newDir;
			
		} else if (mdx == 5) {
			rotation.z = newDir;	
		}
		
		return newDir;
	}
}