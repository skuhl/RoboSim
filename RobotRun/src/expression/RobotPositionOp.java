package expression;
import geom.Point;
import programming.RegStmtPoint;
import robot.RobotRun;

/**
 * An operand thaht represents the current position and orientation of the Robot,
 * or its joint Angles, or a specific value of either point.
 */
public class RobotPositionOp implements Operand {
	/**
	 * 
	 */
	private final RobotRun robotRun;
	/**
	 * The value of valIdx corresponds to:
	 *   -1     ->  The point itself
	 *   0 - 5  ->  J1 - J6
	 *   6 - 11 ->  X, Y, Z, W, P, R
	 */
	private final int valIdx;
	private final boolean isCartesian;

	/**
	 * Default to the entire point
	 */
	public RobotPositionOp(RobotRun robotRun, boolean cartesian) {
		this.robotRun = robotRun;
		valIdx = -1;
		isCartesian = cartesian;
	}

	/**
	 * Specific the index of the value of the point
	 */
	public RobotPositionOp(RobotRun robotRun, int vdx, boolean cartesian) {
		this.robotRun = robotRun;
		valIdx = vdx;
		isCartesian = cartesian;
	}

	/**
	 * Return the current position of the Robot or a specific value of the current position of
	 * the Robot
	 */
	public Object getValue() {
		Point RP = RobotRun.nativeRobotEEPoint(robotRun.getArmModel().getJointAngles());
		RegStmtPoint pt = new RegStmtPoint(RP, isCartesian);

		if (valIdx == -1) {
			// Return the entire point
			return pt;
		} else {
			// Return a specific value of the point
			return pt.getValues()[valIdx];
		}
	}

	public Operand clone() {
		return new RobotPositionOp(this.robotRun, valIdx, isCartesian);
	}

	public String toString() {

		if (valIdx == -1) {
			if (isCartesian) {
				return String.format("LPos");
			} else {
				return String.format("JPos");
			}
		} else {
			// Only show index if the whole point is not used
			if (isCartesian) {
				return String.format("LPos[%d]", valIdx);
			} else {
				return String.format("JPos[%d]", valIdx);
			}
		}
	}
}