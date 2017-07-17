package frame;
import geom.Point;
import geom.RMatrix;
import geom.RQuaternion;
import global.Fields;
import global.MyFloatFormat;
import global.RMath;
import processing.core.PVector;

/**
 * TODO general comments
 * 
 * @author Joshua Hooker
 */
public class UserFrame implements Frame {
	
	private String name;
	
	private PVector originOffset;
	private RQuaternion orienOffset;
	
	private Point[] teachPoints;
	
	private PVector originDirect;
	private RQuaternion orienDirect;

	/**
	 * Initialize all fields to their default values.
	 */
	public UserFrame() {
		name = "";
		originOffset = new PVector(0f, 0f, 0f);
		orienOffset = new RQuaternion();
		teachPoints = new Point[] { null, null, null, null };
		originDirect = new PVector(0f, 0f, 0f);
		orienDirect = new RQuaternion();
	}
	
	/**
	 * Similar to toStringArray, however, it converts the Frame's direct entry
	 * values instead of the current origin and axes of the Frame.
	 * 
	 * @returning  A 6x2-element String array
	 */
	public String[][] directEntryStringArray() {
		String[][] entries = new String[6][2];
		PVector originDirect = getOriginDirect();
		RQuaternion orienDirect = getOrienDirect();
		PVector xyz, wpr;

		if (originDirect == null) {
			xyz = new PVector(0f, 0f, 0f);
			
		} else {
			// Use previous value if it exists
			xyz = RMath.vToWorld(originDirect);
		}

		if (orienDirect == null) {
			wpr = new PVector(0f, 0f, 0f);
			
		} else {
			// Display in degrees
			wpr = RMath.nQuatToWEuler(orienDirect);
		}

		entries[0][0] = "X: ";
		entries[0][1] = String.format("%4.3f", xyz.x);
		entries[1][0] = "Y: ";
		entries[1][1] = String.format("%4.3f", xyz.y);
		entries[2][0] = "Z: ";
		entries[2][1] = String.format("%4.3f", xyz.z);
		// Display in terms of the World frame
		entries[3][0] = "W: ";
		entries[3][1] = String.format("%4.3f", wpr.x);
		entries[4][0] = "P: ";
		entries[4][1] = String.format("%4.3f", wpr.y);
		entries[5][0] = "R: ";
		entries[5][1] = String.format("%4.3f", wpr.z);

		return entries;
	}
	
	/**
	 * TODO comment this
	 * 
	 * @return
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * TODO comment this
	 * 
	 * @return
	 */
	public RMatrix getNativeAxisVectors() {
		return orienOffset.toMatrix();
	}
	
	/**
	 * TODO comment this
	 * 
	 * @return
	 */
	public RQuaternion getOrienDirect() {
		return orienDirect;
	}
	
	/**
	 * TODO comment this
	 * 
	 * @return
	 */
	public RQuaternion getOrientation() {
		return orienOffset;
	}
	
	/**
	 * TODO comment this
	 * 
	 * @return
	 */
	public PVector getOriginDirect() {
		return originDirect;
	}
	
	/**
	 * TODO comment this
	 * 
	 * @return
	 */
	public PVector getOrigin() {
		return originOffset;
	}

	/**
	 * TODO comment this
	 * 
	 * @param idx
	 * @return
	 */
	public Point getPoint(int idx) {

		if (idx >= 0 && idx < teachPoints.length) {
			return teachPoints[idx];
		}

		return null;
	}
	
	/**
	 * TODO comment this
	 * 
	 * @return
	 */
	public boolean is3PtComplete() {
		// Check if all points are taught for the three point method
		return teachPoints[0] != null && teachPoints[1] != null &&
				teachPoints[2] != null;
	}
	
	/**
	 * TODO comment this
	 * 
	 * @return
	 */
	public boolean is4PtComplete() {
		// Check if all points are taught for the fourth point method
		return teachPoints[0] != null && teachPoints[1] != null &&
				teachPoints[2] != null && teachPoints[3] != null;
	}

	/**
	 * TODO comment this
	 */
	public void reset() {
		name = "";
		originOffset.x = 0f;
		originOffset.y = 0f;
		originOffset.z = 0f;
		orienOffset.setValue(0, 1f);
		orienOffset.setValue(1, 0f);
		orienOffset.setValue(2, 0f);
		orienOffset.setValue(3, 0f);
		setPoint(null, 0);
		setPoint(null, 1);
		setPoint(null, 2);
		setPoint(null, 3);
		originDirect.x = 0f;
		originDirect.y = 0f;
		originDirect.z = 0f;
		orienDirect.setValue(0, 1f);
		orienDirect.setValue(1, 0f);
		orienDirect.setValue(2, 0f);
		orienDirect.setValue(3, 0f);
	}
	
	/**
	 * TODO comment this
	 * 
	 * @param newName
	 */
	public void setName(String newName) {
		name = newName;
	}
	
	/**
	 * TODO comment this
	 * 
	 * @param newOrien
	 */
	public void setOrienDirect(RQuaternion newOrien) {
		orienDirect = newOrien;
	}
	
	/**
	 * TODO comment this
	 * 
	 * @param newOrien
	 */
	public void setOrienOffset(RQuaternion newOrien) {
		orienOffset = newOrien;
	}
	
	/**
	 * TODO comment this
	 * 
	 * @param newOrigin
	 */
	public void setOriginDirect(PVector newOrigin) {
		originDirect = newOrigin;
	}
	
	/**
	 * TODO comment this
	 * 
	 * @param newOrigin
	 */
	public void setOrigin(PVector newOrigin) {
		originOffset = newOrigin;
	}

	/**
	 * TODO comment this
	 * 
	 * @param p
	 * @param idx
	 */
	public void setPoint(Point p, int idx) {
		if (idx >= 0 && idx < teachPoints.length) {
			teachPoints[idx] = p;
		}
	}
	
	/**
	 * TODO comment this
	 * 
	 * @return
	 */
	public boolean teach3Pt() {
		if (is3PtComplete()) {
			Point pt0 = getPoint(0);
			Point pt1 = getPoint(1);
			Point pt2 = getPoint(2);
			// Form the orientation offset from the taught points
			RMatrix axesOffset = Fields.createAxesFromThreePoints(pt0.position,
					pt1.position, pt2.position);
			
			if (axesOffset != null) {
				// Set frame offsets
				setOrigin(pt0.position);
				setOrienOffset( RMath.matrixToQuat(axesOffset) );
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * TODO comment this
	 * 
	 * @return
	 */
	public boolean teach4Pt() {
		if (is4PtComplete()) {
			Point pt0 = getPoint(0);
			Point pt1 = getPoint(1);
			Point pt2 = getPoint(2);
			// Form the orientation offset from the taught points
			RMatrix axesOffset = Fields.createAxesFromThreePoints(pt0.position,
					pt1.position, pt2.position);
			
			if (axesOffset != null) {
				// Set frame offsets
				setOrigin(getPoint(4).position);
				setOrienOffset( RMath.matrixToQuat(axesOffset) );
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * TODO comment this
	 * 
	 * @return
	 */
	public boolean teachDirectEntry() {
		if (originDirect != null && orienDirect != null) {
			// Set frame offsets
			setOrigin(getOriginDirect().copy());
			setOrienOffset(getOrienDirect().clone());
			return true;
		}
		
		return false;
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
	 * the User frame's origin or orientation values:
	 * (X, Y, Z, W, P, and R) and their respective labels.
	 *
	 * @return  A 6-element String array
	 */
	public String[] toStringArray() {
		String[] values = new String[6];
		
		// Convert to world frame reference
		PVector displayOrigin = RMath.vToWorld(originOffset);
		/* Convert orientation in to euler angles, in degree, with reference
		 * to the world frame */
		PVector wpr = RMath.nQuatToWEuler(orienOffset);

		values[0] = MyFloatFormat.format(displayOrigin.x);
		values[1] = MyFloatFormat.format(displayOrigin.y);
		values[2] = MyFloatFormat.format( displayOrigin.z);
		values[3] = MyFloatFormat.format(wpr.x);
		values[4] = MyFloatFormat.format(wpr.y);
		values[5] = MyFloatFormat.format(wpr.z);

		return values;
	}
}
