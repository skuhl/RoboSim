package robot;


import geom.BoundingBox;
import global.RMath;
import processing.core.PConstants;
import processing.core.PShape;
import processing.core.PVector;

/**
 * An extension of the robot segment class, which includes the joint associated
 * with a majority of a robotic arm's segments.
 * 
 * @author Joshua Hooker
 */
public class RSegWithJoint extends RSegment {
	
	/**
	 * The bounds placed on this segment's joint rotation.
	 */
	public final float UP_BOUND;
	
	/**
	 * The lower bound of this segment joint's range of motion.
	 */
	public final float LOW_BOUND;
	
	/**
	 * The translation applied to move from the previous segment to this
	 * segment's position.
	 */
	protected final PVector TRANSLATION;
	
	/**
	 * The axis of rotation for this segment's joint
	 */
	protected final PVector AXIS;
	
	/**
	 * The speed modifier for this segment's joint.
	 */
	private float speedModifier;
	
	/**
	 * The rotation of this segment's joint.
	 */
	private float jointRotation;
	
	/**
	 * Creates a joint segment with the given mode, bounding boxes, joint
	 * bounds, offset and orientation.
	 * 
	 * @param model			The model associated with this segment
	 * @param obbs			The bounding boxes associated with this segment
	 * @param lb			The lower bound of this segment's joint range
	 * @param ub			The upper bound if this segment's joint range
	 * @param translation	The offset of the segment's joint axis with respect
	 * 						to its local coordinate system's origin
	 * @param axis			The orientation of the segment's joint axis with
	 * 						respect to its local orientation
	 */
	public RSegWithJoint(PShape model, BoundingBox[] obbs, float lb,
			float ub, PVector translation, PVector axis) {
		
		super(model, obbs);
		
		LOW_BOUND = lb;
		UP_BOUND = ub;
		speedModifier = 1f;
		TRANSLATION = translation;
		AXIS = axis;
		jointRotation = 0f;
	}
	
	/**
	 * Creates a joint segment with the given model, bounding boxes, joint axes
	 * offset and orientation. The segment will have full range of motion.
	 * 
	 * @param model			The model associated with this segment
	 * @param obbs			The bounding boxes associated with this segment
	 * @param translation	The offset of the segment's joint axis with respect
	 * 						to its local coordinate system's origin
	 * @param axis			The orientation of the segment's joint axis with
	 * 						respect to its local orientation
	 */
	public RSegWithJoint(PShape model, BoundingBox[] obbs,
			PVector translation, PVector axis) {
		
		this(model, obbs, 0f, PConstants.TWO_PI, translation, axis);
	}
	
	/**
	 * Determines if the given angle is valid based on the defined upper and
	 * lower bound of this segment's joint.
	 * 
	 * @param angle	The angle to validate
	 * @return		If the given angle is within the bounds of this segment
	 * 				joint's range of motion
	 */
	public boolean anglePermitted(float angle) {
		return RMath.angleWithinBounds(angle, LOW_BOUND, UP_BOUND);
	}
	
	public float getJointRotation() {
		return jointRotation;
	}
	
	public float getMotionSpeed() {
		return Math.abs(speedModifier);
	}
	
	public float getSpeedModifier() {
		return speedModifier;
	}
	
	public void setSpdMod(float speedMod) {
		speedModifier = speedMod;
	}
	
	/**
	 * Updates the joint angle of this segment to the given value, if its is
	 * within the bounds of the segment's joint range.
	 * 
	 * @param newRotation	The new joint rotation value
	 * @return				If the given value is within the bounds of the
	 * 						segment's joint range
	 */
	public boolean setJointRotation(float newRotation) {
		// Validate the given rotation
		if (anglePermitted(newRotation)) {
			jointRotation = newRotation;
			return true;
		}
		
		return false;
	}
}
