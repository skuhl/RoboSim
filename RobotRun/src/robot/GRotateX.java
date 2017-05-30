package robot;

/**
 * Defines a rotation around the x-axis to apply to a graphics object.
 * 
 * @author Joshua Hooker
 */
public class GRotateX implements DrawAction {
	
	public final float rotation;
	
	public GRotateX(float rotX) {
		rotation = rotX;
	}
}
