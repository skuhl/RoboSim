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
public class ToolFrame {
	
	private String name;
	
	private PVector TCPOffset;
	
	private RQuaternion orienOffset;
	
	private final Point[] teachPoints;
	
	private PVector TCPDirect;
	private RQuaternion orienDirect;
	
	
	/**
	 * Initialize all fields to their default values.
	 */
	public ToolFrame() {
		name = "";
		TCPOffset = new PVector(0f, 0f, 0f);
		orienOffset = new RQuaternion();
		teachPoints = new Point[] { null, null, null, null, null, null };
		TCPDirect = new PVector(0f, 0f, 0f);
		orienDirect = new RQuaternion();
	}
	
	/**
	 * TODO comment this
	 * 
	 * @param name
	 * @param TCPOffset
	 * @param orienOffset
	 * @param teachPoints
	 * @param TCPDirect
	 * @param orienDirect
	 */
	public ToolFrame(String name, PVector TCPOffset, RQuaternion orienOffset,
			Point[] teachPoints, PVector TCPDirect, RQuaternion orienDirect) {
		
		this.name = name;
		this.TCPOffset = TCPOffset;
		this.orienOffset = orienOffset;
		this.teachPoints = teachPoints;
		this.TCPDirect = TCPDirect;
		this.orienDirect = orienDirect;
	}
	
	/**
	 * Similar to toStringArray, however, it converts the Frame's direct entry
	 * values instead of the current origin and axes of the Frame.
	 * 
	 * @returning  A 6x2-element String array
	 */
	public String[][] directEntryStringArray() {
		String[][] entries = new String[6][2];
		PVector TCPDirect = getTCPDirect();
		RQuaternion orienDirect = getOrienDirect();
		PVector xyz, wpr;

		if (TCPDirect == null) {
			xyz = new PVector(0f, 0f, 0f);
			
		} else {
			// Use previous value if it exists
			xyz = TCPDirect.copy();
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
	public RQuaternion getOrientationOffset() {
		return orienOffset;
	}
	
	/**
	 * TODO comment this
	 * 
	 * @param idx
	 * @return
	 */
	public Point getTeactPt(int idx) {
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
	public PVector getTCPDirect() {
		return TCPDirect;
	}
	
	/**
	 * TODO comment this
	 * 
	 * @return
	 */
	public PVector getTCPOffset() {
		return TCPOffset;
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
	public boolean is6PtComplete() {
		// Check if all points are taught for the six point method
		return teachPoints[0] != null && teachPoints[1] != null &&
				teachPoints[2] != null && teachPoints[3] != null &&
			   teachPoints[4] != null && teachPoints[5] != null;
	}
	

	/**
	 * TODO comment this
	 */
	public void reset() {
		name = "";
		TCPOffset.x = 0f;
		TCPOffset.y = 0f;
		TCPOffset.z = 0f;
		orienOffset.setValue(0, 1f);
		orienOffset.setValue(1, 0f);
		orienOffset.setValue(2, 0f);
		orienOffset.setValue(3, 0f);
		setTeachPt(null, 0);
		setTeachPt(null, 1);
		setTeachPt(null, 2);
		setTeachPt(null, 3);
		setTeachPt(null, 4);
		setTeachPt(null, 5);
		TCPDirect.x = 0f;
		TCPDirect.y = 0f;
		TCPDirect.z = 0f;
		orienDirect.setValue(0, 1f);
		orienDirect.setValue(1, 0f);
		orienDirect.setValue(2, 0f);
		orienDirect.setValue(3, 0f);
	}
	
	/**
	 * TODO comment this
	 * 
	 * @param newName
	 * @return
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
	 * @param p
	 * @param idx
	 */
	public void setTeachPt(Point p, int idx) {
		if (idx >= 0 && idx < teachPoints.length) {
			teachPoints[idx] = p;
		}
	}
	
	/**
	 * TODO comment this
	 * 
	 * @param newTCP
	 */
	public void setTCPDirect(PVector newTCP) {
		TCPDirect = newTCP;
	}
	
	/**
	 * TODO comment this
	 * 
	 * @param newOffset
	 */
	public void setTCPOffset(PVector newTCP) {
		TCPOffset = newTCP;
	}
	
	/**
	 * TODO comment this
	 * 
	 * @return
	 */
	public boolean teach3Pt() {
		if (is3PtComplete()) {
			Point pt0 = getTeactPt(0);
			Point pt1 = getTeactPt(1);
			Point pt2 = getTeactPt(2);
			
			RMatrix pt0Orien = pt0.orientation.toMatrix();
			RMatrix pt1Orien = pt1.orientation.toMatrix();
			RMatrix pt2Orien = pt2.orientation.toMatrix();
			// Calculate the TCP from the taught points
			double[] tcp = Fields.calculateTCPFromThreePoints(pt0.position,
					pt0Orien, pt1.position, pt1Orien, pt2.position, pt2Orien);
			
			if (tcp != null) {
				PVector TCPVec = new PVector((float)tcp[0], (float)tcp[1],
						(float)tcp[2]);
				// Set the frame offsets
				setTCPOffset(TCPVec);
				setOrienOffset(new RQuaternion());
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
	public boolean teach6Pt() {
		if (is6PtComplete()) {
			Point pt0 = getTeactPt(0);
			Point pt1 = getTeactPt(1);
			Point pt2 = getTeactPt(2);
			Point pt3 = getTeactPt(3);
			Point pt4 = getTeactPt(4);
			Point pt5 = getTeactPt(5);
			
			RMatrix pt0Orien = pt0.orientation.toMatrix();
			RMatrix pt1Orien = pt1.orientation.toMatrix();
			RMatrix pt2Orien = pt2.orientation.toMatrix();
			// Calculate the TCP from the taught points
			double[] tcp = Fields.calculateTCPFromThreePoints(pt0.position,
					pt0Orien, pt1.position, pt1Orien, pt2.position, pt2Orien);
			// Calculate the orientation offset from the taught points
			RMatrix axesOffsets = Fields.createAxesFromThreePoints(pt3.position,
					pt4.position, pt5.position);
			
			if (tcp != null && axesOffsets != null) {
				PVector TCPVec = new PVector((float)tcp[0], (float)tcp[1],
						(float)tcp[2]);
				// Set the frame offsets
				setTCPOffset(TCPVec);
				setOrienOffset( RMath.matrixToQuat(axesOffsets) );
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
		if (TCPDirect != null && orienDirect != null) {
			// Set frame offsets
			setTCPOffset(getTCPDirect().copy());
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
	 * the Tool frame's TCP offset or orientation values:
	 * (X, Y, Z, W, P, and R) and their respective labels.
	 *
	 * @return  A 6-element String array
	 */
	public String[] toStringArray() {
		String[] values = new String[6];

		PVector displayOffset = getTCPOffset();
		/* Convert orientation in to euler angles, in degree, with reference
		 * to the world frame */
		PVector wpr = RMath.nQuatToWEuler(orienOffset);

		values[0] = MyFloatFormat.format(displayOffset.x);
		values[1] = MyFloatFormat.format(displayOffset.y);
		values[2] = MyFloatFormat.format(displayOffset.z);
		values[3] = MyFloatFormat.format(wpr.x);
		values[4] = MyFloatFormat.format(wpr.y);
		values[5] = MyFloatFormat.format(wpr.z);

		return values;
	}
}
