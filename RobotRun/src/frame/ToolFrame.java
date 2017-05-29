package frame;
import geom.Point;
import geom.RMatrix;
import geom.RQuaternion;
import global.Fields;
import global.MyFloatFormat;
import global.RMath;
import processing.core.PVector;

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
		TCPTeachPoints = new Point[] { null, null, null };
	}
	
	/**
	 * Returns the value of the frame's orientation offset.
	 * 
	 * @return	The offset of the Robot's orientation associated with the tool
	 * 			frame
	 */
	public RQuaternion getOrientationOffset() {
		return orientationOffset;
	}

	@Override
	public Point getPoint(int idx) {

		/* Map the index into the 'Point array' to the
		 * actual values stored in the frame */
		switch (idx) {
		case 0:
		case 1:
		case 2:
			return TCPTeachPoints[idx];

		case 3:
		case 4:
		case 5:
			return super.getPoint(idx % 3);

		default:
		}

		return null;
	}
	
	public PVector getTCPOffset() { return TCPOffset; }
	
	@Override
	public void reset() {
		orientationOffset = new RQuaternion();
		setPoint(null, 0);
		setPoint(null, 1);
		setPoint(null, 2);
		setPoint(null, 3);
		setPoint(null, 4);
		setPoint(null, 5);
		setDEOrigin(null);
		setDEOrientationOffset(null);
		TCPOffset = new PVector(0f, 0f, 0f);
		TCPTeachPoints = new Point[] { null, null, null };
	}

	@Override
	public boolean setFrame(int method) {

		if (method == 2) {
			// Direct Entry Method

			if (getDEOrigin() == null || getDEOrientationOffset() == null) {
				// No direct entry values have been set
				return false;
			}

			setTCPOffset(getDEOrigin().copy());
			setOrientation(getDEOrientationOffset().clone());
			return true;
			
		} else if (method >= 0 && method < 2 && TCPTeachPoints[0] != null && TCPTeachPoints[1] != null && TCPTeachPoints[2] != null) {
			// 3-Point or 6-Point Method

			if (method == 1 && (super.getPoint(0) == null || super.getPoint(1) == null || super.getPoint(2) == null)) {
				// Missing points for the coordinate axes
				return false;
			}

			RMatrix pt1_ori = TCPTeachPoints[0].orientation.toMatrix(),
					pt2_ori = TCPTeachPoints[1].orientation.toMatrix(),
					pt3_ori = TCPTeachPoints[2].orientation.toMatrix();

			double[] newTCP = calculateTCPFromThreePoints(TCPTeachPoints[0].position, pt1_ori,
					TCPTeachPoints[1].position, pt2_ori,
					TCPTeachPoints[2].position, pt3_ori);

			RMatrix newAxesVectors = (method == 1) ? createAxesFromThreePoints(super.getPoint(0).position,
					super.getPoint(1).position,
					super.getPoint(2).position)
					: Fields.IDENTITY_MAT.copy();

					if (newTCP == null || newAxesVectors == null) {
						// Invalid point set for the TCP or the coordinate axes
						return false;
					}

					setTCPOffset( new PVector((float)newTCP[0], (float)newTCP[1], (float)newTCP[2]) );
					setOrientation( RMath.matrixToQuat(newAxesVectors) );
					return true;
		}

		return false;
	}

	@Override
	public void setPoint(Point p, int idx) {

		/* Map the index into the 'Point array' to the
		 * actual values stored in the frame */
		switch (idx) {
			case 0:
			case 1:
			case 2:
				TCPTeachPoints[idx] = p;
				return;
	
			case 3:
			case 4:
			case 5:
				super.setPoint(p, idx % 3);
				return;
	
			default:
		}
	}

	public void setTCPOffset(PVector newOffset) {
		TCPOffset = newOffset;
	}
	
	/**
	 * Returns a string array, where each entry is one of
	 * the Tool frame's TCP offset or orientation values:
	 * (X, Y, Z, W, P, and R) and their respective labels.
	 *
	 * @return  A 6-element String array
	 */
	@Override
	public String[] toStringArray() {
		String[] values = new String[6];

		PVector displayOffset;
		/* Convert orientation in to euler angles, in degree, with reference
		 * to the world frame */
		PVector wpr = RMath.nQuatToWEuler(orientationOffset);

		displayOffset = getTCPOffset();

		values[0] = MyFloatFormat.format(displayOffset.x);
		values[1] = MyFloatFormat.format(displayOffset.y);
		values[2] = MyFloatFormat.format(displayOffset.z);
		values[3] = MyFloatFormat.format(wpr.x);
		values[4] = MyFloatFormat.format(wpr.y);
		values[5] = MyFloatFormat.format(wpr.z);

		return values;
	}
}