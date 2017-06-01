package robot;


import geom.BoundingBox;
import geom.MyPShape;
import global.RMath;
import processing.core.PVector;

/**
 * TODO comment this
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
	 * The speed modifier for this segment's joint.
	 */
	public final float SPEED_MODIFIER;
	
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
	 * The direction of the joint's motion.
	 */
	private int jointMotion;
	
	/**
	 * The rotation of this segment's joint.
	 */
	private float jointRotation;
	
	/**
	 * TODO comment this
	 * 
	 * @param model
	 * @param obbs
	 * @param speed
	 * @param lb
	 * @param ub
	 * @param translation
	 * @param axis
	 */
	public RSegWithJoint(MyPShape model, BoundingBox[] obbs, float speed,
		float lb, float ub, PVector translation, PVector axis) {
		
		super(model, obbs);
		
		LOW_BOUND = lb;
		UP_BOUND = ub;
		SPEED_MODIFIER = speed;
		TRANSLATION = translation;
		AXIS = axis;
		jointMotion = 0;
		jointRotation = 0f;
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
	
	public int getJointMotion() {
		return jointMotion;
	}
	
	public float getJointRotation() {
		return jointRotation;
	}
	
	public float getMotionSpeed() {
		return Math.abs(jointMotion);
	}
	
	public boolean isJointInMotion() {
		return jointMotion != 0;
	}
	
	public void setJointMotion(int dir) {
		jointMotion = dir;
	}
	
	public boolean setJointRotation(float newRotation) {
		// Validate the given rotation
		if (anglePermitted(newRotation)) {
			jointRotation = newRotation;
			return true;
		}
		
		return false;
	}
}
