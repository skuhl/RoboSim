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
	 * The joint angles, to which to move.
	 */
	private final float[] TGT_ANGLES;
	
	/**
	 *  The overall speed modifier for the rotational interpolation.
	 */
	private float speed;
	
	/**
	 * Sets the target joint angles and joint motion directions for the
	 * rotational interpolation. In addition, the speed modifiers for each of
	 * the roobt's segments is updated in such a way, that each joint will
	 * finish interpolation at the same time.
	 * 
	 * @param robot		The robot. for which this motion is defined
	 * @param tgtAngles	The target joint angles, to which to interpolate
	 */
	public JointInterpolation(RoboticArm robot, float[] tgtAngles, float speed) {
		TGT_ANGLES = new float[6];
		setupRotationalInterpolation(robot, tgtAngles, speed);
	}
	
	@Override
	public int executeMotion(RoboticArm robot) {
		// Count how many joints are still in motion
		int ret = 0;
		
		for(int jdx = 0; jdx < 6; ++jdx) {
			RSegWithJoint seg = robot.getSegment(jdx);
			
			if (JOINT_MOTION[jdx] != 0) {
				float distToDest = PApplet.abs(seg.getJointRotation()
						- TGT_ANGLES[jdx]);
				float deltaAngle = seg.getSpeedModifier() * speed;
				
				if (deltaAngle > 0.000009f && distToDest >= deltaAngle) {
					++ret;
					/* Move the joint based on the roobt's liveSpeed, the
					 * direction of motion and the segment's speed modifier */
					float newRotation = RMath.mod2PI(seg.getJointRotation()
							+ JOINT_MOTION[jdx] * deltaAngle);
					
					seg.setJointRotation(newRotation);

				} else {
					if (distToDest > 0.00009f) {
						// Destination too close to move at current speed
						seg.setJointRotation(TGT_ANGLES[jdx]);
					}
					
					JOINT_MOTION[jdx] = 0;
				}
			}
		}
		
		return ret;
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
	public void setupRotationalInterpolation(RoboticArm robot,
			float[] tgtAngles, float speed) {
		
		this.speed = speed;
		
		float[] minDist = new float[6];
		float maxMinDist = Float.MIN_VALUE;
		
		for (int jdx = 0; jdx < 6; ++jdx) {
			// Set the target angle for the joint index
			TGT_ANGLES[jdx] = RMath.mod2PI(tgtAngles[jdx]);
			// Calculate the minimum distance to the target angle
			minDist[jdx] = RMath.minDist(robot.getSegment(jdx).getJointRotation(),
					TGT_ANGLES[jdx]);
			
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
			
			// Check joint motion range
			if (seg.LOW_BOUND == 0f && seg.UP_BOUND == PConstants.TWO_PI) {
				JOINT_MOTION[jdx] = (minDist[jdx] < 0) ? -1 : 1;
				
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

				if (minDist[jdx] < 0) {
					if( (dist_lb < 0 && dist_lb > minDist[jdx]) ||
							(dist_ub < 0 && dist_ub > minDist[jdx]) ) {
						
						// One or both bounds lie within the shortest path
						JOINT_MOTION[jdx] = 1;
						
					} else {
						JOINT_MOTION[jdx] = -1;
					}
					
				} else if(minDist[jdx] > 0) {
					if( (dist_lb > 0 && dist_lb < minDist[jdx]) ||
							(dist_ub > 0 && dist_ub < minDist[jdx]) ) {
						
						// One or both bounds lie within the shortest path
						JOINT_MOTION[jdx] = -1;
						
					} else {
						JOINT_MOTION[jdx] = 1;
					}
				}
			}
		}
	}
}
