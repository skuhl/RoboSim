package robot;

import global.RMath;
import processing.core.PApplet;
import processing.core.PConstants;

/**
 * Defines the robot's target joint motion, which uses rotational interpolation
 * to move from the robot's current joint angles to the target angles.
 * 
 * @author Joshua Hooker
 */
public class JointInterpolation extends JointMotion {
	
	/**
	 * Is the speed of the this motion directly linked to live speed of the
	 * robot?
	 */
	private boolean linkToRobotSpd;
	
	/**
	 *  The overall speed modifier for the rotational interpolation.
	 */
	private float speed;
	
	/**
	 * The joint angles, to which to move.
	 */
	private final float[] TGT_ANGLES;
	
	/**
	 * Defines a new joint interpolation motion associated with given robot and
	 * with the given destination joint angles.
	 * 
	 * @param robot		The robot, with which this motion is associated
	 * @param tgtAngles	The target joint angles for this interpolation
	 */
	public JointInterpolation(RoboticArm robot, float[] tgtAngles) {
		TGT_ANGLES = new float[6];
		setupRotationalInterpolation(robot, tgtAngles);
	}
	
	/**
	 * Sets the target joint angles and joint motion directions for the
	 * rotational interpolation. In addition, the speed modifiers for each of
	 * the robot's segments is updated in such a way, that each joint will
	 * finish interpolation at the same time.
	 * 
	 * @param robot		The robot. for which this motion is defined
	 * @param tgtAngles	The target joint angles, to which to interpolate
	 * @param speed		The initial speed of motion
	 */
	public JointInterpolation(RoboticArm robot, float[] tgtAngles, float speed) {
		TGT_ANGLES = new float[6];
		setupRotationalInterpolation(robot, tgtAngles, speed);
	}
	
	@Override
	public int executeMotion(RoboticArm robot) {
		// Count how many joints are still in motion
		int ret = 0;
		
		if (linkToRobotSpd) {
			// Update the speed of motion
			speed = robot.getLiveSpeed() / 100f;
		}
		
		for(int jdx = 0; jdx < 6; ++jdx) {
			RSegWithJoint seg = robot.getSegment(jdx);
			
			if (JOINT_MOTION[jdx] != 0) {
				float distToDest = PApplet.abs(seg.getJointRotation()
						- TGT_ANGLES[jdx]);
				float deltaAngle = seg.getSpeedModifier() * speed;
				
				if (deltaAngle > 0.000009f && distToDest >= deltaAngle) {
					++ret;
					/* Move the joint based on the defined speed of motion, the
					 * direction of motion and the segment's speed modifier */
					float newRotation = RMath.mod2PI(seg.getJointRotation()
							+ JOINT_MOTION[jdx] * deltaAngle);
					
					seg.setJointRotation(newRotation);

				} else {
					if (distToDest > 0.000009f) {
						// Destination too close to move at current speed
						seg.setJointRotation(TGT_ANGLES[jdx]);
						
					} else {
						JOINT_MOTION[jdx] = 0;
					}
				}
			}
		}
		
		return ret;
	}
	
	/**
	 * Indicates whether this motion's speed will change based on the robotic
	 * arm's current live speed or if it will remain constant.
	 * 
	 * @return	Is the speed of this motion based on the robotic arm's current
	 * 			live speed?
	 */
	public boolean isSpdLinkedToRobot() {
		return linkToRobotSpd;
	}
	
	/**
	 * Links/separates this motion's speed with/from the live speed of the
	 * robotic arm, with which this speed is associated.
	 * 
	 * @param linkSpd	Whether or not this motion's speed is linked to the
	 * 					robotic arm's live speed
	 */
	public void linkToRobotSpd(boolean linkSpd) {
		linkToRobotSpd = linkSpd;
	}
	
	/**
	 * Resets the speed and destination angles for this joint interpolation.
	 * This motion's speed will not be linked to the robotic arm's live speed.
	 * 
	 * @param robot
	 * @param tgtAngles
	 * @param speed
	 */
	public void setupRotationalInterpolation(RoboticArm robot,
			float[] tgtAngles, float speed) {
		
		this.speed = speed;
		linkToRobotSpd = false;
		setup(robot, tgtAngles);
	}
	
	/**
	 * Resets the speed and destination angles for this joint interpolation.
	 * This motion's speed will be linked to the robotic arm's live speed.
	 * 
	 * @param robot
	 * @param tgtAngles
	 */
	public void setupRotationalInterpolation(RoboticArm robot, float[] tgtAngles) {
		speed = robot.getLiveSpeed() / 100f;
		linkToRobotSpd = true;
		setup(robot, tgtAngles);
	}
	
	/**
	 * Sets the target joint angles and joint motion directions for the
	 * rotational interpolation. In addition, the speed modifiers for each of
	 * the roobt's segments is updated in such a way, that each joint will
	 * finish interpolation at the same time.
	 * 
	 * @param robot		The robot. for which this motion is defined
	 * @param tgtAngles	The target joint angles, to which to interpolate
	 */
	private void setup(RoboticArm robot, float[] tgtAngles) {
		float[] minDist = new float[6];
		float maxMinDist = Float.MIN_VALUE;
		
		for (int jdx = 0; jdx < 6; ++jdx) {
			// Set the target angle for the joint index
			TGT_ANGLES[jdx] = RMath.mod2PI(tgtAngles[jdx]);
			// Calculate the minimum distance to the target angle
			float minDistVal = RMath.minDist(robot.getSegment(jdx).getJointRotation(),
					TGT_ANGLES[jdx]);
			RSegWithJoint seg = robot.getSegment(jdx);
			
			// Check joint motion range
			if (seg.LOW_BOUND == 0f && seg.UP_BOUND == PConstants.TWO_PI) {
				JOINT_MOTION[jdx] = (minDistVal < 0) ? -1 : 1;
				minDist[jdx] = minDistVal;
				
			} else {  
				/* Determine if at least one bound lies within the range of the
				 * shortest angle between the current joint angle and the
				 * target angle. If so, then take the longer angle, otherwise
				 * choose the shortest angle path. */

				/* The minimum distance from the current joint angle to the
				 * lower bound of the joint's range */
				float dist_lb = RMath.minDist(seg.getJointRotation(),
						seg.LOW_BOUND);

				/* The minimum distance from the current joint angle to the
				 * upper bound of the joint's range */
				float dist_ub = RMath.minDist(seg.getJointRotation(),
						seg.UP_BOUND);

				if (minDistVal < 0) {
					if( (dist_lb < 0 && dist_lb > minDistVal) ||
							(dist_ub < 0 && dist_ub > minDistVal) ) {
						
						// One or both bounds lie within the shortest path
						JOINT_MOTION[jdx] = 1;
						minDist[jdx] = PConstants.TWO_PI + minDistVal;
						
					} else {
						JOINT_MOTION[jdx] = -1;
						minDist[jdx] = minDistVal;
					}
					
				} else if(minDistVal > 0) {
					if( (dist_lb > 0 && dist_lb < minDistVal) ||
							(dist_ub > 0 && dist_ub < minDistVal) ) {
						
						// One or both bounds lie within the shortest path
						JOINT_MOTION[jdx] = -1;
						minDist[jdx] = PConstants.TWO_PI - minDistVal;
						
					} else {
						JOINT_MOTION[jdx] = 1;
						minDist[jdx] = minDistVal;
					}
				}
			}
			
			if (Math.abs(minDist[jdx]) > maxMinDist) {
				/* Update the maximum distance necessary for one of the joints
				 * to reach its target angle. */
				maxMinDist = Math.abs(minDist[jdx]);
			}
		}
		
		for(int jdx = 0; jdx < 6; jdx++) {
			RSegWithJoint seg = robot.getSegment(jdx);
			/* Update the speed modifier for the joint based off the ratio
			 * between the distance necessary for this joint to travel and that
			 * of the joint with the longest distance to travel */
			seg.setSpdMod(Math.abs(minDist[jdx]) / maxMinDist * (PConstants.PI / 60f));
		}
	}
}
