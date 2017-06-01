package geom;

import java.util.Arrays;

import core.RobotRun;
import global.MyFloatFormat;
import global.RMath;
import processing.core.PConstants;
import processing.core.PVector;

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
	
	public Point(PVector pos, RMatrix orient) {
		angles = new float[] { 0f, 0f, 0f, 0f, 0f, 0f };
		position = pos;
		orientation = RMath.matrixToQuat(orient);
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
			p3Joints[i] = (angles[i] + p.angles[i]) % RobotRun.TWO_PI;
		}

		p3.position = p3Pos;
		p3.orientation = p3Orient;
		p3.angles = p3Joints;

		return p3;
	}

	@Override
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
		case 9:   return -PConstants.RAD_TO_DEG*RMath.quatToEuler(orientation).array()[0];
		case 10:  return -PConstants.RAD_TO_DEG*RMath.quatToEuler(orientation).array()[2];
		case 11:  return PConstants.RAD_TO_DEG*RMath.quatToEuler(orientation).array()[1];
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
		angles = RMath.vectorScalarMult(angles, -1);
		return this;
	}

	public void setValue(int idx, float value) {
		PVector vec = RMath.quatToEuler(orientation);

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
		orientation = RMath.eulerToQuat(vec);
		break;
		case 10:  vec.z = value;
		orientation = RMath.eulerToQuat(vec);
		break;
		case 11:  vec.y = -value;
		orientation = RMath.eulerToQuat(vec);
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
		
		PVector limbo = worldFramePosition();
		entries[0][0] = "X: ";
		entries[0][1] = String.format("%4.3f", limbo.x);
		entries[1][0] = "Y: ";
		entries[1][1] = String.format("%4.3f", limbo.y);
		entries[2][0] = "Z: ";
		entries[2][1] = String.format("%4.3f", limbo.z);

		limbo = worldFrameOrientation();
		entries[3][0] = "W: ";
		entries[3][1] = String.format("%4.3f", limbo.x);
		entries[4][0] = "P: ";
		entries[4][1] = String.format("%4.3f", limbo.y);
		entries[5][0] = "R: ";
		entries[5][1] = String.format("%4.3f", limbo.z);

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
				entries[idx][1] = String.format("%4.3f", angles[idx]
						* PConstants.RAD_TO_DEG);
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
		String str0, str1, str2, str3, str4, str5;
		
		if (displayCartesian) {
			PVector limbo = worldFramePosition();
			str0 = "X: " + MyFloatFormat.format(limbo.x);
			str1 = "Y: " + MyFloatFormat.format(limbo.y);
			str2 = "Z: " + MyFloatFormat.format(limbo.z);
			
			limbo = worldFrameOrientation();
			str3 = "W: " + MyFloatFormat.format(limbo.x);
			str4 = "P: " + MyFloatFormat.format(limbo.y);
			str5 = "R: " + MyFloatFormat.format(limbo.z);
			
		} else {
			str0 = "J1: " + MyFloatFormat.format(angles[0]
					* PConstants.RAD_TO_DEG);
			str1 = "J2: " + MyFloatFormat.format(angles[1]
					* PConstants.RAD_TO_DEG);
			str2 = "J3: " + MyFloatFormat.format(angles[2]
					* PConstants.RAD_TO_DEG);
			str3 = "J4: " + MyFloatFormat.format(angles[3]
					* PConstants.RAD_TO_DEG);
			str4 = "J5: " + MyFloatFormat.format(angles[4]
					* PConstants.RAD_TO_DEG);
			str5 = "J6: " + MyFloatFormat.format(angles[5]
					* PConstants.RAD_TO_DEG);
		}
		
		return new String[] {
			// X, Y, Z with space buffers
			String.format("%-13s %-13s %s", str0, str1, str2),
			// W, P, R with space buffers
			String.format("%-13s %-13s %s",  str3, str4, str5)
		};
	}

	@Override
	public String toString() {
		return String.format("P=%s O=%s J=%s", position, orientation,
				Arrays.toString(angles));
	}
	
	/**
	 * @return	The position of this point with reference to the world frame
	 */
	private PVector worldFrameOrientation() {
		if (orientation == null) {
			// Uninitialized
			return new PVector(Float.NaN, Float.NaN, Float.NaN);
		}
		
		return RMath.nQuatToWEuler(orientation);
	}
	
	/**
	 * @return	The orientation of the point with reference to the world frame
	 */
	private PVector worldFramePosition() {
		if (position == null) {
			// Uninitialized
			return new PVector(Float.NaN, Float.NaN, Float.NaN);
		}
		
		return RMath.vToWorld(position);
	}
}
