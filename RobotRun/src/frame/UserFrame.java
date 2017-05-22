package frame;
import geom.Point;
import geom.RMatrix;
import geom.RQuaternion;
import global.MyFloatFormat;
import global.RMath;
import processing.core.PVector;

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

	public Point getOrientOrigin() { return orientOrigin; }

	// Getter and Setters for the User frame's origin
	@Override
	public PVector getOrigin() { return origin; }

	@Override
	public Point getPoint(int idx) {

		/* Map the index into the 'Point array' to the
		 * actual values stored in the frame */
		switch (idx) {
		case 0:
		case 1:
		case 2:
			return super.getPoint(idx);

		case 3:
			return getOrientOrigin();

		default:
		}

		return null;
	}

	@Override
	public void reset() {
		orientationOffset = new RQuaternion();
		setPoint(null, 0);
		setPoint(null, 1);
		setPoint(null, 2);
		setPoint(null, 3);
		setDEOrigin(null);
		setDEOrientationOffset(null);
		origin = new PVector(0f, 0f, 0f);
		setOrientOrigin(null);
	}

	@Override
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
		} else if (mode >= 0 && mode < 2 && getPoint(0) != null && getPoint(1) != null && getPoint(2) != null) {
			// 3-Point or 4-Point Method

			PVector newOrigin = (mode == 0) ? getPoint(0).position : getOrientOrigin().position;
			RMatrix newAxesVectors = createAxesFromThreePoints(getPoint(0).position,
					getPoint(1).position,
					getPoint(2).position);

			if (newOrigin == null || newAxesVectors == null) {
				// Invalid points for the coordinate axes or missing orient origin for the 4-Point Method
				return false;
			}

			setOrientation( RMath.matrixToQuat(newAxesVectors) );
			setOrigin(newOrigin);
			return true;
		}

		return false;
	}

	public void setOrientOrigin(Point orientOrigin) {
		this.orientOrigin = orientOrigin;
	}
	public void setOrigin(PVector newOrigin) { origin = newOrigin; }

	@Override
	public void setPoint(Point p, int idx) {

		/* Map the index into the 'Point array' to the
		 * actual values stored in the frame */
		switch(idx) {
		case 0:
		case 1:
		case 2:
			super.setPoint(p, idx);
			return;

		case 3:
			setOrientOrigin(p);
			return;

		default:
		}
	}

	/**
	 * Returns a string array, where each entry is one of
	 * the User frame's origin or orientation values:
	 * (X, Y, Z, W, P, and R) and their respective labels.
	 *
	 * @return  A 6-element String array
	 */
	@Override
	public String[] toStringArray() {
		String[] values = new String[6];
		
		// Convert to world frame reference
		PVector displayOrigin = RMath.vToWorld(origin);
		/* Convert orientation in to euler angles, in degree, with reference
		 * to the world frame */
		PVector wpr = RMath.nQuatToWEuler(orientationOffset);

		values[0] = MyFloatFormat.format(displayOrigin.x);
		values[1] = MyFloatFormat.format(displayOrigin.y);
		values[2] = MyFloatFormat.format( displayOrigin.z);
		values[3] = MyFloatFormat.format(wpr.x);
		values[4] = MyFloatFormat.format(wpr.y);
		values[5] = MyFloatFormat.format(wpr.z);

		return values;
	}
}