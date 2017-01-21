package robot;

import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.SingularValueDecomposition;

import controlP5.*;
import processing.core.*;
import processing.event.MouseEvent;

import expression.*;
import frame.*;
import geom.*;
import global.*;
import programming.*;
import regs.*;
import screen.ScreenMode;
import screen.ScreenType;
import ui.*;
import window.*;

public class RobotRun extends PApplet {
	public static final PVector ROBOT_POSITION;
	public static final int EXEC_SUCCESS = 0, EXEC_FAILURE = 1, EXEC_PARTIAL = 2;
	
	private static RobotRun instance;
	/*******************************/
	/*        GUI Stuff            */
	/*******************************/
	
	public static final float[][] WORLD_AXES;
	private static final char[][] letters;
	
	public static void main(String[] args) {
		String[] appletArgs = new String[] { "robot.RobotRun" };
		
		if (args != null) {
			PApplet.main(concat(appletArgs, args));
			
		} else {
			PApplet.main(appletArgs);
		}
	}
	
	static {
		instance = null;
		ROBOT_POSITION = new PVector(200, 300, 200);
		WORLD_AXES = new float[][] { { -1,  0,  0 },
									 {  0,  0,  1 },
									 {  0, -1,  0 } };
		
		 letters = new char[][] {{'a', 'b', 'c', 'd', 'e', 'f'},
								 {'g', 'h', 'i', 'j', 'k', 'l'},
								 {'m', 'n', 'o', 'p', 'q', 'r'},
								 {'s', 't', 'u', 'v', 'w', 'x'},
								 {'y', 'z', '_', '@', '*', '.'}};
	}
	
	// Deterimes what type of axes should be displayed
	private static AxesDisplay axesState = AxesDisplay.AXES;
	
	public static final boolean DISPLAY_TEST_OUTPUT = true;
	public static PFont fnt_con14;

	public static PFont fnt_con12;
	public static PFont fnt_conB;
	/**
	 * Determines if the lies within the range of angles that span from rangeStart to rangeEnd,
	 * going clockwise around the Unit Cycle. It is assumed that all parameters are in radians
	 * and within the range [0, TWO_PI).
	 * 
	 * @param angleToVerify  the angle in question
	 * @param rangeStart     the 'lower bounds' of the angle range to check
	 * @param rangeEnd       the 'upper bounds' of the angle range to check
	 */
	public static boolean angleWithinBounds(float angleToVerify, float rangeStart, float rangeEnd) {

		if(rangeStart < rangeEnd) {
			// Joint range does not overlap TWO_PI
			return angleToVerify >= rangeStart && angleToVerify <= rangeEnd;
		} else {
			// Joint range overlaps TWO_PI
			return !(angleToVerify > rangeEnd && angleToVerify < rangeStart);
		}
	}

	/**
	 * Converts the given point, pt, into the Coordinate System defined by the given origin
	 * vector and rotation quaternion axes. The joint angles associated with the point will
	 * be transformed as well, though, if inverse kinematics fails, then the original joint
	 * angles are used instead.
	 * 
	 * @param pt      A point with initialized position and orientation
	 * @param origin  The origin of the Coordinate System
	 * @param axes    The axes of the Coordinate System representing as a rotation quanternion
	 * @returning     The point, pt, interms of the given frame's Coordinate System
	 */
	public static Point applyFrame(Point pt, PVector origin, RQuaternion axes) {
		PVector position = convertToFrame(pt.position, origin, axes);
		RQuaternion orientation = axes.transformQuaternion(pt.orientation);
		// Update joint angles associated with the point
		float[] newJointAngles = inverseKinematics(pt.angles, position, orientation);

		if (newJointAngles != null) {
			return new Point(position, orientation, newJointAngles);
		} else {
			// If inverse kinematics fails use the old angles
			return new Point(position, orientation, pt.angles);
		}
	}
	
	/**
	 * Applies the rotations and translations of the Robot Arm to get to the
	 * face plate center, given the set of six joint angles, each corresponding
	 * to a joint of the Robot Arm and each within the bounds of [0, TWO_PI).
	 * 
	 * @param jointAngles  A valid set of six joint angles (in radians) for the Robot
	 */
	public static void applyModelRotation(float[] jointAngles) {
		instance.translate(ROBOT_POSITION.x, ROBOT_POSITION.y, ROBOT_POSITION.z);

		instance.translate(-50, -166, -358); // -115, -213, -413
		instance.rotateZ(PI);
		instance.translate(150, 0, 150);
		instance.rotateX(PI);
		instance.rotateY(jointAngles[0]);
		instance.rotateX(-PI);
		instance.translate(-150, 0, -150);
		instance.rotateZ(-PI);    
		instance.translate(-115, -85, 180);
		instance.rotateZ(PI);
		instance.rotateY(PI/2);
		instance.translate(0, 62, 62);
		instance.rotateX(jointAngles[1]);
		instance.translate(0, -62, -62);
		instance.rotateY(-PI/2);
		instance.rotateZ(-PI);   
		instance.translate(0, -500, -50);
		instance.rotateZ(PI);
		instance.rotateY(PI/2);
		instance.translate(0, 75, 75);
		instance.rotateZ(PI);
		instance.rotateX(jointAngles[2]);
		instance.rotateZ(-PI);
		instance.translate(0, -75, -75);
		instance.rotateY(PI/2);
		instance.rotateZ(-PI);
		instance.translate(745, -150, 150);
		instance.rotateZ(PI/2);
		instance.rotateY(PI/2);
		instance.translate(70, 0, 70);
		instance.rotateY(jointAngles[3]);
		instance.translate(-70, 0, -70);
		instance.rotateY(-PI/2);
		instance.rotateZ(-PI/2);    
		instance.translate(-115, 130, -124);
		instance.rotateZ(PI);
		instance.rotateY(-PI/2);
		instance.translate(0, 50, 50);
		instance.rotateX(jointAngles[4]);
		instance.translate(0, -50, -50);
		instance.rotateY(PI/2);
		instance.rotateZ(-PI);    
		instance.translate(150, -10, 95);
		instance.rotateY(-PI/2);
		instance.rotateZ(PI);
		instance.translate(45, 45, 0);
		instance.rotateZ(jointAngles[5]);
	}
	
	/**
	 * Calculate the Jacobian matrix for the robotic arm for
	 * a given set of joint rotational values using a 1 DEGREE
	 * offset for each joint rotation value. Each cell of the
	 * resulting matrix will describe the linear approximation
	 * of the robot's motion for each joint in units per radian. 
	 */
	public static float[][] calculateJacobian(float[] angles, boolean posOffset) {
		float dAngle = DEG_TO_RAD;
		if (!posOffset){ dAngle *= -1; }

		float[][] J = new float[7][6];
		//get current ee position
		Point curRP = nativeRobotEEPoint(angles);

		//examine each segment of the arm
		for(int i = 0; i < 6; i += 1) {
			//test angular offset
			angles[i] += dAngle;
			//get updated ee position
			Point newRP = nativeRobotEEPoint(angles);

			if (curRP.orientation.dot(newRP.orientation) < 0f) {
				// Use -q instead of q
				newRP.orientation.scalarMult(-1);
			}

			newRP.orientation.addValues( RQuaternion.scalarMult(-1, curRP.orientation) );

			//get translational delta
			J[0][i] = (newRP.position.x - curRP.position.x) / DEG_TO_RAD;
			J[1][i] = (newRP.position.y - curRP.position.y) / DEG_TO_RAD;
			J[2][i] = (newRP.position.z - curRP.position.z) / DEG_TO_RAD;
			//get rotational delta        
			J[3][i] = newRP.orientation.getValue(0) / DEG_TO_RAD;
			J[4][i] = newRP.orientation.getValue(1) / DEG_TO_RAD;
			J[5][i] = newRP.orientation.getValue(2) / DEG_TO_RAD;
			J[6][i] = newRP.orientation.getValue(3) / DEG_TO_RAD;
			//replace the original rotational value
			angles[i] -= dAngle;
		}

		return J;
	}
	
	public static int clamp(int in, int min, int max) {
		return min(max, max(min, in));
	} 
	/**
	 * Converts the given vector, u, from the Coordinate System defined by the given origin
	 * vector and rotation quaternion axes.
	 * 
	 * @param v       A vector in the XYZ vector space
	 * @param origin  The origin of the Coordinate System
	 * @param axes    The axes of the Coordinate System representing as a rotation quanternion
	 * @returning     The vector, u, in the Native frame
	 */
	public static PVector convertFromFrame(PVector u, PVector origin, RQuaternion axes) {
		RQuaternion invAxes = axes.conjugate();
		invAxes.normalize();
		PVector vRotated = invAxes.rotateVector(u);
		return vRotated.add(origin);
	}
	/**
	 * Converts the given vector form the left-hand Native Coordinate System to the
	 * right-hand World Frame Coordinate System.
	 */
	public static PVector convertNativeToWorld(PVector v) {
		float[][] tMatrix = transformationMatrix(new PVector(0f, 0f, 0f), WORLD_AXES);
		return transformVector(v, invertHCMatrix(tMatrix));
	}

	/**
	 * Converts the given vector, v, into the Coordinate System defined by the given origin
	 * vector and rotation quaternion axes.
	 * 
	 * @param v      A vector in the XYZ vector space
	 * @param origin  The origin of the Coordinate System
	 * @param axes    The axes of the Coordinate System representing as a rotation quanternion
	 * @returning     The vector, v, interms of the given frame's Coordinate System
	 */
	public static PVector convertToFrame(PVector v, PVector origin, RQuaternion axes) {
		PVector vOffset = PVector.sub(v, origin);
		return axes.rotateVector(vOffset);
	}
	/**
	 * Converts the given vector form the right-hand World Frame Coordinate System
	 * to the left-hand Native Coordinate System.
	 */
	public static PVector convertWorldToNative(PVector v) {
		float[][] tMatrix = transformationMatrix(new PVector(0f, 0f, 0f), WORLD_AXES);
		return transformVector(v, tMatrix);
	}
	
	/* Calculate v x v */
	public static float[] crossProduct(float[] v, float[] u) {
		if(v.length != 3 && v.length != u.length) { return null; }

		float[] w = new float[v.length];
		// [a, b, c] x [d, e, f] = [ bf - ce, cd - af, ae - bd ]
		w[0] = v[1] * u[2] - v[2] * u[1];
		w[1] = v[2] * u[0] - v[0] * u[2];
		w[2] = v[0] * u[1] - v[1] * u[0];

		return w;
	}
												 


	//converts a double array to a float array
	public static float[][] doubleToFloat(double[][] m, int l, int w) {
		float[][] r = new float[l][w];

		for(int i = 0; i < l; i += 1) {
			for(int j = 0; j < w; j += 1) {
				r[i][j] = (float)m[i][j];
			}
		}

		return r;
	}
	//calculates rotation matrix from euler angles
	public static float[][] eulerToMatrix(PVector wpr) {
		float[][] r = new float[3][3];
		float xRot = wpr.x;
		float yRot = wpr.y;
		float zRot = wpr.z;

		r[0][0] = (float)Math.cos(yRot)*(float)Math.cos(zRot);
		r[0][1] = (float)Math.sin(xRot)*(float)Math.sin(yRot)*(float)Math.cos(zRot) - (float)Math.cos(xRot)*(float)Math.sin(zRot);
		r[0][2] = (float)Math.cos(xRot)*(float)Math.sin(yRot)*(float)Math.cos(zRot) + (float)Math.sin(xRot)*(float)Math.sin(zRot);
		r[1][0] = (float)Math.cos(yRot)*(float)Math.sin(zRot);
		r[1][1] = (float)Math.sin(xRot)*(float)Math.sin(yRot)*(float)Math.sin(zRot) + (float)Math.cos(xRot)*(float)Math.cos(zRot);
		r[1][2] = (float)Math.cos(xRot)*(float)Math.sin(yRot)*(float)Math.sin(zRot) - (float)Math.sin(xRot)*(float)Math.cos(zRot);
		r[2][0] = -(float)Math.sin(yRot);
		r[2][1] = (float)Math.sin(xRot)*(float)Math.cos(yRot);
		r[2][2] = (float)Math.cos(xRot)*(float)Math.cos(yRot);

		float[] magnitudes = new float[3];

		for(int v = 0; v < r.length; ++v) {
			// Find the magnitude of each axis vector
			for(int e = 0; e < r[0].length; ++e) {
				magnitudes[v] += (float)Math.pow(r[v][e], 2);
			}

			magnitudes[v] = (float)Math.sqrt(magnitudes[v]);
			// Normalize each vector
			for(int e = 0; e < r.length; ++e) {
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

		w = (float)Math.cos(xRot/2)*(float)Math.cos(yRot/2)*(float)Math.cos(zRot/2) + (float)Math.sin(xRot/2)*(float)Math.sin(yRot/2)*(float)Math.sin(zRot/2);
		x = (float)Math.sin(xRot/2)*(float)Math.cos(yRot/2)*(float)Math.cos(zRot/2) - (float)Math.cos(xRot/2)*(float)Math.sin(yRot/2)*(float)Math.sin(zRot/2);
		y = (float)Math.cos(xRot/2)*(float)Math.sin(yRot/2)*(float)Math.cos(zRot/2) + (float)Math.sin(xRot/2)*(float)Math.cos(yRot/2)*(float)Math.sin(zRot/2);
		z = (float)Math.cos(xRot/2)*(float)Math.cos(yRot/2)*(float)Math.sin(zRot/2) - (float)Math.sin(xRot/2)*(float)Math.sin(yRot/2)*(float)Math.cos(zRot/2);

		return new RQuaternion(w, x, y, z);
	}
	//converts a float array to a double array
	public static double[][] floatToDouble(float[][] m, int l, int w) {
		double[][] r = new double[l][w];

		for(int i = 0; i < l; i += 1) {
			for(int j = 0; j < w; j += 1) {
				r[i][j] = (double)m[i][j];
			}
		}

		return r;
	}
	public static AxesDisplay getAxesState() {
		return axesState;
	}
	/**
	 * Returns the instance of this PApplet
	 */
	public static RobotRun getInstance() {
		return instance;
	}

	/**
	 * Returns this PApplet instance's Arm model reference.
	 */
	public static ArmModel getRobot() {
		return instance.getArmModel();
	}
	
	/**
	 * Attempts to calculate the joint angles that would place the Robot in the given target position and
	 * orientation. The srcAngles parameter defines the position of the Robot from which to move, since
	 * this inverse kinematics uses a relative conversion formula. There is no guarantee that the target
	 * position and orientation can be reached; in the case that inverse kinematics fails, then null is
	 * returned. Otherwise, a set of six angles will be returned, though there is no guarantee that these
	 * angles are valid!
	 * 
	 * @param srcAngles       The initial position of the Robot
	 * @param tgtPosition     The desired position of the Robot
	 * @param tgtOrientation  The desited orientation of the Robot
	 */
	public static float[] inverseKinematics(float[] srcAngles, PVector tgtPosition, RQuaternion tgtOrientation) {
		final int limit = 1000;  // Max number of times to loop
		int count = 0;

		float[] angles = srcAngles.clone();

		while(count < limit) {
			Point cPoint = nativeRobotEEPoint(angles);

			if (tgtOrientation.dot(cPoint.orientation) < 0f) {
				// Use -q instead of q
				tgtOrientation.scalarMult(-1);
			}

			//calculate our translational offset from target
			PVector tDelta = PVector.sub(tgtPosition, cPoint.position);
			//calculate our rotational offset from target
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
			//check whether our current position is within tolerance
			if ( (dist < (instance.liveSpeed / 100f)) && (rDist < (0.00005f * instance.liveSpeed)) ) { break; }

			//calculate jacobian, 'J', and its inverse
			float[][] J = calculateJacobian(angles, true);
			RealMatrix m = new Array2DRowRealMatrix(floatToDouble(J, 7, 6));
			RealMatrix JInverse = new SingularValueDecomposition(m).getSolver().getInverse();

			//calculate and apply joint angular changes
			float[] dAngle = {0, 0, 0, 0, 0, 0};
			for(int i = 0; i < 6; i += 1) {
				for(int j = 0; j < 7; j += 1) {
					dAngle[i] += JInverse.getEntry(i, j)*delta[j];
				}

				//update joint angles
				angles[i] += dAngle[i];
				angles[i] += TWO_PI;
				angles[i] %= TWO_PI;
			}

			count += 1;
			if (count == limit) {
				// IK failure
				//if (DISPLAY_TEST_OUTPUT) {
				//  System.out.printf("\nDelta: %s\nAngles: %s\n%s\n%s -> %s\n", arrayToString(delta), arrayToString(angles),
				//                      matrixToString(J), cPoint.orientation, tgtOrientation);
				//}

				return null;
			}
		}

		return angles;
	}
	/**
	 * Find the inverse of the given 4x4 Homogeneous Coordinate Matrix. 
	 * 
	 * This method is based off of the algorithm found on this webpage:
	 *    https://web.archive.org/web/20130806093214/http://www-graphics.stanford.edu/
	 *      courses/cs248-98-fall/Final/q4.html
	 */
	public static float[][] invertHCMatrix(float[][] m) {
		if(m.length != 4 || m[0].length != 4) {
			return null;
		}

		float[][] inverse = new float[4][4];

		/* [ ux vx wx tx ] -1       [ ux uy uz -dot(u, t) ]
		 * [ uy vy wy ty ]     =    [ vx vy vz -dot(v, t) ]
		 * [ uz vz wz tz ]          [ wx wy wz -dot(w, t) ]
		 * [  0  0  0  1 ]          [  0  0  0      1     ]
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

	//calculates euler angles from rotation matrix
	public static PVector matrixToEuler(float[][] r) {
		float yRot1, xRot1, zRot1;
		PVector wpr;

		if(r[2][0] != 1 && r[2][0] != -1) {
			//rotation about y-axis
			yRot1 = -(float)Math.asin(r[2][0]);
			//rotation about x-axis
			xRot1 = (float)Math.atan2(r[2][1]/(float)Math.cos(yRot1), r[2][2]/(float)Math.cos(yRot1));
			//rotation about z-axis
			zRot1 = (float)Math.atan2(r[1][0]/(float)Math.cos(yRot1), r[0][0]/(float)Math.cos(yRot1));
		} else {
			zRot1 = 0;
			if(r[2][0] == -1) {
				yRot1 = PI/2;
				xRot1 = zRot1 + (float)Math.atan2(r[0][1], r[0][2]);
			} else {
				yRot1 = -PI/2;
				xRot1 = -zRot1 + (float)Math.atan2(-r[0][1], -r[0][2]);
			}
		}

		wpr = new PVector(xRot1, yRot1, zRot1);
		return wpr;
	}
	//calculates quaternion from rotation matrix
	public static RQuaternion matrixToQuat(float[][] r) {
		float[] limboQ = new float[4];
		float tr = r[0][0] + r[1][1] + r[2][2];

		if(tr > 0) {
			float S = (float)Math.sqrt(1.0f + tr) * 2; // S=4*q[0] 
			limboQ[0] = S / 4;
			limboQ[1] = (r[2][1] - r[1][2]) / S;
			limboQ[2] = (r[0][2] - r[2][0]) / S; 
			limboQ[3] = (r[1][0] - r[0][1]) / S;
		} else if((r[0][0] > r[1][1]) & (r[0][0] > r[2][2])) {
			float S = (float)Math.sqrt(1.0f + r[0][0] - r[1][1] - r[2][2]) * 2; // S=4*q[1] 
			limboQ[0] = (r[2][1] - r[1][2]) / S;
			limboQ[1] = S / 4;
			limboQ[2] = (r[0][1] + r[1][0]) / S; 
			limboQ[3] = (r[0][2] + r[2][0]) / S;
		} else if(r[1][1] > r[2][2]) {
			float S = (float)Math.sqrt(1.0f + r[1][1] - r[0][0] - r[2][2]) * 2; // S=4*q[2]
			limboQ[0] = (r[0][2] - r[2][0]) / S;
			limboQ[1] = (r[0][1] + r[1][0]) / S; 
			limboQ[2] = S / 4;
			limboQ[3] = (r[1][2] + r[2][1]) / S;
		} else {
			float S = (float)Math.sqrt(1.0f + r[2][2] - r[0][0] - r[1][1]) * 2; // S=4*q[3]
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
	 * Computes the minimum rotational magnitude to move
	 * from src to dest, around the unit circle.
	 * 
	 * @param src   The source angle in radians
	 * @param dset  The destination angle in radians
	 * @returning   The minimum distance between src and dest
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
	 * @param angle  Some rotation in radians
	 * @returning    The equivalent angle within the range [0, TWO_PI)
	 */
	public static float mod2PI(float angle) {
		float temp = angle % TWO_PI;

		if (temp < 0f) {
			temp += TWO_PI;
		}

		return temp;
	}
	/**
	 * Returns the Robot's End Effector position according to the active Tool Frame's
	 * offset in the native Coordinate System.
	 * 
	 * @param jointAngles  A valid set of six joint angles (in radians) for the Robot
	 * @returning          The Robot's End Effector position
	 */
	public static Point nativeRobotEEPoint(float[] jointAngles) {
		Frame activeTool = instance.armModel.getActiveFrame(CoordFrame.TOOL);
		PVector offset;

		if (activeTool != null) {
			// Apply the Tool Tip
			offset = ((ToolFrame)activeTool).getTCPOffset();
		} else {
			offset = new PVector(0f, 0f, 0f);
		}

		return nativeRobotPointOffset(jointAngles, offset);
	}

	/**
	 * Returns a point containing the Robot's faceplate position and orientation
	 * corresponding to the given joint angles, as well as the given joint angles.
	 * 
	 * @param jointAngles  A valid set of six joint angles (in radians) for the
	 *                     Robot
	 * @returning          The Robot's faceplate position and orientation
	 *                     corresponding to the given joint angles
	 */
	public static Point nativeRobotPoint(float[] jointAngles) {
		// Return a point containing the faceplate position, orientation, and joint angles
		return nativeRobotPointOffset(jointAngles, new PVector(0f, 0f, 0f));
	}

	/**
	 * Returns a point containing the Robot's End Effector position and orientation
	 * corresponding to the given joint angles, as well as the given joint angles.
	 * 
	 * @param jointAngles  A valid set of six joint angles (in radians) for the Robot
	 * @param offset       The End Effector offset in the form of a vector
	 * @returning          The Robot's EE position and orientation corresponding to
	 *                     the given joint angles
	 */
	public static Point nativeRobotPointOffset(float[] jointAngles, PVector offset) {
		instance.pushMatrix();
		instance.resetMatrix();
		applyModelRotation(jointAngles);
		// Apply offset
		PVector ee = instance.getCoordFromMatrix(offset.x, offset.y, offset.z);
		float[][] orientationMatrix = instance.getRotationMatrix();
		instance.popMatrix();
		// Return a Point containing the EE position, orientation, and joint angles
		return new Point(ee, matrixToQuat(orientationMatrix), jointAngles);
	}
	
	/* Returns a vector with the opposite sign
	 * as the given vector. */
	public static float[] negate(float[] v) {
		float[] u = new float[v.length];

		for(int e = 0; e < v.length; ++e) {
			u[e] = -v[e];
		}

		return u;
	}
	
	//calculates euler angles from quaternion
	public static PVector quatToEuler(RQuaternion q) {
		float[][] r = q.toMatrix();
		PVector wpr = matrixToEuler(r);
		return wpr;
	}
	//calculates rotation matrix from quaternion
	public static float[][] quatToMatrix(RQuaternion q) {
		float[][] r = new float[3][3];

		r[0][0] = 1 - 2*(q.getValue(2)*q.getValue(2) + q.getValue(3)*q.getValue(3));
		r[0][1] = 2*(q.getValue(1)*q.getValue(2) - q.getValue(0)*q.getValue(3));
		r[0][2] = 2*(q.getValue(0)*q.getValue(2) + q.getValue(1)*q.getValue(3));
		r[1][0] = 2*(q.getValue(1)*q.getValue(2) + q.getValue(0)*q.getValue(3));
		r[1][1] = 1 - 2*(q.getValue(1)*q.getValue(1) + q.getValue(3)*q.getValue(3));
		r[1][2] = 2*(q.getValue(2)*q.getValue(3) - q.getValue(0)*q.getValue(1));
		r[2][0] = 2*(q.getValue(1)*q.getValue(3) - q.getValue(0)*q.getValue(2));
		r[2][1] = 2*(q.getValue(0)*q.getValue(1) + q.getValue(2)*q.getValue(3));
		r[2][2] = 1 - 2*(q.getValue(1)*q.getValue(1) + q.getValue(2)*q.getValue(2));

		float[] magnitudes = new float[3];

		for(int v = 0; v < r.length; ++v) {
			// Find the magnitude of each axis vector
			for(int e = 0; e < r[0].length; ++e) {
				magnitudes[v] += pow(r[v][e], 2);
			}

			magnitudes[v] = sqrt(magnitudes[v]);
			// Normalize each vector
			for(int e = 0; e < r.length; ++e) {
				r[v][e] /= magnitudes[v];
			}
		}
		/**/

		return r;
	}

	/**
	 * Converts the given point, pt, from the Coordinate System defined by the given origin
	 * vector and rotation quaternion axes. The joint angles associated with the point will
	 * be transformed as well, though, if inverse kinematics fails, then the original joint
	 * angles are used instead.
	 * 
	 * @param pt      A point with initialized position and orientation
	 * @param origin  The origin of the Coordinate System
	 * @param axes    The axes of the Coordinate System representing as a rotation quanternion
	 * @returning     The point, pt, interms of the given frame's Coordinate System
	 */
	public static Point removeFrame(Point pt, PVector origin, RQuaternion axes) {
		PVector position = convertFromFrame(pt.position, origin, axes);
		RQuaternion orientation = RQuaternion.mult(pt.orientation, axes);

		// Update joint angles associated with the point
		float[] newJointAngles = inverseKinematics(pt.angles, position, orientation);

		if (newJointAngles != null) {
			return new Point(position, orientation, newJointAngles);
		} else {
			// If inverse kinematics fails use the old angles
			return new Point(position, orientation, pt.angles);
		}
	}
	
	//produces a rotation matrix given a rotation 'theta' around
	//a given axis
	public static float[][] rotateAxisVector(float[][] m, float theta, PVector axis) {
		float s = sin(theta);
		float c = cos(theta);
		float t = 1-c;

		if(c > 0.9f)
			t = 2*sin(theta/2)*sin(theta/2);

		float x = axis.x;
		float y = axis.y;
		float z = axis.z;

		float[][] r = new float[3][3];

		r[0][0] = x*x*t+c;
		r[0][1] = x*y*t-z*s;
		r[0][2] = x*z*t+y*s;
		r[1][0] = y*x*t+z*s;
		r[1][1] = y*y*t+c;
		r[1][2] = y*z*t-x*s;
		r[2][0] = z*x*t-y*s;
		r[2][1] = z*y*t+x*s;
		r[2][2] = z*z*t+c;

		RealMatrix M = new Array2DRowRealMatrix(floatToDouble(m, 3, 3));
		RealMatrix R = new Array2DRowRealMatrix(floatToDouble(r, 3, 3));
		RealMatrix MR = M.multiply(R);

		return doubleToFloat(MR.getData(), 3, 3);
	}
	
	/* Transforms the given vector by the given 3x3 rotation matrix (row major order). */
	public static PVector rotateVector(PVector v, float[][] rotMatrix) {
		if(v == null || rotMatrix == null || rotMatrix.length != 3 || rotMatrix[0].length != 3) {
			return null;
		}

		PVector u = new PVector();
		// Apply the rotation matrix to the given vector
		u.x = v.x * rotMatrix[0][0] + v.y * rotMatrix[1][0] + v.z * rotMatrix[2][0];
		u.y = v.x * rotMatrix[0][1] + v.y * rotMatrix[1][1] + v.z * rotMatrix[2][1];
		u.z = v.x * rotMatrix[0][2] + v.y * rotMatrix[1][2] + v.z * rotMatrix[2][2];

		return u;
	}

	public static void setAxesState(AxesDisplay axesState) {
		RobotRun.axesState = axesState;
	}
	
	/**
	 * Forms the 4x4 transformation matrix (row major order) form the given
	 * origin offset and axes offset (row major order) of the Native Coordinate
	 * system.
	 * 
	 * @param origin  the X, Y, Z, offset of the origin for the Coordinate frame
	 * @param axes    a 3x3 rotatin matrix (row major order) representing the unit
	 *                vector axes offset of the new Coordinate Frame from the Native
	 *                Coordinate Frame
	 * @returning     the 4x4 transformation matrix (column major order) formed from
	 *                the given origin and axes offset
	 */
	public static float[][] transformationMatrix(PVector origin, float[][] axes) {
		float[][] transform = new float[4][4];

		transform[0][0] = axes[0][0];
		transform[1][0] = axes[1][0];
		transform[2][0] = axes[2][0];
		transform[3][0] = 0;
		transform[0][1] = axes[0][1];
		transform[1][1] = axes[1][1];
		transform[2][1] = axes[2][1];
		transform[3][1] = 0;
		transform[0][2] = axes[0][2];
		transform[1][2] = axes[1][2];
		transform[2][2] = axes[2][2];
		transform[3][2] = 0;
		transform[0][3] = origin.x;
		transform[1][3] = origin.y;
		transform[2][3] = origin.z;
		transform[3][3] = 1;

		return transform;
	}
	/* Transforms the given vector from the coordinate system defined by the given
	 * transformation matrix (row major order). */
	public static PVector transformVector(PVector v, float[][] tMatrix) {
		if(tMatrix.length != 4 || tMatrix[0].length != 4) {
			return null;
		}

		PVector u = new PVector();
		// Apply the transformation matrix to the given vector
		u.x = v.x * tMatrix[0][0] + v.y * tMatrix[1][0] + v.z * tMatrix[2][0] + tMatrix[0][3];
		u.y = v.x * tMatrix[0][1] + v.y * tMatrix[1][1] + v.z * tMatrix[2][1] + tMatrix[1][3];
		u.z = v.x * tMatrix[0][2] + v.y * tMatrix[1][2] + v.z * tMatrix[2][2] + tMatrix[2][3];

		return u;
	}
	
	//returns the result of a vector 'v' multiplied by scalar 's'
	public static float[] vectorScalarMult(float[] v, float s) {
		float[] ret = new float[v.length];
		for(int i = 0; i < ret.length; i += 1) { 
			ret[i] = v[i]*s; 
		}

		return ret;
	}
	public MenuScroll contents;
	public MenuScroll options;
	public final ArrayList<Scenario> SCENARIOS = new ArrayList<Scenario>();
	public Scenario activeScenario;
	public boolean showOOBs;
	private Camera camera;
	// The position at which the Robot is drawn
	private ArmModel armModel;
	private ControlP5 cp5;
	private WindowManager manager;
	private Stack<ScreenMode> display_stack;
	private ArrayList<Macro> macros = new ArrayList<Macro>();
	private Macro[] SU_macro_bindings = new Macro[7];
	private Macro edit_macro;

	/*******************************/
	/*      Global Variables       */
	/*******************************/

	// for Execution
	public boolean execSingleInst = false;
	public boolean motionFault = false; //indicates motion error with the Robot
	
	/*******************************/
	/*      Debugging Stuff        */
	/*******************************/

	private ArrayList<String> buffer;
	
	private Point displayPoint;
	
	private int active_prog = -1; // the currently selected program
	
	private int active_instr = -1; // the currently selected instruction

	int temp_select = 0;

	/*****************************************************************************************************************
 NOTE: All the below methods assume that current matrix has the camrea applied!
	 *****************************************************************************************************************/

	boolean shift = false; // Is shift button pressed or not?

	private boolean step = false; // Is step button pressed or not?

	private int record = Fields.OFF;

	ScreenMode mode;

	int g1_px, g1_py; // the left-top corner of group1

	int g1_width, g1_height; // group 1's width and height
	
	int display_px, display_py; // the left-top corner of display screen
	int display_width, display_height; // height and width of display screen
	public Group g1, g2;
	Button bt_record_normal, 
	bt_ee_normal;

	String workingText; // when entering text or a number
	String workingTextSuffix;

	boolean speedInPercentage;
	final int ITEMS_TO_SHOW = 8, // how many programs/ instructions to display on screen
			NUM_ENTRY_LEN = 16, // Maximum character length for a number input
			TEXT_ENTRY_LEN = 16; // Maximum character length for text entry

	// Index of the current frame (Tool or User) selecting when in the Frame menus
	int curFrameIdx = -1;

	// The Frame being taught, during a frame teaching process
	Frame teachFrame = null;

	// Expression operand currently being edited
	ExprOperand opEdit = null;

	int editIdx = -1;

	//variables for keeping track of the last change made to the current program
	Instruction lastInstruct;

	boolean newInstruct;

	int lastLine;

	// store numbers pressed by the user
	ArrayList<Integer> nums = new ArrayList<Integer>();

	// container for instructions being coppied/ cut and pasted
	ArrayList<Instruction> clipBoard = new ArrayList<Instruction>();

	// string for displaying error message to user
	String err = null;

	int prev_select = -1; //saves row_select value if next screen also utilizes this variable
	int active_index = 0; //index of the cursor with respect to the first element on screen

	// how many textlabels have been created for display
	int index_contents = 0, index_options = 100, index_nums = 1000;


	/**
	 * Used for comment name input. The user can cycle through the
	 * six states for each function button in this mode:
	 *
	 * F1 -> A-F/a-f
	 * F2 -> G-L/g-l
	 * F3 -> M-R/m-r
	 * F4 -> S-X/s-x
	 * F5 -> Y-Z/y-z, _, @, *, .
	 */
	private int[] letterStates;

	ArrayList<Point> intermediatePositions;

	int motionFrameCounter = 0;

	float distanceBetweenPoints = 5.0f;

	int interMotionIdx = -1;

	int liveSpeed = 10;

	private boolean executingInstruction = false;

	// Determines what End Effector mapping should be display
	private EEMapping mappingState = EEMapping.LINE;

	public final int MTYPE_JOINT = 0;
	
	public final int MTYPE_LINEAR = 1;

	/* mouse events */

	public final int MTYPE_CIRCULAR = 2;

	public final int FTYPE_TOOL = 0;

	/*Keyboard events*/
	public final int FTYPE_USER = 1;

	/* Button events */

	//stack containing the previously running program state when a new program is called
	private Stack<int[]> call_stack = new Stack<int[]>();

	// Indicates whether a program is currently running
	private boolean programRunning = false;

	/**
	 * Updates the motion of one of the Robot's joints based on
	 * the joint index given and the value of dir (-/+ 1). The
	 * Robot's joint indices range from 0 to 5. ifthe joint
	 * Associate with the given index is already in motion,
	 * in either direction, then calling this method for that
	 * joint index will stop that joint's motion.
	 * 
	 * @returning  The new motion direction of the Robot
	 */
	public float activateLiveJointMotion(int joint, int dir) {

		if (!shift || motionFault) {
			// Only move when shift is set and there is no error
			return 0f;
		}

		return armModel.setJointMotion(joint, dir);
	}

	/**
	 * Updates the motion of the Robot with respect to one of the World axes for
	 * either linear or rotational motion around the axis. Similiar to the
	 * activateLiveJointMotion() method, calling this method for an axis, in which
	 * the Robot is already moving, will result in the termination of the Robot's
	 * motion in that axis. Rotational and linear motion for an axis are mutually
	 * independent in this regard.
	 * 
	 * @param axis        The axis of movement for the robotic arm:
                  x - 0, y - 1, z - 2, w - 3, p - 4, r - 5
	 * @pararm dir        +1 or -1: indicating the direction of motion
	 * @returning         The new direction of motion in the given axis
	 *
	 */
	public float activateLiveWorldMotion(int axis, int dir) {
		if (!shift || motionFault) {
			// Only move when shift is set and there is no error
			return 0f;
		}

		// Initiaize the Robot's destination
		Point RP = nativeRobotEEPoint(armModel.getJointAngles());
		armModel.tgtPosition = RP.position;
		armModel.tgtOrientation = RP.orientation;


		if(axis >= 0 && axis < 3) {
			if(armModel.jogLinear[axis] == 0) {
				// Begin movement on the given axis in the given direction
				armModel.jogLinear[axis] = dir;
			} else {
				// Halt movement
				armModel.jogLinear[axis] = 0;
			}

			return armModel.jogLinear[axis];
		}
		else if(axis >= 3 && axis < 6) {
			axis %= 3;
			if(armModel.jogRot[axis] == 0) {
				// Begin movement on the given axis in the given direction
				armModel.jogRot[axis] = dir;
			}
			else {
				// Halt movement
				armModel.jogRot[axis] = 0;
			}

			return armModel.jogRot[axis];
		}

		return 0f;
	}

	/**
	 * Returns the instruction that is currently active in the currently active program.
	 * 
	 * @returning  The active instruction of the active program or null if no instruction
	 *             is active
	 */
	public Instruction activeInstruction() {
		Program activeProg = activeProgram();
		if (activeProg == null || contents.getItemIdx() < 0 || contents.getItemIdx() >= activeProg.getInstructions().size()) {
			//System.out.printf("Not a valid instruction index: %d!\n", active_instr);   
			return null;

		}

		return activeProg.getInstruction(contents.getItemIdx());
	}

	/**
	 * Returns the active instruction of the active program, if
	 * that instruction is a motion instruction.
	 */
	public MotionInstruction activeMotionInst() {
		Instruction inst = activeInstruction();

		if(inst instanceof MotionInstruction) {
			return (MotionInstruction)inst;
		}

		return null;
	}

	/**
	 * Returns the currently active program or null if no program is active
	 */
	public Program activeProgram() {
		if (getActive_prog() < 0 || getActive_prog() >= armModel.numOfPrograms()) {
			//System.out.printf("Not a valid program index: %d!\n", active_prog);
			return null;
		}

		return armModel.getProgram(getActive_prog());
	}

	public void addNumber(String number) {
		if(mode.getType() == ScreenType.TYPE_NUM_ENTRY) {
			if (workingText.length() < NUM_ENTRY_LEN) {
				workingText += number;
			}
		}
		else if(mode == ScreenMode.SET_MV_INSTR_SPD) {
			workingText += number;
			options.set(1, newLine(workingText + workingTextSuffix));
		}
		else if(mode.getType() == ScreenType.TYPE_POINT_ENTRY) {
			if(contents.getLineIdx() >= 0 && contents.getLineIdx() < contents.size()) {
				String value = contents.get(contents.getLineIdx()).get(1) + number;

				if(value.length() > 9) {
					// Max length of a an input value
					value = value.substring(0,  9);
				}

				// Concatenate the new digit
				contents.get(contents.getLineIdx()).set(1, value);
			}
		}
		else if(mode.getType() == ScreenType.TYPE_TEXT_ENTRY) {
			// Replace current entry with a number
			StringBuilder temp = new StringBuilder(workingText);
			temp.setCharAt(contents.getColumnIdx(), number.charAt(0));
			workingText = temp.toString();
		}

		updateScreen();
	}

	public String arrayToString(float[] array) {
		String s = "[";

		for(int i = 0; i < array.length; i += 1) {
			s += String.format("%5.4f", array[i]);
			if(i != array.length-1) s += ", ";
		}

		return s + "]";
	}
	
	public void arrow_dn() {
		switch(mode) {
		case NAV_PROGRAMS:
			setActive_prog(contents.moveDown(shift));

			if(DISPLAY_TEST_OUTPUT) {
				System.out.printf("\nRow: %d\nProg: %d\nTRS: %d\n\n",  
						contents.getLineIdx(), getActive_prog(), contents.getRenderStart());
			}
			break;
		case NAV_PROG_INSTR:
		case SELECT_COMMENT:
		case SELECT_CUT_COPY:
		case SELECT_INSTR_DELETE:
			if (!isProgramRunning()) {
				// Lock movement when a program is running
				Instruction i = activeInstruction();
				int prevIdx = getSelectedIdx();
				setActive_instr(contents.moveDown(shift)); 
				int curLine = getSelectedLine();

				//special case for select statement column navigation
				if((i instanceof SelectStatement || i instanceof MotionInstruction) && curLine == 1) {
					if(prevIdx >= 3) {
						contents.setColumnIdx(prevIdx - 3);
					} else {
						contents.setColumnIdx(0);
					}
				}

				if(DISPLAY_TEST_OUTPUT) {
					System.out.printf("\nRow: %d\nColumn: %d\nInst: %d\nTRS: %d\n\n",
							contents.getLineIdx(), contents.getColumnIdx(), getActive_instr(), contents.getRenderStart());
				}
			}
			break;
		case NAV_DREGS:
		case NAV_PREGS_J:
		case NAV_PREGS_C:
		case NAV_TOOL_FRAMES:
		case NAV_USER_FRAMES:
		case NAV_MACROS:
		case NAV_MF_MACROS:
			active_index = contents.moveDown(shift);

			if(DISPLAY_TEST_OUTPUT) {
				System.out.printf("\nRow: %d\nColumn: %d\nIdx: %d\nTRS: %d\n\n",
						contents.getLineIdx(), contents.getColumnIdx(), active_index, contents.getRenderStart());
			}
			break;
		case SET_CALL_PROG:
		case DIRECT_ENTRY_TOOL:
		case DIRECT_ENTRY_USER:
		case EDIT_PREG_C:
		case EDIT_PREG_J:
			contents.moveDown(shift);
			break;
		case NAV_MAIN_MENU:
		case NAV_INSTR_MENU:
		case SELECT_FRAME_MODE:
		case FRAME_METHOD_USER:
		case FRAME_METHOD_TOOL:
		case SELECT_INSTR_INSERT:
		case SELECT_IO_INSTR_REG:
		case SELECT_FRAME_INSTR_TYPE:
		case SELECT_REG_STMT:
		case SELECT_COND_STMT:
		case SELECT_JMP_LBL:
		case SELECT_PASTE_OPT:
		case TFRAME_DETAIL:
		case UFRAME_DETAIL:
		case TEACH_3PT_USER:
		case TEACH_3PT_TOOL:
		case TEACH_4PT:
		case TEACH_6PT:
		case NAV_DATA:
		case SWAP_PT_TYPE:
		case SET_MV_INSTR_TYPE:
		case SET_MV_INSTR_REG_TYPE:
		case SET_MACRO_TYPE:
		case SET_MACRO_BINDING:
		case SET_FRM_INSTR_TYPE:
		case SET_REG_EXPR_TYPE:
		case SET_IF_STMT_ACT:
		case SET_SELECT_STMT_ACT:
		case SET_SELECT_STMT_ARG:
		case SET_EXPR_ARG:
		case SET_BOOL_EXPR_ARG:
		case SET_EXPR_OP:
		case SET_IO_INSTR_STATE:
			options.moveDown(false);
			//opt_select = min(opt_select + 1, options.size() - 1);
			break;  
		case ACTIVE_FRAMES:
			updateActiveFramesDisplay();
			workingText = Integer.toString(armModel.getActiveUserFrame() + 1);
			contents.setLineIdx(min(contents.getLineIdx() + 1, contents.size() - 1));
			break;
		default:
			if (mode.getType() == ScreenType.TYPE_TEXT_ENTRY) {
				options.moveDown(false);
				// Reset function key states
				for(int idx = 0; idx < letterStates.length; ++idx) { letterStates[idx] = 0; }
			}
		}  

		updateScreen();
	}

	public void arrow_lt() {
		switch(mode) { 
		case NAV_PROG_INSTR:
			if (!isProgramRunning()) {
				// Lock movement when a program is running
				contents.moveLeft();
			}
			break;
		case NAV_DREGS:
		case NAV_MACROS:
		case NAV_PREGS_J:
		case NAV_PREGS_C:
			contents.setColumnIdx(max(0, contents.getColumnIdx() - 1));
			break;
		default:
			if (mode.getType() == ScreenType.TYPE_TEXT_ENTRY) {
				contents.setColumnIdx(max(0, contents.getColumnIdx() - 1));
				// Reset function key states 
				for(int idx = 0; idx < letterStates.length; ++idx) { letterStates[idx] = 0; }
			} else if(mode.getType() == ScreenType.TYPE_EXPR_EDIT) {  
				contents.setColumnIdx(contents.getColumnIdx() - ((contents.getColumnIdx() - 4 >= options.size()) ? 4 : 0));
			}
		}

		updateScreen();
	}

	public void arrow_rt() {
		switch(mode) {
		case NAV_PROG_INSTR:
			if (!isProgramRunning()) {
				// Lock movement when a program is running
				contents.moveRight();
			}
			break;
		case DIRECT_ENTRY_USER:
		case DIRECT_ENTRY_TOOL:
		case EDIT_PREG_C:
		case EDIT_PREG_J:
			// Delete a digit from the beginning of the number entry
			if(shift) {
				String entry = contents.get(contents.getLineIdx()).get(1);

				if (entry.length() > 1 && !(entry.length() == 2 && entry.charAt(0) == '-')) {

					if(entry.charAt(0) == '-') {
						// Keep negative sign until the last digit is removed
						contents.get(contents.getLineIdx()).set(1, "-" + entry.substring(2, entry.length()));
					} else {
						contents.get(contents.getLineIdx()).set(1, entry.substring(1, entry.length()));
					}
				} else {
					contents.get(contents.getLineIdx()).set(1, "");
				}
			}

			break;
		case NAV_DREGS:
		case NAV_MACROS:
		case NAV_PREGS_J:
		case NAV_PREGS_C:
			contents.setColumnIdx(min(contents.getColumnIdx() + 1, contents.get(contents.getLineIdx()).size() - 1));
			break;
		default:
			if (mode.getType() == ScreenType.TYPE_TEXT_ENTRY) {

				if(shift) {
					// Delete key function
					if(workingText.length() > 1) {
						workingText = workingText.substring(1, workingText.length());
						contents.setColumnIdx(min(contents.getColumnIdx(), workingText.length() - 1));
					}  else {
						workingText = "\0";
					}

					contents.setColumnIdx(max(0, min(contents.getColumnIdx(), contents.get(contents.getLineIdx()).size() - 1)));
				} else if (mode.getType() == ScreenType.TYPE_EXPR_EDIT) {
					contents.setColumnIdx(contents.getColumnIdx() + ((contents.getColumnIdx() + 4 < options.size()) ? 4 : 0));
				} else {
					// Add an insert element if the length of the current comment is less than 16
					int len = workingText.length();
					if(len <= TEXT_ENTRY_LEN && contents.getColumnIdx() == workingText.length() - 1 && workingText.charAt(len - 1) != '\0') {
						workingText += '\0';
						// Update contents to the new string
						updateScreen();
					}

					contents.setColumnIdx(min(contents.getColumnIdx() + 1, contents.get(contents.getLineIdx()).size() - 1));
				}

				// Reset function key states
				for(int idx = 0; idx < letterStates.length; ++idx) { letterStates[idx] = 0; }
			}
		}

		updateScreen();
	}

	public void arrow_up() {
		switch(mode) {
		case NAV_PROGRAMS:
			setActive_prog(contents.moveUp(shift));

			if(DISPLAY_TEST_OUTPUT) {
				System.out.printf("\nOpt: %d\nProg: %d\nTRS: %d\n\n",
						options.getLineIdx(), getActive_prog(), contents.getRenderStart());
			}
			break;
		case NAV_PROG_INSTR:
		case SELECT_COMMENT:
		case SELECT_CUT_COPY:
		case SELECT_INSTR_DELETE:
			if (!isProgramRunning()) {
				// Lock movement when a program is running
				Instruction i = activeInstruction();
				int prevLine = getSelectedLine();
				setActive_instr(contents.moveUp(shift));
				int curLine = getSelectedLine();

				//special case for select statement column navigation
				if((i instanceof SelectStatement || i instanceof MotionInstruction) && curLine == 0) {
					if(prevLine == 1) {
						contents.setColumnIdx(contents.getColumnIdx() + 3);
					}
				}


				if(DISPLAY_TEST_OUTPUT) {
					System.out.printf("\nRow: %d\nColumn: %d\nInst: %d\nTRS: %d\n\n",
							contents.getLineIdx(), contents.getColumnIdx(), getActive_instr(), contents.getRenderStart());
				}
			}
			break;
		case NAV_DREGS:
		case NAV_PREGS_J:
		case NAV_PREGS_C:
		case NAV_TOOL_FRAMES:
		case NAV_USER_FRAMES:
		case NAV_MACROS:
		case NAV_MF_MACROS:
			active_index = contents.moveUp(shift);

			if(DISPLAY_TEST_OUTPUT) {
				System.out.printf("\nRow: %d\nColumn: %d\nIdx: %d\nTRS: %d\n\n",
						contents.getLineIdx(), contents.getColumnIdx(), active_index, contents.getRenderStart());
			}
			break;
		case SET_CALL_PROG:
		case SET_MACRO_PROG:
		case DIRECT_ENTRY_TOOL:
		case DIRECT_ENTRY_USER:
		case EDIT_PREG_C:
		case EDIT_PREG_J:
			contents.moveUp(shift);
			break;
		case NAV_MAIN_MENU:
		case NAV_INSTR_MENU:
		case SELECT_FRAME_MODE:
		case FRAME_METHOD_USER:
		case FRAME_METHOD_TOOL:
		case SELECT_INSTR_INSERT:
		case SELECT_IO_INSTR_REG:
		case SELECT_FRAME_INSTR_TYPE:
		case SELECT_REG_STMT:
		case SELECT_COND_STMT:
		case SELECT_JMP_LBL:
		case SELECT_PASTE_OPT:
		case TFRAME_DETAIL:
		case UFRAME_DETAIL:
		case TEACH_3PT_USER:
		case TEACH_3PT_TOOL:
		case TEACH_4PT:
		case TEACH_6PT:
		case NAV_DATA:
		case SWAP_PT_TYPE:
		case SET_MV_INSTR_TYPE:
		case SET_MV_INSTR_REG_TYPE:
		case SET_MACRO_TYPE:
		case SET_MACRO_BINDING:
		case SET_FRM_INSTR_TYPE:
		case SET_REG_EXPR_TYPE:
		case SET_IF_STMT_ACT:
		case SET_SELECT_STMT_ACT:
		case SET_SELECT_STMT_ARG:
		case SET_EXPR_ARG: 
		case SET_BOOL_EXPR_ARG: 
		case SET_EXPR_OP:
		case SET_IO_INSTR_STATE:
			options.moveUp(false);
			break;
		case ACTIVE_FRAMES:
			updateActiveFramesDisplay();
			workingText = Integer.toString(armModel.getActiveToolFrame() + 1);
			contents.moveUp(false);
			break;
		default:
			if (mode.getType() == ScreenType.TYPE_TEXT_ENTRY) {
				options.moveUp(false); 
				// Reset function key states
				for(int idx = 0; idx < letterStates.length; ++idx) { letterStates[idx] = 0; }
			}
		}

		updateScreen();
	}

	public void BackView() {
		// Back view
		camera.reset();
		camera.rotate(0, PI, 0);
	}

	public void bd() {
		// If there is a previous instruction, then move to it and reverse its affects
		if(mode == ScreenMode.NAV_PROG_INSTR && !isProgramRunning() && shift && isStep()) {
			// Stop any prior Robot movement
			armModel.halt();
			// Safeguard against editing a program while it is running
			contents.setColumnIdx(0);
			// TODO fix backwards
		}
	}

	/**
	 * Initiate a new circular motion instruction according to FANUC methodology.
	 * @param p1 Point 1
	 * @param p2 Point 2
	 * @param p3 Point 3
	 */
	public void beginNewCircularMotion(Point start, Point inter, Point end) {
		calculateArc(start, inter, end);
		interMotionIdx = 0;
		motionFrameCounter = 0;
		if(intermediatePositions.size() > 0) {
			Point tgtPoint = intermediatePositions.get(interMotionIdx);
			armModel.jumpTo(tgtPoint.position, tgtPoint.orientation);
		}
	}

	/**
	 * Initiate a new continuous (curved) motion instruction.
	 * @param model Arm model to use
	 * @param start Start point
	 * @param end Destination point
	 * @param next Point after the destination
	 * @param percentage Intensity of the curve
	 */
	public void beginNewContinuousMotion(Point start, Point end, Point next, float p) {
		calculateContinuousPositions(start, end, next, p);
		motionFrameCounter = 0;
		if(intermediatePositions.size() > 0) {
			Point tgtPoint = intermediatePositions.get(interMotionIdx);
			armModel.jumpTo(tgtPoint.position, tgtPoint.orientation);
		}
	}

	/**
	 * Initiate a new fine (linear) motion instruction.
	 * @param start Start point
	 * @param end Destination point
	 */
	public void beginNewLinearMotion(Point start, Point end) {
		calculateIntermediatePositions(start, end);
		motionFrameCounter = 0;
		if(intermediatePositions.size() > 0) {
			Point tgtPoint = intermediatePositions.get(interMotionIdx);
			armModel.jumpTo(tgtPoint.position, tgtPoint.orientation);
		}
	}

	public void BKSPC() {
		if(mode.getType() == ScreenType.TYPE_NUM_ENTRY) {
			// Functions as a backspace key
			if(workingText.length() > 1) {
				workingText = workingText.substring(0, workingText.length() - 1);
			} 
			else {
				workingText = "";
			}

		} else if(mode.getType() == ScreenType.TYPE_POINT_ENTRY) {

			// backspace function for current row
			if(contents.getLineIdx() >= 0 && contents.getLineIdx() < contents.size()) {
				String value = contents.get(contents.getLineIdx()).get(1);

				if (value.length() == 1) {
					contents.get(contents.getLineIdx()).set(1, "");
				} else if(value.length() > 1) {

					if (value.length() == 2 && value.charAt(0) == '-') {
						// Do not remove line prefix until the last digit is removed
						contents.get(contents.getLineIdx()).set(1, "");
					} else {
						contents.get(contents.getLineIdx()).set(1, value.substring(0, value.length() - 1));
					}
				}
			}
		} else if(mode.getType() == ScreenType.TYPE_TEXT_ENTRY) {
			// Backspace function
			if(workingText.length() > 1) {
				// ifan insert space exists, preserve it
				if(workingText.charAt(workingText.length() - 1) == '\0') {
					workingText = workingText.substring(0, workingText.length() - 2) + "\0";
				} 
				else {
					workingText = workingText.substring(0, workingText.length() - 1);
				}

				contents.setColumnIdx(min(contents.getColumnIdx(), workingText.length() - 1));
			} else {
				workingText = "\0";
			}

			for(int idx = 0; idx < letterStates.length; ++idx) { letterStates[idx] = 0; }
		}

		updateScreen();
	}

	public void BottomView() {
		// Bottom view
		camera.reset();
		camera.rotate(PI / 2f, 0, 0);
	}

	/**
	 * Creates an arc from 'start' to 'end' that passes through the point specified
	 * by 'inter.'
	 * @param start First point
	 * @param inter Second point
	 * @param end Third point
	 */
	public void calculateArc(Point start, Point inter, Point end) {  
		calculateDistanceBetweenPoints();
		intermediatePositions.clear();

		PVector a = start.position;
		PVector b = inter.position;
		PVector c = end.position;
		RQuaternion q1 = start.orientation;
		RQuaternion q2 = end.orientation;
		RQuaternion qi = new RQuaternion();

		// Calculate arc center point
		PVector[] plane = new PVector[3];
		plane = createPlaneFrom3Points(a, b, c);
		PVector center = circleCenter(vectorConvertTo(a, plane[0], plane[1], plane[2]),
				vectorConvertTo(b, plane[0], plane[1], plane[2]),
				vectorConvertTo(c, plane[0], plane[1], plane[2]));
		center = vectorConvertFrom(center, plane[0], plane[1], plane[2]);
		// Now get the radius (easy)
		float r = dist(center.x, center.y, center.z, a.x, a.y, a.z);
		// Calculate a vector from the center to point a
		PVector u = new PVector(a.x-center.x, a.y-center.y, a.z-center.z);
		u.normalize();
		// get the normal of the plane created by the 3 input points
		PVector tmp1 = new PVector(a.x-b.x, a.y-b.y, a.z-b.z);
		PVector tmp2 = new PVector(a.x-c.x, a.y-c.y, a.z-c.z);
		PVector n = tmp1.cross(tmp2);
		tmp1.normalize();
		tmp2.normalize();
		n.normalize();
		// calculate the angle between the start and end points
		PVector vec1 = new PVector(a.x-center.x, a.y-center.y, a.z-center.z);
		PVector vec2 = new PVector(c.x-center.x, c.y-center.y, c.z-center.z);
		vec1.normalize();
		vec2.normalize();
		float theta = atan2(vec1.cross(vec2).mag(), vec1.dot(vec2));

		// finally, draw an arc through all 3 points by rotating the u
		// vector around our normal vector
		float angle = 0, mu = 0;
		int numPoints = (int)(r*theta/distanceBetweenPoints);
		float inc = 1/(float)numPoints;
		float angleInc = (theta)/(float)numPoints;
		for(int i = 0; i < numPoints; i += 1) {
			PVector pos = RQuaternion.rotateVectorAroundAxis(u, n, angle).mult(r).add(center);
			if(i == numPoints-1) pos = end.position;
			qi = RQuaternion.SLERP(q1, q2, mu);
			//println(pos + ", " + end.position);
			intermediatePositions.add(new Point(pos, qi));
			angle += angleInc;
			mu += inc;
		}
	}

	/**
	 * Calculate a "path" (series of intermediate positions) between two
	 * points in a a curved line. Need a third point as well, or a curved
	 * line doesn't make sense.
	 * Here's how this works:
	 *   Assuming our current point is P1, and we're moving to P2 and then P3:
	 *   1 Do linear interpolation between points P2 and P3 FIRST.
	 *   2 Begin interpolation between P1 and P2.
	 *   3 When you're (cont% / 1.5)% away from P2, begin interpolating not towards
	 *     P2, but towards the points defined between P2 and P3 in step 1.
	 *   The mu for this is from 0 to 0.5 instead of 0 to 1.0.
	 *
	 * @param p1 Start point
	 * @param p2 Destination point
	 * @param p3 Third point, needed to figure out how to curve the path
	 * @param percentage Intensity of the curve
	 */
	public void calculateContinuousPositions(Point start, Point end, Point next, float percentage) {
		//percentage /= 2;
		calculateDistanceBetweenPoints();
		percentage /= 1.5f;
		percentage = 1 - percentage;
		percentage = constrain(percentage, 0, 1);
		intermediatePositions.clear();

		PVector p1 = start.position;
		PVector p2 = end.position;
		PVector p3 = next.position;
		RQuaternion q1 = start.orientation;
		RQuaternion q2 = end.orientation;
		RQuaternion q3 = next.orientation;
		RQuaternion qi = new RQuaternion();

		ArrayList<Point> secondaryTargets = new ArrayList<Point>();
		float d1 = dist(p1.x, p1.y, p1.z, p2.x, p2.y, p2.z);
		float d2 = dist(p2.x, p2.y, p2.z, p3.x, p3.y, p3.z);
		int numberOfPoints = 0;
		if(d1 > d2) {
			numberOfPoints = (int)(d1 / distanceBetweenPoints);
		} 
		else {
			numberOfPoints = (int)(d2 / distanceBetweenPoints);
		}

		float mu = 0;
		float increment = 1.0f / (float)numberOfPoints;
		for(int n = 0; n < numberOfPoints; n++) {
			mu += increment;
			qi = RQuaternion.SLERP(q2, q3, mu);
			secondaryTargets.add(new Point(new PVector(
					p2.x * (1 - mu) + (p3.x * mu),
					p2.y * (1 - mu) + (p3.y * mu),
					p2.z * (1 - mu) + (p3.z * mu)),
					qi));
		}

		mu = 0;
		int transitionPoint = (int)((float)numberOfPoints * percentage);
		for(int n = 0; n < transitionPoint; n++) {
			mu += increment;
			qi = RQuaternion.SLERP(q1, q2, mu);
			intermediatePositions.add(new Point(new PVector(
					p1.x * (1 - mu) + (p2.x * mu),
					p1.y * (1 - mu) + (p2.y * mu),
					p1.z * (1 - mu) + (p2.z * mu)),
					qi));
		}

		int secondaryIdx = 0; // accessor for secondary targets

		mu = 0;
		increment /= 2.0f;

		Point currentPoint;
		if(intermediatePositions.size() > 0) {
			currentPoint = intermediatePositions.get(intermediatePositions.size()-1);
		}
		else {
			// NOTE orientation is in Native Coordinates!
			currentPoint = nativeRobotEEPoint(armModel.getJointAngles());
		}

		for(int n = transitionPoint; n < numberOfPoints; n++) {
			mu += increment;
			Point tgt = secondaryTargets.get(secondaryIdx);
			qi = RQuaternion.SLERP(currentPoint.orientation, tgt.orientation, mu);
			intermediatePositions.add(new Point(new PVector(
					currentPoint.position.x * (1 - mu) + (tgt.position.x * mu),
					currentPoint.position.y * (1 - mu) + (tgt.position.y * mu),
					currentPoint.position.z * (1 - mu) + (tgt.position.z * mu)), 
					qi));
			currentPoint = intermediatePositions.get(intermediatePositions.size()-1);
			secondaryIdx++;
		}
		interMotionIdx = 0;
	} // end calculate continuous positions

	/**
	 * Determine how close together intermediate points between two points
	 * need to be based on current speed
	 */
	public void calculateDistanceBetweenPoints() {
		MotionInstruction instruction = activeMotionInst();
		if(instruction != null && instruction.getMotionType() != MTYPE_JOINT)
			distanceBetweenPoints = instruction.getSpeed() / 60.0f;
		else if(armModel.getCurCoordFrame() != CoordFrame.JOINT)
			distanceBetweenPoints = armModel.motorSpeed * liveSpeed / 6000f;
		else distanceBetweenPoints = 5.0f;
	}

	// TODO: Add error check for colinear case (denominator is zero)
	public float calculateH(float x1, float y1, float x2, float y2, float x3, float y3) {
		float numerator = (x2*x2+y2*y2)*y3 - (x3*x3+y3*y3)*y2 - 
				((x1*x1+y1*y1)*y3 - (x3*x3+y3*y3)*y1) +
				(x1*x1+y1*y1)*y2 - (x2*x2+y2*y2)*y1;
		float denominator = (x2*y3-x3*y2) -
				(x1*y3-x3*y1) +
				(x1*y2-x2*y1);
		denominator *= 2;
		return numerator / denominator;
	}

	/**
	 * Calculate a "path" (series of intermediate positions) between two
	 * points in a straight line.
	 * @param start Start point
	 * @param end Destination point
	 */
	public void calculateIntermediatePositions(Point start, Point end) {
		calculateDistanceBetweenPoints();
		intermediatePositions.clear();

		PVector p1 = start.position;
		PVector p2 = end.position;
		RQuaternion q1 = start.orientation;
		RQuaternion q2 = end.orientation;
		RQuaternion qi = new RQuaternion();

		float mu = 0;
		float dist = nDimDist(p1.x, p1.y, p1.z, p2.x, p2.y, p2.z);
		int numberOfPoints = (int)(dist / distanceBetweenPoints);
		
		System.out.printf("Dist: %f\n#ofPts: %d\n\n", dist, numberOfPoints);
		
		/**/
		dist = nDimDist(p1.x, p1.y, p1.z, p2.x, p2.y, p2.z) + nDimDist(q1.getValue(0), q1.getValue(1), q1.getValue(2), q1.getValue(3), q2.getValue(0), q2.getValue(1), q2.getValue(2), q2.getValue(3));
		numberOfPoints = (int)(dist / distanceBetweenPoints);
		
		System.out.printf("Dist: %f\n#ofPts: %d\n\n", dist, numberOfPoints);
		/**/
		
		float increment = 1.0f / (float)numberOfPoints;
		for(int n = 0; n < numberOfPoints; n++) {
			mu += increment;

			qi = RQuaternion.SLERP(q1, q2, mu);
			intermediatePositions.add(new Point(new PVector(
					p1.x * (1 - mu) + (p2.x * mu),
					p1.y * (1 - mu) + (p2.y * mu),
					p1.z * (1 - mu) + (p2.z * mu)),
					qi));
		}

		interMotionIdx = 0;
	} // end calculate intermediate positions
	
	/**
	 * Calculates the distance between two n-dimensional points.
	 * 
	 * @param args	The values for both n-dimensional points
	 * @return		The distance between the two points
	 * @throws		IllegalArgumentException when the number of args is odd
	 */
	public static float nDimDist(float... args) throws IllegalArgumentException {
		if (args.length % 2 != 0) {
			throw new IllegalArgumentException("The dimension of the points must be equal!");
		}
		
		int len = args.length / 2;
		float result = 0f;
		
		/* Calculate the squares of the differences between each portion of the
		 * points */
		for (int idx = 0; idx < len; ++idx) {
			result = (float)Math.pow(args[idx] - args[len + idx], 2.0);
		}
		
		return (float)Math.sqrt(result);
	}

	public float calculateK(float x1, float y1, float x2, float y2, float x3, float y3) {
		float numerator = x2*(x3*x3+y3*y3) - x3*(x2*x2+y2*y2) -
				(x1*(x3*x3+y3*y3) - x3*(x1*x1+y1*y1)) +
				x1*(x2*x2+y2*y2) - x2*(x1*x1+y1*y1);
		float denominator = (x2*y3-x3*y2) -
				(x1*y3-x3*y1) +
				(x1*y2-x2*y1);
		denominator *= 2;
		return numerator / denominator;
	}

	/**
	 * Finds the circle center of 3 points. (That is, find the center of
	 * a circle whose circumference intersects all 3 points.)
	 * The points must all lie
	 * on the same plane (all have the same Z value). Should have a check
	 * for colinear case, currently doesn't.
	 * @param a First point
	 * @param b Second point
	 * @param c Third point
	 * @return Position of circle center
	 */
	public PVector circleCenter(PVector a, PVector b, PVector c) {
		float h = calculateH(a.x, a.y, b.x, b.y, c.x, c.y);
		float k = calculateK(a.x, a.y, b.x, b.y, c.x, c.y);
		return new PVector(h, k, a.z);
	}

	public void clearContents() {
		for(int i = 0; i < index_contents; i += 1) {
			if(cp5.getGroup(Integer.toString(i)) != null)
				cp5.getGroup(Integer.toString(i)).remove();
		}

		index_contents = 0;
	}

	public void ClearFields() {
		/* Clear all input fields for creating and editing world objects. */
		getManager().clearCreateInputFields();
	}

	public void clearNums() {
		for(int i = 1000; i < index_nums; i += 1) {
			if(cp5.getGroup(Integer.toString(i)) != null)
				cp5.getGroup(Integer.toString(i)).remove();
		}

		index_nums = 1000;
	}

	public void clearOptions() {
		for(int i = 100; i < index_options; i += 1) {
			if(cp5.getGroup(Integer.toString(i)) != null)
				cp5.getGroup(Integer.toString(i)).remove();
		}

		index_options = 100;
	}

	/*
	 * Removes all text on screen and prepares the UI to transition to a
	 * new screen display.
	 */
	public void clearScreen() {
		//remove all text labels on screen  
		List<Textarea> displayText = cp5.getAll(Textarea.class);
		for(Textarea t: displayText) {
			// ONLY remove text areas from the Pendant!
			if (t.getParent().equals(g1)) {
				cp5.remove(t.getName());
			}
		}

		cp5.update();
	}

	/**
	 * Takes a vector and a (probably not quite orthogonal) second vector
	 * and computes a vector that's truly orthogonal to the first one and
	 * pointing in the direction closest to the imperfect second vector
	 * @param in First vector
	 * @param second Second vector
	 * @return A vector perpendicular to the first one and on the same side
	 *         from first as the second one.
	 */
	public PVector computePerpendicular(PVector in, PVector second) {
		PVector[] plane = createPlaneFrom3Points(in, second, new PVector(in.x*2, in.y*2, in.z*2));
		PVector v1 = vectorConvertTo(in, plane[0], plane[1], plane[2]);
		PVector v2 = vectorConvertTo(second, plane[0], plane[1], plane[2]);
		PVector perp1 = new PVector(v1.y, -v1.x, v1.z);
		PVector perp2 = new PVector(-v1.y, v1.x, v1.z);
		PVector orig = new PVector(v2.x*5, v2.y*5, v2.z);
		PVector p1 = new PVector(perp1.x*5, perp1.y*5, perp1.z);
		PVector p2 = new PVector(perp2.x*5, perp2.y*5, perp2.z);

		if(dist(orig.x, orig.y, orig.z, p1.x, p1.y, p1.z) <
				dist(orig.x, orig.y, orig.z, p2.x, p2.y, p2.z))
			return vectorConvertFrom(perp1, plane[0], plane[1], plane[2]);
		else return vectorConvertFrom(perp2, plane[0], plane[1], plane[2]);
	}

	/*Arrow keys*/

	public void COORD() {
		if(shift) {
			nextScreen(ScreenMode.ACTIVE_FRAMES);
		} else {  
			// Update the coordinate mode
			coordFrameTransition();
			updateScreen();
		}
	}
	/** 
	 * Transitions to the next Coordinate frame in the cycle, updating the Robot's current frame
	 * in the process and skipping the Tool or User frame if there are no active frames in either
	 * one. Since the Robot's frame is potentially reset in this method, all Robot motion is halted.
	 *
	 * @param model  The Robot Arm, for which to switch coordinate frames
	 */
	public void coordFrameTransition() {
		// Stop Robot movement
		armModel.halt();

		// Increment the current coordinate frame
		switch (armModel.getCurCoordFrame()) {
		case JOINT:
			armModel.setCurCoordFrame(CoordFrame.WORLD);
			break;

		case WORLD:
			armModel.setCurCoordFrame(CoordFrame.TOOL);
			break;

		case TOOL:
			armModel.setCurCoordFrame(CoordFrame.USER);
			break;

		case USER:
			armModel.setCurCoordFrame(CoordFrame.JOINT);
			break;
		}

		// Skip the Tool Frame, if there is no active frame
		if(armModel.getCurCoordFrame() == CoordFrame.TOOL && !(armModel.getActiveToolFrame() >= 0 && armModel.getActiveToolFrame() < Fields.FRAME_SIZE)) {
			armModel.setCurCoordFrame(CoordFrame.USER);
		}

		// Skip the User Frame, if there is no active frame
		if(armModel.getCurCoordFrame() == CoordFrame.USER && !(armModel.getActiveUserFrame() >= 0 && armModel.getActiveUserFrame() < Fields.FRAME_SIZE)) {
			armModel.setCurCoordFrame(CoordFrame.JOINT);
		}

		updateCoordFrame();
	}

	/**
	 * This method attempts to modify the Frame based on the given value of method.
	 * If method is even, then the frame is taught via the 3-Point Method. Otherwise,
	 * the Frame is taught by either the 4-Point or 6-Point Method based on if the
	 * Frame is a Tool Frame or a User Frame.
	 * 
	 * @param frame    The frame to be taught
	 * @param method  The method by which to each the new Frame
	 */
	public void createFrame(Frame frame, int method) {
		if (teachFrame.setFrame(abs(method) % 2)) {
			if (DISPLAY_TEST_OUTPUT) { System.out.printf("Frame set: %d\n", curFrameIdx); }

			// Set new Frame
			if (frame instanceof ToolFrame) {
				// Update the current frame of the Robot Arm
				armModel.setActiveToolFrame(curFrameIdx);
				updateCoordFrame();

				DataManagement.saveState(this);
			} else {
				// Update the current frame of the Robot Arm
				armModel.setActiveUserFrame(curFrameIdx);
				updateCoordFrame();

				DataManagement.saveState(this);
			}

		} else {
			println("Invalid input points");
		}
	}


	/**
	 * This method takes the current values stored in contents (assuming that they corresond to
	 * the six direct entry values X, Y, Z, W, P, R), parses them, saves them to given Frame object,
	 * and sets the current Frame's values to the direct entry value, setting the current frame as
	 * the active frame in the process.
	 * 
	 * @param taughtFrame  the Frame, to which the direct entry values will be stored
	 */
	public void createFrameDirectEntry(Frame taughtFrame, float[] inputs) {
		// The user enters values with reference to the World Frame
		PVector origin, wpr;

		if (taughtFrame instanceof UserFrame) {
			origin = convertWorldToNative( new PVector(inputs[0], inputs[1], inputs[2]) );
		} else {
			// Tool frame origins are actually an offset of the Robot's EE position
			origin = new PVector(inputs[0], inputs[1], inputs[2]);
		}
		// Convert the angles from degrees to radians, then convert from World to Native frame
		wpr = (new PVector(-inputs[3], inputs[5], -inputs[4])).mult(DEG_TO_RAD);

		// Save direct entry values
		taughtFrame.setDEOrigin(origin);
		taughtFrame.setDEOrientationOffset(eulerToQuat(wpr));
		taughtFrame.setFrame(2);

		if(DISPLAY_TEST_OUTPUT) {
			wpr = quatToEuler(taughtFrame.getDEOrientationOffset()).mult(RAD_TO_DEG);
			System.out.printf("\n\n%s\n%s\nFrame set: %d\n", origin.toString(),
					wpr.toString(), curFrameIdx);
		}

		// Set New Frame
		if(taughtFrame instanceof ToolFrame) {
			// Update the current frame of the Robot Arm
			armModel.setActiveToolFrame(curFrameIdx);
		} else {
			// Update the current frame of the Robot Arm
			armModel.setActiveUserFrame(curFrameIdx);
		} 

		updateCoordFrame();
		DataManagement.saveState(this);
	}

	/**
	 * Create a plane (2D coordinate system) out of 3 input points.
	 * @param a First point
	 * @param b Second point
	 * @param c Third point
	 * @return New coordinate system defined by 3 orthonormal vectors
	 */
	public PVector[] createPlaneFrom3Points(PVector a, PVector b, PVector c) {  
		PVector n1 = new PVector(a.x-b.x, a.y-b.y, a.z-b.z);
		n1.normalize();
		PVector n2 = new PVector(a.x-c.x, a.y-c.y, a.z-c.z);
		n2.normalize();
		PVector x = n1.copy();
		PVector z = n1.cross(n2);
		PVector y = x.cross(z);
		y.normalize();
		z.normalize();
		PVector[] coordinateSystem = new PVector[3];
		coordinateSystem[0] = x;
		coordinateSystem[1] = y;
		coordinateSystem[2] = z;
		return coordinateSystem;
	}

	public void createRegisterPoint(boolean fromJointAngles) {
		// Obtain point inputs from UI display text
		float[] inputs = new float[6];
		try {
			for(int idx = 0; idx < inputs.length; ++idx) {
				String inputStr = contents.get(idx).get(contents.getColumnIdx());
				inputs[idx] = Float.parseFloat(inputStr);
				// Bring the input values with the range [-9999, 9999]
				inputs[idx] = max(-9999f, min(inputs[idx], 9999f));
			}
		} catch (NumberFormatException NFEx) {
			// Invalid input
			println("Values must be real numbers!");
			return;
		}

		if(fromJointAngles) {
			// Bring angles within range: (0, TWO_PI)
			for(int idx = 0; idx < inputs.length; ++idx) {
				inputs[idx] = mod2PI(inputs[idx] * DEG_TO_RAD);
			}

			armModel.getPReg(active_index).point = nativeRobotEEPoint(inputs);
		} else {
			PVector position = convertWorldToNative( new PVector(inputs[0], inputs[1], inputs[2]) );
			// Convert the angles from degrees to radians, then convert from World to Native frame, and finally convert to a quaternion
			RQuaternion orientation = eulerToQuat( (new PVector(-inputs[3], inputs[5], -inputs[4]).mult(DEG_TO_RAD)) );

			// Use default the Robot's joint angles for computing inverse kinematics
			float[] jointAngles = inverseKinematics(new float[] {0f, 0f, 0f, 0f, 0f, 0f}, position, orientation);
			armModel.getPReg(active_index).point = new Point(position, orientation, jointAngles);
		}

		armModel.getPReg(active_index).isCartesian = !fromJointAngles;
		DataManagement.saveState(this);
	}

	public void CreateWldObj() {
		/* Create a world object from the input fields in the Create window. */
		if (activeScenario != null) {
			WorldObject newObject = getManager().createWorldObject();

			if (newObject != null) {
				newObject.setLocalCenter( new PVector(-500f, 0f, 0f) );
				activeScenario.addWorldObject(newObject);
			}
		}
	}

	// Data button
	public void da() {
		resetStack();
		nextScreen(ScreenMode.NAV_DATA);
	}

	public void DeleteWldObj() {
		// Delete focused world object
		int ret = getManager().deleteActiveWorldObject();
		if (DISPLAY_TEST_OUTPUT) { System.out.printf("World Object removed: %d\n", ret); }
	}

	/**
	 * Displays coordinate frame associated with the current Coordinate frame. The active User frame is displayed in the User and Tool
	 * Coordinate Frames. The World frame is display in the World Coordinate frame and the Tool Coordinate Frame in the case that no
	 * active User frame is set. The active Tool frame axes are displayed in the Tool frame in addition to the current User (or World)
	 * frame. Nothing is displayed in the Joint Coordinate Frame.
	 */
	public void displayAxes() {

		Point eePoint = nativeRobotEEPoint(armModel.getJointAngles());

		if (axesState == AxesDisplay.AXES && armModel.getCurCoordFrame() == CoordFrame.TOOL) {
			Frame activeTool = armModel.getActiveFrame(CoordFrame.TOOL);

			// Draw the axes of the active Tool frame at the Robot End Effector
			displayOriginAxes(eePoint.position, activeTool.getWorldAxisVectors(), 200f, color(255, 0, 255));
		} else {
			// Draw axes of the Robot's End Effector frame for testing purposes
			//displayOriginAxes(eePoint.position, eePoint.orientation.toMatrix(), 200f, color(255, 0, 255));

			/* Draw a pink point for the Robot's current End Effecot position */
			pushMatrix();
			translate(eePoint.position.x, eePoint.position.y, eePoint.position.z);

			stroke(color(255, 0, 255));
			noFill();
			sphere(4);

			popMatrix();
		}

		if (axesState == AxesDisplay.AXES) {
			// Display axes
			if (armModel.getCurCoordFrame() != CoordFrame.JOINT) {
				Frame activeUser = armModel.getActiveFrame(CoordFrame.USER);

				if(armModel.getCurCoordFrame() != CoordFrame.WORLD && activeUser != null) {
					// Draw the axes of the active User frame
					displayOriginAxes(activeUser.getOrigin(), activeUser.getWorldAxisVectors(), 10000f, color(0));

				} else {
					// Draw the axes of the World frame
					displayOriginAxes(new PVector(0f, 0f, 0f), WORLD_AXES, 10000f, color(0));
				}
			}

		} else if (axesState == AxesDisplay.GRID) {
			// Display gridlines spanning from axes of the current frame
			Frame active;
			float[][] displayAxes;
			PVector displayOrigin;

			switch(armModel.getCurCoordFrame()) {
			case JOINT:
			case WORLD:
				displayAxes = new float[][] { {1f, 0f, 0f}, {0f, 1f, 0f}, {0f, 0f, 1f} };
				displayOrigin = new PVector(0f, 0f, 0f);
				break;
			case TOOL:
				active = armModel.getActiveFrame(CoordFrame.TOOL);
				displayAxes = active.getNativeAxisVectors();
				displayOrigin = eePoint.position;
				break;
			case USER:
				active = armModel.getActiveFrame(CoordFrame.USER);
				displayAxes = active.getNativeAxisVectors();
				displayOrigin = active.getOrigin();
				break;
			default:
				// No gridlines are displayed in the Joint Coordinate Frame
				return;
			}

			// Draw grid lines every 100 units, from -3500 to 3500, in the x and y plane, on the floor plane
			displayGridlines(displayAxes, displayOrigin, 35, 100);
		}
	}


	/**
	 * Gridlines are drawn, spanning from two of the three axes defined by the given axes vector set. The two axes that form a
	 * plane that has the lowest offset of the xz-plane (hence the two vectors with the minimum y-values) are chosen to be
	 * mapped to the xz-plane and their reflection on the xz-plane are drawn the along with a grid is formed from the the two
	 * reflection axes at the base of the Robot.
	 * 
	 * @param axesVectors     A rotation matrix (in row major order) that defines the axes of the frame to map to the xz-plane
	 * @param origin          The xz-origin at which to drawn the reflection axes
	 * @param halfNumOfLines  Half the number of lines to draw for one of the axes
	 * @param distBwtLines    The distance between each gridline
	 */
	public void displayGridlines(float[][] axesVectors, PVector origin, int halfNumOfLines, float distBwtLines) {
		int vectorPX = -1, vectorPZ = -1;

		// Find the two vectors with the minimum y values
		for (int v = 0; v < axesVectors.length; ++v) {
			int limboX = (v + 1) % axesVectors.length,
					limboY = (limboX + 1) % axesVectors.length;
			// Compare the y value of the current vector to those of the other two vectors
			if (abs(axesVectors[v][1]) >= abs(axesVectors[limboX][1]) && abs(axesVectors[v][1]) >= abs(axesVectors[limboY][1])) {
				vectorPX = limboX;
				vectorPZ = limboY;
				break;
			}
		}

		if (vectorPX == -1 || vectorPZ == -1) {
			println("Invalid axes-origin pair for grid lines!");
			return;
		}

		pushMatrix();
		// Map the chosen two axes vectors to the xz-plane at the y-position of the Robot's base
		applyMatrix(axesVectors[vectorPX][0], 0, axesVectors[vectorPZ][0], origin.x,
				0, 1,                        0, ROBOT_POSITION.y,
				axesVectors[vectorPX][2], 0, axesVectors[vectorPZ][2], origin.z,
				0, 0,                        0,        1);

		float lineLen = halfNumOfLines * distBwtLines;

		// Draw axes lines in red
		stroke(255, 0, 0);
		line(-lineLen, 0, 0, lineLen, 0, 0);
		line(0, 0, -lineLen, 0, 0, lineLen);
		// Draw remaining gridlines in black
		stroke(25, 25, 25);
		for(int linePosScale = 1; linePosScale <= halfNumOfLines; ++linePosScale) {
			line(distBwtLines * linePosScale, 0, -lineLen, distBwtLines * linePosScale, 0, lineLen);
			line(-lineLen, 0, distBwtLines * linePosScale, lineLen, 0, distBwtLines * linePosScale);

			line(-distBwtLines * linePosScale, 0, -lineLen, -distBwtLines * linePosScale, 0, lineLen);
			line(-lineLen, 0, -distBwtLines * linePosScale, lineLen, 0, -distBwtLines * linePosScale);
		}

		popMatrix();
		mapToRobotBasePlane();
	}

	/**
	 * Given a set of 3 orthogonal unit vectors a point in space, lines are
	 * drawn for each of the three vectors, which intersect at the origin point.
	 *
	 * @param origin       A point in space representing the intersection of the
	 *                     three unit vectors
	 * @param axesVectors  A set of three orthogonal unti vectors
	 * @param axesLength   The length, to which the all axes, will be drawn
	 * @param originColor  The color of the point to draw at the origin
	 */
	public void displayOriginAxes(PVector origin, float[][] axesVectors, float axesLength, int originColor) {

		pushMatrix();    
		// Transform to the reference frame defined by the axes vectors
		applyMatrix(axesVectors[0][0], axesVectors[1][0], axesVectors[2][0], origin.x,
				axesVectors[0][1], axesVectors[1][1], axesVectors[2][1], origin.y,
				axesVectors[0][2], axesVectors[1][2], axesVectors[2][2], origin.z,
				0, 0, 0, 1);

		// X axis
		stroke(255, 0, 0);
		line(-axesLength, 0, 0, axesLength, 0, 0);
		// Y axis
		stroke(0, 255, 0);
		line(0, -axesLength, 0, 0, axesLength, 0);
		// Z axis
		stroke(0, 0, 255);
		line(0, 0, -axesLength, 0, 0, axesLength);

		// Draw a sphere on the positive direction for each axis
		float dotPos = max(100f, min(axesLength, 500));

		stroke(originColor);
		sphere(4);
		stroke(0);
		translate(dotPos, 0, 0);
		sphere(4);
		translate(-dotPos, dotPos, 0);
		sphere(4);
		translate(0, -dotPos, dotPos);
		sphere(4);

		popMatrix();
	}

	/**
	 * Display any currently taught points during the processes of either the 3-Point, 4-Point, or 6-Point Methods.
	 */
	public void displayTeachPoints() {
		// Teach points are displayed only while the Robot is being taught a frame
		if(teachFrame != null && mode.getType() == ScreenType.TYPE_TEACH_POINTS) {

			int size = 3;

			if (mode == ScreenMode.TEACH_6PT && teachFrame instanceof ToolFrame) {
				size = 6;
			} else if (mode == ScreenMode.TEACH_4PT && teachFrame instanceof UserFrame) {
				size = 4;
			}

			for (int idx = 0; idx < size; ++idx) {
				Point pt = teachFrame.getPoint(idx);

				if (pt != null) {
					pushMatrix();
					// Applies the point's position
					translate(pt.position.x, pt.position.y, pt.position.z);

					// Draw color-coded sphere for the point
					noFill();
					int pointColor = color(255, 0, 255);

					if (teachFrame instanceof ToolFrame) {

						if (idx < 3) {
							// TCP teach points
							pointColor = color(130, 130, 130);
						} else if (idx == 3) {
							// Orient origin point
							pointColor = color(255, 130, 0);
						} else if (idx == 4) {
							// Axes X-Direction point
							pointColor = color(255, 0, 0);
						} else if (idx == 5) {
							// Axes Y-Diretion point
							pointColor = color(0, 255, 0);
						}
					} else if (teachFrame instanceof UserFrame) {

						if (idx == 0) {
							// Orient origin point
							pointColor = color(255, 130, 0);
						} else if (idx == 1) {
							// Axes X-Diretion point
							pointColor = color(255, 0, 0);
						} else if (idx == 2) {
							// Axes Y-Diretion point
							pointColor = color(0, 255, 0);
						} else if (idx == 3) {
							// Axes Origin point
							pointColor = color(0, 0, 255);
						}
					}

					stroke(pointColor);
					sphere(3);

					popMatrix();
				}
			}
		}
	}

	public void draw() {
		// Apply the camera for drawing objects
		directionalLight(255, 255, 255, 1, 1, 0);
		ambientLight(150, 150, 150);

		background(127);

		hint(ENABLE_DEPTH_TEST);
		background(255);
		noStroke();
		noFill();

		pushMatrix();
		camera.apply();

		Program p = activeProgram();

		updateAndDrawObjects(activeScenario, p, armModel);
		displayAxes();
		displayTeachPoints();

		WorldObject wldObj = getManager().getActiveWorldObject();

		if (wldObj != null) {
			pushMatrix();

			if (wldObj instanceof Part) {
				Fixture reference = ((Part)wldObj).getFixtureRef();

				if (reference != null) {
					// Draw part's orientation with reference to its fixture
					reference.applyCoordinateSystem();
				}
			}

			displayOriginAxes(wldObj.getLocalCenter(), wldObj.getLocalOrientationAxes(), 500f, color(0));

			popMatrix();
		}

		if (displayPoint != null) {
			// Display the point with its local orientation axes
			displayOriginAxes(displayPoint.position, displayPoint.orientation.toMatrix(), 100f, color(0, 100, 15));
		}

		//TESTING CODE: DRAW INTERMEDIATE POINTS
		noLights();
		noStroke();
		pushMatrix();
		//if(intermediatePositions != null) {
		//  int count = 0;
		//  for(Point pt : intermediatePositions) {
		//    if(count % 20 == 0) {
		//      pushMatrix();
		//      stroke(0);
		//      translate(pt.position.x, pt.position.y, pt.position.z);
		//      sphere(5);
		//      popMatrix();
		//    }
		//    count += 1;
		//  }
		//}
		popMatrix();
		popMatrix();

		hint(DISABLE_DEPTH_TEST);
		// Apply the camera for drawing text and windows
		ortho();
		showMainDisplayText();
		cp5.draw();
		//println(frameRate + " fps");
	}

	public void editExpression(Expression expr, int selectIdx) {
		int[] elements = expr.mapToEdit();
		opEdit = expr;
		ExpressionElement e = expr.get(elements[selectIdx]);

		if(e instanceof Expression) {
			//if selecting the open or close paren
			if(selectIdx == 0 || selectIdx == e.getLength() || 
					elements[selectIdx - 1] != elements[selectIdx] || 
					elements[selectIdx + 1] != elements[selectIdx]) {
				nextScreen(ScreenMode.SET_EXPR_ARG);
			} else {
				int startIdx = expr.getStartingIdx(elements[selectIdx]);
				editExpression((Expression)e, selectIdx - startIdx - 1);
			}
		} else if(e instanceof ExprOperand) {
			editOperand((ExprOperand)e, elements[selectIdx]);
		} else {
			editIdx = elements[selectIdx];
			nextScreen(ScreenMode.SET_EXPR_OP);
		}
	}

	/**
	 * Accepts an ExpressionOperand object and forwards the UI to the appropriate
	 * menu to edit said object based on the operand type.
	 *
	 * @param o - The operand to be edited.
	 * @ins_idx - The index of the operand's container ExpressionElement list into which this
	 *     operand is stored.
	 *
	 */
	public void editOperand(ExprOperand o, int ins_idx) {
		switch(o.type) {
		case -2: //Uninit
			editIdx = ins_idx;
			nextScreen(ScreenMode.SET_EXPR_ARG);
			break;
		case 0: //Float const
			opEdit = o;
			nextScreen(ScreenMode.INPUT_CONST);
			break;
		case 1: //Bool const
			opEdit = o;
			nextScreen(ScreenMode.SET_BOOL_CONST);
			break;
		case 2: //Data reg
			opEdit = o;
			nextScreen(ScreenMode.INPUT_DREG_IDX);
			break;
		case 3: //IO reg
			opEdit = o;
			nextScreen(ScreenMode.INPUT_IOREG_IDX);
			break;
		case 4: // Pos reg
			opEdit = o;
			nextScreen(ScreenMode.INPUT_PREG_IDX1);
			break;
		case 5: // Pos reg at index
			opEdit = o;
			nextScreen(ScreenMode.INPUT_PREG_IDX2);
			nextScreen(ScreenMode.INPUT_PREG_IDX1);
			break;
		}
	}

	public void editTextEntry(int fIdx) {
		char newChar = letters[fIdx][letterStates[fIdx]];
		if(options.getLineIdx() == 0 && !(fIdx == 4 && letterStates[fIdx] > 1)) {
			// Use uppercase character
			newChar = (char)(newChar - 32);
		}

		StringBuilder temp = new StringBuilder(workingText);
		temp.setCharAt(contents.getColumnIdx(), newChar);
		workingText = temp.toString();

		// Update current letter state
		letterStates[fIdx] = (letterStates[fIdx] + 1) % 6;
		for(int idx = 0; idx < letterStates.length; idx += 1) {
			// Reset all other letter states
			if (idx != fIdx) {
				letterStates[idx] = 0;
			}
		}
	}

	public void EE() {
		armModel.cycleEndEffector();
	}

	public void ENTER() {
		Program p = activeProgram();

		switch(mode) {
		//Main menu
		case NAV_MAIN_MENU:
			if(options.getLineIdx() == 0) { // Frames
				nextScreen(ScreenMode.SELECT_FRAME_MODE);
			} else if(options.getLineIdx() == 1) { // Macros
				nextScreen(ScreenMode.NAV_MACROS);
			} else { // Manual Functions
				nextScreen(ScreenMode.NAV_MF_MACROS);
			}
			break; 
			//Frame nav and edit
		case SELECT_FRAME_MODE:
			if(options.getLineIdx() == 0) {
				nextScreen(ScreenMode.NAV_TOOL_FRAMES);
			}
			else if(options.getLineIdx() == 1) {
				nextScreen(ScreenMode.NAV_USER_FRAMES);
			}
			break;
		case ACTIVE_FRAMES:
			updateActiveFramesDisplay();
			break;
		case NAV_TOOL_FRAMES:
			curFrameIdx = contents.get(contents.getLineIdx()).getItemIdx();
			nextScreen(ScreenMode.TFRAME_DETAIL);
			break;
		case NAV_USER_FRAMES:
			curFrameIdx = contents.get(contents.getLineIdx()).getItemIdx();
			nextScreen(ScreenMode.UFRAME_DETAIL);
			break;
		case FRAME_METHOD_USER:
			// User Frame teaching methods
			teachFrame = armModel.getUserFrame(curFrameIdx);
			if(options.getLineIdx() == 0) {
				nextScreen(ScreenMode.TEACH_3PT_USER);
			} 
			else if(options.getLineIdx() == 1) {
				nextScreen(ScreenMode.TEACH_4PT);
			} 
			else if(options.getLineIdx() == 2) {
				nextScreen(ScreenMode.DIRECT_ENTRY_USER);
			}
			break;
		case FRAME_METHOD_TOOL:
			teachFrame = armModel.getToolFrame(curFrameIdx);
			// Tool Frame teaching methods
			if(options.getLineIdx() == 0) {
				nextScreen(ScreenMode.TEACH_3PT_TOOL);
			} 
			else if(options.getLineIdx() == 1) {
				nextScreen(ScreenMode.TEACH_6PT);
			} 
			else if(options.getLineIdx() == 2) {
				nextScreen(ScreenMode.DIRECT_ENTRY_TOOL);
			}
			break;
		case TEACH_3PT_TOOL:
		case TEACH_3PT_USER:
			createFrame(teachFrame, 0);
			lastScreen();
			break;
		case TEACH_4PT:
		case TEACH_6PT:
			createFrame(teachFrame, 1);
			lastScreen();
			break;
		case DIRECT_ENTRY_TOOL:
		case DIRECT_ENTRY_USER:
			// User defined x, y, z, w, p, and r values
			float[] inputs = new float[] { 0f, 0f, 0f, 0f, 0f, 0f };

			try {
				// Parse each input value
				for(int val = 0; val < inputs.length; ++val) {
					String str = contents.get(val).get(1);

					if(str.length() < 0) {
						// No value entered
						updateScreen();
						println("All entries must have a value!");
						return;
					}

					// Remove prefix
					inputs[val] = Float.parseFloat(str);
					// Bring within range of values
					inputs[val] = max(-9999f, min(inputs[val], 9999f));
				}

				createFrameDirectEntry(teachFrame, inputs);
			} catch (NumberFormatException NFEx) {
				// Invalid number
				println("Entries must be real numbers!");
				return;
			}

			if (teachFrame instanceof UserFrame) {
				nextScreen(ScreenMode.UFRAME_DETAIL);
			} else {
				nextScreen(ScreenMode.TFRAME_DETAIL);
			}
			break;  

			//Program nav and edit
		case PROG_CREATE:
			if(!workingText.equals("\0")) {
				if (workingText.charAt(workingText.length() - 1) == '\0') {
					// Remove insert character
					workingText = workingText.substring(0, workingText.length() - 1);
				}

				int new_prog = armModel.addProgram(new Program(workingText));
				setActive_prog(new_prog);
				setActive_instr(0);
				contents.reset();

				DataManagement.saveState(this);    
				switchScreen(ScreenMode.NAV_PROG_INSTR);
			}
			break;
		case PROG_RENAME:
			if(!workingText.equals("\0")) {
				if (workingText.charAt(workingText.length() - 1) == '\0') {
					// Remove insert character
					workingText = workingText.substring(0, workingText.length() - 1);
				}
				// Renmae the program
				activeProgram().setName(workingText);
				setActive_instr(0);
				contents.reset();

				DataManagement.saveState(this);
				resetStack();
				nextScreen(ScreenMode.NAV_PROGRAMS);
			}
			break;
		case PROG_COPY:
			if(!workingText.equals("\0")) {
				if (workingText.charAt(workingText.length() - 1) == '\0') {
					// Remove insert character
					workingText = workingText.substring(0, workingText.length() - 1);
				}

				Program newProg = activeProgram().clone();
				newProg.setName(workingText);
				int new_prog = armModel.addProgram(newProg);
				setActive_prog(new_prog);
				setActive_instr(0);
				contents.reset();

				DataManagement.saveState(this);
				resetStack();
				nextScreen(ScreenMode.NAV_PROGRAMS);
			}
			break;
		case NAV_PROGRAMS:
			if(armModel.numOfPrograms() != 0) {
				setActive_instr(0);
				contents.reset();
				nextScreen(ScreenMode.NAV_PROG_INSTR);
			}
			break;

			//Instruction options menu
		case NAV_INSTR_MENU:
			MotionInstruction m;

			switch(options.getLineIdx()) {
			case 0: //Insert
				nextScreen(ScreenMode.CONFIRM_INSERT);
				break;
			case 1: //Delete
				contents.resetSelection(p.getInstructions().size());
				nextScreen(ScreenMode.SELECT_INSTR_DELETE);
				break;
			case 2: //Cut/Copy
				contents.resetSelection(p.getInstructions().size());
				nextScreen(ScreenMode.SELECT_CUT_COPY);
				break;
			case 3: //Paste
				nextScreen(ScreenMode.SELECT_PASTE_OPT);          
				break;
			case 4: //Find/Replace
				nextScreen(ScreenMode.FIND_REPL);
				break;
			case 5: //Renumber
				nextScreen(ScreenMode.CONFIRM_RENUM);
				break;
			case 6: //Comment
				contents.resetSelection(p.getInstructions().size());
				nextScreen(ScreenMode.SELECT_COMMENT);
				break;
			case 7: //Undo
			case 8: //Remark
			}

			break;

			//Instruction insert menus
		case SELECT_INSTR_INSERT:
			switch(options.getLineIdx()){
			case 0: // I/O
				nextScreen(ScreenMode.SELECT_IO_INSTR_REG);
				break;
			case 1: // Offset/Frames
				nextScreen(ScreenMode.SELECT_FRAME_INSTR_TYPE);
				break;
			case 2: //Register 
				nextScreen(ScreenMode.SELECT_REG_STMT);
				break;
			case 3: //IF/ SELECT
				nextScreen(ScreenMode.SELECT_COND_STMT);
				break;
			case 4: //JMP/ LBL
				nextScreen(ScreenMode.SELECT_JMP_LBL);
				break;
			case 5: //Call
				newCallInstruction();
				switchScreen(ScreenMode.SET_CALL_PROG);
				break;
			}

			break;
		case SELECT_IO_INSTR_REG:
			newIOInstruction();
			display_stack.pop();
			lastScreen();
			break;
		case SELECT_FRAME_INSTR_TYPE:
			if(options.getLineIdx() == 0){
				newFrameInstruction(FTYPE_TOOL);
			} else {
				newFrameInstruction(FTYPE_USER);
			}

			display_stack.pop();
			switchScreen(ScreenMode.SET_FRAME_INSTR_IDX);
			break;
		case SELECT_REG_STMT:
			display_stack.pop();
			display_stack.pop();

			if(options.getLineIdx() == 0) {
				newRegisterStatement(new DataRegister());
			} else if(options.getLineIdx() == 1){
				newRegisterStatement(new IORegister());
			} else if(options.getLineIdx() == 2){
				newRegisterStatement(new PositionRegister());
			} else {
				newRegisterStatement(new PositionRegister(), 0);
				display_stack.push(ScreenMode.SET_REG_EXPR_IDX2);
			}

			nextScreen(ScreenMode.SET_REG_EXPR_IDX1);
			break;
		case SELECT_COND_STMT:
			if(options.getLineIdx() == 0) {
				newIfStatement();
				display_stack.pop();
				switchScreen(ScreenMode.SET_EXPR_OP);
			} else if(options.getLineIdx() == 1) {
				newIfExpression();
				display_stack.pop();
				lastScreen();
			} else {
				newSelectStatement();
				display_stack.pop();
				lastScreen();
			}

			break;
		case SELECT_JMP_LBL:
			display_stack.pop();

			if(options.getLineIdx() == 0) {
				newLabel();
				switchScreen(ScreenMode.SET_LBL_NUM);
			} else {
				newJumpInstruction();
				switchScreen(ScreenMode.SET_JUMP_TGT);
			}

			break;

			//Movement instruction edit
		case SET_MV_INSTR_TYPE:
			m = activeMotionInst();
			if(options.getLineIdx() == 0) {
				if(m.getMotionType() != MTYPE_JOINT) m.setSpeed(m.getSpeed()/armModel.motorSpeed);
				m.setMotionType(MTYPE_JOINT);
			} else if(options.getLineIdx() == 1) {
				if(m.getMotionType() == MTYPE_JOINT) m.setSpeed(armModel.motorSpeed*m.getSpeed());
				m.setMotionType(MTYPE_LINEAR);
			} else if(options.getLineIdx() == 2) {
				if(m.getMotionType() == MTYPE_JOINT) m.setSpeed(armModel.motorSpeed*m.getSpeed());
				m.setMotionType(MTYPE_CIRCULAR);
			}

			lastScreen();
			break;
		case SET_MV_INSTR_REG_TYPE:
			int line = getSelectedLine();
			m = line == 0 ? activeMotionInst() : activeMotionInst().getSecondaryPoint();

			if(options.getLineIdx() == 0) {
				m.setGlobalPosRegUse(false);

			} else if(options.getLineIdx() == 1) {
				m.setGlobalPosRegUse(true);
			}

			lastScreen();
			break;
		case SET_MV_INSTR_SPD:
			line = getSelectedLine();
			m = line == 0 ? activeMotionInst() : activeMotionInst().getSecondaryPoint();

			float tempSpeed = Float.parseFloat(workingText);
			if(tempSpeed >= 5.0f) {
				if(speedInPercentage) {
					if(tempSpeed > 100) tempSpeed = 10; 
					tempSpeed /= 100.0f;
				} else if(tempSpeed > armModel.motorSpeed) {
					tempSpeed = armModel.motorSpeed;
				}

				m.setSpeed(tempSpeed);
				DataManagement.saveState(this);
			}

			lastScreen();
			break;
		case SET_MV_INSTR_IDX:
			try {
				int tempRegister = Integer.parseInt(workingText);
				int lbound = 1, ubound;

				if (activeMotionInst().usesGPosReg()) {
					ubound = 100;

				} else {
					ubound = 1000;
				}

				line = getSelectedLine();
				m = line == 0 ? activeMotionInst() : activeMotionInst().getSecondaryPoint();

				if(tempRegister < lbound || tempRegister > ubound) {
					// Invalid register index
					err = String.format("Only registers %d-%d are valid!", lbound, ubound);
					lastScreen();
					return;
				}

				m.setPositionNum(tempRegister - 1);
			} catch (NumberFormatException NFEx) { /* Ignore invalid numbers */ }

			lastScreen();
			break;
		case SET_MV_INSTR_TERM:
			try {
				int tempTerm = Integer.parseInt(workingText);
				line = getSelectedLine();
				m = line == 0 ? activeMotionInst() : activeMotionInst().getSecondaryPoint();

				if(tempTerm >= 0 && tempTerm <= 100) {
					m.setTermination(tempTerm);
				}
			} catch (NumberFormatException NFEx) { /* Ignore invalid input */ }

			lastScreen();
			break;
		case SET_MV_INSTR_OFFSET:
			try {
				int tempRegister = Integer.parseInt(workingText) - 1;
				line = getSelectedLine();
				m = line == 0 ? activeMotionInst() : activeMotionInst().getSecondaryPoint();

				if(tempRegister < 1 || tempRegister > 1000) {
					// Invalid register index
					err = "Only registers 1 - 1000 are legal!";
					lastScreen();
					return;
				} else if((armModel.getPReg(tempRegister)).point == null) {
					// Invalid register index
					err = "This register is uninitailized!";
					lastScreen();
					return;
				}

				m.setOffset(tempRegister);
			} catch (NumberFormatException NFEx) { /* Ignore invalid numbers */ }

			lastScreen();
			break;

			//Expression edit
		case SET_EXPR_ARG:
			Expression expr = (Expression)opEdit;

			if(options.getLineIdx() == 0) {
				//set arg to new data reg
				ExprOperand operand = new ExprOperand(new DataRegister());
				opEdit = expr.setOperand(editIdx, operand);
				switchScreen(ScreenMode.INPUT_DREG_IDX);
			} else if(options.getLineIdx() == 1) {
				//set arg to new io reg
				ExprOperand operand = new ExprOperand(new IORegister());
				opEdit = expr.setOperand(editIdx, operand);
				switchScreen(ScreenMode.INPUT_IOREG_IDX);
			} else if(options.getLineIdx() == 2) {
				ExprOperand operand = new ExprOperand(new PositionRegister());
				opEdit = expr.setOperand(editIdx, operand);
				switchScreen(ScreenMode.INPUT_PREG_IDX1);
			} else if(options.getLineIdx() == 3) {
				ExprOperand operand = new ExprOperand(new PositionRegister(), 0);
				opEdit = expr.setOperand(editIdx, operand);
				display_stack.pop();
				display_stack.push(ScreenMode.INPUT_PREG_IDX2);
				nextScreen(ScreenMode.INPUT_PREG_IDX1);
			} else if(options.getLineIdx() == 4) {
				//set arg to new expression
				Expression oper = new Expression();
				expr.setOperand(editIdx, oper);
				lastScreen();
			} else {
				//set arg to new constant
				opEdit = expr.getOperand(editIdx).reset();
				switchScreen(ScreenMode.INPUT_CONST);
			}

			break;
		case SET_BOOL_EXPR_ARG:
			if(options.getLineIdx() == 0) {
				//set arg to new data reg
				opEdit.set(new DataRegister());
				switchScreen(ScreenMode.INPUT_DREG_IDX);
			} else if(options.getLineIdx() == 1) {
				//set arg to new io reg
				opEdit.set(new IORegister());
				switchScreen(ScreenMode.INPUT_IOREG_IDX);
			} else {
				//set arg to new constant
				opEdit.reset();
				switchScreen(ScreenMode.INPUT_CONST);
			}
			break;
		case SET_IF_STMT_ACT:
			IfStatement stmt = (IfStatement)activeInstruction();
			if(options.getLineIdx() == 0) {
				stmt.setInstr(new JumpInstruction());
				switchScreen(ScreenMode.SET_JUMP_TGT);
			} else {
				stmt.setInstr(new CallInstruction());
				switchScreen(ScreenMode.SET_CALL_PROG);
			}

			break;
		case SET_EXPR_OP:
			if(opEdit instanceof Expression) {
				expr = (Expression)opEdit;

				switch(options.getLineIdx()) {
				case 0:
					expr.setOperator(editIdx, Operator.ADDTN);
					break;
				case 1:
					expr.setOperator(editIdx, Operator.SUBTR);
					break;
				case 2:
					expr.setOperator(editIdx, Operator.MULT);
					break;
				case 3:
					expr.setOperator(editIdx, Operator.DIV);
					break;
				case 4:
					expr.setOperator(editIdx, Operator.INTDIV);
					break;
				case 5:
					expr.setOperator(editIdx, Operator.MOD);
					break;
				case 6:
					expr.setOperator(editIdx, Operator.EQUAL);
					break;
				case 7:
					expr.setOperator(editIdx, Operator.NEQUAL);
					break;
				case 8:
					expr.setOperator(editIdx, Operator.GRTR);
					break;
				case 9:
					expr.setOperator(editIdx, Operator.LESS);
					break;
				case 10:
					expr.setOperator(editIdx, Operator.GREQ);
					break;
				case 11:
					expr.setOperator(editIdx, Operator.LSEQ);
					break;
				case 12:
					expr.setOperator(editIdx, Operator.AND);
					break;
				case 13:
					expr.setOperator(editIdx, Operator.OR);
					break;
				case 14:
					expr.setOperator(editIdx, Operator.NOT);
					break;
				}
			}
			else if(opEdit instanceof AtomicExpression) {
				AtomicExpression atmExpr = (AtomicExpression)opEdit;

				switch(options.getLineIdx()) {
				case 0:
					atmExpr.setOperator(Operator.EQUAL);
					break;
				case 1:
					atmExpr.setOperator(Operator.NEQUAL);
					break;
				case 2:
					atmExpr.setOperator(Operator.GRTR);
					break;
				case 3:
					atmExpr.setOperator(Operator.LESS);
					break;
				case 4:
					atmExpr.setOperator(Operator.GREQ);
					break;
				case 5:
					atmExpr.setOperator(Operator.LSEQ);
					break;
				}
			}

			lastScreen();
			break;
		case INPUT_DREG_IDX:
		case INPUT_IOREG_IDX:
		case INPUT_PREG_IDX1:
		case INPUT_PREG_IDX2:
			try {
				int idx = Integer.parseInt(workingText);
				
				if (mode == ScreenMode.INPUT_DREG_IDX) {
					
					if (idx < 1 || idx > 100) {
						System.out.println("Invalid index!");
						
					} else {
						opEdit.set(armModel.getDReg(idx - 1));
					}
					
				} else if (mode == ScreenMode.INPUT_PREG_IDX1) {
					
					if (idx < 1 || idx > 100) {
						System.out.println("Invalid index!");
						
					} else {
						opEdit.set(armModel.getPReg(idx - 1));
					}
					
				} else if (mode == ScreenMode.INPUT_PREG_IDX2) {
					
					if (idx < 1 || idx > 6) {
						System.out.println("Invalid index!");
						
					} else {
						opEdit.set(idx - 1);
					}
					
				} else if (mode == ScreenMode.INPUT_IOREG_IDX) {
					
					if (idx < 1 || idx > 5) {
						System.out.println("Invalid index!");
						
					} else {
						opEdit.set(armModel.getIOReg(idx - 1));
					}
				}

			} catch(NumberFormatException e) {}

			lastScreen();
			break;
		case INPUT_CONST:
			try{
				float data = Float.parseFloat(workingText);
				opEdit.set(data);
			} catch(NumberFormatException e) {}

			lastScreen();
			break;
		case SET_BOOL_CONST:
			if(options.getLineIdx() == 0) {
				opEdit.set(true);
			} else {
				opEdit.set(false);
			}

			lastScreen();
			break;

			//Select statement edit
		case SET_SELECT_STMT_ACT:
			SelectStatement s = (SelectStatement)activeInstruction();
			int i = (getSelectedIdx() - 3) / 3;

			if(options.getLineIdx() == 0) {
				s.getInstrs().set(i, new JumpInstruction());
			} else {
				s.getInstrs().set(i, new CallInstruction());
			}

			lastScreen();
			break;
		case SET_SELECT_STMT_ARG:
			if(options.getLineIdx() == 0) {
				opEdit.set(new DataRegister());
			} else {
				opEdit.reset();
			}

			nextScreen(ScreenMode.SET_SELECT_ARGVAL);
			break;
		case SET_SELECT_ARGVAL:
			try {
				s = (SelectStatement)activeInstruction();
				float f = Float.parseFloat(workingText);

				if(opEdit.type == ExpressionElement.UNINIT) {
					opEdit.set(f);
				} else if(opEdit.type == ExpressionElement.DREG) {
					//println(regFile.DAT_REG[(int)f - 1].value);
					opEdit.set(armModel.getDReg((int)f - 1));
				}
			} catch(NumberFormatException ex) {}

			display_stack.pop();
			lastScreen();
			break;

			//IO instruction edit
		case SET_IO_INSTR_STATE:
			IOInstruction ioInst = (IOInstruction)activeInstruction();

			if(options.getLineIdx() == 0) {
				ioInst.setState(Fields.ON);
			} else {
				ioInst.setState(Fields.OFF);
			}

			lastScreen();
			break;
		case SET_IO_INSTR_IDX:
			try {
				int tempReg = Integer.parseInt(workingText);

				if(tempReg < 0 || tempReg >= 5) {
					System.out.println("Invalid index!");
					
				} else {
					ioInst = (IOInstruction)activeInstruction();
					ioInst.setReg(tempReg);
				}
			}
			catch (NumberFormatException NFEx){ /* Ignore invalid input */ }

			lastScreen();
			break;

			//Frame instruction edit
		case SET_FRM_INSTR_TYPE:
			FrameInstruction fInst = (FrameInstruction)activeInstruction();

			if(options.getLineIdx() == 0)
				fInst.setFrameType(FTYPE_TOOL);
			else
				fInst.setFrameType(FTYPE_USER);

			lastScreen();
			break;      
		case SET_FRAME_INSTR_IDX:
			try {
				int frameIdx = Integer.parseInt(workingText) - 1;

				if(frameIdx >= -1 && frameIdx < Fields.FRAME_SIZE){
					fInst = (FrameInstruction)activeInstruction();
					fInst.setReg(frameIdx);
				}
			}
			catch (NumberFormatException NFEx){ /* Ignore invalid input */ }

			lastScreen();
			break;

			//Register statement edit
		case SET_REG_EXPR_TYPE:
			RegisterStatement regStmt = (RegisterStatement)activeInstruction();
			display_stack.pop();

			if(options.getLineIdx() == 0) {
				regStmt.setRegister(new DataRegister());
			} else if(options.getLineIdx() == 1) {
				regStmt.setRegister(new IORegister());
			} else if(options.getLineIdx() == 2) {
				regStmt.setRegister(new PositionRegister());
			} else {
				regStmt.setRegister(new PositionRegister(), 0);
				display_stack.push(ScreenMode.SET_REG_EXPR_IDX2);
			}

			nextScreen(ScreenMode.SET_REG_EXPR_IDX1);
			break;
		case SET_REG_EXPR_IDX1:
			try {
				int idx = Integer.parseInt(workingText);
				regStmt = (RegisterStatement)activeInstruction();
				Register reg = regStmt.getReg();

				if (idx < 1 || ((reg instanceof DataRegister || reg instanceof PositionRegister) && idx > 100)
							|| (reg instanceof IORegister && idx > 5)) {
					// Index is out of bounds
					println("Invalid register index!");
					
				} else {
					
					if(regStmt.getReg() instanceof DataRegister) {
						regStmt.setRegister(armModel.getDReg(idx - 1));
						
					} else if(regStmt.getReg() instanceof IORegister) {
						regStmt.setRegister(armModel.getIOReg(idx - 1));
						
					} else if(regStmt.getReg() instanceof PositionRegister) {
						if (regStmt.getPosIdx() < 0) {
							// Update a position register operand
							regStmt.setRegister(armModel.getPReg(idx - 1));
							
						} else {
							// Update a position register index operand
							regStmt.setRegister(armModel.getPReg(idx - 1), regStmt.getPosIdx());
						}
						
					}
				}
			}
			catch (NumberFormatException NFEx){ /* Ignore invalid input */ }

			lastScreen();
			break;
		case SET_REG_EXPR_IDX2:
			try {
				int idx = Integer.parseInt(workingText);

				if (idx < 1 || idx > 6) {
					println("Invalid position index!"); 
				} else {
					regStmt = (RegisterStatement)activeInstruction();
					if(regStmt.getReg() instanceof PositionRegister) {
						regStmt.setPosIdx(idx - 1);
					}
				}
			} catch (NumberFormatException NFEx){ /* Ignore invalid input */ }

			lastScreen();
			break;

			//Jump/ Label instruction edit
		case SET_LBL_NUM:
			try {
				int idx = Integer.parseInt(workingText);

				if (idx < 0 || idx > 99) {
					println("Invalid label index!");
				} else {
					((LabelInstruction)activeInstruction()).setLabelNum(idx);
				}
			}
			catch (NumberFormatException NFEx){ /* Ignore invalid input */ }

			lastScreen();
			break;
		case SET_JUMP_TGT:
			try {
				int lblNum = Integer.parseInt(workingText);
				int lblIdx = p.findLabelIdx(lblNum);

				if(activeInstruction() instanceof IfStatement) {
					IfStatement ifStmt = (IfStatement)activeInstruction();
					((JumpInstruction)ifStmt.getInstr()).setTgtLblNum(lblNum);
				} 
				else if(activeInstruction() instanceof SelectStatement) {
					SelectStatement sStmt = (SelectStatement)activeInstruction();
					((JumpInstruction)sStmt.getInstrs().get(editIdx)).setTgtLblNum(lblNum);
				}
				else {
					if(lblIdx != -1) {
						JumpInstruction jmp = (JumpInstruction)activeInstruction();
						jmp.setTgtLblNum(lblNum);
					} else {
						err = "Invalid label number.";
					}
				}
			}
			catch (NumberFormatException NFEx){ /* Ignore invalid input */ }

			lastScreen();
			break;

			//Call instruction edit
		case SET_CALL_PROG:
			if(activeInstruction() instanceof IfStatement) {
				IfStatement ifStmt = (IfStatement)activeInstruction();
				((CallInstruction)ifStmt.getInstr()).setProgIdx(options.getLineIdx());
			}
			else if(activeInstruction() instanceof SelectStatement) {
				SelectStatement sStmt = (SelectStatement)activeInstruction();
				CallInstruction c = (CallInstruction)sStmt.getInstrs().get(editIdx);
				c.setProgIdx(contents.getLineIdx());
			}
			else {
				CallInstruction call = (CallInstruction)activeInstruction();
				call.setProgIdx(contents.getLineIdx());
			}

			lastScreen();
			break;

			//Macro edit screens
		case SET_MACRO_PROG:
			if(edit_macro == null) {
				edit_macro = new Macro(contents.getLineIdx());
				macros.add(edit_macro);
				switchScreen(ScreenMode.SET_MACRO_TYPE);
			} else {
				edit_macro.setProgram(contents.getLineIdx());
			}
			break;
		case SET_MACRO_TYPE:
			if(options.getLineIdx() == 0) {
				edit_macro.setManual(false);
				switchScreen(ScreenMode.SET_MACRO_BINDING);
			} else if(options.getLineIdx() == 1) {
				edit_macro.setManual(true);
				edit_macro.clearNum();
				lastScreen();
			}
			break;
		case SET_MACRO_BINDING:
			edit_macro.setNum(options.getLineIdx());
			lastScreen();
			break;

		case NAV_MF_MACROS:
			int macro_idx = contents.get(active_index).getItemIdx();
			macros.get(macro_idx).execute();
			break;

			//Program instruction editing and navigation
		case SELECT_CUT_COPY:
		case SELECT_INSTR_DELETE:
			contents.toggleSelect(getActive_instr());
			updateScreen();
			break;
		case SELECT_PASTE_OPT:         
			if(options.getLineIdx() == 0) {
				pasteInstructions(Fields.CLEAR_POSITION);
			} else if(options.getLineIdx() == 1) {
				pasteInstructions(Fields.PASTE_DEFAULT);
			} else if(options.getLineIdx() == 2) {
				pasteInstructions(Fields.NEW_POSITION);
			} else if(options.getLineIdx() == 3){
				pasteInstructions(Fields.PASTE_REVERSE | Fields.CLEAR_POSITION);
			} else if(options.getLineIdx() == 4) {
				pasteInstructions(Fields.PASTE_REVERSE);
			} else if(options.getLineIdx() == 5) {
				pasteInstructions(Fields.PASTE_REVERSE | Fields.NEW_POSITION );
			} else if(options.getLineIdx() == 6) {
				pasteInstructions(Fields.PASTE_REVERSE | Fields.REVERSE_MOTION);
			} else {
				pasteInstructions(Fields.PASTE_REVERSE | Fields.NEW_POSITION | Fields.REVERSE_MOTION);
			}

			display_stack.pop();
			lastScreen();
			break;
		case SELECT_COMMENT:
			activeInstruction().toggleCommented();

			updateScreen(); 
			break;
		case VIEW_INST_REG:
			displayPoint = null;
			lastScreen();
			break;
		case FIND_REPL:
			lastScreen();  
			break;
		case JUMP_TO_LINE:
			int jumpToInst = Integer.parseInt(workingText) - 1;
			setActive_instr(max(0, min(jumpToInst, activeProgram().getInstructions().size() - 1)));

			lastScreen();
			break;
		case SWAP_PT_TYPE:
			if(options.getLineIdx() == 0) {
				// View Cartesian values
				nextScreen(ScreenMode.NAV_PREGS_C);
			} else if(options.getLineIdx() == 1) {
				// View Joint values
				nextScreen(ScreenMode.NAV_PREGS_J);
			}
			break;

			//Register navigation/ edit
		case NAV_DATA:
			if(options.getLineIdx() == 0) {
				// Data Register Menu
				nextScreen(ScreenMode.NAV_DREGS);
			} else if(options.getLineIdx() == 1) {
				// Position Register Menu
				nextScreen(ScreenMode.NAV_PREGS_C);
			}
			break;
		case CP_DREG_COM:
			int regIdx = -1;

			try {
				// Copy the comment of the curent Data register to the Data register at the specified index
				regIdx = Integer.parseInt(workingText) - 1;
				armModel.getDReg(regIdx).comment = armModel.getDReg(active_index).comment;
				DataManagement.saveState(this);
			} catch (NumberFormatException MFEx) {
				println("Only real numbers are valid!");
			} catch (IndexOutOfBoundsException IOOBEx) {
				println("Only positve integers between 1 and 100 are valid!");
			}

			lastScreen();
			break;
		case CP_DREG_VAL:
			regIdx = -1;

			try {
				// Copy the value of the curent Data register to the Data register at the specified index
				regIdx = Integer.parseInt(workingText) - 1;
				armModel.getDReg(regIdx).value = armModel.getDReg(active_index).value;
				DataManagement.saveState(this);
			} catch (NumberFormatException MFEx) {
				println("Only real numbers are valid!");
			} catch (IndexOutOfBoundsException IOOBEx) {
				println("Only positve integers between 1 and 100 are valid!");
			}

			lastScreen();
			break;
		case CP_PREG_COM:
			regIdx = -1;

			try {
				// Copy the comment of the curent Position register to the Position register at the specified index
				regIdx = Integer.parseInt(workingText) - 1;
				armModel.getPReg(regIdx).comment = armModel.getPReg(active_index).comment;
				DataManagement.saveState(this);
			} catch (NumberFormatException MFEx) {
				println("Only real numbers are valid!");
			} catch (IndexOutOfBoundsException IOOBEx) {
				println("Only positve integers between 1 and 100 are valid!");
			}

			lastScreen();
			break;
		case CP_PREG_PT:
			regIdx = -1;

			try {
				// Copy the point of the curent Position register to the Position register at the specified index
				regIdx = Integer.parseInt(workingText) - 1;
				armModel.getPReg(regIdx).point = armModel.getPReg(active_index).point.clone();
				DataManagement.saveState(this);
			} catch (NumberFormatException MFEx) {
				println("Only real numbers are valid!");
			} catch (IndexOutOfBoundsException IOOBEx) {
				println("Only positve integers between 1 and 100 are valid!");
			}

			lastScreen();
			break;
		case EDIT_DREG_VAL:   
			Float f = null;

			try {
				// Read inputed Float value
				f = Float.parseFloat(workingText);
				// Clamp the value between -9999 and 9999, inclusive
				f = max(-9999f, min(f, 9999f));
				System.out.printf("Index; %d\n", active_index);
				DataRegister dReg = armModel.getDReg(active_index);
				
				if (dReg != null) {
					// Save inputed value
					dReg.value = f;
					DataManagement.saveState(this);
				}
			} catch (NumberFormatException NFEx) {
				// Invalid input value
				println("Value must be a real number!");
			}

			lastScreen();
			break;
		case NAV_DREGS:
			if(contents.getColumnIdx() == 0) {
				// Edit register comment
				nextScreen(ScreenMode.EDIT_DREG_COM);
			} else if(contents.getColumnIdx() >= 1) {
				// Edit Data Register value
				nextScreen(ScreenMode.EDIT_DREG_VAL);
			}
			break;
		case NAV_PREGS_J:
		case NAV_PREGS_C:   
			if(contents.getColumnIdx() == 0) {
				// Edit register comment
				nextScreen(ScreenMode.EDIT_PREG_COM);
			} else if(contents.getColumnIdx() >= 1) {
				// Edit Position Register value
				nextScreen((mode == (ScreenMode.NAV_PREGS_C)) ? ScreenMode.EDIT_PREG_C : ScreenMode.EDIT_PREG_J);
			}
			break;
		case EDIT_PREG_C:
			createRegisterPoint(false);  
			lastScreen();
			break;
		case EDIT_PREG_J:      
			createRegisterPoint(true);
			lastScreen();
			break;
		case EDIT_PREG_COM:
			if (!workingText.equals("\0")) {
				if(workingText.charAt(  workingText.length() - 1  ) == '\0') {
					workingText = workingText.substring(0, workingText.length() - 1);
				}
				// Save the inputed comment to the selected register
				armModel.getPReg(active_index).comment = workingText;
				DataManagement.saveState(this);
				workingText = "";
				lastScreen();
			}
			break;
		case EDIT_DREG_COM:
			if (!workingText.equals("\0")) {
				if(workingText.charAt(  workingText.length() - 1  ) == '\0') {
					workingText = workingText.substring(0, workingText.length() - 1);
				}
				// Save the inputed comment to the selected register\
				armModel.getDReg(active_index).comment = workingText;
				DataManagement.saveState(this);
				workingText = "";
				lastScreen();
			}
			break;
		default:
			break;
		}
	}//End enter

	/**
	 * Move the arm model between two points according to its current speed.
	 * @param model The arm model
	 * @param speedMult Speed multiplier
	 */
	public boolean executeMotion(ArmModel model, float speedMult) {
		motionFrameCounter++;
		// speed is in pixels per frame, multiply that by the current speed setting
		// which is contained in the motion instruction
		float currentSpeed = model.motorSpeed * speedMult;
		if(currentSpeed * motionFrameCounter > distanceBetweenPoints) {
			interMotionIdx++;
			motionFrameCounter = 0;
			if(interMotionIdx >= intermediatePositions.size()) {
				interMotionIdx = -1;
				return true;
			}

			int ret = EXEC_SUCCESS;
			if(intermediatePositions.size() > 0) {
				Point tgtPoint = intermediatePositions.get(interMotionIdx);
				ret = armModel.jumpTo(tgtPoint.position, tgtPoint.orientation);
			}

			if(ret == EXEC_FAILURE) {
				triggerFault();
				return true;
			}
		}

		return false;
	} // end execute linear motion

	/**
	 * Executes a program. Returns true when done.
	 * @param program - Program to execute
	 * @param model   - Arm model to use
	 * @return        - True if done executing, false if otherwise.
	 */
	public boolean executeProgram(Program program, ArmModel model, boolean singleInstr) {
		Instruction activeInstr = activeInstruction();
		int nextInstr = getActive_instr() + 1;

		//stop executing if no valid program is selected or we reach the end of the program
		if(motionFault || activeInstr == null) {
			return true;
		}
		else if (!activeInstr.isCommented()){
			if (activeInstr instanceof MotionInstruction) {
				MotionInstruction motInstr = (MotionInstruction)activeInstr;

				//start a new instruction
				if(!isExecutingInstruction()) {
					setExecutingInstruction(setUpInstruction(program, model, motInstr));

					if (!isExecutingInstruction()) {
						// Motion Instruction failed
						nextInstr = -1;
					}
				}
				//continue current motion instruction
				else {
					if(motInstr.getMotionType() == MTYPE_JOINT) {
						setExecutingInstruction(!(model.interpolateRotation(motInstr.getSpeedForExec(model))));  
					}
					else {  
						setExecutingInstruction(!(executeMotion(model, motInstr.getSpeedForExec(model))));
					}
				}
			} 
			else if (activeInstr instanceof JumpInstruction) {
				setExecutingInstruction(false);
				nextInstr = activeInstr.execute();

			} else if (activeInstr instanceof CallInstruction) {
				setExecutingInstruction(false);
				nextInstr = activeInstr.execute();

			} else {
				setExecutingInstruction(false);

				if(activeInstr.execute() != 0) {
					nextInstr = -1;
				}
			}//end of instruction type check
		} //skip commented instructions

		// Move to next instruction after current is finished
		if(!isExecutingInstruction()) {
			if (nextInstr == -1) {
				// If a command fails
				triggerFault();
				return true;

			}
			else {
				// Move to nextInstruction
				int size = activeProgram().getInstructions().size() + 1;      
				setActive_instr(max(0, min(nextInstr, size - 1)));
				if(display_stack.peek() == ScreenMode.NAV_PROG_INSTR)
					contents.setLineIdx(getInstrLine(getActive_instr()));
			}

			updateScreen();
		}

		return (!isExecutingInstruction() && singleInstr);
	}//end executeProgram

	public void f1() {
		switch(mode) {
		case NAV_PROGRAMS:
			nextScreen(ScreenMode.PROG_CREATE);
			break;
		case NAV_PROG_INSTR:
			if(shift) {
				newMotionInstruction();
				contents.setColumnIdx(0);

				if(getSelectedLine() == 0) {
					contents.setLineIdx(contents.getLineIdx() + 1);
					updateScreen();
					if(getSelectedLine() == 0) {
						setActive_instr(getActive_instr() + 1);
					}
				}
			}
			break;
		case NAV_TOOL_FRAMES:
			if(shift) {
				// Reset the highlighted frame in the tool frame list
				armModel.getToolFrame(active_index).reset();
				DataManagement.saveState(this);
				updateScreen();
			} else {
				// Set the current tool frame
				armModel.setActiveToolFrame(active_index);
				updateCoordFrame();
			}
			break;
		case NAV_USER_FRAMES:
			if(shift) {
				// Reset the highlighted frame in the user frames list
				armModel.getUserFrame(active_index).reset();
				DataManagement.saveState(this);
				updateScreen();
			} else {
				// Set the current user frame
				armModel.setActiveUserFrame(active_index);
				updateCoordFrame();
			}
			break;
		case ACTIVE_FRAMES:
			if(contents.getLineIdx() == 0) {
				nextScreen(ScreenMode.NAV_TOOL_FRAMES);
			} else if(contents.getLineIdx() == 1) {
				nextScreen(ScreenMode.NAV_USER_FRAMES);
			}
			break;
		case NAV_MACROS:
			edit_macro = null;
			nextScreen(ScreenMode.SET_MACRO_PROG);
			break;
		case NAV_DREGS:
			// Clear Data Register entry
			DataRegister dReg = armModel.getDReg(active_index);
			
			if (dReg != null) {
				dReg.comment = null;
				dReg.value = null;
			}
			
			DataManagement.saveState(this);
			break;
		case NAV_PREGS_J:
		case NAV_PREGS_C:
			// Clear Position Register entry
			PositionRegister pReg = armModel.getPReg(active_index);
			
			if (pReg != null) {
				pReg.comment = null;
				pReg.point = null;
			}
			
			DataManagement.saveState(this);
			break;
		default:
			if (mode.getType() == ScreenType.TYPE_TEXT_ENTRY) {
				editTextEntry(0);
			}
		}

		updateScreen();
	}

	public void f2() {
		switch(mode) {
		case NAV_PROGRAMS:
			if(armModel.numOfPrograms() > 0) {
				nextScreen(ScreenMode.PROG_RENAME);
			}
			break;
		case NAV_PROG_INSTR:
			nextScreen(ScreenMode.SELECT_INSTR_INSERT);
			break;
		case TFRAME_DETAIL:
			switchScreen(ScreenMode.FRAME_METHOD_TOOL);
			//nextScreen(Screen.TOOL_FRAME_METHODS);
			break;
		case TEACH_3PT_TOOL:
		case TEACH_6PT:
		case DIRECT_ENTRY_TOOL:
			lastScreen();
			break;
		case UFRAME_DETAIL:
			switchScreen(ScreenMode.FRAME_METHOD_USER);
			//nextScreen(Screen.USER_FRAME_METHODS);
			break;
		case TEACH_3PT_USER:
		case TEACH_4PT:
		case DIRECT_ENTRY_USER:
			lastScreen();
			break;
		case NAV_DREGS:
			// Data Register copy menus
			if (contents.getColumnIdx() == 0) {
				nextScreen(ScreenMode.CP_DREG_COM);
			} else if (contents.getColumnIdx() == 1) {
				nextScreen(ScreenMode.CP_DREG_VAL);
			}
			break;
		case NAV_PREGS_J:
		case NAV_PREGS_C:
			// Position Register copy menus
			if (contents.getColumnIdx() == 0) {
				nextScreen(ScreenMode.CP_PREG_COM);
			} else if (contents.getColumnIdx() == 1) {
				nextScreen(ScreenMode.CP_PREG_PT);
			}
			break;
		default:
			if (mode.getType() == ScreenType.TYPE_TEXT_ENTRY) {
				editTextEntry(1);
				updateScreen();
			}
		}
	}

	public void f3() {
		switch(mode){
		case NAV_PROGRAMS:
			if(armModel.numOfPrograms() > 0) {
				nextScreen(ScreenMode.CONFIRM_PROG_DELETE);
			}
			break;
		case NAV_PROG_INSTR:
			int selectIdx = getSelectedIdx();
			if(activeInstruction() instanceof IfStatement) {
				IfStatement stmt = (IfStatement)activeInstruction();

				if(stmt.getExpr() instanceof Expression && selectIdx >= 2) {
					((Expression)stmt.getExpr()).insertElement(selectIdx - 3);
					updateScreen();
					arrow_rt();
				}
			} 
			else if(activeInstruction() instanceof SelectStatement) {
				SelectStatement stmt = (SelectStatement)activeInstruction();

				if(selectIdx >= 3) {
					stmt.addCase();
					updateScreen();
					arrow_dn();
				}
			}
			else if(activeInstruction() instanceof RegisterStatement) {
				RegisterStatement stmt = (RegisterStatement)activeInstruction();
				int rLen = (stmt.getPosIdx() == -1) ? 2 : 3;

				if(selectIdx > rLen) {
					stmt.getExpr().insertElement(selectIdx - (rLen + 2));
					updateScreen();
					arrow_rt();
				}
			}

			updateScreen();
			break;
		case SELECT_CUT_COPY:
			ArrayList<Instruction> inst = activeProgram().getInstructions();
			clipBoard = new ArrayList<Instruction>();

			int remIdx = 0;
			for(int i = 0; i < inst.size(); i += 1){
				if(contents.isSelected(i)){
					clipBoard.add(inst.get(remIdx));
					inst.remove(remIdx);
				} else{
					remIdx += 1;
				}
			}

			updateInstructions();
			break;
		case NAV_TOOL_FRAMES:
			active_index = 0;
			switchScreen(ScreenMode.NAV_USER_FRAMES);
			break;
		case NAV_USER_FRAMES:
			active_index = 0;
			switchScreen(ScreenMode.NAV_TOOL_FRAMES);
			break;
		case NAV_DREGS:
			// Switch to Position Registers
			nextScreen(ScreenMode.NAV_PREGS_C);
			break;
		case NAV_PREGS_J:
		case NAV_PREGS_C:
			if (shift) {
				switchScreen(ScreenMode.SWAP_PT_TYPE);
			} else {
				// Switch to Data Registers
				nextScreen(ScreenMode.NAV_DREGS);
			}
			break;
		default:
			if (mode.getType() == ScreenType.TYPE_TEXT_ENTRY) {
				editTextEntry(2);
				updateScreen();
			}
		}
	}

	public void f4() {
		Program p = activeProgram();

		switch(mode) {
		case NAV_PROGRAMS:
			if(armModel.numOfPrograms() > 0) {
				nextScreen(ScreenMode.PROG_COPY);
			}
			break;
		case NAV_PROG_INSTR:
			Instruction ins = activeInstruction();

			if (ins != null) {
				int selectIdx = getSelectedIdx();
				getInstrEdit(ins, selectIdx);
			}
			
			break;
		case CONFIRM_INSERT:
			try {
				int lines_to_insert = Integer.parseInt(workingText);
				for(int i = 0; i < lines_to_insert; i += 1)
					p.getInstructions().add(getActive_instr(), new Instruction());

				updateInstructions();
			}
			catch(Exception e){
				e.printStackTrace();
			}

			lastScreen();
			break;
		case CONFIRM_PROG_DELETE:
			int progIdx = getActive_prog();

			if(progIdx >= 0 && progIdx < armModel.numOfPrograms()) {
				armModel.removeProgram(progIdx);

				if(getActive_prog() >= armModel.numOfPrograms()) {
					setActive_prog(armModel.numOfPrograms() - 1);
					contents.setLineIdx(min(getActive_prog(), ITEMS_TO_SHOW - 1));
				}

				lastScreen();
				DataManagement.saveState(this);
			}
			break;
		case SELECT_INSTR_DELETE:
			ArrayList<Instruction> inst = p.getInstructions();

			int remIdx = 0;
			for(int i = 0; i < inst.size(); i += 1){
				if(contents.isSelected(i)){
					inst.remove(remIdx);
				} else{
					remIdx += 1;
				}
			}

			display_stack.pop();
			updateInstructions();
			break;
		case SELECT_CUT_COPY:
			inst = p.getInstructions();
			clipBoard = new ArrayList<Instruction>();

			for(int i = 0; i < inst.size(); i += 1){
				if(contents.isSelected(i))
					clipBoard.add(inst.get(i).clone());
			}

			display_stack.pop();
			updateInstructions();
			break;
		case FIND_REPL:
			int lineIdx = 0;
			String s;

			for(Instruction instruct: p.getInstructions()){
				s = (lineIdx + 1) + ") " + instruct.toString();

				if(s.toUpperCase().contains(workingText.toUpperCase())){
					break;
				}

				lineIdx += 1;
			}

			display_stack.pop();
			setActive_instr(lineIdx);
			updateInstructions();
			break;
		case SELECT_COMMENT:
			display_stack.pop();
			updateInstructions();
			break;
		case CONFIRM_RENUM:
			Point[] pTemp = new Point[1000];
			int posIdx = 0;

			//make a copy of the current positions in p
			for(int i = 0; i < 1000; i += 1){
				pTemp[i] = p.getPosition(i);
			}

			p.clearPositions();

			//rearrange positions
			for(int i = 0; i < p.getInstructions().size(); i += 1) {
				Instruction instr = p.getInstruction(i);
				if(instr instanceof MotionInstruction) {
					int instructPos = ((MotionInstruction)instr).getPositionNum();
					p.setPosition(posIdx, pTemp[instructPos]);
					((MotionInstruction)instr).setPositionNum(posIdx);
					posIdx += 1;
				}
			}

			display_stack.pop();
			updateInstructions();
			break;
		case NAV_MACROS:
			edit_macro = macros.get(contents.getLineIdx());

			if(contents.getColumnIdx() == 1) {
				nextScreen(ScreenMode.SET_MACRO_PROG);
			} else if(contents.getColumnIdx() == 2) {
				nextScreen(ScreenMode.SET_MACRO_TYPE);
			} else {
				if(!macros.get(contents.getLineIdx()).isManual())
					nextScreen(ScreenMode.SET_MACRO_BINDING);
			}
			break;
		case NAV_PREGS_J:
		case NAV_PREGS_C:
			if (shift && !isProgramRunning()) {
				// Stop any prior jogging motion
				armModel.halt();

				// Move To function
				Point pt = (armModel.getPReg(active_index)).point.clone();

				if (pt != null) {
					// Move the Robot to the select point
					if (mode == ScreenMode.NAV_PREGS_C) {
						Frame active = armModel.getActiveFrame(CoordFrame.USER);

						if (active != null) {
							pt = removeFrame(pt, active.getOrigin(), active.getOrientation());
							if (DISPLAY_TEST_OUTPUT) {
								System.out.printf("pt: %s\n", pt.position.toString());
							}
						}

						armModel.moveTo(pt.position, pt.orientation);
					} else {
						armModel.moveTo(pt.angles);
					}
				} else {
					println("Position register is uninitialized!");
				}
			}

			break;
		default:
			if (mode.getType() == ScreenType.TYPE_TEACH_POINTS) {

				if (shift && teachFrame != null) {
					Point tgt = teachFrame.getPoint(options.getLineIdx());

					if (mode == ScreenMode.TEACH_3PT_USER || mode == ScreenMode.TEACH_4PT) {
						if (tgt != null && tgt.position != null && tgt.orientation != null) {
							// Move to the point's position and orientation
							armModel.moveTo(tgt.position, tgt.orientation);
						}
					}
					else {
						if (tgt != null && tgt.angles != null) {
							// Move to the point's joint angles
							armModel.moveTo(tgt.angles);
						}
					}
				}
			}
			else if (mode.getType() == ScreenType.TYPE_TEXT_ENTRY) {
				editTextEntry(3);
			}

		}

		updateScreen();
	}

	public void f5() {
		switch(mode) {
		case NAV_PROG_INSTR:
			Instruction i = activeInstruction();
			int selectLine = getSelectedLine();
			int selectIdx = getSelectedIdx();

			if(selectIdx == 0) {
				nextScreen(ScreenMode.NAV_INSTR_MENU);
			}
			else if(i instanceof MotionInstruction) {
				if(selectIdx == 3 || (contents.getColumnIdx() == 0 && selectLine == 1)) {
					nextScreen(ScreenMode.VIEW_INST_REG); 
				}
			}
			else if(i instanceof IfStatement) {
				IfStatement stmt = (IfStatement)i;
				if(stmt.getExpr() instanceof Expression) {
					((Expression)stmt.getExpr()).removeElement(selectIdx - 3);
				}
			}
			else if(i instanceof SelectStatement) {
				SelectStatement stmt = (SelectStatement)i;
				if(selectIdx >= 3) {
					stmt.deleteCase((selectIdx - 3)/3);
				}
			}
			else if(i instanceof RegisterStatement) {
				RegisterStatement stmt = (RegisterStatement)i;
				int rLen = (stmt.getPosIdx() == -1) ? 2 : 3;
				if(selectIdx > (rLen + 1) && selectIdx < stmt.getExpr().getLength() + rLen) {
					stmt.getExpr().removeElement(selectIdx - (rLen + 2));
				}
			}
			break;
		case VIEW_INST_REG:
			MotionInstruction m = (MotionInstruction)activeInstruction();

			if(getSelectedIdx() == 3) {
				m.toggleOffsetActive();
			} else {
				m.getSecondaryPoint().toggleOffsetActive();
			}

			switchScreen(ScreenMode.SET_MV_INSTR_OFFSET);
			break;      
		case TEACH_3PT_USER:
		case TEACH_3PT_TOOL:
		case TEACH_4PT:
		case TEACH_6PT:
			if (shift) {
				// Save the Robot's current position and joint angles
				Point pt;

				if (mode == ScreenMode.TEACH_3PT_USER || mode == ScreenMode.TEACH_4PT) {
					pt = nativeRobotEEPoint(armModel.getJointAngles());
				} else {
					pt = nativeRobotPoint(armModel.getJointAngles());
				}

				teachFrame.setPoint(pt, options.getLineIdx());
				DataManagement.saveState(this);
				updateScreen();
			}
			break;
		case CONFIRM_PROG_DELETE:
			options.reset();
			lastScreen();
			break;
		case SELECT_INSTR_DELETE:
		case CONFIRM_INSERT:
		case CONFIRM_RENUM:
		case FIND_REPL:
		case SELECT_CUT_COPY:
			display_stack.pop();
			updateInstructions();
			break;
		case NAV_PREGS_J:
		case NAV_PREGS_C:
			PositionRegister pReg = armModel.getPReg(active_index);
			
			if (shift && pReg != null) {
				// Save the Robot's current position and joint angles
				Point curRP = nativeRobotEEPoint(armModel.getJointAngles());
				Frame active = armModel.getActiveFrame(CoordFrame.USER);

				if (active != null) {
					// Save Cartesian values in terms of the active User frame
					curRP = applyFrame(curRP, active.getOrigin(), active.getOrientation());
				} 

				pReg.point = curRP;
				pReg.isCartesian = (mode == ScreenMode.NAV_PREGS_C);
				DataManagement.saveState(this);
			}
			break;
		default:
			if (mode.getType() == ScreenType.TYPE_TEXT_ENTRY) {
				editTextEntry(4);
			}
		}

		updateScreen();
	}


	public void fd() {  
		if(mode == ScreenMode.NAV_PROG_INSTR && !isProgramRunning() && shift) {
			// Stop any prior Robot movement
			armModel.halt();
			// Safeguard against editing a program while it is running
			contents.setColumnIdx(0);

			setExecutingInstruction(false);
			// Run single instruction when step is set
			execSingleInst = isStep();

			setProgramRunning(true);
		}
	}

	public void FrontView() {
		// Default view
		camera.reset();
	}

	public int getActive_instr() {
		return active_instr;
	}

	public int getActive_prog() {
		return active_prog;
	}

	public ArmModel getArmModel() {
		return armModel;
	}

	public Stack<int[]> getCall_stack() {
		return call_stack;
	}

	public Camera getCamera() {
		return camera;
	}

	//Main display content text
	public void getContents(ScreenMode mode) {
		switch(mode) {
		//Program list navigation/ edit
		case NAV_PROGRAMS:
		case SET_CALL_PROG:
		case SET_MACRO_PROG:
			contents.setContents(loadPrograms());
			break;

		case PROG_CREATE:
		case PROG_RENAME:
		case PROG_COPY:
			contents.setContents(loadTextInput());
			break;

			//View instructions
		case CONFIRM_INSERT:
		case CONFIRM_RENUM:
		case FIND_REPL:
		case NAV_PROG_INSTR:
		case VIEW_INST_REG:
		case SELECT_INSTR_DELETE:
		case SELECT_COMMENT:
		case SELECT_CUT_COPY:
		case SELECT_PASTE_OPT:
		case SET_MV_INSTR_TYPE:
		case SET_MV_INSTR_REG_TYPE:
		case SET_MV_INSTR_IDX:
		case SET_MV_INSTR_SPD:
		case SET_MV_INSTR_TERM:
		case SET_MV_INSTR_OFFSET:
		case SET_IO_INSTR_STATE:
		case SET_IO_INSTR_IDX:
		case SET_FRM_INSTR_TYPE:
		case SET_FRAME_INSTR_IDX:
		case SET_REG_EXPR_TYPE:
		case SET_REG_EXPR_IDX1:
		case SET_REG_EXPR_IDX2:
		case SET_IF_STMT_ACT:
		case SET_SELECT_STMT_ARG:
		case SET_SELECT_ARGVAL:
		case SET_SELECT_STMT_ACT:
		case SET_EXPR_ARG:
		case SET_BOOL_EXPR_ARG:
		case SET_EXPR_OP:
		case INPUT_DREG_IDX:
		case INPUT_IOREG_IDX:
		case INPUT_PREG_IDX1:
		case INPUT_PREG_IDX2:
		case INPUT_CONST:
		case SET_BOOL_CONST:
		case SET_LBL_NUM:
		case SET_JUMP_TGT:
			contents.setContents(loadInstructions(getActive_prog()));
			break;

		case ACTIVE_FRAMES:
			contents.clear();
			/* workingText corresponds to the active row's index display */
			if (this.contents.getLineIdx() == 0) {
				contents.addLine("Tool: ", workingText);
				contents.addLine("User: ", Integer.toString(armModel.getActiveUserFrame() + 1));
			} else {
				contents.addLine("Tool: ", Integer.toString(armModel.getActiveToolFrame() + 1));
				contents.addLine("User: ", workingText);
			}
			break;

			//View frame details
		case NAV_TOOL_FRAMES:
			contents.setContents(loadFrames(CoordFrame.TOOL));
			break;
		case NAV_USER_FRAMES:
			contents.setContents(loadFrames(CoordFrame.USER));
			break;
			//View frame details
		case TFRAME_DETAIL:
		case TEACH_3PT_TOOL:
		case TEACH_6PT:
			contents.setContents(loadFrameDetail(CoordFrame.TOOL));
			break;
		case UFRAME_DETAIL:
		case TEACH_3PT_USER:
		case TEACH_4PT:
			contents.setContents(loadFrameDetail(CoordFrame.USER));
			break;
		case FRAME_METHOD_TOOL:
		case FRAME_METHOD_USER:
		case DIRECT_ENTRY_USER:
		case DIRECT_ENTRY_TOOL:
		case EDIT_PREG_C:
		case EDIT_PREG_J:
		case EDIT_DREG_VAL:
		case CP_DREG_COM:
		case CP_DREG_VAL:
		case CP_PREG_COM:
		case CP_PREG_PT:
		case SWAP_PT_TYPE:
			break;

			//View/ edit macros
		case NAV_MACROS:
		case SET_MACRO_TYPE:
		case SET_MACRO_BINDING:
			contents.setContents(loadMacros());
			break;

		case NAV_MF_MACROS:
			contents.setContents(loadManualFunct());
			break;

			//View/ edit registers
		case NAV_DREGS:
		case NAV_PREGS_C:
		case NAV_PREGS_J:
			contents.setContents(loadRegisters());
			break;
		case EDIT_DREG_COM:
		case EDIT_PREG_COM:
			contents.setContents(loadTextInput());
			break;

		default:
			contents.clear();
			break;
		}
	}

	public MenuScroll getContentsMenu() {
		return contents;
	}

	/* This method transforms the given coordinates into a vector
	 * in the Processing's native coordinate system. */
	public PVector getCoordFromMatrix(float x, float y, float z) {
		PVector vector = new PVector();

		vector.x = modelX(x, y, z);
		vector.y = modelY(x, y, z);
		vector.z = modelZ(x, y, z);

		return vector;
	}

	public ControlP5 getCp5() {
		return cp5;
	}

	//Function label text
	public String[] getFunctionLabels(ScreenMode mode){
		String[] funct = new String[5];

		switch(mode) {
		case NAV_PROGRAMS:
			// F2, F3
			funct[0] = "[Create]";
			if(armModel.numOfPrograms() > 0) {
				funct[1] = "[Rename]";
				funct[2] = "[Delete]";
				funct[3] = "[Copy]";
				funct[4] = "";
			} else {
				funct[1] = "";
				funct[2] = "";
				funct[3] = "";
				funct[4] = "";
			}
			break;
		case NAV_PROG_INSTR:
			// F1, F4, F5f
			funct[0] = "[New Pt]";
			funct[1] = "[New Ins]";
			funct[2] = "";
			funct[3] = "[Edit]";
			funct[4] = (contents.getColumnIdx() == 0) ? "[Opt]" : "";
			if(activeInstruction() instanceof MotionInstruction) {
				funct[4] = (contents.getColumnIdx() == 3) ? "[Reg]" : funct[4];
			} 
			else if(activeInstruction() instanceof IfStatement) {
				IfStatement stmt = (IfStatement)activeInstruction();
				int selectIdx = getSelectedIdx();

				if(stmt.getExpr() instanceof Expression) {
					if(selectIdx > 1 && selectIdx < stmt.getExpr().getLength() + 1) {
						funct[2] = "[Insert]";
					}
					if(selectIdx > 2 && selectIdx < stmt.getExpr().getLength() + 1) {
						funct[4] = "[Delete]";
					}
				}
			} else if(activeInstruction() instanceof SelectStatement) {
				int selectIdx = getSelectedIdx();

				if(selectIdx >= 3) {
					funct[2] = "[Insert]";
					funct[4] = "[Delete]";
				}
			} else if(activeInstruction() instanceof RegisterStatement) {
				RegisterStatement stmt = (RegisterStatement)activeInstruction();
				int rLen = (stmt.getPosIdx() == -1) ? 2 : 3;
				int selectIdx = getSelectedIdx();

				if(selectIdx > rLen && selectIdx < stmt.getExpr().getLength() + rLen) {
					funct[2] = "[Insert]";
				}
				if(selectIdx > (rLen + 1) && selectIdx < stmt.getExpr().getLength() + rLen) {
					funct[4] = "[Delete]";
				}
			}
			break;
		case VIEW_INST_REG:
			funct[0] = "";
			funct[1] = "";
			funct[2] = "";
			funct[3] = "";
			funct[4] = "[Offset]";
			break;
		case SELECT_COMMENT:
			funct[0] = "";
			funct[1] = "";
			funct[2] = "";
			funct[3] = "[Done]";
			funct[4] = "";
			break;
		case SELECT_CUT_COPY:
			funct[0] = "";
			funct[1] = "";
			funct[2] = "[Cut]";
			funct[3] = "[Copy]";
			funct[4] = "[Cancel]";
			break;
		case NAV_TOOL_FRAMES:
		case NAV_USER_FRAMES:
			// F1, F2, F3
			if(shift) {
				funct[0] = "[Clear]";
				funct[1] = "";
				funct[2] = "[Switch]";
				funct[3] = "";
				funct[4] = "";
			} else {
				funct[0] = "[Set]";
				funct[1] = "";
				funct[2] = "[Switch]";
				funct[3] = "";
				funct[4] = "";
			}
			break;
		case TFRAME_DETAIL:
		case UFRAME_DETAIL:
			// F2
			funct[0] = "";
			funct[1] = "[Method]";
			funct[2] = "";
			funct[3] = "";
			funct[4] = "";
			break;
		case TEACH_3PT_TOOL:
		case TEACH_3PT_USER:
		case TEACH_4PT:
		case TEACH_6PT:
			// F1, F5
			funct[0] = "";
			funct[1] = "[Method]";
			funct[2] = "";
			funct[3] = "[Mov To]";
			funct[4] = "[Record]";
			break;
		case DIRECT_ENTRY_TOOL:
		case DIRECT_ENTRY_USER:
			funct[0] = "";
			funct[1] = "[Method]";
			funct[2] = "";
			funct[3] = "";
			funct[4] = "";
			break;
		case ACTIVE_FRAMES:
			// F1, F2
			funct[0] = "[List]";
			funct[1] = "";
			funct[2] = "";
			funct[3] = "";
			funct[4] = "";
			break;
		case NAV_MACROS:
			funct[0] = "[New]";
			funct[1] = "";
			funct[2] = "";
			funct[3] = "[Edit]";
			funct[4] = "";
			break;
		case NAV_PREGS_C:
		case NAV_PREGS_J:
			// F1 - F5
			if (shift) {
				funct[0] = "[Clear]";
				funct[1] = "[Copy]";
				funct[2] = "[Type]";
				funct[3] = "[Move To]";
				funct[4] = "[Record]";
			} else {
				funct[0] = "[Clear]";
				funct[1] = "[Copy]";
				funct[2] = "[Switch]";
				funct[3] = "[Move To]";
				funct[4] = "[Record]";
			}
			break;
		case NAV_DREGS:
			// F1 - F3
			funct[0] = "[Clear]";
			funct[1] = "[Copy]";
			funct[2] = "[Switch]";
			funct[3] = "";
			funct[4] = "";
			break;
		case CONFIRM_INSERT:
		case CONFIRM_PROG_DELETE:
		case CONFIRM_RENUM:
		case FIND_REPL:
		case SELECT_INSTR_DELETE:
			// F4, F5
			funct[0] = "";
			funct[1] = "";
			funct[2] = "";
			funct[3] = "[Confirm]";
			funct[4] = "[Cancel]";
			break;
		default:
			if (mode.getType() == ScreenType.TYPE_TEXT_ENTRY) {
				if(options.getLineIdx() == 0) {
					// F1 - F5
					funct[0] = "[ABCDEF]";
					funct[1] = "[GHIJKL]";
					funct[2] = "[MNOPQR]";
					funct[3] = "[STUVWX]";
					funct[4] = "[YZ_@*.]";
				} else {
					funct[0] = "[abcdef]";
					funct[1] = "[ghijkl]";
					funct[2] = "[mnopqr]";
					funct[3] = "[stuvwx]";
					funct[4] = "[yz_@*.]";
				}
			} else {
				funct[0] = "";
				funct[1] = "";
				funct[2] = "";
				funct[3] = "";
				funct[4] = "";
			}
			break;
		}

		return funct;
	}

	//Header text
	public String getHeader(ScreenMode mode){
		String header = null;

		switch(mode) {
		case NAV_PROGRAMS:
			header = "PROGRAMS";
			break;
		case PROG_CREATE:
			header = "NAME PROGRAM";
			break;
		case PROG_RENAME:
			header = "RENAME PROGRAM";
			break;
		case PROG_COPY:
			header = "COPY PROGRAM";
			break;
		case CONFIRM_INSERT:
		case CONFIRM_RENUM:
		case NAV_PROG_INSTR:
		case NAV_INSTR_MENU:
		case SET_MV_INSTR_SPD:
		case SET_MV_INSTR_IDX:
		case SET_MV_INSTR_TERM:
		case SET_MV_INSTR_OFFSET:
		case SELECT_INSTR_INSERT:
		case SET_IO_INSTR_STATE:
		case SET_FRM_INSTR_TYPE:
		case SET_FRAME_INSTR_IDX:
		case SET_EXPR_ARG:
		case SET_BOOL_EXPR_ARG:
		case SET_JUMP_TGT:
		case SELECT_CUT_COPY:    
		case SELECT_INSTR_DELETE:
		case VIEW_INST_REG:
			header = activeProgram().getName();
			break;
		case SELECT_IO_INSTR_REG:
			header = "SELECT IO REGISTER";
			break;
		case SELECT_FRAME_INSTR_TYPE:
			header = "SELECT FRAME INSTRUCTION TYPE";
			break;
		case SELECT_COND_STMT:
			header = "INSERT IF/ SELECT STATEMENT";
			break;
		case SELECT_JMP_LBL:
			header = "INSERT JUMP/ LABEL INSTRUCTION";
			break;
		case SET_CALL_PROG:
			header = "SELECT CALL TARGET";
			break;
		case ACTIVE_FRAMES:
			header = "ACTIVE FRAMES";
			break;
		case NAV_TOOL_FRAMES:
			header = "TOOL FRAMES";
			break;
		case NAV_USER_FRAMES:
			header = "USER FRAMES";
			break;
		case TFRAME_DETAIL:
			header = String.format("TOOL FRAME: %d", curFrameIdx + 1);
			break;
		case UFRAME_DETAIL:
			header = String.format("USER FRAME: %d", curFrameIdx + 1);
			break;
		case FRAME_METHOD_TOOL:
			header = String.format("TOOL FRAME: %d", curFrameIdx + 1);
			break;
		case FRAME_METHOD_USER:
			header = String.format("USER FRAME: %d", curFrameIdx + 1);
			break;
		case TEACH_3PT_TOOL:
		case TEACH_3PT_USER:
			header = "THREE POINT METHOD";
			break;
		case TEACH_4PT:
			header = "FOUR POINT METHOD";
			break;
		case TEACH_6PT:
			header = "SIX POINT METHOD";
			break;
		case DIRECT_ENTRY_TOOL:
		case DIRECT_ENTRY_USER:
			header = "DIRECT ENTRY METHOD";
			break;
		case NAV_MACROS:
		case SET_MACRO_TYPE:
		case SET_MACRO_BINDING:
			header = "VIEW/ EDIT MACROS";
			break;
		case NAV_MF_MACROS:
			header = "EXECUTE MANUAL FUNCTION";
			break;
		case SET_MACRO_PROG:
			header = "SELECT MACRO PROGRAM";
			break;
		case NAV_DATA:
			header = "VIEW REGISTERS";
			break;
		case NAV_DREGS:
		case CP_DREG_COM:
		case CP_DREG_VAL:
			header = "REGISTERS";
			break;
		case NAV_PREGS_J:
			header = "POSTION REGISTERS (J)";
			break;
		case NAV_PREGS_C:
			header = "POSTION REGISTERS (C)";
			break;
		case CP_PREG_COM:
		case CP_PREG_PT:
		case SWAP_PT_TYPE:
			header = "POSTION REGISTERS";
			break;
		case EDIT_DREG_VAL:
			header = "REGISTERS";
			break;
		case EDIT_PREG_C:
		case EDIT_PREG_J:
			header = "POSITION REGISTER: ";
			String pRegComm = armModel.getPReg(active_index).comment;
			
			if(mode != ScreenMode.EDIT_DREG_COM && pRegComm != null) {
				// Show comment if it exists
				header += pRegComm;
			} 
			else {
				header += active_index;
			}
			break;
		case EDIT_DREG_COM:
			header = String.format("Enter a name for R[%d]", active_index);
			break;
		case EDIT_PREG_COM:
			header = String.format("Enter a name for PR[%d]", active_index);
			break;
		default:
			break;
		}

		return header;
	}
	
	public void getInstrEdit(Instruction ins, int selectIdx) {
		if(ins instanceof MotionInstruction) {
			if(getSelectedLine() == 0) {
				// edit movement instruction line 1
				switch(contents.getColumnIdx()) {
				case 2: // motion type
					nextScreen(ScreenMode.SET_MV_INSTR_TYPE);
					break;
				case 3: // register type
					nextScreen(ScreenMode.SET_MV_INSTR_REG_TYPE);
					break;
				case 4: // register
					nextScreen(ScreenMode.SET_MV_INSTR_IDX);
					break;
				case 5: // speed
					nextScreen(ScreenMode.SET_MV_INSTR_SPD);
					break;
				case 6: // termination type
					nextScreen(ScreenMode.SET_MV_INSTR_TERM);
					break;
				case 7: // offset register
					nextScreen(ScreenMode.SET_MV_INSTR_OFFSET);
					break;
				}
			} else {
				// edit movement instruciton line 2 (circular only)
				switch(contents.getColumnIdx()) {
				case 0: // register type
					nextScreen(ScreenMode.SET_MV_INSTR_REG_TYPE);
					break;
				case 1: // register
					nextScreen(ScreenMode.SET_MV_INSTR_IDX);
					break;
				case 2: // speed
					nextScreen(ScreenMode.SET_MV_INSTR_SPD);
					break;
				case 3: // termination type
					nextScreen(ScreenMode.SET_MV_INSTR_TERM);
					break;
				case 4: // offset register
					nextScreen(ScreenMode.SET_MV_INSTR_OFFSET);
					break;
				}
			}
		}
		else if(ins instanceof FrameInstruction) {
			switch(selectIdx) {
			case 1:
				nextScreen(ScreenMode.SET_FRM_INSTR_TYPE);
				break;
			case 2:
				nextScreen(ScreenMode.SET_FRAME_INSTR_IDX);
				break;
			}
		}
		else if(ins instanceof IOInstruction) {
			switch(selectIdx) {
			case 1:
				nextScreen(ScreenMode.SET_IO_INSTR_IDX);
				break;
			case 2:
				nextScreen(ScreenMode.SET_IO_INSTR_STATE);
				break;
			}
		}
		else if(ins instanceof LabelInstruction){
			nextScreen(ScreenMode.SET_LBL_NUM);
		}
		else if(ins instanceof JumpInstruction){
			nextScreen(ScreenMode.SET_JUMP_TGT);
		}
		else if(ins instanceof CallInstruction){
			nextScreen(ScreenMode.SET_CALL_PROG);
		} 
		else if(ins instanceof IfStatement){
			IfStatement stmt = (IfStatement)ins;

			if(stmt.getExpr() instanceof Expression) {
				int len = stmt.getExpr().getLength();

				if(selectIdx >= 3 && selectIdx < len + 1) {
					editExpression((Expression)stmt.getExpr(), selectIdx - 3);
				} else if(selectIdx == len + 2) {
					nextScreen(ScreenMode.SET_IF_STMT_ACT);
				} else if(selectIdx == len + 3) {
					if(stmt.getInstr() instanceof JumpInstruction) {
						nextScreen(ScreenMode.SET_JUMP_TGT);
					} else {
						nextScreen(ScreenMode.SET_CALL_PROG);
					}
				}
			} 
			else if(stmt.getExpr() instanceof AtomicExpression) {
				if(selectIdx == 2) {
					opEdit = ((AtomicExpression)stmt.getExpr()).getArg1();
					nextScreen(ScreenMode.SET_BOOL_EXPR_ARG);
				} else if(selectIdx == 3) {
					opEdit = stmt.getExpr();
					nextScreen(ScreenMode.SET_EXPR_OP);
				} else if(selectIdx == 4) {
					opEdit = ((AtomicExpression)stmt.getExpr()).getArg2();
					nextScreen(ScreenMode.SET_BOOL_EXPR_ARG);
				} else if(selectIdx == 5) {
					nextScreen(ScreenMode.SET_IF_STMT_ACT);
				} else {
					if(stmt.getInstr() instanceof JumpInstruction) {
						nextScreen(ScreenMode.SET_JUMP_TGT);
					} else {
						nextScreen(ScreenMode.SET_CALL_PROG);
					}
				}
			}
		} 
		else if(ins instanceof SelectStatement) {
			SelectStatement stmt = (SelectStatement)ins;

			if(selectIdx == 2) {
				opEdit = stmt.getArg();
				nextScreen(ScreenMode.SET_SELECT_STMT_ARG);
			} else if((selectIdx - 3) % 3 == 0 && selectIdx > 2) {
				opEdit = stmt.getCases().get((selectIdx - 3)/3);
				nextScreen(ScreenMode.SET_SELECT_STMT_ARG);
			} else if((selectIdx - 3) % 3 == 1) {
				editIdx = (selectIdx - 3)/3;
				nextScreen(ScreenMode.SET_SELECT_STMT_ACT);
			} else if((selectIdx - 3) % 3 == 2) {
				editIdx = (selectIdx - 3)/3;
				if(stmt.getInstrs().get(editIdx) instanceof JumpInstruction) {
					nextScreen(ScreenMode.SET_JUMP_TGT);
				} else if(stmt.getInstrs().get(editIdx) instanceof CallInstruction) {
					nextScreen(ScreenMode.SET_CALL_PROG);
				}
			}
		} else if(ins instanceof RegisterStatement) {
			RegisterStatement stmt = (RegisterStatement)ins;
			int len = stmt.getExpr().getLength();
			int rLen = (stmt.getPosIdx() == -1) ? 2 : 3;

			if(selectIdx == 1) {
				nextScreen(ScreenMode.SET_REG_EXPR_TYPE);
			} else if(selectIdx == 2) {
				nextScreen(ScreenMode.SET_REG_EXPR_IDX1);
			} else if(selectIdx == 3 && stmt.getPosIdx() != -1) {
				nextScreen(ScreenMode.SET_REG_EXPR_IDX2);
			} else if(selectIdx >= rLen + 1 && selectIdx <= len + rLen) {
				editExpression(stmt.getExpr(), selectIdx - (rLen + 2));
			}
		}
	}
	
	/**
	 * Returns the first line in the current list of contents that the instruction 
	 * matching the given index appears on.
	 */
	public int getInstrLine(int instrIdx) {
		ArrayList<DisplayLine> instr = loadInstructions(getActive_prog());
		int row = instrIdx;

		while(instr.get(row).getItemIdx() != instrIdx) {
			row += 1;
			if(contents.getLineIdx() >= contents.size() - 1) break;
		}

		return row;
	}

	public WindowManager getManager() {
		return manager;
	}

	//Options menu text
	public void getOptions(ScreenMode mode) {
		options.clear();
		
		switch(mode) {
		//Main menu and submenus
		case NAV_MAIN_MENU:
			options.addLine("1 Frames"           );
			options.addLine("2 Macros"           );
			options.addLine("3 Manual Fncts"     );
			break;

		case CONFIRM_PROG_DELETE:
			options.addLine("Delete selected program?");
			break;

			//Instruction options
		case NAV_INSTR_MENU:
			options.addLine("1 Insert"           );
			options.addLine("2 Delete"           );
			options.addLine("3 Cut/ Copy"        );
			options.addLine("4 Paste"            );
			options.addLine("5 Find/ Replace"    );
			options.addLine("6 Renumber"         );
			options.addLine("7 Comment"          );
			options.addLine("8 Remark"           );
			break;
		case CONFIRM_INSERT:
			options.addLine("Enter number of lines to insert:");
			options.addLine("\0" + workingText);
			break;
		case SELECT_INSTR_DELETE:
			options.addLine("Select lines to delete (ENTER).");
			break;
		case SELECT_CUT_COPY:
			options.addLine("Select lines to cut/ copy (ENTER).");
			break;
		case SELECT_PASTE_OPT:
			options.addLine("1 Logic");
			options.addLine("2 Position");
			options.addLine("3 Pos ID");
			options.addLine("4 R Logic");
			options.addLine("5 R Position");
			options.addLine("6 R Pos ID");
			options.addLine("7 RM Pos ID");
			break;
		case FIND_REPL:
			options.addLine("Enter text to search for:");
			options.addLine("\0" + workingText);
			break;
		case CONFIRM_RENUM: 
			options.addLine("Renumber program positions?");
			break;
		case SELECT_COMMENT:
			options.addLine("Select lines to comment/uncomment.");
			break;

			//Instruction edit options
		case SET_MV_INSTR_TYPE:
		case SET_MV_INSTR_REG_TYPE:
		case SET_MV_INSTR_IDX:
		case SET_MV_INSTR_SPD:
		case SET_MV_INSTR_TERM:
		case SET_MV_INSTR_OFFSET:
		case SET_IO_INSTR_STATE:
		case SET_IO_INSTR_IDX:
		case SET_FRM_INSTR_TYPE:
		case SET_FRAME_INSTR_IDX:
		case SET_REG_EXPR_TYPE:
		case SET_REG_EXPR_IDX1:
		case SET_REG_EXPR_IDX2:
		case SET_IF_STMT_ACT:
		case SET_SELECT_STMT_ARG:
		case SET_SELECT_ARGVAL:
		case SET_SELECT_STMT_ACT:
		case SET_EXPR_ARG:
		case SET_BOOL_EXPR_ARG:
		case SET_EXPR_OP:
		case INPUT_DREG_IDX:
		case INPUT_IOREG_IDX:
		case INPUT_PREG_IDX1:
		case INPUT_PREG_IDX2:
		case INPUT_CONST:
		case SET_BOOL_CONST:
		case SET_LBL_NUM:
		case SET_JUMP_TGT:
			options.setContents(loadInstrEdit(mode));
			break;

			//Insert instructions (non-movemet)
		case SELECT_INSTR_INSERT:
			options.addLine("1. I/O"       );
			options.addLine("2. Frames"    );
			options.addLine("3. Registers" );
			options.addLine("4. IF/SELECT" );
			options.addLine("5. JMP/LBL"   );
			options.addLine("6. CALL"      );
			options.addLine("7. WAIT (NA)"      );
			options.addLine("8. Macro (NA)"     );
			break;
		case SELECT_IO_INSTR_REG:
			options.setContents(loadIORegisters());
			break;
		case SELECT_FRAME_INSTR_TYPE:
			options.addLine("1. TFRAME_NUM = ...");
			options.addLine("2. UFRAME_NUM = ...");
			break;
		case SELECT_REG_STMT:
			options.addLine("1. R[x] = (...)");
			options.addLine("2. IO[x] = (...)");
			options.addLine("3. PR[x] = (...)");
			options.addLine("4. PR[x, y] = (...)");
			break;
		case SELECT_COND_STMT:
			options.addLine("1. IF Stmt");
			options.addLine("2. IF (...)");
			options.addLine("3. SELECT Stmt");
			break;
		case SELECT_JMP_LBL:
			options.addLine("1. LBL[...]");
			options.addLine("2. JMP LBL[...]");
			break;

			//Frame navigation and edit menus
		case SELECT_FRAME_MODE:
			options.addLine("1. Tool Frame");
			options.addLine("2. User Frame");
			break;
		case FRAME_METHOD_TOOL:
			options.addLine("1. Three Point Method");
			options.addLine("2. Six Point Method");
			options.addLine("3. Direct Entry Method");
			break;
		case FRAME_METHOD_USER:
			options.addLine("1. Three Point Method");
			options.addLine("2. Four Point Method");
			options.addLine("3. Direct Entry Method");
			break;
		case VIEW_INST_REG:
			options.setContents(loadInstructionReg());
			break;
		case TEACH_3PT_TOOL:
		case TEACH_3PT_USER:
		case TEACH_4PT:
		case TEACH_6PT:
			options.setContents(loadPointList());
			break;

			//Macro edit menus
		case SET_MACRO_TYPE:
			options.addLine("1. Shift + User Key");
			options.addLine("2. Manual Function");
			break;
		case SET_MACRO_BINDING:
			options.addLine("1. Tool 1");
			options.addLine("2. Tool 2");
			options.addLine("3. MVMU");
			options.addLine("4. Setup");
			options.addLine("5. Status");
			options.addLine("6. POSN");
			options.addLine("7. I/O");
			break;

			//Data navigation and edit menus
		case NAV_DATA:
			options.addLine("1. Data Registers");
			options.addLine("2. Position Registers");
			break;
		case NAV_PREGS_J:
		case NAV_PREGS_C:
			Point pt = armModel.getPReg(active_index).point;
			// Display the point with the Position register of the highlighted line, when viewing the Position registers
			if (pt != null) {
				String[] pregEntry = pt.toLineStringArray(mode == ScreenMode.NAV_PREGS_C);

				for (String line : pregEntry) {
					options.addLine(line);
				}
			}

			break;
		case CP_DREG_COM:
			options.addLine(String.format("Move R[%d]'s comment to:", active_index + 1));
			options.addLine(String.format("R[%s]", workingText));
			break;
		case CP_DREG_VAL:
			options.addLine(String.format("Move R[%d]'s value to:", active_index + 1));
			options.addLine(String.format("R[%s]", workingText));
			break;
		case CP_PREG_COM:
			options.addLine(String.format("Move PR[%d]'s comment to:", active_index + 1));
			options.addLine(String.format("PR[%s]", workingText));
			break;
		case CP_PREG_PT:
			options.addLine(String.format("Move PR[%d]'s point to:", active_index + 1));
			options.addLine(String.format("PR[%s]", workingText));
			break;
		case SWAP_PT_TYPE:
			options.addLine("1. Cartesian");
			options.addLine("2. Joint");
			break;
		case EDIT_DREG_VAL:
			options.addLine("Input register value:");
			options.addLine("\0" + workingText);
			break;
		case EDIT_RSTMT:
			options.addLine("Register");
			options.addLine("Position Register Point");
			options.addLine("Position Register Value");
			break;

			//Misc functions
		case JUMP_TO_LINE:
			options.addLine("Use number keys to enter line number to jump to");
			options.addLine("\0" + workingText);
			break;
		default:
			if (mode.getType() == ScreenType.TYPE_TEXT_ENTRY) {
				options.addLine("1. Uppercase");
				options.addLine("1. Lowercase");
			}
		}
	}

	public MenuScroll getOptionsMenu() {
		return options;
	}

	public int getRecord() {
		return record;
	}

	/**
	 * Returns a 3x3 rotation matrix of the current transformation
	 * matrix on the stack (in row major order).
	 */
	public float[][] getRotationMatrix() {
		float[][] rMatrix = new float[3][3];
		// Calculate origin point
		PVector origin = getCoordFromMatrix(0f, 0f, 0f),
				// Create axes vectors
				vx = getCoordFromMatrix(1f, 0f, 0f).sub(origin),
				vy = getCoordFromMatrix(0f, 1f, 0f).sub(origin),
				vz = getCoordFromMatrix(0f, 0f, 1f).sub(origin);
		// Save values in a 3x3 rotation matrix
		rMatrix[0][0] = vx.x;
		rMatrix[0][1] = vx.y;
		rMatrix[0][2] = vx.z;
		rMatrix[1][0] = vy.x;
		rMatrix[1][1] = vy.y;
		rMatrix[1][2] = vy.z;
		rMatrix[2][0] = vz.x;
		rMatrix[2][1] = vz.y;
		rMatrix[2][2] = vz.z;

		return rMatrix;
	}

	public int getSelectedIdx() {
		if(mode.getType() == ScreenType.TYPE_LINE_SELECT) return 0;

		int idx = contents.getColumnIdx();
		for(int i = contents.getLineIdx() - 1; i >= 0; i -= 1) {
			if(contents.get(i).getItemIdx() != contents.get(i + 1).getItemIdx()) break;
			idx += contents.get(i).size();
		}

		return idx;
	}

	public int getSelectedLine() {
		int row = 0;
		DisplayLine currRow = contents.get(contents.getLineIdx());
		while(contents.getLineIdx() - row >= 0 && currRow.getItemIdx() == contents.get(contents.getLineIdx() - row).getItemIdx()) {
			row += 1;
		}

		return row - 1;
	}

	public Macro[] getSU_macro_bindings() {
		return SU_macro_bindings;
	}

	/*Text generation methods*/

	/* Returns a 4x4 vector array which reflects the current transform matrix on the top
	 * of the stack (ignores scaling values though) */
	public float[][] getTransformationMatrix() {
		float[][] transform = new float[4][4];

		// Caculate four vectors corresponding to the four columns of the transform matrix
		PVector origin = getCoordFromMatrix(0, 0, 0);
		PVector xAxis = getCoordFromMatrix(1, 0, 0).sub(origin);
		PVector yAxis = getCoordFromMatrix(0, 1, 0).sub(origin);
		PVector zAxis = getCoordFromMatrix(0, 0, 1).sub(origin);

		// Place the values of each vector in the correct cells of the transform  matrix
		transform[0][0] = xAxis.x;
		transform[0][1] = xAxis.y;
		transform[0][2] = xAxis.z;
		transform[0][3] = origin.x;
		transform[1][0] = yAxis.x;
		transform[1][1] = yAxis.y;
		transform[1][2] = yAxis.z;
		transform[1][3] = origin.y;
		transform[2][0] = zAxis.x;
		transform[2][1] = zAxis.y;
		transform[2][2] = zAxis.z;
		transform[2][3] = origin.z;
		transform[3][0] = 0;
		transform[3][1] = 0;
		transform[3][2] = 0;
		transform[3][3] = 1;

		return transform;
	}

	public void gui() {
		g1_px = 0;
		g1_py = (Fields.SMALL_BUTTON - 15) + 1;
		g1_width = 440;
		g1_height = 720;
		display_px = 10;
		display_py = 0;//(Fields.SMALL_BUTTON - 15) + 1;
		display_width = g1_width - 20;
		display_height = 280;

		display_stack.push(ScreenMode.DEFAULT);
		mode = display_stack.peek();

		// group 1: display and function buttons
		g1 = cp5.addGroup("DISPLAY")
				.setPosition(g1_px, g1_py)
				.setBackgroundColor(color(127,127,127,100))
				.setWidth(g1_width)
				.setHeight(g1_height)
				.setBackgroundHeight(g1_height)
				.hideBar();

		cp5.addTextarea("txt")
		.setPosition(display_px, 0)
		.setSize(display_width, display_height)
		.setColorBackground(Fields.UI_LIGHT)
		.moveTo(g1);

		/**********************Top row buttons**********************/

		//calculate how much space each button will be given
		int button_offsetX = Fields.LARGE_BUTTON + 1;
		int button_offsetY = Fields.LARGE_BUTTON + 1;  

		int record_normal_px = WindowManager.lButtonWidth * 5 + Fields.LARGE_BUTTON + 1;
		int record_normal_py = 0;   
		PImage[] record = {loadImage("images/record-35x20.png"), 
				loadImage("images/record-over.png"), 
				loadImage("images/record-on.png")};   
		bt_record_normal = cp5.addButton("record_normal")
				.setPosition(record_normal_px, record_normal_py)
				.setSize(Fields.SMALL_BUTTON, Fields.SMALL_BUTTON)
				.setImages(record)
				.updateSize();     

		int EE_normal_px = record_normal_px + Fields.LARGE_BUTTON + 1;
		int EE_normal_py = 0;   
		PImage[] EE = {loadImage("images/EE_35x20.png"), 
				loadImage("images/EE_over.png"), 
				loadImage("images/EE_down.png")};   
		bt_ee_normal = cp5.addButton("EE")
				.setPosition(EE_normal_px, EE_normal_py)
				.setSize(Fields.SMALL_BUTTON, Fields.SMALL_BUTTON)
				.setImages(EE)
				.updateSize();

		/********************Function Row********************/

		int f1_px = display_px;
		int f1_py = display_py + display_height + 2;
		int f_width = display_width/5 - 1;
		cp5.addButton("f1")
		.setPosition(f1_px, f1_py)
		.setSize(f_width, Fields.LARGE_BUTTON)
		.setCaptionLabel("F1")
		.setColorBackground(Fields.BUTTON_DEFAULT)
		.setColorCaptionLabel(Fields.BUTTON_TEXT)  
		.moveTo(g1);     

		int f2_px = f1_px + f_width + 1;
		int f2_py = f1_py;
		cp5.addButton("f2")
		.setPosition(f2_px, f2_py)
		.setSize(f_width, Fields.LARGE_BUTTON)
		.setCaptionLabel("F2")
		.setColorBackground(Fields.BUTTON_DEFAULT)
		.setColorCaptionLabel(Fields.BUTTON_TEXT)  
		.moveTo(g1);  

		int f3_px = f2_px + f_width + 1;
		int f3_py = f2_py;
		cp5.addButton("f3")
		.setPosition(f3_px, f3_py)
		.setSize(f_width, Fields.LARGE_BUTTON)
		.setCaptionLabel("F3")
		.setColorBackground(Fields.BUTTON_DEFAULT)
		.setColorCaptionLabel(Fields.BUTTON_TEXT)  
		.moveTo(g1);    

		int f4_px = f3_px + f_width + 1;
		int f4_py = f3_py;   
		cp5.addButton("f4")
		.setPosition(f4_px, f4_py)
		.setSize(f_width, Fields.LARGE_BUTTON)
		.setCaptionLabel("F4")
		.setColorBackground(Fields.BUTTON_DEFAULT)
		.setColorCaptionLabel(Fields.BUTTON_TEXT)  
		.moveTo(g1);   

		int f5_px = f4_px + f_width + 1;
		int f5_py = f4_py;   
		cp5.addButton("f5")
		.setPosition(f5_px, f5_py)
		.setSize(f_width, Fields.LARGE_BUTTON)
		.setCaptionLabel("F5")
		.setColorBackground(Fields.BUTTON_DEFAULT)
		.setColorCaptionLabel(Fields.BUTTON_TEXT)  
		.moveTo(g1);

		/**********************Step/Shift Row**********************/

		int st_px = f1_px;
		int st_py = f1_py + button_offsetY + 10;   
		cp5.addButton("st")
		.setPosition(st_px, st_py)
		.setSize(Fields.LARGE_BUTTON, Fields.LARGE_BUTTON)
		.setCaptionLabel("STEP")
		.setColorBackground(Fields.BUTTON_DEFAULT)
		.setColorCaptionLabel(Fields.BUTTON_TEXT)  
		.moveTo(g1);

		int mu_px = st_px + Fields.LARGE_BUTTON + 19;
		int mu_py = st_py;   
		cp5.addButton("mu")
		.setPosition(mu_px, mu_py)
		.setSize(Fields.LARGE_BUTTON, Fields.SMALL_BUTTON)
		.setCaptionLabel("MENU")
		.setColorBackground(Fields.BUTTON_DEFAULT)
		.setColorCaptionLabel(Fields.BUTTON_TEXT)  
		.moveTo(g1);

		int se_px = mu_px + Fields.LARGE_BUTTON + 15;
		int se_py = mu_py;
		cp5.addButton("se")
		.setPosition(se_px, se_py)
		.setSize(Fields.LARGE_BUTTON, Fields.SMALL_BUTTON)
		.setCaptionLabel("SELECT")
		.setColorBackground(Fields.BUTTON_DEFAULT)
		.setColorCaptionLabel(Fields.BUTTON_TEXT)  
		.moveTo(g1);     

		int ed_px = se_px + button_offsetX;
		int ed_py = se_py;   
		cp5.addButton("ed")
		.setPosition(ed_px, ed_py)
		.setSize(Fields.LARGE_BUTTON, Fields.SMALL_BUTTON)
		.setCaptionLabel("EDIT")
		.setColorBackground(Fields.BUTTON_DEFAULT)
		.setColorCaptionLabel(Fields.BUTTON_TEXT)  
		.moveTo(g1);      

		int da_px = ed_px + button_offsetX;
		int da_py = ed_py;   
		cp5.addButton("da")
		.setPosition(da_px, da_py)
		.setSize(Fields.LARGE_BUTTON, Fields.SMALL_BUTTON)
		.setCaptionLabel("DATA")
		.setColorBackground(Fields.BUTTON_DEFAULT)
		.setColorCaptionLabel(Fields.BUTTON_TEXT)  
		.moveTo(g1);

		int fn_px = da_px + Fields.LARGE_BUTTON + 15;
		int fn_py = da_py;   
		cp5.addButton("Fn")
		.setPosition(fn_px, fn_py)
		.setSize(Fields.LARGE_BUTTON, Fields.SMALL_BUTTON)
		.setCaptionLabel("FCTN")
		.setColorBackground(Fields.BUTTON_DEFAULT)
		.setColorCaptionLabel(Fields.BUTTON_TEXT)  
		.moveTo(g1);

		int sf_px = fn_px + Fields.LARGE_BUTTON + 19;
		int sf_py = fn_py;
		cp5.addButton("sf")
		.setPosition(sf_px, sf_py)
		.setSize(Fields.LARGE_BUTTON, Fields.LARGE_BUTTON)
		.setCaptionLabel("SHIFT")
		.setColorBackground(Fields.BUTTON_DEFAULT)
		.setColorCaptionLabel(Fields.BUTTON_TEXT)  
		.moveTo(g1);

		int pr_px = mu_px;
		int pr_py = mu_py + button_offsetY;   
		cp5.addButton("pr")
		.setPosition(pr_px, pr_py + 15)
		.setSize(Fields.LARGE_BUTTON, Fields.SMALL_BUTTON)
		.setCaptionLabel("PREV")
		.setColorBackground(Fields.BUTTON_DEFAULT)
		.setColorCaptionLabel(Fields.BUTTON_TEXT)  
		.moveTo(g1);

		int ne_px = fn_px;
		int ne_py = mu_py + button_offsetY;
		cp5.addButton("ne")
		.setPosition(ne_px, ne_py + 15)
		.setSize(Fields.LARGE_BUTTON, Fields.SMALL_BUTTON)
		.setCaptionLabel("NEXT")
		.setColorBackground(Fields.BUTTON_DEFAULT)
		.setColorCaptionLabel(Fields.BUTTON_TEXT)  
		.moveTo(g1);

		/***********************Arrow Keys***********************/
		button_offsetY = Fields.SMALL_BUTTON + 1;

		PImage[] imgs_arrow_up = {loadImage("images/arrow-up.png"), 
				loadImage("images/arrow-up_over.png"), 
				loadImage("images/arrow-up_down.png")};   
		int up_px = ed_px + 5;
		int up_py = ed_py + button_offsetY + 10;
		cp5.addButton("arrow_up")
		.setPosition(up_px, up_py)
		.setSize(Fields.SMALL_BUTTON, Fields.SMALL_BUTTON)
		.setImages(imgs_arrow_up)
		.updateSize()
		.moveTo(g1);     

		PImage[] imgs_arrow_down = {loadImage("images/arrow-down.png"), 
				loadImage("images/arrow-down_over.png"), 
				loadImage("images/arrow-down_down.png")};   
		int dn_px = up_px;
		int dn_py = up_py + button_offsetY;
		cp5.addButton("arrow_dn")
		.setPosition(dn_px, dn_py)
		.setSize(Fields.SMALL_BUTTON, Fields.SMALL_BUTTON)
		.setImages(imgs_arrow_down)
		.updateSize()
		.moveTo(g1);    

		PImage[] imgs_arrow_l = {loadImage("images/arrow-l.png"), 
				loadImage("images/arrow-l_over.png"), 
				loadImage("images/arrow-l_down.png")};
		int lt_px = dn_px - button_offsetX;
		int lt_py = dn_py - button_offsetY/2;
		cp5.addButton("arrow_lt")
		.setPosition(lt_px, lt_py)
		.setSize(Fields.SMALL_BUTTON, Fields.SMALL_BUTTON)
		.setImages(imgs_arrow_l)
		.updateSize()
		.moveTo(g1);  

		PImage[] imgs_arrow_r = {loadImage("images/arrow-r.png"), 
				loadImage("images/arrow-r_over.png"), 
				loadImage("images/arrow-r_down.png")};
		int rt_px = dn_px + button_offsetX;
		int rt_py = lt_py;
		cp5.addButton("arrow_rt")
		.setPosition(rt_px, rt_py)
		.setSize(Fields.SMALL_BUTTON, Fields.SMALL_BUTTON)
		.setImages(imgs_arrow_r)
		.updateSize()
		.moveTo(g1);      

		//--------------------------------------------------------------//
		//                           Group 2                            //
		//--------------------------------------------------------------//
		int g2_offsetY = display_py + display_height + 4*Fields.LARGE_BUTTON - 10;

		/**********************Numpad Block*********************/

		int LINE_px = ed_px - 7*button_offsetX/2;
		int LINE_py = g2_offsetY + 5*button_offsetY;
		cp5.addButton("LINE")
		.setPosition(LINE_px, LINE_py)
		.setSize(Fields.LARGE_BUTTON, Fields.SMALL_BUTTON)
		.setCaptionLabel("-")
		.setColorBackground(Fields.BUTTON_DEFAULT)
		.setColorCaptionLabel(Fields.BUTTON_TEXT)  
		.moveTo(g1);    

		int PERIOD_px = LINE_px + button_offsetX;
		int PERIOD_py = LINE_py - button_offsetY;
		cp5.addButton("PERIOD")
		.setPosition(PERIOD_px, PERIOD_py)
		.setSize(Fields.LARGE_BUTTON, Fields.SMALL_BUTTON)
		.setCaptionLabel(".")
		.setColorBackground(Fields.BUTTON_DEFAULT)
		.setColorCaptionLabel(Fields.BUTTON_TEXT)  
		.moveTo(g1);   

		int COMMA_px = PERIOD_px + button_offsetX;
		int COMMA_py = PERIOD_py;
		cp5.addButton("COMMA")
		.setPosition(COMMA_px, COMMA_py)
		.setSize(Fields.LARGE_BUTTON, Fields.SMALL_BUTTON)
		.setCaptionLabel(",")
		.setColorBackground(Fields.BUTTON_DEFAULT)
		.setColorCaptionLabel(Fields.BUTTON_TEXT)  
		.moveTo(g1);

		int POSN_px = LINE_px + button_offsetX;
		int POSN_py = LINE_py;
		cp5.addButton("POSN")
		.setPosition(POSN_px, POSN_py)
		.setSize(Fields.LARGE_BUTTON, Fields.SMALL_BUTTON)
		.setCaptionLabel("POSN")
		.setColorBackground(Fields.BUTTON_DEFAULT)
		.setColorCaptionLabel(Fields.BUTTON_TEXT)  
		.moveTo(g1);

		int IO_px = POSN_px + button_offsetX;
		int IO_py = POSN_py;
		cp5.addButton("IO")
		.setPosition(IO_px, IO_py)
		.setSize(Fields.LARGE_BUTTON, Fields.SMALL_BUTTON)
		.setCaptionLabel("I/O")
		.setColorBackground(Fields.BUTTON_DEFAULT)
		.setColorCaptionLabel(Fields.BUTTON_TEXT)  
		.moveTo(g1);

		int NUM_px = LINE_px;
		int NUM_py = LINE_py - button_offsetY;
		for(int i = 0; i < 10; i += 1) {
			cp5.addButton("NUM"+i)
			.setPosition(NUM_px, NUM_py)
			.setSize(Fields.LARGE_BUTTON, Fields.SMALL_BUTTON)
			.setCaptionLabel(""+i)
			.setColorBackground(Fields.BUTTON_DEFAULT)
			.setColorCaptionLabel(Fields.BUTTON_TEXT)
			.moveTo(g1);

			if(i % 3 == 0) {
				NUM_px = LINE_px;
				NUM_py -= button_offsetY;
			}
			else {
				NUM_px += button_offsetX;
			}
		}

		int RESET_px = LINE_px;
		int RESET_py = NUM_py;
		cp5.addButton("RESET")
		.setPosition(RESET_px, RESET_py)
		.setSize(Fields.LARGE_BUTTON, Fields.SMALL_BUTTON)
		.setCaptionLabel("RESET")
		.setColorBackground(Fields.BUTTON_DEFAULT)
		.setColorCaptionLabel(Fields.BUTTON_TEXT)  
		.moveTo(g1);   

		int BKSPC_px = RESET_px + button_offsetX;
		int BKSPC_py = RESET_py;
		cp5.addButton("BKSPC")
		.setPosition(BKSPC_px, BKSPC_py)
		.setSize(Fields.LARGE_BUTTON, Fields.SMALL_BUTTON)
		.setCaptionLabel("BKSPC")
		.setColorBackground(Fields.BUTTON_DEFAULT)
		.setColorCaptionLabel(Fields.BUTTON_TEXT)  
		.moveTo(g1);      

		int ITEM_px = BKSPC_px + button_offsetX;
		int ITEM_py = BKSPC_py;
		cp5.addButton("ITEM")
		.setPosition(ITEM_px, ITEM_py)
		.setSize(Fields.LARGE_BUTTON, Fields.SMALL_BUTTON)
		.setCaptionLabel("ITEM")
		.setColorBackground(Fields.BUTTON_DEFAULT)
		.setColorCaptionLabel(Fields.BUTTON_TEXT)  
		.moveTo(g1);

		/***********************Util Block*************************/

		int ENTER_px = ed_px;
		int ENTER_py = g2_offsetY;
		cp5.addButton("ENTER")
		.setPosition(ENTER_px, ENTER_py)
		.setSize(Fields.LARGE_BUTTON, Fields.SMALL_BUTTON)
		.setCaptionLabel("ENTER")
		.setColorBackground(Fields.BUTTON_DEFAULT)
		.setColorCaptionLabel(Fields.BUTTON_TEXT)  
		.moveTo(g1);    

		int TOOL1_px = ENTER_px;
		int TOOL1_py = ENTER_py + button_offsetY;
		cp5.addButton("TOOL1")
		.setPosition(TOOL1_px, TOOL1_py)
		.setSize(Fields.LARGE_BUTTON, Fields.SMALL_BUTTON)
		.setCaptionLabel("TOOL1")
		.setColorBackground(Fields.BUTTON_DEFAULT)
		.setColorCaptionLabel(Fields.BUTTON_TEXT)  
		.moveTo(g1);   

		int TOOL2_px = TOOL1_px;
		int TOOL2_py = TOOL1_py + button_offsetY;
		cp5.addButton("TOOL2")
		.setPosition(TOOL2_px, TOOL2_py)
		.setSize(Fields.LARGE_BUTTON, Fields.SMALL_BUTTON)
		.setCaptionLabel("TOOL2")
		.setColorBackground(Fields.BUTTON_DEFAULT)
		.setColorCaptionLabel(Fields.BUTTON_TEXT)  
		.moveTo(g1);

		int MOVEMENU_px = TOOL2_px;
		int MOVEMENU_py = TOOL2_py + button_offsetY;
		cp5.addButton("MVMU")
		.setPosition(MOVEMENU_px, MOVEMENU_py)
		.setSize(Fields.LARGE_BUTTON, Fields.SMALL_BUTTON)
		.setCaptionLabel("MVMU")
		.setColorBackground(Fields.BUTTON_DEFAULT)
		.setColorCaptionLabel(Fields.BUTTON_TEXT)  
		.moveTo(g1); 

		int SETUP_px = MOVEMENU_px;
		int SETUP_py = MOVEMENU_py + button_offsetY;
		cp5.addButton("SETUP")
		.setPosition(SETUP_px, SETUP_py)
		.setSize(Fields.LARGE_BUTTON, Fields.SMALL_BUTTON)
		.setCaptionLabel("SETUP")
		.setColorBackground(Fields.BUTTON_DEFAULT)
		.setColorCaptionLabel(Fields.BUTTON_TEXT)  
		.moveTo(g1);    

		int STATUS_px = SETUP_px;
		int STATUS_py = SETUP_py + button_offsetY;
		cp5.addButton("STATUS")
		.setPosition(STATUS_px, STATUS_py)
		.setSize(Fields.LARGE_BUTTON, Fields.SMALL_BUTTON)
		.setCaptionLabel("STATUS")
		.setColorBackground(Fields.BUTTON_DEFAULT)
		.setColorCaptionLabel(Fields.BUTTON_TEXT)  
		.moveTo(g1);

		/********************Joint Control Block*******************/

		int hd_px = STATUS_px + 3*button_offsetX/2;
		int hd_py = g2_offsetY;   
		cp5.addButton("hd")
		.setPosition(hd_px, hd_py)
		.setSize(Fields.LARGE_BUTTON, Fields.SMALL_BUTTON)
		.setCaptionLabel("HOLD")
		.setColorBackground(Fields.BUTTON_DEFAULT)
		.setColorCaptionLabel(Fields.BUTTON_TEXT)  
		.moveTo(g1);    

		int fd_px = hd_px;
		int fd_py = hd_py + button_offsetY;   
		cp5.addButton("fd")
		.setPosition(fd_px, fd_py)
		.setSize(Fields.LARGE_BUTTON, Fields.SMALL_BUTTON)
		.setCaptionLabel("FWD")
		.setColorBackground(Fields.BUTTON_DEFAULT)
		.setColorCaptionLabel(Fields.BUTTON_TEXT)  
		.moveTo(g1);   

		int bd_px = fd_px;
		int bd_py = fd_py + button_offsetY;   
		cp5.addButton("bd")
		.setPosition(bd_px, bd_py)
		.setSize(Fields.LARGE_BUTTON, Fields.SMALL_BUTTON)
		.setCaptionLabel("BWD")
		.setColorBackground(Fields.BUTTON_DEFAULT)
		.setColorCaptionLabel(Fields.BUTTON_TEXT)  
		.moveTo(g1);

		int COORD_px = bd_px;   
		int COORD_py = bd_py + button_offsetY;
		cp5.addButton("COORD")
		.setPosition(COORD_px, COORD_py)
		.setSize(Fields.LARGE_BUTTON, Fields.SMALL_BUTTON)
		.setCaptionLabel("COORD")
		.setColorBackground(Fields.BUTTON_DEFAULT)
		.setColorCaptionLabel(Fields.BUTTON_TEXT)
		.moveTo(g1);

		int SPEEDUP_px = COORD_px;
		int SPEEDUP_py = COORD_py + button_offsetY;
		cp5.addButton("SPEEDUP")
		.setPosition(SPEEDUP_px, SPEEDUP_py)
		.setSize(Fields.LARGE_BUTTON, Fields.SMALL_BUTTON)
		.setCaptionLabel("+%")
		.setColorBackground(Fields.BUTTON_DEFAULT)
		.setColorCaptionLabel(Fields.BUTTON_TEXT)  
		.moveTo(g1);   

		int SLOWDOWN_px = SPEEDUP_px;
		int SLOWDOWN_py = SPEEDUP_py + button_offsetY;
		cp5.addButton("SLOWDOWN")
		.setPosition(SLOWDOWN_px, SLOWDOWN_py)
		.setSize(Fields.LARGE_BUTTON, Fields.SMALL_BUTTON)
		.setCaptionLabel("-%")
		.setColorBackground(Fields.BUTTON_DEFAULT)
		.setColorCaptionLabel(Fields.BUTTON_TEXT)  
		.moveTo(g1);

		int JOINT_px = SLOWDOWN_px + button_offsetX;
		int JOINT_py = g2_offsetY;
		String[] labels = {" -X\n(J1)", " +X\n(J1)",
				" -Y\n(J2)", " +Y\n(J2)",
				" -Z\n(J3)", " +Z\n(J3)",
				"-XR\n(J4)", "+XR\n(J4)",
				"-YR\n(J5)", "+YR\n(J5)",
				"-ZR\n(J6)", "+ZR\n(J6)"};

		for(int i = 1; i <= 6; i += 1) {
			cp5.addButton("JOINT"+i+"_NEG")
			.setPosition(JOINT_px, JOINT_py)
			.setSize(Fields.LARGE_BUTTON, Fields.SMALL_BUTTON)
			.setCaptionLabel(labels[(i-1)*2])
			.setColorBackground(Fields.BUTTON_DEFAULT)
			.setColorCaptionLabel(Fields.BUTTON_TEXT)  
			.moveTo(g1)
			.getCaptionLabel()
			.alignY(TOP);

			JOINT_px += Fields.LARGE_BUTTON + 1; 
			cp5.addButton("JOINT"+i+"_POS")
			.setPosition(JOINT_px, JOINT_py)
			.setSize(Fields.LARGE_BUTTON, Fields.SMALL_BUTTON)
			.setCaptionLabel(labels[(i-1)*2 + 1])
			.setColorBackground(Fields.BUTTON_DEFAULT)
			.setColorCaptionLabel(Fields.BUTTON_TEXT)  
			.moveTo(g1)
			.getCaptionLabel()
			.alignY(TOP);

			JOINT_px = SLOWDOWN_px + button_offsetX;
			JOINT_py += Fields.SMALL_BUTTON + 1;
		}

		List<Button> buttons = cp5.getAll(Button.class);
		for(Button b : buttons) {
			b.getCaptionLabel().setFont(fnt_conB);
		}
	}// End UI setup
	/* Stops all of the Robot's movement */
	public void hd() {
		armModel.halt();
	}

	public void HideObjects() {
		// Toggle object display on or off
		showOOBs = !showOOBs;
		getManager().updateScenarioWindowContentPositions();
	}

	public void IO() {
		if(getSU_macro_bindings()[6] != null && shift) {
			getSU_macro_bindings()[6].execute();
		}
	}

	public boolean isExecutingInstruction() {
		return executingInstruction;
	}

	public boolean isProgramRunning() {
		return programRunning;
	}

	public boolean isStep() {
		return step;
	}

	public void ITEM() {
		if(mode == ScreenMode.NAV_PROG_INSTR) {
			options.reset();
			workingText = "";
			nextScreen(ScreenMode.JUMP_TO_LINE);
		}
	}

	public void JOINT1_NEG() {
		updateRobotJogMotion(0, -1);
	}

	public void JOINT1_POS() {
		updateRobotJogMotion(0, 1);
	}

	public void JOINT2_NEG() {
		updateRobotJogMotion(1, -1);
	}

	public void JOINT2_POS() {
		updateRobotJogMotion(1, 1);
	}

	public void JOINT3_NEG() {
		if (shift) {
			updateRobotJogMotion(2, -1);
		}
	}

	public void JOINT3_POS() {
		updateRobotJogMotion(2, 1);
	}

	public void JOINT4_NEG() {
		updateRobotJogMotion(3, -1);
	}

	public void JOINT4_POS() {
		updateRobotJogMotion(3, 1);
	}

	public void JOINT5_NEG() {
		updateRobotJogMotion(4, -1);
	}

	public void JOINT5_POS() {
		updateRobotJogMotion(4, 1);
	}

	public void JOINT6_NEG() {
		updateRobotJogMotion(5, -1);
	}

	public void JOINT6_POS() {
		updateRobotJogMotion(5, 1);
	}

	public void keyPressed() {

		if (key == 27) {
			key = 0;
		}

		if (getManager() != null && getManager().isATextFieldActive()) {
			// Disable other key events when typing in a text field
			return;
		}

		if(mode.getType() == ScreenType.TYPE_TEXT_ENTRY) {
			// Modify the input name for the new program
			if(key == BACKSPACE && workingText.length() > 0) {

				if(workingText.length() > 1) {
					workingText = workingText.substring(0, workingText.length() - 1);
					contents.setColumnIdx(min(contents.getColumnIdx(), workingText.length() - 1));
				}  else {
					workingText = "\0";
				}

				updateScreen();
			} else if(key == DELETE && workingText.length() > 0) {

				if(workingText.length() > 1) {
					workingText = workingText.substring(1, workingText.length());
					contents.setColumnIdx(min(contents.getColumnIdx(), workingText.length() - 1));
				}  else {
					workingText = "\0";
				}

				updateScreen();
				// Valid characters in a program name or comment
			} else if(workingText.length() < TEXT_ENTRY_LEN && (key >= 'a' && key <= 'z') || (key >= 'A' && key <= 'Z')
					|| (key >= '0' && key <= '9') || key == '.' || key == '@' || key == '*' || key == '_') {
				StringBuilder temp;
				// Insert the typed character
				if (workingText.length() > 0 && workingText.charAt(contents.getColumnIdx()) != '\0') {
					temp = new StringBuilder(workingText.substring(0, contents.getColumnIdx()) + "\0" + workingText.substring(contents.getColumnIdx(), workingText.length()));
				} else {
					temp = new StringBuilder(workingText); 
				}

				temp.setCharAt(contents.getColumnIdx(), key);
				workingText = temp.toString();

				// Add an insert element if the length of the current comment is less than 16
				int len = workingText.length();
				if(len <= TEXT_ENTRY_LEN && contents.getColumnIdx() == workingText.length() - 1 && workingText.charAt(len - 1) != '\0') {
					workingText += '\0';
				}

				contents.setColumnIdx(min(contents.getColumnIdx() + 1, workingText.length() - 1));
				// Update contents to the new string
				updateScreen();
			}

			return;
		} else if (key == 'a') {
			// Cycle through Axes display states
			switch (axesState) {
			case NONE:
				axesState = AxesDisplay.AXES;
				break;
			case AXES:
				axesState = AxesDisplay.GRID;
				break;
			default:
				axesState = AxesDisplay.NONE;
			}

		} else if(key == 'e') {
			// Cycle through EE Mapping states
			switch (mappingState) {
			case NONE:
				mappingState = EEMapping.LINE;
				break;
			case LINE:
				mappingState = EEMapping.DOT;
				break;
			default:
				mappingState = EEMapping.NONE;
			}

		} else if (key == 'f' ) {
			// Display the User and Tool frames associated with the current motion instruction
			if (DISPLAY_TEST_OUTPUT && mode == ScreenMode.NAV_PROG_INSTR && (contents.getColumnIdx() == 3 || contents.getColumnIdx() == 4)) {
				Instruction inst = activeInstruction();

				if (inst instanceof MotionInstruction) {
					MotionInstruction mInst = (MotionInstruction)inst;
					System.out.printf("\nUser frame: %d\nTool frame: %d\n", mInst.getUserFrame(), mInst.getToolFrame());
				}
			}

		} else if (key == 'm') {
			// Print the current mode to the console
			println(mode.toString());

		} else if (key == 'p') {
			// Toggle the Robot's End Effector state
			if (!isProgramRunning()) {
				armModel.toggleEEState();
			}

		} else if (key == 's') {
			// Save EVERYTHING!
			DataManagement.saveState(this);

		} else if (key == 't') {
			// Restore default Robot joint angles
			float[] rot = {0, 0, 0, 0, 0, 0};
			armModel.setJointAngles(rot);
			intermediatePositions.clear();

		} else if(key == 'w') {
			// Write anything stored in the String buffer to a text file
			writeBuffer();

		} else if (key == 'y') {
			// Apply another set of default Robot joint angles
			float[] rot = {PI, 0, 0, 0, 0, PI};
			armModel.setJointAngles(rot);
			intermediatePositions.clear();

		}
	}

	/**
	 * Transitions the display to the previous screen that the user was on.
	 */
	public boolean lastScreen() {
		// Stop a program from executing when transition screens
		setProgramRunning(false);
		if (display_stack.peek() == ScreenMode.DEFAULT) {
			if (DISPLAY_TEST_OUTPUT) { System.out.printf("%s\n", mode); }
			return false;
		}
		else{
			display_stack.pop();
			if (DISPLAY_TEST_OUTPUT) { System.out.printf("%s => %s\n", mode, display_stack.peek()); }
			mode = display_stack.peek();

			loadScreen();
			return true;
		}
	}

	public void LeftView() {
		// Left view
		camera.reset();
		camera.rotate(0, PI / 2f, 0);
	}


	public void LINE() {
		if(mode.getType() == ScreenType.TYPE_POINT_ENTRY) {

			if(contents.getLineIdx() >= 0 && contents.getLineIdx() < contents.size()) {
				String value = contents.get(contents.getLineIdx()).get(1);

				// Mutliply current number by -1
				if(value.length() > 0 && value.charAt(0) == '-') {
					contents.get(contents.getLineIdx()).set(1, value.substring(1, value.length()));
				} else {
					contents.get(contents.getLineIdx()).set(1, "-" + value);
				}
			}

		} else if(mode.getType() == ScreenType.TYPE_NUM_ENTRY) {

			// Mutliply current number by -1
			if(workingText.length() > 0 && workingText.charAt(0) == '-') {
				workingText = workingText.substring(1);
			} else {
				workingText = "-" + workingText;
			}

		}

		updateScreen();
	}

	/**
	 * Transitions to the Frame Details menu, which displays
	 * the x, y, z, w, p, r values associated with the Frame
	 * at curFrameIdx in either the Tool Frames or User Frames,
	 * based on the value of super_mode.
	 */
	public ArrayList<DisplayLine> loadFrameDetail(CoordFrame coordFrame) {
		ArrayList<DisplayLine> details = new ArrayList<DisplayLine>();

		// Display the frame set name as well as the index of the currently selected frame
		if(coordFrame == CoordFrame.TOOL) {
			String[] fields = armModel.getToolFrame(curFrameIdx).toLineStringArray();
			// Place each value in the frame on a separate lien
			for(String field : fields) { details.add(newLine(field)); }

		} else if(coordFrame == CoordFrame.USER) {
			String[] fields = armModel.getUserFrame(curFrameIdx).toLineStringArray();
			// Place each value in the frame on a separate lien
			for(String field : fields) { details.add(newLine(field)); }

		} else {
			return null;
		}

		return details;
	}

	/**
	 * Takes the values associated with the given Frame's direct entry values
	 * (X, Y, Z, W, P, R) and fills a 2D ArrayList, where the first column is
	 * the prefix for the value in the second column.
	 * 
	 * @param f        The frame to be displayed for editing
	 * @returning      A 2D ArrayList with the prefixes and values associated
	 *                 with the Frame
	 */
	public ArrayList<DisplayLine> loadFrameDirectEntry(Frame f) {
		ArrayList<DisplayLine> frame = new ArrayList<DisplayLine>();

		String[][] entries = f.directEntryStringArray();

		for (int line = 0; line < entries.length; ++line) {
			frame.add(newLine(line, entries[line][0], entries[line][1]));
		}

		return frame;
	}

	/**
	 * Loads the set of Frames that correspond to the given coordinate frame.
	 * Only TOOL and USER have Frames sets as of now.
	 * 
	 * @param coorFrame  the integer value representing the coordinate frame
	 *                   of the desired frame set
	 */
	public ArrayList<DisplayLine> loadFrames(CoordFrame coordFrame) {
		ArrayList<DisplayLine> frameDisplay = new ArrayList<DisplayLine>();
		
		if (coordFrame == CoordFrame.TOOL) {
			// Display Tool frames
			for(int idx = 0; idx < Fields.FRAME_SIZE; idx += 1) {
				// Display each frame on its own line
				String[] strArray = armModel.getToolFrame(idx).toLineStringArray();
				frameDisplay.add(newLine(idx, String.format("%-4s %s", String.format("%d)", idx + 1), strArray[0])));
				frameDisplay.add(newLine(idx, String.format("%s", strArray[1])));
				frameDisplay.get(idx*2 + 1).setxAlign(38);
			}
			
		} else {
			// Display User frames
			for(int idx = 0; idx < Fields.FRAME_SIZE; idx += 1) {
				// Display each frame on its own line
				String[] strArray = armModel.getUserFrame(idx).toLineStringArray();
				frameDisplay.add(newLine(idx, String.format("%-4s %s", String.format("%d)", idx + 1), strArray[0])));
				frameDisplay.add(newLine(idx, String.format("%s", strArray[1])));
				frameDisplay.get(idx*2 + 1).setxAlign(38);
			}
		}

		return frameDisplay;
	}

	public ArrayList<DisplayLine> loadInstrEdit(ScreenMode mode) {
		ArrayList<DisplayLine> edit = new ArrayList<DisplayLine>();
		
		switch(mode){
		case SET_MV_INSTR_TYPE:
			edit.add(newLine("1.JOINT"));
			edit.add(newLine("2.LINEAR"));
			edit.add(newLine("3.CIRCULAR"));
			break;
		case SET_MV_INSTR_REG_TYPE:
			edit.add(newLine("1.LOCAL(P)"));
			edit.add(newLine("2.GLOBAL(PR)"));
			break;
		case SET_MV_INSTR_IDX:
			edit.add(newLine("Enter desired position/ register:"));
			edit.add(newLine("\0" + workingText));
			break;
		case SET_MV_INSTR_SPD:
			edit.add(newLine("Enter desired speed:"));
			MotionInstruction castIns = activeMotionInst();

			if(castIns.getMotionType() == MTYPE_JOINT) {
				speedInPercentage = true;
				workingTextSuffix = "%";
			} else {
				workingTextSuffix = "mm/s";
				speedInPercentage = false;
			}

			edit.add(newLine(workingText + workingTextSuffix));
			break;
		case SET_MV_INSTR_TERM:
			edit.add(newLine("Enter desired termination %(0-100):"));
			edit.add(newLine("\0" + workingText));
			break;
		case SET_MV_INSTR_OFFSET:
			edit.add(newLine("Enter desired offset register (1-1000):"));
			edit.add(newLine("\0" + workingText));
			break;
		case SET_IO_INSTR_STATE:
			edit.add(newLine("1. ON"));
			edit.add(newLine("2. OFF"));
			break;
		case SET_IO_INSTR_IDX:
			edit.add(newLine("Select I/O register index:"));
			edit.add(newLine("\0" + workingText));
			break;
		case SET_FRM_INSTR_TYPE:
			edit.add(newLine("1. TFRAME_NUM = x"));
			edit.add(newLine("2. UFRAME_NUM = x"));
			break;
		case SET_FRAME_INSTR_IDX:
			edit.add(newLine("Select frame index:"));
			edit.add(newLine("\0" + workingText));
			break;
		case SET_REG_EXPR_TYPE:
			edit.add(newLine("1. R[x] = (...)"));
			edit.add(newLine("2. IO[x] = (...)"));
			edit.add(newLine("3. PR[x] = (...)"));
			edit.add(newLine("4. PR[x, y] = (...)"));
			break;
		case SET_REG_EXPR_IDX1:
			edit.add(newLine("Select register index:"));
			edit.add(newLine("\0" + workingText));
			break;
		case SET_REG_EXPR_IDX2:
			edit.add(newLine("Select point index:"));
			edit.add(newLine("\0" + workingText));
			break;
		case SET_EXPR_OP:
			if(opEdit instanceof Expression) {
				if(activeInstruction() instanceof IfStatement) {
					edit.add(newLine("1. + "));
					edit.add(newLine("2. - "));
					edit.add(newLine("3. * "));
					edit.add(newLine("4. / "));
					edit.add(newLine("5. | "));
					edit.add(newLine("6. % "));
					edit.add(newLine("7. = "));
					edit.add(newLine("8. <> "));
					edit.add(newLine("9. > "));
					edit.add(newLine("10. < "));
					edit.add(newLine("11. >= "));
					edit.add(newLine("12. <= "));
					edit.add(newLine("13. AND "));
					edit.add(newLine("14. OR "));
					edit.add(newLine("15. NOT "));
				} else {
					edit.add(newLine("1. + "));
					edit.add(newLine("2. - "));
					edit.add(newLine("3. * "));
					edit.add(newLine("4. / "));
					edit.add(newLine("5. | "));
					edit.add(newLine("6. % "));
				}
			} else {
				edit.add(newLine("1. ... =  ..."));
				edit.add(newLine("2. ... <> ..."));
				edit.add(newLine("3. ... >  ..."));
				edit.add(newLine("4. ... <  ..."));
				edit.add(newLine("5. ... >= ..."));
				edit.add(newLine("6. ... <= ..."));
			}
			
			break;
		case SET_EXPR_ARG:
		case SET_BOOL_EXPR_ARG:
			edit.add(newLine("R[x]"));
			edit.add(newLine("IO[x]"));
			if(opEdit instanceof Expression) {
				edit.add(newLine("PR[x]"));
				edit.add(newLine("PR[x, y]"));
				edit.add(newLine("(...)"));
			}
			edit.add(newLine("Const"));
			break;
		case INPUT_DREG_IDX:
		case INPUT_IOREG_IDX:
		case INPUT_PREG_IDX1:
			edit.add(newLine("Input register index:"));
			edit.add(newLine("\0" + workingText));
			break;
		case INPUT_PREG_IDX2:
			edit.add(newLine("Input position value index:"));
			edit.add(newLine("\0" + workingText));
			break;
		case INPUT_CONST:
			edit.add(newLine("Input constant value:"));
			edit.add(newLine("\0" + workingText));
			break;
		case SET_BOOL_CONST:
			edit.add(newLine("1. False"));
			edit.add(newLine("2. True"));
			break;
		case SET_IF_STMT_ACT:
		case SET_SELECT_STMT_ACT:
			edit.add(newLine("JMP LBL[x]"));
			edit.add(newLine("CALL"));
			break;
		case SET_SELECT_STMT_ARG:
			edit.add(newLine("R[x]"));
			edit.add(newLine("Const"));
			break;
		case SET_SELECT_ARGVAL:
			edit.add(newLine("Input value/ register index:"));
			edit.add(newLine("\0" + workingText));
			break;
		case SET_LBL_NUM:
			edit.add(newLine("Set label number:"));
			edit.add(newLine("\0" + workingText));
			break;
		case SET_JUMP_TGT:
			edit.add(newLine("Set jump target label:"));
			edit.add(newLine("\0" + workingText));
			break;
		default:
			break;
		}
		
		return edit;
	}

	public ArrayList<DisplayLine> loadInstructionReg() {
		ArrayList<DisplayLine> instrReg = new ArrayList<DisplayLine>();
		
		// show register contents if you're highlighting a register
		Instruction ins = activeInstruction();
		if(ins instanceof MotionInstruction) {
			MotionInstruction castIns = (MotionInstruction)ins;
			Point p = castIns.getPoint(activeProgram());

			if (p != null) {
				instrReg.add(newLine("Position values (press ENTER to exit):"));

				String[] regEntry = p.toLineStringArray(castIns.getMotionType() != MTYPE_JOINT);

				for (String line : regEntry) {
					instrReg.add(newLine(line));
				}

				if (castIns.getUserFrame() != -1) {
					Frame uFrame = armModel.getUserFrame(castIns.getUserFrame());
					displayPoint = removeFrame(p, uFrame.getOrigin(), uFrame.getOrientation());

				} else {
					displayPoint = p;
				}
			}

		} else {
			instrReg.add(newLine("This position is empty (press ENTER to exit):"));
		}
		
		return instrReg;
	}

	// prepare for displaying motion instructions on screen
	public ArrayList<DisplayLine> loadInstructions(int programID) {
		ArrayList<DisplayLine> instruct_list = new ArrayList<DisplayLine>();
		int tokenOffset = Fields.TXT_PAD - Fields.PAD_OFFSET;

		Program p = armModel.getProgram(programID);
		int size = p.getInstructions().size();

		for(int i = 0; i < size; i+= 1) {
			DisplayLine line = new DisplayLine(i);
			Instruction instr = p.getInstruction(i);
			int xPos = 10;

			// Add line number
			if (instr == null) {
				line.add( String.format("%d) ...", i+1) );
				continue;
				
			} else if(instr.isCommented()) {
				line.add("//"+Integer.toString(i+1) + ")");
			} else {
				line.add(Integer.toString(i+1) + ")");
			}

			int numWdth = line.get(line.size() - 1).length();
			xPos += numWdth*Fields.CHAR_WDTH + tokenOffset;

			if(instr instanceof MotionInstruction) {
				// Show '@' at the an instrution, if the Robot's position is close to that position stored in the instruction's register
				MotionInstruction a = (MotionInstruction)instr;
				Point ee_point = nativeRobotEEPoint(armModel.getJointAngles());
				Point instPt = a.getVector(p);

				if(instPt != null && ee_point.position.dist(instPt.position) < (liveSpeed / 100f)) {
					line.add("@");
				}
				else {
					line.add("\0");
				}

				xPos += Fields.CHAR_WDTH + tokenOffset;
			}

			String[] fields = instr.toStringArray();

			for (int j = 0; j < fields.length; j += 1) {
				String field = fields[j];
				xPos += field.length()*Fields.CHAR_WDTH + tokenOffset;

				if(field.equals("\n") && j != fields.length - 1) {
					instruct_list.add(line);
					if(instr instanceof SelectStatement) {
						xPos = 11*Fields.CHAR_WDTH + 3*tokenOffset;
					} else {
						xPos = 3*Fields.CHAR_WDTH + 3*tokenOffset;
					}

					line = new DisplayLine(i, xPos);
					xPos += field.length()*Fields.CHAR_WDTH + tokenOffset;
				} else if(xPos > display_width) {
					instruct_list.add(line);
					xPos = 2*Fields.CHAR_WDTH + tokenOffset;

					line = new DisplayLine(i, xPos);
					field = ": " + field;
					xPos += field.length()*Fields.CHAR_WDTH + tokenOffset;
				}

				if(!field.equals("\n")) {
					line.add(field);
				}
			}

			instruct_list.add(line);
		}

		if(mode.getType() != ScreenType.TYPE_LINE_SELECT) {
			instruct_list.add(newLine(size, "[End]"));
		}

		return instruct_list;
	}

	public ArrayList<DisplayLine> loadIORegisters() {
		ArrayList<DisplayLine> ioRegs = new ArrayList<DisplayLine>();

		for(int i = 0; i < ArmModel.IOREG_NUM; i += 1){
			IORegister ioReg = armModel.getIOReg(i);
			String state = (ioReg.state == Fields.ON) ? "ON" : "OFF";
			String ee;

			if (ioReg.comment != null) {
				ee = ioReg.comment;
			} else {
				ee = "UINIT";
			}

			ioRegs.add(newLine(String.format("IO[%d:%-8s] = %s", i + 1, ee, state)));
		}

		return ioRegs;
	}

	public ArrayList<DisplayLine> loadMacros() {
		ArrayList<DisplayLine> macroDisplay = new ArrayList<DisplayLine>();

		for(int i = 0; i < macros.size(); i += 1) {
			String[] strArray = macros.get(i).toStringArray();
			macroDisplay.add(newLine(i, ""+(i+1), strArray[0], strArray[1], strArray[2]));  
		}

		return macroDisplay;
	}

	public ArrayList<DisplayLine> loadManualFunct() {
		ArrayList<DisplayLine> functionDisplay = new ArrayList<DisplayLine>();
		int macroNum = 0;

		for(int i = 0; i < macros.size(); i += 1) {
			if(macros.get(i).isManual()) {
				macroNum += 1;
				String manFunct = macros.get(i).toString();
				functionDisplay.add(newLine(i, macroNum + " " + manFunct));
			}
		}

		return functionDisplay;
	}

	/**
	 * Displays the points along with their respective titles for the
	 * current frame teach method (discluding the Direct Entry method).
	 */
	public ArrayList<DisplayLine> loadPointList() {
		ArrayList<DisplayLine> points = new ArrayList<DisplayLine>();

		if(teachFrame != null) {

			ArrayList<String> temp = new ArrayList<String>();
			// Display TCP teach points
			if(mode == ScreenMode.TEACH_3PT_TOOL || mode == ScreenMode.TEACH_6PT) {
				temp.add("First Approach Point: ");
				temp.add("Second Approach Point: ");
				temp.add("Third Approach Point: ");
			}
			// Display Axes Vectors teach points
			if(mode == ScreenMode.TEACH_3PT_USER || mode == ScreenMode.TEACH_4PT || mode == ScreenMode.TEACH_6PT) {
				temp.add("Orient Origin Point: ");
				temp.add("X Axis Point: ");
				temp.add("Y Axis Point: ");
			}
			// Display origin offset point
			if(mode == ScreenMode.TEACH_4PT) {
				// Name of fourth point for the four point method?
				temp.add("Origin: ");
			}

			// Determine if the point has been set yet
			for(int idx = 0; idx < temp.size(); ++idx) {
				// Add each line to options
				points.add(newLine(temp.get(idx) + ((teachFrame.getPoint(idx) != null) ? "RECORDED" : "UNINIT")));
			}
		} else {
			// No teach points
			points.add(newLine("Error: teachFrame not set!"));
		}

		return points;
	}

	/**
	 * This method will transition to the INPUT_POINT_C or INPUT_POINT_J modes
	 * based whether the current mode is VIEW_REG_C or VIEW_REG_J. In either
	 * mode, the user will be prompted to input 6 floating-point values (X, Y,
	 * Z, W, P, R or J1 - J6 for INPUT_POINT_C or INPUT_POINT_J respectively).
	 * The input method is similar to inputting the value in DIRECT_ENTRY mode.
	 */
	public ArrayList<DisplayLine> loadPosRegEntry(PositionRegister reg) {
		ArrayList<DisplayLine> register = new ArrayList<DisplayLine>();

		if(reg.point == null) {
			// Initialize values to zero if the entry is null
			if(mode == ScreenMode.EDIT_PREG_C) {
				register.add(newLine(0, "X: ",  ""));
				register.add(newLine(1, "Y: ",  ""));
				register.add(newLine(2, "Z: ",  ""));
				register.add(newLine(3, "W: ",  ""));
				register.add(newLine(4, "P: ",  ""));
				register.add(newLine(5, "R: ",  ""));

			} else if(mode == ScreenMode.EDIT_PREG_J) {
				for(int idx = 0; idx < 6; idx += 1) {
					register.add(newLine(idx, String.format("J%d: ", idx), ""));
				}
			}
		} else {

			// List current entry values if the Register is initialized
			String[][] entries;

			if (mode == ScreenMode.EDIT_PREG_J) {
				// List joint angles
				entries = reg.point.toJointStringArray();
			} else {
				// Display Cartesian values
				entries = reg.point.toCartesianStringArray();
			}

			for(int idx = 0; idx < entries.length; ++idx) {
				register.add(newLine(idx, entries[idx][0], entries[idx][1]));
			}
		}

		return register;
	}

	public ArrayList<DisplayLine> loadPrograms() {
		ArrayList<DisplayLine> progs = new ArrayList<DisplayLine>();
		int size = armModel.numOfPrograms();

		//int start = start_render;
		//int end = min(start + ITEMS_TO_SHOW, size);

		for(int i = 0; i < size; i += 1) {
			progs.add(newLine(i, armModel.getProgram(i).getName()));
		}

		return progs;
	}

	/**
	 * Displays the list of Registers in mode VIEW_REG or the Position Registers
	 * for modes VIEW_REG_J or VIEW_REG_C. In mode VIEW_REG_J the joint angles
	 * associated with the Point are displayed and the Cartesian values are
	 * displayed in mode VIEW_REG_C.
	 */
	public ArrayList<DisplayLine> loadRegisters() { 
		ArrayList<DisplayLine> regs = new ArrayList<DisplayLine>();

		// View Registers or Position Registers
		//int start = start_render;
		//int end = min(start + ITEMS_TO_SHOW, DREG.length);

		// Display a subset of the list of registers
		for (int idx = 0; idx < ArmModel.DPREG_NUM; ++idx) {
			Register reg;

			if(mode == ScreenMode.NAV_DREGS) {
				reg = armModel.getDReg(idx);
			} else {
				reg = armModel.getPReg(idx);
			}

			// Display the comment associated with a specific Register entry
			String regLbl = reg.toStringWithComm();
			// Display Register value (* if uninitialized)
			String regEntry = "*";

			if(mode == ScreenMode.NAV_DREGS) {
				Float val = ((DataRegister)reg).value;
				
				if(val != null) {
					// Display Register value
					regEntry = String.format("%4.3f", val);
				}

			} else if (((PositionRegister)reg).point != null) {
				regEntry = "...Edit...";
			}

			regs.add(newLine(idx, regLbl, regEntry));
		}

		return regs;
	}

	public void loadScreen() {  
		switch(mode){
		//Main menu
		case NAV_MAIN_MENU:
			active_index = 0;
			options.reset();
			contents.setColumnIdx(0);
			break;

			//Frames
		case ACTIVE_FRAMES:
			contents.setLineIdx(0);
			contents.setColumnIdx(1);
			workingText = Integer.toString(armModel.getActiveToolFrame() + 1);
			break;
		case SELECT_FRAME_MODE:
			active_index = 0;
			options.reset();
			break;
		case NAV_TOOL_FRAMES:
		case NAV_USER_FRAMES:
			contents.setLineIdx(active_index*2);
			contents.setColumnIdx(0);
			break;
		case TFRAME_DETAIL:
		case UFRAME_DETAIL:
			contents.reset();
			break;
		case TEACH_3PT_TOOL:
		case TEACH_3PT_USER:
		case TEACH_4PT:
		case TEACH_6PT:
			options.reset();
			break;
		case FRAME_METHOD_TOOL:
		case FRAME_METHOD_USER:
			contents.setLineIdx(-1);
			contents.setColumnIdx(-1);
			options.reset();
			break;

			//Programs and instructions
		case NAV_PROGRAMS:
			// Stop Robot movement (i.e. program execution)
			armModel.halt();
			contents.setLineIdx(getActive_prog());
			contents.setColumnIdx(0);
			setActive_instr(0);
			break;
		case PROG_CREATE:
			contents.setLineIdx(1);
			contents.setColumnIdx(0);
			options.reset();
			workingText = "\0";
			break;
		case PROG_RENAME:
			setActive_prog(contents.getLineIdx());
			contents.setLineIdx(1);
			contents.setColumnIdx(0);
			options.reset();
			workingText = activeProgram().getName();
			break;
		case PROG_COPY:
			setActive_prog(contents.getLineIdx());
			contents.setLineIdx(1);
			contents.setColumnIdx(0);
			options.reset();
			workingText = "\0";
			break;
		case NAV_PROG_INSTR:
			//need to enforce row/ column select limits based on 
			//program length/ instruction width
			if(prev_select != -1) {
				contents.setLineIdx(prev_select);
				prev_select = -1;
			}
			
			break;
		case SET_CALL_PROG:
			prev_select = contents.getLineIdx();
			contents.reset();
			break;
		case CONFIRM_INSERT:
			workingText = "";
			break;
		case SELECT_INSTR_INSERT:
		case SELECT_JMP_LBL:
		case SELECT_REG_STMT:
		case SELECT_COND_STMT:
		case SELECT_PASTE_OPT:
		case SET_IF_STMT_ACT:
		case SET_SELECT_STMT_ACT:
		case SET_SELECT_STMT_ARG:
		case SET_EXPR_ARG:
		case SET_BOOL_EXPR_ARG:
		case SET_EXPR_OP:
			options.reset();
			break;
		case SET_MV_INSTR_OFFSET:
		case INPUT_DREG_IDX:
		case INPUT_IOREG_IDX:
		case INPUT_PREG_IDX1:
		case INPUT_PREG_IDX2:
		case INPUT_CONST:
			workingText = "";
			break;
		case SET_IO_INSTR_IDX:
		case SET_JUMP_TGT:
		case SET_LBL_NUM:
			contents.setColumnIdx(1);
			options.reset();
			workingText = ""; 
			break;
		case SET_MV_INSTR_TYPE:
			MotionInstruction mInst = activeMotionInst();

			switch (mInst.getMotionType()) {
			case MTYPE_JOINT:
				options.reset();
				break;
			case MTYPE_LINEAR:
				options.setLineIdx(1);
				break;
			case MTYPE_CIRCULAR:
				options.setLineIdx(2);
				break;
			}

			break;
		case SET_MV_INSTR_SPD:
			mInst = activeMotionInst();
			int instSpd;
			// Convert speed into an integer value
			if (mInst.getMotionType() == MTYPE_JOINT) {
				instSpd = Math.round(mInst.getSpeed() * 100f);
			} else {
				instSpd = Math.round(mInst.getSpeed());
			}

			options.reset();
			workingText = Integer.toString(instSpd);
			break;
		case SET_MV_INSTR_REG_TYPE:
			mInst = activeMotionInst();

			if (mInst.usesGPosReg()) {
				options.setLineIdx(1);
			} else {
				options.reset();
			}

			break;
		case SET_MV_INSTR_IDX:
			mInst = activeMotionInst();
			options.reset();
			workingText = Integer.toString(mInst.getPositionNum() + 1);
			break;
		case SET_MV_INSTR_TERM:
			mInst = activeMotionInst();
			options.reset();
			workingText = Integer.toString(mInst.getTermination());
			break;
		case SET_FRAME_INSTR_IDX:
		case SET_SELECT_ARGVAL:
		case SET_REG_EXPR_IDX1:
		case SET_REG_EXPR_IDX2:
			options.reset();
			workingText = "";
			break;
		case SET_IO_INSTR_STATE:
		case SET_FRM_INSTR_TYPE:
		case SET_REG_EXPR_TYPE:
			options.reset();
			break;
		case SELECT_INSTR_DELETE:
		case SELECT_COMMENT:
		case SELECT_CUT_COPY:
			Program p = activeProgram();
			int size = p.getInstructions().size() - 1;
			setActive_instr(max(0, min(getActive_instr(), size)));
			contents.setColumnIdx(0);
			break;

			//Macros
		case NAV_MACROS:
			contents.setLineIdx(active_index);
			break;
		case SET_MACRO_PROG:
			contents.setLineIdx(0);
			break;
		case SET_MACRO_TYPE:
		case SET_MACRO_BINDING:
			options.reset();
			break;

			//Registers
		case NAV_DATA:
			options.reset();
			active_index = 0;
			break;
		case NAV_DREGS:
		case NAV_PREGS_J:
		case NAV_PREGS_C:
			contents.setLineIdx(active_index);
			contents.setColumnIdx(0);
			break;
		case DIRECT_ENTRY_TOOL:
		case DIRECT_ENTRY_USER:
			contents.setLineIdx(0);
			contents.setColumnIdx(1);
			contents.setContents(loadFrameDirectEntry(teachFrame));
			break;
		case NAV_INSTR_MENU:
			options.reset();
			break;
		case SWAP_PT_TYPE:
			options.reset();
			break;
		case CP_DREG_COM:
		case CP_DREG_VAL:
		case CP_PREG_COM:
		case CP_PREG_PT:
			options.setLineIdx(1);
			workingText = Integer.toString((active_index + 1));
			break;
		case EDIT_DREG_COM:
			contents.setLineIdx(1);
			contents.setColumnIdx(0);
			options.reset();

			String c = armModel.getDReg(active_index).comment;
			if(c != null && c.length() > 0) {
				workingText = c;
			}
			else {
				workingText = "\0";
			}

			break;   
		case EDIT_PREG_COM:
			contents.setLineIdx(1);
			contents.setColumnIdx(0);
			options.reset();
			
			c = armModel.getPReg(active_index).comment;
			if(c != null && c.length() > 0) {
				workingText = c;
			}
			else {
				workingText = "\0";
			}

			println(workingText.length());
			break;
		case EDIT_DREG_VAL:
			options.reset();
			// Bring up float input menu
			Float val = armModel.getDReg(active_index).value;
			if(val != null) {
				workingText = Float.toString(val);
			} else {
				workingText = "";
			}
			break;
		case EDIT_PREG_C:
		case EDIT_PREG_J:
			contents.setLineIdx(0);
			contents.setColumnIdx(1);
			contents.setContents(loadPosRegEntry(armModel.getPReg(active_index)));
			break;
		default:
			break;
		}

		updateScreen();
	}

	/**
	 * Build a PShape object from the contents of the given .stl source file
	 * stored in /RobotRun/data/.
	 * 
	 * @throws NullPointerException  if the given filename does not pertain
	 *         to a valid .stl file located in RobotRun/data/
	 */
	public PShape loadSTLModel(String filename, int fill) throws NullPointerException {
		ArrayList<Triangle> triangles = new ArrayList<Triangle>();
		byte[] data = loadBytes(filename);

		int n = 84; // skip header and number of triangles

		while(n < data.length) {
			Triangle t = new Triangle();
			for(int m = 0; m < 4; m++) {
				byte[] bytesX = new byte[4];
				bytesX[0] = data[n+3]; bytesX[1] = data[n+2];
				bytesX[2] = data[n+1]; bytesX[3] = data[n];
				n += 4;
				byte[] bytesY = new byte[4];
				bytesY[0] = data[n+3]; bytesY[1] = data[n+2];
				bytesY[2] = data[n+1]; bytesY[3] = data[n];
				n += 4;
				byte[] bytesZ = new byte[4];
				bytesZ[0] = data[n+3]; bytesZ[1] = data[n+2];
				bytesZ[2] = data[n+1]; bytesZ[3] = data[n];
				n += 4;
				t.components[m] = new PVector(
						ByteBuffer.wrap(bytesX).getFloat(),
						ByteBuffer.wrap(bytesY).getFloat(),
						ByteBuffer.wrap(bytesZ).getFloat()
						);
			}
			triangles.add(t);
			n += 2; // skip meaningless "attribute byte count"
		}

		PShape mesh = createShape();
		mesh.beginShape(TRIANGLES);
		mesh.noStroke();
		mesh.fill(fill);
		for(Triangle t : triangles) {
			mesh.normal(t.components[0].x, t.components[0].y, t.components[0].z);
			mesh.vertex(t.components[1].x, t.components[1].y, t.components[1].z);
			mesh.vertex(t.components[2].x, t.components[2].y, t.components[2].z);
			mesh.vertex(t.components[3].x, t.components[3].y, t.components[3].z);
		}
		mesh.endShape();

		return mesh;
	}

	/**
	 * This method loads text to screen in sfuch a way as to allow the user
	 * to input an arbitrary character string consisting of letters (a-z
	 * upper and lower case) and/ or special characters (_, @, *, .) via
	 * the function row, as well as numbers via the number pad. Strings are
	 * limited to 16 characters and can be used to name new routines, as well
	 * as set remark fields for frames and instructions.
	 */
	public ArrayList<DisplayLine> loadTextInput() {
		ArrayList<DisplayLine> remark = new ArrayList<DisplayLine>();

		remark.add(newLine("\0"));

		DisplayLine line = new DisplayLine();
		// Give each letter in the name a separate column
		for(int idx = 0; idx < workingText.length() && idx < TEXT_ENTRY_LEN; idx += 1) {
			line.add( Character.toString(workingText.charAt(idx)) );
		}

		remark.add(line);

		return remark;
	}

	/**
	 * This method will draw the End Effector grid mapping based on the value of EE_MAPPING:
	 *
	 *  0 -> a line is drawn between the EE and the grid plane
	 *  1 -> a point is drawn on the grid plane that corresponds to the EE's xz coordinates
	 *  For any other value, nothing is drawn
	 */
	public void mapToRobotBasePlane() {

		PVector ee_pos = nativeRobotEEPoint(armModel.getJointAngles()).position;

		// Change color of the EE mapping based on if it lies below or above the ground plane
		int c = (ee_pos.y <= ROBOT_POSITION.y) ? color(255, 0, 0) : color(150, 0, 255);

		// Toggle EE mapping type with 'e'
		switch (mappingState) {
		case LINE:
			stroke(c);
			// Draw a line, from the EE to the grid in the xy plane, parallel to the xy plane
			line(ee_pos.x, ee_pos.y, ee_pos.z, ee_pos.x, ROBOT_POSITION.y, ee_pos.z);
			break;

		case DOT:
			noStroke();
			fill(c);
			// Draw a point, which maps the EE's position to the grid in the xy plane
			pushMatrix();
			rotateX(PI / 2);
			translate(0, 0, -ROBOT_POSITION.y);
			ellipse(ee_pos.x, ee_pos.z, 10, 10);
			popMatrix();
			break;

		default:
			// No EE grid mapping
		}
	}

	/**
	 * Returns a string represenation of the given matrix.
	 * 
	 * @param matrix  A non-null matrix
	 */
	public String matrixToString(float[][] matrix) {
		String mStr = "";

		for(int row = 0; row < matrix.length; ++row) {
			mStr += "\n[";

			for(int col = 0; col < matrix[0].length; ++col) {
				// Account for the negative sign character
				if(matrix[row][col] >= 0) { mStr += " "; }

				mStr += String.format(" %5.6f", matrix[row][col]);
			}

			mStr += "  ]";
		}

		return (mStr + "\n");
	}

	public void mouseDragged(MouseEvent e) {
		if (mouseButton == CENTER) {
			// Drag the center mouse button to pan the camera
			float transScale = camera.getScale();
			camera.move(transScale * (mouseX - pmouseX), transScale * (mouseY - pmouseY), 0);
		}

		if (mouseButton == RIGHT) {
			// Drag right mouse button to rotate the camera
			float rotScale = DEG_TO_RAD / 4f;
			camera.rotate(rotScale * (mouseY - pmouseY), rotScale * (mouseX - pmouseX), 0);
		}
	}

	public void mouseWheel(MouseEvent event) {
		if (getManager() != null && getManager().isMouseOverADropdownList()) {
			// Disable zomming when selecting an element from a dropdown list
			return;
		}

		float e = event.getCount();
		// Control scaling of the camera with the mouse wheel
		if (e > 0) {
			camera.changeScale(1.05f);
		} else if (e < 0) {
			camera.changeScale(0.95f);
		}
	}

	// Menu button
	public void mu() {
		resetStack();
		nextScreen(ScreenMode.NAV_MAIN_MENU);
	}

	public void MVMU() {
		if(getSU_macro_bindings()[2] != null && shift) {
			getSU_macro_bindings()[2].execute();
		}
	}

	public void newCallInstruction() {
		Program p = activeProgram();
		CallInstruction call = new CallInstruction();

		if(getActive_instr() != p.getInstructions().size()) {
			p.overwriteInstruction(getActive_instr(), call);
		} else {
			p.addInstruction(call);
		}
	}

	public void newFrameInstruction(int fType) {
		Program p = activeProgram();
		FrameInstruction f = new FrameInstruction(fType, -1);

		if(getActive_instr() != p.getInstructions().size()) {
			p.overwriteInstruction(getActive_instr(), f);
		} else {
			p.addInstruction(f);
		}
	}

	public void newIfExpression() {
		Program p = activeProgram();
		IfStatement stmt = new IfStatement();

		if(getActive_instr() != p.getInstructions().size()) {
			p.overwriteInstruction(getActive_instr(), stmt);
		} else {
			p.addInstruction(stmt);
		}
	}
	public void newIfStatement() {
		Program p = activeProgram();
		IfStatement stmt = new IfStatement(Operator.EQUAL, null);
		opEdit = stmt.getExpr();

		if(getActive_instr() != p.getInstructions().size()) {
			p.overwriteInstruction(getActive_instr(), stmt);
		} else {
			p.addInstruction(stmt);
		}
	}
	public void newIOInstruction() {
		Program p = activeProgram();
		IOInstruction io = new IOInstruction(options.getLineIdx(), Fields.OFF);

		if(getActive_instr() != p.getInstructions().size()) {
			p.overwriteInstruction(getActive_instr(), io);
		} else {
			p.addInstruction(io);
		}
	}
	
	public void newJumpInstruction() {
		Program p = activeProgram();
		JumpInstruction j = new JumpInstruction(-1);

		if(getActive_instr() != p.getInstructions().size()) {
			p.overwriteInstruction(getActive_instr(), j);
		} else {
			p.addInstruction(j);
		}
	}
	
	public void newLabel() {
		Program p = activeProgram();

		LabelInstruction l = new LabelInstruction(-1);

		if(getActive_instr() != p.getInstructions().size()) {
			p.overwriteInstruction(getActive_instr(), l);
		} else {
			p.addInstruction(l);
		}
	}
	
	public DisplayLine newLine(int itemIdx, String... columns) {
		DisplayLine line =  new DisplayLine(itemIdx);

		for(String col : columns) {
			line.add(col);
		}

		return line;
	}
	/**
	 * Given a set of Strings this method returns a single
	 * String ArrayList, which contains all the given elements
	 * in the order that they are given as arguments.
	 * 
	 * @param columns  A list of Strings
	 * @return         An ArrayList containing all the given
	 *                 Strings
	 */
	public DisplayLine newLine(String... columns) {
		DisplayLine line =  new DisplayLine();

		for(String col : columns) {
			line.add(col);
		}

		return line;
	}

	public void newMotionInstruction() {
		Point pt = nativeRobotEEPoint(armModel.getJointAngles());
		Frame active = armModel.getActiveFrame(CoordFrame.USER);

		if (active != null) {
			// Convert into currently active frame
			pt = applyFrame(pt, active.getOrigin(), active.getOrientation());

			if (DISPLAY_TEST_OUTPUT) {
				System.out.printf("New: %s\n", convertNativeToWorld(pt.position));
			}
		}

		Program prog = activeProgram();
		int reg = prog.getNextPosition();

		if(getSelectedLine() > 0) {
			// Update the secondary point of a circular instruction
			MotionInstruction m = (MotionInstruction)activeInstruction();
			m.getSecondaryPoint().setPositionNum(reg);
			prog.setPosition(reg, pt);
		}
		else {
			MotionInstruction insert = new MotionInstruction(
					armModel.getCurCoordFrame() == CoordFrame.JOINT ? MTYPE_JOINT : MTYPE_LINEAR,
					getActive_instr(),
					false,
					(armModel.getCurCoordFrame() == CoordFrame.JOINT ? 50 : 50 * armModel.motorSpeed) / 100f,
					0,
					armModel.getActiveUserFrame(),
					armModel.getActiveToolFrame());

			prog.setPosition(getActive_instr(), pt);

			if(getActive_instr() != prog.getInstructions().size()) {
				// overwrite current instruction
				prog.overwriteInstruction(getActive_instr(), insert);
			} else {
				prog.addInstruction(insert);
			}
		}
	}

	public void newRegisterStatement(Register r) {
		Program p = activeProgram();
		RegisterStatement stmt = new RegisterStatement(r);

		if(getActive_instr() != p.getInstructions().size()) {
			p.overwriteInstruction(getActive_instr(), stmt);
		} else {
			p.addInstruction(stmt);
		}
	}

	public void newRegisterStatement(Register r, int i) {
		Program p = activeProgram();
		RegisterStatement stmt = new RegisterStatement(r, i);

		if(getActive_instr() != p.getInstructions().size()) {
			p.overwriteInstruction(getActive_instr(), stmt);
		} else {
			p.addInstruction(stmt);
		}
	}

	public void NewScenario() {
		Scenario newScenario = getManager().initializeScenario();

		if (newScenario != null) {
			// Add the new scenario
			SCENARIOS.add(newScenario);
		}
	}

	public void newSelectStatement() {
		Program p = activeProgram();
		SelectStatement stmt = new SelectStatement();

		if(getActive_instr() != p.getInstructions().size()) {
			p.overwriteInstruction(getActive_instr(), stmt);
		} else {
			p.addInstruction(stmt);
		}
	} 


	/**
	 * Transitions the display to the given screen and pushes that screen
	 * onto the stack.
	 * 
	 * @param next    The new screen mode
	 */
	public void nextScreen(ScreenMode next) {
		// Stop a program from executing when transition screens
		setProgramRunning(false);
		if (DISPLAY_TEST_OUTPUT) { System.out.printf("%s => %s\n", mode, next);  }

		mode = next;
		display_stack.push(mode);
		loadScreen();
	}

	public void NUM0() {
		addNumber("0");
	}

	public void NUM1() {
		addNumber("1");
	}

	public void NUM2() {
		addNumber("2");
	}

	public void NUM3() {
		addNumber("3");
	}

	public void NUM4() {
		addNumber("4");
	}

	public void NUM5() {
		addNumber("5");
	}

	public void NUM6() {
		addNumber("6");
	}

	public void NUM7() {
		addNumber("7");
	}

	public void NUM8() {
		addNumber("8");
	}

	public void NUM9() {
		addNumber("9");
	}

	public void pasteInstructions() {
		pasteInstructions(0);
	}

	public void pasteInstructions(int options) {
		ArrayList<Instruction> pasteList = new ArrayList<Instruction>();
		Program p = activeProgram();

		/* Pre-process instructions for insertion into program. */
		for(int i = 0; i < clipBoard.size(); i += 1) {
			Instruction instr = clipBoard.get(i).clone();

			if(instr instanceof MotionInstruction) {
				MotionInstruction m = (MotionInstruction)instr;

				if((options & Fields.CLEAR_POSITION) == Fields.CLEAR_POSITION) {
					m.setPositionNum(-1);
				}
				else if((options & Fields.NEW_POSITION) == Fields.NEW_POSITION) {
					/* Copy the current instruction's position to a new local position
       index and update the instruction to use this new position */
					int instrPos = m.getPositionNum();
					int nextPos = p.getNextPosition();

					p.addPosition(p.getPosition(instrPos).clone());
					m.setPositionNum(nextPos);
				}

				if((options & Fields.REVERSE_MOTION) == Fields.REVERSE_MOTION) {
					MotionInstruction next = null;

					for(int j = i + 1; j < clipBoard.size(); j += 1) {
						if(clipBoard.get(j) instanceof MotionInstruction) {
							next = (MotionInstruction)clipBoard.get(j).clone();
							break;
						}
					}

					if(next != null) {
						println("asdf");
						m.setMotionType(next.getMotionType());
						m.setSpeed(next.getSpeed());
					}
				}
			}

			pasteList.add(instr);
		}

		/* Perform forward/ reverse insertion. */
		for(int i = 0; i < clipBoard.size(); i += 1) {
			Instruction instr;
			if((options & Fields.PASTE_REVERSE) == Fields.PASTE_REVERSE) {
				instr = pasteList.get(pasteList.size() - 1 - i);
			} else {
				instr = pasteList.get(i);
			}

			p.addInstruction(getActive_instr() + i, instr);
		}
	}

	public void PERIOD() {
		if(mode.getType() == ScreenType.TYPE_POINT_ENTRY) {

			if(contents.getLineIdx() >= 0 && contents.getLineIdx() < contents.size()) {

				// Add decimal point
				String value = contents.get(contents.getLineIdx()).get(1) + ".";

				if(value.length() > 9) {
					// Max length of a an input value
					value = value.substring(0,  9);
				}

				contents.get(contents.getLineIdx()).set(1, value);
			}
		} else if(mode.getType() == ScreenType.TYPE_NUM_ENTRY) {

			if(workingText.length() < NUM_ENTRY_LEN) {
				workingText += ".";
			}
		} else if(mode != ScreenMode.EDIT_DREG_COM) {
			workingText += ".";
		}

		updateScreen();
	}

	public void POSN() {
		if(getSU_macro_bindings()[5] != null && shift) {
			getSU_macro_bindings()[5].execute();
		}
	}

	public void pr() {
		lastScreen();
	}

	public void RESET() {
		if (shift) {
			// Reset robot fault
			armModel.halt();
			motionFault = false;
		}
	}

	//turn of highlighting on all active movement buttons
	public void resetButtonColors() {
		for(int i = 1; i <= 6; i += 1) {
			((Button)cp5.get("JOINT"+i+"_NEG")).setColorBackground(Fields.BUTTON_DEFAULT);
			((Button)cp5.get("JOINT"+i+"_POS")).setColorBackground(Fields.BUTTON_DEFAULT);
		}
	}

	public void resetStack(){
		// Stop a program from executing when transition screens
		setProgramRunning(false);
		display_stack.clear();

		mode = ScreenMode.DEFAULT;
		display_stack.push(mode);
	}

	public void RightView() {
		// Right view
		camera.reset();
		camera.rotate(0, 3f * PI / 2f, 0);
	}

	public void SaveScenario() {
		if (activeScenario != null) {
			// Save the current version of the active scenario
			for (int idx = 0; idx < SCENARIOS.size(); ++idx) {
				if (SCENARIOS.get(idx).getName().equals( activeScenario.getName() )) {
					SCENARIOS.set(idx, (Scenario)activeScenario.clone());
				}
			}
		}
		
		// Save all scenarios
		DataManagement.saveState(this);
	}

	// Select button
	public void se() {
		// Save when exiting a program
		DataManagement.saveState(this); 

		setActive_prog(0);
		setActive_instr(-1);

		resetStack();
		nextScreen(ScreenMode.NAV_PROGRAMS);
	}

	public void setActive_instr(int active_instr) {
		this.active_instr = active_instr;
	}

	public void setActive_prog(int active_prog) {
		this.active_prog = active_prog;
	}

	public void setCall_stack(Stack<int[]> call_stack) {
		this.call_stack = call_stack;
	}

	public void setExecutingInstruction(boolean executingInstruction) {
		this.executingInstruction = executingInstruction;
	}

	public void setManager(WindowManager manager) {
		this.manager = manager;
	}

	public void setProgramRunning(boolean programRunning) {
		this.programRunning = programRunning;
	}

	public void setRecord(int record) {
		this.record = record;
	}

	public void SetScenario() {
		/* Get the scenario, which is associated with the scenario
		 * dropdownlist's label */
		Scenario limbo = getManager().getActiveScenario();
		
		if (limbo != null) {
			if (activeScenario != null &&
					!limbo.getName().equals(activeScenario.getName())) {
				/* Replace the old version of this scenario with the active
				 * one, if the scenario to be loaded is not currently active */
				for (int idx = 0; idx < SCENARIOS.size(); ++idx) {
					if (SCENARIOS.get(idx).getName().equals( activeScenario.getName() )) {
						SCENARIOS.set(idx, (Scenario)activeScenario.clone());
					}
				}
			}
			
			// Set the active scenario to a copy of the loaded scenario
			activeScenario = (Scenario)limbo.clone();
		}
	}

	public void setStep(boolean step) {
		this.step = step;
	}

	public void setSU_macro_bindings(Macro[] sU_macro_bindings) {
		SU_macro_bindings = sU_macro_bindings;
	}

	public void settings() {  size(1080, 720, P3D); }

	public void setup() {
		instance = this;
		letterStates = new int[] {0, 0, 0, 0, 0};
		
		//size(1200, 800, P3D);
		//create font and text display background
		fnt_con14 = createFont("data/Consolas.ttf", 14);
		fnt_con12 = createFont("data/Consolas.ttf", 12);
		fnt_conB = createFont("data/ConsolasBold.ttf", 12);

		camera = new Camera();

		//load model and save data
		armModel = new ArmModel(0);
		intermediatePositions = new ArrayList<Point>();
		activeScenario = null;
		showOOBs = true;
		
		DataManagement.initialize(this);
		DataManagement.loadState(this);

		//set up UI
		cp5 = new ControlP5(this);
		// Explicitly draw the ControlP5 elements
		cp5.setAutoDraw(false);
		setManager(new WindowManager(cp5, fnt_con12, fnt_con14));
		display_stack = new Stack<ScreenMode>();
		contents = new MenuScroll(this, "cont", ITEMS_TO_SHOW, 10, 20);
		options = new MenuScroll(this, "opt", 3, 10, 180);
		gui();

		buffer = new ArrayList<String>();
		displayPoint = null;
	}

	public void SETUP() {
		if(getSU_macro_bindings()[3] != null && shift) {
			getSU_macro_bindings()[3].execute();
		}
	}

	/**
	 * Sets up an instruction for execution.
	 *
	 * @param program Program that the instruction belongs to
	 * @param model Arm model to use
	 * @param instruction The instruction to execute
	 * @return Returns false on failure (invalid instruction), true on success
	 */
	public boolean setUpInstruction(Program program, ArmModel model, MotionInstruction instruction) {
		Point start = nativeRobotEEPoint(model.getJointAngles());

		if (!instruction.checkFrames(armModel.getActiveToolFrame(), armModel.getActiveUserFrame())) {
			// Current Frames must match the instruction's frames
			System.out.printf("Tool frame: %d : %d\nUser frame: %d : %d\n\n", instruction.getToolFrame(),
					armModel.getActiveToolFrame(), instruction.getUserFrame(), armModel.getActiveUserFrame());
			return false;
		} else if(instruction.getVector(program) == null) {
			return false;
		}

		if(instruction.getMotionType() == MTYPE_JOINT) {
			armModel.setupRotationInterpolation(instruction.getVector(program).angles);
		} // end joint movement setup
		else if(instruction.getMotionType() == MTYPE_LINEAR) {

			if(instruction.getTermination() == 0 || execSingleInst) {
				beginNewLinearMotion(start, instruction.getVector(program));
			} 
			else {
				Point nextPoint = null;
				for(int n = getActive_instr()+1; n < program.getInstructions().size(); n++) {
					Instruction nextIns = program.getInstructions().get(n);
					if(nextIns instanceof MotionInstruction) {
						MotionInstruction castIns = (MotionInstruction)nextIns;
						nextPoint = castIns.getVector(program);
						break;
					}
				}
				if(nextPoint == null) {
					beginNewLinearMotion(start, instruction.getVector(program));
				} 
				else {
					beginNewContinuousMotion(start, 
							instruction.getVector(program),
							nextPoint, 
							instruction.getTermination() / 100f);
				}
			} // end if termination type is continuous
		} // end linear movement setup
		else if(instruction.getMotionType() == MTYPE_CIRCULAR) {
			MotionInstruction nextIns = instruction.getSecondaryPoint();
			Point nextPoint = nextIns.getVector(program);

			beginNewCircularMotion(start, instruction.getVector(program), nextPoint);
		} // end circular movement setup

		return true;
	} // end setUpInstruction

	//toggle shift on/ off and button highlight
	public void sf() {
		if(!shift) {
			((Button)cp5.get("sf")).setColorBackground(Fields.BUTTON_ACTIVE);
		} else {
			// Stop Robot jog movement when shift is off
			armModel.halt();
			((Button)cp5.get("sf")).setColorBackground(Fields.BUTTON_DEFAULT);
		}

		shift = !shift;
		updateScreen();
	}

	/**
	 * Displays important information in the upper-right corner of the screen.
	 */
	public void showMainDisplayText() {
		fill(0);
		textAlign(RIGHT, TOP);
		int lastTextPositionX = width - 20,
				lastTextPositionY = 20;
		String coordFrame = "Coordinate Frame: ";

		switch(armModel.getCurCoordFrame()) {
		case JOINT:
			coordFrame += "Joint";
			break;
		case WORLD:
			coordFrame += "World";
			break;
		case TOOL:
			coordFrame += "Tool";
			break;
		case USER:
			coordFrame += "User";
			break;
		default:
		}

		Point RP = nativeRobotEEPoint(armModel.getJointAngles());

		String[] cartesian = RP.toLineStringArray(true),
				joints = RP.toLineStringArray(false);
		// Display the current Coordinate Frame name
		text(coordFrame, lastTextPositionX, lastTextPositionY);
		lastTextPositionY += 20;
		// Display the Robot's speed value as a percent
		text(String.format("Jog Speed: %d%%", liveSpeed), lastTextPositionX, lastTextPositionY);
		lastTextPositionY += 20;
		// Display the title of the currently active scenario
		String scenarioTitle;

		if (activeScenario != null) {
			scenarioTitle = "Scenario: " + activeScenario.getName();
		} else {
			scenarioTitle = "No active scenario";
		}

		text(scenarioTitle, lastTextPositionX, lastTextPositionY);
		lastTextPositionY += 40;
		// Display the Robot's current position and orientation ini the World frame
		text("Robot Position and Orientation", lastTextPositionX, lastTextPositionY);
		lastTextPositionY += 20;
		text("World", lastTextPositionX, lastTextPositionY);
		lastTextPositionY += 20;

		for (String line : cartesian) {
			text(line, lastTextPositionX, lastTextPositionY);
			lastTextPositionY += 20;
		}

		Frame active = armModel.getActiveFrame(CoordFrame.USER);

		if (active != null) {
			// Display Robot's current position and orientation in the currently active User frame
			RP.position = convertToFrame(RP.position, active.getOrigin(), active.getOrientation());
			RP.orientation = active.getOrientation().transformQuaternion(RP.orientation);
			cartesian = RP.toLineStringArray(true);

			lastTextPositionY += 20;
			text(String.format("User: %d", armModel.getActiveUserFrame() + 1), lastTextPositionX, lastTextPositionY);
			lastTextPositionY += 20;

			for (String line : cartesian) {
				text(line, lastTextPositionX, lastTextPositionY);
				lastTextPositionY += 20;
			}
		}

		lastTextPositionY += 20;
		// Display the Robot's current joint angle values
		text("Joint", lastTextPositionX, lastTextPositionY);
		lastTextPositionY += 20;
		for (String line : joints) {
			text(line, lastTextPositionX, lastTextPositionY);
			lastTextPositionY += 20;
		}

		WorldObject toEdit = getManager().getActiveWorldObject();
		// Display the position and orientation of the active world object
		if (toEdit != null) {
			String[] dimFields = toEdit.dimFieldsToStringArray();
			// Convert the values into the World Coordinate System
			PVector position = convertNativeToWorld(toEdit.getLocalCenter());
			PVector wpr =  matrixToEuler(toEdit.getLocalOrientationAxes()).mult(RAD_TO_DEG);
			// Create a set of uniform Strings
			String[] fields = new String[] { String.format("X: %4.3f", position.x), String.format("Y: %4.3f", position.y),
					String.format("Z: %4.3f", position.z), String.format("W: %4.3f", -wpr.x),
					String.format("P: %4.3f", -wpr.z), String.format("R: %4.3f", wpr.y) };

			lastTextPositionY += 20;
			text(toEdit.getName(), lastTextPositionX, lastTextPositionY);
			lastTextPositionY += 20;
			String dimDisplay = "";
			// Display the dimensions of the world object (if any)
			for (int idx = 0; idx < dimFields.length; ++idx) {
				if ((idx + 1) < dimFields.length) {
					dimDisplay += String.format("%-12s", dimFields[idx]);

				} else {
					dimDisplay += String.format("%s", dimFields[idx]);
				}
			}

			text(dimDisplay, lastTextPositionX, lastTextPositionY);

			lastTextPositionY += 20;
			// Add space patting
			text(String.format("%-12s %-12s %s", fields[0], fields[1], fields[2]), lastTextPositionX, lastTextPositionY);
			lastTextPositionY += 20;
			text(String.format("%-12s %-12s %s", fields[3], fields[4], fields[5]), lastTextPositionX, lastTextPositionY);
			lastTextPositionY += 20;
		}

		lastTextPositionY += 20;
		// Display the current axes display state
		text(String.format("Axes Display: %s", axesState.name()),  lastTextPositionX, height - 50);

		if (axesState == AxesDisplay.GRID) {
			// Display the current ee mapping state
			text(String.format("EE Mapping: %s", mappingState.name()),  lastTextPositionX, height - 30);
		}

		if (DISPLAY_TEST_OUTPUT) {
			fill(215, 0, 0);

			// Display a message when there is an error with the Robot's movement
			if (motionFault) {
				text("Motion Fault (press SHIFT + Reset)", lastTextPositionX, lastTextPositionY);
				lastTextPositionY += 20;
			}

			// Display a message if the Robot is in motion
			if (armModel.modelInMotion()) {
				text("Robot is moving", lastTextPositionX, lastTextPositionY);
				lastTextPositionY += 20;
			}

			if (isProgramRunning()) {
				text("Program executing", lastTextPositionX, lastTextPositionY);
				lastTextPositionY += 20;
			}

			// Display a message while the robot is carrying an object
			if(armModel.held != null) {
				text("Object held", lastTextPositionX, lastTextPositionY);
				lastTextPositionY += 20;

				PVector held_pos = armModel.held.getLocalCenter();
				String obj_pos = String.format("(%f, %f, %f)", held_pos.x, held_pos.y, held_pos.z);
				text(obj_pos, lastTextPositionX, lastTextPositionY);
				lastTextPositionY += 20;
			}
		}

		getManager().updateWindowDisplay();
	}

	public void SLOWDOWN() {
		// Reduce the speed at which the Robot jogs
		if (shift) {

			if (liveSpeed > 50) {
				liveSpeed = 50;
			} else if (liveSpeed > 5) {
				liveSpeed = 5;
			} else {
				liveSpeed = 1;
			}
		} else if (liveSpeed > 1) {

			if (liveSpeed <= 5f) {
				--liveSpeed;
			} else if (liveSpeed <= 50) {
				liveSpeed -= 5;
			} else {
				liveSpeed -= 10;
			}
		}

		// The Robot's speed multiplier is bounded to the range 1% to 100%
		liveSpeed = max(1, liveSpeed);
	}

	public void SPEEDUP() {
		// Increase the speed at which the Robot jogs
		if (shift) {

			if (liveSpeed < 5) {
				liveSpeed = 5;
			} else if (liveSpeed < 50) {
				liveSpeed = 50;
			} else {
				liveSpeed = 100;
			}
		} else if (liveSpeed < 100) {

			if (liveSpeed < 5) {
				++liveSpeed;
			} else if (liveSpeed < 50) {
				liveSpeed += 5;
			} else if (liveSpeed < 100) {
				liveSpeed += 10f;
			}
		}

		// The Robot's speed multiplier is bounded to the range 1% to 100%
		liveSpeed = min(liveSpeed, 100);
	}

	//toggle step on/ off and button highlight
	public void st() {
		if(!isStep()) {
			((Button)cp5.get("st")).setColorBackground(Fields.BUTTON_ACTIVE);
		}
		else {
			((Button)cp5.get("st")).setColorBackground(Fields.BUTTON_DEFAULT);
		}


		setStep(!isStep());
		updateScreen();
	}

	public void STATUS() {
		if(getSU_macro_bindings()[4] != null && shift) {
			getSU_macro_bindings()[4].execute();
		}
	}

	/**
	 * Transitions to the given screen without saving the current screen on the stack.
	 * 
	 * @param nextScreen  The new screen mode
	 */
	public void switchScreen(ScreenMode nextScreen) {
		// Stop a program from executing when transition screens
		setProgramRunning(false);
		if (DISPLAY_TEST_OUTPUT) { System.out.printf("%s => %s\n", mode, nextScreen);  }

		mode = nextScreen;
		display_stack.pop();
		display_stack.push(mode);
		loadScreen();
	}

	public void TOOL1() {
		if(getSU_macro_bindings()[0] != null && shift) {
			getSU_macro_bindings()[0].execute();
		}
	}

	public void TOOL2() {
		if(getSU_macro_bindings()[1] != null && shift) {
			getSU_macro_bindings()[1].execute();
		}
	}

	public void TopView() {
		// Top view
		camera.reset();
		camera.rotate(3f * PI / 2f, 0, 0);
	}

	/**
	 * Stop robot motion, program execution
	 */
	public void triggerFault() {
		armModel.halt();
		motionFault = true;
	}

	/**
	 * Updates the index display in the Active Frames menu based on the
	 * current value of workingText
	 */
	public void updateActiveFramesDisplay() {
		// Attempt to parse the inputted integer value
		try {
			int frameIdx = Integer.parseInt(workingText) - 1;

			if (frameIdx >= -1 && frameIdx < 10) {
				// Update the appropriate active Frame index
				if (contents.getLineIdx() == 0) {
					armModel.setActiveToolFrame(frameIdx);
				} else {
					armModel.setActiveUserFrame(frameIdx);
				}

				updateCoordFrame();
			}

		} catch(NumberFormatException NFEx) {
			// Non-integer value
		}
		// Update display
		if (contents.getLineIdx() == 0) {
			workingText = Integer.toString(armModel.getActiveToolFrame() + 1);
		} else {
			workingText = Integer.toString(armModel.getActiveUserFrame() + 1);
		}

		contents.get(contents.getLineIdx()).set(contents.getColumnIdx(), workingText);
		updateScreen();
	}

	/**
	 * Updates the position and orientation of the Robot as well as all the World
	 * Objects associated with the current scenario. Updates the bounding box color,
	 * position and oientation of the Robot and all World Objects as well. Finally,
	 * all the World Objects and the Robot are drawn.
	 * 
	 * @param s       The currently active scenario
	 * @param active  The currently selected program
	 * @param model   The Robot Arm model
	 */
	public void updateAndDrawObjects(Scenario s, Program active, ArmModel model) {
		model.updateRobot(active);

		if (s != null) {
			s.resetObjectHitBoxColors();
		}

		model.resetOBBColors(); 
		model.checkSelfCollisions();

		if (s != null) {
			s.updateAndDrawObjects(model);
		}
		model.draw();

		model.updatePreviousEEOrientation();
	}

	/**
	 * Transition back to the World Frame, if the current Frame is Tool or User and there are no active frame
	 * set for that Coordinate Frame. This method will halt the motion of the Robot if the active frame is changed.
	 */
	public void updateCoordFrame() {

		// Return to the World Frame, if no User Frame is active
		if(armModel.getCurCoordFrame() == CoordFrame.TOOL && !(armModel.getActiveToolFrame() >= 0 && armModel.getActiveToolFrame() < Fields.FRAME_SIZE)) {
			armModel.setCurCoordFrame(CoordFrame.WORLD);
			// Stop Robot movement
			armModel.halt();
		}

		// Return to the World Frame, if no User Frame is active
		if(armModel.getCurCoordFrame() == CoordFrame.USER && !(armModel.getActiveUserFrame() >= 0 && armModel.getActiveUserFrame() < Fields.FRAME_SIZE)) {
			armModel.setCurCoordFrame(CoordFrame.WORLD);
			// Stop Robot movement
			armModel.halt();
		}
	}

	/**
	 * Deals with updating the UI after confirming/canceling a deletion
	 */
	public void updateInstructions() {
		int instSize = activeProgram().getInstructions().size();

		setActive_instr(min(getActive_instr(),  instSize - 1));
		contents.setLineIdx(min(getActive_instr(), ITEMS_TO_SHOW - 1));
		contents.setColumnIdx(0);
		
		lastScreen();
	}

	public void updateRobotJogMotion(int button, int direction) {
		// Only six jog button pairs exist
		if (button >= 0 && button < 6) {
			float newDir;

			if(armModel.getCurCoordFrame() == CoordFrame.JOINT) {
				// Move single joint
				newDir = activateLiveJointMotion(button, direction);
			} else {
				// Move entire robot in a single axis plane
				newDir = activateLiveWorldMotion(button, direction);
			}

			Button negButton = ((Button)cp5.get("JOINT" + (button + 1) + "_NEG")),
					posButton = ((Button)cp5.get("JOINT" + (button + 1) + "_POS"));

			if (newDir > 0) {
				// Positive motion
				negButton.setColorBackground(Fields.BUTTON_DEFAULT);
				posButton.setColorBackground(Fields.BUTTON_ACTIVE);
			} else if (newDir < 0) {
				// Negative motion
				negButton.setColorBackground(Fields.BUTTON_ACTIVE);
				posButton.setColorBackground(Fields.BUTTON_DEFAULT);
			} else {
				// No motion
				negButton.setColorBackground(Fields.BUTTON_DEFAULT);
				posButton.setColorBackground(Fields.BUTTON_DEFAULT);
			}
		}
	}

	// update text displayed on screen
	public void updateScreen() {
		int next_px = display_px;
		int next_py = display_py;
		//int txt, bg;

		clearScreen();

		// draw display background
		cp5.addTextarea("txt")
		.setPosition(display_px,display_py)
		.setSize(display_width, display_height)
		.setColorBackground(Fields.UI_LIGHT)
		.moveTo(g1);

		String header = null;
		// display the name of the program that is being edited
		header = getHeader(mode);

		if(header != null) {
			// Display header field
			cp5.addTextarea("header")
			.setText(" " + header)
			.setFont(fnt_con14)
			.setPosition(next_px, next_py)
			.setSize(display_width, 20)
			.setColorValue(Fields.UI_LIGHT)
			.setColorBackground(Fields.UI_DARK)
			.hideScrollbar()
			.show()
			.moveTo(g1);

			next_py += 20;
		}

		getContents(mode);
		getOptions(mode);
		
		if(contents.size() == 0) {
			options.setLocation(10, 20);
			options.setMaxDisplay(8);
		}
		else {
			options.setLocation(10, 200);
			options.setMaxDisplay(3);
		}
		
		contents.drawLines(mode);
		options.drawLines(mode);
		
		// display hints for function keys
		String[] funct;
		funct = getFunctionLabels(mode);

		//set f button text labels
		for(int i = 0; i < 5; i += 1) {
			cp5.addTextarea("lf"+i)
			.setText(funct[i])
			.setFont(fnt_con12)
			// Keep function labels in their original place
			.setPosition(display_width*i/5 + 15 , display_height - g1_py)
			.setSize(display_width/5 - 5, 20)
			.setColorValue(Fields.UI_DARK)
			.setColorBackground(Fields.UI_LIGHT)
			.hideScrollbar()
			.moveTo(g1);
		}
	} // end updateScreen()

	public void UpdateWldObj() {
		/* Confirm changes made to the orientation and
		 * position of the selected world object. */
		getManager().editWorldObject();
	}

	/**
	 * Convert a point based on a coordinate system defined as
	 * 3 orthonormal vectors. Reverse operation of vectorConvertTo.
	 * @param point Point to convert
	 * @param xAxis X axis of target coordinate system
	 * @param yAxis Y axis of target coordinate system
	 * @param zAxis Z axis of target coordinate system
	 * @return Coordinates of point after conversion
	 */
	public PVector vectorConvertFrom(PVector point, PVector xAxis,
			PVector yAxis, PVector zAxis)
	{
		PMatrix3D matrix = new PMatrix3D(xAxis.x, yAxis.x, zAxis.x, 0,
				xAxis.y, yAxis.y, zAxis.y, 0,
				xAxis.z, yAxis.z, zAxis.z, 0,
				0,       0,       0,       1);
		PVector result = new PVector();
		matrix.mult(point, result);
		return result;
	}
	
	/**
	 * Convert a point based on a coordinate system defined as
	 * 3 orthonormal vectors.
	 * @param point Point to convert
	 * @param xAxis X axis of target coordinate system
	 * @param yAxis Y axis of target coordinate system
	 * @param zAxis Z axis of target coordinate system
	 * @return Coordinates of point after conversion
	 */
	public PVector vectorConvertTo(PVector point, PVector xAxis,
			PVector yAxis, PVector zAxis)
	{
		PMatrix3D matrix = new PMatrix3D(xAxis.x, xAxis.y, xAxis.z, 0,
				yAxis.x, yAxis.y, yAxis.z, 0,
				zAxis.x, zAxis.y, zAxis.z, 0,
				0,       0,       0,       1);
		PVector result = new PVector();
		matrix.mult(point, result);
		return result;
	}
	
	/**
	 * Writes anything stored in the ArrayList String buffers to tmp\test.out.
	 */
	private int writeBuffer() {
		try {
			PrintWriter out = new PrintWriter(sketchPath("tmp/test.out"));

			for (String line : buffer) {
				out.print(line);
			}

			println("Write to buffer successful.");
			out.close();
		} catch(Exception Ex) {
			Ex.printStackTrace();
			return 1;
		}

		buffer.clear();
		return 0;
	}
}
