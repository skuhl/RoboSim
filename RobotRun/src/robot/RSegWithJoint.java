package robot;


import geom.BoundingBox;
import geom.MyPShape;
import global.RMath;

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
	 * @param drawActions
	 * @param lb
	 * @param ub
	 */
	public RSegWithJoint(MyPShape model, BoundingBox[] obbs,
			DrawAction[] drawActions, float speed, float lb, float ub) {
		
		super(model, obbs, drawActions);
		
		LOW_BOUND = lb;
		UP_BOUND = ub;
		SPEED_MODIFIER = speed;
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
