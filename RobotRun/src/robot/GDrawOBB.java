package robot;

import geom.BoundingBox;

/**
 * Defines a bounding box to be drawn with a graphics object.
 * 
 * @author Joshua Hooker
 */
public class GDrawOBB implements DrawAction {
	
	public final BoundingBox OBB;
	
	public GDrawOBB(BoundingBox obb) {
		this.OBB = obb;
	}
}
