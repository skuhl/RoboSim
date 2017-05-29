package robot;

import enums.GTransform;
import geom.MyPShape;
import global.RMath;

/**
 * TODO comment this
 * 
 * @author Joshua Hooker
 */
public class RSegWithJoint extends RSegment {
	
	/**
	 * The axis of rotation for this segment's joint.
	 */
	private final GTransform ROTATION_AXIS;
	
	/**
	 * The bounds placed on this segment's joint rotation.
	 */
	private final float LOW_BOUND, UP_BOUND;
	
	/**
	 * The rotation of this segment's joint.
	 */
	private float jointRotation;
	
	public RSegWithJoint(MyPShape model, GTransform axisOfRotation, float lb, float ub) {
		super(model);
		ROTATION_AXIS = axisOfRotation;
		LOW_BOUND = lb;
		UP_BOUND = ub;
		jointRotation = 0f;
	}
	
	public boolean anglePermitted(float angle) {
		return RMath.angleWithinBounds(angle, LOW_BOUND, UP_BOUND);
	}
	
	public GTransform getAxisOfRotation() {
		return ROTATION_AXIS;
	}
	
	public float getJointRotation() {
		return jointRotation;
	}
	
	public void rotate(float delta) {
		jointRotation = RMath.mod2PI(jointRotation + delta);
	}
	
	public void setJointRotation(float newRotation) {
		if (anglePermitted(newRotation)) {
			jointRotation = newRotation;
		}
	}
	
}
