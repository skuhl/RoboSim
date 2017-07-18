package frame;

import geom.Point;
import geom.RMatrix;
import geom.RQuaternion;
import global.Fields;
import global.MyFloatFormat;
import global.RMath;
import processing.core.PVector;

/**
 * Defines tool frames of a robotic arm.
 * 
 * @author Joshua Hooker
 */
public class ToolFrame {
	
	/**
	 * The name associated with this frame.
	 */
	private String name;
	
	/**
	 * The current TCP offset of this frame.
	 */
	private PVector TCPOffset;
	
	/**
	 * The current orientation offset of this frame.
	 */
	private RQuaternion orienOffset;
	
	/**
	 * The set of taught and untaught points for this frame's point teaching
	 * methods. A tool frame has a maximum of six points that can be taught
	 * (three for the TCP and three for the orientation).
	 */
	private final Point[] teachPoints;
	
	/**
	 * The last TCP taught with the direct entry method to this frame.
	 */
	private PVector TCPDirect;
	
	/**
	 * The last orientation offset taught with the direct entry method to this
	 * frame.
	 */
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
	 * Creates the frame defined by the given fields.
	 * 
	 * @param name			The name associated with the frame
	 * @param TCPOffset		The current TCP offset of the frame
	 * @param orienOffset	The current orientation offset of the frame
	 * @param teachPoints	The current set of taught and untaught points
	 * 						(must be of length 6)
	 * @param TCPDirect		The last TCP offset taught via a direct entry to
	 * 						the frame
	 * @param orienDirect	The last orientation offset taught via direct entry
	 * 						to the frame
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
	 * Getter method for this frame's name.
	 * 
	 * @return	This frame's name
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Returns the frame's orientation offset as a rotation matrix.
	 * 
	 * @return	The orientation offset of this frame
	 */
	public RMatrix getNativeAxisVectors() {
		return orienOffset.toMatrix();
	}
	
	/**
	 * Returns the last orientation taught to this frame via a direct entry.
	 * 
	 * @return	The last direct entry orientation offset
	 */
	public RQuaternion getOrienDirect() {
		return orienDirect;
	}
	
	/**
	 * Returns the current orientation offset of this frame.
	 * 
	 * @return	This frame's orientation offset
	 */
	public RQuaternion getOrientationOffset() {
		return orienOffset;
	}
	
	/**
	 * Returns the teach point of this frame associated with the given index.
	 * The index-point association is as follows:
	 * 
	 * 0-2	->	the TCP points
	 * 3	->	the orient origin point
	 * 4	->	the x direction point
	 * 5	->	the y direction point
	 * 
	 * If a point is untaught or if the given index is invalid, then null is
	 * returned.
	 * 
	 * @param idx	The index of a taught point
	 * @return		The point associated with the given index or null
	 */
	public Point getTeactPt(int idx) {
		if (idx >= 0 && idx < teachPoints.length) {
			return teachPoints[idx];
		}

		return null;
	}
	
	/**
	 * Returns the last TCP offset taught to this frame with the direct entry
	 * method.
	 * 
	 * @return	The last direct entry TCP offset
	 */
	public PVector getTCPDirect() {
		return TCPDirect;
	}
	
	/**
	 * The current TCP offset of this frame.
	 * 
	 * @return	This frame's TCP offset
	 */
	public PVector getTCPOffset() {
		return TCPOffset;
	}
	
	/**
	 * Determines if all the teach points for the TCP offset are taught for
	 * this frame.
	 * 
	 * @return	If the teach points for the TCP offset are taught
	 */
	public boolean is3PtComplete() {
		// Check if all points are taught for the three point method
		return teachPoints[0] != null && teachPoints[1] != null &&
				teachPoints[2] != null;
	}
	
	/**
	 * Determines if all the teach points for both the TCP and orientation
	 * offset are taught for this frame.
	 * 
	 * @return	If all teach points are taught
	 */
	public boolean is6PtComplete() {
		// Check if all points are taught for the six point method
		return teachPoints[0] != null && teachPoints[1] != null &&
				teachPoints[2] != null && teachPoints[3] != null &&
			   teachPoints[4] != null && teachPoints[5] != null;
	}
	

	/**
	 * Reinitializes ALL this frames fields to their default values.
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
	 * Updates this frames name.
	 * 
	 * @param newName	The new name for this frame
	 */
	public void setName(String newName) {
		name = newName;
	}
	
	/**
	 * Updates the last orientation direct entry taught to this frame.
	 * 
	 * @param newOrien	The latest orientation offset taught via the direct
	 * 					entry method
	 */
	public void setOrienDirect(RQuaternion newOrien) {
		orienDirect = newOrien;
	}
	
	/**
	 * Updates the orientation offset of this frame.
	 * 
	 * @param newOrien	The new orientation offset
	 */
	public void setOrienOffset(RQuaternion newOrien) {
		orienOffset = newOrien;
	}

	/**
	 * Updates a point taught to this frame.
	 * 
	 * @param pt		The new taught point
	 * @param idx		The index of the new taught point
	 */
	public void setTeachPt(Point pt, int idx) {
		if (idx >= 0 && idx < teachPoints.length) {
			teachPoints[idx] = pt;
		}
	}
	
	/**
	 * Updates the last TCP taught to this frame with the direct entry method.
	 * 
	 * @param newTCP	The latest TCP offset taught via the direct entry
	 * 					method
	 */
	public void setTCPDirect(PVector newTCP) {
		TCPDirect = newTCP;
	}
	
	/**
	 * Updates the TCP offset of this frame.
	 * 
	 * @param newOffset	The new TCP offset of this frame
	 */
	public void setTCPOffset(PVector newTCP) {
		TCPOffset = newTCP;
	}
	
	/**
	 * Updates the TCP of this frame based off the TCP teach points associated
	 * with this frame.
	 * 
	 * @return	If the frame was taught correctly with the three point method
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
	 * Updates the TCP and orientation offset of this frame based off the teach
	 * points associated with this frame.
	 * 
	 * @return	If the frame was taught successfully with the six point method
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
	 * Updates the TCP and orientation offset of this frame with the direct
	 * entry TCP and orientation offsets.
	 * 
	 * @return	If the frame was taught successfully with the direct entry
	 * 			method
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
