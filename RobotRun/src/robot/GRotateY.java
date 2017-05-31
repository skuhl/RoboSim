package robot;

/**
 * Defines a rotation around the y-axis to apply to a graphics object.
 * 
 * @author Joshua Hooker
 */
public class GRotateY implements DrawAction {
	
	public final FloatWrapper ROTATION;
	
	public GRotateY(float rotY) {
		ROTATION = new FloatWrapper(rotY);
	}
	
	public GRotateY(FloatWrapper rotYWrapper) {
		ROTATION = rotYWrapper;
	}
}
