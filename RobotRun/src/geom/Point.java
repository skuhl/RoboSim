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
		resPos.x = RMath.clamp(resPos.x, -9999f, 9999f);
		resPos.y = RMath.clamp(resPos.y, -9999f, 9999f);
		resPos.z = RMath.clamp(resPos.z, -9999f, 9999f);
		RQuaternion resOrien = RQuaternion.mult(orientation, orien).normalize();
		
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
	
	public Point add(Point p) {
		Point jointSum = add(p.angles);
		Point cartSum = add(p.position, p.orientation);
		
		return new Point(cartSum.position, cartSum.orientation, jointSum.angles);
	}
	
	public Point sub(Point p) {
		return add(p.negate());
	}
	
	public Point negate() {
		PVector negPos = new PVector().sub(position);
		RQuaternion negOrient = orientation.conjugate();
		float[] negJointAngles = new float[6];
		
		
		for(int i = 0; i < 6; i += 1) {
			negJointAngles[i] = -angles[i];
		}
		
		return new Point(negPos, negOrient, negJointAngles);
	}

	@Override
	public Point clone() {
		return new Point(position.copy(), orientation.clone(), angles.clone());
	}
	
	/**
	 * Compares the Cartesian values of this point and pt to see if they are
	 * close enough to be considered the same position and orientation.
	 * 
	 * @param pt	The point to compare to this
	 * @return		If the Cartesian values of pt and this are close enough
	 * 				together to be considered the same
	 */
	public boolean compareCartesian(Point pt) {
		float posDist = PVector.dist(pt.position, position);
		
		if (posDist > 0.01f) {
			return false;
			
		} else {
			// TODO quaternion difference
		}
		
		return true;
	}
	
	/**
	 * Compares the joint angle values of this point and pt to see if they are
	 * close enough to be considered the same.
	 * 
	 * @param pt	The point to compare to this
	 * @return		If the joint values of this and pt are close enough to be
	 * 				considered the same
	 */
	public boolean compareJoint(Point pt) {
		for (int jdx = 0; jdx < 6; ++jdx) {
			float diff = Math.abs(pt.angles[jdx] - angles[jdx]);
			
			// Difference is greater than ~0.005 degrees
			if (diff > 0.000009f) {
				return false;
			}
		}
		
		return true;
	}
	
	/**
	 * Returns a point, with Cartesian values, which are the negation of this
	 * point's Cartesian values.
	 * 
	 * @return	A point with negated Cartesian values
	 */
	public Point negateCartesian() {
		PVector nPos = PVector.mult(position, -1f);
		RQuaternion nOrien = orientation.conjugate();
		
		return new Point(nPos, nOrien, angles.clone());
	}
	
	/**
	 * Computes a point with joint values, which are the negation of this
	 * point's joint values.
	 * 
	 * @return	A point with negated joint values
	 */
	public Point negateJoint() {
		float[] nAngles = new float[6];
		
		for (int jdx = 0; jdx < 6; ++jdx) {
			nAngles[jdx] = -angles[jdx];
		}
		
		return new Point(position.copy(), orientation.clone(), nAngles);
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
