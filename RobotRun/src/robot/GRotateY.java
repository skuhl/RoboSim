package robot;

/**
 * Defines a rotation around the y-axis to apply to a graphics object.
 * 
 * @author Joshua Hooker
 */
public class GRotateY implements DrawAction {
	
	public final float rotation;
	
	public GRotateY(float rotY) {
		rotation = rotY;
	}
}
