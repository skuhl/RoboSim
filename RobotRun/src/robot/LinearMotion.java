package robot;

/**
 * Defines robot motion in a Cartesian frame.
 * 
 * @author Joshua Hooker
 */
public abstract class LinearMotion implements RobotMotion {
	
	/**
	 * Has the motion incurred a fault by some means (i.e. inverse kinematics
	 * failure).
	 */
	protected boolean motionFault;
	
	/**
	 * Initialize motion fault to false.
	 */
	public LinearMotion() {
		motionFault = false;
	}
	
	@Override
	public abstract int executeMotion(RoboticArm robot);

	@Override
	public abstract void halt();

	public boolean hasFault() {
		return motionFault;
	}
	
	@Override
	public abstract boolean hasMotion();
	
	public void setFault(boolean newState) {
		motionFault = newState;
		
		if (newState) {
			// Stop motion on fault
			halt();
		}
	}
}
