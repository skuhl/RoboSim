package programming;
import java.util.Arrays;

import robot.RQuaternion;
import geom.Point;
import processing.core.PVector;
import robot.RobotRun;

/**
 * This class defines a Point, which stores a position and orientation
 * in space (X, Y, Z, W, P, R) or the joint angles (J1 - J6) necessary
 * for the Robot to reach the position and orientation of the register point.
 * 
 * This class is designed to temporary store the values of a Point object
 * in order to bypass multiple conversion between Euler angles and
 * Quaternions during the evaluation of Register Statement Expressions.
 */
public class RegStmtPoint {
	/**
	 * The values associated with a register point:
	 * 
	 * For a Cartesian point:
	 *   0, 1, 2 -> X, Y, Z
	 *   3. 4, 5 -> W, P, R
	 * 
	 * For a Joint point:
	 *   0 - 5 -> J1 - J6
	 */
	private final float[] values;
	private boolean isCartesian;

	public RegStmtPoint() {
		values = new float[] { 0f, 0f, 0f, 0f, 0f, 0f };
		isCartesian = false;
	}

	public RegStmtPoint(float[] iniValues, boolean cartesian) {
		if (iniValues.length < 6) {
			// Not valid input values
			values = new float[] { 0f, 0f, 0f, 0f, 0f, 0f };
		} else {
			// Copy initial values
			values = Arrays.copyOfRange(iniValues, 0, 6);
		}

		isCartesian = cartesian;
	}

	public RegStmtPoint(Point initial, boolean cartesian) {
		values = new float[6];
		isCartesian = cartesian;
		// Convert to W, P, R values
		PVector wpr = RobotRun.quatToEuler(initial.orientation);
		// Copy values into this point
		getValues()[0] = initial.position.x;
		getValues()[1] = initial.position.x;
		getValues()[2] = initial.position.x;
		getValues()[3] = wpr.x;
		getValues()[4] = wpr.y;
		getValues()[5] = wpr.z;
	}

	public RegStmtPoint add(RegStmtPoint pt) {
		if (pt == null || pt.isCartesian() != isCartesian) {
			// Must be the same type of point
			return null;
		}

		float[] sums = new float[6];
		// Compute sums
		for (int pdx = 0; pdx < getValues().length; ++pdx) {
			sums[pdx] = getValues()[pdx] + pt.getValue(pdx);
		}

		return new RegStmtPoint(sums, isCartesian);
	}

	/**
	 * Returns an independent replica of this point object.
	 */
	public RegStmtPoint clone() {
		return new RegStmtPoint(getValues(), isCartesian);
	}

	public Float getValue(int val) {
		if (val < 0 || val >= getValues().length) {
			// Not a valid index
			return null;
		}
		// Return value associated with the index
		return new Float(getValues()[val]);
	}

	public float[] getValues() {
		return values;
	}

	public boolean isCartesian() { return isCartesian; }

	public void setValue(int val, float newVal) {
		if (val >= 0 && val < getValues().length) {
			// Set the specified entry to the given value
			getValues()[val] = newVal;
		}
	}

	public RegStmtPoint subtract(RegStmtPoint pt) {
		if (pt == null || pt.isCartesian() != isCartesian) {
			// Must be the same type of point
			return null;
		}

		float[] differences = new float[6];
		// Compute sums
		for (int pdx = 0; pdx < getValues().length; ++pdx) {
			differences[pdx] = getValues()[pdx] - pt.getValue(pdx);
		}

		return new RegStmtPoint(differences, isCartesian);
	}

	public Point toPoint() {

		if (isCartesian) {
			PVector position = new PVector(getValues()[0], getValues()[1], getValues()[2]),
					wpr = new PVector(getValues()[3], getValues()[4], getValues()[5]);
			// Convet back to quaternion
			RQuaternion orientation = RobotRun.eulerToQuat(wpr);
			// TODO initialize angles?
			return new Point(position, orientation);
		} else {
			// Use forward kinematics to find the position and orientation of the joint angles
			return RobotRun.nativeRobotEEPoint(RobotRun.getRobot(), getValues());
		}
	}

	public String toString() {
		if (isCartesian) {
			// X, Y, Z, W, P, R
			return String.format("[ %4.3f, %4.3f, %4.3f], [ %4.3f, %4.3f, %4.3f ]",
					getValues()[0], getValues()[1], getValues()[2],
					Math.toDegrees(getValues()[3]), Math.toDegrees(getValues()[4]), Math.toDegrees(getValues()[5]));
		} else {
			// J1 - J6
			return String.format("[ %4.3f, %4.3f, %4.3f, %4.3f, %4.3f, %4.3f ]",
					Math.toDegrees(getValues()[0]), Math.toDegrees(getValues()[1]), Math.toDegrees(getValues()[2]),
					Math.toDegrees(getValues()[3]), Math.toDegrees(getValues()[4]), Math.toDegrees(getValues()[5]));
		}
	}
}