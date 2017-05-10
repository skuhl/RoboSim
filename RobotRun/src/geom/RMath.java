package geom;

import java.util.Arrays;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.SingularValueDecomposition;

import processing.core.PConstants;
import processing.core.PVector;
import robot.RobotRun;
import robot.RoboticArm;

public class RMath {
	static final float DEG_TO_RAD = RobotRun.DEG_TO_RAD;
	static final float RAD_TO_DEG = RobotRun.RAD_TO_DEG;
	
	static final float PI = RobotRun.PI;
	static final float TWO_PI = RobotRun.TWO_PI;

	/**
	 * Calculate the Jacobian matrix for the robotic arm for a given set of
	 * joint rotational values using a 1 DEGREE offset for each joint rotation
	 * value. Each cell of the resulting matrix will describe the linear
	 * approximation of the robot's motion for each joint in units per radian.
	 */
	public static float[][] calculateJacobian(RoboticArm model, float[] angles, boolean posOffset) {
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

		return J;
	}
	
	public static int clamp(int in, int min, int max) {
		return Math.min(max, Math.max(min, in));
	}
	
	public static float clamp(float in, float min, float max) {
		return Math.min(max, Math.max(min, in));
	}

	// converts a double array to a float array
	public static float[][] doubleToFloat(double[][] m, int l, int w) {
		float[][] r = new float[l][w];

		for (int i = 0; i < l; i += 1) {
			for (int j = 0; j < w; j += 1) {
				r[i][j] = (float) m[i][j];
			}
		}

		return r;
	}

	// calculates rotation matrix from euler angles
	public static float[][] eulerToMatrix(PVector wpr) {
		float[][] r = new float[3][3];
		float xRot = wpr.x;
		float yRot = wpr.y;
		float zRot = wpr.z;

		r[0][0] = (float) Math.cos(yRot) * (float) Math.cos(zRot);
		r[0][1] = (float) Math.sin(xRot) * (float) Math.sin(yRot) * (float) Math.cos(zRot)
				- (float) Math.cos(xRot) * (float) Math.sin(zRot);
		r[0][2] = (float) Math.cos(xRot) * (float) Math.sin(yRot) * (float) Math.cos(zRot)
				+ (float) Math.sin(xRot) * (float) Math.sin(zRot);
		r[1][0] = (float) Math.cos(yRot) * (float) Math.sin(zRot);
		r[1][1] = (float) Math.sin(xRot) * (float) Math.sin(yRot) * (float) Math.sin(zRot)
				+ (float) Math.cos(xRot) * (float) Math.cos(zRot);
		r[1][2] = (float) Math.cos(xRot) * (float) Math.sin(yRot) * (float) Math.sin(zRot)
				- (float) Math.sin(xRot) * (float) Math.cos(zRot);
		r[2][0] = -(float) Math.sin(yRot);
		r[2][1] = (float) Math.sin(xRot) * (float) Math.cos(yRot);
		r[2][2] = (float) Math.cos(xRot) * (float) Math.cos(yRot);

		float[] magnitudes = new float[3];

		for (int v = 0; v < r.length; ++v) {
			// Find the magnitude of each axis vector
			for (int e = 0; e < r[0].length; ++e) {
				magnitudes[v] += (float) Math.pow(r[v][e], 2);
			}

			magnitudes[v] = (float) Math.sqrt(magnitudes[v]);
			// Normalize each vector
			for (int e = 0; e < r.length; ++e) {
				r[v][e] /= magnitudes[v];
			}
		}
		/**/

		return r;
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
	public static double[][] floatToDouble(float[][] m, int l, int w) {
		double[][] r = new double[l][w];

		for (int i = 0; i < l; i += 1) {
			for (int j = 0; j < w; j += 1) {
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
			float[][] J = calculateJacobian(model, angles, true);
			/**
			 * if ( (dist < (getActiveRobot().getLiveSpeed() / 100f)) && (rDist
			 * < (0.00005f * getActiveRobot().getLiveSpeed())) ) {
			 * 
			 * System.out.printf("%s\n", Arrays.toString(J)); } /
			 **/
			RealMatrix m = new Array2DRowRealMatrix(floatToDouble(J, 7, 6));
			RealMatrix JInverse = new SingularValueDecomposition(m).getSolver().getInverse();

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
				System.out.printf("%s\n", Arrays.toString(J));
				return null;
			}
		}

		return angles;
	}

	/**
	 * Find the inverse of the given column major 4x4 Homogeneous Coordinate Matrix.
	 * 
	 * This method is based off of the algorithm found on this webpage:
	 * https://web.archive.org/web/20130806093214/http://www-graphics.stanford.edu/
	 * courses/cs248-98-fall/Final/q4.html
	 */
	public static float[][] invertHCMatrix(float[][] m) {
		if (m.length != 4 || m[0].length != 4) {
			return null;
		}

		float[][] inverse = new float[4][4];

		/*
		 * [ ux vx wx tx ] -1 [ ux uy uz -dot(u, t) ] [ uy vy wy ty ] = [ vx vy
		 * vz -dot(v, t) ] [ uz vz wz tz ] [ wx wy wz -dot(w, t) ] [ 0 0 0 1 ] [
		 * 0 0 0 1 ]
		 */
		inverse[0][0] = m[0][0];
		inverse[0][1] = m[1][0];
		inverse[0][2] = m[2][0];
		inverse[0][3] = -(m[0][0] * m[0][3] + m[0][1] * m[1][3] + m[0][2] * m[2][3]);
		inverse[1][0] = m[0][1];
		inverse[1][1] = m[1][1];
		inverse[1][2] = m[2][1];
		inverse[1][3] = -(m[1][0] * m[0][3] + m[1][1] * m[1][3] + m[1][2] * m[2][3]);
		inverse[2][0] = m[0][2];
		inverse[2][1] = m[1][2];
		inverse[2][2] = m[2][2];
		inverse[2][3] = -(m[2][0] * m[0][3] + m[2][1] * m[1][3] + m[2][2] * m[2][3]);
		inverse[3][0] = 0;
		inverse[3][1] = 0;
		inverse[3][2] = 0;
		inverse[3][3] = 1;

		return inverse;
	}

	// calculates euler angles from rotation matrix
	public static PVector matrixToEuler(float[][] r) {
		float yRot1, xRot1, zRot1;
		PVector wpr;

		if (r[2][0] != 1 && r[2][0] != -1) {
			// rotation about y-axis
			yRot1 = -(float) Math.asin(r[2][0]);
			// rotation about x-axis
			xRot1 = (float) Math.atan2(r[2][1] / (float) Math.cos(yRot1), r[2][2] / (float) Math.cos(yRot1));
			// rotation about z-axis
			zRot1 = (float) Math.atan2(r[1][0] / (float) Math.cos(yRot1), r[0][0] / (float) Math.cos(yRot1));
		} else {
			zRot1 = 0;
			if (r[2][0] == -1) {
				yRot1 = PI / 2;
				xRot1 = zRot1 + (float) Math.atan2(r[0][1], r[0][2]);
			} else {
				yRot1 = -PI / 2;
				xRot1 = -zRot1 + (float) Math.atan2(-r[0][1], -r[0][2]);
			}
		}

		wpr = new PVector(xRot1, yRot1, zRot1);
		return wpr;
	}

	// calculates quaternion from rotation matrix
	public static RQuaternion matrixToQuat(float[][] r) {
		float[] limboQ = new float[4];
		float tr = r[0][0] + r[1][1] + r[2][2];

		if (tr > 0) {
			float S = (float) Math.sqrt(1.0f + tr) * 2; // S=4*q[0]
			limboQ[0] = S / 4;
			limboQ[1] = (r[2][1] - r[1][2]) / S;
			limboQ[2] = (r[0][2] - r[2][0]) / S;
			limboQ[3] = (r[1][0] - r[0][1]) / S;
		} else if (r[0][0] > r[1][1] & r[0][0] > r[2][2]) {
			float S = (float) Math.sqrt(1.0f + r[0][0] - r[1][1] - r[2][2]) * 2; // S=4*q[1]
			limboQ[0] = (r[2][1] - r[1][2]) / S;
			limboQ[1] = S / 4;
			limboQ[2] = (r[0][1] + r[1][0]) / S;
			limboQ[3] = (r[0][2] + r[2][0]) / S;
		} else if (r[1][1] > r[2][2]) {
			float S = (float) Math.sqrt(1.0f + r[1][1] - r[0][0] - r[2][2]) * 2; // S=4*q[2]
			limboQ[0] = (r[0][2] - r[2][0]) / S;
			limboQ[1] = (r[0][1] + r[1][0]) / S;
			limboQ[2] = S / 4;
			limboQ[3] = (r[1][2] + r[2][1]) / S;
		} else {
			float S = (float) Math.sqrt(1.0f + r[2][2] - r[0][0] - r[1][1]) * 2; // S=4*q[3]
			limboQ[0] = (r[1][0] - r[0][1]) / S;
			limboQ[1] = (r[0][2] + r[2][0]) / S;
			limboQ[2] = (r[1][2] + r[2][1]) / S;
			limboQ[3] = S / 4;
		}
		
		RQuaternion q = new RQuaternion(limboQ[0], limboQ[1], limboQ[2], limboQ[3]);
		q.normalize();

		return q;
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

	public static void printMat(float[][] mat) {
		for (int i = 0; i < mat.length; i += 1) {
			System.out.print("[");
			for (int j = 0; j < mat[0].length; j += 1) {
				if (j < mat[0].length - 1) {
					System.out.print(String.format("%12f, ", mat[i][j]));
				} else {
					System.out.print(String.format("%12f", mat[i][j]));
				}
			}
			System.out.println("]");
		}
		System.out.println();
	}

	// calculates euler angles from quaternion
	public static PVector quatToEuler(RQuaternion q) {
		float[][] r = q.toMatrix();
		PVector wpr = matrixToEuler(r);
		return wpr;
	}

	// calculates rotation matrix from quaternion
	public static float[][] quatToMatrix(RQuaternion q) {
		float[][] r = new float[3][3];

		r[0][0] = 1 - 2 * (q.getValue(2) * q.getValue(2) + q.getValue(3) * q.getValue(3));
		r[0][1] = 2 * (q.getValue(1) * q.getValue(2) - q.getValue(0) * q.getValue(3));
		r[0][2] = 2 * (q.getValue(0) * q.getValue(2) + q.getValue(1) * q.getValue(3));
		r[1][0] = 2 * (q.getValue(1) * q.getValue(2) + q.getValue(0) * q.getValue(3));
		r[1][1] = 1 - 2 * (q.getValue(1) * q.getValue(1) + q.getValue(3) * q.getValue(3));
		r[1][2] = 2 * (q.getValue(2) * q.getValue(3) - q.getValue(0) * q.getValue(1));
		r[2][0] = 2 * (q.getValue(1) * q.getValue(3) - q.getValue(0) * q.getValue(2));
		r[2][1] = 2 * (q.getValue(0) * q.getValue(1) + q.getValue(2) * q.getValue(3));
		r[2][2] = 1 - 2 * (q.getValue(1) * q.getValue(1) + q.getValue(2) * q.getValue(2));

		float[] magnitudes = new float[3];

		for (int v = 0; v < r.length; ++v) {
			// Find the magnitude of each axis vector
			for (int e = 0; e < r[0].length; ++e) {
				magnitudes[v] += Math.pow(r[v][e], 2);
			}

			magnitudes[v] = (float) Math.sqrt(magnitudes[v]);
			// Normalize each vector
			for (int e = 0; e < r.length; ++e) {
				r[v][e] /= magnitudes[v];
			}
		}
		/**/

		return r;
	}

	// Rotates the matrix 'm' by an angle 'theta' around the given 'axis'
	public static float[][] rotateAxisVector(float[][] m, float theta, PVector axis) {
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
		r[0][1] = x * y * t - z * s;
		r[0][2] = x * z * t + y * s;
		r[1][0] = y * x * t + z * s;
		r[1][1] = y * y * t + c;
		r[1][2] = y * z * t - x * s;
		r[2][0] = z * x * t - y * s;
		r[2][1] = z * y * t + x * s;
		r[2][2] = z * z * t + c;

		RealMatrix M = new Array2DRowRealMatrix(floatToDouble(m, 3, 3));
		RealMatrix R = new Array2DRowRealMatrix(floatToDouble(r, 3, 3));
		RealMatrix MR = M.multiply(R);

		return doubleToFloat(MR.getData(), 3, 3);
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
	 * @returning the 4x4 transformation matrix (column major order) formed from
	 *            the given origin and axes offset
	 */
	public static float[][] transformationMatrix(PVector origin, float[][] axes) {
		float[][] transform = new float[4][4];

		transform[0][0] = axes[0][0];
		transform[1][0] = axes[0][1];
		transform[2][0] = axes[0][2];
		transform[3][0] = 0;
		transform[0][1] = axes[1][0];
		transform[1][1] = axes[1][1];
		transform[2][1] = axes[1][2];
		transform[3][1] = 0;
		transform[0][2] = axes[2][0];
		transform[1][2] = axes[2][1];
		transform[2][2] = axes[2][2];
		transform[3][2] = 0;
		transform[0][3] = origin.x;
		transform[1][3] = origin.y;
		transform[2][3] = origin.z;
		transform[3][3] = 1;

		return transform;
	}
	
	public static float[][] mat4fMultiply(float[][] m1, float[][] m2) {
		float[][] mr = new float[4][4];
		
		if(m1.length != 4 || m2.length != 4 || m1[0].length != 4 || m2[0].length != 4) {
			return null;
		}
		
		for(int i = 0; i < 4; i += 1) {
			mr[0][i] = m1[0][0]*m2[0][i] + m1[0][1]*m2[1][i] + m1[0][2]*m2[2][i] + m1[0][3]*m2[3][i];
			mr[1][i] = m1[1][0]*m2[0][i] + m1[1][1]*m2[1][i] + m1[1][2]*m2[2][i] + m1[1][3]*m2[3][i];
			mr[2][i] = m1[2][0]*m2[0][i] + m1[2][1]*m2[1][i] + m1[2][2]*m2[2][i] + m1[2][3]*m2[3][i];
			mr[3][i] = m1[3][0]*m2[0][i] + m1[3][1]*m2[1][i] + m1[3][2]*m2[2][i] + m1[3][3]*m2[3][i];
		}
		
		return mr;
	}
	
	/**
	 * TODO
	 * 
	 * @param v
	 * @param rMatrix
	 * @return
	 */
	public static PVector rotateVector(PVector v, float[][] rMatrix) {
		if (rMatrix.length != 3 || rMatrix[0].length != 3) {
			return null;
		}
		
		PVector u = new PVector();
		// Apply the transformation matrix to the given vector
		u.x = v.x * rMatrix[0][0] + v.y * rMatrix[0][1] + v.z * rMatrix[0][2];
		u.y = v.x * rMatrix[1][0] + v.y * rMatrix[1][1] + v.z * rMatrix[1][2];
		u.z = v.x * rMatrix[2][0] + v.y * rMatrix[2][1] + v.z * rMatrix[2][2];

		return u;
	}
	
	/*
	 * Transforms the given vector from the coordinate system defined by the
	 * given transformation matrix (column major order).
	 */
	public static PVector vectorMatrixMult(PVector v, float[][] tMatrix) {
		if (tMatrix.length != 4 || tMatrix[0].length != 4) {
			return null;
		}

		PVector u = new PVector();
		// Apply the transformation matrix to the given vector
		u.x = v.x * tMatrix[0][0] + v.y * tMatrix[0][1] + v.z * tMatrix[0][2] + tMatrix[0][3];
		u.y = v.x * tMatrix[1][0] + v.y * tMatrix[1][1] + v.z * tMatrix[1][2] + tMatrix[1][3];
		u.z = v.x * tMatrix[2][0] + v.y * tMatrix[2][1] + v.z * tMatrix[2][2] + tMatrix[2][3];
		float w = tMatrix[3][0] + tMatrix[3][1] + tMatrix[3][2] + tMatrix[3][3];
		
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
	 * Returns a string that represents the given rotation (3x3) or
	 * transformation (4x4) matrix. The rotation portion of a
	 * transformation matrix as well as a rotation matrix are in
	 * row major order.
	 * 
	 * @param matrix	A rotation or transformation matrix
	 * @return			The string representation of the given matrix
	 */
	public static String toString(float[][] matrix) {
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
	
}
