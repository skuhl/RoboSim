package robot;

/**
 * Defines a rotation around the z-axis to apply to a graphics object.
 * 
 * @author Joshua Hooker
 */
public class GRotateZ implements DrawAction {
	
	public final FloatWrapper ROTATION;
	
	public GRotateZ(float rotZ) {
		ROTATION = new FloatWrapper(rotZ);
	}
	
	public GRotateZ(FloatWrapper rotZWrapper) {
		ROTATION = rotZWrapper;
	}
}
