package expression;

import geom.Point;
import robot.RoboticArm;

/**
 * Defines the LPos and JPos operands for register expressions.
 * 
 * @author Joshua Hooker
 */
public class RobotPoint extends Operand<RoboticArm> implements PointMath {
	
	/**
	 * Is this point currently defined as Cartesian?
	 */
	private boolean isCartesian;
	
	/**
	 * Defines a robot point operand for the given robotic arm with the given
	 * point type.
	 * 
	 * @param r			The robotic arm associated with the robot point
	 * @param isCart	The point type
	 */
	public RobotPoint(RoboticArm r, boolean isCart) {
		super(r, Operand.ROBOT);
		isCartesian = isCart;
	}
	
	@Override
	public Point getPointValue() {
		return value.getToolTipUser();
	}

	@Override
	public Operand<RoboticArm> clone() {
		return new RobotPoint(value, isCartesian);
	}


	/**
	 * @return	The type of this robot point
	 */
	public boolean isCartesian() {
		return isCartesian;
	}
	
	/**
	 * Redefine the point type of this operand.
	 * 
	 * @param isCart	The type of this operand's point value
	 */
	public void setType(boolean isCart) {
		isCartesian = isCart;
	}
	
	@Override
	public String toString() {
		if (value == null) {
			throw new IllegalStateException("No robot defined for a robot point!");	
		}
		
		return (isCartesian) ? "LPos" : "JPos";
	}
	
	@Override
	public String[] toStringArray() {
		return new String[] { toString() };
	}
}

