package robot;

/**
 * Defines motion for a robotic arm.
 * 
 * @author Joshua Hooker
 */
public interface RobotMotion {
	
	/**
	 * Updates the robot according to the motion defined by this object.
	 * 
	 * @param robot	The robot to act upon
	 * @return		Some indication of the results of the motion
	 */
	public abstract int executeMotion(RoboticArm robot);
	
	/**
	 * Stops the motion defined by this object.
	 */
	public abstract void halt();
	
	/**
	 * Checks if the motion defined by this object is still active.
	 * 
	 * @return	If there is motion
	 */
	public abstract boolean hasMotion();
}
