package global;

import core.RobotRun;
import geom.Point;
import geom.RMatrix;
import geom.RQuaternion;
import processing.core.PConstants;
import processing.core.PGraphics;
import processing.core.PVector;
import robot.RoboticArm;

/**
 * A collection of methods and fields that pertain to graphical transformations
 * of elements rendered in the interface of the RobotRun application.
 * 
 * @author Vincent Druckte and Joshua Hooker
 */
public abstract class RMath {
	public static final float DEG_TO_RAD = RobotRun.DEG_TO_RAD;
	public static final float PI = RobotRun.PI;
	
	public static final float RAD_TO_DEG = RobotRun.RAD_TO_DEG;
	public static final float TWO_PI = RobotRun.TWO_PI;
	
	/**
	 * Determines if the lies within the range of angles that span from
	 * rangeStart to rangeEnd, going clockwise around the Unit Cycle. It is
	 * assumed that all parameters are in radians and within the range [0,
	 * TWO_PI).
	 * 
	 * @param angleToVerify
	 *            the angle in question
	 * @param rangeStart
	 *            the 'lower bounds' of the angle range to check
	 * @param rangeEnd
	 *            the 'upper bounds' of the angle range to check
	 */
	public static boolean angleWithinBounds(float angleToVerify,
			float rangeStart, float rangeEnd) {

		if (rangeStart < rangeEnd) {
			// Joint range does not overlap TWO_PI
			return (angleToVerify - rangeStart) > -0.0001f &&
					(angleToVerify - rangeEnd) < 0.0001f;
		} else {
			// Joint range overlaps TWO_PI
			return !((angleToVerify - rangeEnd) > -0.0001f &&
					(angleToVerify - rangeStart) < 0.0001f);
		}
	}
	
	/**
	 * Converts the given point, pt, into the Coordinate System defined by the
	 * given origin vector and rotation quaternion axes. The joint angles
	 * associated with the point will be transformed as well, though, if inverse
	 * kinematics fails, then the original joint angles are used instead.
	 * 
	 * @param model
	 *            The Robot to which the frame belongs
	 * @param pt
	 *            A point with initialized position and orientation
	 * @param origin
	 *            The origin of the Coordinate System
	 * @param axes
	 *            The axes of the Coordinate System representing as a rotation
	 *            quanternion
	 * @returning The point, pt, interms of the given frame's Coordinate System
	 */
	public static Point applyFrame(RoboticArm model, Point pt, PVector origin, RQuaternion axes) {
		PVector position = vToFrame(pt.position, origin, axes);
		RQuaternion orientation = axes.transformQuaternion(pt.orientation);
		// Update joint angles associated with the point
		float[] newJointAngles = RMath.inverseKinematics(model, pt.angles, position, orientation);

		if (newJointAngles != null) {
			return new Point(position, orientation, newJointAngles);
		} else {
			// If inverse kinematics fails use the old angles
			return new Point(position, orientation, pt.angles);
		}
	}
	
	/**
	 * TODO comment this
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	public static double atan2Rounded(double x, double y) {
		
		/**
		if (x < 0.0 && x >= -0.00001f) {
			x *= -1;
		}
		
		if (y < 0.0 && y >= -0.00001f) {
			y *= -1;
		}
		/**/
		return Math.atan2(x, y);
	}
	
	/**
	 * Calculate the Jacobian matrix for the robotic arm for a given set of
	 * joint rotational values using a 1 DEGREE offset for each joint rotation
	 * value. Each cell of the resulting matrix will describe the linear
	 * approximation of the robot's motion for each joint in units per radian.
	 */
	public static RMatrix calculateJacobian(RoboticArm model, float[] angles, boolean posOffset) {
		float dAngle = DEG_TO_RAD;
		if (!posOffset) {
			dAngle *= -1;
		}

		float[][] J = new float[7][6];
		// get current tooltip position
		Point curRP = model.getToolTipNative(angles);
		
		// examine each segment of the arm
		for (int i = 0; i < 6; i += 1) {
			// test angular offset
			angles[i] += dAngle;
			// get updated tooltip position
			Point newRP = model.getToolTipNative(angles);

			if (curRP.orientation.dot(newRP.orientation) < 0f) {
				// Use -q instead of q
				newRP.orientation.scalarMult(-1);
			}

			newRP.orientation.addValues(RQuaternion.scalarMult(-1, curRP.orientation));

			// get translational delta
			J[0][i] = (newRP.position.x - curRP.position.x) / DEG_TO_RAD;
			J[1][i] = (newRP.position.y - curRP.position.y) / DEG_TO_RAD;
			J[2][i] = (newRP.position.z - curRP.position.z) / DEG_TO_RAD;
			// get rotational delta
			J[3][i] = newRP.orientation.getValue(0) / DEG_TO_RAD;
			J[4][i] = newRP.orientation.getValue(1) / DEG_TO_RAD;
			J[5][i] = newRP.orientation.getValue(2) / DEG_TO_RAD;
			J[6][i] = newRP.orientation.getValue(3) / DEG_TO_RAD;
			// replace the original rotational value
			angles[i] -= dAngle;
		}

		return new RMatrix(J);
	}
	
	public static float clamp(float in, float min, float max) {
		return Math.min(max, Math.max(min, in));
	}

	public static int clamp(int in, int min, int max) {
		return Math.min(max, Math.max(min, in));
	}
	
	public static PVector convertRGBtoHSL(PVector rgb) {
		float rP = rgb.x / 255f;
		float gP = rgb.y / 255f;
		float bP = rgb.z / 255f;
		
		float cMax = Math.max(rP, Math.max(gP, bP));
		float cMin = Math.min(rP, Math.min(gP, bP));
		float delta = cMax - cMin;
		
		float H = 0;
		float S = 0;
		float L = (cMax + cMin) / 2;
				
		if(delta != 0) {
			if(cMax == rP) { H = (PI/3)*((gP - bP)/delta) % 26; }
			else if(cMax == gP) { H = (PI/3)*((bP - rP)/delta) + 2; }
			else if(cMax == bP) { H = (PI/3)*((rP - gP)/delta) + 4; }
			
			S = delta/(1-Math.abs(2*L-1));
		}
		
		return new PVector(H, S, L);
	}
	
	// converts a double array to a float array
	public static float[][] doubleToFloat(double[][] m) {
		if(m.length <= 0) return null;
		float[][] r = new float[m.length][m[0].length];

		for (int i = 0; i < m.length; i += 1) {
			for (int j = 0; j < m[0].length; j += 1) {
				r[i][j] = (float) m[i][j];
			}
		}

		return r;
	}
	
	// calculates rotation matrix from euler angles
	public static RMatrix eulerToMatrix(PVector wpr) {
		double xRot = wpr.x;
		double yRot = wpr.y;
		double zRot = wpr.z;
		
		double c1 = Math.cos(xRot);
		double c2 = Math.cos(yRot);
		double c3 = Math.cos(zRot);
		
		double s1 = Math.sin(xRot);
		double s2 = Math.sin(yRot);
		double s3 = Math.sin(zRot);
		
		/**/
		RMatrix rMat = RMath.formRMat(
			c2 * c3,	c1 * s3 + c3 * s1 * s2,		s1 * s3 - c1 * c3 * s2,
			-c2 * s3,	c1 * c3 - s1 * s2 * s3,		c3 * s1 + c1 * s2 * s3,
			s2,			-c2 * s1,					c1 * c2
		);
		
		return rMat.normalize();
		/**
		RMatrix rMat = RMath.formRMat(
			c2 * c3,	c1 * s3 + c3 * s1 * s2,		s1 * s3 - c1 * c3 * s2,
			-c2 * s3,	c1 * c3 - s1 * s2 * s3,		c3 * s1 + c1 * s2 * s3,
			s2,			-c2 * s1,					c1 * c2
		);
		
		return rMat.transpose().normalize();
		/**/
	}

	/**
	 * Converts the given Euler angle set values to a quaternion
	 */
	public static RQuaternion eulerToQuat(PVector wpr) {
		RMatrix m = eulerToMatrix(wpr);
		
		/*float w, x, y, z;
		float xRot = wpr.x;
		float yRot = wpr.y;
		float zRot = wpr.z;

		w = (float) Math.cos(xRot / 2) * (float) Math.cos(yRot / 2) * (float) Math.cos(zRot / 2)
				+ (float) Math.sin(xRot / 2) * (float) Math.sin(yRot / 2) * (float) Math.sin(zRot / 2);
		x = (float) Math.sin(xRot / 2) * (float) Math.cos(yRot / 2) * (float) Math.cos(zRot / 2)
				- (float) Math.cos(xRot / 2) * (float) Math.sin(yRot / 2) * (float) Math.sin(zRot / 2);
		y = (float) Math.cos(xRot / 2) * (float) Math.sin(yRot / 2) * (float) Math.cos(zRot / 2)
				+ (float) Math.sin(xRot / 2) * (float) Math.cos(yRot / 2) * (float) Math.sin(zRot / 2);
		z = (float) Math.cos(xRot / 2) * (float) Math.cos(yRot / 2) * (float) Math.sin(zRot / 2)
				- (float) Math.sin(xRot / 2) * (float) Math.sin(yRot / 2) * (float) Math.cos(zRot / 2);*/

		return matrixToQuat(m);
	}

	// converts a float array to a double array
	public static double[][] floatToDouble(float[][] m) {
		if(m.length <= 0) return null;
		double[][] r = new double[m.length][m[0].length];

		for (int i = 0; i < m.length; i += 1) {
			for (int j = 0; j < m[0].length; j += 1) {
				r[i][j] = m[i][j];
			}
		}

		return r;
	}

	/**
	 * Define a rotation matrix from the given rotation entries. (For mxy, x is
	 * the row index and y is the column index in the rotation matrix).
	 */
	public static RMatrix formRMat(double m00, double m01, double m02,
			double m10, double m11, double m12,
			double m20, double m21, double m22) {
		
		return new RMatrix(
			new double[][] {
				{ m00, m01, m02 },
				{ m10, m11, m12 },
				{ m20, m21, m22 }
			}
		);
	}
	
	/**
	 * Forms a rotation matrix from the given unit vector, axis, and angle of
	 * rotation, theta, about axis.
	 * 
	 * @param axis		A unit vector representing the axis of rotation
	 * @param theta	The angle of rotaiton around axis
	 * @return		The rotation matrix representing the rotation of theta
	 * 				around axis.
	 */
	public static RMatrix formRMat(PVector axis, float theta) {
		double[][] rMat = new double[3][3];
		
		double ct = Math.cos(theta);
		double st = Math.sin(theta);
		double one_ct = 1f - ct;
		
		rMat[0][0] = ct + axis.x * axis.x * one_ct;
		rMat[0][1] = axis.x * axis.y * one_ct - axis.z * st;
		rMat[0][2] = axis.x * axis.z * one_ct + axis.y * st;
		rMat[1][0] = axis.y * axis.x * one_ct + axis.z * st;
		rMat[1][1] = ct + axis.y * axis.y * one_ct;
		rMat[1][2] = axis.y * axis.z * one_ct - axis.x * st;
		rMat[2][0] = axis.z * axis.x * one_ct - axis.y * st;
		rMat[2][1] = axis.z * axis.y * one_ct + axis.x * st;
		rMat[2][2] = ct + axis.z * axis.z * one_ct;
		
		return new RMatrix(rMat);
	}
	
	/**
	 * Returns a rotation matrix representing the rotation of the given
	 * transformation matrix.
	 * 
	 * @param tMat	A transformation matrix
	 */
	public static RMatrix formRMat(RMatrix tMat) {
		
		return formRMat(
			tMat.getEntry(0, 0), tMat.getEntry(0, 1), tMat.getEntry(0, 2),
			tMat.getEntry(1, 0), tMat.getEntry(1, 1), tMat.getEntry(1, 2),
			tMat.getEntry(2, 0), tMat.getEntry(2, 1), tMat.getEntry(2, 2)
		);
	}
	
	/**
	 * Define a transformation matrix from the given translation entries. (For
	 * mxy, x is the row index and y is the column index in the transformation
	 * matrix).
	 * 
	 * @param m03	The x translation
	 * @param m13	The y translation
	 * @param m23	The z translation
	 */
	public static RMatrix formTMat(double m03, double m13, double m23) {
		return formTMat(
			1.0, 0.0, 0.0, m03,
			0.0, 1.0, 0.0, m13,
			0.0, 0.0, 1.0, m23,
			0.0, 0.0, 0.0, 1.0
		);
	}
	
	/**
	 * Define a transformation matrix from the given rotation and translation
	 * entries. (For mxy, x is the row index and y is the column index in the
	 * transformation matrix).
	 */
	public static RMatrix formTMat(double m00, double m01, double m02,
			double m03, double m10, double m11, double m12, double m13,
			double m20, double m21, double m22, double m23) {
		
		return formTMat(
			m00, m01, m02, m03,
			m10, m11, m12, m13,
			m20, m21, m22, m23,
			0.0, 0.0, 0.0, 1.0
		);
	}
	
	/**
	 * Define a transformation matrix from the given entries (For mxy, x is the
	 * row index and y is the column index in the transformation matrix).
	 */
	public static RMatrix formTMat(double m00, double m01, double m02,
			double m03, double m10, double m11, double m12, double m13,
			double m20, double m21, double m22, double m23, double m30,
			double m31, double m32, double m33) {
		
		return new RMatrix(
				new double[][] {
					{ m00, m01, m02, m03 },
					{ m10, m11, m12, m13 },
					{ m20, m21, m22, m23 },
					{ m30, m31, m32, m33 }
				}
		);
	}
	
	/**
	 * Define a transformation matrix from the given translation vector.
	 * 
	 * @param translation	The translation portion of the matrix
	 */
	public static RMatrix formTMat(final PVector translation) {
		return formTMat(translation.x, translation.y, translation.z);
	}
	
	/**
	 * Forms a transformation matrix with the rotation defined by the given
	 * axis and rotation about the axis.
	 */
	public static RMatrix formTMat(PVector axis, float theta) {
		double[][] tMat = new double[4][4];
		
		double ct = Math.cos(theta);
		double st = Math.sin(theta);
		double one_ct = 1f - ct;
		
		tMat[0][0] = ct + axis.x * axis.x * one_ct;
		tMat[0][1] = axis.x * axis.y * one_ct - axis.z * st;
		tMat[0][2] = axis.x * axis.z * one_ct + axis.y * st;
		tMat[0][3] = 0f;
		tMat[1][0] = axis.y * axis.x * one_ct + axis.z * st;
		tMat[1][1] = ct + axis.y * axis.y * one_ct;
		tMat[1][2] = axis.y * axis.z * one_ct - axis.x * st;
		tMat[1][3] = 0f;
		tMat[2][0] = axis.z * axis.x * one_ct - axis.y * st;
		tMat[2][1] = axis.z * axis.y * one_ct + axis.x * st;
		tMat[2][2] = ct + axis.z * axis.z * one_ct;
		tMat[2][3] = 0f;
		tMat[3][0] = 0f;
		tMat[3][1] = 0f;
		tMat[3][2] = 0f;
		tMat[3][3] = 1f;
		
		return new RMatrix(tMat);
	}
	
	/**
	 * Forms the 4x4 transformation matrix (row major order) form the given
	 * origin offset and axes offset (row major order) of the Native Coordinate
	 * system.
	 * 
	 * @param origin
	 *            the X, Y, Z, offset of the origin for the Coordinate frame
	 * @param axes
	 *            a 3x3 rotatin matrix (row major order) representing the unit
	 *            vector axes offset of the new Coordinate Frame from the Native
	 *            Coordinate Frame
	 * @returning the 4x4 transformation matrix (row major order) formed from
	 *            the given origin and axes offset
	 */
	public static RMatrix formTMat(PVector origin, RMatrix axes) {		
		return formTMat(
			axes.getEntry(0, 0), axes.getEntry(0, 1), axes.getEntry(0, 2), (double)origin.x,
			axes.getEntry(1, 0), axes.getEntry(1, 1), axes.getEntry(1, 2), (double)origin.y,
			axes.getEntry(2, 0), axes.getEntry(2, 1), axes.getEntry(2, 2), (double)origin.z
		);
	}
	
	/**
	 * Forms a transformation matrix from the given rotation matrix.
	 * 
	 * @param axes	The rotation matrix
	 * @return		A transformation matrix with the given rotation: no
	 * 				translation or scaling
	 */
	public static RMatrix fromTMat(RMatrix axes) {		
		return formTMat(
			axes.getEntry(0, 0), axes.getEntry(0, 1), axes.getEntry(0, 2), 0.0,
			axes.getEntry(1, 0), axes.getEntry(1, 1), axes.getEntry(1, 2), 0.0,
			axes.getEntry(2, 0), axes.getEntry(2, 1), axes.getEntry(2, 2), 0.0
		);
	}
	
	/**
	 * Returns the current orientation of the given graphics object in the form
	 * of a 3x3 rotation matrix.
	 * 
	 * @param g	A graphics object
	 * @return	The orientation axes of g
	 */
	public static RMatrix getOrientationAxes(PGraphics g) {
		// Pull the origin and axes vectors from the g's orientation
		PVector origin = getPosition(g, 0f, 0f, 0f),
				vx = getPosition(g, 1f, 0f, 0f).sub(origin),
				vy = getPosition(g, 0f, 1f, 0f).sub(origin),
				vz = getPosition(g, 0f, 0f, 1f).sub(origin);
		
		return formRMat(
			vx.x, vy.x, vz.x,
			vx.y, vy.y, vz.y,
			vx.z, vy.z, vz.z	
		);
	}
	
	/**
	 * Returns the position defined by x, y, and z in g's coordinate frame in
	 * terms of the native coordinate frame.
	 * 
	 * @param g	A graphics object
	 * @param x	The x position in terms of g's coordinate frame
	 * @param y	The y position in terms of g's coordinate frame
	 * @param z The z position in terms of g's coordinate frame
	 * @return	The native coordinate frame position
	 */
	public static PVector getPosition(PGraphics g, float x, float y, float z) {
		PVector position = new PVector();
		position.x = g.modelX(x, y, z);
		position.y = g.modelY(x, y, z);
		position.z = g.modelZ(x, y, z);
		
		return position;
	}
	
	/**
	 * Returns the transformation matrix representing the orientation and
	 * position associated with the given graphic object's coordinate system.
	 * 
	 * @param g	A graphics object
	 * @return	The current transformation associated with g
	 */
	public static RMatrix getTMat(PGraphics g) {
		PVector origin = getPosition(g, 0, 0, 0);
		PVector xAxis = getPosition(g, 1, 0, 0).sub(origin);
		PVector yAxis = getPosition(g, 0, 1, 0).sub(origin);
		PVector zAxis = getPosition(g, 0, 0, 1).sub(origin);

		return formTMat(
				xAxis.x, yAxis.x, zAxis.x, origin.x,
				xAxis.y, yAxis.y, zAxis.y, origin.y,
				xAxis.z, yAxis.z, zAxis.z, origin.z
		);
	}
	
	/**
	 * Attempts to calculate the joint angles that would place the Robot in the
	 * given target position and orientation. The srcAngles parameter defines
	 * the position of the Robot from which to move, since this inverse
	 * kinematics uses a relative conversion formula. There is no guarantee that
	 * the target position and orientation can be reached; in the case that
	 * inverse kinematics fails, then null is returned. Otherwise, a set of six
	 * angles will be returned, though there is no guarantee that these angles
	 * are valid!
	 * 
	 * @param model
	 *            The Robot model of which to base the inverse kinematics off
	 * @param startAngles
	 *            The initial position of the Robot
	 * @param tgtPosition
	 *            The desired position of the Robot
	 * @param tgtOrientation
	 *            The desired orientation of the Robot
	 */
	public static float[] inverseKinematics(RoboticArm model, float[] startAngles, PVector tgtPosition,
			RQuaternion tgtOrientation) {

		final int limit = 1000; // Max number of times to loop
		int count = 0;

		float[] angles = startAngles.clone();

		while (count < limit) {
			Point cPoint = model.getToolTipNative(angles);

			if (tgtOrientation.dot(cPoint.orientation) < 0f) {
				// Use -q instead of q
				tgtOrientation.scalarMult(-1);
			}

			// calculate our translational offset from target
			PVector tDelta = PVector.sub(tgtPosition, cPoint.position);
			// calculate our rotational offset from target
			RQuaternion rDelta = RQuaternion.addValues(tgtOrientation, RQuaternion.scalarMult(-1, cPoint.orientation));
			float[] delta = new float[7];

			delta[0] = tDelta.x;
			delta[1] = tDelta.y;
			delta[2] = tDelta.z;
			delta[3] = rDelta.getValue(0);
			delta[4] = rDelta.getValue(1);
			delta[5] = rDelta.getValue(2);
			delta[6] = rDelta.getValue(3);

			float dist = PVector.dist(cPoint.position, tgtPosition);
			float rDist = rDelta.magnitude();
			// check whether our current position is within tolerance
			if (dist <= (0.001f * model.getLiveSpeed()) &&
					rDist <= (0.00005f * model.getLiveSpeed())) {
				
				return angles;
			}

			// calculate jacobian, 'J', and its inverse
			RMatrix J = calculateJacobian(model, angles, true);
			RMatrix JInverse = J.getInverse();

			// calculate and apply joint angular changes
			float[] dAngle = { 0, 0, 0, 0, 0, 0 };
			for (int i = 0; i < 6; i += 1) {
				for (int j = 0; j < 7; j += 1) {
					dAngle[i] += JInverse.getEntry(i, j) * delta[j];
				}
				
				angles[i] = RMath.mod2PI(angles[i] + dAngle[i]);
			}
			
			++count;
			
			if (count == limit) {
				//Fields.debug("IK\n%s\n", J.toString());
			}
		}

		return null;
	}

	/**
	 * Computes the inverse of the given row major 4x4 Homogeneous Coordinate Matrix.
	 * 
	 * This method is based off of the algorithm found on this webpage:
	 * https://web.archive.org/web/20130806093214/http://www-graphics.stanford.edu/
	 * courses/cs248-98-fall/Final/q4.html
	 */
	public static RMatrix invertHCMatrix(RMatrix mat) {
		float[][] d = mat.getDataF();
		if (d.length != 4 || d[0].length != 4) {
			return null;
		}

		float[][] inv = new float[4][4];
		
		/*
		 * [ ux ux wx tz ] -1		[ ux uy uz -dot(u, t) ]
		 * [ uy uy wy ty ]		=	[ vx vy vz -dot(v, t) ]
		 * [ uz uz wz tw ]			[ wx wy wz -dot(w, t) ]
		 * [  0  0  0  1 ]			[  0  0  0          1 ]
		 */
		inv[0][0] = d[0][0];
		inv[0][1] = d[1][0];
		inv[0][2] = d[2][0];
		inv[0][3] = -(d[0][0] * d[0][3] + d[1][0] * d[1][3] + d[2][0] * d[2][3]);
		inv[1][0] = d[0][1];
		inv[1][1] = d[1][1];
		inv[1][2] = d[2][1];
		inv[1][3] = -(d[0][1] * d[0][3] + d[1][1] * d[1][3] + d[2][1] * d[2][3]);
		inv[2][0] = d[0][2];
		inv[2][1] = d[1][2];
		inv[2][2] = d[2][2];
		inv[2][3] = -(d[0][2] * d[0][3] + d[1][2] * d[1][3] + d[2][2] * d[2][3]);
		inv[3][0] = 0;
		inv[3][1] = 0;
		inv[3][2] = 0;
		inv[3][3] = 1;

		return new RMatrix(inv);
	}

	/**
	 * Tests orientation conversion methods
	 * 
	 * @param args	Unused
	 */
	public static void main(String[] args) {
		
		//float[] testVals = new float[] { 0f, 1f / 8f, 1f / 4f,  };
		
		PVector wpr = new PVector(-170f, 90f, 170f);
		wpr.mult(PConstants.DEG_TO_RAD);
		
		RMatrix m = eulerToMatrix(wpr);
		
		PVector wpr1 = matrixToEuler(m);
		
		System.out.printf("\n%s\n%s\n%s\n", wpr, m, wpr1);
	}

	// calculates euler angles from rotation matrix
	public static PVector matrixToEuler(RMatrix m) {
		float x, y, z;
		
		m.normalize();
		
		/**
		double s2 = m.getEntry(0, 2);
		
		if (Math.abs(1 - s2) > 0.00002f) {
			// No singularity
			double c2 = Math.sqrt(
					m.getEntry(2, 1)*m.getEntry(2, 1) +
					m.getEntry(2, 2)*m.getEntry(2, 2)
					);
			
			x = (float) atan2Rounded(m.getEntry(2, 1) / -c2, m.getEntry(2, 2) / c2);
			y = (float) atan2Rounded(s2, c2);
			z = (float) atan2Rounded(m.getEntry(1, 0) / -c2, m.getEntry(0, 0) / c2);
			
		} else {
			// Set z rotation to some value
			z = 0;
			
			if (RMath.sign(s2) == 1) {
				y = -PConstants.PI / 2;
				x = (float) atan2Rounded(m.getEntry(1, 2), m.getEntry(1, 1)) + z;
				
			} else {
				y = PConstants.PI / 2;
				x = (float) atan2Rounded(m.getEntry(1, 2), m.getEntry(1, 1)) - z;
			}
		}
		
		/**/
		x = (float) atan2Rounded(-m.getEntry(2, 1), m.getEntry(2, 2));
		y = (float) atan2Rounded(m.getEntry(2, 0), Math.sqrt(
				m.getEntry(2, 1)*m.getEntry(2, 1) +
				m.getEntry(2, 2)*m.getEntry(2, 2)
				));
		z = (float) atan2Rounded(-m.getEntry(1, 0), m.getEntry(0, 0));
		
		/**/
		
		return new PVector(x, y, z);
	}
	
	// calculates quaternion from rotation matrix
	public static RQuaternion matrixToQuat(RMatrix m) {
		float[][] d = m.getDataF();
		float[] qVals = new float[4];
		float diag = d[0][0] + d[1][1] + d[2][2];

		if (diag > 0) {
			float S = (float) Math.sqrt(1.0f + diag) * 2; // S=4*q[0]
			qVals[0] = S / 4;
			qVals[1] = (d[1][2] - d[2][1]) / S;
			qVals[2] = (d[2][0] - d[0][2]) / S;
			qVals[3] = (d[0][1] - d[1][0]) / S;
		} else if (d[0][0] > d[1][1] & d[0][0] > d[2][2]) {
			float S = (float) Math.sqrt(1.0f + d[0][0] - d[1][1] - d[2][2]) * 2; // S=4*q[1]
			qVals[0] = (d[1][2] - d[2][1]) / S;
			qVals[1] = S / 4;
			qVals[2] = (d[1][0] + d[0][1]) / S;
			qVals[3] = (d[2][0] + d[0][2]) / S;
		} else if (d[1][1] > d[2][2]) {
			float S = (float) Math.sqrt(1.0f + d[1][1] - d[0][0] - d[2][2]) * 2; // S=4*q[2]
			qVals[0] = (d[2][0] - d[0][2]) / S;
			qVals[1] = (d[1][0] + d[0][1]) / S;
			qVals[2] = S / 4;
			qVals[3] = (d[2][1] + d[1][2]) / S;
		} else {
			float S = (float) Math.sqrt(1.0f + d[2][2] - d[0][0] - d[1][1]) * 2; // S=4*q[3]
			qVals[0] = (d[0][1] - d[1][0]) / S;
			qVals[1] = (d[2][0] + d[0][2]) / S;
			qVals[2] = (d[2][1] + d[1][2]) / S;
			qVals[3] = S / 4;
		}
		
		RQuaternion q = new RQuaternion(qVals[0], qVals[1], qVals[2], qVals[3]);
		q.normalize();

		return q;
	}
	
	/**
	 * Finds the maximum value amongst all given float values.
	 * 
	 * @param args	A set of float values
	 * @return		The maximum amongst all values of args
	 */
	public static float max(float... args) {
		
		if (args == null || args.length == 0) {
			// Because why not
			return Float.MAX_VALUE;
			
		} else if (args.length == 1) {
			// No comparison needed
			return args[0];
		}
		
		// Find the maximum amongst all given values
		float max = Float.MIN_VALUE;
		
		for (float val : args) {
			if (val > max) {
				max = val;
			}
		}
		
		return max;
	}
	
	/**
	 * Finds the maximum value amongst all given integer values.
	 * 
	 * @param args	A set of integer values
	 * @return		The maximum amongst all values of args
	 */
	public static int max(int... args) {
		
		if (args == null || args.length == 0) {
			// Because why not
			return Integer.MAX_VALUE;
			
		} else if (args.length == 1) {
			// No comparison needed
			return args[0];
		}
		
		// Find the maximum amongst all given values
		int max = Integer.MIN_VALUE;
		
		for (int val : args) {
			if (val > max) {
				max = val;
			}
		}
		
		return max;
	}
	
	/**
	 * Finds the minimum value amongst all given float values.
	 * 
	 * @param args	A set of float values
	 * @return		The minimum amongst all values of args
	 */
	public static float min(float... args) {
		
		if (args == null || args.length == 0) {
			// Because why not
			return Float.MIN_VALUE;
			
		} else if (args.length == 1) {
			// No comparison needed
			return args[0];
		}
		
		// Find the minimum amongst all given values
		float min = Float.MAX_VALUE;
		
		for (float val : args) {
			if (val < min) {
				min = val;
			}
		}
		
		return min;
	}
	
	/**
	 * Find the minimum value amongst all given integer values.
	 * 
	 * @param args	A set of integer values
	 * @return		The minimum amongst all values of args
	 */
	public static int min(int... args) {
		
		if (args == null || args.length == 0) {
			// Because why not
			return Integer.MIN_VALUE;
			
		} else if (args.length == 1) {
			// No comparison needed
			return args[0];
			
		}
		
		// Find the minimum amongst all given values
		int min = Integer.MAX_VALUE;
		
		for (int val : args) {
			if (val < min) {
				min = val;
			}
		}
		
		return min;
	}

	/**
	 * Computes the minimum rotational magnitude to move from src to dest,
	 * around the unit circle.
	 * 
	 * @param src
	 *            The source angle in radians
	 * @param dset
	 *            The destination angle in radians
	 * @returning The minimum distance between src and dest
	 */
	public static float minDist(float src, float dest) {
		// Bring angles within range [0, TWO_PI)
		float difference = mod2PI(dest) - mod2PI(src);

		if (difference > PI) {
			difference -= TWO_PI;
		} else if (difference < -PI) {
			difference += TWO_PI;
		}

		return difference;
	}

	/**
	 * Brings the given angle (in radians) within the range: [0, TWO_PI).
	 * 
	 * @param angle
	 *            Some rotation in radians
	 * @returning The equivalent angle within the range [0, TWO_PI)
	 */
	public static float mod2PI(float angle) {
		float temp = angle % TWO_PI;

		if (temp < 0f) {
			temp += TWO_PI;
		}

		return temp;
	}
	
	/**
	 * Takes a quaternion and converts it to euler angles, in degrees. Then the
	 * world frame orientation is applied to the orientation.
	 * 
	 * @param q	A normalized quanternion
	 * @return	A set of euler angles, in degrees, representing the orientation
	 * 			of the quaternion with reference to the world frame
	 */
	public static PVector nQuatToWEuler(RQuaternion q) {
		// Convert to euler angles
		PVector wpr = quatToEuler(q);
		wpr.mult(RAD_TO_DEG);
		
		float limbo = wpr.y;
		// Convert to world frame
		wpr.x *= -1;
		wpr.y = wpr.z;
		wpr.z = -limbo;
		
		return wpr;
	}
	
	/**
	 * Takes in a 3x3 rotation matrix and converts it to euler angles, in
	 * degrees. Then, the world frame orientation is applied to the
	 * orientation.
	 * 
	 * @param m	A 3x3 rotation matrix
	 * @return	A set of euler angles, in degrees, representing the
	 * 			orientation of the rotation matrix with reference to the world
	 * 			frame
	 */
	public static PVector nRMatToWEuler(RMatrix m) {
		// Convert to euler angles
		PVector wpr = matrixToEuler(m);
		wpr.mult(RAD_TO_DEG);
		
		float limbo = wpr.y;
		// Convert to world frame
		wpr.x *= -1;
		wpr.y = wpr.z;
		wpr.z = -limbo;
		
		return wpr;
	}

	public static void printMat(RMatrix mat) {
		System.out.println(mat.toString());
	}

	// calculates euler angles from quaternion
	public static PVector quatToEuler(RQuaternion q) {
		RMatrix r = q.toMatrix();
		return matrixToEuler(r);
	}

	/**
	 * Converts the given point, pt, from the Coordinate System defined by the
	 * given origin vector and rotation quaternion axes. The joint angles
	 * associated with the point will be transformed as well, though, if inverse
	 * kinematics fails, then the original joint angles are used instead.
	 * 
	 * @param model
	 *            The Robot, to which the frame belongs
	 * @param pt
	 *            A point with initialized position and orientation
	 * @param origin
	 *            The origin of the Coordinate System
	 * @param axes
	 *            The axes of the Coordinate System representing as a rotation
	 *            quanternion
	 * @returning The point, pt, interms of the given frame's Coordinate System
	 */
	public static Point removeFrame(RoboticArm model, Point pt, PVector origin, RQuaternion axes) {
		PVector position = vFromFrame(pt.position, origin, axes);
		RQuaternion orientation = RQuaternion.mult(pt.orientation, axes);

		// Update joint angles associated with the point
		float[] newJointAngles = RMath.inverseKinematics(model, pt.angles, position, orientation);

		if (newJointAngles != null) {
			return new Point(position, orientation, newJointAngles);
		} else {
			// If inverse kinematics fails use the old angles
			return new Point(position, orientation, pt.angles);
		}
	}
	
	/**
	 * Converts the rotation matrix from the native coordinate frame to the
	 * world frame.
	 * 
	 * @param rMat	A valid rotation matrix
	 * @return		The rotation matrix in terms of the world frame
	 */
	public static RMatrix rMatToWorld(RMatrix rMat) {
		return rMat.multiply(Fields.WORLD_AXES_MAT);
	}
	
	/**
	 * Applies the orientation represented by the given 3x3 rotation matrix,
	 * m, to the given position vector, v.
	 * 
	 * @param v	A 3D position vector
	 * @param r	A 3x3 rotation matrix
	 * @return	v rotated by m
	 */
	public static PVector rotateVector(PVector v, float[][] r) {
		if (r.length != 3 || r[0].length != 3) {
			return null;
		}
		
		PVector u = new PVector();
		// Apply the rotation matrix to the given vector
		u.x = v.x * r[0][0] + v.y * r[1][0] + v.z * r[2][0];
		u.y = v.x * r[0][1] + v.y * r[1][1] + v.z * r[2][1];
		u.z = v.x * r[0][2] + v.y * r[1][2] + v.z * r[2][2];

		return u;
	}
	
	/**
	 * Applies the orientation represented by the given rotation matrix, rMat,
	 * to the given position vector, v.
	 * 
	 * @param v	A 3D position vector
	 * @param r	A 3x3 rotation matrix
	 * @return	v rotated by rMat
	 */
	public static PVector rotateVector(final PVector v, final RMatrix rMat) {
		
		PVector u = new PVector();
		// Apply the rotation matrix to the given vector
		u.x = rMat.getEntryF(0, 0) * v.x + rMat.getEntryF(1, 0) * v.y +
						rMat.getEntryF(2, 0) * v.z;
		u.y = rMat.getEntryF(0, 1) * v.x + rMat.getEntryF(1, 1) * v.y +
						rMat.getEntryF(2, 1) * v.z;
		u.z = rMat.getEntryF(0, 2) * v.x + rMat.getEntryF(1, 2) * v.y +
						rMat.getEntryF(2, 2) * v.z;
		
		return u;
	}
	
	/**
	 * Returns a -1 or 1 depending on the sign of the given double value.
	 * Although, if the given value is NaN, then 0 is returned.
	 * 
	 * @param val	A double value
	 * @return		-1 for negative values, 1 for positive values, or 0 for
	 * 				NaN
	 */
	public static int sign(double val) {
		
		if (Double.isNaN(val)) {
			// No sign for NaN
			return 0;
		}
		
		return (val < 0.0) ? -1 : 1;
	}
	
	/**
	 * Returns a -1 or 1 depending on the sign of the given float value.
	 * Although, if the given value is NaN, then 0 is returned.
	 * 
	 * @param val	A floating-point value
	 * @return		-1 for negative values, 1 for positive values, or 0 for
	 * 				NaN
	 */
	public static int sign(float val) {
		
		if (Float.isNaN(val)) {
			// No sign for NaN
			return 0;
		}
		
		return (val < 0f) ? -1 : 1;
	}
	
	/**
	 * Returns the sign of the given integer value. Zero is assumed to be
	 * positive.
	 * 
	 * @param val	An integer value
	 * @return		-1 for negative values, or 1 for positive values and zero
	 */
	public static int sign(int val) {
		return (val < 0f) ? -1 : 1;
	}
	
	/**
	 * Applies the given translations to the given transformation
	 * matrix, tMat.
	 * 
	 * @param tMat	A transformation matrix
	 * @param tx	The x translation
	 * @param ty	The y translation
	 * @param tz	The z translation
	 */
	public static void translateTMat(RMatrix tMat, double tx, double ty,
			double tz) {
		// Calculate the new translations for the transformation matrix
		double newX = tMat.getEntry(0, 0) * tx + tMat.getEntry(0, 1) * ty +
						tMat.getEntry(0, 2) * tz;
		
		double newY = tMat.getEntry(1, 0) * tx + tMat.getEntry(1, 1) * ty +
						tMat.getEntry(1, 2) * tz;
		
		double newZ = tMat.getEntry(2, 0) * tx + tMat.getEntry(2, 1) * ty +
						tMat.getEntry(2, 2) * tz;
		
		tMat.setEntry(0, 3, tMat.getEntry(0, 3) + newX);
		tMat.setEntry(1, 3, tMat.getEntry(1, 3) + newY);
		tMat.setEntry(2, 3, tMat.getEntry(2, 3) + newZ);
	}
	
	/**
	 * Applies the given translation, delta, to the given transformation
	 * matrix, tMat.
	 * 
	 * @param tMat	A transformation matrix
	 * @param delta	The translation to apply to tMat
	 */
	public static void translateTMat(RMatrix tMat, final PVector delta) {
		translateTMat(tMat, delta.x, delta.y, delta.z);
	}

	/**
	 * Multiplies a 3 element vector, v, by the given 4x4 transformation matrix, t.
	 * 
	 * @param v	A 3D position vector
	 * @param t	A column-major, 4x4 transformation matrix
	 * @return	A vector containing the product of v and t
	 */
	public static PVector vectorMatrixMult(PVector v, RMatrix mat) {
		float[][] d = mat.getDataF();
		
		if (d.length != 4 || d[0].length != 4) {
			return null;
		}

		PVector u = new PVector();
		// Apply the transformation matrix to the given vector
		u.x = v.x * d[0][0] + v.y * d[0][1] + v.z * d[0][2] + d[0][3];
		u.y = v.x * d[1][0] + v.y * d[1][1] + v.z * d[1][2] + d[1][3];
		u.z = v.x * d[2][0] + v.y * d[2][1] + v.z * d[2][2] + d[2][3];
		float w = v.x*d[3][0] + v.y*d[3][1] + v.z*d[3][2] + d[3][3];
		
		if(w != 1) {
			u.div(w);
		}

		return u;
	}
	
	// returns the result of a vector 'v' multiplied by scalar 's'
	public static float[] vectorScalarMult(float[] v, float s) {
		float[] ret = new float[v.length];
		for (int i = 0; i < ret.length; i += 1) {
			ret[i] = v[i] * s;
		}

		return ret;
	}
	
	/**
	 * Converts the given vector, u, from the Coordinate System defined by the
	 * given origin vector and rotation quaternion axes.
	 * 
	 * @param v
	 *            A vector in the XYZ vector space
	 * @param origin
	 *            The origin of the Coordinate System
	 * @param axes
	 *            The axes of the Coordinate System representing as a rotation
	 *            quanternion
	 * @returning The vector, u, in the Native frame
	 */
	public static PVector vFromFrame(PVector u, PVector origin, RQuaternion axes) {
		RQuaternion invAxes = axes.conjugate();
		invAxes.normalize();
		PVector vRotated = invAxes.rotateVector(u);
		return vRotated.add(origin);
	}
	
	/**
	 * Applies the inverse of the world coordinate frame onto the given
	 * position vector, effectively removing the world coordinate frame
	 * transformation.
	 * 
	 * @param v	a xyz position vector
	 * @return	v transformed by the inverse world coordinate system
	 */
	public static PVector vFromWorld(PVector v) {
		return RMath.rotateVector(v, Fields.NATIVE_AXES_MAT);
	}
	
	/**
	 * Converts the given vector, v, into the Coordinate System defined by the
	 * given origin vector and rotation quaternion axes.
	 * 
	 * @param v
	 *            A vector in the XYZ vector space
	 * @param origin
	 *            The origin of the Coordinate System
	 * @param axes
	 *            The axes of the Coordinate System representing as a rotation
	 *            quanternion
	 * @returning The vector, v, interms of the given frame's Coordinate System
	 */
	public static PVector vToFrame(PVector v, PVector origin, RQuaternion axes) {
		PVector vOffset = PVector.sub(v, origin);
		return axes.rotateVector(vOffset);
	}
	
	/**
	 * Transforms the given position vector into the world coordinate frame.
	 * 
	 * @param v	a xyz position vector
	 * @return	v transformed by the world coordinate frame
	 */
	public static PVector vToWorld(PVector v) {
		return RMath.rotateVector(v, Fields.WORLD_AXES_MAT);
	}
	

	
	/**
	 * Takes a set of euler angles, in degrees and applies the inverse of the
	 * world frame to the rotations and converts the euler angles to a
	 * quaternion.
	 * 
	 * @param wpr	A set of euler angles, in degrees
	 * @return		A quaternion representing the product of the given
	 * 				orientation and the inverse of the world frame
	 * 				orientation
	 */
	public static RQuaternion wEulerToNQuat(PVector wpr) {
		float limbo = wpr.y;
		// Convert from world frame
		wpr.x *= -1;
		wpr.y = -wpr.z;
		wpr.z = limbo;
		// Convert to radians
		wpr.mult(DEG_TO_RAD);
		
		return RMath.eulerToQuat(wpr);
	}
	
	/**
	 * Takes a set of euler angles, in degrees, and applies the inverse of the
	 * world frame to the orientation and converts the euler angles to a
	 * 3x3 rotation matrix.
	 * 
	 * @param wpr	A set of euler angles, in degrees
	 * @return		A rotation matrix representing the product of the given
	 * 				orientation and the inverse of the world frame
	 * 				orientation
	 */
	public static RMatrix wEulerToNRMat(PVector wpr) {
		float limbo = wpr.y;
		// Convert from world frame
		wpr.x *= -1;
		wpr.y = -wpr.z;
		wpr.z = limbo;
		// Convert to radians
		wpr.mult(DEG_TO_RAD);
		
		return RMath.eulerToMatrix(wpr);
	}
}
