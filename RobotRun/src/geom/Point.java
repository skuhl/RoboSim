package geom;

import java.util.Arrays;

import global.MyFloatFormat;
import global.RMath;
import processing.core.PConstants;
import processing.core.PVector;

/**
 * A class defining a position and orientation of the robot's tool tip as well
 * as a set of joint angles, which define a position and orientation of the
 * robot.
 * 
 * NOTE: due to the relationship between the robot's tool tip Cartesian values
 * and its joint angles, the angles defined by a Point do NOT necessarily
 * correspond to the position and orientation defined by the point.
 * 
 * @author Joshua Hooker and Vincent Druckte
 */
public class Point  {
	
	/**
	 * A position of the robot's tool tip defined with respect to some frame
	 * (the world or a user frame).
	 */
	public PVector position;
	
	/**
	 * An orientation of the robot's tool tip defined with respect to some
	 * frame (either the world frame or a user frame)
	 */
	public RQuaternion orientation;
	
	/**
	 * A set of joint angles representation an orientation of the robot. This
	 * does not necessary correlate with the defined position and orientation.
	 */
	public float[] angles;
	
	/**
	 * Initializes the position, orientation and angles associated with this
	 * point.
	 */
	public Point() {
		position = new PVector(0f, 0f, 0f);
		orientation = new RQuaternion();
		angles = new float[] { 0f, 0f, 0f, 0f, 0f, 0f };
	}

	/**
	 * Defines a point with the given position and orientation. The angles are
	 * assumed to be zero.
	 * 
	 * @param pos		The position of the robot's tool tip
	 * @param orient	The orientation of the robot's tool tip
	 */
	public Point(PVector pos, RQuaternion orient) {
		position = pos;
		orientation = orient;
		angles = new float[] { 0f, 0f, 0f, 0f, 0f, 0f };
	}
	
	/**
	 * Defines a point with the given position and orientation. The angles are
	 * assumed to be zero.
	 * 
	 * @param pos	The position of the robot's tool tip
	 * @param rMat	The orientation of the robot's tool tip
	 */
	public Point(PVector pos, RMatrix rMat) {
		angles = new float[] { 0f, 0f, 0f, 0f, 0f, 0f };
		position = pos;
		orientation = RMath.matrixToQuat(rMat);
	}
	
	/**
	 * Defines a point with the given position and orientation as well as a
	 * point with the given joint angles.
	 * 
	 * @param pos			The position of the robot's tool tip
	 * @param orient		The orientation of the robot's tool tip
	 * @param jointAngles	The set of joint angles defining a position and
	 * 						orientation of the robot
	 */
	public Point(PVector pos, RQuaternion orient, float[] jointAngles) {
		position = pos;
		orientation = orient;
		angles = jointAngles;
	}
	
	/**
	 * Defines a point, whose position and orientation are the combination of
	 * the given position and orientation with that of this position. The joint
	 * angles of the defined point are that of this point. 
	 * 
	 * @param pos		The position to add with this point's position
	 * @param orien		The combine to merge with this point's orientation
	 * @return			A point with the modified position and orientation
	 */
	public Point add(PVector pos, RQuaternion orien) {
		PVector resPos = PVector.add(position, pos);
		RQuaternion resOrien = orientation.mult(orien);
		
		return new Point(resPos, resOrien, angles.clone());
	}
	
	/**
	 * Defines a point, whose joint angles are the sum of the given joint angle
	 * set and that of this point. The position and orientation of the defined
	 * point are that of this point.
	 * 
	 * @param jointAngles	The joint angles to add to this point's joint angles
	 * @return				A point with the modified joint angles
	 */
	public Point add(float[] jointAngles) {
		float[] resJointAngles = new float[6];
		
		for (int jdx = 0; jdx < 6; ++jdx) {
			// Keep angles within the range [0, TWO_PI)
			resJointAngles[jdx] = RMath.mod2PI(this.angles[jdx] + jointAngles[jdx]);
		}
		
		return new Point(position.copy(), orientation.clone(), resJointAngles);
	}

	@Override
	public Point clone() {
		return new Point(position.copy(), orientation.clone(), angles.clone());
	}
	
	/**
	 * Returns the value of the point defined by the given index, with respect
	 * to the world frame.
	 * 
	 * @param idx	The index corresponding to a value of the point
	 * @return		The world frame value corresponding to the given index
	 */
	public Float getWorldValue(int idx) {
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
	 * Sets the value of the point associated with the given index to the given
	 * value, assuming the given value is with respect to the world frame.
	 * 
	 * @param idx	The index of a value of the point
	 * @param value	The new value of the point, with respect to the world frame
	 */
	public void setWorldValue(int idx, float value) {
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
	 * Returns a string array, where each entry is one of the values of the
	 * Cartesian represent of the point: (X, Y, Z, W, P, and R) and their
	 * respective labels. The first column is the label associated with the
	 * Cartesian value entry, in the second column.
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
	 * Returns a String array, whose entries are the joint values of the point
	 * with their respective labels (J1-J6). The first column is the label
	 * associated with the joint value in the second column.
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
	 * Converts the original toStringArray into a 2x1 String array, where the
	 * position values are in the first entry and the orientation values are in
	 * the second entry (or in the case of a joint angles, J1-J3 on the first
	 * and J4-J6 on the second). Each entry has space buffers between the
	 * values represented in the String.
	 * 
	 * @param catersian		whether to use the angle values or the Cartesian
	 * 						values associated with the point
	 * @return				A 2-element String array
	 */
	public String[] toLineStringArray(boolean catersian) {
		String str0, str1, str2, str3, str4, str5;
		
		if (catersian) {
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
			// position with space buffers
			String.format("%-13s %-13s %s", str0, str1, str2),
			// orientaiton with space buffers
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
