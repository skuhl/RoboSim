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
						robot.getLiveSpeed() / 100f;
				float trialAngle = RMath.mod2PI(seg.getJointRotation() + delta);
				
				if(!seg.setJointRotation(trialAngle)) {
					// Invalid joint rotation
					Fields.debug("A[%d]: %f\n", jdx, trialAngle);
					JOINT_MOTION[jdx] = 0;
					// TODO REFACTOR THIS
					// RobotRun.getInstance().updateRobotJogMotion(i, 0);
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
	
	public int getMotion(int mdx) {
		if (mdx >= 0 && mdx < JOINT_MOTION.length) {
			return JOINT_MOTION[mdx];
		}
		// Invalid motion index
		return 0;
	}
	
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
