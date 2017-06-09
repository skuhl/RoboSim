package robot;

/**
 * Defines motion in the robot's joint coordinate frame.
 * 
 * @author Joshua Hooker
 */
public abstract class JointMotion implements RobotMotion {
	
	/**
	 * The directions of motion for each joint.
	 */
	protected final int[] JOINT_MOTION;
	
	/**
	 * Initializes the all joint motion to zeros.
	 */
	public JointMotion() {
		JOINT_MOTION = new int[] { 0, 0, 0, 0, 0, 0 };
	}
	
	@Override
	public abstract int executeMotion(RoboticArm robot);
	
	@Override
	public void halt() {
		for (int mdx = 0; mdx < JOINT_MOTION.length; ++mdx) {
			JOINT_MOTION[mdx] = 0;
		}
	}
	
	@Override
	public boolean hasMotion() {
		for (int mdx = 0; mdx < JOINT_MOTION.length; ++mdx) {
			if (JOINT_MOTION[mdx] != 0) {
				// At least one joint is in motion
				return true;
			}
		}
		// No joints are in motion
		return false;
	}
}
