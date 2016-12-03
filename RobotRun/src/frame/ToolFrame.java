package frame;
import robot.RQuaternion;
import geom.Point;
import processing.core.PVector;
import robot.RobotRun;

public class ToolFrame extends Frame {
	// The TCP offset associated with this frame
	private PVector TCPOffset;
	// For 3-Point and Six-Point Methods
	private Point[] TCPTeachPoints;

	/**
	 * Initialize all fields
	 */
	public ToolFrame() {
		super();
		TCPOffset = new PVector(0f, 0f, 0f);
		setTCPTeachPoints(new Point[] { null, null, null });
	}

	@Override
	public RQuaternion getOrientation() {
		RQuaternion robotOrientation = RobotRun.getInstance().nativeRobotPoint(RobotRun.getInstance().getArmModel().getJointAngles()).orientation;
		// Tool frame axes orientation = (orientation offset x Model default orientation ^ -1) x Model current orientation
		return RQuaternion.mult(RobotRun.getInstance().getArmModel().DEFAULT_ORIENTATION.transformQuaternion(orientationOffset), robotOrientation);
	}

	public void setPoint(Point p, int idx) {

		/* Map the index into the 'Point array' to the
		 * actual values stored in the frame */
		switch (idx) {
		case 0:
		case 1:
		case 2:
			getTCPTeachPoints()[idx] = p;
			return;

		case 3:
		case 4:
		case 5:
			getAxesTeachPoints()[ idx % 3 ] = p;
			return;

		default:
		}
	}

	public Point getPoint(int idx) {

		/* Map the index into the 'Point array' to the
		 * actual values stored in the frame */
		switch (idx) {
		case 0:
		case 1:
		case 2:
			return getTCPTeachPoints()[idx];

		case 3:
		case 4:
		case 5:
			return getAxesTeachPoints()[ idx % 3 ];

		default:
		}

		return null;
	}

	public boolean setFrame(int method) {

		if (method == 2) {
			// Direct Entry Method

			if (getDEOrigin() == null || getDEOrientationOffset() == null) {
				// No direct entry values have been set
				return false;
			}

			setTCPOffset(getDEOrigin());
			setOrientation( (RQuaternion)getDEOrientationOffset().clone() );
			return true;
		} else if (method >= 0 && method < 2 && getTCPTeachPoints()[0] != null && getTCPTeachPoints()[1] != null && getTCPTeachPoints()[2] != null) {
			// 3-Point or 6-Point Method

			if (method == 1 && (getAxesTeachPoints()[0] == null || getAxesTeachPoints()[1] == null || getAxesTeachPoints()[2] == null)) {
				// Missing points for the coordinate axes
				return false;
			}

			float[][] pt1_ori = getTCPTeachPoints()[0].orientation.toMatrix(),
					pt2_ori = getTCPTeachPoints()[1].orientation.toMatrix(),
					pt3_ori = getTCPTeachPoints()[2].orientation.toMatrix();

			double[] newTCP = calculateTCPFromThreePoints(getTCPTeachPoints()[0].position, pt1_ori,
					getTCPTeachPoints()[1].position, pt2_ori,
					getTCPTeachPoints()[2].position, pt3_ori);

			float[][] newAxesVectors = (method == 1) ? createAxesFromThreePoints(getAxesTeachPoints()[0].position,
					getAxesTeachPoints()[1].position,
					getAxesTeachPoints()[2].position)
					: new float[][] { {1, 0, 0}, {0, 1, 0}, {0, 0, 1} };

					if (newTCP == null || newAxesVectors == null) {
						// Invalid point set for the TCP or the coordinate axes
						return false;
					}

					setTCPOffset( new PVector((float)newTCP[0], (float)newTCP[1], (float)newTCP[2]) );
					setOrientation( RobotRun.getInstance().matrixToQuat(newAxesVectors) );
					return true;
		}

		return false;
	}

	/**
	 * Returns a string array, where each entry is one of
	 * the Tool frame's TCP offset or orientation values:
	 * (X, Y, Z, W, P, and R) and their respective labels.
	 *
	 * @return  A 6-element String array
	 */
	public String[] toStringArray() {

		String[] values = new String[6];

		PVector displayOffset;
		// Convert angles to degrees
		PVector wpr = RobotRun.getInstance().quatToEuler(orientationOffset).mult(RobotRun.RAD_TO_DEG);

		displayOffset = getTCPOffset();

		values[0] = String.format("X: %4.3f", displayOffset.x);
		values[1] = String.format("Y: %4.3f", displayOffset.y);
		values[2] = String.format("Z: %4.3f", displayOffset.z);
		// Display angles in terms of the World frame
		values[3] = String.format("W: %4.3f", -wpr.x);
		values[4] = String.format("P: %4.3f", -wpr.z);
		values[5] = String.format("R: %4.3f", wpr.y);

		return values;
	}

	/**
	 * Tool Frames have no origin offset.
	 */
	public PVector getOrigin() { return new PVector(0f, 0f, 0f); }

	// Getter and Setter for TCP offset value
	public void setTCPOffset(PVector newOffset) { TCPOffset = newOffset; }
	public PVector getTCPOffset() { return TCPOffset; }

	public Point[] getTCPTeachPoints() {
		return TCPTeachPoints;
	}

	public void setTCPTeachPoints(Point[] tCPTeachPoints) {
		TCPTeachPoints = tCPTeachPoints;
	}
}