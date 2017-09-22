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
	
	/**
	 * Checks if the linear motion met a motion fault.
	 * 
	 * @return	Has this motion met a motion fault?
	 */
	public boolean hasFault() {
		return motionFault;
	}
	
	@Override
	public abstract boolean hasMotion();
	
	/**
	 * Sets the motion fault flag of this motion object to the given state. If
	 * the given value is true, then all motion is halted.
	 * 
	 * @param newState	The new motion fault state
	 */
	public void setFault(boolean newState) {
		motionFault = newState;
		
		if (newState) {
			// Stop motion on fault
			halt();
		}
	}
}
