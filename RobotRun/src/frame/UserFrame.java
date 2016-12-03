package frame;
import robot.RQuaternion;
import geom.Point;
import processing.core.PVector;
import robot.RobotRun;

public class UserFrame extends Frame {
	private PVector origin;
	// For the 4-Point Method
	private Point orientOrigin;

	/**
	 * Initialize all fields
	 */
	public UserFrame() {
		super();
		origin = new PVector(0f, 0f, 0f);
		setOrientOrigin(null);
	}

	@Override
	public RQuaternion getOrientation() { return orientationOffset; }

	public void setPoint(Point p, int idx) {

		/* Map the index into the 'Point array' to the
		 * actual values stored in the frame */
		switch(idx) {
		case 0:
		case 1:
		case 2:
			getAxesTeachPoints()[idx] = p;
			return;

		case 3:
			setOrientOrigin(p);
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
			return getAxesTeachPoints()[idx];

		case 3:
			return getOrientOrigin();

		default:
		}

		return null;
	}

	public boolean setFrame(int mode) {

		if (mode == 2) {
			// Direct Entry Method

			if (getDEOrigin() == null || getDEOrientationOffset() == null) {
				// No direct entry values have been set
				return false;
			}

			setOrigin(getDEOrigin());
			setOrientation( (RQuaternion)getDEOrientationOffset().clone() );
			return true;
		} else if (mode >= 0 && mode < 2 && getAxesTeachPoints()[0] != null && getAxesTeachPoints()[1] != null && getAxesTeachPoints()[2] != null) {
			// 3-Point or 4-Point Method

			PVector newOrigin = (mode == 0) ? getAxesTeachPoints()[0].position : getOrientOrigin().position;
			float[][] newAxesVectors = createAxesFromThreePoints(getAxesTeachPoints()[0].position,
					getAxesTeachPoints()[1].position,
					getAxesTeachPoints()[2].position);

			if (newOrigin == null || newAxesVectors == null) {
				// Invalid points for the coordinate axes or missing orient origin for the 4-Point Method
				return false;
			}

			setOrientation( RobotRun.getInstance().matrixToQuat(newAxesVectors) );
			setOrigin(newOrigin);
			return true;
		}

		return false;
	}

	/**
	 * Returns a string array, where each entry is one of
	 * the User frame's origin or orientation values:
	 * (X, Y, Z, W, P, and R) and their respective labels.
	 *
	 * @return  A 6-element String array
	 */
	public String[] toStringArray() {

		String[] values = new String[6];

		PVector displayOrigin;
		// Convert angles to degrees and to the World Coordinate Frame
		PVector wpr = RobotRun.getInstance().quatToEuler(orientationOffset).mult(RobotRun.RAD_TO_DEG);

		// Convert to World frame reference
		displayOrigin = RobotRun.getInstance().convertNativeToWorld(origin);

		values[0] = String.format("X: %4.3f", displayOrigin.x);
		values[1] = String.format("Y: %4.3f", displayOrigin.y);
		values[2] = String.format("Z: %4.3f", displayOrigin.z);
		// Display angles in terms of the World frame
		values[3] = String.format("W: %4.3f", -wpr.x);
		values[4] = String.format("P: %4.3f", -wpr.z);
		values[5] = String.format("R: %4.3f", wpr.y);

		return values;
	}

	// Getter and Setters for the User frame's origin
	public PVector getOrigin() { return origin; }
	public void setOrigin(PVector newOrigin) { origin = newOrigin; }

	public Point getOrientOrigin() {
		return orientOrigin;
	}

	public void setOrientOrigin(Point orientOrigin) {
		this.orientOrigin = orientOrigin;
	}
}