package robot;

import global.Fields;
import global.RMath;

/**
 * Defines the robot's jog motion in the joint coordinate frame.
 * 
 * @author Joshua Hooker
 */
public class JointJog extends JointMotion {
	
	/**
	 * Initializes the all joint motion to zeros.
	 */
	public JointJog() {
		super();
	}
	
	@Override
	public int executeMotion(RoboticArm robot) {
		for(int jdx = 0; jdx < 6; jdx += 1) {
			RSegWithJoint seg = robot.getSegment(jdx);
			
			// Is the joint in motion?
			if (JOINT_MOTION[jdx] != 0) {
				/* Move the joint based on the roobt's liveSpeed, the direction
				 * of motion and the segment's speed modifier */
				float delta = JOINT_MOTION[jdx] * seg.getSpeedModifier() *
						robot.getSpeedForCoord();
				float trialAngle = RMath.mod2PI(seg.getJointRotation() + delta);
				
				if(!seg.setJointRotation(trialAngle)) {
					// Invalid joint rotation
					Fields.debug("A[%d]: %f\n", jdx, trialAngle);
					JOINT_MOTION[jdx] = 0;
				}
			}
		}
		
		return 0;
	}
	
	/**
	 * Creates a copy of the directions of joint motion defined by this object.
	 * 
	 * @return	A copy of the joint motion directions
	 */
	public int[] getJogMotion() {
		return JOINT_MOTION.clone();
	}
	
	/**
	 * Returns the direction of motion for the axis associated with the given
	 * index. The six motion axes are the six joint axes of a robotic arm.
	 * 
	 * @param mdx	The index of the motion axis (0 - 5)
	 * @return		The direction of the motion associated with the specified
	 * 				motion axis
	 */
	public int getMotion(int mdx) {
		if (mdx >= 0 && mdx < JOINT_MOTION.length) {
			return JOINT_MOTION[mdx];
		}
		// Invalid motion index
		return 0;
	}
	
	/**
	 * Updates the direction of motion associated with the motion axis with the
	 * given index. Setting the direction of motion for an axis to its current
	 * value will reset the motion to 0.
	 * 
	 * @param mdx		The index of a motion axis (0 - 5)
	 * @param newDir	The new direction of motion for the specified motion
	 * 					axis (0, -1, and 1)
	 * @return			The old direction of motion for the specified axis
	 */
	public int setMotion(int mdx, int newDir) {
		if (mdx >= 0 && mdx < JOINT_MOTION.length) {
			if (JOINT_MOTION[mdx] == newDir) {
				JOINT_MOTION[mdx] = 0;
				
			} else {
				JOINT_MOTION[mdx] = newDir;
			}
			
			return JOINT_MOTION[mdx];
		}
		// Invalid motion index
		return 0;
	}
}
