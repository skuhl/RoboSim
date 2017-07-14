package robot;

import geom.BoundingBox;
import geom.MyPShape;
import geom.Part;
import geom.RRay;
import global.Fields;
import processing.core.PVector;

/**
 * Defines a segment of the robotic arm with its bounding boxes and model set.
 * 
 * @author Joshua Hooker
 */
public class RSegment {
	
	/**
	 * The bounding boxes associated with this segment.
	 */
	protected final BoundingBox[] OBBS;
	
	/**
	 * The models defining the shape of this segment.
	 */
	protected final MyPShape[] MODEL_SET;
	
	/**
	 * Defines a segment with the given model, bounding boxes, and draw
	 * actions.
	 * 
	 * @param model			The model defining the shape of this segment
	 * @param obbs			The bounding boxes associated with this segment
	 */
	public RSegment(MyPShape model, BoundingBox[] obbs) {
		OBBS = obbs;
		this.MODEL_SET = new MyPShape[] { model };
	}
	
	/**
	 * Defines a segment with the given model set, bounding boxes, and draw
	 * actions.
	 * 
	 * @param MODEL			The models defining the shape of this segment
	 * @param obbs			The bounding boxes associated with this segment
	 */
	public RSegment(MyPShape[] modelSet, BoundingBox[] obbs) {
		OBBS = obbs;
		this.MODEL_SET = modelSet;
	}
	
	/**
	 * Checks if the given part's bounding box is intersecting with any of the
	 * bounding boxes associated with this robot segment, returning the number
	 * of boxes, with which the part's bounding box has intersections.
	 * 
	 * @param p	The part with which to check for collisions
	 * @return	The number of bounding boxes of this segment that intersect
	 * 			with the given part's bounding box
	 */
	public int checkCollision(Part p) {
		int ret = 0;
		
		for (BoundingBox obb : OBBS) {
			// set the color of the bounding box
			if (p.collision(obb)) {
				obb.setColor(Fields.OBB_COLLISION);
				ret = 1;
			}
			
		}
		
		return ret;
	}
	
	/**
	 * Checks if the given ray intersects with any of the bounding boxes
	 * associated with this robot segment. The position of the closest (if any)
	 * intersection between the given ray and a bounding box associated with
	 * this robot segment is returned or null if no intersection exists.
	 * 
	 * @param ray	The ray with which to check for collisions
	 * @return		The position of the closest intersection (if any)
	 */
	public PVector closestCollision(RRay ray) {
		PVector closestCollPt = null;
		
		for (BoundingBox obb : OBBS) {
			PVector collPt = obb.collision(ray);
			
			if (collPt != null && (closestCollPt == null ||
					PVector.dist(ray.getOrigin(), collPt) <
					PVector.dist(ray.getOrigin(), closestCollPt))) {
				
				// Find the closest collision to the ray origin
				closestCollPt = collPt;
			}
		}
		
		return closestCollPt;
	}
	
	/**
	 * Reset the color of the segment's bounding boxes to the default OBB
	 * color.
	 */
	public void resetOBBColors() {
		for (BoundingBox obb : OBBS) {
			obb.setColor(Fields.OBB_DEFAULT);
		}
	}
}
