package robot;

/**
 * Defines a rotation around the x-axis to apply to a graphics object.
 * 
 * @author Joshua Hooker
 */
public class GRotateX implements DrawAction {
	
	public final FloatWrapper ROTATION;
	
	public GRotateX(float rotX) {
		ROTATION = new FloatWrapper(rotX);
	}
	
	public GRotateX(FloatWrapper rotXWrapper) {
		ROTATION = rotXWrapper;
	}
}
