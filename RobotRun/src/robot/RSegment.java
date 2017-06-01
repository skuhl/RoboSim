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
	 * TODO comment this
	 * 
	 * @param p
	 * @return
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
	 * TODO comment this
	 * 
	 * @param ray
	 * @return
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
