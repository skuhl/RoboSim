package geom;

import global.Fields;
import processing.core.PVector;
import robot.RQuaternion;
import robot.RobotRun;

public class Point  {
	// X, Y, Z
	public PVector position;
	// Q1 - Q4
	public RQuaternion orientation;
	// J1 - J6
	public float[] angles;

	public Point() {
		angles = new float[] { 0f, 0f, 0f, 0f, 0f, 0f };
		position = new PVector(0f, 0f, 0f);
		orientation = new RQuaternion();
	}

	public Point(float x, float y, float z, float r, float i, float j, float k,
			float j1, float j2, float j3, float j4, float j5, float j6) {
		angles = new float[6];
		position = new PVector(x,y,z);
		orientation = new RQuaternion(r, i, j, k);
		angles[0] = j1;
		angles[1] = j2;
		angles[2] = j3;
		angles[3] = j4;
		angles[4] = j5;
		angles[5] = j6;
	}

	public Point(PVector pos, RQuaternion orient) {
		angles = new float[] { 0f, 0f, 0f, 0f, 0f, 0f };
		position = pos;
		orientation = orient;
	}

	public Point(PVector pos, RQuaternion orient, float[] jointAngles) {
		position = pos;
		orientation = orient;
		angles = jointAngles;
	}

	/**
	 * Computes and returns the result of the addition of this point with
	 * another point, 'p.' Does not alter the original values of this point.
	 */
	public Point add(Point p) {
		Point p3 = new Point();

		PVector p3Pos = PVector.add(position, p.position);
		RQuaternion p3Orient = RQuaternion.mult(orientation, p.orientation);
		float[] p3Joints = new float[6];

		for(int i = 0; i < 6; i += 1) {
			p3Joints[i] = angles[i] + p.angles[i];
		}

		p3.position = p3Pos;
		p3.orientation = p3Orient;
		p3.angles = p3Joints;

		return p3;
	}

	public Point clone() {
		return new Point(position.copy(), (RQuaternion)orientation.clone(), angles.clone());
	}

	public Float getValue(int idx) {
		switch(idx) {
		// Joint angles
		case 0:
		case 1:
		case 2:
		case 3:
		case 4:
		case 5:   return angles[idx];
		// Position
		case 6:   return -position.x;
		case 7:   return position.z;
		case 8:   return -position.y;
		// Orientation
		case 9:   return -RobotRun.RAD_TO_DEG*RobotRun.quatToEuler(orientation).array()[0];
		case 10:  return -RobotRun.RAD_TO_DEG*RobotRun.quatToEuler(orientation).array()[2];
		case 11:  return RobotRun.RAD_TO_DEG*RobotRun.quatToEuler(orientation).array()[1];
		default:
		}

		return null;
	}

	/**
	 * Negates the current values of the point.
	 */
	public Point negate() {
		position = position.mult(-1);
		orientation = RQuaternion.scalarMult(-1, orientation);
		angles = RobotRun.vectorScalarMult(angles, -1);
		return this;
	}

	public void setValue(int idx, float value) {
		PVector vec = RobotRun.quatToEuler(orientation);

		switch(idx) {
		// Joint angles
		case 0:
		case 1:
		case 2:
		case 3:
		case 4:
		case 5:   angles[idx] = value;
		break;
		// Position
		case 6:   position.x = -value;
		break;
		case 7:   position.z = value;
		break;
		case 8:   position.y = -value;
		break;
		// Orientation
		case 9:   vec.x = -value;
		orientation = RobotRun.eulerToQuat(vec);
		break;
		case 10:  vec.z = -value;
		orientation = RobotRun.eulerToQuat(vec);
		break;
		case 11:  vec.y = value;
		orientation = RobotRun.eulerToQuat(vec);
		break;
		default:
		}
	}

	/**
	 * Returns a string array, where each entry is one of the values of the Cartiesian
	 * represent of the Point: (X, Y, Z, W, P, and R) and their respective labels.
	 * 
	 * @return  A 6x2-element String array
	 */
	public String[][] toCartesianStringArray() {
		String[][] entries = new String[6][2];

		PVector pos;
		if (position == null) {
			// Uninitialized
			pos = new PVector(Float.NaN, Float.NaN, Float.NaN);
		} else {
			// Display in terms of the World Frame
			pos = RobotRun.convertNativeToWorld(position);
		}

		// Convert Quaternion to Euler Angles
		PVector angles;
		if (orientation == null) {
			// Uninitialized
			angles = new PVector(Float.NaN, Float.NaN, Float.NaN);
		} else {
			// Display in degrees
			float[][] axes = orientation.toMatrix();
			float[][] transform = new float[3][3];
			
			transform[0][0] = -axes[0][0];
			transform[0][1] = -axes[0][1];
			transform[0][2] = -axes[0][2];
			
			transform[1][0] = axes[2][0];
			transform[1][1] = axes[2][1];
			transform[1][2] = axes[2][2];
			
			transform[2][0] = -axes[1][0];
			transform[2][1] = -axes[1][1];
			transform[2][2] = -axes[1][2];
			
			angles = RobotRun.matrixToEuler(transform).mult(RobotRun.RAD_TO_DEG);
		}

		entries[0][0] = "X: ";
		entries[0][1] = String.format("%4.3f", pos.x);
		entries[1][0] = "Y: ";
		entries[1][1] = String.format("%4.3f", pos.y);
		entries[2][0] = "Z: ";
		entries[2][1] = String.format("%4.3f", pos.z);
		// Display angles in terms of the World frame
		entries[3][0] = "W: ";
		entries[3][1] = String.format("%4.3f", -angles.x);
		entries[4][0] = "P: ";
		entries[4][1] = String.format("%4.3f", -angles.z);
		entries[5][0] = "R: ";
		entries[5][1] = String.format("%4.3f", angles.y);

		return entries;
	}

	/**
	 * Returns a String array, whose entries are the joint values of the
	 * Point with their respective labels (J1-J6).
	 * 
	 * @return  A 6x2-element String array
	 */
	public String[][] toJointStringArray() {
		String[][] entries = new String[6][2];

		for(int idx = 0; idx < angles.length; ++idx) {
			entries[idx][0] = String.format("J%d: ", (idx + 1));

			if (angles == null) {
				entries[idx][1] = Float.toString(Float.NaN);
			} else {
				entries[idx][1] = String.format("%4.3f", angles[idx] * RobotRun.RAD_TO_DEG);
			}
		}

		return entries;
	}

	/**
	 * Converts the original toStringArray into a 2x1 String array, where the origin
	 * values are in the first element and the W, P, R values are in the second
	 * element (or in the case of a joint angles, J1-J3 on the first and J4-J6 on
	 * the second), where each element has space buffers.
	 * 
	 * @param displayCartesian  whether to display the joint angles or the cartesian
	 *                          values associated with the point
	 * @returning               A 2-element String array
	 */
	public String[] toLineStringArray(boolean displayCartesian) {
		String[][] entries;

		if (displayCartesian) {
			entries = toCartesianStringArray();
		} else {
			entries = toJointStringArray();
		}


		String[] line = new String[2];
		// X, Y, Z with space buffers
		line[0] = String.format("%-12s %-12s %s", entries[0][0].concat(entries[0][1]),
				entries[1][0].concat(entries[1][1]), entries[2][0].concat(entries[2][1]));
		// W, P, R with space buffers
		line[1] = String.format("%-12s %-12s %s", entries[3][0].concat(entries[3][1]),
				entries[4][0].concat(entries[4][1]), entries[5][0].concat(entries[5][1]));

		return line;
	}

	public String toString() {
		return String.format("P: { %s, %s }", position, orientation);
	}
} // end Point class