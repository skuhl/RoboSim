package robot;

import java.util.ArrayList;

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
	public final float LOW_BOUND, UP_BOUND;
	
	/**
	 * Describes the direction and magnitude of the joint's motion.
	 */
	private float jointMotion;
	
	/**
	 * The rotation of this segment's joint.
	 */
	private float jointRotation;
	
	/**
	 * 
	 * 
	 * @param model
	 * @param obbs
	 * @param drawActions
	 * @param lb
	 * @param ub
	 */
	public RSegWithJoint(MyPShape model, ArrayList<BoundingBox> obbs,
			ArrayList<DrawAction> drawActions, float lb, float ub) {
		
		super(model, obbs, drawActions);
		
		LOW_BOUND = lb;
		UP_BOUND = ub;
		jointMotion = 0f;
		jointRotation = 0f;
	}
	
	public boolean anglePermitted(float angle) {
		return RMath.angleWithinBounds(angle, LOW_BOUND, UP_BOUND);
	}
	
	public float getJointRotation() {
		return jointRotation;
	}
	
	public boolean isJointInMotion() {
		return Math.abs(jointMotion) < 0.001f;
	}
	
	public void setJointMotion(float motion) {
		jointMotion = motion;
	}
	
	public void setJointRotation(float newRotation) {
		// Validate the given rotation
		if (anglePermitted(newRotation)) {
			jointRotation = newRotation;
		}
	}
	
	public void updateJoint() {
		// TODO
	}
}
