package geom;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.SingularValueDecomposition;

import global.Fields;
import processing.core.PConstants;
import processing.core.PVector;
import robot.RobotRun;
import robot.RoboticArm;

public class RMath {
	static final float DEG_TO_RAD = RobotRun.DEG_TO_RAD;
	static final float PI = RobotRun.PI;
	
	static final float RAD_TO_DEG = RobotRun.RAD_TO_DEG;
	static final float TWO_PI = RobotRun.TWO_PI;
	
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
	public static boolean angleWithinBounds(float angleToVerify, float rangeStart, float rangeEnd) {

		if (rangeStart < rangeEnd) {
			// Joint range does not overlap TWO_PI
			return (angleToVerify - rangeStart) > -0.0001f && (angleToVerify - rangeEnd) < 0.0001f;
			// return angleToVerify >= rangeStart && angleToVerify <= rangeEnd;
		} else {
			// Joint range overlaps TWO_PI
			return !((angleToVerify - rangeEnd) > -0.0001f && (angleToVerify - rangeStart) < 0.0001f);
			// return !(angleToVerify > rangeEnd && angleToVerify < rangeStart);
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
		// get current ee position
		Point curRP = RobotRun.nativeRobotEEPoint(model, angles);

		// examine each segment of the arm
		for (int i = 0; i < 6; i += 1) {
			// test angular offset
			angles[i] += dAngle;
			// get updated ee position
			Point newRP = RobotRun.nativeRobotEEPoint(model, angles);

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
		float[][] r = new float[3][3];
		float xRot = wpr.x;
		float yRot = wpr.y;
		float zRot = wpr.z;

		r[0][0] = (float) Math.cos(yRot) * (float) Math.cos(zRot);
		r[1][0] = (float) Math.sin(xRot) * (float) Math.sin(yRot) * (float) Math.cos(zRot)
				- (float) Math.cos(xRot) * (float) Math.sin(zRot);
		r[2][0] = (float) Math.cos(xRot) * (float) Math.sin(yRot) * (float) Math.cos(zRot)
				+ (float) Math.sin(xRot) * (float) Math.sin(zRot);
		r[0][1] = (float) Math.cos(yRot) * (float) Math.sin(zRot);
		r[1][1] = (float) Math.sin(xRot) * (float) Math.sin(yRot) * (float) Math.sin(zRot)
				+ (float) Math.cos(xRot) * (float) Math.cos(zRot);
		r[2][1] = (float) Math.cos(xRot) * (float) Math.sin(yRot) * (float) Math.sin(zRot)
				- (float) Math.sin(xRot) * (float) Math.cos(zRot);
		r[0][2] = -(float) Math.sin(yRot);
		r[1][2] = (float) Math.sin(xRot) * (float) Math.cos(yRot);
		r[2][2] = (float) Math.cos(xRot) * (float) Math.cos(yRot);

		return new RMatrix(r).normalize();
	}

	/**
	 * Converts the given Euler angle set values to a quaternion
	 */
	public static RQuaternion eulerToQuat(PVector wpr) {
		float w, x, y, z;
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
				- (float) Math.sin(xRot / 2) * (float) Math.sin(yRot / 2) * (float) Math.cos(zRot / 2);

		return new RQuaternion(w, x, y, z);
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
	 * @param srcAngles
	 *            The initial position of the Robot
	 * @param tgtPosition
	 *            The desired position of the Robot
	 * @param tgtOrientation
	 *            The desired orientation of the Robot
	 */
	public static float[] inverseKinematics(RoboticArm model, float[] srcAngles, PVector tgtPosition,
			RQuaternion tgtOrientation) {

		final int limit = 1000; // Max number of times to loop
		int count = 0;

		float[] angles = srcAngles.clone();

		while (count < limit) {
			Point cPoint = RobotRun.nativeRobotEEPoint(model, angles);
			float cumulativeOffset = 0;

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
			if (dist < RobotRun.getActiveRobot().getLiveSpeed() / 100f && rDist < 0.00005f * RobotRun.getActiveRobot().getLiveSpeed()) {
				break;
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

				// update joint angles
				cumulativeOffset += dAngle[i];
				// prevents IK algorithm from producing unrealistic motion
				if (Math.abs(cumulativeOffset) > PConstants.PI) {
					// System.out.println("Optimal solution not found.");
					// return null;
				}
				angles[i] += dAngle[i];
				angles[i] += TWO_PI;
				angles[i] %= TWO_PI;
			}

			// System.out.println(String.format("IK result for cycle %d: [%f,
			// %f, %f, %f, %f, %f]", count, angles[0], angles[1], angles[2],
			// angles[3], angles[4], angles[5]));
			count += 1;
			if (count == limit) {
				System.out.printf("%s\n", J.toString());
				return null;
			}
		}

		return angles;
	}

	/**
	 * Computes the inverse of the given row major 4x4 Homogeneous Coordinate Matrix.
	 * 
	 * This method is based off of the algorithm found on this webpage:
	 * https://web.archive.org/web/20130806093214/http://www-graphics.stanford.edu/
	 * courses/cs248-98-fall/Final/q4.html
	 */
	public static RMatrix invertHCMatrix(RMatrix mat) {
		float[][] d = mat.getFloatData();
		if (d.length != 4 || d[0].length != 4) {
			return null;
		}

		float[][] inv = new float[4][4];
		
		/*
		 * [ ux uy uz tz ] -1		[ ux vx wx -dot(u, t) ]
		 * [ vx vy vz ty ]		=	[ uy vy wy -dot(v, t) ]
		 * [ vx vy vz tw ]			[ uy vy wy -dot(w, t) ]
		 * [  0  0  0  1 ]			[  0  0  0          1 ]
		 */
		inv[0][0] = d[0][0];
		inv[0][1] = d[1][0];
		inv[0][2] = d[2][0];
		inv[0][3] = -(d[0][0] * d[0][3] + d[0][1] * d[1][3] + d[0][2] * d[2][3]);
		inv[1][0] = d[0][1];
		inv[1][1] = d[1][1];
		inv[1][2] = d[2][1];
		inv[1][3] = -(d[1][0] * d[0][3] + d[1][1] * d[1][3] + d[1][2] * d[2][3]);
		inv[2][0] = d[0][2];
		inv[2][1] = d[1][2];
		inv[2][2] = d[2][2];
		inv[2][3] = -(d[2][0] * d[0][3] + d[2][1] * d[1][3] + d[2][2] * d[2][3]);
		inv[3][0] = 0;
		inv[3][1] = 0;
		inv[3][2] = 0;
		inv[3][3] = 1;

		return new RMatrix(inv);
	}

	// calculates euler angles from rotation matrix
	public static PVector matrixToEuler(RMatrix m) {
		float[][] r = m.getFloatData();
		float yRot1, xRot1, zRot1;
		PVector wpr;

		if (r[0][2] != 1 && r[0][2] != -1) {
			// rotation about y-axis
			yRot1 = -(float) Math.asin(r[0][2]);
			// rotation about x-axis
			xRot1 = (float) Math.atan2(r[1][2] / (float) Math.cos(yRot1), r[2][2] / (float) Math.cos(yRot1));
			// rotation about z-axis
			zRot1 = (float) Math.atan2(r[0][1] / (float) Math.cos(yRot1), r[0][0] / (float) Math.cos(yRot1));
		} else {
			zRot1 = 0;
			if (r[0][2] == -1) {
				yRot1 = PI / 2;
				xRot1 = zRot1 + (float) Math.atan2(r[1][0], r[2][0]);
			} else {
				yRot1 = -PI / 2;
				xRot1 = -zRot1 + (float) Math.atan2(-r[1][0], -r[2][0]);
			}
		}

		wpr = new PVector(xRot1, yRot1, zRot1);
		return wpr;
	}
	
	// calculates quaternion from rotation matrix
	public static RQuaternion matrixToQuat(RMatrix m) {
		float[][] d = m.getFloatData();
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
	 * Returns a string that represents the given floating-point matrix in the
	 * format:
	 * 
	 * [ XXXXX.XXX XXXXX.XXX ... XXXXX.XXX ]
	 * [ XXXXX.XXX XXXXX.XXX ... XXXXX.XXX ]
	 *   .
	 *   .
	 *   .
	 * [ XXXXX.XXX XXXXX.XXX ... XXXXX.XXX ]
	 * 
	 * The precision of each element is 4 digits before and 3 digits after the
	 * decimal point. In addition, space padding is applied for non-negative
	 * values.
	 * 
	 * @param matrix	A floating-point matrix
	 * @return			The string representation of the given matrix
	 */
	public static String matrixToString(float[][] matrix) {
		String str = new String();
		
		for (int row = 0; row < matrix.length; ++row) {
			str += "[ ";
			
			for (int column = 0; column < matrix[row].length; ++column) {
				String val = String.format("%4.3f", matrix[row][column]);
				// Add padding
				str += String.format("%9s ", val);
			}
			
			str += "]\n";
		}
		
		
		return str;
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
	public static float minimumDistance(float src, float dest) {
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

	public static void printMat(RMatrix mat) {
		float[][] d = mat.getFloatData();
		
		for (int i = 0; i < d.length; i += 1) {
			System.out.print("[");
			for (int j = 0; j < d[0].length; j += 1) {
				if (j < d[0].length - 1) {
					System.out.print(String.format("%12f, ", d[i][j]));
				} else {
					System.out.print(String.format("%12f", d[i][j]));
				}
			}
			System.out.println("]");
		}
		System.out.println();
	}

	// calculates euler angles from quaternion
	public static PVector quatToEuler(RQuaternion q) {
		RMatrix r = q.toMatrix();
		PVector wpr = matrixToEuler(r);
		return wpr;
	}
	
	// calculates rotation matrix from quaternion
	public static RMatrix quatToMatrix(RQuaternion q) {
		float[][] r = new float[3][3];

		r[0][0] = 1 - 2 * (q.getValue(2) * q.getValue(2) + q.getValue(3) * q.getValue(3));
		r[1][0] = 2 * (q.getValue(1) * q.getValue(2) - q.getValue(0) * q.getValue(3));
		r[2][0] = 2 * (q.getValue(0) * q.getValue(2) + q.getValue(1) * q.getValue(3));
		r[0][1] = 2 * (q.getValue(1) * q.getValue(2) + q.getValue(0) * q.getValue(3));
		r[1][1] = 1 - 2 * (q.getValue(1) * q.getValue(1) + q.getValue(3) * q.getValue(3));
		r[2][1] = 2 * (q.getValue(2) * q.getValue(3) - q.getValue(0) * q.getValue(1));
		r[0][2] = 2 * (q.getValue(1) * q.getValue(3) - q.getValue(0) * q.getValue(2));
		r[1][2] = 2 * (q.getValue(0) * q.getValue(1) + q.getValue(2) * q.getValue(3));
		r[2][2] = 1 - 2 * (q.getValue(1) * q.getValue(1) + q.getValue(2) * q.getValue(2));

		return new RMatrix(r).normalize();
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

	// Rotates the matrix 'm' by an angle 'theta' around the given 'axis'
	public static RMatrix rotateAxisVector(RMatrix m, float theta, PVector axis) {
		float s = (float) Math.sin(theta);
		float c = (float) Math.cos(theta);
		float t = 1 - c;

		if (c > 0.9f) {
			t = (float)(2 * Math.sin(theta / 2) * Math.sin(theta / 2));
		}

		float x = axis.x;
		float y = axis.y;
		float z = axis.z;
		
		float[][] r = new float[3][3];

		r[0][0] = x * x * t + c;
		r[1][0] = x * y * t - z * s;
		r[2][0] = x * z * t + y * s;
		r[0][1] = y * x * t + z * s;
		r[1][1] = y * y * t + c;
		r[2][1] = y * z * t - x * s;
		r[0][2] = z * x * t - y * s;
		r[1][2] = z * y * t + x * s;
		r[2][2] = z * z * t + c;
		
		RMatrix R = new RMatrix(r);
		RMatrix MR = m.multiply(R);

		return MR;
	}
	
	/*
	 * Transforms the given vector from the coordinate system defined by the
	 * given transformation matrix (row major order).
	 */
	public static PVector rotateVector(PVector v, float[][] r) {
		if (r.length != 3 || r[0].length != 3) {
			return null;
		}
		
		PVector u = new PVector();
		// Apply the rotation matrix to the given vector
		u.x = v.x * r[0][0] + v.y * r[0][1] + v.z * r[0][2];
		u.y = v.x * r[1][0] + v.y * r[1][1] + v.z * r[1][2];
		u.z = v.x * r[2][0] + v.y * r[2][1] + v.z * r[2][2];

		return u;
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
	public static RMatrix transformationMatrix(PVector origin, RMatrix axes) {
		float[][] d = axes.getFloatData();
		float[][] mat = new float[4][4];
		
		mat[0][0] = d[0][0];
		mat[0][1] = d[0][1];
		mat[0][2] = d[0][2];
		mat[0][3] = origin.x;
		
		mat[1][0] = d[1][0];
		mat[1][1] = d[1][1];
		mat[1][2] = d[1][2];
		mat[1][3] = origin.y;
		
		mat[2][0] = d[2][0];
		mat[2][1] = d[2][1];
		mat[2][2] = d[2][2];
		mat[2][3] = origin.z;
		
		mat[3][0] = 0;
		mat[3][1] = 0;
		mat[3][2] = 0;
		mat[3][3] = 1;
		
		return new RMatrix(mat);
	}

	/**
	 * Multiplies a 3 element vector, v, by the given 4x4 transformation matrix, t.
	 * 
	 * @param v	A 3D position vector
	 * @param t	A column-major, 4x4 transformation matrix
	 * @return	A vector containing the product of v and t
	 */
	public static PVector vectorMatrixMult(PVector v, RMatrix mat) {
		float[][] d = mat.getFloatData();
		
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
		return RMath.rotateVector(v, Fields.NATIVE_AXES);
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
		return RMath.rotateVector(v, Fields.WORLD_AXES);
	}
	
}
