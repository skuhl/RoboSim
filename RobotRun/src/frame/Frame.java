package frame;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.linear.SingularValueDecomposition;

import geom.Point;
import geom.RMath;
import geom.RQuaternion;
import global.Fields;
import processing.core.PApplet;
import processing.core.PVector;
import robot.RobotRun;

public abstract class Frame {
	// The orientation of the frame in the form of a unit quaternion
	protected RQuaternion orientationOffset;
	/* The three points used to define a coordinate axis for 6-Point Method
	 * of Tool Frames and 3-Point or 4_Point Methods of User Frames */
	private Point[] axesTeachPoints;
	// For Direct Entry
	private PVector DEOrigin;
	private RQuaternion DEOrientationOffset;

	public Frame() {
		orientationOffset = new RQuaternion();
		axesTeachPoints = new Point[] { null, null, null };
		DEOrigin = null;
		DEOrientationOffset = null;
	}
	
	/**
	 * This method calculates a TCP offset for the Robot given a valid set of position and orientation values, where each pair ( [pos, ori1],
	 * [pos2, ori2], and [pos3, ori3] ) represent a recorded position and orientation of the Robot. A position contains the X, Y, Z values of
	 * the Robot at the point, while the orientation matrix is a rotation matrix, which describes the Robot's orientation at a one of the points.
	 * Do to the nature of this algorithm, an average TCP value is calculated from three separate calculations.
	 *
	 * @param pos1  The X, Y, Z position of the Robot's faceplate at first point
	 * @param ori1  The orientation of the Robot at the first point
	 * @param pos2  The X, Y, Z position of the Robot's faceplate at the second point
	 * @param ori2  The orientation of the Robot at the second point
	 * @param pos3  the X, Y, Z position of the Robot's faceplate at the third point
	 * @param ori3  The orientation of the Robot at the third point
	 * @return      The new TCP for the Robot, null is returned if the given points
	 *              are invalid
	 */
	public double[] calculateTCPFromThreePoints(PVector pos1, float[][] ori1, 
			PVector pos2, float[][] ori2, 
			PVector pos3, float[][] ori3) {

		RealVector avg_TCP = new ArrayRealVector(new double[] {0.0f, 0.0f, 0.0f} , false);
		int counter = 3;

		while (counter-- > 0) {

			RealMatrix Ar = null, Br = null, Cr = null;
			PVector vt = null;

			if (counter == 0) {
				/* Case 3: C = point 1 */
				Ar = new Array2DRowRealMatrix(RMath.floatToDouble(ori2, 3, 3));
				Br = new Array2DRowRealMatrix(RMath.floatToDouble(ori3, 3, 3));
				Cr = new Array2DRowRealMatrix(RMath.floatToDouble(ori1, 3, 3));
				/* 2Ct - At - Bt */
				vt = PVector.sub(PVector.mult(pos1, 2), PVector.add(pos2, pos3));

			} else if (counter == 1) {
				/* Case 2: C = point 2 */
				Ar = new Array2DRowRealMatrix(RMath.floatToDouble(ori3, 3, 3));
				Br = new Array2DRowRealMatrix(RMath.floatToDouble(ori1, 3, 3));
				Cr = new Array2DRowRealMatrix(RMath.floatToDouble(ori2, 3, 3));
				/* 2Ct - At - Bt */
				vt = PVector.sub(PVector.mult(pos2, 2), PVector.add(pos3, pos1));

			} else if (counter == 2) {
				/* Case 1: C = point 3 */
				Ar = new Array2DRowRealMatrix(RMath.floatToDouble(ori1, 3, 3));
				Br = new Array2DRowRealMatrix(RMath.floatToDouble(ori2, 3, 3));
				Cr = new Array2DRowRealMatrix(RMath.floatToDouble(ori3, 3, 3));
				/* 2Ct - At - Bt */
				vt = PVector.sub(PVector.mult(pos3, 2), PVector.add(pos1, pos2));

			}

			/****************************************************************
				Three Point Method Calculation
				
				------------------------------------------------------------
				A, B, C      transformation matrices
				Ar, Br, Cr   rotational portions of A, B, C respectively
				At, Bt, Ct   translational portions of A, B, C repectively
				x            TCP point with respect to the EE
				------------------------------------------------------------
				
				Ax = Bx = Cx
				Ax = (Ar)x + At
				
				(A - B)x = 0
				(Ar - Br)x + At - Bt = 0
				
				Ax + Bx - 2Cx = 0
				(Ar + Br - 2Cr)x + At + Bt - 2Ct = 0
				(Ar + Br - 2Cr)x = 2Ct - At - Bt
				x = (Ar + Br - 2Cr) ^ -1 * (2Ct - At - Bt)

			 ****************************************************************/

			RealVector b = new ArrayRealVector(new double[] { vt.x, vt.y, vt.z }, false);
			/* Ar + Br - 2Cr */
			RealMatrix R = ( ( Ar.add(Br) ).subtract( Cr.scalarMultiply(2) ) ).transpose();

			/* (R ^ -1) * b */
			avg_TCP = avg_TCP.add( (new SingularValueDecomposition(R)).getSolver().getInverse().operate(b) );

			if (Fields.DEBUG) {
				System.out.printf("\n%s\n\n", RobotRun.getInstance().matrixToString( RMath.doubleToFloat(R.getData(), 3, 3) ));
			}
		}

		/* Take the average of the three cases: where C = the first point, the second point, and the third point */
		avg_TCP = avg_TCP.mapMultiply( 1.0f / 3.0f );

		if(Fields.DEBUG) {
			System.out.printf("(Ar + Br - 2Cr) ^ -1 * (2Ct - At - Bt):\n\n[%5.4f]\n[%5.4f]\n[%5.4f]\n\n", avg_TCP.getEntry(0), avg_TCP.getEntry(1), avg_TCP.getEntry(2));
		}

		for(int idx = 0; idx < avg_TCP.getDimension(); ++idx) {
			// Extremely high values may indicate that the given points are invalid
			if(PApplet.abs((float)avg_TCP.getEntry(idx)) > 1000.0f) {
				return null;
			}
		}

		return avg_TCP.toArray();
	}

	/**
	 * Creates a 3x3 rotation matrix based off of two vectors defined by the
	 * given set of three points, which are defined by the three given PVectors.
	 * The three points are used to form two vectors. The first vector is treated
	 * as the positive x-axis and the second one is the psuedo-positive y-axis.
	 * These vectors are crossed to form the z-axis. The x-axis is then crossed
	 * with the positive z-axis to form the true y-axis. Finally, the axes are
	 * converted from the World frame reference, to Native Coordinates.
	 *
	 * @param p1      the origin reference point used to form the positive x-
	 *                and y-axes
	 * @param p2      the point used to create the preliminary positive x-axis
	 * @param p3      the point used to create the preliminary positive y-axis
	 * @return        a set of three unit vectors that represent an axes (row
	 *                major order)
	 */
	public float[][] createAxesFromThreePoints(PVector p1, PVector p2, PVector p3) {
		float[][] axesRefWorld = new float[3][3];
		PVector xAxis = PVector.sub(p2, p1);
		PVector yAxis = PVector.sub(p3, p1);
		PVector zAxis = yAxis.cross(xAxis);

		yAxis = xAxis.cross(zAxis);
		// Create unit vectors
		xAxis.normalize();
		yAxis.normalize();
		zAxis.normalize();

		if ((xAxis.x == 0f && xAxis.y == 0f && xAxis.z == 0f) ||
				(yAxis.x == 0f && yAxis.y == 0f && yAxis.z == 0f) ||
				(zAxis.x == 0f && zAxis.y == 0f && zAxis.z == 0f)) {
			// One of the three axis vectors is the zero vector
			return null;
		}

		axesRefWorld[0][0] = xAxis.x;
		axesRefWorld[0][1] = xAxis.y;
		axesRefWorld[0][2] = xAxis.z;
		axesRefWorld[1][0] = yAxis.x;
		axesRefWorld[1][1] = yAxis.y;
		axesRefWorld[1][2] = yAxis.z;
		axesRefWorld[2][0] = zAxis.x;
		axesRefWorld[2][1] = zAxis.y;
		axesRefWorld[2][2] = zAxis.z;

		RealMatrix axes = new Array2DRowRealMatrix(RMath.floatToDouble(axesRefWorld, 3, 3)),
				invWorldAxes =  new Array2DRowRealMatrix(RMath.floatToDouble(Fields.NATIVE_AXES, 3, 3));
		// Remove the World frame transformation from the axes vectors
		return RMath.doubleToFloat(invWorldAxes.multiply(axes).getData(), 3, 3);
	}

	/**
	 * Similiar to toStringArray, however, it converts the Frame's direct entry
	 * values instead of the current origin and axes of the Frame.
	 * 
	 * @returning  A 6x2-element String array
	 */
	public String[][] directEntryStringArray() {
		String[][] entries = new String[6][2];
		PVector xyz, wpr;

		if (getDEOrigin() == null) {
			xyz = new PVector(0f, 0f, 0f);
		} else {
			// Use previous value if it exists
			if (this instanceof UserFrame) {
				xyz = RMath.vToWorld(getDEOrigin());
			} else {
				// Tool Frame origins are an offset of the Robot's End Effector
				xyz = getDEOrigin();
			}
		}

		if (getDEOrientationOffset() == null) {
			wpr = new PVector(0f, 0f, 0f);
		} else {
			// Display in degrees
			wpr = RMath.quatToEuler(getDEOrientationOffset()).mult(RobotRun.RAD_TO_DEG);
		}

		entries[0][0] = "X: ";
		entries[0][1] = String.format("%4.3f", xyz.x);
		entries[1][0] = "Y: ";
		entries[1][1] = String.format("%4.3f", xyz.y);
		entries[2][0] = "Z: ";
		entries[2][1] = String.format("%4.3f", xyz.z);
		// Display in terms of the World frame
		entries[3][0] = "W: ";
		entries[3][1] = String.format("%4.3f", -wpr.x);
		entries[4][0] = "P: ";
		entries[4][1] = String.format("%4.3f", wpr.z);
		entries[5][0] = "R: ";
		entries[5][1] = String.format("%4.3f", -wpr.y);

		return entries;
	}
	
	public RQuaternion getDEOrientationOffset() {
		return DEOrientationOffset;
	}

	public PVector getDEOrigin() {
		return DEOrigin;
	}

	/* Returns a set of axes unit vectors representing the axes
	 * of the frame in reference to the Native Coordinate System. */
	public float[][] getNativeAxisVectors() { return getOrientation().toMatrix(); }

	/**
	 * Returns the orientation of the axes for this frame.
	 */
	public abstract RQuaternion getOrientation();

	/**
	 * Return the origin of this Frame's coordinate System.
	 */
	public abstract PVector getOrigin();

	/**
	 * Returns the position of the teach point at the given index in the Frame's list
	 * of teach points. Valid indices are described in setPoint().
	 */
	public Point getPoint(int idx) {
		/* Map the index into the 'Point array' to the
		 * actual values stored in the frame */
		switch (idx) {
			case 0:
			case 1:
			case 2:
				return axesTeachPoints[ idx ];
				
			default:
				return null;
		}
	}

	/**
	 * Resets all the fields of the frame to their default values.
	 */
	public abstract void reset();

	public void setDEOrientationOffset(RQuaternion dEOrientationOffset) {
		DEOrientationOffset = dEOrientationOffset;
	}

	public void setDEOrigin(PVector dEOrigin) {
		DEOrigin = dEOrigin;
	}

	/**
	 * Based on value of method, an attempt will be made to set the current origin offset and axes vectors.
	 * For the value of method:
	 * 0 -> 3-Point Method
	 * 1 -> 6-Point Methd for Tool Frames or 4-Point Method for User Frames
	 * 2 -> Direct Entry Method
	 * 
	 * Assuming that all the correct fields have been initialized to values, which will produce valid output,
	 * for a given method, then the Frames origin offset and axes vectors are modified to fit the given method.
	 * For Tool Frames, the 3-Point method uses the TCPTeacchPoints list exclusively to constuct the TCP offset;
	 * the 6-Point Method uses both the TCPTeachPoints list and the axesTeachPoints list. For User Frames, the
	 * 3-Point uses the axesTeachPoints list exculsively; the 4-Point Method uses both the axesTeachPoint list
	 * as well as the orientOrigin point. For either Tool or User Frames, the Direct Entry Method uses the
	 * DEOrigin opint and the DEAxesOffsets array.
	 * 
	 * @param method  an integer between 0-2, which determines, which teaching method will be used to contruct
	 *                the Frame
	 * @returning     If the Frame was set or not; it is possible that some of the fields are not initialized or
	 *                will not produce valid output.
	 */
	public abstract boolean setFrame(int method);

	public void setOrientation(RQuaternion newAxes) {
		orientationOffset = newAxes;
	}

	/**
	 * Sets the Frame's point at the given index in its list of taugh points.
	 * For a Tool Frame, the list of possible taught points includes the three
	 * points for the teaching of the TCP offset as well as the three points
	 * for the Coordinate Axes; six points in total. The valid values for indices
	 * are as follows:
	 * 
	 * 0 -> TCP teach point 1
	 * 1 -> TCP teach point 2
	 * 2 -> TCP teach point 3
	 * 3 -> Orient Origin point
	 * 4 -> X-Direction point
	 * 5 -> Y-Direction point
	 * 
	 * For a User Frame, the list of possible taugh points includes the three
	 * points for the Coordinate Axes as well as the Axes Origin point; four
	 * in total. The valid values for index are as follows:
	 * 
	 * 0 -> Orient Origin point
	 * 1 -> X-Direction point
	 * 2 -> Y-Direction point
	 * 3 -> Axes Origin point
	 * 
	 * Because the orientation of a point is only necessary for the creation of the
	 * TCP offset, the orientation of the given point will be ignored for all other
	 * points aside from the TCP teach points and only the position of the point will
	 * be recorded.
	 * 
	 * @param p    A point, which contains the position and orientation of the Robot
	 *             at a specific point in space
	 * @param idx  The index, at which to save the given point, in the Frame's list
	 *             of taught points
	 */
	public void setPoint(Point p, int idx) {
		/* Map the index into the 'Point array' to the
		 * actual values stored in the frame */
		switch (idx) {
			case 0:
			case 1:
			case 2:
				axesTeachPoints[ idx % 3 ] = p;
				return;
	
			default:
		}
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
	public String[] toLineStringArray() {
		String[] entries = toStringArray();
		String[] line = new String[2];
		// X, Y, Z with space buffers
		line[0] = String.format("%-12s %-12s %s", entries[0], entries[1], entries[2]);
		// W, P, R with space buffers
		line[1] = String.format("%-12s %-12s %s", entries[3], entries[4], entries[5]);

		return line;
	}

	/**
	 * Returns a string array, where each entry is one of
	 * the Frames six Cartesian values: (X, Y, Z, W, P,
	 * and R) and their respective labels.
	 *
	 * @return  A 6-element String array
	 */
	public abstract String[] toStringArray();
}
